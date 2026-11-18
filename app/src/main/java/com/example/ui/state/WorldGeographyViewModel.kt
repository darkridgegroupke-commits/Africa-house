package com.example.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Country
import com.example.data.model.County
import com.example.data.model.InternalRelocation
import com.example.data.model.StateRegion
import com.example.data.model.UserAccount
import com.example.data.repository.WorldGeographyRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class WorldAtlasSnapshot(
    val userAccount: UserAccount? = null,
    val countries: List<Country> = emptyList(),
    val statesForSelectedCountry: List<StateRegion> = emptyList(),
    val countiesForSelectedState: List<County> = emptyList(),
    val allCounties: List<County> = emptyList(),
    val relocations: List<InternalRelocation> = emptyList(),
)

data class RelocationFeedback(
    val isBusy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorldGeographyViewModel(
    private val repository: WorldGeographyRepository,
    private val currentUserId: String,
    initialDisplayName: String,
) : ViewModel() {

    private val _selectedCountryId = MutableStateFlow(101L)
    val selectedCountryId: StateFlow<Long> = _selectedCountryId.asStateFlow()

    private val _selectedStateId = MutableStateFlow(201L)
    val selectedStateId: StateFlow<Long> = _selectedStateId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _relocationFeedback = MutableStateFlow(RelocationFeedback())
    val relocationFeedback: StateFlow<RelocationFeedback> = _relocationFeedback.asStateFlow()

    private val _bootstrapError = MutableStateFlow<String?>(null)
    val bootstrapError: StateFlow<String?> = _bootstrapError.asStateFlow()

    init {
        viewModelScope.launch {
            val accountResult = repository.ensureUserAccount(currentUserId, initialDisplayName)
            if (accountResult.isFailure) {
                _bootstrapError.value = accountResult.exceptionOrNull()?.localizedMessage
                    ?: "Failed to initialize citizen account."
            }
            val seedResult = repository.ensureWorldSeeded()
            if (seedResult.isFailure) {
                _bootstrapError.value = seedResult.exceptionOrNull()?.localizedMessage
                    ?: "Failed to initialize African world registry."
            }
        }
    }

    private val statesFlow = _selectedCountryId.flatMapLatest { countryId ->
        if (countryId <= 0L) flowOf(emptyList())
        else repository.observeStatesForCountry(countryId)
    }

    private val countiesFlow = _selectedStateId.flatMapLatest { stateId ->
        if (stateId <= 0L) flowOf(emptyList())
        else repository.observeCountiesForState(stateId)
    }

    val worldState: StateFlow<UiState<WorldAtlasSnapshot>> = combine(
        repository.observeUserAccount(currentUserId),
        repository.observeCountries(),
        statesFlow,
        countiesFlow,
        repository.observeAllCounties(),
        repository.observeRelocations(currentUserId),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        WorldAtlasSnapshot(
            userAccount = values[0] as UserAccount?,
            countries = values[1] as List<Country>,
            statesForSelectedCountry = values[2] as List<StateRegion>,
            countiesForSelectedState = values[3] as List<County>,
            allCounties = values[4] as List<County>,
            relocations = values[5] as List<InternalRelocation>,
        )
    }
        .map<WorldAtlasSnapshot, UiState<WorldAtlasSnapshot>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing world geography", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load African world geography"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading,
        )

    fun selectCountry(countryId: Long) {
        _selectedCountryId.value = countryId
        _relocationFeedback.value = RelocationFeedback()
    }

    fun selectState(stateId: Long) {
        _selectedStateId.value = stateId
        _relocationFeedback.value = RelocationFeedback()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearRelocationFeedback() {
        _relocationFeedback.update { it.copy(message = null, isError = false) }
    }

    fun settleOrRelocate(
        countryId: Long,
        stateId: Long,
        countyId: Long,
        countyName: String,
        stateName: String,
    ) {
        viewModelScope.launch {
            _relocationFeedback.value = RelocationFeedback(isBusy = true)
            val result = repository.settleOrRelocateWithinCountry(
                userId = currentUserId,
                targetCountryId = countryId,
                targetStateId = stateId,
                targetCountyId = countyId,
                countyLabel = countyName,
                stateLabel = stateName,
            )
            result.fold(
                onSuccess = { msg ->
                    _relocationFeedback.value = RelocationFeedback(
                        isBusy = false,
                        message = msg,
                        isError = false,
                    )
                },
                onFailure = { err ->
                    _relocationFeedback.value = RelocationFeedback(
                        isBusy = false,
                        message = err.localizedMessage ?: "Unable to complete relocation.",
                        isError = true,
                    )
                },
            )
        }
    }

    fun retryBootstrap(displayName: String) {
        viewModelScope.launch {
            _bootstrapError.value = null
            repository.ensureUserAccount(currentUserId, displayName)
            val seedRes = repository.ensureWorldSeeded()
            if (seedRes.isFailure) {
                _bootstrapError.value = seedRes.exceptionOrNull()?.localizedMessage
                    ?: "Failed to synchronize African world registry."
            }
        }
    }

    private companion object {
        const val TAG = "WorldGeoVM"
    }
}
