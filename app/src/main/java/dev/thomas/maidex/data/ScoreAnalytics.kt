package dev.thomas.maidex.data

private data class ScoredChart(
    val chart: SongChart,
    val score: UserScore,
)

data class MedalCountRow(
    val label: String,
    val counts: List<Int>,
)

data class MedalLevelTable(
    val title: String,
    val levels: List<String>,
    val rows: List<MedalCountRow>,
)

enum class BestScoreMetric(val label: String) {
    SSS_PLUS("Best SSS+"),
    FDX("Best FDX"),
    FDX_PLUS("Best FDX+"),
    AP("Best AP"),
    AP_PLUS("Best AP+"),
    DX_SCORE("Best DX score"),
}

data class BestScore(
    val metric: BestScoreMetric,
    val chart: SongChart?,
    val score: UserScore?,
)

data class ScoreAnalyticsSummary(
    val playedCharts: Int,
    val levelTables: List<MedalLevelTable>,
    val utage: MedalLevelTable,
    val bestScores: List<BestScore>,
)

object ScoreAnalytics {
    val standardLevelBands = listOf(
        listOf("13", "13+", "14", "14+", "15"),
        listOf("10", "10+", "11", "11+", "12", "12+"),
        listOf("7", "7+", "8", "8+", "9", "9+"),
        listOf("1", "2", "3", "4", "5", "6"),
    )
    val utageLevels = listOf("10?", "11?", "11+?", "12?", "12+?", "13?", "13+?", "14?", "14+?", "*")

    fun summarize(
        charts: List<SongChart>,
        scores: Map<String, UserScore>,
    ): ScoreAnalyticsSummary {
        val scoredCharts = charts.mapNotNull { chart ->
            scores[chart.chartKey]?.let { score -> ScoredChart(chart, score) }
        }
        return ScoreAnalyticsSummary(
            playedCharts = scoredCharts.size,
            levelTables = standardLevelBands.mapIndexed { index, levels ->
                medalTable(
                    title = "Levels ${levels.first()}–${levels.last()} · ${index + 1}/${standardLevelBands.size}",
                    levels = levels,
                    scoredCharts = scoredCharts,
                    levelOf = { scoredChart ->
                        scoredChart.chart.level.takeIf { level ->
                            scoredChart.chart.type != "utage" && level in levels
                        }
                    },
                )
            },
            utage = medalTable(
                title = "UTAGE estimated levels",
                levels = utageLevels,
                scoredCharts = scoredCharts,
                levelOf = {
                    if (it.chart.type != "utage") null
                    else it.chart.level?.takeIf(utageLevels::contains)
                },
            ),
            bestScores = bestScores(scoredCharts),
        )
    }

    private fun medalTable(
        title: String,
        levels: List<String>,
        scoredCharts: List<ScoredChart>,
        levelOf: (ScoredChart) -> String?,
    ): MedalLevelTable {
        val levelIndexes = levels.withIndex().associate { (index, level) -> level to index }
        val comboMedals = listOf(ComboMedal.FC, ComboMedal.FC_PLUS, ComboMedal.AP, ComboMedal.AP_PLUS)
        val syncMedals = listOf(SyncMedal.FS, SyncMedal.FS_PLUS, SyncMedal.FDX, SyncMedal.FDX_PLUS)
        val counts = Array(comboMedals.size + syncMedals.size) { IntArray(levels.size) }

        scoredCharts.forEach { scoredChart ->
            val levelIndex = levelIndexes[levelOf(scoredChart)] ?: return@forEach
            comboMedals.indexOf(scoredChart.score.comboMedal)
                .takeIf { it >= 0 }
                ?.let { counts[it][levelIndex]++ }
            syncMedals.indexOf(scoredChart.score.syncMedal)
                .takeIf { it >= 0 }
                ?.let { counts[comboMedals.size + it][levelIndex]++ }
        }

        val labels = comboMedals.map(ComboMedal::label) + syncMedals.map(SyncMedal::label)
        return MedalLevelTable(
            title = title,
            levels = levels,
            rows = labels.mapIndexed { index, label ->
                MedalCountRow(label, counts[index].toList())
            },
        )
    }

    private fun bestScores(scoredCharts: List<ScoredChart>): List<BestScore> {
        fun bestChart(predicate: (UserScore) -> Boolean): ScoredChart? = scoredCharts
            .asSequence()
            .filter { predicate(it.score) }
            .maxWithOrNull(
                compareBy<ScoredChart> { it.chart.effectiveLevel ?: Double.NEGATIVE_INFINITY }
                    .thenBy { it.score.achievement }
                    .thenBy { it.score.dxScorePercentage ?: Double.NEGATIVE_INFINITY },
            )

        fun result(metric: BestScoreMetric, scoredChart: ScoredChart?) = BestScore(
            metric = metric,
            chart = scoredChart?.chart,
            score = scoredChart?.score,
        )

        val bestDxScore = scoredCharts.maxWithOrNull(
            compareBy<ScoredChart> { it.score.dxScorePercentage ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.chart.effectiveLevel ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.score.dxScore },
        )
        return listOf(
            result(BestScoreMetric.SSS_PLUS, bestChart { it.grade == Grade.SSS_PLUS }),
            result(BestScoreMetric.FDX, bestChart { it.syncMedal == SyncMedal.FDX }),
            result(BestScoreMetric.FDX_PLUS, bestChart { it.syncMedal == SyncMedal.FDX_PLUS }),
            result(BestScoreMetric.AP, bestChart { it.comboMedal == ComboMedal.AP }),
            result(BestScoreMetric.AP_PLUS, bestChart { it.comboMedal == ComboMedal.AP_PLUS }),
            result(BestScoreMetric.DX_SCORE, bestDxScore),
        )
    }
}
