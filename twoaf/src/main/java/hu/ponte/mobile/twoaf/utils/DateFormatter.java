package hu.ponte.mobile.twoaf.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.ponte.mobile.twoaf.exception.TwoafException;

public class DateFormatter {
    // Based on: https://tools.ietf.org/html/rfc7231#section-7.1.1.1
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime()</code> format.
     */
    public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
    private static final String[] DEFAULT_PATTERNS = new String[]{
            PATTERN_RFC1036,
            PATTERN_RFC1123,
            PATTERN_ASCTIME
    };

    public static long getDateInMillis(String dateTime) {
        return getDateInMillis(DEFAULT_PATTERNS, dateTime);
    }

    public static long getDateInMillis(String[] dateFormats, String dateTime) {
        for (String dateFormat : dateFormats) {
            long date = getDateInMillis(dateFormat, dateTime);
            if (date != -1) return date;
        }
        throw new TwoafException(null, TwoafException.TwoafReason.NO_DATE_FORMAT);
    }

    public static long getDateInMillis(String dateFormat, String dateTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
            Date date = formatter.parse(dateTime);
            return date.getTime();
        } catch (ParseException e) {// Ignore exception}
            return -1;
        }
    }
}
