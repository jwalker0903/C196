package edu.wgu.jwalker.c196;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseDetailActivity extends AppCompatActivity {

    DatabaseHelper db;
    Calendar myCalendar = Calendar.getInstance();
    EditText courseName, courseDescription, courseMentorName, courseMentorPhone, courseMentorMail;
    TextView courseStartDate, courseEndDate;
    TableLayout assessmentsTable_CD, courseNotesTable;
    int dateFieldToggle;
    Spinner termSpinner, statusSpinner;
    ScrollView courseDetailView;
    String courseID = null;
    Button newNoteButton_CD;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Boolean startDateChanged = false;
    Boolean endDateChanged = false;

    //to generate DatePicker in date fields
    DatePickerDialog.OnDateSetListener datePick = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Course Detail");

        db = new DatabaseHelper(this);

        //casting
        courseDetailView = (ScrollView) findViewById(R.id.courseDetailView);
        courseName = (EditText) findViewById(R.id.courseName_CD);
        termSpinner = (Spinner) findViewById(R.id.termSpinner_CD);
        courseDescription = (EditText) findViewById(R.id.courseDescription_CD);
        courseStartDate = (TextView) findViewById(R.id.editStart_CD);
        courseEndDate = (TextView) findViewById(R.id.editEnd_CD);
        statusSpinner = (Spinner) findViewById(R.id.statusSpinner_CD);
        courseMentorName = (EditText) findViewById(R.id.editMentorName_CD);
        courseMentorPhone = (EditText) findViewById(R.id.editMentorPhone_CD);
        courseMentorMail = (EditText) findViewById(R.id.editMentorEmail_CD);
        courseNotesTable = (TableLayout) findViewById(R.id.notesTable_CD);
        assessmentsTable_CD = (TableLayout) findViewById(R.id.assessmentsTable_CD);
        newNoteButton_CD = (Button) findViewById(R.id.newNoteButton_CD);

        //fill in spinners
        //status spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.course_status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        //terms spinner
        List<String> termSpinnerList = db.getTermNames();
        ArrayAdapter<String> termAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, termSpinnerList);
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        termSpinner.setAdapter(termAdapter);

        //get data from previous activity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        courseID = sharedPreferences.getString("course", "");

        //fill in fields
        fillInCourse(courseID);

        //create listeners on date fields
        courseStartDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { startDateChanged = true; }
        });
        courseEndDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { endDateChanged = true; }
        });

        //onClick for new Note button
        newNoteButton_CD.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                newNote_CD();
            }
        });

    }

    public void fillInCourse(String courseID){
        Cursor res = db.getCourse(courseID);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        //get course data and set text fields
        res.moveToFirst();
        courseName.setText(res.getString(1));
        courseDescription.setText(res.getString(3));
        courseMentorName.setText(res.getString(6));
        courseMentorPhone.setText(res.getString(7));
        courseMentorMail.setText(res.getString(8));

        //set spinner starting values
        //term spinner
        String termSet = db.getTermName(res.getString(2));
        ArrayAdapter termAdap = (ArrayAdapter) termSpinner.getAdapter();
        int termSpinnerPosition = termAdap.getPosition(termSet);
        termSpinner.setSelection(termSpinnerPosition);

        //status spinner
        String statusSet = res.getString(9);
        ArrayAdapter statusAdap = (ArrayAdapter) statusSpinner.getAdapter();
        int statusSpinnerPosition = statusAdap.getPosition(statusSet);
        statusSpinner.setSelection(statusSpinnerPosition);

        //fill in dates
        long startNum = res.getLong(4);
        long endNum = res.getLong(5);
        String start = sdf.format(startNum);
        String end = sdf.format(endNum);
        courseStartDate.setText(start);
        courseEndDate.setText(end);

        //fill in assessments
        fillInAssessments(courseID);

        //fill in notes
        fillInNotes(courseID);

    }

    //onclick method for delete button
    public void deleteButton_CD(View v){
        db.deleteCourse(courseID);

        //switch to Courses Activity
        Intent intent = new Intent(v.getContext(), CoursesActivity.class);
        v.getContext().startActivity(intent);
    }

    //onClick method for Save button
    public void saveButton_CD(View v){
        boolean dataInvalid = false;
        String name = courseName.getText().toString();
        String description = courseDescription.getText().toString();
        String start = courseStartDate.getText().toString();
        String end = courseEndDate.getText().toString();
        long numStart = 1;
        long numEnd = 1;
        String status = statusSpinner.getSelectedItem().toString();
        String term = termSpinner.getSelectedItem().toString();
        String menName = courseMentorName.getText().toString();
        String menPhone = courseMentorPhone.getText().toString();
        String menMail = courseMentorMail.getText().toString();


        //DATA VALIDATION METHODS
        //check for blank course name
        if (name.isEmpty()){
            Toast.makeText(CourseDetailActivity.this, "Enter name.", Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        //verify name is unique
        if (!dataInvalid){
            List<String> courseNames = db.getCourseNames();
            int temp = courseNames.contains(name) ? 1 : 2;
            if (temp == 1){
                int idFromDatabase = db.getCourseID(name);
                if (Integer.parseInt(courseID) != idFromDatabase) {
                    dataInvalid = true;
                    Toast.makeText(CourseDetailActivity.this, "Course name already exists.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //convert dates to long
        if (!dataInvalid) {
            //check that dates are not empty
            if (start.isEmpty() || end.isEmpty()) {
                dataInvalid = true;
                Toast.makeText(CourseDetailActivity.this, "Dates cannot be blank.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = sdf.parse(start);
                    numStart = date.getTime();
                    date = sdf.parse(end);
                    numEnd = date.getTime();
                } catch (ParseException e) {
                    dataInvalid = true;
                    Toast.makeText(CourseDetailActivity.this, "There was a problem with your dates.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //ensure that the end date is before start date
        if (!dataInvalid){
            boolean datesvalid = (numEnd > numStart);
            if (!datesvalid){
                Toast.makeText(CourseDetailActivity.this, "End date must be after start date.", Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            }
        }

        //ensure that the dates are within the selected term
        if (!dataInvalid){
            List<Long> termDates = db.getTermDates(term);
            boolean datesvalid = (numStart >= termDates.get(0));

            if (!datesvalid){
                Toast.makeText(CourseDetailActivity.this, "Start date outside of term.", Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            } else {
                datesvalid = (numEnd <= termDates.get(1));
                if (!datesvalid){
                    Toast.makeText(CourseDetailActivity.this, "End date outside of term.", Toast.LENGTH_SHORT).show();
                    dataInvalid = true;
                }
            }
        }


        //ensure dates don't overlap with other courses
        //only runs if dates have changed
        if (!dataInvalid){
            if (!startDateChanged || !endDateChanged) {
                boolean overlap = db.dateCompares("courses", numStart, numEnd);
                if (overlap) {
                    Toast.makeText(CourseDetailActivity.this, "Course dates overlap with another course.", Toast.LENGTH_SHORT).show();
                    dataInvalid = true;
                }
            }
        }

        //enter data into database
        if (!dataInvalid){
            //get term ID for course
            int termID = db.getTermId(term);

            boolean isUpdated = db.updateCourse(courseID, name, termID, description, numStart, numEnd,
                    status, menName, menPhone, menMail);

            if (isUpdated){
                Toast.makeText(CourseDetailActivity.this, "Course updated.", Toast.LENGTH_SHORT).show();

                //switch views
                finish();
            } else {
                Toast.makeText(CourseDetailActivity.this, "Course not updated.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void newNote_CD(){

        //create note dialog
        AlertDialog.Builder noteBuilder = new AlertDialog.Builder(this);
        noteBuilder.setTitle("Note");

        //set up the input
        final EditText noteInput = new EditText(this);
        noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteInput.setSingleLine(false);
        noteInput.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        noteInput.setVerticalScrollBarEnabled(true);
        noteInput.setMovementMethod(ScrollingMovementMethod.getInstance());
        noteBuilder.setView(noteInput);

        //create dialog buttons
        noteBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteText = noteInput.getText().toString();
                boolean isInserted = db.insertCourseNote(courseID, noteText);
                if (isInserted) {
                    Toast.makeText(CourseDetailActivity.this, "Note added.", Toast.LENGTH_SHORT).show();
                    fillInCourse(courseID);
                } else {
                    Toast.makeText(CourseDetailActivity.this, "Note not added.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        noteBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        noteBuilder.show();

    }

    public void fillInNotes(final String courseID){
        //clear table
        courseNotesTable.removeAllViews();


        //get notes
        Cursor res = db.getCourseNotes(courseID);
        ArrayList<List> notesList = new ArrayList<>();

        while(res.moveToNext()){
            String id = res.getString(0);
            String timestamp = res.getString(3);
            String note = res.getString(2);

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(timestamp);
            data.add(note);

            notesList.add(data);
        }

        for (List<String> list : notesList){
            final TableRow row = new TableRow(CourseDetailActivity.this);
            final TableRow row2 = new TableRow(CourseDetailActivity.this);
            TextView timestampCol = new TextView(CourseDetailActivity.this);
            TextView noteCol = new TextView(CourseDetailActivity.this);

            timestampCol.setText(list.get(1));
            timestampCol.setPadding(0, 0, 18, 0);
            noteCol.setMaxEms(12);
            noteCol.setSingleLine();
            noteCol.setEllipsize(TextUtils.TruncateAt.END);
            noteCol.setText(list.get(2));
            final String noteText = list.get(2);
            final String noteID = list.get(0);

            //create dialog box to view complete note on click
            noteCol.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //create note dialog
                    AlertDialog.Builder noteBuilder = new AlertDialog.Builder(CourseDetailActivity.this);
                    noteBuilder.setTitle("Note");

                    //set up the input
                    final TextView noteInput = new TextView(CourseDetailActivity.this);
                    noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    noteInput.setSingleLine(false);
                    noteInput.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    noteInput.setVerticalScrollBarEnabled(true);
                    noteInput.setMovementMethod(ScrollingMovementMethod.getInstance());
                    noteInput.setText(noteText);
                    noteBuilder.setView(noteInput);

                    //create dialog buttons
                    //to share note
                    noteBuilder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, noteText);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);
                            dialog.dismiss();
                        }
                    });

                    //to dismiss note
                    noteBuilder.setNeutralButton("Return", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    //to delete note
                    noteBuilder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.deleteCourseNote(noteID);
                            fillInNotes(courseID);
                        }
                    });

                    noteBuilder.show();
                }
            });

            row.addView(timestampCol);
            row.addView(noteCol);
            courseNotesTable.addView(row);
            courseNotesTable.addView(row2);
        }

        res.close();
    }

    public void fillInAssessments(String courseID){
        //clear table
        assessmentsTable_CD.removeAllViews();

        //get assessments
        Cursor res = db.getAssessmentsForOneCourse(courseID);
        ArrayList<List> assessmentList = new ArrayList<>();

        while(res.moveToNext()){
            String name = res.getString(3);
            String type = res.getString(5);
            String dueDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(res.getLong(6)));

            List<String> data = new ArrayList();
            data.add(name);
            data.add(type);
            data.add(dueDate);
            assessmentList.add(data);
        }

        for (List<String> list : assessmentList){
            TableRow row = new TableRow(CourseDetailActivity.this);
            TextView nameCol = new TextView(CourseDetailActivity.this);
            TextView typeCol = new TextView(CourseDetailActivity.this);
            TextView dateCol = new TextView(CourseDetailActivity.this);

            Button assessmentDetails_CD = new Button(CourseDetailActivity.this);
            assessmentDetails_CD.setText("DETAILS");
            assessmentDetails_CD.setPadding(1, 0, 0, 0);

            nameCol.setText(list.get(0));
            final String assessName = nameCol.getText().toString();
            typeCol.setText(" " + list.get(1));
            dateCol.setText(" " + list.get(2));

            assessmentDetails_CD.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //put assessment name in Shared Preferences to send to next activity
                    editor.putString("assessment", assessName);
                    editor.commit();

                    //switch to assessment details activity
                    Intent intent = new Intent(v.getContext(), AssessmentDetailActivity.class);
                    v.getContext().startActivity(intent);
                }
            });
            row.addView(nameCol);
            row.addView(typeCol);
            row.addView(dateCol);
            row.addView(assessmentDetails_CD);
            assessmentsTable_CD.addView(row);
        }
    }

    //onClick methods for date views
    public void startDateSelect_CD(View v){
            DatePickerDialog dpd = new DatePickerDialog(CourseDetailActivity.this, datePick, myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

            List<Long> termDates = db.getTermDates(termSpinner.getSelectedItem().toString());
            dateFieldToggle = 1;

            dpd.getDatePicker().setMinDate(termDates.get(0));
            dpd.getDatePicker().setMaxDate(termDates.get(1));
            dpd.show();
    }
    public void endDateSelect_CD(View v){
        DatePickerDialog dpd = new DatePickerDialog(CourseDetailActivity.this, datePick, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        List<Long> termDates = db.getTermDates(termSpinner.getSelectedItem().toString());
        dateFieldToggle = 2;

        dpd.getDatePicker().setMinDate(termDates.get(0));
        dpd.getDatePicker().setMaxDate(termDates.get(1));
        dpd.show();
    }

    //onClick method for add Assessment button
    public void addAssessment_CD(View v){
        editor.putString("courseName", courseName.getText().toString());
        editor.commit();
        Intent intent = new Intent(v.getContext(), AssessmentsActivity.class);
        intent.putExtra("activity", ".CourseDetailActivity");
        v.getContext().startActivity(intent);
    }

    //onClick method for addAlert button
    public void addAlert_CD(View v){
        //set course alerts
        String start = courseStartDate.getText().toString();
        String end = courseEndDate.getText().toString();

        //convert dates from textview to long
        if (!start.isEmpty() && !end.isEmpty()){
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            try {
                Date startDate = sdf.parse(start);
                Date endDate = sdf.parse(end);
                long startNum = startDate.getTime();
                long endNum = endDate.getTime();

                Intent alertIntent = new Intent(this, AlarmReceiver.class);

                AlarmManager alarmManagerStart = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                AlarmManager alarmManagerEnd = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                alarmManagerStart.set(AlarmManager.RTC_WAKEUP, startNum, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
                alarmManagerEnd.set(AlarmManager.RTC_WAKEUP, endNum, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

                Toast.makeText(CourseDetailActivity.this, "Alert set.", Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {

            }
        } else {
            Toast.makeText(CourseDetailActivity.this, "Dates cannot be blank.", Toast.LENGTH_SHORT).show();
        }
    }

    //method to update TextViews for start and end dates
    private void updateLabel(){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        String newDate = sdf.format(myCalendar.getTime());

        if(dateFieldToggle == 1) {
            courseStartDate.setText(newDate);
        }
        else
            courseEndDate.setText(newDate);
    }
}
