package com.redtop.engaze.common.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.Nullable;

public class DateUtil {

    public static String dateTimeFormatPattern = "yyyy-MM-dd'T'HH:mm:ss";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormatPattern);

    public static String getDayOfWeek(Calendar cal) {
        int result = cal.get(Calendar.DAY_OF_WEEK);
        String day = null;
        switch (result) {
            case Calendar.MONDAY:
                day = "MON";
                break;
            case Calendar.TUESDAY:
                day = "TUE";
                break;
            case Calendar.WEDNESDAY:
                day = "WED";
                break;
            case Calendar.THURSDAY:
                day = "THU";
                break;
            case Calendar.FRIDAY:
                day = "FRI";
                break;
            case Calendar.SATURDAY:
                day = "SAT";
                break;
            case Calendar.SUNDAY:
                day = "SUN";
                break;
        }
        return day;
    }

    public static String getDayOfMonth(Calendar cal) {
        int result = cal.get(Calendar.DAY_OF_MONTH);
        return Integer.toString(result);
    }

    public static String getYear(Calendar cal) {
        int result = cal.get(Calendar.YEAR);
        return Integer.toString(result);
    }

    public static String getTimeInHHMMa(String strDate, String strOriginalFormat) {
        String time = "";
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat(strOriginalFormat);
            Date date;
            date = originalFormat.parse(strDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
            time = formatDate.format(cal.getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return time;
    }

    public static String getTime(Calendar cal) {
        SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
        String time = formatDate.format(cal.getTime());
        return time;
    }

    public static String getShortMonth(Calendar cal) {
        int result = cal.get(Calendar.MONTH);
        String month = null;
        switch (result) {
            case Calendar.JANUARY:
                month = "JAN";
                break;
            case Calendar.FEBRUARY:
                month = "FEB";
                break;
            case Calendar.MARCH:
                month = "MAR";
                break;
            case Calendar.APRIL:
                month = "APR";
                break;
            case Calendar.MAY:
                month = "MAY";
                break;
            case Calendar.JUNE:
                month = "JUN";
                break;
            case Calendar.JULY:
                month = "JUL";
                break;
            case Calendar.AUGUST:
                month = "AUG";
                break;
            case Calendar.SEPTEMBER:
                month = "SEP";
                break;
            case Calendar.OCTOBER:
                month = "OCT";
                break;
            case Calendar.NOVEMBER:
                month = "NOV";
                break;
            case Calendar.DECEMBER:
                month = "DEC";
                break;
        }
        return month;
    }

    public static String ConvertDateToString(Date date) {
        try {
            return dateFormat.format(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Date ConvertStringToDate(String strDate) {
        try {
            return dateFormat.parse(strDate);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getUtcDateTime(Calendar cal) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TimeZone utcZone = TimeZone.getTimeZone("UTC");
            simpleDateFormat.setTimeZone(utcZone);
            Date myDate = simpleDateFormat.parse(cal.getTime().toString());

            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            return simpleDateFormat.format(myDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static String convertUtcToLocalDateTime(String inputDateTime, @Nullable SimpleDateFormat simpleDateFormat) {
        try {
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat(dateTimeFormatPattern);
            }
            TimeZone utcZone = TimeZone.getTimeZone("UTC");
            simpleDateFormat.setTimeZone(utcZone);
            Date myDate = simpleDateFormat.parse(inputDateTime);

            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(simpleDateFormat.toPattern());
            String localDate = simpleDateFormat1.format(myDate);
            return localDate;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }

    public static String convertUtcToLocalDateTime(Date inputDateTime, @Nullable SimpleDateFormat simpleDateFormat) {
        try {
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            }
            TimeZone utcZone = TimeZone.getTimeZone("UTC");


            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(simpleDateFormat.toPattern());
            String localDate = simpleDateFormat1.format(inputDateTime);
            return localDate;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }

    public static Date convertToLocalDateTime(String inputDateTime, SimpleDateFormat simpleDateFormat) {
        try {
            TimeZone utcZone = TimeZone.getTimeZone("UTC");
            simpleDateFormat.setTimeZone(utcZone);
            Date myDate = simpleDateFormat.parse(inputDateTime);
            return myDate;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public static String convertToUtcDateTime(Date inputDateTime, @Nullable SimpleDateFormat simpleDateFormat) {

        try {
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            }
            TimeZone utcZone = TimeZone.getTimeZone("UTC");
            simpleDateFormat.setTimeZone(utcZone);
            String utcDate = simpleDateFormat.format(inputDateTime);
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            return utcDate;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }

    public static String convertToUtcDateTime(String inputDateTime, SimpleDateFormat simpleDateFormat) {
        try {
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            Date myDate = simpleDateFormat.parse(inputDateTime);

            TimeZone utcZone = TimeZone.getTimeZone("UTC");
            simpleDateFormat.setTimeZone(utcZone);
            String utcDate = simpleDateFormat.format(myDate);
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            return utcDate;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }

    public static Boolean compareDate(Date d1, Date d2) {
        if (d1.getTime() > d2.getTime()) {
            return true;
        }
        return false;
    }

    public static String getDurationText(long diffMinutes) {
        String durationText = " ";
        int days = (int) diffMinutes / 24 / 60;
        int Hours = (int) diffMinutes / 60 % 24;
        int minutes = (int) diffMinutes % 60;
        if (days > 0) {
            if (days > 1) {
                durationText += days + " days ";
            } else {
                durationText += days + " day ";
            }
        }
        if (Hours > 0) {
            if (Hours > 1) {
                durationText += Hours + " hours ";
            } else {
                durationText += Hours + " hour ";
            }
        }

        if (minutes > 0 && days == 0) {
            if (minutes > 1) {
                durationText += minutes + " mins";
            } else {
                durationText += minutes + " min";
            }
        } else if (durationText.equals(" ")) {
            durationText = "0";
        }

        durationText = durationText.trim();
        return durationText;
    }
}
