package com.peterarkt.customerconnect;

import com.peterarkt.customerconnect.ui.utils.ValidationUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by USUARIO on 21/02/2018.
 */

public class CustomerConnectUnitTest {

    @Test
    public void validation_emailValidation() throws Exception {
        assertEquals(true,ValidationUtils.emailIsValid("peter@mail.com"));
        assertEquals(false,ValidationUtils.emailIsValid("peter@mail"));
        assertEquals(false,ValidationUtils.emailIsValid("petermail.com"));
        assertEquals(false,ValidationUtils.emailIsValid("peter"));
        assertEquals(true,ValidationUtils.emailIsValid("peter-ark-t@mail.com"));
    }

    @Test
    public void validation_phoneValidation() throws Exception {
        assertEquals(true,ValidationUtils.phoneNumberIsValid("123456789"));
        assertEquals(false,ValidationUtils.phoneNumberIsValid("12345-6789"));
        assertEquals(false,ValidationUtils.phoneNumberIsValid("0A123456"));
        assertEquals(false,ValidationUtils.phoneNumberIsValid("0a123456"));
        assertEquals(false,ValidationUtils.phoneNumberIsValid("*02"));
    }

}
