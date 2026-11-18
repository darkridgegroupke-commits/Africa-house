package com.example.data.seed

import com.example.data.model.Country
import com.example.data.model.County
import com.example.data.model.StateRegion
import com.google.firebase.firestore.FieldValue

/**
 * Seeded Baseline Geography for House of Africa (Update 1).
 * 12 sovereign African countries (/world/101..112), 24 states/regions (/state/201..224),
 * and 28 counties with cities & settlements (/county/301..328).
 * Never invents live player counts or fake active company metrics.
 */
object AfricanWorldCatalog {

    const val SEED_LABEL: String = "Seeded Baseline Geography"

    val statutoryDepartments: List<String> = listOf(
        "Finance", "Immigration", "Interior", "Defence", "Agriculture",
        "Mining", "Energy", "Transport", "Health", "Education",
        "Infrastructure", "Trade", "Justice", "Environment",
    )

    val countries: List<Country> = listOf(
        Country(
            countryId = 101L, worldScope = "AFRICA", name = "Nigeria", isoCode = "NGA", flagEmoji = "🇳🇬",
            capitalCity = "Abuja", currencyCode = "NGN", currencyName = "Nigerian Naira",
            parliamentName = "National Assembly", parliamentSeats = 469L, population = 223800000L,
            infrastructureIndex = 64L, incomeTaxRatePct = 18.0, corporateTaxRatePct = 25.0,
            salesTaxRatePct = 7.5, immigrationRenewalYears = 10L,
            naturalResources = listOf("Crude Oil", "Natural Gas", "Tin", "Iron Ore", "Limestone"),
            industries = listOf("Energy", "Agriculture", "Telecommunications", "Banking & Finance", "Manufacturing"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 102L, worldScope = "AFRICA", name = "Kenya", isoCode = "KEN", flagEmoji = "🇰🇪",
            capitalCity = "Nairobi", currencyCode = "KES", currencyName = "Kenyan Shilling",
            parliamentName = "Parliament of Kenya", parliamentSeats = 416L, population = 55100000L,
            infrastructureIndex = 68L, incomeTaxRatePct = 20.0, corporateTaxRatePct = 30.0,
            salesTaxRatePct = 16.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Geothermal Energy", "Soda Ash", "Titanium", "Fluorspar"),
            industries = listOf("Technology", "Agriculture", "Tourism", "Transport", "Banking & Finance"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 103L, worldScope = "AFRICA", name = "South Africa", isoCode = "ZAF", flagEmoji = "🇿🇦",
            capitalCity = "Pretoria", currencyCode = "ZAR", currencyName = "South African Rand",
            parliamentName = "Parliament of South Africa", parliamentSeats = 490L, population = 60400000L,
            infrastructureIndex = 74L, incomeTaxRatePct = 22.0, corporateTaxRatePct = 27.0,
            salesTaxRatePct = 15.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Gold", "Platinum", "Chromium", "Coal", "Manganese"),
            industries = listOf("Mining", "Manufacturing", "Banking & Finance", "Retail", "Energy"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 104L, worldScope = "AFRICA", name = "Egypt", isoCode = "EGY", flagEmoji = "🇪🇬",
            capitalCity = "Cairo", currencyCode = "EGP", currencyName = "Egyptian Pound",
            parliamentName = "House of Representatives", parliamentSeats = 596L, population = 112700000L,
            infrastructureIndex = 71L, incomeTaxRatePct = 17.5, corporateTaxRatePct = 22.5,
            salesTaxRatePct = 14.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Natural Gas", "Crude Oil", "Phosphates", "Iron Ore"),
            industries = listOf("Transport", "Construction", "Tourism", "Food Processing", "Textiles"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 105L, worldScope = "AFRICA", name = "Ghana", isoCode = "GHA", flagEmoji = "🇬🇭",
            capitalCity = "Accra", currencyCode = "GHS", currencyName = "Ghanaian Cedi",
            parliamentName = "Parliament of Ghana", parliamentSeats = 275L, population = 34100000L,
            infrastructureIndex = 62L, incomeTaxRatePct = 17.5, corporateTaxRatePct = 25.0,
            salesTaxRatePct = 15.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Gold", "Cocoa", "Bauxite", "Timber", "Offshore Oil"),
            industries = listOf("Agriculture", "Mining", "Telecommunications", "Retail", "Energy"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 106L, worldScope = "AFRICA", name = "Ethiopia", isoCode = "ETH", flagEmoji = "🇪🇹",
            capitalCity = "Addis Ababa", currencyCode = "ETB", currencyName = "Ethiopian Birr",
            parliamentName = "House of Peoples' Representatives", parliamentSeats = 547L, population = 126500000L,
            infrastructureIndex = 58L, incomeTaxRatePct = 18.0, corporateTaxRatePct = 30.0,
            salesTaxRatePct = 15.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Hydroelectric Power", "Potash", "Tantalum", "Gold"),
            industries = listOf("Agriculture", "Energy", "Manufacturing", "Transport", "Construction"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 107L, worldScope = "AFRICA", name = "Morocco", isoCode = "MAR", flagEmoji = "🇲🇦",
            capitalCity = "Rabat", currencyCode = "MAD", currencyName = "Moroccan Dirham",
            parliamentName = "Parliament of Morocco", parliamentSeats = 515L, population = 37800000L,
            infrastructureIndex = 73L, incomeTaxRatePct = 19.0, corporateTaxRatePct = 25.0,
            salesTaxRatePct = 20.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Phosphates", "Solar Energy", "Cobalt", "Fisheries"),
            industries = listOf("Manufacturing", "Tourism", "Agriculture", "Energy", "Transport"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 108L, worldScope = "AFRICA", name = "Tanzania", isoCode = "TZA", flagEmoji = "🇹🇿",
            capitalCity = "Dodoma", currencyCode = "TZS", currencyName = "Tanzanian Shilling",
            parliamentName = "National Assembly of Tanzania", parliamentSeats = 393L, population = 67400000L,
            infrastructureIndex = 59L, incomeTaxRatePct = 18.0, corporateTaxRatePct = 30.0,
            salesTaxRatePct = 18.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Gold", "Natural Gas", "Tanzanite", "Nickel"),
            industries = listOf("Agriculture", "Mining", "Tourism", "Transport", "Construction"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 109L, worldScope = "AFRICA", name = "Senegal", isoCode = "SEN", flagEmoji = "🇸🇳",
            capitalCity = "Dakar", currencyCode = "XOF", currencyName = "West African CFA Franc",
            parliamentName = "National Assembly of Senegal", parliamentSeats = 165L, population = 17800000L,
            infrastructureIndex = 63L, incomeTaxRatePct = 18.0, corporateTaxRatePct = 28.0,
            salesTaxRatePct = 18.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Phosphates", "Zircon", "Offshore Gas", "Fisheries"),
            industries = listOf("Transport", "Agriculture", "Food Processing", "Tourism", "Energy"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 110L, worldScope = "AFRICA", name = "Rwanda", isoCode = "RWA", flagEmoji = "🇷🇼",
            capitalCity = "Kigali", currencyCode = "RWF", currencyName = "Rwandan Franc",
            parliamentName = "Parliament of Rwanda", parliamentSeats = 106L, population = 14100000L,
            infrastructureIndex = 70L, incomeTaxRatePct = 18.0, corporateTaxRatePct = 28.0,
            salesTaxRatePct = 18.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Tin", "Tungsten", "Tantalum", "Methane Gas"),
            industries = listOf("Technology", "Agriculture", "Tourism", "Construction", "Banking & Finance"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 111L, worldScope = "AFRICA", name = "Angola", isoCode = "AGO", flagEmoji = "🇦🇴",
            capitalCity = "Luanda", currencyCode = "AOA", currencyName = "Angolan Kwanza",
            parliamentName = "National Assembly of Angola", parliamentSeats = 220L, population = 36700000L,
            infrastructureIndex = 57L, incomeTaxRatePct = 17.0, corporateTaxRatePct = 25.0,
            salesTaxRatePct = 14.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Crude Oil", "Diamonds", "Iron Ore", "Copper"),
            industries = listOf("Energy", "Mining", "Transport", "Construction", "Agriculture"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
        Country(
            countryId = 112L, worldScope = "AFRICA", name = "Cote d'Ivoire", isoCode = "CIV", flagEmoji = "🇨🇮",
            capitalCity = "Yamoussoukro", currencyCode = "XOF", currencyName = "West African CFA Franc",
            parliamentName = "Parliament of Cote d'Ivoire", parliamentSeats = 354L, population = 28900000L,
            infrastructureIndex = 65L, incomeTaxRatePct = 18.0, corporateTaxRatePct = 25.0,
            salesTaxRatePct = 18.0, immigrationRenewalYears = 10L,
            naturalResources = listOf("Cocoa", "Gold", "Cashews", "Offshore Oil"),
            industries = listOf("Agriculture", "Food Processing", "Transport", "Energy", "Retail"),
            treasuryStatus = "National Treasury Live ($SEED_LABEL)", activePartiesCount = 0L,
        ),
    )

    private val defaultStateDepartments = listOf(
        "Finance", "Agriculture", "Infrastructure", "Trade", "Health", "Education", "Transport",
    )

    val states: List<StateRegion> = listOf(
        StateRegion(stateId = 201L, countryId = 101L, name = "Lagos State", capitalCity = "Ikeja", governorTitle = "Governor of Lagos State", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 76L, population = 16500000L, economicFocus = "Maritime Trade, Banking & Technology", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 202L, countryId = 101L, name = "Rivers State", capitalCity = "Port Harcourt", governorTitle = "Governor of Rivers State", departments = defaultStateDepartments, stateTaxRatePct = 4.0, infrastructureIndex = 65L, population = 7800000L, economicFocus = "Energy Refining, Port Logistics & Processing", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 203L, countryId = 102L, name = "Nairobi Region", capitalCity = "Nairobi", governorTitle = "Governor of Nairobi", departments = defaultStateDepartments, stateTaxRatePct = 5.0, infrastructureIndex = 78L, population = 5300000L, economicFocus = "East African Finance, Tech & Aviation", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 204L, countryId = 102L, name = "Coast Region", capitalCity = "Mombasa", governorTitle = "Governor of Coast Region", departments = defaultStateDepartments, stateTaxRatePct = 4.0, infrastructureIndex = 67L, population = 4600000L, economicFocus = "Deepwater Port Trade, Tourism & Logistics", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 205L, countryId = 103L, name = "Gauteng Province", capitalCity = "Johannesburg", governorTitle = "Premier of Gauteng", departments = defaultStateDepartments, stateTaxRatePct = 5.0, infrastructureIndex = 80L, population = 15800000L, economicFocus = "Mining Capital, Banking & Heavy Industry", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 206L, countryId = 103L, name = "Western Cape", capitalCity = "Cape Town", governorTitle = "Premier of Western Cape", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 82L, population = 7400000L, economicFocus = "Agri-Exports, Tech, Tourism & Ports", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 207L, countryId = 104L, name = "Cairo Governorate", capitalCity = "Cairo", governorTitle = "Governor of Cairo", departments = defaultStateDepartments, stateTaxRatePct = 4.0, infrastructureIndex = 74L, population = 10200000L, economicFocus = "Commerce, Media, Construction & Services", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 208L, countryId = 104L, name = "Alexandria Governorate", capitalCity = "Alexandria", governorTitle = "Governor of Alexandria", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 72L, population = 5500000L, economicFocus = "Mediterranean Shipping & Food Processing", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 209L, countryId = 105L, name = "Greater Accra", capitalCity = "Accra", governorTitle = "Regional Minister of Greater Accra", departments = defaultStateDepartments, stateTaxRatePct = 4.0, infrastructureIndex = 70L, population = 5450000L, economicFocus = "West African Trade, Telecoms & Finance", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 210L, countryId = 105L, name = "Ashanti Region", capitalCity = "Kumasi", governorTitle = "Regional Minister of Ashanti", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 62L, population = 5440000L, economicFocus = "Gold Mining, Cocoa & Timber Processing", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 211L, countryId = 106L, name = "Addis Ababa", capitalCity = "Addis Ababa", governorTitle = "Mayor of Addis Ababa", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 69L, population = 5200000L, economicFocus = "Continental Diplomacy, Aviation & Manufacturing", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 212L, countryId = 106L, name = "Oromia Region", capitalCity = "Adama", governorTitle = "President of Oromia", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 56L, population = 40800000L, economicFocus = "Highland Coffee, Agriculture & Hydro Energy", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 213L, countryId = 107L, name = "Casablanca-Settat", capitalCity = "Casablanca", governorTitle = "Wali of Casablanca-Settat", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 79L, population = 7600000L, economicFocus = "Atlantic Port Manufacturing, Aerospace & Banking", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 214L, countryId = 107L, name = "Tanger-Tetouan", capitalCity = "Tangier", governorTitle = "Wali of Tanger-Tetouan", departments = defaultStateDepartments, stateTaxRatePct = 4.0, infrastructureIndex = 77L, population = 3900000L, economicFocus = "Med Container Terminal, Automotive & Solar", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 215L, countryId = 108L, name = "Dar es Salaam Region", capitalCity = "Dar es Salaam", governorTitle = "Regional Commissioner of Dar", departments = defaultStateDepartments, stateTaxRatePct = 4.0, infrastructureIndex = 68L, population = 5800000L, economicFocus = "Indian Ocean Port, Rail Freight & Commerce", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 216L, countryId = 108L, name = "Arusha Region", capitalCity = "Arusha", governorTitle = "Regional Commissioner of Arusha", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 63L, population = 2400000L, economicFocus = "East African Diplomacy, Mining & Eco-Tourism", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 217L, countryId = 109L, name = "Dakar Region", capitalCity = "Dakar", governorTitle = "Governor of Dakar", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 70L, population = 4000000L, economicFocus = "Atlantic Gateway Port, Fintech & Agro-Industry", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 218L, countryId = 109L, name = "Thies Region", capitalCity = "Thies", governorTitle = "Governor of Thies", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 61L, population = 2200000L, economicFocus = "Phosphate Mining, Rail Hub & Horticulture", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 219L, countryId = 110L, name = "Kigali City", capitalCity = "Kigali", governorTitle = "Mayor of Kigali", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 75L, population = 1750000L, economicFocus = "Innovation Hub, Clean Energy & Conferences", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 220L, countryId = 110L, name = "Western Province", capitalCity = "Rubavu", governorTitle = "Governor of Western Province", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 64L, population = 2890000L, economicFocus = "Lake Kivu Methane Gas, Tea & Cross-Border Trade", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 221L, countryId = 111L, name = "Luanda Province", capitalCity = "Luanda", governorTitle = "Governor of Luanda", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 64L, population = 9100000L, economicFocus = "Offshore Energy Services, Port & Construction", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 222L, countryId = 111L, name = "Benguela Province", capitalCity = "Benguela", governorTitle = "Governor of Benguela", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 60L, population = 2800000L, economicFocus = "Lobito Corridor Rail, Minerals & Fisheries", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 223L, countryId = 112L, name = "Abidjan District", capitalCity = "Abidjan", governorTitle = "Minister-Governor of Abidjan", departments = defaultStateDepartments, stateTaxRatePct = 4.5, infrastructureIndex = 72L, population = 6300000L, economicFocus = "Cocoa Export Port, Refining & Regional Finance", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
        StateRegion(stateId = 224L, countryId = 112L, name = "Vallee du Bandama", capitalCity = "Bouake", governorTitle = "Regional President of Bandama", departments = defaultStateDepartments, stateTaxRatePct = 3.5, infrastructureIndex = 60L, population = 2100000L, economicFocus = "Inland Agro-Processing, Textiles & Cotton", treasuryStatus = "State Treasury Live ($SEED_LABEL)"),
    )

    val counties: List<County> = listOf(
        County(countyId = 301L, stateId = 201L, countryId = 101L, name = "Eti-Osa County", settlements = listOf("Victoria Island", "Ikoyi", "Lekki"), localServices = listOf("Deepwater Port Link", "County Registry", "Municipal Transit", "Clinics"), infrastructureIndex = 79L, population = 1450000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Coastal commercial, banking, and maritime hub"),
        County(countyId = 302L, stateId = 201L, countryId = 101L, name = "Ikeja County", settlements = listOf("Ikeja", "Alausa", "Ogba"), localServices = listOf("Aviation Cargo Terminal", "Industrial Grid", "Courts", "Public Schools"), infrastructureIndex = 75L, population = 1120000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "State capital, manufacturing parks, and tech corridor"),
        County(countyId = 303L, stateId = 202L, countryId = 101L, name = "Port Harcourt County", settlements = listOf("Old GRA", "Trans-Amadi", "Diobu"), localServices = listOf("Refinery Logistics", "Harbor Customs", "Power Substation"), infrastructureIndex = 68L, population = 1860000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Hydrocarbon processing and Delta marine freight"),
        County(countyId = 304L, stateId = 203L, countryId = 102L, name = "Westlands County", settlements = listOf("Westlands", "Kilimani", "Parklands"), localServices = listOf("Fiber Hub", "County Tax Desk", "Rapid Transit"), infrastructureIndex = 80L, population = 980000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Regional corporate headquarters and fintech cluster"),
        County(countyId = 305L, stateId = 204L, countryId = 102L, name = "Mombasa Island County", settlements = listOf("Mvita", "Kilindini", "Nyali"), localServices = listOf("Kilindini Container Port", "Rail Terminus", "Customs"), infrastructureIndex = 71L, population = 1280000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Indian Ocean gateway serving East & Central Africa"),
        County(countyId = 306L, stateId = 205L, countryId = 103L, name = "Sandton & Joburg Central", settlements = listOf("Sandton", "Rosebank", "Braamfontein"), localServices = listOf("Financial Clearing", "Gautrain Rail", "High-Voltage Grid"), infrastructureIndex = 83L, population = 2400000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Mining finance, stock exchange, and corporate services"),
        County(countyId = 307L, stateId = 206L, countryId = 103L, name = "Cape Town Metro County", settlements = listOf("City Bowl", "Century City", "Stellenbosch"), localServices = listOf("Table Bay Harbor", "Solar Microgrid", "Agri-Inspection"), infrastructureIndex = 84L, population = 1950000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Atlantic shipping, renewable energy, and agri-processing"),
        County(countyId = 308L, stateId = 207L, countryId = 104L, name = "New Cairo & Zamalek", settlements = listOf("New Cairo", "Zamalek", "Maadi"), localServices = listOf("Metro Transit", "Trade Courts", "Municipal Water"), infrastructureIndex = 77L, population = 2150000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Administrative commerce, construction, and telecoms"),
        County(countyId = 309L, stateId = 208L, countryId = 104L, name = "Alexandria Port County", settlements = listOf("El Raml", "Smouha", "Borg El Arab"), localServices = listOf("Mediterranean Port", "Food Silos", "Industrial Rail"), infrastructureIndex = 73L, population = 1750000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Maritime import-export and textile manufacturing"),
        County(countyId = 310L, stateId = 209L, countryId = 105L, name = "Accra Metro County", settlements = listOf("Osu", "Airport City", "Tema"), localServices = listOf("Tema Harbor Link", "County Clinics", "Digital Registry"), infrastructureIndex = 72L, population = 1650000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Gulf of Guinea trade port and commercial district"),
        County(countyId = 311L, stateId = 210L, countryId = 105L, name = "Kumasi Metro County", settlements = listOf("Adum", "Asokwa", "Suame"), localServices = listOf("Timber Exchange", "Artisan Industrial Park", "Transit Hub"), infrastructureIndex = 63L, population = 1480000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Inland cocoa logistics, gold trade, and workshops"),
        County(countyId = 312L, stateId = 211L, countryId = 106L, name = "Bole & Kirkos County", settlements = listOf("Bole", "Kirkos", "Kazanchis"), localServices = listOf("Air Cargo Hub", "Light Rail", "Diplomatic Precinct"), infrastructureIndex = 70L, population = 1320000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Pan-African institutional corridor and air logistics"),
        County(countyId = 313L, stateId = 212L, countryId = 106L, name = "Adama Valley County", settlements = listOf("Adama", "Bishoftu", "Mojo Dry Port"), localServices = listOf("Mojo Dry Port", "Hydro Grid", "Agri-Warehouses"), infrastructureIndex = 61L, population = 1850000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Dry-port rail freight, coffee processing, and textiles"),
        County(countyId = 314L, stateId = 213L, countryId = 107L, name = "Casablanca Anfa County", settlements = listOf("Anfa", "Maarif", "Ain Sebaa"), localServices = listOf("Casablanca Finance City", "Port Terminal", "Tramway"), infrastructureIndex = 81L, population = 2100000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "North African banking, automotive parts, and maritime cargo"),
        County(countyId = 315L, stateId = 214L, countryId = 107L, name = "Tanger-Med County", settlements = listOf("Tangier", "Ksar es-Seghir", "Fnideq"), localServices = listOf("Tanger-Med Container Hub", "Free Trade Zone", "High-Speed Rail"), infrastructureIndex = 79L, population = 1450000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Strait transshipment port and renewable-powered factories"),
        County(countyId = 316L, stateId = 215L, countryId = 108L, name = "Ilala & Kinondoni County", settlements = listOf("Kivukoni", "Msasani", "Kariakoo"), localServices = listOf("Dar Harbor", "SGR Rail Terminus", "Central Market"), infrastructureIndex = 69L, population = 2300000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Great Lakes maritime trade and commercial exchange"),
        County(countyId = 317L, stateId = 216L, countryId = 108L, name = "Arusha Central County", settlements = listOf("Arusha", "Njiro", "Tengeru"), localServices = listOf("EAC Corridor", "Mining Assay Office", "Regional Airport"), infrastructureIndex = 65L, population = 860000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Gemstone exchange, agribusiness, and regional services"),
        County(countyId = 318L, stateId = 217L, countryId = 109L, name = "Plateau & Almadies County", settlements = listOf("Dakar Plateau", "Almadies", "Diamniadio"), localServices = listOf("Autonomous Port of Dakar", "TER Rail", "Tech Park"), infrastructureIndex = 72L, population = 1780000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "West Atlantic freight, fintech, and agro-exports"),
        County(countyId = 319L, stateId = 218L, countryId = 109L, name = "Thies Central County", settlements = listOf("Thies", "Mbour", "Tivaouane"), localServices = listOf("Mineral Rail Spur", "Coastal Fisheries Hub", "Solar Grid"), infrastructureIndex = 62L, population = 1150000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Phosphate mining logistics and horticultural estates"),
        County(countyId = 320L, stateId = 219L, countryId = 110L, name = "Gasabo & Nyarugenge", settlements = listOf("Nyarugenge", "Kimihurura", "Masoro"), localServices = listOf("Kigali SEZ", "Innovation City", "Clean Bus Network"), infrastructureIndex = 76L, population = 1280000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Digital government services, light manufacturing, and banking"),
        County(countyId = 321L, stateId = 220L, countryId = 110L, name = "Rubavu Kivu County", settlements = listOf("Gisenyi", "Nyamyumba", "Mushonyi"), localServices = listOf("Methane Power Station", "Border Trade Post", "Lake Port"), infrastructureIndex = 66L, population = 720000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Lake energy extraction, coffee washing, and border trade"),
        County(countyId = 322L, stateId = 221L, countryId = 111L, name = "Ingombota & Talatona", settlements = listOf("Ingombota", "Talatona", "Viana"), localServices = listOf("Port of Luanda", "Industrial SEZ", "Energy Ministry Hub"), infrastructureIndex = 66L, population = 2600000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Offshore petroleum logistics and metropolitan construction"),
        County(countyId = 323L, stateId = 222L, countryId = 111L, name = "Lobito Corridor County", settlements = listOf("Lobito", "Benguela", "Catumbela"), localServices = listOf("Lobito Mineral Port", "Benguela Railway", "Cold Storage"), infrastructureIndex = 63L, population = 1100000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Transcontinental mineral rail export and coastal fisheries"),
        County(countyId = 324L, stateId = 223L, countryId = 112L, name = "Plateau & Treichville", settlements = listOf("Le Plateau", "Treichville", "Cocody"), localServices = listOf("Port of Abidjan", "Cocoa Silos", "BRVM Exchange"), infrastructureIndex = 74L, population = 2400000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Regional stock exchange, cocoa export docks, and energy"),
        County(countyId = 325L, stateId = 224L, countryId = 112L, name = "Bouake Central County", settlements = listOf("Bouake", "Sakassou", "Katiola"), localServices = listOf("Wholesale Produce Market", "Textile Mills", "Rail Yard"), infrastructureIndex = 61L, population = 1050000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Cashew & cotton processing and overland transit"),
        County(countyId = 326L, stateId = 201L, countryId = 101L, name = "Apapa Maritime County", settlements = listOf("Apapa", "Tin Can Island", "Festac"), localServices = listOf("Container Wharf", "Customs Clearing", "Tank Farms"), infrastructureIndex = 67L, population = 1090000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Primary West African bulk cargo and bonded warehousing"),
        County(countyId = 327L, stateId = 203L, countryId = 102L, name = "Embakasi Logistics County", settlements = listOf("Embakasi", "JKIA Freight", "Industrial Area"), localServices = listOf("Air Freight Hub", "SGR Inland Depot", "Manufacturing Grid"), infrastructureIndex = 74L, population = 1340000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "East African air cargo and industrial warehousing"),
        County(countyId = 328L, stateId = 205L, countryId = 103L, name = "Ekurhuleni Industrial", settlements = listOf("Germiston", "Kempton Park", "Boksburg"), localServices = listOf("OR Tambo Air Cargo", "Foundries", "Rail Freight"), infrastructureIndex = 79L, population = 2150000L, registeredBusinesses = 0L, marketplaceStatus = "TSN Exchange Active", economicSummary = "Heavy manufacturing, metal fabrication, and air freight"),
    )

    fun Country.toCreateMap(): Map<String, Any> = mapOf(
        "countryId" to countryId,
        "worldScope" to worldScope,
        "name" to name,
        "isoCode" to isoCode,
        "flagEmoji" to flagEmoji,
        "capitalCity" to capitalCity,
        "currencyCode" to currencyCode,
        "currencyName" to currencyName,
        "parliamentName" to parliamentName,
        "parliamentSeats" to parliamentSeats,
        "population" to population,
        "infrastructureIndex" to infrastructureIndex,
        "incomeTaxRatePct" to incomeTaxRatePct,
        "corporateTaxRatePct" to corporateTaxRatePct,
        "salesTaxRatePct" to salesTaxRatePct,
        "immigrationRenewalYears" to immigrationRenewalYears,
        "naturalResources" to naturalResources,
        "industries" to industries,
        "treasuryStatus" to treasuryStatus,
        "activePartiesCount" to activePartiesCount,
        "createdAt" to FieldValue.serverTimestamp(),
    )

    fun StateRegion.toCreateMap(): Map<String, Any> = mapOf(
        "stateId" to stateId,
        "countryId" to countryId,
        "name" to name,
        "capitalCity" to capitalCity,
        "governorTitle" to governorTitle,
        "departments" to departments,
        "stateTaxRatePct" to stateTaxRatePct,
        "infrastructureIndex" to infrastructureIndex,
        "population" to population,
        "economicFocus" to economicFocus,
        "treasuryStatus" to treasuryStatus,
        "createdAt" to FieldValue.serverTimestamp(),
    )

    fun County.toCreateMap(): Map<String, Any> = mapOf(
        "countyId" to countyId,
        "stateId" to stateId,
        "countryId" to countryId,
        "name" to name,
        "settlements" to settlements,
        "localServices" to localServices,
        "infrastructureIndex" to infrastructureIndex,
        "population" to population,
        "registeredBusinesses" to registeredBusinesses,
        "marketplaceStatus" to marketplaceStatus,
        "economicSummary" to economicSummary,
        "createdAt" to FieldValue.serverTimestamp(),
    )
}
