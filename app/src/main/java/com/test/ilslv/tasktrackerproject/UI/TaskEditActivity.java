package com.test.ilslv.tasktrackerproject.UI;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.test.ilslv.tasktrackerproject.Domain.Task;
import com.test.ilslv.tasktrackerproject.Domain.TaskEditContract;
import com.test.ilslv.tasktrackerproject.Domain.TaskStatus;
import com.test.ilslv.tasktrackerproject.R;
import com.test.ilslv.tasktrackerproject.TasksTrackerApp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskEditActivity extends AppCompatActivity implements TaskEditContract.TaskEditView,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.titleControl)
    EditText titleControl;
    @BindView(R.id.dateControl)
    Button dateControl;
    @BindView(R.id.statusControl)
    Spinner statusControl;
    @BindView(R.id.desriptionControl)
    EditText descriptionControl;
    @BindView(R.id.addOrEditTask)
    Button editTask;

    @Inject
    TaskEditContract.TaskEditPresenter taskEditPresenter;

    UUID taskId;

    Date taskDate;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    private int taskYear;
    private int taskMonth;
    private int taskDay;
    LinkedHashMap<String, TaskStatus> statusMap;
    String[] spinnerData = new String[3];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_task);
        ButterKnife.bind(this);
        TasksTrackerApp.getTasksTrackerComponent().inject(this);

        statusMap = new LinkedHashMap<>();
        statusMap.put(TaskStatus.NEW.getName(), TaskStatus.NEW);
        statusMap.put(TaskStatus.INPROGRESS.getName(), TaskStatus.INPROGRESS);
        statusMap.put(TaskStatus.DONE.getName(), TaskStatus.DONE);
        spinnerData = statusMap.keySet().toArray(spinnerData);

        taskEditPresenter.onCreate(this);
        editTask.setText("Изменить");
        Intent intent = getIntent();
        taskId = UUID.fromString(intent.getStringExtra("taskId"));

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        datePickerDialog = new DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        timePickerDialog = new TimePickerDialog(
                this,
                this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        dateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.create();
                datePickerDialog.show();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusControl.setAdapter(adapter);

        if (savedInstanceState == null)
            taskEditPresenter.getNoEditTaskData(taskId);
        else {
            titleControl.setText(savedInstanceState.getString("taskTitle"));
            descriptionControl.setText(savedInstanceState.getString("taskDescription"));
            statusControl.setSelection(savedInstanceState.getInt("taskStatus"));

            Locale loc = new Locale("ru");
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", loc);
            format.setTimeZone(TimeZone.getDefault());
            dateControl.setText(format.format(new Date(savedInstanceState.getLong("taskDate"))).toString());
        }

        editTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String position = (String) statusControl.getSelectedItem();
                taskEditPresenter.editTask(
                        taskId,
                        titleControl.getText().toString(),
                        taskDate,
                        statusMap.get(position),
                        descriptionControl.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskEditPresenter.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("taskTitle", titleControl.getText().toString());
        outState.putLong("taskDate", taskDate.getTime());
        outState.putInt("taskStatus", statusControl.getSelectedItemPosition());
        outState.putString("taskDescription", descriptionControl.getText().toString());
    }

    @Override
    public void showTaskData(Task task) {
        titleControl.setText(task.getTaskTitle());

        List<TaskStatus> keys = new ArrayList<>(statusMap.values());
        int position = keys.indexOf(task.getTaskStatus());
        statusControl.setSelection(position);

        Locale loc = new Locale("ru");
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", loc);
        format.setTimeZone(TimeZone.getDefault());
        dateControl.setText(format.format(task.getTaskDate()).toString());

        descriptionControl.setText(task.getTaskDescription());
    }

    @Override
    public void showResult() {
        this.finish();
    }

    @Override
    public void showError() {
        Toast.makeText(getApplicationContext(), "Заполните данные о задаче", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        taskYear = year - 1900;
        taskMonth = month;
        taskDay = day;
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minuets) {
        taskDate = new Date(taskYear, taskMonth, taskDay, hour, minuets);
        Locale loc = new Locale("ru");
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", loc);
        format.setTimeZone(TimeZone.getDefault());
        dateControl.setText(format.format(taskDate).toString());
    }
}
