package com.example.tiendatematisada251630.model

enum class BillingType(val label: String) {
    CF("Consumidor Final (CF)"),
    NIT("Factura con NIT")
}

enum class PaymentMethod(val label: String) {
    CASH_ON_DELIVERY("Efectivo contra entrega"),
    BANK_TRANSFER("Transferencia bancaria")
}

data class CheckoutUiState(
    val fullName: String = "",
    val phone: String = "",
    val billingType: BillingType = BillingType.CF,
    val nit: String = "",
    val businessName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val isFullNameTouched: Boolean = false,
    val isPhoneTouched: Boolean = false,
    val isNitTouched: Boolean = false,
    val isBusinessNameTouched: Boolean = false
) {
    val fullNameError: String?
        get() = validateFullName(fullName)

    val phoneError: String?
        get() = validatePhone(phone)

    val nitError: String?
        get() = if (billingType == BillingType.NIT) validateNit(nit) else null

    val businessNameError: String?
        get() = if (billingType == BillingType.NIT) validateBusinessName(businessName) else null

    val isFormValid: Boolean
        get() = fullNameError == null &&
            phoneError == null &&
            nitError == null &&
            businessNameError == null
}

data class OrderReceipt(
    val folio: String,
    val clientName: String,
    val billingDescription: String,
    val paymentDescription: String,
    val totalCents: Int
)

fun validateFullName(value: String): String? {
    val trimmed = value.trim()
    return when {
        trimmed.any(Char::isDigit) -> "El nombre no puede contener números."
        trimmed.count(Char::isLetter) < 3 -> "Ingresa al menos 3 letras."
        else -> null
    }
}

fun validatePhone(value: String): String? =
    if (value.trim().matches(Regex("\\d{8}"))) {
        null
    } else {
        "Ingresa exactamente 8 dígitos, sin espacios ni guiones."
    }

fun validateNit(value: String): String? =
    if (value.trim().matches(Regex("\\d{5,}"))) {
        null
    } else {
        "Ingresa al menos 5 dígitos para este ejercicio."
    }

fun validateBusinessName(value: String): String? =
    if (value.trim().length >= 3) {
        null
    } else {
        "Ingresa al menos 3 caracteres."
    }
