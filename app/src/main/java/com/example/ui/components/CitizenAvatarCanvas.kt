package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerCharacter
import com.example.ui.state.ActionFeedback
import com.example.ui.theme.AvatarClothEmerald
import com.example.ui.theme.AvatarClothKenteGold
import com.example.ui.theme.AvatarClothObsidianSuit
import com.example.ui.theme.AvatarClothRoyalIndigo
import com.example.ui.theme.AvatarClothSavannaTerracotta
import com.example.ui.theme.AvatarHairAuburn
import com.example.ui.theme.AvatarHairEspresso
import com.example.ui.theme.AvatarHairObsidian
import com.example.ui.theme.AvatarHairSilver
import com.example.ui.theme.AvatarHairSovereignGold
import com.example.ui.theme.AvatarSkinCaramel
import com.example.ui.theme.AvatarSkinChestnut
import com.example.ui.theme.AvatarSkinDeepUmber
import com.example.ui.theme.AvatarSkinHoney
import com.example.ui.theme.AvatarSkinMahogany
import com.example.ui.theme.AvatarSkinWarmSand
import com.example.ui.theme.LocalHoaColors

object AvatarOptionsCatalog {
    val skinToneNames = listOf("Deep Umber", "Mahogany", "Chestnut", "Caramel", "Honey", "Warm Sand")
    val hairStyles = listOf("SHORT_FADE", "AFRO_CROWN", "BRAIDS_LOCS", "HIJAB_WRAP", "ROYAL_CROWN", "BALD_CLEAN")
    val hairStyleLabels = mapOf(
        "SHORT_FADE" to "Short Fade",
        "AFRO_CROWN" to "Afro Crown",
        "BRAIDS_LOCS" to "Braids & Locs",
        "HIJAB_WRAP" to "Headwrap / Hijab",
        "ROYAL_CROWN" to "Sovereign Crown",
        "BALD_CLEAN" to "Clean Shaved",
    )
    val hairColorNames = listOf("Obsidian", "Espresso", "Auburn", "Silver", "Gold")
    val faceStyles = listOf("CONFIDENT", "WARM_SMILE", "STATESMAN", "SCHOLAR")
    val clothingStyles = listOf("KENTE_ROBE", "EMERALD_SUIT", "DASHIKI", "DIPLOMAT_COAT", "HERITAGE_WRAP")
    val clothingLabels = mapOf(
        "KENTE_ROBE" to "Kente Ceremonial",
        "EMERALD_SUIT" to "Emerald Suit",
        "DASHIKI" to "Modern Dashiki",
        "DIPLOMAT_COAT" to "Diplomat Coat",
        "HERITAGE_WRAP" to "Heritage Indigo",
    )
    val accessories = listOf("NONE", "GOLD_GLASSES", "SOVEREIGN_PIN", "HERITAGE_NECKLACE", "CREST_MEDALLION")
    val accessoryLabels = mapOf(
        "NONE" to "None",
        "GOLD_GLASSES" to "Gold Spectacles",
        "SOVEREIGN_PIN" to "Gold Lapel Pin",
        "HERITAGE_NECKLACE" to "Beaded Necklace",
        "CREST_MEDALLION" to "Republic Medallion",
    )
    val emblemStyles = listOf("ROYAL_KENTE", "EMERALD_CROWN", "SAVANNA_SUN", "CONTINENT_STAR")
}

@Composable
fun CitizenAvatarBadge(
    character: PlayerCharacter,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
) {
    CitizenAvatarBadge(
        avatarStyle = character.avatarStyle,
        accentIndex = character.avatarAccentIndex,
        skinToneIdx = character.avatarSkinToneIdx,
        hairStyle = character.avatarHairStyle,
        hairColorIdx = character.avatarHairColorIdx,
        faceStyle = character.avatarFaceStyle,
        clothingStyle = character.avatarClothingStyle,
        accessory = character.avatarAccessory,
        size = size,
        modifier = modifier,
    )
}

/**
 * Multi-layer custom Canvas citizen portrait & sovereign crest renderer.
 * All colors originate strictly from theme Color.kt palettes and MaterialTheme tokens.
 */
