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
        Assert.assertTrue(pack.determineWinningTurn(Suit.HEART).equals(pack.turns[3])) //Joker always wins

        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14)
        pack.turns[1].playedCard = Card(Suit.HEART, 4)
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 4)
        pack.turns[3].playedCard = Card(Suit.JOKER, 15)
        Assert.assertTrue(pack.determineWinningTurn(Suit.DIAMOND).equals(pack.turns[3])) //Joker always wins

        pack.turns[0].playedCard = Card(Suit.JOKER, 15)
        pack.turns[1].playedCard = Card(Suit.HEART, 4)
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 4)
        pack.turns[3].playedCard = Card(Suit.HEART, 15)
        Assert.assertTrue(pack.determineWinningTurn(Suit.HEART).equals(pack.turns[0])) //Joker always wins
    }

    @Test
    fun normalCardHierachyWinsPackWithHighestCard() {
        var pack: Pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4));
        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14) //Jan plays Ace of diamonds
        pack.turns[1].playedCard = Card(Suit.DIAMOND, 4) //Piet plays 4 of diamonds
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[0])) //Ace is highest

        pack.turns[0].playedCard = Card(Suit.DIAMOND, 4) //Jan plays 4 of diamonds
        pack.turns[1].playedCard = Card(Suit.DIAMOND, 12) //Piet plays Queen of diamonds
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[2])) //King is highest

        pack.turns[0].playedCard = Card(Suit.HEART, 4) //Jan plays 4 of diamonds
        pack.turns[1].playedCard = Card(Suit.DIAMOND, 12) //Piet plays Queen of diamonds
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[0])) //4 of hearts is highest
    }

    @Test
    fun highestTrumpWinsPack() {
        var pack: Pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4));
        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14) //Jan plays Ace of diamonds
        pack.turns[1].playedCard = Card(Suit.SPADE, 5) //Piet plays 4 of spades
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[1])) //5 of spades is highest

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.SPADE, 5)
        pack.turns[2].playedCard = Card(Suit.SPADE, 13)
        pack.turns[3].playedCard = Card(Suit.SPADE, 11)
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.SPADE, 5)
        pack.turns[2].playedCard = Card(Suit.SPADE, 13)
        pack.turns[3].playedCard = Card(Suit.CLUB, 11)
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.SPADE, 5)
        pack.turns[2].playedCard = Card(Suit.CLUB, 13)
        pack.turns[3].playedCard = Card(Suit.SPADE, 11)
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 14)
        pack.turns[1].playedCard = Card(Suit.CLUB, 11)
        pack.turns[2].playedCard = Card(Suit.SPADE, 11)
        pack.turns[3].playedCard = Card(Suit.JOKER, 15)
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))

        pack.turns[0].playedCard = Card(Suit.CLUB, 14)
        pack.turns[1].playedCard = Card(Suit.CLUB, 5)
        pack.turns[2].playedCard = Card(Suit.SPADE, 13)
        pack.turns[3].playedCard = Card(Suit.CLUB, 11)
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[3]))
    }

    @Test
    fun noTrumpsObeysNormalHierarchy() {
        var pack: Pack = Pack(Player("Jan", 1, 1), Player("Piet", 2, 2), Player("San", 1, 3),Player("Juan", 2, 4));
        pack.turns[0].playedCard = Card(Suit.DIAMOND, 14) //Jan plays Ace of diamonds
        pack.turns[1].playedCard = Card(Suit.SPADE, 5) //Piet plays 4 of spades
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13) //San plays King of diamonds
        pack.turns[3].playedCard = Card(Suit.DIAMOND, 11) //Juan plays Jack of diamonds
        Assert.assertTrue(pack.determineWinningTurn(Suit.JOKER).equals(pack.turns[0]))

        pack.turns[0].playedCard = Card(Suit.SPADE, 5)
        pack.turns[1].playedCard = Card(Suit.CLUB, 6)
        pack.turns[2].playedCard = Card(Suit.DIAMOND, 13)
        pack.turns[3].playedCard = Card(Suit.HEART, 11)
        Assert.assertTrue(pack.determineWinningTurn(Suit.SPADE).equals(pack.turns[0]))
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
            Assert.assertEquals(e.message, "Suit Spades has no possible bet that's greater than 10 of Clubs")
            return
        }
        Assert.fail("Betting any number of spades against 10 of CLUBS should not be allowed, but it is.")
    }

    @Test
    fun getMinimumBetForSuitAlwaysReturnsValidBet() {
        //TODO: Write test
    }

}
