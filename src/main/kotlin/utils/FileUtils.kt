package utils

private val classLoader = object {}::class.java.classLoader

fun readInputLines(name: String): List<String> = readResourceStream("inputs/$name.txt").bufferedReader().readLines()

private fun readResourceStream(path: String) = requireNotNull(classLoader.getResourceAsStream(path)) { "Resource $path not found" }
