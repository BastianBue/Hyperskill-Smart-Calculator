fun main() {
    // write your code here
    val (a, b) = List(2){ readln().toBigInteger() }
    val (result , remainder) = a.divideAndRemainder(b)
    println("$a = $b * $result + $remainder")
}