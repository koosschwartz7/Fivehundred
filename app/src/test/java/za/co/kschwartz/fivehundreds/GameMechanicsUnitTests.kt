package za.co.kschwartz.fivehundreds

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import za.co.kschwartz.fivehundreds.domain.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GameMechanicsUnitTests {

    @Test
    fun jokerAlwaysWinsPack() {
        var pack: Pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4));
        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14) //Jan plays Ace of diamonds
        pack.turns[1].playedCard = Card(Suit.HEART, 4) //Piet trumps with 4 of hearts
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 4) //San plays 4 of diamonds
        pack.turns[3].playedCard = Card(Suit.JOKER, 15) //Juan plays Joker
        assertTrue(pack.determineWinningTurn(Suit.HEART).equals(pack.turns[3])) //Joker always wins

        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14)
        pack.turns[1].playedCard = Card(Suit.HEART, 4)
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 4)
        pack.turns[3].playedCard = Card(Suit.JOKER, 15)
        assertTrue(pack.determineWinningTurn(Suit.DIAMOND).equals(pack.turns[3])) //Joker always wins

        pack.turns[0].playedCard = Card(Suit.JOKER, 15)
        pack.turns[1].playedCard = Card(Suit.HEART, 4)
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 4)
        pack.turns[3].playedCard = Card(Suit.HEART, 15)
        assertTrue(pack.determineWinningTurn(Suit.HEART).equals(pack.turns[0])) //Joker always wins
    }

    @Test
    fun normalCardHierachyWinsPackWithHighestCard() {
        var pack: Pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4));
        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14) //Jan plays Ace of diamonds
        pack.turns[1].playedCard = Card(Suit.DIAMOND, 4) //Piet plays 4 of diamonds
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[0])) //Ace is highest

        pack.turns[0].playedCard = Card(Suit.DIAMOND, 4) //Jan plays 4 of diamonds
        pack.turns[1].playedCard = Card(Suit.DIAMOND, 12) //Piet plays Queen of diamonds
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[2])) //King is highest

        pack.turns[0].playedCard = Card(Suit.HEART, 4) //Jan plays 4 of diamonds
        pack.turns[1].playedCard = Card(Suit.DIAMOND, 12) //Piet plays Queen of diamonds
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[0])) //4 of hearts is highest
    }

    @Test
    fun highestTrumpWinsPack() {
        var pack: Pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4));
        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14) //Jan plays Ace of diamonds
        pack.turns[1].playedCard = Card(Suit.SPADE, 5) //Piet plays 4 of spades
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[1])) //5 of spades is highest

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.SPADE, 5)
        pack.turns[2].playedCard = Card(Suit.SPADE, 13)
        pack.turns[3].playedCard = Card(Suit.SPADE, 11)
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.SPADE, 5)
        pack.turns[2].playedCard = Card(Suit.SPADE, 13)
        pack.turns[3].playedCard = Card(Suit.CLUB, 11)
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.SPADE, 5)
        pack.turns[2].playedCard = Card(Suit.CLUB, 13)
        pack.turns[3].playedCard = Card(Suit.SPADE, 11)
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.CLUB, 11)
        pack.turns[2].playedCard = Card(Suit.SPADE, 11)
        pack.turns[3].playedCard = Card(Suit.JOKER, 15)
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.CLUB, 14)
        pack.turns[1].playedCard = Card(Suit.CLUB, 5)
        pack.turns[2].playedCard = Card(Suit.SPADE, 13)
        pack.turns[3].playedCard = Card(Suit.CLUB, 11)
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))
    }

    @Test
    fun noTrumpsObeysNormalHierarchy() {
        var pack: Pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4));
        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14) //Jan plays Ace of diamonds
        pack.turns[1].playedCard = Card(Suit.SPADE, 5) //Piet plays 4 of spades
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        assertTrue(pack.determineWinningTurn(Suit.JOKER).equals(pack.turns[0]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 5)
        pack.turns[1].playedCard = Card(Suit.CLUB, 6)
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13)
        pack.turns[3].playedCard = Card(Suit.HEART, 11)
        assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[0]))
    }

    @Test
    fun minimumFirstBetIsAlwaysSixPackOfSuit() {
        var round: Round = Round(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)

        var minimumAllowedBetForSuit = round.getMinimumAllowedBetForSuit(Suit.SPADE, round.players[0]);
        Assert.assertEquals(minimumAllowedBetForSuit.trumpSuit, Suit.SPADE)
        Assert.assertEquals(minimumAllowedBetForSuit.nrPacks, 6)

        minimumAllowedBetForSuit = round.getMinimumAllowedBetForSuit(Suit.CLUB, round.players[0]);
        Assert.assertEquals(minimumAllowedBetForSuit.trumpSuit, Suit.CLUB)
        Assert.assertEquals(minimumAllowedBetForSuit.nrPacks, 6)

        minimumAllowedBetForSuit = round.getMinimumAllowedBetForSuit(Suit.DIAMOND, round.players[0]);
        Assert.assertEquals(minimumAllowedBetForSuit.trumpSuit, Suit.DIAMOND)
        Assert.assertEquals(minimumAllowedBetForSuit.nrPacks, 6)

        minimumAllowedBetForSuit = round.getMinimumAllowedBetForSuit(Suit.HEART, round.players[0]);
        Assert.assertEquals(minimumAllowedBetForSuit.trumpSuit, Suit.HEART)
        Assert.assertEquals(minimumAllowedBetForSuit.nrPacks, 6)

        minimumAllowedBetForSuit = round.getMinimumAllowedBetForSuit(Suit.JOKER, round.players[0]);
        Assert.assertEquals(minimumAllowedBetForSuit.trumpSuit, Suit.JOKER)
        Assert.assertEquals(minimumAllowedBetForSuit.nrPacks, 6)
    }

    @Test
    fun slammingBetCannotBeSupersededByLowerOrSameSuit() {
        var round: Round = Round(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        val bet:Bet = Bet(Suit.CLUB, round.players[0], 10)
        round.placeBet(bet)

        round.getMinimumAllowedBetForSuit(Suit.DIAMOND, round.players[0]) //Allowed
        try {
            round.getMinimumAllowedBetForSuit(Suit.SPADE, round.players[0]) //No possible bet for SPADES, throws SuitBetNotAllowedException
        } catch (e: SuitBetNotAllowedException) {
            Assert.assertEquals("Suit Spades has no possible bet that's greater than 10 of Clubs", e.message)
            return
        }
        Assert.fail("Betting any number of spades against 10 of CLUBS should not be allowed, but it is.")
    }

    @Test
    fun getMinimumBetForSuitAlwaysReturnsValidBet() {
        var round: Round = Round(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        //When no bets have been placed yet, lowest possible bet (6 pack) is generated
        var bet = round.getMinimumAllowedBetForSuit(Suit.SPADE, round.players[0])
        Assert.assertEquals(Suit.SPADE, bet.trumpSuit)
        Assert.assertEquals(6, bet.nrPacks)
        bet = round.getMinimumAllowedBetForSuit(Suit.DIAMOND, round.players[0])
        Assert.assertEquals(Suit.DIAMOND, bet.trumpSuit)
        Assert.assertEquals(6, bet.nrPacks)

        round.placeBet(Bet(Suit.DIAMOND,round.players[0]))

        //When a bet has been placed, placing a bet for a weaker/lighter Suit or the same suit must be one pack more.
        bet = round.getMinimumAllowedBetForSuit(Suit.CLUB, round.players[0])
        Assert.assertEquals(Suit.CLUB, bet.trumpSuit)
        Assert.assertEquals(7, bet.nrPacks)
        bet = round.getMinimumAllowedBetForSuit(Suit.DIAMOND, round.players[0])
        Assert.assertEquals(Suit.DIAMOND, bet.trumpSuit)
        Assert.assertEquals(7, bet.nrPacks)

        //When a bet has been placed, you can bet a stronger/heavier Suit with same pack value
        bet = round.getMinimumAllowedBetForSuit(Suit.HEART, round.players[0])
        Assert.assertEquals(Suit.HEART, bet.trumpSuit)
        Assert.assertEquals(6, bet.nrPacks)
        bet = round.getMinimumAllowedBetForSuit(Suit.JOKER, round.players[0])
        Assert.assertEquals(Suit.JOKER, bet.trumpSuit)
        Assert.assertEquals(6, bet.nrPacks)
    }

    @Test
    fun getMinimumBetForSuitDoesNotAcceptNullSuit() {
        var round: Round = Round(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        try {
            round.getMinimumAllowedBetForSuit(Suit.NULLSUIT, round.players[0])
        } catch (e: SuitBetNotAllowedException) {
            Assert.assertEquals("NoSuit is not allowed as a bet!", e.message)
            return
        }
        Assert.fail("Betting any number of spades against 10 of CLUBS should not be allowed, but it is.")
    }

    @Test
    fun mayPlaceBetCorrectlyValidatesPotentialBet() {
        var round: Round = Round(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        val aPlayer =  round.players[0]
        //When no bets have been placed yet, any bet is accepted.
        assertTrue(round.mayPlaceBet(Bet(Suit.SPADE, aPlayer, 6)))

        //Suit weight holds for betting
        round.placeBet(Bet(Suit.DIAMOND, aPlayer, 6))
        Assert.assertFalse(round.mayPlaceBet(Bet(Suit.SPADE, aPlayer, 6)))
        Assert.assertFalse(round.mayPlaceBet(Bet(Suit.CLUB, aPlayer, 6)))
        Assert.assertFalse(round.mayPlaceBet(Bet(Suit.DIAMOND, aPlayer, 6)))
        assertTrue(round.mayPlaceBet(Bet(Suit.HEART, aPlayer, 6)))
        assertTrue(round.mayPlaceBet(Bet(Suit.JOKER, aPlayer, 6)))

        //Betting bigger pack value for lighter Suit is accepted
        assertTrue(round.mayPlaceBet(Bet(Suit.SPADE, aPlayer, 7)))
        assertTrue(round.mayPlaceBet(Bet(Suit.CLUB, aPlayer, 7)))
        assertTrue(round.mayPlaceBet(Bet(Suit.DIAMOND, aPlayer, 7)))

        //May not bet a pack value larger than 10
        assertTrue(round.mayPlaceBet(Bet(Suit.SPADE, aPlayer, 10)))
        Assert.assertFalse(round.mayPlaceBet(Bet(Suit.SPADE, aPlayer, 11)))
    }

    @Test
    fun dealHandDealsTenUniqueCardsToEachPlayer() {
        var round: Round = Round(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        val deck = FivehundredDeck();
        deck.reset()
        round.dealHand(deck)

        //All players have 10 cards in hand
        for (player in round.players) {
            assertEquals(10, player.hand.size)
        }

        //Deck has 3 cards left as "kitty"
        assertEquals(3, deck.cards.size)

        //No player has the same card as another player
        for (pIndex in 0..2) {
            val player = round.players[pIndex]
            for (card in player.hand) {
                for (otherPIndex in (pIndex + 1)..3) {
                    val otherPlayer = round.players[otherPIndex]
                    for (otherPCard in otherPlayer.hand) {
                        assertFalse(card.suit == otherPCard.suit && card.value == otherPCard.value)
                    }
                }
            }
        }
    }

    @Test
    fun allTurnsPlayedCorrectlyChecksAllPlayersPlayed() {
        var pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4))
        assertFalse(pack.allTurnsPlayed())
        pack.getNextPlayableTurn().playedCard = Card(Suit.SPADE, 10)
        assertFalse(pack.allTurnsPlayed())
        pack.getNextPlayableTurn().playedCard = Card(Suit.SPADE, 11)
        assertFalse(pack.allTurnsPlayed())
        pack.getNextPlayableTurn().playedCard = Card(Suit.SPADE, 12)
        assertFalse(pack.allTurnsPlayed())
        pack.getNextPlayableTurn().playedCard = Card(Suit.SPADE, 13)
        assertTrue(pack.allTurnsPlayed())
    }

    @Test
    fun roundScoreCalculatesCorrectly() {
        var round: Round = Round(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        round.placeBet(Bet(Suit.DIAMOND, round.players[0]))

        //Team one wins bet of 6-Diamonds with 6 packs
        playWinningPackForTeam(round, 0, 1)
        playWinningPackForTeam(round, 1, 1)
        playWinningPackForTeam(round, 2, 1)
        playWinningPackForTeam(round, 3, 1)
        playWinningPackForTeam(round, 4, 1)
        playWinningPackForTeam(round, 5, 1)
        playWinningPackForTeam(round, 6, 2)
        playWinningPackForTeam(round, 7, 2)
        playWinningPackForTeam(round, 8, 2)
        playWinningPackForTeam(round, 9, 2)
        assertEquals(80, round.getTotalScoreForTeam(1))
        assertEquals(40, round.getTotalScoreForTeam(2))

        //Team one wins bet of 7-Diamonds with 7 packs
        round = Round(za.co.kschwartz.fivehundreds.domain.Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        round.placeBet(Bet(Suit.DIAMOND, round.players[0], 7))
        playWinningPackForTeam(round, 0, 1)
        playWinningPackForTeam(round, 1, 1)
        playWinningPackForTeam(round, 2, 1)
        playWinningPackForTeam(round, 3, 1)
        playWinningPackForTeam(round, 4, 1)
        playWinningPackForTeam(round, 5, 1)
        playWinningPackForTeam(round, 6, 1)
        playWinningPackForTeam(round, 7, 2)
        playWinningPackForTeam(round, 8, 2)
        playWinningPackForTeam(round, 9, 2)
        assertEquals(130, round.getTotalScoreForTeam(1))
        assertEquals(30, round.getTotalScoreForTeam(2))

        //Team one wins bet of 7-Diamonds with 8 packs
        round = Round(za.co.kschwartz.fivehundreds.domain.Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        round.placeBet(Bet(Suit.DIAMOND, round.players[0], 7))
        playWinningPackForTeam(round, 0, 1)
        playWinningPackForTeam(round, 1, 1)
        playWinningPackForTeam(round, 2, 1)
        playWinningPackForTeam(round, 3, 1)
        playWinningPackForTeam(round, 4, 1)
        playWinningPackForTeam(round, 5, 1)
        playWinningPackForTeam(round, 6, 1)
        playWinningPackForTeam(round, 7, 1)
        playWinningPackForTeam(round, 8, 2)
        playWinningPackForTeam(round, 9, 2)
        assertEquals(140, round.getTotalScoreForTeam(1))
        assertEquals(20, round.getTotalScoreForTeam(2))

        //Team one loses bet of 7-Diamonds with 2 packs
        round = Round(za.co.kschwartz.fivehundreds.domain.Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        round.placeBet(Bet(Suit.DIAMOND, round.players[0], 7))
        playWinningPackForTeam(round, 0, 2)
        playWinningPackForTeam(round, 1, 2)
        playWinningPackForTeam(round, 2, 2)
        playWinningPackForTeam(round, 3, 2)
        playWinningPackForTeam(round, 4, 2)
        playWinningPackForTeam(round, 5, 2)
        playWinningPackForTeam(round, 6, 2)
        playWinningPackForTeam(round, 7, 2)
        playWinningPackForTeam(round, 8, 1)
        playWinningPackForTeam(round, 9, 1)
        assertEquals(0, round.getTotalScoreForTeam(1))
        assertEquals(210, round.getTotalScoreForTeam(2))

        //Team 2 slams
        round = Round(za.co.kschwartz.fivehundreds.domain.Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        round.placeBet(Bet(Suit.DIAMOND, round.players[0], 7))
        playWinningPackForTeam(round, 0, 2)
        playWinningPackForTeam(round, 1, 2)
        playWinningPackForTeam(round, 2, 2)
        playWinningPackForTeam(round, 3, 2)
        playWinningPackForTeam(round, 4, 2)
        playWinningPackForTeam(round, 5, 2)
        playWinningPackForTeam(round, 6, 2)
        playWinningPackForTeam(round, 7, 2)
        playWinningPackForTeam(round, 8, 2)
        playWinningPackForTeam(round, 9, 2)
        assertEquals(0, round.getTotalScoreForTeam(1))
        assertEquals(250, round.getTotalScoreForTeam(2))

        //Team one loses bet of 8-No Trumps with 2 packs
        round = Round(za.co.kschwartz.fivehundreds.domain.Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4), 1,1)
        round.placeBet(Bet(Suit.JOKER, round.players[0], 8))
        playWinningPackForTeam(round, 0, 2)
        playWinningPackForTeam(round, 1, 2)
        playWinningPackForTeam(round, 2, 2)
        playWinningPackForTeam(round, 3, 2)
        playWinningPackForTeam(round, 4, 2)
        playWinningPackForTeam(round, 5, 2)
        playWinningPackForTeam(round, 6, 2)
        playWinningPackForTeam(round, 7, 2)
        playWinningPackForTeam(round, 8, 1)
        playWinningPackForTeam(round, 9, 1)
        assertEquals(0, round.getTotalScoreForTeam(1))
        assertEquals(300, round.getTotalScoreForTeam(2))

    }

    fun playWinningPackForTeam(round: Round, packIndex: Int, teamNr: Int) {
        val firstTurn = round.packs[packIndex].getNextPlayableTurn()
        if (firstTurn.player.team == teamNr) {
            firstTurn.playedCard = Card(Suit.SPADE, 14)
            round.packs[packIndex].getNextPlayableTurn().playedCard = Card(Suit.SPADE, 8)
        } else {
            firstTurn.playedCard = Card(Suit.SPADE, 8)
            round.packs[packIndex].getNextPlayableTurn().playedCard = Card(Suit.SPADE, 14)
        }
        round.packs[packIndex].getNextPlayableTurn().playedCard = Card(Suit.SPADE, 6)
        round.packs[packIndex].getNextPlayableTurn().playedCard = Card(Suit.SPADE, 7)
    }
}