@Composable
fun CitizenAvatarBadge(
    avatarStyle: String,
    accentIndex: Int,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
    skinToneIdx: Int = 2,
    hairStyle: String = "SHORT_FADE",
    hairColorIdx: Int = 0,
    faceStyle: String = "CONFIDENT",
    clothingStyle: String = "KENTE_ROBE",
    accessory: String = "NONE",
) {
    val hoaColors = LocalHoaColors.current
    val primary = MaterialTheme.colorScheme.primary
    val gold = hoaColors.goldAccent
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val accentPalette = listOf(gold, primary, secondary, tertiary)
    val accent = accentPalette[accentIndex.coerceIn(0, accentPalette.lastIndex)]

    val skinPalette = listOf(
        AvatarSkinDeepUmber,
        AvatarSkinMahogany,
        AvatarSkinChestnut,
        AvatarSkinCaramel,
        AvatarSkinHoney,
        AvatarSkinWarmSand,
    )
    val hairPalette = listOf(
        AvatarHairObsidian,
        AvatarHairEspresso,
        AvatarHairAuburn,
        AvatarHairSilver,
        AvatarHairSovereignGold,
    )
    val skinColor = skinPalette[skinToneIdx.coerceIn(0, skinPalette.lastIndex)]
    val hairColor = hairPalette[hairColorIdx.coerceIn(0, hairPalette.lastIndex)]
    val clothColor = when (clothingStyle) {
        "EMERALD_SUIT" -> AvatarClothEmerald
        "DASHIKI" -> AvatarClothSavannaTerracotta
        "DIPLOMAT_COAT" -> AvatarClothObsidianSuit
        "HERITAGE_WRAP" -> AvatarClothRoyalIndigo
        else -> AvatarClothKenteGold
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) / 2f

        // Background sovereign medallion backdrop
        drawCircle(color = hoaColors.forestHeader, radius = r)
        drawCircle(color = accent, radius = r * 0.92f, style = Stroke(width = r * 0.065f))

        // Clothing / Shoulders silhouette
        val shoulders = Path().apply {
            moveTo(cx - r * 0.62f, cy + r * 0.72f)
            quadraticTo(cx - r * 0.56f, cy + r * 0.20f, cx, cy + r * 0.18f)
            quadraticTo(cx + r * 0.56f, cy + r * 0.20f, cx + r * 0.62f, cy + r * 0.72f)
            close()
        }
        drawPath(shoulders, color = clothColor)

        // Kente or lapel trim detail
        val trimColor = if (clothingStyle == "KENTE_ROBE") AvatarClothEmerald else gold
        drawLine(
            color = trimColor,
            start = Offset(cx - r * 0.16f, cy + r * 0.22f),
            end = Offset(cx, cy + r * 0.64f),
            strokeWidth = r * 0.06f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = trimColor,
            start = Offset(cx + r * 0.16f, cy + r * 0.22f),
            end = Offset(cx, cy + r * 0.64f),
            strokeWidth = r * 0.06f,
            cap = StrokeCap.Round,
        )

        // Neck
        drawRoundRect(
            color = skinColor,
            topLeft = Offset(cx - r * 0.12f, cy + r * 0.04f),
            size = Size(r * 0.24f, r * 0.24f),
            cornerRadius = CornerRadius(r * 0.06f),
        )

        // Hair back layer (for braids/afro/headwrap)
        when (hairStyle) {
            "AFRO_CROWN" -> {
                drawCircle(color = hairColor, radius = r * 0.41f, center = Offset(cx, cy - r * 0.22f))
            }
            "BRAIDS_LOCS" -> {
                drawRoundRect(
                    color = hairColor,
                    topLeft = Offset(cx - r * 0.36f, cy - r * 0.50f),
                    size = Size(r * 0.72f, r * 0.74f),
                    cornerRadius = CornerRadius(r * 0.28f),
                )
            }
            "HIJAB_WRAP" -> {
                drawCircle(color = clothColor, radius = r * 0.42f, center = Offset(cx, cy - r * 0.16f))
                drawCircle(color = gold, radius = r * 0.42f, center = Offset(cx, cy - r * 0.16f), style = Stroke(width = r * 0.03f))
            }
        }

        // Head / Face
        drawCircle(color = skinColor, radius = r * 0.31f, center = Offset(cx, cy - r * 0.14f))

        // Hair top layer
        when (hairStyle) {
            "SHORT_FADE" -> {
                drawArc(
                    color = hairColor,
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = true,
                    topLeft = Offset(cx - r * 0.31f, cy - r * 0.46f),
                    size = Size(r * 0.62f, r * 0.40f),
                )
            }
            "ROYAL_CROWN" -> {
                val crown = Path().apply {
                    moveTo(cx - r * 0.28f, cy - r * 0.36f)
                    lineTo(cx - r * 0.28f, cy - r * 0.58f)
                    lineTo(cx - r * 0.12f, cy - r * 0.44f)
                    lineTo(cx, cy - r * 0.66f)
                    lineTo(cx + r * 0.12f, cy - r * 0.44f)
                    lineTo(cx + r * 0.28f, cy - r * 0.58f)
                    lineTo(cx + r * 0.28f, cy - r * 0.36f)
                    close()
                }
                drawPath(crown, color = gold)
            }
        }

        // Eyes & Expression
        val eyeY = cy - r * 0.16f
        val eyeOffset = r * 0.11f
        drawCircle(color = AvatarHairObsidian, radius = r * 0.032f, center = Offset(cx - eyeOffset, eyeY))
        drawCircle(color = AvatarHairObsidian, radius = r * 0.032f, center = Offset(cx + eyeOffset, eyeY))

        // Brows (Statesman & Scholar look more focused)
        val browOffsetY = if (faceStyle == "STATESMAN") r * 0.06f else r * 0.075f
        drawLine(
            color = hairColor,
            start = Offset(cx - eyeOffset - r * 0.05f, eyeY - browOffsetY),
            end = Offset(cx - eyeOffset + r * 0.05f, eyeY - browOffsetY),
            strokeWidth = r * 0.025f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = hairColor,
            start = Offset(cx + eyeOffset - r * 0.05f, eyeY - browOffsetY),
            end = Offset(cx + eyeOffset + r * 0.05f, eyeY - browOffsetY),
            strokeWidth = r * 0.025f,
            cap = StrokeCap.Round,
        )

        // Mouth / Expression
        val mouthY = cy - r * 0.01f
        when (faceStyle) {
            "WARM_SMILE" -> {
                drawArc(
                    color = AvatarHairEspresso,
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.10f, mouthY - r * 0.04f),
                    size = Size(r * 0.20f, r * 0.11f),
                    style = Stroke(width = r * 0.028f, cap = StrokeCap.Round),
                )
            }
            "STATESMAN" -> {
                drawLine(
                    color = AvatarHairEspresso,
                    start = Offset(cx - r * 0.08f, mouthY),
                    end = Offset(cx + r * 0.08f, mouthY),
                    strokeWidth = r * 0.028f,
                    cap = StrokeCap.Round,
                )
            }
            else -> {
                drawArc(
                    color = AvatarHairEspresso,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.08f, mouthY - r * 0.03f),
                    size = Size(r * 0.16f, r * 0.07f),
                    style = Stroke(width = r * 0.025f, cap = StrokeCap.Round),
                )
            }
        }

        // Accessories
        when (accessory) {
            "GOLD_GLASSES" -> {
                drawCircle(
                    color = gold,
                    radius = r * 0.072f,
                    center = Offset(cx - eyeOffset, eyeY),
                    style = Stroke(width = r * 0.022f),
                )
                drawCircle(
                    color = gold,
                    radius = r * 0.072f,
                    center = Offset(cx + eyeOffset, eyeY),
                    style = Stroke(width = r * 0.022f),
                )
                drawLine(
                    color = gold,
                    start = Offset(cx - eyeOffset + r * 0.07f, eyeY),
                    end = Offset(cx + eyeOffset - r * 0.07f, eyeY),
                    strokeWidth = r * 0.022f,
                )
            }
            "SOVEREIGN_PIN" -> {
                drawCircle(color = gold, radius = r * 0.06f, center = Offset(cx + r * 0.24f, cy + r * 0.36f))
            }
            "HERITAGE_NECKLACE" -> {
                for (i in -2..2) {
                    drawCircle(
                        color = if (i % 2 == 0) gold else AvatarClothSavannaTerracotta,
                        radius = r * 0.03f,
                        center = Offset(cx + i * r * 0.07f, cy + r * 0.24f + kotlin.math.abs(i) * r * -0.015f),
                    )
                }
            }
            "CREST_MEDALLION" -> {
                val badge = Path().apply {
                    moveTo(cx, cy + r * 0.30f)
                    lineTo(cx + r * 0.08f, cy + r * 0.42f)
                    lineTo(cx, cy + r * 0.54f)
                    lineTo(cx - r * 0.08f, cy + r * 0.42f)
                    close()
                }
                drawPath(badge, color = gold)
            }
        }

        // Subtle sovereign emblem pip at bottom rim matching avatarStyle
        val emblemCenter = Offset(cx, cy + r * 0.77f)
        when (avatarStyle) {
            "SAVANNA_SUN" -> drawCircle(color = gold, radius = r * 0.07f, center = emblemCenter)
            "CONTINENT_STAR" -> drawCircle(color = accent, radius = r * 0.07f, center = emblemCenter)
            "EMERALD_CROWN" -> drawCircle(color = hoaColors.goldAccent, radius = r * 0.065f, center = emblemCenter)
        }
    }
}

@Composable
fun ActionFeedbackBanner(
    feedback: ActionFeedback,
    modifier: Modifier = Modifier,
) {
    if (!feedback.isBusy && feedback.message.isNullOrBlank()) return
    val bgColor: Color = if (feedback.isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val fgColor: Color = if (feedback.isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        border = BorderStroke(1.dp, fgColor.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (feedback.isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = fgColor,
                    strokeWidth = 2.dp,
                )
            }
            Text(
                text = feedback.message ?: "Processing...",
                style = MaterialTheme.typography.bodySmall,
                color = fgColor,
            )
        }
    }
}
