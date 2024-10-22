package vickram.tech.utils

enum class ROLE {
    MERCHANT,
    CUSTOMER,
}

enum class PERMISSION {
    READ,
    WRITE,
    UPDATE,
    DELETE,
    ALL
}

enum class ORDER_STATUS {
    PENDING,
    DELIVERED,
    CANCELLED
}

enum class TRANSACTION_STATUS {
    SUCCESSFUL,
    FAILED
}

const val callBackUrl = "https://ngrok.io/callback"
const val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}\$"
const val phonePattern = "^254\\d{9}\$"
enum class RESULT_ERROR_CODE(val code: String, val description: String) {
    INVALID_MSISDN("C2B00011", "Invalid MSISDN"),
    INVALID_ACCOUNT_NUMBER("C2B00012", "Invalid Account Number"),
    INVALID_AMOUNT("C2B00013", "Invalid Amount"),
    INVALID_KYC_DETAILS("C2B00014", "Invalid KYC Details"),
    INVALID_SHORTCODE("C2B00015", "Invalid Shortcode"),
    OTHER_ERROR("C2B00016", "Other Error"),
}

