package utils

object Regexes {
    val WhiteSpace = Regex("""\s+""")
    val NonNegativeDigits = Regex("""\d+""")
    val Digits = Regex("""-?\d+""")
}
