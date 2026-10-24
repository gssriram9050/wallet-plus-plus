package util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Small helper methods that format text for the console. */
public final class Format {

    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");

    private Format() {
        // utility class - no objects needed
    }

    /** 1500 -> "Rs. 1,500.00" (plain "Rs." is used because the Windows CMD cannot always show the rupee symbol). */
    public static String money(BigDecimal value) { return "Rs. " + number(value); }

    /** 1500 -> "1,500.00" */
    public static String number(BigDecimal value) { return String.format(Locale.US, "%,.2f", value); }
    public static String dateTime(LocalDateTime time) { return time.format(LONG_DATE); }
    public static String shortDateTime(LocalDateTime time) { return time.format(SHORT_DATE); }

    /** Repeats one character, e.g. line('=', 5) -> "=====". */
    public static String line(char ch, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /** "Label          : value" with the label padded to a fixed width. */
    public static String row(String label, Object value) { return String.format("%-16s: %s", label, value); }

    public static String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int left = (width - text.length()) / 2;
        return line(' ', left) + text;
    }

    /** Cuts long text so table columns stay aligned. */
    public static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
