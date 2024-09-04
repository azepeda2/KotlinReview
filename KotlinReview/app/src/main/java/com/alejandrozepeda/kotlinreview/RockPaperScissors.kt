package com.alejandrozepeda.kotlinreview

fun main() {
    var AIChoice = ""
    var PlayerChoice = ""
    var Winner = ""

    val randomValue =  (0..2).random()

    when (randomValue) {
        0 -> AIChoice = "ROCK"
        1 -> AIChoice = "PAPER"
        2 -> AIChoice = "SCISSORS"
    }

    println("Please choose rock paper or scissors")
    PlayerChoice = readln()
    PlayerChoice = PlayerChoice.trim().uppercase()
    println("Opoonent chose: $AIChoice")
    println("You chose: $PlayerChoice")


    Winner = when {
        PlayerChoice == AIChoice -> "Tie"
        PlayerChoice == "ROCK" && AIChoice == "SCISSORS" -> "Player"
        PlayerChoice == "PAPER" && AIChoice == "ROCK" -> "Player"
        PlayerChoice == "SCISSORS" && AIChoice == "PAPER" -> "Player"
        else -> "Opponent"
    }

    if (Winner == "Tie") {
        println("Its' a Tie!")
    } else {
        println("$Winner is the winner!")
    }
}
