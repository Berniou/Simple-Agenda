package com.simpleagenda.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static long startOfDayMillis(long utcMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(utcMillis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long todayStartMillis() {
        return startOfDayMillis(System.currentTimeMillis());
    }

    public static String formatDayTitle(long dayMillis, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM", locale);
        return sdf.format(dayMillis);
    }

    public static String formatTime(int minutesFromMidnight) {
        int h = minutesFromMidnight / 60;
        int m = minutesFromMidnight % 60;
        return String.format(Locale.FRANCE, "%02d:%02d", h, m);
    }

    public static int hoursToMinutes(int hours) {
        return hours * 60;
    }
}
