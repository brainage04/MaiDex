package io.github.brainage04.maidex.data

/** English meanings that are explicitly verified instead of inferred from search aliases. */
object TitleMeaningMetadata {
    private val bySourceSongId = mapOf(
        "躯樹の墓守" to "The Gravekeeper of a Dead Tree",
    )

    fun forSong(sourceSongId: String): String = bySourceSongId[sourceSongId].orEmpty()
}
