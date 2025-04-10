package android.telephony;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_IDP_STRING;
import static com.android.internal.telephony.TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utilities for dealing with phone number strings.
 */
public class PhoneNumberUtils {

    public static final char PAUSE = ',';

    public static final char WAIT = ';';

    public static final char WILD = 'N';

    public static final int TOA_International = 0x91;

    public static final int TOA_Unknown = 0x81;

    static final String LOG_TAG = "PhoneNumberUtils";

    private static final boolean DBG = false;

    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN = Pattern.compile("[\\+]?[0-9.-]+");

    /** True if c is ISO-LATIN characters 0-9 */
    public static boolean isISODigit(char c) {
        return c >= '0' && c <= '9';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # */
    public static final boolean is12Key(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD  */
    public static final boolean isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD;
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , + (no WILD)  */
    public static final boolean isReallyDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD, WAIT, PAUSE   */
    public static final boolean isNonSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD || c == WAIT || c == PAUSE;
    }

    /** This any anything to the right of this char is part of the
     *  post-dial string (eg this is PAUSE or WAIT)
     */
    public static final boolean isStartsPostDial(char c) {
        return c == PAUSE || c == WAIT;
    }

    /** Returns true if ch is not dialable or alpha char */
    private static boolean isSeparator(char ch) {
        return !isDialable(ch) && !(('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z'));
    }

    /** Extracts the phone number from an Intent.
     *
     * @param intent the intent to get the number of
     * @param context a context to use for database access
     *
     * @return the phone number that would be called by the intent, or
     *         <code>null</code> if the number cannot be found.
     */
    public static String getNumberFromIntent(Intent intent, Context context) {
        String number = null;
        Uri uri = intent.getData();
        String scheme = uri.getScheme();
        if (scheme.equals("tel")) {
            return uri.getSchemeSpecificPart();
        }
        if (scheme.equals("voicemail")) {
            return TelephonyManager.getDefault().getVoiceMailNumber();
        }
        if (context == null) {
            return null;
        }
        String type = intent.resolveType(context);
        String phoneColumn = null;
        final String authority = uri.getAuthority();
        if (Contacts.AUTHORITY.equals(authority)) {
            phoneColumn = Contacts.People.Phones.NUMBER;
        } else if (ContactsContract.AUTHORITY.equals(authority)) {
            phoneColumn = ContactsContract.CommonDataKinds.Phone.NUMBER;
        }
        final Cursor c = context.getContentResolver().query(uri, new String[] { phoneColumn }, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    number = c.getString(c.getColumnIndex(phoneColumn));
                }
            } finally {
                c.close();
            }
        }
        return number;
    }

    /** Extracts the network address portion and canonicalizes
     *  (filters out separators.)
     *  Network address portion is everything up to DTMF control digit
     *  separators (pause or wait), but without non-dialable characters.
     *
     *  Please note that the GSM wild character is allowed in the result.
     *  This must be resolved before dialing.
     *
     *  Allows + only in the first  position in the result string.
     *
     *  Returns null if phoneNumber == null
     */
    public static String extractNetworkPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        boolean firstCharAdded = false;
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (isDialable(c) && (c != '+' || !firstCharAdded)) {
                firstCharAdded = true;
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            }
        }
        return ret.toString();
    }

    /**
     * Strips separators from a phone number string.
     * @param phoneNumber phone number to strip.
     * @return phone string stripped of separators.
     */
    public static String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    /** or -1 if both are negative */
    private static int minPositive(int a, int b) {
        if (a >= 0 && b >= 0) {
            return (a < b) ? a : b;
        } else if (a >= 0) {
            return a;
        } else if (b >= 0) {
            return b;
        } else {
            return -1;
        }
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    /** index of the last character of the network portion
     *  (eg anything after is a post-dial string)
     */
    private static int indexOfLastNetworkChar(String a) {
        int pIndex, wIndex;
        int origLength;
        int trimIndex;
        origLength = a.length();
        pIndex = a.indexOf(PAUSE);
        wIndex = a.indexOf(WAIT);
        trimIndex = minPositive(pIndex, wIndex);
        if (trimIndex < 0) {
            return origLength - 1;
        } else {
            return trimIndex - 1;
        }
    }

    /**
     * Extracts the post-dial sequence of DTMF control digits, pauses, and
     * waits. Strips separators. This string may be empty, but will not be null
     * unless phoneNumber == null.
     *
     * Returns null if phoneNumber == null
     */
    public static String extractPostDialPortion(String phoneNumber) {
        if (phoneNumber == null) return null;
        int trimIndex;
        StringBuilder ret = new StringBuilder();
        trimIndex = indexOfLastNetworkChar(phoneNumber);
        for (int i = trimIndex + 1, s = phoneNumber.length(); i < s; i++) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    /**
     * Compare phone numbers a and b, return true if they're identical enough for caller ID purposes.
     */
    public static boolean compare(String a, String b) {
        return compare(a, b, false);
    }

    /**
     * Compare phone numbers a and b, and return true if they're identical
     * enough for caller ID purposes. Checks a resource to determine whether
     * to use a strict or loose comparison algorithm.
     */
    public static boolean compare(Context context, String a, String b) {
        boolean useStrict = context.getResources().getBoolean(com.android.internal.R.bool.config_use_strict_phone_number_comparation);
        return compare(a, b, useStrict);
    }

    /**
     * @hide only for testing.
     */
    public static boolean compare(String a, String b, boolean useStrictComparation) {
        return (useStrictComparation ? compareStrictly(a, b) : compareLoosely(a, b));
    }

    /**
     * Compare phone numbers a and b, return true if they're identical
     * enough for caller ID purposes.
     *
     * - Compares from right to left
     * - requires MIN_MATCH (7) characters to match
     * - handles common trunk prefixes and international prefixes
     *   (basically, everything except the Russian trunk prefix)
     *
     * Note that this method does not return false even when the two phone numbers
     * are not exactly same; rather; we can call this method "similar()", not "equals()".
     *
     * @hide
     */
    public static boolean compareLoosely(String a, String b) {
        int ia, ib;
        int matched;
        if (a == null || b == null) return a == b;
        if (a.length() == 0 || b.length() == 0) {
            return false;
        }
        ia = indexOfLastNetworkChar(a);
        ib = indexOfLastNetworkChar(b);
        matched = 0;
        while (ia >= 0 && ib >= 0) {
            char ca, cb;
            boolean skipCmp = false;
            ca = a.charAt(ia);
            if (!isDialable(ca)) {
                ia--;
                skipCmp = true;
            }
            cb = b.charAt(ib);
            if (!isDialable(cb)) {
                ib--;
                skipCmp = true;
            }
            if (!skipCmp) {
                if (cb != ca && ca != WILD && cb != WILD) {
                    break;
                }
                ia--;
                ib--;
                matched++;
            }
        }
        if (matched < MIN_MATCH) {
            int aLen = a.length();
            if (aLen == b.length() && aLen == matched) {
                return true;
            }
            return false;
        }
        if (matched >= MIN_MATCH && (ia < 0 || ib < 0)) {
            return true;
        }
        if (matchIntlPrefix(a, ia + 1) && matchIntlPrefix(b, ib + 1)) {
            return true;
        }
        if (matchTrunkPrefix(a, ia + 1) && matchIntlPrefixAndCC(b, ib + 1)) {
            return true;
        }
        if (matchTrunkPrefix(b, ib + 1) && matchIntlPrefixAndCC(a, ia + 1)) {
            return true;
        }
        return false;
    }

    /**
     * @hide
     */
    public static boolean compareStrictly(String a, String b) {
        return compareStrictly(a, b, true);
    }

    /**
     * @hide
     */
    public static boolean compareStrictly(String a, String b, boolean acceptInvalidCCCPrefix) {
        if (a == null || b == null) {
            return a == b;
        } else if (a.length() == 0 && b.length() == 0) {
            return false;
        }
        int forwardIndexA = 0;
        int forwardIndexB = 0;
        CountryCallingCodeAndNewIndex cccA = tryGetCountryCallingCodeAndNewIndex(a, acceptInvalidCCCPrefix);
        CountryCallingCodeAndNewIndex cccB = tryGetCountryCallingCodeAndNewIndex(b, acceptInvalidCCCPrefix);
        boolean bothHasCountryCallingCode = false;
        boolean okToIgnorePrefix = true;
        boolean trunkPrefixIsOmittedA = false;
        boolean trunkPrefixIsOmittedB = false;
        if (cccA != null && cccB != null) {
            if (cccA.countryCallingCode != cccB.countryCallingCode) {
                return false;
            }
            okToIgnorePrefix = false;
            bothHasCountryCallingCode = true;
            forwardIndexA = cccA.newIndex;
            forwardIndexB = cccB.newIndex;
        } else if (cccA == null && cccB == null) {
            okToIgnorePrefix = false;
        } else {
            if (cccA != null) {
                forwardIndexA = cccA.newIndex;
            } else {
                int tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                if (tmp >= 0) {
                    forwardIndexA = tmp;
                    trunkPrefixIsOmittedA = true;
                }
            }
            if (cccB != null) {
                forwardIndexB = cccB.newIndex;
            } else {
                int tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                if (tmp >= 0) {
                    forwardIndexB = tmp;
                    trunkPrefixIsOmittedB = true;
                }
            }
        }
        int backwardIndexA = a.length() - 1;
        int backwardIndexB = b.length() - 1;
        while (backwardIndexA >= forwardIndexA && backwardIndexB >= forwardIndexB) {
            boolean skip_compare = false;
            final char chA = a.charAt(backwardIndexA);
            final char chB = b.charAt(backwardIndexB);
            if (isSeparator(chA)) {
                backwardIndexA--;
                skip_compare = true;
            }
            if (isSeparator(chB)) {
                backwardIndexB--;
                skip_compare = true;
            }
            if (!skip_compare) {
                if (chA != chB) {
                    return false;
                }
                backwardIndexA--;
                backwardIndexB--;
            }
        }
        if (okToIgnorePrefix) {
            if ((trunkPrefixIsOmittedA && forwardIndexA <= backwardIndexA) || !checkPrefixIsIgnorable(a, forwardIndexA, backwardIndexA)) {
                if (acceptInvalidCCCPrefix) {
                    return compare(a, b, false);
                } else {
                    return false;
                }
            }
            if ((trunkPrefixIsOmittedB && forwardIndexB <= backwardIndexB) || !checkPrefixIsIgnorable(b, forwardIndexA, backwardIndexB)) {
                if (acceptInvalidCCCPrefix) {
                    return compare(a, b, false);
                } else {
                    return false;
                }
            }
        } else {
            boolean maybeNamp = !bothHasCountryCallingCode;
            while (backwardIndexA >= forwardIndexA) {
                final char chA = a.charAt(backwardIndexA);
                if (isDialable(chA)) {
                    if (maybeNamp && tryGetISODigit(chA) == 1) {
                        maybeNamp = false;
                    } else {
                        return false;
                    }
                }
                backwardIndexA--;
            }
            while (backwardIndexB >= forwardIndexB) {
                final char chB = b.charAt(backwardIndexB);
                if (isDialable(chB)) {
                    if (maybeNamp && tryGetISODigit(chB) == 1) {
                        maybeNamp = false;
                    } else {
                        return false;
                    }
                }
                backwardIndexB--;
            }
        }
        return true;
    }

    /**
     * Returns the rightmost MIN_MATCH (5) characters in the network portion
     * in *reversed* order
     *
     * This can be used to do a database lookup against the column
     * that stores getStrippedReversed()
     *
     * Returns null if phoneNumber == null
     */
    public static String toCallerIDMinMatch(String phoneNumber) {
        String np = extractNetworkPortion(phoneNumber);
        return internalGetStrippedReversed(np, MIN_MATCH);
    }

    /**
     * Returns the network portion reversed.
     * This string is intended to go into an index column for a
     * database lookup.
     *
     * Returns null if phoneNumber == null
     */
    public static String getStrippedReversed(String phoneNumber) {
        String np = extractNetworkPortion(phoneNumber);
        if (np == null) return null;
        return internalGetStrippedReversed(np, np.length());
    }

    /**
     * Returns the last numDigits of the reversed phone number
     * Returns null if np == null
     */
    private static String internalGetStrippedReversed(String np, int numDigits) {
        if (np == null) return null;
        StringBuilder ret = new StringBuilder(numDigits);
        int length = np.length();
        for (int i = length - 1, s = length; i >= 0 && (s - i) <= numDigits; i--) {
            char c = np.charAt(i);
            ret.append(c);
        }
        return ret.toString();
    }

    /**
     * Basically: makes sure there's a + in front of a
     * TOA_International number
     *
     * Returns null if s == null
     */
    public static String stringFromStringAndTOA(String s, int TOA) {
        if (s == null) return null;
        if (TOA == TOA_International && s.length() > 0 && s.charAt(0) != '+') {
            return "+" + s;
        }
        return s;
    }

    /**
     * Returns the TOA for the given dial string
     * Basically, returns TOA_International if there's a + prefix
     */
    public static int toaFromString(String s) {
        if (s != null && s.length() > 0 && s.charAt(0) == '+') {
            return TOA_International;
        }
        return TOA_Unknown;
    }

    /**
     *  3GPP TS 24.008 10.5.4.7
     *  Called Party BCD Number
     *
     *  See Also TS 51.011 10.5.1 "dialing number/ssc string"
     *  and TS 11.11 "10.3.1 EF adn (Abbreviated dialing numbers)"
     *
     * @param bytes the data buffer
     * @param offset should point to the TOA (aka. TON/NPI) octet after the length byte
     * @param length is the number of bytes including TOA byte
     *                and must be at least 2
     *
     * @return partial string on invalid decode
     *
     * FIXME(mkf) support alphanumeric address type
     *  currently implemented in SMSMessage.getAddress()
     */
    public static String calledPartyBCDToString(byte[] bytes, int offset, int length) {
        boolean prependPlus = false;
        StringBuilder ret = new StringBuilder(1 + length * 2);
        if (length < 2) {
            return "";
        }
        if ((bytes[offset] & 0xf0) == (TOA_International & 0xf0)) {
            prependPlus = true;
        }
        internalCalledPartyBCDFragmentToString(ret, bytes, offset + 1, length - 1);
        if (prependPlus && ret.length() == 0) {
            return "";
        }
        if (prependPlus) {
            String retString = ret.toString();
            Pattern p = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$");
            Matcher m = p.matcher(retString);
            if (m.matches()) {
                if ("".equals(m.group(2))) {
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(3));
                    ret.append(m.group(4));
                    ret.append(m.group(5));
                    ret.append("+");
                } else {
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(2));
                    ret.append(m.group(3));
                    ret.append("+");
                    ret.append(m.group(4));
                    ret.append(m.group(5));
                }
            } else {
                p = Pattern.compile("(^[#*])(.*)([#*])(.*)");
                m = p.matcher(retString);
                if (m.matches()) {
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(2));
                    ret.append(m.group(3));
                    ret.append("+");
                    ret.append(m.group(4));
                } else {
                    ret = new StringBuilder();
                    ret.append('+');
                    ret.append(retString);
                }
            }
        }
        return ret.toString();
    }

    private static void internalCalledPartyBCDFragmentToString(StringBuilder sb, byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            byte b;
            char c;
            c = bcdToChar((byte) (bytes[i] & 0xf));
            if (c == 0) {
                return;
            }
            sb.append(c);
            b = (byte) ((bytes[i] >> 4) & 0xf);
            if (b == 0xf && i + 1 == length + offset) {
                break;
            }
            c = bcdToChar(b);
            if (c == 0) {
                return;
            }
            sb.append(c);
        }
    }

    /**
     * Like calledPartyBCDToString, but field does not start with a
     * TOA byte. For example: SIM ADN extension fields
     */
    public static String calledPartyBCDFragmentToString(byte[] bytes, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        internalCalledPartyBCDFragmentToString(ret, bytes, offset, length);
        return ret.toString();
    }

    /** returns 0 on invalid value */
    private static char bcdToChar(byte b) {
        if (b < 0xa) {
            return (char) ('0' + b);
        } else switch(b) {
            case 0xa:
                return '*';
            case 0xb:
                return '#';
            case 0xc:
                return PAUSE;
            case 0xd:
                return WILD;
            default:
                return 0;
        }
    }

    private static int charToBCD(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c == '*') {
            return 0xa;
        } else if (c == '#') {
            return 0xb;
        } else if (c == PAUSE) {
            return 0xc;
        } else if (c == WILD) {
            return 0xd;
        } else {
            throw new RuntimeException("invalid char for BCD " + c);
        }
    }

    /**
     * Return true iff the network portion of <code>address</code> is,
     * as far as we can tell on the device, suitable for use as an SMS
     * destination address.
     */
    public static boolean isWellFormedSmsAddress(String address) {
        String networkPortion = PhoneNumberUtils.extractNetworkPortion(address);
        return (!(networkPortion.equals("+") || TextUtils.isEmpty(networkPortion))) && isDialable(networkPortion);
    }

    public static boolean isGlobalPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        Matcher match = GLOBAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber);
        return match.matches();
    }

    private static boolean isDialable(String address) {
        for (int i = 0, count = address.length(); i < count; i++) {
            if (!isDialable(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNonSeparator(String address) {
        for (int i = 0, count = address.length(); i < count; i++) {
            if (!isNonSeparator(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Note: calls extractNetworkPortion(), so do not use for
     * SIM EF[ADN] style records
     *
     * Returns null if network portion is empty.
     */
    public static byte[] networkPortionToCalledPartyBCD(String s) {
        String networkPortion = extractNetworkPortion(s);
        return numberToCalledPartyBCDHelper(networkPortion, false);
    }

    /**
     * Same as {@link #networkPortionToCalledPartyBCD}, but includes a
     * one-byte length prefix.
     */
    public static byte[] networkPortionToCalledPartyBCDWithLength(String s) {
        String networkPortion = extractNetworkPortion(s);
        return numberToCalledPartyBCDHelper(networkPortion, true);
    }

    /**
     * Convert a dialing number to BCD byte array
     *
     * @param number dialing number string
     *        if the dialing number starts with '+', set to internationl TOA
     * @return BCD byte array
     */
    public static byte[] numberToCalledPartyBCD(String number) {
        return numberToCalledPartyBCDHelper(number, false);
    }

    /**
     * If includeLength is true, prepend a one-byte length value to
     * the return array.
     */
    private static byte[] numberToCalledPartyBCDHelper(String number, boolean includeLength) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        boolean hasPlus = number.indexOf('+') != -1;
        if (hasPlus) numberLenEffective--;
        if (numberLenEffective == 0) return null;
        int resultLen = (numberLenEffective + 1) / 2;
        int extraBytes = 1;
        if (includeLength) extraBytes++;
        resultLen += extraBytes;
        byte[] result = new byte[resultLen];
        int digitCount = 0;
        for (int i = 0; i < numberLenReal; i++) {
            char c = number.charAt(i);
            if (c == '+') continue;
            int shift = ((digitCount & 0x01) == 1) ? 4 : 0;
            result[extraBytes + (digitCount >> 1)] |= (byte) ((charToBCD(c) & 0x0F) << shift);
            digitCount++;
        }
        if ((digitCount & 0x01) == 1) result[extraBytes + (digitCount >> 1)] |= 0xF0;
        int offset = 0;
        if (includeLength) result[offset++] = (byte) (resultLen - 1);
        result[offset] = (byte) (hasPlus ? TOA_International : TOA_Unknown);
        return result;
    }

    /** The current locale is unknown, look for a country code or don't format */
    public static final int FORMAT_UNKNOWN = 0;

    /** NANP formatting */
    public static final int FORMAT_NANP = 1;

    /** Japanese formatting */
    public static final int FORMAT_JAPAN = 2;

    /** List of country codes for countries that use the NANP */
    private static final String[] NANP_COUNTRIES = new String[] { "US", "CA", "AS", "AI", "AG", "BS", "BB", "BM", "VG", "KY", "DM", "DO", "GD", "GU", "JM", "PR", "MS", "MP", "KN", "LC", "VC", "TT", "TC", "VI" };

    /**
     * Breaks the given number down and formats it according to the rules
     * for the country the number is from.
     *
     * @param source the phone number to format
     * @return a locally acceptable formatting of the input, or the raw input if
     *  formatting rules aren't known for the number
     */
    public static String formatNumber(String source) {
        SpannableStringBuilder text = new SpannableStringBuilder(source);
        formatNumber(text, getFormatTypeForLocale(Locale.getDefault()));
        return text.toString();
    }

    /**
     * Returns the phone number formatting type for the given locale.
     *
     * @param locale The locale of interest, usually {@link Locale#getDefault()}
     * @return the formatting type for the given locale, or FORMAT_UNKNOWN if the formatting
     * rules are not known for the given locale
     */
    public static int getFormatTypeForLocale(Locale locale) {
        String country = locale.getCountry();
        return getFormatTypeFromCountryCode(country);
    }

    /**
     * Formats a phone number in-place. Currently only supports NANP formatting.
     *
     * @param text The number to be formatted, will be modified with the formatting
     * @param defaultFormattingType The default formatting rules to apply if the number does
     * not begin with +<country_code>
     */
    public static void formatNumber(Editable text, int defaultFormattingType) {
        int formatType = defaultFormattingType;
        if (text.length() > 2 && text.charAt(0) == '+') {
            if (text.charAt(1) == '1') {
                formatType = FORMAT_NANP;
            } else if (text.length() >= 3 && text.charAt(1) == '8' && text.charAt(2) == '1') {
                formatType = FORMAT_JAPAN;
            } else {
                formatType = FORMAT_UNKNOWN;
            }
        }
        switch(formatType) {
            case FORMAT_NANP:
                formatNanpNumber(text);
                return;
            case FORMAT_JAPAN:
                formatJapaneseNumber(text);
                return;
            case FORMAT_UNKNOWN:
                removeDashes(text);
                return;
        }
    }

    private static final int NANP_STATE_DIGIT = 1;

    private static final int NANP_STATE_PLUS = 2;

    private static final int NANP_STATE_ONE = 3;

    private static final int NANP_STATE_DASH = 4;

    /**
     * Formats a phone number in-place using the NANP formatting rules. Numbers will be formatted
     * as:
     *
     * <p><code>
     * xxxxx
     * xxx-xxxx
     * xxx-xxx-xxxx
     * 1-xxx-xxx-xxxx
     * +1-xxx-xxx-xxxx
     * </code></p>
     *
     * @param text the number to be formatted, will be modified with the formatting
     */
    public static void formatNanpNumber(Editable text) {
        int length = text.length();
        if (length > "+1-nnn-nnn-nnnn".length()) {
            return;
        } else if (length <= 5) {
            return;
        }
        CharSequence saved = text.subSequence(0, length);
        removeDashes(text);
        length = text.length();
        int dashPositions[] = new int[3];
        int numDashes = 0;
        int state = NANP_STATE_DIGIT;
        int numDigits = 0;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            switch(c) {
                case '1':
                    if (numDigits == 0 || state == NANP_STATE_PLUS) {
                        state = NANP_STATE_ONE;
                        break;
                    }
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '0':
                    if (state == NANP_STATE_PLUS) {
                        text.replace(0, length, saved);
                        return;
                    } else if (state == NANP_STATE_ONE) {
                        dashPositions[numDashes++] = i;
                    } else if (state != NANP_STATE_DASH && (numDigits == 3 || numDigits == 6)) {
                        dashPositions[numDashes++] = i;
                    }
                    state = NANP_STATE_DIGIT;
                    numDigits++;
                    break;
                case '-':
                    state = NANP_STATE_DASH;
                    break;
                case '+':
                    if (i == 0) {
                        state = NANP_STATE_PLUS;
                        break;
                    }
                default:
                    text.replace(0, length, saved);
                    return;
            }
        }
        if (numDigits == 7) {
            numDashes--;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            text.replace(pos + i, pos + i, "-");
        }
        int len = text.length();
        while (len > 0) {
            if (text.charAt(len - 1) == '-') {
                text.delete(len - 1, len);
                len--;
            } else {
                break;
            }
        }
    }

    /**
     * Formats a phone number in-place using the Japanese formatting rules.
     * Numbers will be formatted as:
     *
     * <p><code>
     * 03-xxxx-xxxx
     * 090-xxxx-xxxx
     * 0120-xxx-xxx
     * +81-3-xxxx-xxxx
     * +81-90-xxxx-xxxx
     * </code></p>
     *
     * @param text the number to be formatted, will be modified with
     * the formatting
     */
    public static void formatJapaneseNumber(Editable text) {
        JapanesePhoneNumberFormatter.format(text);
    }

    /**
     * Removes all dashes from the number.
     *
     * @param text the number to clear from dashes
     */
    private static void removeDashes(Editable text) {
        int p = 0;
        while (p < text.length()) {
            if (text.charAt(p) == '-') {
                text.delete(p, p + 1);
            } else {
                p++;
            }
        }
    }

    static final int MIN_MATCH = 7;

    /**
     * isEmergencyNumber: checks a given number against the list of
     *   emergency numbers provided by the RIL and SIM card.
     *
     * @param number the number to look up.
     * @return if the number is in the list of emergency numbers
     * listed in the ril / sim, then return true, otherwise false.
     */
    public static boolean isEmergencyNumber(String number) {
        if (number == null) return false;
        number = extractNetworkPortion(number);
        String numbers = SystemProperties.get("ro.ril.ecclist");
        if (!TextUtils.isEmpty(numbers)) {
            for (String emergencyNum : numbers.split(",")) {
                if (emergencyNum.equals(number)) {
                    return true;
                }
            }
            return false;
        }
        return (number.equals("112") || number.equals("911"));
    }

    /**
     * isVoiceMailNumber: checks a given number against the voicemail
     *   number provided by the RIL and SIM card. The caller must have
     *   the READ_PHONE_STATE credential.
     *
     * @param number the number to look up.
     * @return true if the number is in the list of voicemail. False
     * otherwise, including if the caller does not have the permission
     * to read the VM number.
     * @hide TODO: pending API Council approval
     */
    public static boolean isVoiceMailNumber(String number) {
        String vmNumber;
        try {
            vmNumber = TelephonyManager.getDefault().getVoiceMailNumber();
        } catch (SecurityException ex) {
            return false;
        }
        number = extractNetworkPortion(number);
        return !TextUtils.isEmpty(number) && compare(number, vmNumber);
    }

    /**
     * Translates any alphabetic letters (i.e. [A-Za-z]) in the
     * specified phone number into the equivalent numeric digits,
     * according to the phone keypad letter mapping described in
     * ITU E.161 and ISO/IEC 9995-8.
     *
     * @return the input string, with alpha letters converted to numeric
     *         digits using the phone keypad letter mapping.  For example,
     *         an input of "1-800-GOOG-411" will return "1-800-4664-411".
     */
    public static String convertKeypadLettersToDigits(String input) {
        if (input == null) {
            return input;
        }
        int len = input.length();
        if (len == 0) {
            return input;
        }
        char[] out = input.toCharArray();
        for (int i = 0; i < len; i++) {
            char c = out[i];
            out[i] = (char) KEYPAD_MAP.get(c, c);
        }
        return new String(out);
    }

    /**
     * The phone keypad letter mapping (see ITU E.161 or ISO/IEC 9995-8.)
     * TODO: This should come from a resource.
     */
    private static final SparseIntArray KEYPAD_MAP = new SparseIntArray();

    static {
        KEYPAD_MAP.put('a', '2');
        KEYPAD_MAP.put('b', '2');
        KEYPAD_MAP.put('c', '2');
        KEYPAD_MAP.put('A', '2');
        KEYPAD_MAP.put('B', '2');
        KEYPAD_MAP.put('C', '2');
        KEYPAD_MAP.put('d', '3');
        KEYPAD_MAP.put('e', '3');
        KEYPAD_MAP.put('f', '3');
        KEYPAD_MAP.put('D', '3');
        KEYPAD_MAP.put('E', '3');
        KEYPAD_MAP.put('F', '3');
        KEYPAD_MAP.put('g', '4');
        KEYPAD_MAP.put('h', '4');
        KEYPAD_MAP.put('i', '4');
        KEYPAD_MAP.put('G', '4');
        KEYPAD_MAP.put('H', '4');
        KEYPAD_MAP.put('I', '4');
        KEYPAD_MAP.put('j', '5');
        KEYPAD_MAP.put('k', '5');
        KEYPAD_MAP.put('l', '5');
        KEYPAD_MAP.put('J', '5');
        KEYPAD_MAP.put('K', '5');
        KEYPAD_MAP.put('L', '5');
        KEYPAD_MAP.put('m', '6');
        KEYPAD_MAP.put('n', '6');
        KEYPAD_MAP.put('o', '6');
        KEYPAD_MAP.put('M', '6');
        KEYPAD_MAP.put('N', '6');
        KEYPAD_MAP.put('O', '6');
        KEYPAD_MAP.put('p', '7');
        KEYPAD_MAP.put('q', '7');
        KEYPAD_MAP.put('r', '7');
        KEYPAD_MAP.put('s', '7');
        KEYPAD_MAP.put('P', '7');
        KEYPAD_MAP.put('Q', '7');
        KEYPAD_MAP.put('R', '7');
        KEYPAD_MAP.put('S', '7');
        KEYPAD_MAP.put('t', '8');
        KEYPAD_MAP.put('u', '8');
        KEYPAD_MAP.put('v', '8');
        KEYPAD_MAP.put('T', '8');
        KEYPAD_MAP.put('U', '8');
        KEYPAD_MAP.put('V', '8');
        KEYPAD_MAP.put('w', '9');
        KEYPAD_MAP.put('x', '9');
        KEYPAD_MAP.put('y', '9');
        KEYPAD_MAP.put('z', '9');
        KEYPAD_MAP.put('W', '9');
        KEYPAD_MAP.put('X', '9');
        KEYPAD_MAP.put('Y', '9');
        KEYPAD_MAP.put('Z', '9');
    }

    private static final char PLUS_SIGN_CHAR = '+';

    private static final String PLUS_SIGN_STRING = "+";

    private static final String NANP_IDP_STRING = "011";

    private static final int NANP_LENGTH = 10;

    /**
     * This function checks if there is a plus sign (+) in the passed-in dialing number.
     * If there is, it processes the plus sign based on the default telephone
     * numbering plan of the system when the phone is activated and the current
     * telephone numbering plan of the system that the phone is camped on.
     * Currently, we only support the case that the default and current telephone
     * numbering plans are North American Numbering Plan(NANP).
     *
     * The passed-in dialStr should only contain the valid format as described below,
     * 1) the 1st character in the dialStr should be one of the really dialable
     *    characters listed below
     *    ISO-LATIN characters 0-9, *, # , +
     * 2) the dialStr should already strip out the separator characters,
     *    every character in the dialStr should be one of the non separator characters
     *    listed below
     *    ISO-LATIN characters 0-9, *, # , +, WILD, WAIT, PAUSE
     *
     * Otherwise, this function returns the dial string passed in
     *
     * @param dialStr the original dial string
     * @return the converted dial string if the current/default countries belong to NANP,
     * and if there is the "+" in the original dial string. Otherwise, the original dial
     * string returns.
     *
     * This API is for CDMA only
     *
     * @hide TODO: pending API Council approval
     */
    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        if (!TextUtils.isEmpty(dialStr)) {
            if (isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
                String currIso = SystemProperties.get(PROPERTY_OPERATOR_ISO_COUNTRY, "");
                String defaultIso = SystemProperties.get(PROPERTY_ICC_OPERATOR_ISO_COUNTRY, "");
                if (!TextUtils.isEmpty(currIso) && !TextUtils.isEmpty(defaultIso)) {
                    return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, getFormatTypeFromCountryCode(currIso), getFormatTypeFromCountryCode(defaultIso));
                }
            }
        }
        return dialStr;
    }

    /**
     * This function should be called from checkAndProcessPlusCode only
     * And it is used for test purpose also.
     *
     * It checks the dial string by looping through the network portion,
     * post dial portion 1, post dial porting 2, etc. If there is any
     * plus sign, then process the plus sign.
     * Currently, this function supports the plus sign conversion within NANP only.
     * Specifically, it handles the plus sign in the following ways:
     * 1)+1NANP,remove +, e.g.
     *   +18475797000 is converted to 18475797000,
     * 2)+NANP or +non-NANP Numbers,replace + with the current NANP IDP, e.g,
     *   +8475797000 is converted to 0118475797000,
     *   +11875767800 is converted to 01111875767800
     * 3)+1NANP in post dial string(s), e.g.
     *   8475797000;+18475231753 is converted to 8475797000;18475231753
     *
     *
     * @param dialStr the original dial string
     * @param currFormat the numbering system of the current country that the phone is camped on
     * @param defaultFormat the numbering system of the country that the phone is activated on
     * @return the converted dial string if the current/default countries belong to NANP,
     * and if there is the "+" in the original dial string. Otherwise, the original dial
     * string returns.
     *
     * @hide
     */
    public static String cdmaCheckAndProcessPlusCodeByNumberFormat(String dialStr, int currFormat, int defaultFormt) {
        String retStr = dialStr;
        if (dialStr != null && dialStr.lastIndexOf(PLUS_SIGN_STRING) != -1) {
            if ((currFormat == defaultFormt) && (currFormat == FORMAT_NANP)) {
                String postDialStr = null;
                String tempDialStr = dialStr;
                retStr = null;
                if (DBG) log("checkAndProcessPlusCode,dialStr=" + dialStr);
                do {
                    String networkDialStr;
                    networkDialStr = extractNetworkPortion(tempDialStr);
                    networkDialStr = processPlusCodeWithinNanp(networkDialStr);
                    if (!TextUtils.isEmpty(networkDialStr)) {
                        if (retStr == null) {
                            retStr = networkDialStr;
                        } else {
                            retStr = retStr.concat(networkDialStr);
                        }
                    } else {
                        Log.e("checkAndProcessPlusCode: null newDialStr", networkDialStr);
                        return dialStr;
                    }
                    postDialStr = extractPostDialPortion(tempDialStr);
                    if (!TextUtils.isEmpty(postDialStr)) {
                        int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);
                        if (dialableIndex >= 1) {
                            retStr = appendPwCharBackToOrigDialStr(dialableIndex, retStr, postDialStr);
                            tempDialStr = postDialStr.substring(dialableIndex);
                        } else {
                            if (dialableIndex < 0) {
                                postDialStr = "";
                            }
                            Log.e("wrong postDialStr=", postDialStr);
                        }
                    }
                    if (DBG) log("checkAndProcessPlusCode,postDialStr=" + postDialStr);
                } while (!TextUtils.isEmpty(postDialStr) && !TextUtils.isEmpty(tempDialStr));
            } else {
                Log.e("checkAndProcessPlusCode:non-NANP not supported", dialStr);
            }
        }
        return retStr;
    }

    private static String getDefaultIdp() {
        String ps = null;
        SystemProperties.get(PROPERTY_IDP_STRING, ps);
        if (TextUtils.isEmpty(ps)) {
            ps = NANP_IDP_STRING;
        }
        return ps;
    }

    private static boolean isTwoToNine(char c) {
        if (c >= '2' && c <= '9') {
            return true;
        } else {
            return false;
        }
    }

    private static int getFormatTypeFromCountryCode(String country) {
        int length = NANP_COUNTRIES.length;
        for (int i = 0; i < length; i++) {
            if (NANP_COUNTRIES[i].compareToIgnoreCase(country) == 0) {
                return FORMAT_NANP;
            }
        }
        if ("jp".compareToIgnoreCase(country) == 0) {
            return FORMAT_JAPAN;
        }
        return FORMAT_UNKNOWN;
    }

    /**
     * This function checks if the passed in string conforms to the NANP format
     * i.e. NXX-NXX-XXXX, N is any digit 2-9 and X is any digit 0-9
     */
    private static boolean isNanp(String dialStr) {
        boolean retVal = false;
        if (dialStr != null) {
            if (dialStr.length() == NANP_LENGTH) {
                if (isTwoToNine(dialStr.charAt(0)) && isTwoToNine(dialStr.charAt(3))) {
                    retVal = true;
                    for (int i = 1; i < NANP_LENGTH; i++) {
                        char c = dialStr.charAt(i);
                        if (!PhoneNumberUtils.isISODigit(c)) {
                            retVal = false;
                            break;
                        }
                    }
                }
            }
        } else {
            Log.e("isNanp: null dialStr passed in", dialStr);
        }
        return retVal;
    }

    /**
    * This function checks if the passed in string conforms to 1-NANP format
    */
    private static boolean isOneNanp(String dialStr) {
        boolean retVal = false;
        if (dialStr != null) {
            String newDialStr = dialStr.substring(1);
            if ((dialStr.charAt(0) == '1') && isNanp(newDialStr)) {
                retVal = true;
            }
        } else {
            Log.e("isOneNanp: null dialStr passed in", dialStr);
        }
        return retVal;
    }

    /**
     * This function handles the plus code conversion within NANP CDMA network
     * If the number format is
     * 1)+1NANP,remove +,
     * 2)other than +1NANP, any + numbers,replace + with the current IDP
     */
    private static String processPlusCodeWithinNanp(String networkDialStr) {
        String retStr = networkDialStr;
        if (DBG) log("processPlusCodeWithinNanp,networkDialStr=" + networkDialStr);
        if (networkDialStr != null & networkDialStr.charAt(0) == PLUS_SIGN_CHAR && networkDialStr.length() > 1) {
            String newStr = networkDialStr.substring(1);
            if (isOneNanp(newStr)) {
                retStr = newStr;
            } else {
                String idpStr = getDefaultIdp();
                retStr = networkDialStr.replaceFirst("[+]", idpStr);
            }
        }
        if (DBG) log("processPlusCodeWithinNanp,retStr=" + retStr);
        return retStr;
    }

    private static int findDialableIndexFromPostDialStr(String postDialStr) {
        for (int index = 0; index < postDialStr.length(); index++) {
            char c = postDialStr.charAt(index);
            if (isReallyDialable(c)) {
                return index;
            }
        }
        return -1;
    }

    private static String appendPwCharBackToOrigDialStr(int dialableIndex, String origStr, String dialStr) {
        String retStr;
        if (dialableIndex == 1) {
            StringBuilder ret = new StringBuilder(origStr);
            ret = ret.append(dialStr.charAt(0));
            retStr = ret.toString();
        } else {
            String nonDigitStr = dialStr.substring(0, dialableIndex);
            retStr = origStr.concat(nonDigitStr);
        }
        return retStr;
    }

    /** all of a up to len must be an international prefix or
     *  separators/non-dialing digits
     */
    private static boolean matchIntlPrefix(String a, int len) {
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            switch(state) {
                case 0:
                    if (c == '+') state = 1; else if (c == '0') state = 2; else if (isNonSeparator(c)) return false;
                    break;
                case 2:
                    if (c == '0') state = 3; else if (c == '1') state = 4; else if (isNonSeparator(c)) return false;
                    break;
                case 4:
                    if (c == '1') state = 5; else if (isNonSeparator(c)) return false;
                    break;
                default:
                    if (isNonSeparator(c)) return false;
                    break;
            }
        }
        return state == 1 || state == 3 || state == 5;
    }

    /** all of 'a' up to len must be a (+|00|011)country code)
     *  We're fast and loose with the country code. Any \d{1,3} matches */
    private static boolean matchIntlPrefixAndCC(String a, int len) {
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            switch(state) {
                case 0:
                    if (c == '+') state = 1; else if (c == '0') state = 2; else if (isNonSeparator(c)) return false;
                    break;
                case 2:
                    if (c == '0') state = 3; else if (c == '1') state = 4; else if (isNonSeparator(c)) return false;
                    break;
                case 4:
                    if (c == '1') state = 5; else if (isNonSeparator(c)) return false;
                    break;
                case 1:
                case 3:
                case 5:
                    if (isISODigit(c)) state = 6; else if (isNonSeparator(c)) return false;
                    break;
                case 6:
                case 7:
                    if (isISODigit(c)) state++; else if (isNonSeparator(c)) return false;
                    break;
                default:
                    if (isNonSeparator(c)) return false;
            }
        }
        return state == 6 || state == 7 || state == 8;
    }

    /** all of 'a' up to len must match non-US trunk prefix ('0') */
    private static boolean matchTrunkPrefix(String a, int len) {
        boolean found;
        found = false;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            if (c == '0' && !found) {
                found = true;
            } else if (isNonSeparator(c)) {
                return false;
            }
        }
        return found;
    }

    private static final boolean COUNTLY_CALLING_CALL[] = { true, true, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, true, true, false, true, true, true, true, true, false, true, false, false, true, true, false, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, false, true, false, false, true, true, true, true, true, true, true, false, false, true, false };

    private static final int CCC_LENGTH = COUNTLY_CALLING_CALL.length;

    /**
     * @return true when input is valid Country Calling Code.
     */
    private static boolean isCountryCallingCode(int countryCallingCodeCandidate) {
        return countryCallingCodeCandidate > 0 && countryCallingCodeCandidate < CCC_LENGTH && COUNTLY_CALLING_CALL[countryCallingCodeCandidate];
    }

    /**
     * Returns interger corresponding to the input if input "ch" is
     * ISO-LATIN characters 0-9.
     * Returns -1 otherwise
     */
    private static int tryGetISODigit(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        } else {
            return -1;
        }
    }

    private static class CountryCallingCodeAndNewIndex {

        public final int countryCallingCode;

        public final int newIndex;

        public CountryCallingCodeAndNewIndex(int countryCode, int newIndex) {
            this.countryCallingCode = countryCode;
            this.newIndex = newIndex;
        }
    }

    private static CountryCallingCodeAndNewIndex tryGetCountryCallingCodeAndNewIndex(String str, boolean acceptThailandCase) {
        int state = 0;
        int ccc = 0;
        final int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch(state) {
                case 0:
                    if (ch == '+') state = 1; else if (ch == '0') state = 2; else if (ch == '1') {
                        if (acceptThailandCase) {
                            state = 8;
                        } else {
                            return null;
                        }
                    } else if (isDialable(ch)) {
                        return null;
                    }
                    break;
                case 2:
                    if (ch == '0') state = 3; else if (ch == '1') state = 4; else if (isDialable(ch)) {
                        return null;
                    }
                    break;
                case 4:
                    if (ch == '1') state = 5; else if (isDialable(ch)) {
                        return null;
                    }
                    break;
                case 1:
                case 3:
                case 5:
                case 6:
                case 7:
                    {
                        int ret = tryGetISODigit(ch);
                        if (ret > 0) {
                            ccc = ccc * 10 + ret;
                            if (ccc >= 100 || isCountryCallingCode(ccc)) {
                                return new CountryCallingCodeAndNewIndex(ccc, i + 1);
                            }
                            if (state == 1 || state == 3 || state == 5) {
                                state = 6;
                            } else {
                                state++;
                            }
                        } else if (isDialable(ch)) {
                            return null;
                        }
                    }
                    break;
                case 8:
                    if (ch == '6') state = 9; else if (isDialable(ch)) {
                        return null;
                    }
                    break;
                case 9:
                    if (ch == '6') {
                        return new CountryCallingCodeAndNewIndex(66, i + 1);
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Currently this function simply ignore the first digit assuming it is
     * trunk prefix. Actually trunk prefix is different in each country.
     *
     * e.g.
     * "+79161234567" equals "89161234567" (Russian trunk digit is 8)
     * "+33123456789" equals "0123456789" (French trunk digit is 0)
     *
     */
    private static int tryGetTrunkPrefixOmittedIndex(String str, int currentIndex) {
        int length = str.length();
        for (int i = currentIndex; i < length; i++) {
            final char ch = str.charAt(i);
            if (tryGetISODigit(ch) >= 0) {
                return i + 1;
            } else if (isDialable(ch)) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Return true if the prefix of "str" is "ignorable". Here, "ignorable" means
     * that "str" has only one digit and separater characters. The one digit is
     * assumed to be trunk prefix.
     */
    private static boolean checkPrefixIsIgnorable(final String str, int forwardIndex, int backwardIndex) {
        boolean trunk_prefix_was_read = false;
        while (backwardIndex >= forwardIndex) {
            if (tryGetISODigit(str.charAt(backwardIndex)) >= 0) {
                if (trunk_prefix_was_read) {
                    return false;
                } else {
                    trunk_prefix_was_read = true;
                }
            } else if (isDialable(str.charAt(backwardIndex))) {
                return false;
            }
            backwardIndex--;
        }
        return true;
    }
}
