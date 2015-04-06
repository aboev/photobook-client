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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Alex on 2014-12-06.
 */
public class PhoneUtils {
    private static String LOG_TAG = "PhoneUtils";

    // Implement phone number normalization. Ex. 010-1111-2222 -> +82 10-1111-2222
    public final static String getNormalizedPhoneNumber(String strRawPhoneNum) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String strCountryCode = getCountryCode();
        String strNormPhoneNUmber = strRawPhoneNum;
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(strRawPhoneNum, strCountryCode);
            strNormPhoneNUmber = phoneUtil.format(phoneNumber,
                    PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            Log.d(LOG_TAG, "NumberParseException on " + strRawPhoneNum + ", country code " +
                strCountryCode);
        }
        return strNormPhoneNUmber;
    }

    private static String getCountryCode(){
        return Photobook.getPreferences().strCountryCode;
    }

    public static String getCountryCode(String strPhone){
        String strCode = Photobook.getMainActivity().getResources().
                getString(R.string.default_country_code_iso);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(strPhone, "");
            strCode = codeMap.get(phoneNumber.getCountryCode());
        } catch (NumberParseException e) {
            Log.d(LOG_TAG, "NumberParseException " + e.getLocalizedMessage());
        }
        return strCode;
    }

    public final static String getPhoneNumber() {
        TelephonyManager tm =  (TelephonyManager)
                Photobook.getMainActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = "";
        if ((tm.getLine1Number() != null) && (!tm.getLine1Number().isEmpty()))
            mPhoneNumber = tm.getLine1Number();
        return mPhoneNumber;
    }

    private static Map<Integer, String> codeMap = new HashMap<Integer, String>();
    static {
        codeMap.put(93,"AF");
        codeMap.put(355,"AL");
        codeMap.put(213,"DZ");
        codeMap.put(376,"AD");
        codeMap.put(244,"AO");
        codeMap.put(54,"AR");
        codeMap.put(374,"AM");
        codeMap.put(61,"AU");
        codeMap.put(43,"AT");
        codeMap.put(994,"AZ");
        codeMap.put(973,"BH");
        codeMap.put(880,"BD");
        codeMap.put(375,"BY");
        codeMap.put(32,"BE");
        codeMap.put(501,"BZ");
        codeMap.put(229,"BJ");
        codeMap.put(975,"BT");
        codeMap.put(591,"BO");
        codeMap.put(387,"BA");
        codeMap.put(267,"BW");
        codeMap.put(55,"BR");
        codeMap.put(673,"BN");
        codeMap.put(359,"BG");
        codeMap.put(226,"BF");
        codeMap.put(257,"BI");
        codeMap.put(855,"KH");
        codeMap.put(237,"CM");
        codeMap.put(238,"CV");
        codeMap.put(236,"CF");
        codeMap.put(235,"TD");
        codeMap.put(56,"CL");
        codeMap.put(86,"CN");
        codeMap.put(57,"CO");
        codeMap.put(269,"KM");
        codeMap.put(243,"CD");
        codeMap.put(242,"CG");
        codeMap.put(506,"CR");
        codeMap.put(225,"CI");
        codeMap.put(385,"HR");
        codeMap.put(53,"CU");
        codeMap.put(357,"CY");
        codeMap.put(420,"CZ");
        codeMap.put(45,"DK");
        codeMap.put(253,"DJ");
        codeMap.put(593,"EC");
        codeMap.put(20,"EG");
        codeMap.put(503,"SV");
        codeMap.put(240,"GQ");
        codeMap.put(291,"ER");
        codeMap.put(372,"EE");
        codeMap.put(251,"ET");
        codeMap.put(679,"FJ");
        codeMap.put(358,"FI");
        codeMap.put(33,"FR");
        codeMap.put(241,"GA");
        codeMap.put(220,"GM");
        codeMap.put(995,"GE");
        codeMap.put(49,"DE");
        codeMap.put(233,"GH");
        codeMap.put(30,"GR");
        codeMap.put(502,"GT");
        codeMap.put(224,"GN");
        codeMap.put(245,"GW");
        codeMap.put(592,"GY");
        codeMap.put(509,"HT");
        codeMap.put(504,"HN");
        codeMap.put(36,"HU");
        codeMap.put(354,"IS");
        codeMap.put(91,"IN");
        codeMap.put(62,"ID");
        codeMap.put(98,"IR");
        codeMap.put(964,"IQ");
        codeMap.put(353,"IE");
        codeMap.put(972,"IL");
        codeMap.put(39,"IT");
        codeMap.put(81,"JP");
        codeMap.put(962,"JO");
        codeMap.put(254,"KE");
        codeMap.put(686,"KI");
        codeMap.put(850,"KP");
        codeMap.put(82,"KR");
        codeMap.put(965,"KW");
        codeMap.put(996,"KG");
        codeMap.put(856,"LA");
        codeMap.put(371,"LV");
        codeMap.put(961,"LB");
        codeMap.put(266,"LS");
        codeMap.put(231,"LR");
        codeMap.put(218,"LY");
        codeMap.put(423,"LI");
        codeMap.put(370,"LT");
        codeMap.put(352,"LU");
        codeMap.put(389,"MK");
        codeMap.put(261,"MG");
        codeMap.put(265,"MW");
        codeMap.put(60,"MY");
        codeMap.put(960,"MV");
        codeMap.put(223,"ML");
        codeMap.put(356,"MT");
        codeMap.put(692,"MH");
        codeMap.put(222,"MR");
        codeMap.put(230,"MU");
        codeMap.put(52,"MX");
        codeMap.put(691,"FM");
        codeMap.put(373,"MD");
        codeMap.put(377,"MC");
        codeMap.put(976,"MN");
        codeMap.put(382,"ME");
        codeMap.put(212,"MA");
        codeMap.put(258,"MZ");
        codeMap.put(95,"MM");
        codeMap.put(264,"NA");
        codeMap.put(674,"NR");
        codeMap.put(977,"NP");
        codeMap.put(31,"NL");
        codeMap.put(64,"NZ");
        codeMap.put(505,"NI");
        codeMap.put(227,"NE");
        codeMap.put(234,"NG");
        codeMap.put(47,"NO");
        codeMap.put(968,"OM");
        codeMap.put(92,"PK");
        codeMap.put(680,"PW");
        codeMap.put(507,"PA");
        codeMap.put(675,"PG");
        codeMap.put(595,"PY");
        codeMap.put(51,"PE");
        codeMap.put(63,"PH");
        codeMap.put(48,"PL");
        codeMap.put(351,"PT");
        codeMap.put(974,"QA");
        codeMap.put(40,"RO");
        codeMap.put(7,"RU");
        codeMap.put(250,"RW");
        codeMap.put(685,"WS");
        codeMap.put(378,"SM");
        codeMap.put(239,"ST");
        codeMap.put(966,"SA");
        codeMap.put(221,"SN");
        codeMap.put(381,"RS");
        codeMap.put(248,"SC");
        codeMap.put(232,"SL");
        codeMap.put(65,"SG");
        codeMap.put(421,"SK");
        codeMap.put(386,"SI");
        codeMap.put(677,"SB");
        codeMap.put(252,"SO");
        codeMap.put(27,"ZA");
        codeMap.put(34,"ES");
        codeMap.put(94,"LK");
        codeMap.put(249,"SD");
        codeMap.put(597,"SR");
        codeMap.put(268,"SZ");
        codeMap.put(46,"SE");
        codeMap.put(41,"CH");
        codeMap.put(963,"SY");
        codeMap.put(992,"TJ");
        codeMap.put(255,"TZ");
        codeMap.put(66,"TH");
        codeMap.put(670,"TL");
        codeMap.put(228,"TG");
        codeMap.put(676,"TO");
        codeMap.put(216,"TN");
        codeMap.put(90,"TR");
        codeMap.put(993,"TM");
        codeMap.put(688,"TV");
        codeMap.put(256,"UG");
        codeMap.put(380,"UA");
        codeMap.put(971,"AE");
        codeMap.put(44,"GB");
        codeMap.put(1,"US");
        codeMap.put(598,"UY");
        codeMap.put(998,"UZ");
        codeMap.put(678,"VU");
        codeMap.put(379,"VA");
        codeMap.put(58,"VE");
        codeMap.put(84,"VN");
        codeMap.put(967,"YE");
        codeMap.put(260,"ZM");
        codeMap.put(263,"ZW");
        codeMap.put(886,"TW");
        codeMap.put(672,"NF");
        codeMap.put(687,"NC");
        codeMap.put(689,"PF");
        codeMap.put(590,"GP");
        codeMap.put(508,"PM");
        codeMap.put(681,"WF");
        codeMap.put(682,"CK");
        codeMap.put(683,"NU");
        codeMap.put(690,"TK");
        codeMap.put(246,"IO");
        codeMap.put(500,"FK");
        codeMap.put(350,"GI");
        codeMap.put(290,"SH");
        codeMap.put(852,"HK");
        codeMap.put(853,"MO");
        codeMap.put(298,"FO");
        codeMap.put(299,"GL");
        codeMap.put(594,"GF");
        codeMap.put(596,"MQ");
        codeMap.put(262,"RE");
        codeMap.put(297,"AW");
        codeMap.put(599,"AN");
        codeMap.put(247,"AC");
        codeMap.put(970,"PS");
    }
}
