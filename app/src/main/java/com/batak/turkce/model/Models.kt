package com.batak.turkce.model

import kotlinx.serialization.Serializable

@Serializable
enum class Suit(val labelTr: String, val symbol: String, val isRed: Boolean) {
    SPADES("Maça", "♠", false),
    HEARTS("Kupa", "♥", true),
    DIAMONDS("Karo", "♦", true),
    CLUBS("Sinek", "♣", false);

    companion object {
        val displayOrder: List<Suit> = listOf(SPADES, HEARTS, CLUBS, DIAMONDS)
    }
}

@Serializable
enum class Rank(val value: Int, val label: String) {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A");

    companion object {
        val byValue: Map<Int, Rank> = entries.associateBy { it.value }
    }
}

@Serializable
data class PlayingCard(val suit: Suit, val rank: Rank) {
    val label: String get() = rank.label + suit.symbol
}

@Serializable
enum class Difficulty(val labelTr: String) {
    EASY("Kolay"),
    NORMAL("Normal"),
    HARD("Zor")
}

@Serializable
enum class GameMode(val labelTr: String, val descriptionTr: String) {
    SOLO("Eşsiz (Tek)", "Herkes kendi ihalesini tutmaya çalışır. İhale 5'ten başlar."),
    PARTNERED("Eşli (2v2)", "Karşılıklı oturanlar eş olur. İhale 7'den başlar.")
}

enum class CardDesign(val labelTr: String) {
    KLASIK("Klasik"),
    MODERN("Modern")
}

enum class AppTheme(val labelTr: String) {
    DARK("Koyu Tema"),
    LIGHT("Açık Tema"),
    SYSTEM("Sistem")
}

object Seat {
    const val HUMAN = 0
    const val COUNT = 4
    val all = 0 until COUNT
}
