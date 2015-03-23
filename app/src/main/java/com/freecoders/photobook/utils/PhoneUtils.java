package com.freecoders.photobook.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.freecoders.photobook.R;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 * Created by Alex on 2014-12-06.
 */
public class PhoneUtils {

    // Implement phone number normalization. Ex. 010-1111-2222 -> +82 10-1111-2222
    public final static String getNormalizedPhoneNumber(String strRawPhoneNum) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Integer intCountryCode = getCountryCode();
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber().
                setCountryCode(intCountryCode).setRawInput(strRawPhoneNum);
        String strNormPhoneNUmber = phoneUtil.format(phoneNumber,
                PhoneNumberUtil.PhoneNumberFormat.E164);
        return strNormPhoneNUmber;
    }

    private static Integer getCountryCode(){
        return Photobook.getPreferences().intCountryCode;
    }

    public static Integer getCountryCode(String strPhone){
        Integer intCode = R.integer.default_country_code;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(strPhone, "");
            intCode = phoneNumber.getCountryCode();
        } catch (NumberParseException e) {
            Log.d(Constants.LOG_TAG, "NumberParseException " + e.getLocalizedMessage());
        }
        return intCode;
    }

    public final static String getPhoneNumber() {
        TelephonyManager tm =  (TelephonyManager)
                Photobook.getMainActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = "";
        if ((tm.getLine1Number() != null) && (!tm.getLine1Number().isEmpty()))
            mPhoneNumber = tm.getLine1Number();
        return mPhoneNumber;
    }
}
