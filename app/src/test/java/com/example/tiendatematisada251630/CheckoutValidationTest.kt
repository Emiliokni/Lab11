package com.example.tiendatematisada251630

import com.example.tiendatematisada251630.model.BillingType
import com.example.tiendatematisada251630.model.CheckoutUiState
import com.example.tiendatematisada251630.model.validateBusinessName
import com.example.tiendatematisada251630.model.validateFullName
import com.example.tiendatematisada251630.model.validateNit
import com.example.tiendatematisada251630.model.validatePhone
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutValidationTest {
    @Test
    fun fullNameRequiresThreeLettersAndRejectsDigits() {
        assertNotNull(validateFullName("Al"))
        assertNotNull(validateFullName("Ana 2"))
        assertNull(validateFullName("  Ana María  "))
        assertNull(validateFullName("José"))
    }

    @Test
    fun phoneRequiresExactlyEightDigits() {
        assertNull(validatePhone("55123456"))
        assertNotNull(validatePhone("5512-3456"))
        assertNotNull(validatePhone("5512345"))
        assertNotNull(validatePhone("551234567"))
    }

    @Test
    fun fiscalValidatorsApplyTheirDocumentedRules() {
        assertNull(validateNit("12345"))
        assertNotNull(validateNit("1234"))
        assertNull(validateBusinessName("  Café GT  "))
        assertNotNull(validateBusinessName("  AB  "))
    }

    @Test
    fun cfIgnoresFiscalFieldsButNitRequiresThem() {
        val base = CheckoutUiState(
            fullName = "Ana López",
            phone = "55123456"
        )

        assertTrue(base.isFormValid)
        assertNull(base.nitError)
        assertNull(base.businessNameError)

        val withNit = base.copy(billingType = BillingType.NIT)
        assertFalse(withNit.isFormValid)
        assertNotNull(withNit.nitError)
        assertNotNull(withNit.businessNameError)

        assertTrue(
            withNit.copy(nit = "12345", businessName = "Café GT").isFormValid
        )
    }
}
