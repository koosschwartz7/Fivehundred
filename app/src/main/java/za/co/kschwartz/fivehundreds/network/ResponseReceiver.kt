package za.co.kschwartz.fivehundreds.network

interface ResponseReceiver {

    fun joinMatchSuccess()

    fun joinMatchFailure(message: String)

}