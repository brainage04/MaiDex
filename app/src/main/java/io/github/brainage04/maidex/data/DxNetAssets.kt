package io.github.brainage04.maidex.data

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

    fun gradeIconUrl(grade: Grade): String {
        val name = when (grade) {
            Grade.SSS_PLUS -> "sssp"
            Grade.SS_PLUS -> "ssp"
            Grade.S_PLUS -> "sp"
            else -> grade.name.lowercase()
        }
        return "$IMAGE_ROOT/music_icon_$name.png?ver=$ASSET_VERSION"
    }

    fun comboIconUrl(medal: ComboMedal): String = when (medal) {
        ComboMedal.NONE -> ""
        ComboMedal.FC -> "$IMAGE_ROOT/music_icon_fc.png?ver=$ASSET_VERSION"
        ComboMedal.FC_PLUS -> "$IMAGE_ROOT/music_icon_fcp.png?ver=$ASSET_VERSION"
        ComboMedal.AP -> "$IMAGE_ROOT/music_icon_ap.png?ver=$ASSET_VERSION"
        ComboMedal.AP_PLUS -> "$IMAGE_ROOT/music_icon_app.png?ver=$ASSET_VERSION"
    }

    fun syncIconUrl(medal: SyncMedal): String = when (medal) {
        SyncMedal.NONE -> ""
        SyncMedal.SYNC -> "$IMAGE_ROOT/music_icon_sync.png?ver=$ASSET_VERSION"
        SyncMedal.FS -> "$IMAGE_ROOT/music_icon_fs.png?ver=$ASSET_VERSION"
        SyncMedal.FS_PLUS -> "$IMAGE_ROOT/music_icon_fsp.png?ver=$ASSET_VERSION"
        SyncMedal.FDX -> "$IMAGE_ROOT/music_icon_fsd.png?ver=$ASSET_VERSION"
        SyncMedal.FDX_PLUS -> "$IMAGE_ROOT/music_icon_fsdp.png?ver=$ASSET_VERSION"
    }

    fun chartTypeIconUrl(type: String): String = when (type.lowercase()) {
        "dx" -> "$IMAGE_ROOT/music_dx.png?ver=$ASSET_VERSION"
        "std" -> "$IMAGE_ROOT/music_standard.png?ver=$ASSET_VERSION"
        else -> ""
    }


    fun dxStarIconUrl(stars: Int): String =
        stars.takeIf { it in 1..5 }
            ?.let { "$IMAGE_ROOT/playlog/dxstar_$it.png?ver=$ASSET_VERSION" }
            .orEmpty()

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
