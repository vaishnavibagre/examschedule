package com.ulan.timetable.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pd.chocobar.ChocoBar;
import com.ulan.timetable.R;
import com.ulan.timetable.adapters.ExamsAdapter;
import com.ulan.timetable.adapters.FragmentsTabAdapter;
import com.ulan.timetable.adapters.TeachersAdapter;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.model.Exam;
import com.ulan.timetable.model.Homework;
import com.ulan.timetable.model.Note;
import com.ulan.timetable.model.Teacher;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.receivers.DoNotDisturbReceiversKt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.views.picker.RGBPickerView;


/**
 * Created by Ulan on 22.10.2018.
 */
public class AlertDialogsHelper {
    //TODO: Rewrite Dialogs to and returning a dialog object, without activity

    public static void getEditSubjectDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull Runnable runOnSafe, @NonNull final Week week) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subject_dialog);
        editTextHashs.put(R.string.subject, subject);
        final EditText teacher = alertLayout.findViewById(R.id.teacher_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.room_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        final TextView from_hour = alertLayout.findViewById(R.id.from_hour);
        final TextView to_hour = alertLayout.findViewById(R.id.to_hour);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(week.getColor(), Color.WHITE, Color.BLACK));

        subject.setText(week.getSubject());
        teacher.setText(week.getTeacher());
        room.setText(week.getRoom());
        from_time.setText(WeekUtils.localizeTime(activity, week.getFromTime()));
        to_time.setText(WeekUtils.localizeTime(activity, week.getToTime()));
        from_hour.setText("" + WeekUtils.getMatchingScheduleBegin(week.getFromTime(), activity));
        to_hour.setText("" + WeekUtils.getMatchingScheduleEnd(week.getToTime(), activity));
        select_color.setBackgroundColor(week.getColor() != 0 ? week.getColor() : Color.WHITE);

        from_time.setOnClickListener(v -> {
            int mHour = Integer.parseInt(week.getFromTime().substring(0, week.getFromTime().indexOf(":")));
            int mMinute = Integer.parseInt(week.getFromTime().substring(week.getFromTime().indexOf(":") + 1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        from_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setFromTime(newTime);
                        from_hour.setText("" + WeekUtils.getMatchingScheduleBegin(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleBegin(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute), activity);
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        to_time.setOnClickListener(v -> {
            int mHour = Integer.parseInt(week.getToTime().substring(0, week.getToTime().indexOf(":")));
            int mMinute = Integer.parseInt(week.getToTime().substring(week.getToTime().indexOf(":") + 1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute1) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        to_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setToTime(newTime);
                        to_hour.setText("" + WeekUtils.getMatchingScheduleEnd(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleEnd(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1), activity);
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        from_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Integer.parseInt(from_hour.getText().toString()));
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                        week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                        from_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        to_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Integer.parseInt(to_hour.getText().toString()));
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                        week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                        to_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));


        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_subject);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            from_time.setText(R.string.select_start_time);
            to_time.setText(R.string.select_end_time);
            from_hour.setText(R.string.lesson);
            to_hour.setText(R.string.lesson);
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            from_hour.setText(R.string.lesson);
            to_hour.setText(R.string.lesson);
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) /*|| TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            } else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                week.setSubject(subject.getText().toString());
                week.setTeacher(teacher.getText().toString());
                week.setRoom(room.getText().toString());
                week.setColor(buttonColor.getColor());
                dbHelper.updateWeek(week);
                runOnSafe.run();
                databaseChanged(activity);
                dialog.dismiss();
            }
        });
    }

    public static void getAddSubjectDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final FragmentsTabAdapter adapter, @NonNull final ViewPager viewPager) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subject_dialog);
        subject.requestFocus();
        editTextHashs.put(R.string.subject, subject);
        final EditText teacher = alertLayout.findViewById(R.id.teacher_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.room_dialog);
//        editTextHashs.put(R.string.room, room);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.7f
        );

        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        from_time.setLayoutParams(params);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        to_time.setLayoutParams(params);
        final TextView from_hour = alertLayout.findViewById(R.id.from_hour);
        from_hour.setLayoutParams(params);
        final TextView to_hour = alertLayout.findViewById(R.id.to_hour);
        to_hour.setLayoutParams(params);

        if (PreferenceUtil.showTimes(activity)) {
            from_time.setVisibility(View.VISIBLE);
            to_time.setVisibility(View.VISIBLE);
            from_hour.setVisibility(View.GONE);
            to_hour.setVisibility(View.GONE);
        } else {
            from_time.setVisibility(View.GONE);
            to_time.setVisibility(View.GONE);
            from_hour.setVisibility(View.VISIBLE);
            to_hour.setVisibility(View.VISIBLE);
        }

        from_hour.setText(R.string.select_start_time);
        to_hour.setText(R.string.select_end_time);

        final Button select_color = alertLayout.findViewById(R.id.select_color);

        final Week week = new Week();

        from_time.setOnClickListener(v -> {
            int mHour, mMinute;
            try {
                String time = from_time.getText().toString();
                mHour = Integer.parseInt(time.substring(0, time.indexOf(":")));
                mMinute = Integer.parseInt(time.substring(time.indexOf(":") + 1));
            } catch (Exception ignore) {
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        from_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setFromTime(newTime);
                        from_hour.setText("" + WeekUtils.getMatchingScheduleBegin(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleBegin(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute), activity);
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        to_time.setOnClickListener(v -> {
            int hour, minute;
            try {
                String time = WeekUtils.getMatchingTimeEnd(WeekUtils.getMatchingScheduleBegin(from_time.getText().toString(), activity), activity);
                hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
                minute = Integer.parseInt(time.substring(time.indexOf(":") + 1));
            } catch (Exception ignore) {
                final Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute1) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        to_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setToTime(newTime);
                        to_hour.setText("" + WeekUtils.getMatchingScheduleEnd(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleEnd(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1), activity);
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, hour, minute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        from_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            try {
                numberPicker.setValue(Integer.parseInt(from_hour.getText().toString()));
            } catch (Exception ignore) {
            }
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                        week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                        from_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        to_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            try {
                numberPicker.setValue(Integer.parseInt(from_hour.getText().toString()) + 1);
            } catch (Exception ignore) {
            }
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                        week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                        to_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_subject);
        alert.setCancelable(false);
        Button cancel = alertLayout.findViewById(R.id.cancel);
        Button submit = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();

        //Preselection
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (PreferenceUtil.isPreselectionList(activity)) {
                ArrayList<Week> customWeeks = WeekUtils.getPreselection(activity);

                ArrayList<String> subjects = new ArrayList<>();
                for (Week w : customWeeks) {
                    subjects.add(w.getSubject());
                }

                new MaterialDialog.Builder(activity)
                        .title(R.string.pick_a_subject)
                        .items(subjects)
                        .itemsCallback((dialog1, view1, which, text) -> {
                            Week w = customWeeks.get(which);
                            subject.setText(w.getSubject());
                            teacher.setText(w.getTeacher());
                            room.setText(w.getRoom());
                            select_color.setBackgroundColor(w.getColor());
                            select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));

                            String key = ((WeekdayFragment) adapter.getItem(viewPager.getCurrentItem())).getKey();
                            ArrayList<Week> weeks = dbHelper.getWeek(key);
                            int valueNew = 1;
                            if (weeks.size() > 0) {
                                valueNew = WeekUtils.getMatchingScheduleEnd(weeks.get(weeks.size() - 1).getToTime(), activity) + 1;
                            }
                            from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(valueNew, activity)));
                            week.setFromTime(WeekUtils.getMatchingTimeBegin(valueNew, activity));
                            from_hour.setText("" + valueNew);
                            to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(valueNew, activity)));
                            week.setToTime(WeekUtils.getMatchingTimeEnd(valueNew, activity));
                            to_hour.setText("" + valueNew);

                            dialog.show();
                        })
                        .positiveText(R.string.new_subject)
                        .onPositive((dialog1, which) -> {
                            String key = ((WeekdayFragment) adapter.getItem(viewPager.getCurrentItem())).getKey();
                            ArrayList<Week> weeks = dbHelper.getWeek(key);
                            int valueNew = 1;
                            if (weeks.size() > 0) {
                                valueNew = WeekUtils.getMatchingScheduleEnd(weeks.get(weeks.size() - 1).getToTime(), activity) + 1;
                            }
                            from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(valueNew, activity)));
                            week.setFromTime(WeekUtils.getMatchingTimeBegin(valueNew, activity));
                            from_hour.setText("" + valueNew);
                            to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(valueNew, activity)));
                            week.setToTime(WeekUtils.getMatchingTimeEnd(valueNew, activity));
                            to_hour.setText("" + valueNew);
                            select_color.setBackgroundColor(Color.WHITE);
                            select_color.setTextColor(Color.BLACK);

                            dialog.show();
                        })
                        .show();
            } else {
                String key = ((WeekdayFragment) adapter.getItem(viewPager.getCurrentItem())).getKey();
                ArrayList<Week> weeks = dbHelper.getWeek(key);
                int valueNew = 1;
                if (weeks.size() > 0) {
                    valueNew = WeekUtils.getMatchingScheduleEnd(weeks.get(weeks.size() - 1).getToTime(), activity) + 1;
                }
                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(valueNew, activity)));
                week.setFromTime(WeekUtils.getMatchingTimeBegin(valueNew, activity));
                from_hour.setText("" + valueNew);
                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(valueNew, activity)));
                week.setToTime(WeekUtils.getMatchingTimeEnd(valueNew, activity));
                to_hour.setText("" + valueNew);

                dialog.show();
            }
        });

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            from_time.setText(R.string.select_start_time);
            to_time.setText(R.string.select_end_time);
            from_hour.setText(R.string.select_start_time);
            to_hour.setText(R.string.select_end_time);
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        submit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) /*|| TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            } else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                week.setSubject(subject.getText().toString());
                week.setFragment(((WeekdayFragment) adapter.getItem(viewPager.getCurrentItem())).getKey());
                week.setTeacher(teacher.getText().toString());
                week.setRoom(room.getText().toString());
                week.setColor(buttonColor.getColor());
                dbHelper.insertWeek(week);
                adapter.notifyDataSetChanged();
                databaseChanged(activity);
                cancel.performClick();
            }
        });
    }

    public static void getEditTeacherDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final ArrayList<Teacher> adapter, @NonNull final ListView listView, int listposition) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText name = alertLayout.findViewById(R.id.name_dialog);
        editTextHashs.put(R.string.name, name);
        final EditText post = alertLayout.findViewById(R.id.post_dialog);
//        editTextHashs.put(R.string.post, post);
        final EditText phone_number = alertLayout.findViewById(R.id.phonenumber_dialog);
//        editTextHashs.put(R.string.phone_number, phone_number);
        final EditText email = alertLayout.findViewById(R.id.email_dialog);
        editTextHashs.put(R.string.email, email);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Teacher teacher = adapter.get(listposition);

        name.setText(teacher.getName());
        post.setText(teacher.getPost());
        phone_number.setText(teacher.getPhonenumber());
        email.setText(teacher.getEmail());
        select_color.setBackgroundColor(teacher.getColor() != 0 ? teacher.getColor() : Color.WHITE);

        select_color.setOnClickListener((View v) -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_teacher);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> dialog.dismiss());

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(name.getText()) /*|| TextUtils.isEmpty(post.getText()) || TextUtils.isEmpty(phone_number.getText())*/ || TextUtils.isEmpty(email.getText())) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else {
                TeachersAdapter teachersAdapter = (TeachersAdapter) listView.getAdapter();
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                teacher.setName(name.getText().toString());
                teacher.setPost(post.getText().toString());
                teacher.setPhonenumber(phone_number.getText().toString());
                teacher.setEmail(email.getText().toString());
                teacher.setColor(buttonColor.getColor());
                dbHelper.updateTeacher(teacher);
                teachersAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

    public static void getAddTeacherDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final TeachersAdapter adapter) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText name = alertLayout.findViewById(R.id.name_dialog);
        editTextHashs.put(R.string.name, name);
        final EditText post = alertLayout.findViewById(R.id.post_dialog);
//        editTextHashs.put(R.string.post, post);
        final EditText phone_number = alertLayout.findViewById(R.id.phonenumber_dialog);
//        editTextHashs.put(R.string.phone_number, phone_number);
        final EditText email = alertLayout.findViewById(R.id.email_dialog);
        editTextHashs.put(R.string.email, email);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(((ColorDrawable) select_color.getBackground()).getColor(), Color.WHITE, Color.BLACK));
        final Teacher teacher = new Teacher();

        select_color.setOnClickListener((View v) -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_teacher));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialog.show());

        cancel.setOnClickListener(v -> dialog.dismiss());

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(name.getText()) /*|| TextUtils.isEmpty(post.getText()) || TextUtils.isEmpty(phone_number.getText())*/ || TextUtils.isEmpty(email.getText())) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                teacher.setName(name.getText().toString());
                teacher.setPost(post.getText().toString());
                teacher.setPhonenumber(phone_number.getText().toString());
                teacher.setEmail(email.getText().toString());
                teacher.setColor(buttonColor.getColor());
                dbHelper.insertTeacher(teacher);

                adapter.clear();
                adapter.addAll(dbHelper.getTeacher());
                adapter.notifyDataSetChanged();

                name.getText().clear();
                post.getText().clear();
                phone_number.getText().clear();
                email.getText().clear();
                select_color.setBackgroundColor(Color.WHITE);
                name.requestFocus();
                dialog.dismiss();
            }
        });
    }

    public static void getEditNoteDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final ArrayList<Note> adapter, @NonNull final ListView listView, int listposition) {

        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Note note = adapter.get(listposition);

        select_color.setBackgroundColor(note.getColor() != 0 ? note.getColor() : Color.WHITE);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(note.getColor(), Color.WHITE, Color.BLACK));

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_note);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {

            select_color.setBackgroundColor(Color.WHITE);
            dialog.dismiss();
        });


    }

    public static void getAddNoteDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout ) {


        final Button select_color = alertLayout.findViewById(R.id.select_color);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(((ColorDrawable) select_color.getBackground()).getColor(), Color.WHITE, Color.BLACK));
        final Note note = new Note();

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_note);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialog.show());

        cancel.setOnClickListener(v -> {

            select_color.setBackgroundColor(Color.WHITE);
            dialog.dismiss();
        });


    }

    public static void getEditExamDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final ArrayList<Exam> adapter, @NonNull final ListView listView, int listposition) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subjectexam_dialog);
        editTextHashs.put(R.string.subject, subject);
        final EditText teacher = alertLayout.findViewById(R.id.teacherexam_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.roomexam_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView date = alertLayout.findViewById(R.id.dateexam_dialog);
        final TextView time = alertLayout.findViewById(R.id.timeexam_dialog);
        final TextView hour = alertLayout.findViewById(R.id.hourexam_dialog);
        final Button select_color = alertLayout.findViewById(R.id.select_color);

        final Exam exam = adapter.get(listposition);

        subject.setText(exam.getSubject());
        teacher.setText(exam.getTeacher());
        room.setText(exam.getRoom());
        date.setText(WeekUtils.localizeDate(activity, exam.getDate()));
        if (exam.getTime() != null && !exam.getTime().trim().isEmpty()) {
            hour.setText("" + WeekUtils.getMatchingScheduleBegin(exam.getTime(), activity));
            time.setText(WeekUtils.localizeTime(activity, exam.getTime()));
        } else {
            hour.setText("0");
            time.setText("0:0");
        }
        select_color.setBackgroundColor(exam.getColor());
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(exam.getColor(), Color.WHITE, Color.BLACK));

        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                date.setText(WeekUtils.localizeDate(activity, new Date(cal.getTimeInMillis())));

                exam.setDate(String.format(Locale.getDefault(), "%02d-%02d-%02d", year, month + 1, dayOfMonth));
            }, mYear, mMonth, mdayofMonth);
            datePickerDialog.setTitle(R.string.choose_date);
            datePickerDialog.show();
        });

        time.setOnClickListener(v -> {
            int mHour, mMinute;
            try {
                mHour = Integer.parseInt(exam.getTime().substring(0, exam.getTime().indexOf(":")));
                mMinute = Integer.parseInt(exam.getTime().substring(exam.getTime().indexOf(":") + 1));
            } catch (Exception ignore) {
                mHour = 0;
                mMinute = 0;
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        time.setText(WeekUtils.localizeTime(activity, newTime));
                        exam.setTime(newTime);
                        hour.setText("" + WeekUtils.getMatchingScheduleBegin(newTime, activity));
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            try {
                numberPicker.setValue(Integer.parseInt(hour.getText().toString()));
            } catch (Exception e) {
                numberPicker.setValue(1);
            }
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                        exam.setTime(WeekUtils.getMatchingTimeBegin(value, activity));
                        hour.setText("" + value);
                    })
                    .show();
        });

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));


        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_exam));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText())/* || TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!date.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.date_error, Snackbar.LENGTH_LONG).show();
            } /*else if (!time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            }*/ else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                exam.setSubject(subject.getText().toString());
                exam.setTeacher(teacher.getText().toString());
                exam.setRoom(room.getText().toString());
                exam.setColor(buttonColor.getColor());

                dbHelper.updateExam(exam);

                ExamsAdapter examsAdapter = (ExamsAdapter) listView.getAdapter();
                examsAdapter.notifyDataSetChanged();

                dialog.dismiss();

                new MaterialDialog.Builder(activity)
                        .content(R.string.add_to_calendar)
                        .positiveText(R.string.yes)
                        .onPositive((MaterialDialog s, DialogAction w) -> {
                            String year = exam.getDate().substring(0, exam.getDate().indexOf("-"));
                            String month = exam.getDate().substring(year.length() + 1, exam.getDate().indexOf("-") + year.length() - 1);
                            String day = exam.getDate().substring(year.length() + month.length() + 2);

                            String hour2, minute;
                            if (exam.getTime() != null && !exam.getTime().trim().isEmpty()) {
                                hour2 = exam.getTime().substring(0, exam.getTime().indexOf(":"));
                                minute = exam.getTime().substring(hour2.length() + 1);
                            } else {
                                hour2 = "0";
                                minute = "0";
                            }

                            Calendar timeCalendar = Calendar.getInstance();
                            timeCalendar.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour2), Integer.parseInt(minute));

                            Intent intent = new Intent(Intent.ACTION_INSERT)
                                    .setData(CalendarContract.Events.CONTENT_URI)
                                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeCalendar.getTimeInMillis())
                                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeCalendar.getTimeInMillis())
                                    .putExtra(CalendarContract.Events.TITLE, exam.getSubject())
                                    .putExtra(CalendarContract.Events.DESCRIPTION, exam.getTeacher())
                                    .putExtra(CalendarContract.Events.EVENT_LOCATION, exam.getRoom());
                            try {
                                activity.startActivity(intent);
                            } catch (ActivityNotFoundException e2) {
                                ChocoBar.builder().setActivity(activity).setText(activity.getString(R.string.no_calendar_app)).setDuration(ChocoBar.LENGTH_LONG).red().show();
                            }

                        })
                        .negativeText(R.string.no)
                        .show();
            }
        });
    }

    public static void getAddExamDialog(DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final ExamsAdapter adapter) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subjectexam_dialog);
        editTextHashs.put(R.string.subject, subject);
        subject.requestFocus();
        final EditText teacher = alertLayout.findViewById(R.id.teacherexam_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.roomexam_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView date = alertLayout.findViewById(R.id.dateexam_dialog);
        final TextView time = alertLayout.findViewById(R.id.timeexam_dialog);
        final TextView hour = alertLayout.findViewById(R.id.hourexam_dialog);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(((ColorDrawable) select_color.getBackground()).getColor(), Color.WHITE, Color.BLACK));

        hour.setText(R.string.select_time);
        if (PreferenceUtil.showTimes(activity)) {
            hour.setVisibility(View.GONE);
            time.setVisibility(View.VISIBLE);
            time.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .7f));
        } else {
            hour.setVisibility(View.VISIBLE);
            time.setVisibility(View.GONE);
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .7f));
        }

        final Exam exam = new Exam();

        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                date.setText(WeekUtils.localizeDate(activity, new Date(cal.getTimeInMillis())));

                exam.setDate(String.format(Locale.getDefault(), "%02d-%02d-%02d", year, month + 1, dayOfMonth));
            }, mYear, mMonth, mdayofMonth);
            datePickerDialog.setTitle(R.string.choose_date);
            datePickerDialog.show();
        });

        time.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        time.setText(WeekUtils.localizeTime(activity, newTime));
                        exam.setTime(newTime);
                        hour.setText("" + WeekUtils.getMatchingScheduleBegin(newTime, activity));
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                        exam.setTime(WeekUtils.getMatchingTimeBegin(value, activity));
                        hour.setText("" + value);
                    })
                    .show();
        });

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));


        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_exam));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialog.show());

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) /*|| TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!date.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.date_error, Snackbar.LENGTH_LONG).show();
            } /*else if (!time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            }*/ else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                exam.setSubject(subject.getText().toString());
                exam.setTeacher(teacher.getText().toString());
                exam.setRoom(room.getText().toString());
                exam.setColor(buttonColor.getColor());

                dbHelper.insertExam(exam);

                adapter.clear();
                adapter.addAll(dbHelper.getExam());
                adapter.notifyDataSetChanged();

                subject.getText().clear();
                teacher.getText().clear();
                room.getText().clear();
                date.setText(R.string.choose_date);
                time.setText(R.string.select_time);
                hour.setText(R.string.select_time);
                select_color.setBackgroundColor(Color.WHITE);
                subject.requestFocus();
                dialog.dismiss();

                new MaterialDialog.Builder(activity)
                        .content(R.string.add_to_calendar)
                        .positiveText(R.string.yes)
                        .onPositive((MaterialDialog s, DialogAction w) -> {
                            String year = exam.getDate().substring(0, exam.getDate().indexOf("-"));
                            String month = exam.getDate().substring(year.length() + 1, exam.getDate().indexOf("-") + year.length() - 1);
                            String day = exam.getDate().substring(year.length() + month.length() + 2);

                            String hour2, minute;
                            if (exam.getTime() != null && !exam.getTime().trim().isEmpty()) {
                                hour2 = exam.getTime().substring(0, exam.getTime().indexOf(":"));
                                minute = exam.getTime().substring(hour2.length() + 1);
                            } else {
                                hour2 = "0";
                                minute = "0";
                            }

                            Calendar timeCalendar = Calendar.getInstance();
                            timeCalendar.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour2), Integer.parseInt(minute));

                            Intent intent = new Intent(Intent.ACTION_INSERT)
                                    .setData(CalendarContract.Events.CONTENT_URI)
                                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeCalendar.getTimeInMillis())
                                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeCalendar.getTimeInMillis())
                                    .putExtra(CalendarContract.Events.TITLE, exam.getSubject())
                                    .putExtra(CalendarContract.Events.DESCRIPTION, exam.getTeacher())
                                    .putExtra(CalendarContract.Events.EVENT_LOCATION, exam.getRoom());
                            try {
                                activity.startActivity(intent);
                            } catch (ActivityNotFoundException e2) {
                                ChocoBar.builder().setActivity(activity).setText(activity.getString(R.string.no_calendar_app)).setDuration(ChocoBar.LENGTH_LONG).red().show();
                            }

                        })
                        .negativeText(R.string.no)
                        .show();
            }
        });
    }

    public static void getDeleteDialog(@NonNull Context context, @NonNull Runnable runnable, String deleteSubject) {
        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.are_you_sure))
                .content(context.getString(R.string.delete_content, deleteSubject))
                .positiveText(context.getString(R.string.yes))
                .onPositive((dialog, which) -> {
                    runnable.run();
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .negativeText(context.getString(R.string.no))
                .show();
    }

    private static void databaseChanged(Context context) {
        NotificationUtil.sendNotificationCurrentLesson(context, false);
        DoNotDisturbReceiversKt.setDoNotDisturbReceivers(context, false);
    }
}
