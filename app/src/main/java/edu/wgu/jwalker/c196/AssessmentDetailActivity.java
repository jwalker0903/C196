package edu.wgu.jwalker.c196;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssessmentDetailActivity extends AppCompatActivity {

    //variables
    DatabaseHelper db;
    Calendar myCalendar = Calendar.getInstance();
    ScrollView assessmentEntryView_ADD;
    Button saveButton_ADD, cancelButton_ADD;
    EditText assessmentHeader_ADD, assessmentCode_ADD, assessmentDescription_ADD;
    TextView dueDate_ADD;
    Spinner courseSpinner_ADD, statusSpinner_ADD, typeSpinner_ADD;
    public static final String myPREFERENCES = "MyPrefs";
    String assessmentID = null;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //    to generate DatePicker in date fields
    DatePickerDialog.OnDateSetListener datePick = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(myCalendar);
            myCalendar = Calendar.getInstance();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);
        db = new DatabaseHelper(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Assessment");

        assessmentEntryView_ADD = (ScrollView) findViewById(R.id.assessmentEntryView_ADD);
        saveButton_ADD = (Button) findViewById(R.id.saveButton_ADD);
        cancelButton_ADD = (Button) findViewById(R.id.cancelButton_ADD);
        assessmentHeader_ADD = (EditText) findViewById(R.id.assessmentHeader_ADD);
        assessmentCode_ADD = (EditText) findViewById(R.id.assessmentCode_ADD);
        assessmentDescription_ADD = (EditText) findViewById(R.id.assessmentDescription_ADD);
        dueDate_ADD = (TextView) findViewById (R.id.dueDate_ADD);
        typeSpinner_ADD = (Spinner) findViewById(R.id.typeSpinner_ADD);
        statusSpinner_ADD = (Spinner) findViewById(R.id.statusSpinner_ADD);
        courseSpinner_ADD = (Spinner) findViewById(R.id.courseSpinner_ADD);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        assessmentID = sharedPreferences.getString("assessment", "");

        //fill in assessment detail
        populateAssessmentFields(assessmentID);

    }

    //onClick for date view
    public void dateSelect_ADD(View v){
        DatePickerDialog dpd = new DatePickerDialog(AssessmentDetailActivity.this, datePick, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        List<Long> courseDates = db.getCourseDates(courseSpinner_ADD.getSelectedItem().toString());

        dpd.getDatePicker().setMinDate(courseDates.get(0));
        dpd.getDatePicker().setMaxDate(courseDates.get(1));
        dpd.show();
    }

    //method to fill in fields
    public void populateAssessmentFields(String assessmentID){
        //fill in spinners
        //course spinner
        List<String> courseSpinnerList_ADD = db.getCourseNames();
        ArrayAdapter<String> courseAdapter_ADD = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, courseSpinnerList_ADD);
        courseAdapter_ADD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner_ADD.setAdapter(courseAdapter_ADD);

        //type spinner
        ArrayAdapter<CharSequence> typeAdapter_ADD = ArrayAdapter.createFromResource(this, R.array.assessment_type, android.R.layout.simple_spinner_item);
        typeAdapter_ADD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner_ADD.setAdapter(typeAdapter_ADD);

        //status spinner
        ArrayAdapter<CharSequence> statusAdapter_ADD = ArrayAdapter.createFromResource(this, R.array.assessment_status, android.R.layout.simple_spinner_item);
        statusAdapter_ADD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner_ADD.setAdapter(statusAdapter_ADD);

        //get assessment data
        Cursor res = db.getAssessmentData(assessmentID);
        res.moveToFirst();

        //fill in blanks
        assessmentHeader_ADD.setText(res.getString(3));
        assessmentCode_ADD.setText(res.getString(2));
        assessmentDescription_ADD.setText(res.getString(4));

        //set spinner starting values
        //course spinner
        int courseSpinnerPosition = courseAdapter_ADD.getPosition(db.getCourseName(res.getString(1)));
        courseSpinner_ADD.setSelection(courseSpinnerPosition);
        //type spinner
        int typeSpinnerPosition = typeAdapter_ADD.getPosition(res.getString(5));
        typeSpinner_ADD.setSelection(typeSpinnerPosition);
        //status spinner
        int statusSpinnerPosition = statusAdapter_ADD.getPosition(res.getString(7));
        statusSpinner_ADD.setSelection(statusSpinnerPosition);

        //convert date to long and fill in date field
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        dueDate_ADD.setText(sdf.format(res.getLong(6)));

    }

    //onClick for save button
    public void saveAssessment_ADD(View v){

        boolean dataInvalid = false;
        String name = assessmentHeader_ADD.getText().toString();
        String code = assessmentCode_ADD.getText().toString();
        String description = assessmentDescription_ADD.getText().toString();
        String course = courseSpinner_ADD.getSelectedItem().toString();
        String type = typeSpinner_ADD.getSelectedItem().toString();
        String status = statusSpinner_ADD.getSelectedItem().toString();
        String dueDateString = dueDate_ADD.getText().toString();
        long dateNum = 1;

        //data validation methods
        //check for blank name
        if (name.isEmpty()){
            Toast.makeText(AssessmentDetailActivity.this, "Enter name.", Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        //check for blank code
        if (!dataInvalid){
            if (code.isEmpty()) {
                dataInvalid = true;
                Toast.makeText(AssessmentDetailActivity.this, "Enter code.", Toast.LENGTH_SHORT).show();
            }
        }

        //check for blank date and convert to long
        if (!dataInvalid){
            if (dueDateString.isEmpty()){
                dataInvalid = true;
                Toast.makeText(AssessmentDetailActivity.this, "Select date.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = sdf.parse(dueDateString);
                    dateNum = date.getTime();
                } catch (ParseException e){
                    dataInvalid = true;
                    Toast.makeText(AssessmentDetailActivity.this, "There was a problem with your dates.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //check that due date is within course dates
        if (!dataInvalid){
            List<Long> courseDates = db.getCourseDates(course);
            boolean datesvalid = (dateNum >= courseDates.get(0));

            if(!datesvalid){
                dataInvalid = true;
                Toast.makeText(AssessmentDetailActivity.this, "Due date outside of course dates.", Toast.LENGTH_SHORT).show();
            } else {
                datesvalid = (dateNum <= courseDates.get(1));
                if (!datesvalid){
                    dataInvalid = true;
                    Toast.makeText(AssessmentDetailActivity.this, "Due date outside of course dates.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //enter data into database
        if (!dataInvalid){
            //get courseID
            int course_id = db.getCourseID(course);

            //insert data
            boolean isUpdated = db.updateAssessment(assessmentID, name, code, description, course_id, type, status, dateNum);

            if (isUpdated){
                Toast.makeText(AssessmentDetailActivity.this, "Assessment updated.", Toast.LENGTH_SHORT).show();

                //switch views
                finish();

            } else {
                Toast.makeText(AssessmentDetailActivity.this, "Assessment not updated.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //onClick for Cancel button
    public void cancelAssessmentAdd_ADD(View v){
        finish();
    }

    //DatePicker update method
    private void updateLabel(Calendar chosenCalendar){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String newDate = sdf.format(chosenCalendar.getTime());
        dueDate_ADD.setText(newDate);
    }

    //onClick to add alert for date
    public void addAlert_ADD(View view) {
        //set course alerts
        String dueDate = dueDate_ADD.getText().toString();

        //convert dates from textview to long
        if (!dueDate.isEmpty()){
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            try {
                Date date = sdf.parse(dueDate);
                long dateNum = date.getTime();

                Intent alertIntent = new Intent(this, AlarmReceiver.class);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                alarmManager.set(AlarmManager.RTC_WAKEUP, dateNum, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

                Toast.makeText(AssessmentDetailActivity.this, "Alert set.", Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {

            }
        } else {
            Toast.makeText(AssessmentDetailActivity.this, "Dates cannot be blank.", Toast.LENGTH_SHORT).show();
        }
    }
}
