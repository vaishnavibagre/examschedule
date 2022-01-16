package com.ulan.timetable.adapters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;

import com.ulan.timetable.R;
import com.ulan.timetable.activities.TeachersActivity;
import com.ulan.timetable.model.Exam;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.ColorPalette;
import com.ulan.timetable.utils.DbHelper;
import com.ulan.timetable.utils.PreferenceUtil;
import com.ulan.timetable.utils.WeekUtils;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Ulan on 17.12.2018.
 */
public class ExamsAdapter extends ArrayAdapter<Exam> {

    @NonNull
    private final AppCompatActivity mActivity;
    private final DbHelper dbHelper;
    @NonNull
    private final ArrayList<Exam> examlist;
    private Exam exam;
    private final ListView mListView;

    private static class ViewHolder {
        TextView subject;
        TextView teacher;
        TextView room;
        TextView date;
        TextView time;
        CardView cardView;
        ImageView popup;
    }

    public ExamsAdapter(DbHelper dbHelper, @NonNull AppCompatActivity activity, ListView listView, int resource, @NonNull ArrayList<Exam> objects) {
        super(activity, resource, objects);
        this.dbHelper = dbHelper;
        mActivity = activity;
        mListView = listView;
        examlist = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String subject = Objects.requireNonNull(getItem(position)).getSubject();
        String teacher = Objects.requireNonNull(getItem(position)).getTeacher();
        String room = Objects.requireNonNull(getItem(position)).getRoom();
        String date = Objects.requireNonNull(getItem(position)).getDate();
        String time = Objects.requireNonNull(getItem(position)).getTime();
        int color = Objects.requireNonNull(getItem(position)).getColor();

        exam = new Exam(subject, teacher, time, date, room, color);
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.listview_exams_adapter, parent, false);
            holder = new ViewHolder();
            holder.subject = convertView.findViewById(R.id.subjectexams);
            holder.teacher = convertView.findViewById(R.id.teacherexams);
            holder.room = convertView.findViewById(R.id.roomexams);
            holder.date = convertView.findViewById(R.id.dateexams);
            holder.time = convertView.findViewById(R.id.timeexams);
            holder.cardView = convertView.findViewById(R.id.exams_cardview);
            holder.popup = convertView.findViewById(R.id.popupbtn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Setup colors based on Background
        int textColor = ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK);
        holder.subject.setTextColor(textColor);
        holder.teacher.setTextColor(textColor);
        holder.room.setTextColor(textColor);
        holder.date.setTextColor(textColor);
        holder.time.setTextColor(textColor);
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.roomimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.teacherimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.teacherimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.timeimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.popupbtn), ColorStateList.valueOf(textColor));
        convertView.findViewById(R.id.line).setBackgroundColor(textColor);


        holder.subject.setText(exam.getSubject());
        holder.teacher.setText(exam.getTeacher());
        holder.teacher.setOnClickListener((View v) -> mActivity.startActivity(new Intent(mActivity, TeachersActivity.class)));
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.teacher.setBackgroundResource(outValue.resourceId);

        holder.room.setText(exam.getRoom());

        if (PreferenceUtil.showTimes(getContext()))
            holder.time.setText(WeekUtils.localizeTime(getContext(), exam.getTime()));
        else if (exam.getTime() != null && !exam.getTime().trim().isEmpty())
            holder.time.setText(WeekUtils.localizeTime(getContext(), "" + WeekUtils.getMatchingScheduleBegin(exam.getTime(), getContext())));
        else
            holder.time.setText("");

        holder.date.setText(WeekUtils.localizeDate(getContext(), exam.getDate()));

        holder.cardView.setCardBackgroundColor(exam.getColor());
        holder.popup.setOnClickListener(v -> {
            ContextThemeWrapper theme = new ContextThemeWrapper(mActivity, PreferenceUtil.isDark(getContext()) ? R.style.Widget_AppCompat_PopupMenu : R.style.Widget_AppCompat_Light_PopupMenu);
            final PopupMenu popup = new PopupMenu(theme, holder.popup);
            popup.inflate(R.menu.popup_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.delete_popup) {
                        AlertDialogsHelper.getDeleteDialog(getContext(), () -> {
                            dbHelper.deleteExamById(Objects.requireNonNull(getItem(position)));
                            dbHelper.updateExam(Objects.requireNonNull(getItem(position)));
                            examlist.remove(position);
                            notifyDataSetChanged();
                        }, getContext().getString(R.string.delete_exam, exam.getSubject()));
                        return true;
                    } else if (itemId == R.id.edit_popup) {
                        final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.dialog_add_exam, null);
                        AlertDialogsHelper.getEditExamDialog(dbHelper, mActivity, alertLayout, examlist, mListView, position);
                        notifyDataSetChanged();
                        return true;
                    }
                    return onMenuItemClick(item);
                }
            });
            popup.show();
        });

        hidePopUpMenu(holder);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    public ArrayList<Exam> getExamList() {
        return examlist;
    }

    public Exam getExam() {
        return exam;
    }

    private void hidePopUpMenu(@NonNull ViewHolder holder) {
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();
        if (checkedItems.size() > 0) {
            for (int i = 0; i < checkedItems.size(); i++) {
                int key = checkedItems.keyAt(i);
                if (checkedItems.get(key)) {
                    holder.popup.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            holder.popup.setVisibility(View.VISIBLE);
        }
    }
}
