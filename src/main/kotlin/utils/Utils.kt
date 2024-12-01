package utils

fun <T> List<T>.minusIndex(index: Int) = filterIndexed { idx, _ -> idx != index }

fun <T> Iterable<T>.groupByCount(): Map<T, Int> = groupingBy { it }.eachCount()

fun <T> List<List<T>>.transpose(): List<List<T>> = first().indices.map { columnIndex -> map { it[columnIndex] } }
