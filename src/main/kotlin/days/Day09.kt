package days

import utils.map
import utils.readInput
import utils.swap

// TODO find more performant solution + tidy up
object Day09 {
    fun part1(input: String): Long = input.toInts().defragmentBitByBit().checkSum()

    fun part2(input: String): Long = input.toInts().defragmentFileByFile().checkSum()

    private fun List<Int>.defragmentBitByBit(): List<Int> {
        val aggregate = individualDiskEntries().toMutableList()
        val (fileBlockIndexes, freeSpaceIndexes) =
            aggregate.withIndex().partition { it.value is DiskEntry.FileBlock }
                .map { it.map { it.index } }

        freeSpaceIndexes.asSequence()
            .zip(fileBlockIndexes.asReversed().asSequence())
            .takeWhile {
                val indexOfLast = aggregate.indexOfLast { it is DiskEntry.FileBlock }
                val indexOfFirst = aggregate.indexOfFirst { it is DiskEntry.FreeSpace }
                indexOfLast > indexOfFirst
            }.forEach { (freeSpaceIndex, fileBlockIndex) ->
                aggregate.swap(freeSpaceIndex, fileBlockIndex)
            }

        return aggregate
            .takeWhile { it is DiskEntry.FileBlock }
            .map { (it as DiskEntry.FileBlock).id }
    }

    private fun List<Int>.individualDiskEntries(): List<DiskEntry> = buildList {
        var isFileBlock = true
        var id = 0
        this@individualDiskEntries.forEach {
            this += when (isFileBlock) {
                true -> List(it) { DiskEntry.FileBlock(id) }.also { id++ }
                false -> List(it) { DiskEntry.FreeSpace }
            }
            isFileBlock = !isFileBlock
        }
    }

    private sealed interface DiskEntry {
        data class FileBlock(val id: Int) : DiskEntry
        data object FreeSpace : DiskEntry
    }

    private data class GroupedEntries(
        val groups: List<EntriesGroup>,
        val lastFileBlockId: Int,
    )

    private data class EntriesGroup(val entry: DiskEntry, val count: Int)

    private fun List<Int>.defragmentFileByFile(): List<EntriesGroup> {
        val (entriesGroups, lastFileBlockId) = entriesGroups()
        return entriesGroups.toMutableList().apply { defragmentFileByFile(lastFileBlockId) }
    }

    private fun MutableList<EntriesGroup>.defragmentFileByFile(lastFileBlockId: Int) {
        (lastFileBlockId downTo 0).asSequence()
            .map { id ->
                withIndex().first { (it.value.entry as? DiskEntry.FileBlock)?.id == id }
            }.mapNotNull { indexedFileBlock ->
                findFirstFreeSpaceGroupOrNull(indexedFileBlock)
            }.forEach { (fileBlockGroup, freeSpaceGroup) ->
                defragmentEntriesGroup(fileBlockGroup, freeSpaceGroup)
            }
    }

    private fun List<Int>.entriesGroups(): GroupedEntries {
        var isFileBlock = true
        var currentId = 0
        val groups = map { count ->
            when (isFileBlock) {
                true -> EntriesGroup(DiskEntry.FileBlock(currentId++), count)
                else -> EntriesGroup(DiskEntry.FreeSpace, count)
            }.also { isFileBlock = !isFileBlock }
        }
        return GroupedEntries(groups, currentId - 1)
    }

    private fun List<EntriesGroup>.findFirstFreeSpaceGroupOrNull(
        indexedFileBlock: IndexedValue<EntriesGroup>,
    ): Pair<IndexedValue<EntriesGroup>, IndexedValue<EntriesGroup>>? = asSequence()
        .withIndex()
        .take(indexedFileBlock.index)
        .firstOrNull { it.value.entry is DiskEntry.FreeSpace && it.value.count >= indexedFileBlock.value.count }
        ?.let { indexedFileBlock to it }

    private fun MutableList<EntriesGroup>.defragmentEntriesGroup(
        fileBlockGroup: IndexedValue<EntriesGroup>,
        freeSpaceGroup: IndexedValue<EntriesGroup>,
    ) {
        when {
            freeSpaceGroup.value.count == fileBlockGroup.value.count -> swap(fileBlockGroup.index, freeSpaceGroup.index)

            freeSpaceGroup.value.count > fileBlockGroup.value.count -> {
                this[freeSpaceGroup.index] = fileBlockGroup.value
                this[fileBlockGroup.index] = EntriesGroup(DiskEntry.FreeSpace, fileBlockGroup.value.count)
                add(
                    freeSpaceGroup.index + 1,
                    EntriesGroup(DiskEntry.FreeSpace, freeSpaceGroup.value.count - fileBlockGroup.value.count),
                )
            }
        }
    }

    private fun String.toInts() = map { it.digitToInt() }

    private fun List<Int>.checkSum() = withIndex().sumOf { (index, value) -> index * value.toLong() }

    @JvmName("entriesGroupsCheckSum")
    private fun List<EntriesGroup>.checkSum(): Long {
        var effectiveIndex = 0
        return sumOf { (entry, count) ->
            when (entry) {
                is DiskEntry.FileBlock -> (effectiveIndex..<effectiveIndex + count).sumOf { it * entry.id.toLong() }
                DiskEntry.FreeSpace -> 0L
            }.also { effectiveIndex += count }
        }
    }
}

fun main() {
    val input = readInput("Day09")
    println("part1: ${Day09.part1(input)}")
    println("part2: ${Day09.part2(input)}")
}
