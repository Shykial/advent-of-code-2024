package utils

import kotlinx.coroutines.flow.flow

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

fun <T> MutableList<T>.swap(firstIndex: Int, secondIndex: Int) {
    set(firstIndex, set(secondIndex, get(firstIndex)))
}

inline fun <T, R> Pair<T, T>.map(transform: (T) -> R): Pair<R, R> = transform(first) to transform(second)

fun String.cutAt(index: Int): Pair<String, String> = take(index) to drop(index)

fun String.cutInHalf(): Pair<String, String> = cutAt(length / 2)

inline fun <T> Sequence<T>.takeWhileInclusive(crossinline predicate: (T) -> Boolean) = Sequence {
    object : Iterator<T> {
        private val originalIterator = this@takeWhileInclusive.iterator()
        private var predicateMet = true

        override fun next(): T = originalIterator.next().also { predicateMet = predicate(it) }

        override fun hasNext() = predicateMet && originalIterator.hasNext()
    }
}

inline fun <T> Sequence<T>.chunkedBy(crossinline keySelector: (T) -> Any?): Sequence<List<T>> {
    val iterator = iterator()
    if (!iterator.hasNext()) return emptySequence()
    val first = iterator.next()
    var currentAggregate = mutableListOf(first)
    var currentKey = keySelector(first)

    return sequence {
        iterator.forEach { element ->
            when (val key = keySelector(element)) {
                currentKey -> currentAggregate += element
                else -> {
                    yield(currentAggregate)
                    currentAggregate = mutableListOf(element)
                    currentKey = key
                }
            }
        }
        yield(currentAggregate)
    }
}

inline fun <T : Any> generateFlow(seed: T, crossinline supplier: suspend (T) -> T?) = flow {
    var current: T? = seed
    while (current != null) {
        emit(current)
        current = supplier(current)
    }
}
