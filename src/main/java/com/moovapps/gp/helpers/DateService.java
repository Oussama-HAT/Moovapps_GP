package com.moovapps.gp.helpers;

import com.axemble.vdoc.sdk.structs.Period;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateService {
    private static final long MILLISECONDS_PER_DAY = 86400000L;

    public enum MoisEmun {
        _0("0", "janvier"),
        _1("1", "février"),
        _2("2", "mars"),
        _3("3", "avril"),
        _4("4", "mai"),
        _5("5", "juin"),
        _6("6", "juillet"),
        _7("7", "août"),
        _8("8", "septembre"),
        _9("9", "octobre"),
        _10("10", "novembre"),
        _11("11", "décembre");

        protected String key;

        protected String label;

        MoisEmun(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public String getKey() {
            return this.key;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public static String byMonthEnLettre(Date date) {
        String month = null;
        byte b;
        int i;
        MoisEmun[] arrayOfMoisEmun;
        for (i = (arrayOfMoisEmun = MoisEmun.values()).length, b = 0; b < i; ) {
            MoisEmun m = arrayOfMoisEmun[b];
            if (Integer.parseInt(m.key) == getMonth(date)) {
                month = m.getLabel();
                break;
            }
            b++;
        }
        return month;
    }

    public static int getWeekOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(3);
    }

    public static int getWeekOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(4);
    }

    public static String getdayEnLettre(Date date) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int intDay = cal.get(7);
            String day = null;
            switch (intDay) {
                case 2:
                    day = "Lundi";
                    break;
                case 3:
                    day = "Mardi";
                    break;
                case 4:
                    day = "Mercredi";
                    break;
                case 5:
                    day = "Jeudi";
                    break;
                case 6:
                    day = "Vendredi";
                    break;
                case 7:
                    day = "Samedi";
                    break;
                case 1:
                    day = "Dimanche";
                    break;
            }
            return day;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static float getCountDaysBetweenDates(Date startDate, Date endDate) {
        if (startDate != null && endDate != null)
            return (float) ((endDate.getTime() - startDate.getTime()) / 86400000L + 1L);
        return 0.0F;
    }

    public static boolean isNotWEDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(7);
        if (dayOfWeek % 7 < 2)
            return false;
        return true;
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(5, days);
        return cal.getTime();
    }

    public static boolean isInPeriod(Date date, Period period) {
        long dateTime = date.getTime();
        long startDateTime = period.getStartDate().getTime();
        long endDateTime = period.getEndDate().getTime();
        if (startDateTime <= dateTime && endDateTime >= dateTime)
            return true;
        return false;
    }

    public static int getDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(5);
    }

    public static int getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(2, -1);
        return cal.getActualMaximum(5);
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(2);
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(1);
    }

    private static void clearTime(Calendar cal) {
        cal.set(11, 0);
        cal.clear(12);
        cal.clear(13);
        cal.clear(14);
    }

    public static Date clearDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        clearTime(cal);
        return cal.getTime();
    }

    public static Period clearPeriode(Period period) {
        return new Period(clearDate(period.getStartDate()), clearDate(period.getEndDate()));
    }

    public static String formatDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(date);
    }
}
