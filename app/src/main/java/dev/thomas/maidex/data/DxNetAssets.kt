package dev.thomas.maidex.data

internal object DxNetAssets {
    private const val IMAGE_ROOT = "https://maimaidx-eng.com/maimai-mobile/img"
    private const val ASSET_VERSION = "1.65"
    private val titleRarities = setOf("normal", "bronze", "silver", "gold", "rainbow")

    const val starIconUrl = "$IMAGE_ROOT/icon_star.png"

    fun decorate(profile: PlayerProfile): PlayerProfile = profile.copy(
        titleBackgroundUrl = profile.titleBackgroundUrl.ifBlank { titleBackgroundUrl(profile.titleRarity) },
        ratingBaseUrl = profile.ratingBaseUrl.ifBlank { ratingBaseUrl(profile.officialRating) },
        starIconUrl = profile.starIconUrl.ifBlank { starIconUrl },
    )

    fun titleBackgroundUrl(rarity: String): String {
        val verifiedRarity = rarity.lowercase().takeIf(titleRarities::contains) ?: "normal"
        return "$IMAGE_ROOT/trophy_$verifiedRarity.png?ver=$ASSET_VERSION"
    }

    fun ratingBaseUrl(rating: Int): String {
        val color = when {
            rating >= 16_000 -> "rainbow_kiwami"
            rating >= 15_000 -> "rainbow"
            rating >= 14_500 -> "platinum"
            rating >= 14_000 -> "gold"
            rating >= 13_000 -> "silver"
            rating >= 12_000 -> "bronze"
            rating >= 10_000 -> "purple"
            rating >= 7_000 -> "red"
            rating >= 4_000 -> "orange"
            rating >= 2_000 -> "green"
            else -> "blue"
        }
        return "$IMAGE_ROOT/rating_base_$color.png?ver=$ASSET_VERSION"
    }
}
