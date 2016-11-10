package io.sympli.find_e.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import io.sympli.find_e.R;

public final class DateUtil {

    public static String getLastConnectionTime(Context context, Date date) {
        if (date == null) {
            return "";
        }
        Calendar today = Calendar.getInstance();
        Calendar dateToParse = Calendar.getInstance();
        dateToParse.setTime(date);
        if (dateToParse.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            if (dateToParse.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                if (dateToParse.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                    int minutesDiff = (today.get(Calendar.HOUR_OF_DAY) * 60 +
                            today.get(Calendar.MINUTE)) -
                            (dateToParse.get(Calendar.HOUR_OF_DAY) * 60 +
                                    dateToParse.get(Calendar.MINUTE));
                    if (minutesDiff >= 60) {
                        int hoursDiff = today.get(Calendar.HOUR_OF_DAY) - dateToParse.get(Calendar.HOUR_OF_DAY);
                        return String.format(context.getResources().
                                getQuantityString(R.plurals.label_hours, hoursDiff), hoursDiff);
                    } else {
                        if (minutesDiff == 0) {
                            return context.getString(R.string.label_minutes_zero);
                        }
                        return String.format(context.getResources().
                                getQuantityString(R.plurals.label_minutes, minutesDiff), minutesDiff);
                    }
                } else {
                    int daysDiff = today.get(Calendar.DAY_OF_MONTH) - dateToParse.get(Calendar.DAY_OF_MONTH);
                    return String.format(context.getResources().
                            getQuantityString(R.plurals.label_days, daysDiff), daysDiff);
                }
            } else {
                int monthsDiff = today.get(Calendar.MONTH) - dateToParse.get(Calendar.MONTH);
                return String.format(context.getResources().
                        getQuantityString(R.plurals.label_months, monthsDiff), monthsDiff);
            }
        } else {
            int yearsDiff = today.get(Calendar.YEAR) - dateToParse.get(Calendar.YEAR);
            return String.format(context.getResources().
                    getQuantityString(R.plurals.label_years, yearsDiff), yearsDiff);
        }
    }
}
