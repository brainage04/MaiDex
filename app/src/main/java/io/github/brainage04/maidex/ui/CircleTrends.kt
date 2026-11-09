package io.github.brainage04.maidex.ui

import io.github.brainage04.maidex.data.CircleDailySnapshot

/** One circle member's points across the daily snapshots, oldest day first. */
internal data class MemberTrend(
    val key: String,
    val name: String,
    val isCurrentUser: Boolean,
    val points: List<Pair<String, Int>>,
)

/**
 * Every member seen in [snapshots] with their daily points: the signed-in player first, then the
 * others by their latest points, highest first. Days a member was absent are skipped.
 */
internal fun circleMemberTrends(snapshots: List<CircleDailySnapshot>): List<MemberTrend> {
    val members = snapshots.asSequence()
        .flatMap { it.circle.members.asSequence() }
        .distinctBy { it.key }
        .toList()
    val trends = members.map { member ->
        MemberTrend(
            key = member.key,
            name = snapshots.lastNotNullOf { snapshot -> snapshot.circle.members.firstOrNull { it.key == member.key }?.name },
            isCurrentUser = snapshots.any { snapshot ->
                snapshot.circle.members.any { it.key == member.key && it.isCurrentUser }
            },
            points = snapshots.mapNotNull { snapshot ->
                snapshot.circle.members.firstOrNull { it.key == member.key }?.let { snapshot.day to it.points }
            },
        )
    }
    return trends.sortedWith(
        compareByDescending<MemberTrend> { it.isCurrentUser }.thenByDescending { it.points.lastOrNull()?.second ?: 0 },
    )
}

private inline fun <T, R : Any> List<T>.lastNotNullOf(transform: (T) -> R?): R =
    asReversed().firstNotNullOf(transform)
