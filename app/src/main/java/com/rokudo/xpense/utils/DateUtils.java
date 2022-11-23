package com.rokudo.xpense.utils;

import com.rokudo.xpense.models.TransEntry;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateUtils {
    public static SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());

    public static List<TransEntry> getLast7Days(SimpleDateFormat dayOfMonthFormat) {
        List<TransEntry> transEntryList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0, 0);
        Date today = calendar.getTime();

        for (int i = 0; i < 7; i++) {
            Date date = new Date(today.getTime() - Duration.ofDays(i).toMillis());
            transEntryList.add(new TransEntry(dayOfMonthFormat.format(date), date, 0f));
        }

        return transEntryList;
    }
}
