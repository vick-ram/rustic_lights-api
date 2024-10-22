package vickram.tech.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vickram.tech.utils.TRANSACTION_STATUS
import vickram.tech.utils.UUIDSerializer
import java.util.UUID

@Serializable
data class STKPushRequest(
    @SerialName("BusinessShortCode") val businessShortCode: String = "174379",
    @SerialName("Password") val password: String,
    @SerialName("Timestamp") val timestamp: String,
    @SerialName("TransactionType") val transactionType: String = "CustomerPayBillOnline",
    @SerialName("Amount") val amount: String,
    @SerialName("PartyA") val partyA: String,
    @SerialName("PartyB") val partyB: String = "174379",
    @SerialName("PhoneNumber") val phoneNumber: String,
    @SerialName("CallBackURL") val callBackURL: String,
    @SerialName("AccountReference") val accountReference: String,
    @SerialName("TransactionDesc") val transactionDesc: String
)

@Serializable
data class STKPushResponse(
    @SerialName("MerchantRequestID") val merchantRequestID: String,
    @SerialName("CheckoutRequestID") val checkoutRequestID: String,
    @SerialName("ResponseCode") val responseCode: String,
    @SerialName("ResponseDescription") val responseDescription: String,
    @SerialName("CustomerMessage") val customerMessage: String
)

@Serializable
data class STKSuccessResponse(
    @SerialName("Body") val body: Body
)

@Serializable
data class Body(
    @SerialName("stkCallback")
    val stkCallback: StkCallback
)

@Serializable
data class StkCallback(
    @SerialName("MerchantRequestID")
    val merchantRequestID: String,
    @SerialName("CheckoutRequestID")
    val checkoutRequestID: String,
    @SerialName("ResultCode")
    val resultCode: Int,
    @SerialName("ResultDesc")
    val resultDesc: String,
    @SerialName("CallbackMetadata")
    val callbackMetadata: CallbackMetadata
)

@Serializable
data class CallbackMetadata(
    @SerialName("Item")
    val item: List<Item>
)

@Serializable
data class Item(
    @SerialName("Name")
    val name: String,
    @SerialName("Value")
    @Contextual
    val value: Any
)

@Serializable
data class STKErrorResponse(
    @SerialName("Body") val body: ErrorBody
)

@Serializable
data class ErrorBody(
    @SerialName("stkCallback")
    val stkCallback: ErrorStkCallback
)

@Serializable
data class ErrorStkCallback(
    @SerialName("MerchantRequestID")
    val merchantRequestID: String,
    @SerialName("CheckoutRequestID")
    val checkoutRequestID: String,
    @SerialName("ResultCode")
    val resultCode: Int,
    @SerialName("ResultDesc")
    val resultDesc: String
)

@Serializable
data class Transaction(
    @Serializable(with = UUIDSerializer::class)
    val transactionId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val order: UUID,
    val transactionCode: String,
    val transactionAmount: String,
    val transactionType: String,
    val transactionTime: String,
    val transactionStatus: TRANSACTION_STATUS,
)
