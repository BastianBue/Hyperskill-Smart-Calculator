fun main() {
    // write your code here
    val (a, b) = List(2) { readln().toBigInteger() }
    println(if (a < b) b else a)
}