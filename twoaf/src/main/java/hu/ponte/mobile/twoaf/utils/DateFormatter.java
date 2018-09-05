package hu.ponte.mobile.twoaf.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
    private static final SimpleDateFormat[] DEFAULT_PATTERNS = new SimpleDateFormat[]{
            new SimpleDateFormat(PATTERN_RFC1036, Locale.US),
            new SimpleDateFormat(PATTERN_RFC1123, Locale.US),
            new SimpleDateFormat(PATTERN_ASCTIME, Locale.US)
    };

    static {
        for (SimpleDateFormat dateFormatter : DEFAULT_PATTERNS) {
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    public static long getDateInMillis(String dateTime) {
        return getDateInMillis(DEFAULT_PATTERNS, dateTime);
    }

    public static long getDateInMillis(SimpleDateFormat[] dateFormats, String dateTime) {
        if (dateTime == null || dateTime.isEmpty())
            throw new TwoafException(null, TwoafException.TwoafReason.EMPTY_DATE_FIELD);
        dateTime = trimDate(dateTime);

        for (SimpleDateFormat dateFormat : dateFormats) {
            long date = getDateInMillis(dateFormat, dateTime);
            if (date != -1) return date;
        }
        throw new TwoafException(null, TwoafException.TwoafReason.NO_DATE_FORMAT);
    }

    public static long getDateInMillis(SimpleDateFormat dateFormat, String dateTime) {
        try {
            Date date = dateFormat.parse(dateTime);
            return date.getTime();
        } catch (ParseException e) {// Ignore exception}
            return -1;
        }
    }

    private static String trimDate(String dateValue) {
        // trim single quotes around date if present -- see apache issue #5279
        if (dateValue.length() > 1 && dateValue.startsWith("'") && dateValue.endsWith("'"))
            dateValue = dateValue.substring(1, dateValue.length() - 1);
        return dateValue;
    }
}
