/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ulan.timetable.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ulan.timetable.R;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.model.Week;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class WeekUtils {
    @Nullable
    public static Week getNextWeek(@NonNull ArrayList<Week> weeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
        String hour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        if (hour.length() < 2)
            hour = "0" + hour;
        String minutes = "" + calendar.get(Calendar.MINUTE);
        if (minutes.length() < 2)
            minutes = "0" + minutes;
        String now = hour + ":" + minutes;

        for (int i = 0; i < weeks.size(); i++) {
            Week week = weeks.get(i);
            if ((now.compareToIgnoreCase(week.getFromTime()) >= 0 && now.compareToIgnoreCase(week.getToTime()) <= 0) || now.compareToIgnoreCase(week.getToTime()) <= 0) {
                return week;
            }
        }
        return null;
    }

    @NonNull
    public static ArrayList<Week> getAllWeeks(@NonNull DbHelper dbHelper) {
        String[] keys = new String[]{WeekdayFragment.KEY_MONDAY_FRAGMENT,
                WeekdayFragment.KEY_TUESDAY_FRAGMENT,
                WeekdayFragment.KEY_WEDNESDAY_FRAGMENT,
                WeekdayFragment.KEY_THURSDAY_FRAGMENT,
                WeekdayFragment.KEY_FRIDAY_FRAGMENT,
                WeekdayFragment.KEY_SATURDAY_FRAGMENT,
                WeekdayFragment.KEY_SUNDAY_FRAGMENT};

        Calendar calendar = Calendar.getInstance();
        ArrayList<Week> customWeeks = getWeeks(dbHelper, keys, calendar);

        calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR) - 1);
        customWeeks.addAll(getWeeks(dbHelper, keys, calendar));
        return removeDuplicates(customWeeks);
    }

    @NonNull
    private static ArrayList<Week> getWeeks(@NonNull DbHelper dbHelper, @NonNull String[] keys, Calendar calendar) {
        ArrayList<Week> weeks = new ArrayList<>();
        for (String key : keys) {
            weeks.addAll(dbHelper.getWeek(key, calendar));
        }
        return weeks;
    }

    @NotNull
    public static ArrayList<Week> getPreselection(@NonNull AppCompatActivity activity) {
        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> customWeeks = getAllWeeks(dbHelper);

        ArrayList<String> subjects = new ArrayList<>();
        for (Week w : customWeeks) {
            subjects.add(w.getSubject().toUpperCase());
        }

        ArrayList<String> preselectedValues = new ArrayList<>(Arrays.asList(activity.getResources().getStringArray(R.array.preselected_subjects_values)));
        String[] preselected = PreferenceUtil.getPreselectionElements(activity);

        int[] preselectedColors = activity.getResources().getIntArray(R.array.preselected_subjects_colors);
        String[] preselectedLanguage = activity.getResources().getStringArray(R.array.preselected_subjects);

        for (int i = 0; i < preselected.length; i++) {
            if (preselectedValues.contains(preselected[i])) {
                String langValue = preselectedLanguage[preselectedValues.indexOf(preselected[i])];
                if (!subjects.contains(langValue.toUpperCase()))
                    customWeeks.add(0, new Week(langValue, "", "", "", "", preselectedColors[i]));
            }
        }

        Collections.sort(customWeeks, (week1, week2) -> week1.getSubject().compareToIgnoreCase(week2.getSubject()));
        return customWeeks;
    }

    @NonNull
    private static ArrayList<Week> removeDuplicates(@NonNull ArrayList<Week> weeks) {
        ArrayList<Week> returnValue = new ArrayList<>();
        ArrayList<String> returnValueSubjects = new ArrayList<>();
        for (Week w : weeks) {
            if (!returnValueSubjects.contains(w.getSubject().toUpperCase())) {
                returnValue.add(w);
                returnValueSubjects.add(w.getSubject().toUpperCase());
            }
        }
        return returnValue;
    }

    public static int getMatchingScheduleBegin(String time, Context context) {
        return getMatchingScheduleBegin(time, PreferenceUtil.getStartTime(context), PreferenceUtil.getPeriodLength(context));
    }

    public static int getMatchingScheduleBegin(String time, int[] startTime, int lessonDuration) {
        int schedule = getDurationOfWeek(new Week("", "", "", startTime[0] + ":" + (startTime[1] - 1), time, 0), false, lessonDuration);
        if (schedule == 0)
            return 1;
        else
            return schedule;
    }

    public static int getMatchingScheduleEnd(String time, Context context) {
        return getMatchingScheduleEnd(time, PreferenceUtil.getStartTime(context), PreferenceUtil.getPeriodLength(context));
    }

    public static int getMatchingScheduleEnd(String time, int[] startTime, int lessonDuration) {
        int schedule = getDurationOfWeek(new Week("", "", "", startTime[0] + ":" + startTime[1], time, 0), false, lessonDuration);
        if (schedule == 0)
            return 1;
        else
            return schedule;
    }

    @NonNull
    public static String getMatchingTimeBegin(int hour, Context context) {
        return getMatchingTimeBegin(hour, PreferenceUtil.getStartTime(context), PreferenceUtil.getPeriodLength(context));
    }

    @NonNull
    public static String getMatchingTimeBegin(int hour, int[] startTime, int lessonDuration) {
        Calendar startOfSchool = Calendar.getInstance();
        startOfSchool.set(Calendar.HOUR_OF_DAY, startTime[0]);
        startOfSchool.set(Calendar.MINUTE, startTime[1]);
        startOfSchool.setTimeInMillis(startOfSchool.getTimeInMillis() + (hour - 1) * lessonDuration * 60 * 1000);

        return String.format(Locale.getDefault(), "%02d:%02d", startOfSchool.get(Calendar.HOUR_OF_DAY), startOfSchool.get(Calendar.MINUTE));
    }

    @NonNull
    public static String getMatchingTimeEnd(int hour, Context context) {
        return getMatchingTimeEnd(hour, PreferenceUtil.getStartTime(context), PreferenceUtil.getPeriodLength(context));
    }

    @NonNull
    public static String getMatchingTimeEnd(int hour, int[] startTime, int lessonDuration) {
        Calendar startOfSchool = Calendar.getInstance();
        startOfSchool.set(Calendar.HOUR_OF_DAY, startTime[0]);
        startOfSchool.set(Calendar.MINUTE, startTime[1]);
        startOfSchool.setTimeInMillis(startOfSchool.getTimeInMillis() + (hour) * lessonDuration * 60 * 1000);

        return String.format(Locale.getDefault(), "%02d:%02d", startOfSchool.get(Calendar.HOUR_OF_DAY), startOfSchool.get(Calendar.MINUTE));
    }

    public static int getDurationOfWeek(@NonNull Week w, boolean countOnlyIfFitsLessonsTime, int lessonDuration) {
        Calendar weekCalendarStart = Calendar.getInstance();
        int startHour = Integer.parseInt(w.getFromTime().substring(0, w.getFromTime().indexOf(":")));
        weekCalendarStart.set(Calendar.HOUR_OF_DAY, startHour);
        int startMinute = Integer.parseInt(w.getFromTime().substring(w.getFromTime().indexOf(":") + 1));
        weekCalendarStart.set(Calendar.MINUTE, startMinute);

        Calendar weekCalendarEnd = Calendar.getInstance();
        int endHour = Integer.parseInt(w.getToTime().substring(0, w.getToTime().indexOf(":")));
        weekCalendarEnd.set(Calendar.HOUR_OF_DAY, endHour);
        int endMinute = Integer.parseInt(w.getToTime().substring(w.getToTime().indexOf(":") + 1));
        weekCalendarEnd.set(Calendar.MINUTE, endMinute);

        long differencesInMillis = weekCalendarEnd.getTimeInMillis() - weekCalendarStart.getTimeInMillis();
        int inMinutes = (int) (differencesInMillis / 1000 / 60);

        if (inMinutes < lessonDuration && countOnlyIfFitsLessonsTime)
            return 0;

        int multiplier;
        if (inMinutes % lessonDuration > 0 && !countOnlyIfFitsLessonsTime) {
            multiplier = inMinutes / lessonDuration + 1;
        } else
            multiplier = inMinutes / lessonDuration;

        return multiplier;
    }

    public static boolean isEvenWeek(@NonNull Calendar termStart1, @NonNull Calendar now1, boolean startOnSunday) {
        boolean isEven = true;

        Calendar termStart = Calendar.getInstance(Locale.GERMAN);
        termStart.setTimeInMillis(termStart1.getTimeInMillis());
        Calendar now = Calendar.getInstance(Locale.GERMAN);
        now.setTimeInMillis(now1.getTimeInMillis());

        int weekDifference = now.get(Calendar.WEEK_OF_YEAR) - termStart.get(Calendar.WEEK_OF_YEAR);
        if (weekDifference < 0) {
            weekDifference = -weekDifference;
        }

        for (int i = 0; i < weekDifference; i++) {
            isEven = !isEven;
        }

        if (startOnSunday) {
            if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                isEven = !isEven;
            }
        }

        return isEven;
    }

    public static String localizeDate(Context context, String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return localizeDate(context, dateFormat.parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    public static String localizeDate(Context context, Date date) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return dateFormat.format(date);
    }

    public static String localizeTime(Context context, String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        try {
            return localizeTime(context, dateFormat.parse(time));
        } catch (Exception e) {
            return time;
        }
    }

    private static String localizeTime(Context context, Date date) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);
        return dateFormat.format(date);
    }
}
