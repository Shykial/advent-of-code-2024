package utils

fun <T> List<T>.minusIndex(index: Int) = filterIndexed { idx, _ -> idx != index }

fun <T> Iterable<T>.groupByCount(): Map<T, Int> = groupingBy { it }.eachCount()

fun <T> List<List<T>>.transpose(): List<List<T>> = first().indices.map { columnIndex -> map { it[columnIndex] } }

inline fun <T> Iterable<T>.splitBy(delimiterPredicate: (T) -> Boolean): List<List<T>> = buildList {
    var currentAggregate: MutableList<T>? = null
    this@splitBy.forEach { element ->
        if (delimiterPredicate(element)) {
            currentAggregate?.let { this += it }
            currentAggregate = null
        } else {
            if (currentAggregate == null) {
                currentAggregate = mutableListOf()
            }
            currentAggregate += element
        }
    }
    currentAggregate?.let { this += it }
}

fun <T> List<T>.pairCombinations(): Sequence<Pair<T, T>> =
    asSequence().flatMapIndexed { index, element ->
        (index + 1..lastIndex).asSequence().map { element to this[it] }
    }
