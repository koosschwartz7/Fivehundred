package za.co.kschwartz.fivehundreds.domain

import android.graphics.drawable.Icon
import za.co.kschwartz.fivehundreds.R

class Card(suit: Suit = Suit.NULLSUIT, value: Int = 0) {
    val suit: Suit = suit
    val value: Int = value
    var imgResId = R.drawable.back;

    object Icons {
        val map: HashMap<String, Int> = hashMapOf(
                "spades_5" to R.drawable.spades_5,
                "spades_6" to R.drawable.spades_6,
                "spades_7" to R.drawable.spades_7,
                "spades_8" to R.drawable.spades_8,
                "spades_9" to R.drawable.spades_9,
                "spades_10" to R.drawable.spades_10,
                "spades_11" to R.drawable.spades_11,
                "spades_12" to R.drawable.spades_12,
                "spades_13" to R.drawable.spades_13,
                "spades_14" to R.drawable.spades_14,
                "clubs_5" to R.drawable.clubs_5,
                "clubs_6" to R.drawable.clubs_6,
                "clubs_7" to R.drawable.clubs_7,
                "clubs_8" to R.drawable.clubs_8,
                "clubs_9" to R.drawable.clubs_9,
                "clubs_10" to R.drawable.clubs_10,
                "clubs_11" to R.drawable.clubs_11,
                "clubs_12" to R.drawable.clubs_12,
                "clubs_13" to R.drawable.clubs_13,
                "clubs_14" to R.drawable.clubs_14,
                "diamonds_4" to R.drawable.diamonds_4,
                "diamonds_5" to R.drawable.diamonds_5,
                "diamonds_6" to R.drawable.diamonds_6,
                "diamonds_7" to R.drawable.diamonds_7,
                "diamonds_8" to R.drawable.diamonds_8,
                "diamonds_9" to R.drawable.diamonds_9,
                "diamonds_10" to R.drawable.diamonds_10,
                "diamonds_11" to R.drawable.diamonds_11,
                "diamonds_12" to R.drawable.diamonds_12,
                "diamonds_13" to R.drawable.diamonds_13,
                "diamonds_14" to R.drawable.diamonds_14,
                "hearts_4" to R.drawable.hearts_4,
                "hearts_5" to R.drawable.hearts_5,
                "hearts_6" to R.drawable.hearts_6,
                "hearts_7" to R.drawable.hearts_7,
                "hearts_8" to R.drawable.hearts_8,
                "hearts_9" to R.drawable.hearts_9,
                "hearts_10" to R.drawable.hearts_10,
                "hearts_11" to R.drawable.hearts_11,
                "hearts_12" to R.drawable.hearts_12,
                "hearts_13" to R.drawable.hearts_13,
                "hearts_14" to R.drawable.hearts_14,
                "joker_15" to R.drawable.red_joker,
                "nosuit_99" to R.drawable.back,
                "nosuit_0" to R.drawable.back
        )
    }

    init {
        val resId:Int? = Icons.map[suit.title.toLowerCase()+"_"+value.toString()]
        if (resId != null) {
            imgResId = resId
        }
    }

    fun getDisplayName(): String {
        if (value == 15) {
            return getValueDisplayName()
        } else {
            return getValueDisplayName() + " of " + suit.title
        }
    }

    private fun getValueDisplayName(): String {
        when (value) {
            11 -> return "Jack"
            12 -> return "Queen"
            13 -> return "King"
            14 -> return "Ace"
            15 -> return "Joker"
            else -> {
                return value.toString()
            }
        }
    }

    fun isTrump(trumpSuit: Suit):Boolean {
        if (trumpSuit.equals(Suit.SPADE)) {
            return suit.equals(Suit.SPADE) || (suit.equals(Suit.CLUB) && value == 11)
        } else if (trumpSuit.equals(Suit.CLUB)) {
            return suit.equals(Suit.CLUB) || (suit.equals(Suit.SPADE) && value == 11)
        } else if (trumpSuit.equals(Suit.HEART)) {
            return suit.equals(Suit.HEART) || (suit.equals(Suit.DIAMOND) && value == 11)
        } else if (trumpSuit.equals(Suit.DIAMOND)) {
            return suit.equals(Suit.DIAMOND) || (suit.equals(Suit.HEART) && value == 11)
        } else return suit.equals(Suit.JOKER)
    }

}