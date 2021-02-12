package za.co.kschwartz.fivehundreds.network

import za.co.kschwartz.fivehundreds.domain.Match

interface ResponseReceiver {

    fun joinMatchSuccess(match: Match)

    fun joinMatchFailure(message: String)

    fun leaveMatchSuccess(match: Match)

    fun matchUpdated(match: Match)

}