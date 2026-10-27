package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable snapshot of the data shown on one job card.
 * All parsing lives here so it can be unit tested without a browser.
 */
public record JobCard(
        String jobId,
        String title,
        String experience,
        String salary,
        List<String> tags,
        String postedText) {

    private static final String NUM = "(\\d+(?:\\.\\d+)?)";
    private static final Pattern RANGE = Pattern.compile(NUM + "\\s*-\\s*" + NUM);
    private static final Pattern SINGLE = Pattern.compile(NUM);
    private static final Pattern DAYS = Pattern.compile("(\\d+)\\+?\\s*day");

    /** Returned when the posting age cannot be determined (never matches). */
    public static final int UNKNOWN_DAYS = Integer.MAX_VALUE;

    public JobCard {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public double maxExperience() {
        return maxValue(experience);
    }

    public double maxSalaryLpa() {
        double value = maxValue(salary);
        // "1,00,000-2,00,000 PA" style values are in rupees, convert to lakhs.
        return value >= 1000 ? value / 100_000 : value;
    }

    public boolean isSalaryUndisclosed() {
        return salary != null
                && salary.toLowerCase(Locale.ROOT).contains("not disclosed");
    }

    /**
     * "Just now" / "Few hours ago" / "Today" -> 0
     * "1 Day Ago" -> 1, "30+ Days Ago" -> 30, anything else -> UNKNOWN_DAYS
     */
    public int daysSincePosted() {
        if (postedText == null || postedText.isBlank()) {
            return UNKNOWN_DAYS;
        }
        String text = postedText.toLowerCase(Locale.ROOT);

        if (text.contains("just now") || text.contains("today")
                || text.contains("hour") || text.contains("minute")) {
            return 0;
        }
        Matcher m = DAYS.matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : UNKNOWN_DAYS;
    }

    /**
     * "4-8 Yrs" -> 8, "5-12 Lacs PA" -> 12, "6 Yrs" -> 6,
     * "3.5-5.5 Lacs" -> 5.5, "Not disclosed" -> -1
     */
    static double maxValue(String text) {
        if (text == null || text.isBlank()) {
            return -1;
        }
        String cleaned = text.replace(",", "");

        Matcher range = RANGE.matcher(cleaned);
        if (range.find()) {
            return Double.parseDouble(range.group(2));
        }
        Matcher single = SINGLE.matcher(cleaned);
        return single.find() ? Double.parseDouble(single.group(1)) : -1;
    }
}
