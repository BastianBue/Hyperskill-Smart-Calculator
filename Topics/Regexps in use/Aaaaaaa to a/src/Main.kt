fun main() {
    val text = readln()
    // write your code here
    println("[aA]+".toRegex().replace(text, "a"))
}