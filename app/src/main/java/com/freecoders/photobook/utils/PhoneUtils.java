package com.freecoders.photobook.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

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


        String countryCode = getCountryISOCode().toUpperCase();
        String strNormPhoneNUmber = strRawPhoneNum;
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(strRawPhoneNum, countryCode);
            strNormPhoneNUmber = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            //Log.d(Constants.LOG_TAG,"Norm phone number: "+strNormPhoneNUmber);

        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return strNormPhoneNUmber;
    }

    // Implement local ISO country code from SIM
    private static String getCountryISOCode(){
        //Context aContext = Photobook.getMainActivity().getApplication();
        //TelephonyManager tm = (TelephonyManager)aContext.getSystemService(aContext.TELEPHONY_SERVICE);
        TelephonyManager tm =  (TelephonyManager)
                Photobook.getMainActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        if (countryCode.isEmpty()) {
            try {
                //countryCode = tm.getSimCountryIso();
                Locale locale = Locale.getDefault();
                countryCode = locale.getCountry();
                Log.d(Constants.LOG_TAG, "Identified country code " +
                        countryCode + " from locale");
                //Log.d(Constants.LOG_TAG,"Country code: "+countryCode);
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, "getCountryISOCode exception");
                countryCode = "KR";
            }
        } else {
            Log.d(Constants.LOG_TAG, "Identified country code " + countryCode + " from SIM");
        }
        //String countryCode = tm.getNetworkCountryIso();
        return countryCode;
    }

    public final static String getPhoneNumber() {
        TelephonyManager tm =  (TelephonyManager)
                Photobook.getMainActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tm.getLine1Number();
        if ((mPhoneNumber != null) && (!mPhoneNumber.isEmpty())) {
            mPhoneNumber = getNormalizedPhoneNumber(mPhoneNumber);
        } else {
            mPhoneNumber = "";
        }
        return mPhoneNumber;
    }
}



