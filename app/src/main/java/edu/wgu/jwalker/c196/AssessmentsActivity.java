package edu.wgu.jwalker.c196;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssessmentsActivity extends AppCompatActivity {

    //variables
    DatabaseHelper db;
    Calendar myCalendar = Calendar.getInstance();
    TableLayout assessmentListTable_AA;
    ScrollView assessmentListView_AA, assessmentEntryView;
    Button saveButton_AA, addButton_AA, cancelButton_AA;
    EditText assessmentHeader, assessmentCode, assessmentDescription;
    TextView dueDate;
    Spinner courseSpinner_AA, statusSpinner_AA, typeSpinner_AA;
    public static final String myPREFERENCES = "MyPrefs";
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
    public void onResume(){
        super.onResume();
        //refill the table
        populateAssessments();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessments);
        db = new DatabaseHelper(this);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        getSupportActionBar().setTitle("Assessments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //casting
        assessmentListTable_AA = (TableLayout) findViewById(R.id.assessmentListTable_AA);
        assessmentListView_AA = (ScrollView) findViewById(R.id.assessmentListView_AA);
        assessmentEntryView = (ScrollView) findViewById(R.id.assessmentEntryView);
        saveButton_AA = (Button) findViewById(R.id.saveButton_AA);
        addButton_AA = (Button) findViewById(R.id.addAssessmentButton_AA);
        cancelButton_AA = (Button) findViewById(R.id.cancelButton_AA);
        assessmentHeader = (EditText) findViewById(R.id.assessmentHeader);
        assessmentCode = (EditText) findViewById(R.id.assessmentCode);
        assessmentDescription = (EditText) findViewById(R.id.assessmentDescription);
        dueDate = (TextView) findViewById (R.id.dueDate_AA);
        typeSpinner_AA = (Spinner) findViewById(R.id.typeSpinner_AA);
        statusSpinner_AA = (Spinner) findViewById(R.id.statusSpinner_AA);
        courseSpinner_AA = (Spinner) findViewById(R.id.courseSpinner_AA);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        assessmentListView_AA.setVisibility(View.VISIBLE);

        //fill the table with existing assessments
        populateAssessments();

        //if coming from Course Detail for new Assessment
        Intent intent = getIntent();
        String activity = intent.getStringExtra("activity");
        if (activity != null) {
            if (activity.equals(".CourseDetailActivity")) {
                addButton_AA.performClick();
                editor.remove("activity");
                editor.commit();
            }
        }

    }

    //on click for date view
    public void dateSelect(View v){
        DatePickerDialog dpd = new DatePickerDialog(AssessmentsActivity.this, datePick, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        List<Long> courseDates = db.getCourseDates(courseSpinner_AA.getSelectedItem().toString());

        dpd.getDatePicker().setMinDate(courseDates.get(0));
        dpd.getDatePicker().setMaxDate(courseDates.get(1));
        dpd.show();
    }

    //populate the list of assessments
    public void populateAssessments(){

        //clear existing view
        assessmentListTable_AA.removeAllViews();

        //get existing Assessments from database
        ArrayList<List> allAssessments = getAssessments();

        for (List<String> list : allAssessments){
            TableRow row = new TableRow(AssessmentsActivity.this);
            TextView idCol = new TextView(AssessmentsActivity.this);
            TextView nameCol = new TextView(AssessmentsActivity.this);
            TextView typeCol = new TextView(AssessmentsActivity.this);
            Button detailBtn_AA = new Button(AssessmentsActivity.this);

            idCol.setText(list.get(0));
            idCol.setMinWidth(120);
            final String idNum = idCol.getText().toString();
            nameCol.setText(list.get(1));
            nameCol.setMinWidth(250);
            nameCol.setMaxEms(10);
            nameCol.setEllipsize(TextUtils.TruncateAt.END);
            nameCol.setSingleLine();
            typeCol.setText(list.get(2));
            typeCol.setMinWidth(250);

            detailBtn_AA.setText("Details");
            detailBtn_AA.setPadding(5,0, 0, 0);
            detailBtn_AA.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //save data for next activity
                    editor.putString("assessment", idNum);
                    editor.commit();

                    //get to the details activity
                    Intent intent = new Intent(v.getContext(), AssessmentDetailActivity.class);
                    v.getContext().startActivity(intent);
                }
            });

            row.addView(idCol);
            row.addView(nameCol);
            row.addView(typeCol);
            row.addView(detailBtn_AA);
            assessmentListTable_AA.addView(row);
        }
    }

    //onClick method for addAssessment button
    public void addAssessment(View v){
        assessmentListView_AA.setVisibility(View.GONE);
        addButton_AA.setVisibility(View.GONE);
        assessmentEntryView.setVisibility(View.VISIBLE);

        //fill in spinners
        //course spinner
        List<String> courseSpinnerList_AA = db.getCourseNames();
        ArrayAdapter<String> courseAdapter_AA = new ArrayAdapter<String>(
               this, android.R.layout.simple_spinner_item, courseSpinnerList_AA);
        courseAdapter_AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner_AA.setAdapter(courseAdapter_AA);
        //set selection if coming from course detail
        String course = sharedPreferences.getString("courseName", "");
        if (!course.isEmpty()){
            int courseSel = courseAdapter_AA.getPosition(course);
            courseSpinner_AA.setSelection(courseSel);
            editor.remove("courseName");
            editor.commit();
        }

        //type spinner
        ArrayAdapter<CharSequence> typeAdapter_AA = ArrayAdapter.createFromResource(this, R.array.assessment_type, android.R.layout.simple_spinner_item);
        typeAdapter_AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner_AA.setAdapter(typeAdapter_AA);

        //status spinner
        ArrayAdapter<CharSequence> statusAdapter_AA = ArrayAdapter.createFromResource(this, R.array.assessment_status, android.R.layout.simple_spinner_item);
        statusAdapter_AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner_AA.setAdapter(statusAdapter_AA);
    }

    //onClick method for save button
    public void saveAssessment_AA(View v){
        boolean dataInvalid = false;
        String name = assessmentHeader.getText().toString();
        String code = assessmentCode.getText().toString();
        String description = assessmentDescription.getText().toString();
        String course = courseSpinner_AA.getSelectedItem().toString();
        String type = typeSpinner_AA.getSelectedItem().toString();
        String status = statusSpinner_AA.getSelectedItem().toString();
        String dueDateString = dueDate.getText().toString();
        long dateNum = 1;

        //data validation methods
        //check for blank name
        if (name.isEmpty()){
            Toast.makeText(AssessmentsActivity.this, "Enter name.", Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        //check that assessment name is unique
        if (!dataInvalid){
            List<String> assessments = db.getAssessmentNames();
            int temp = assessments.contains(name) ? 1 : 2;
            if (temp == 1){
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Assessment name already exists.", Toast.LENGTH_SHORT).show();
            }
        }

        //check for blank code
        if (!dataInvalid){
            if (code.isEmpty()) {
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Enter code.", Toast.LENGTH_SHORT).show();
            }
        }

        //check for blank date and convert to long
        if (!dataInvalid){
            if (dueDateString.isEmpty()){
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Select date.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = sdf.parse(dueDateString);
                    dateNum = date.getTime();
                } catch (ParseException e){
                    dataInvalid = true;
                    Toast.makeText(AssessmentsActivity.this, "There was a problem with your dates.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //check that due date is within course dates
        if (!dataInvalid){
            List<Long> courseDates = db.getCourseDates(course);
            boolean datesvalid = (dateNum >= courseDates.get(0));

            if(!datesvalid){
                dataInvalid = true;
                Toast.makeText(AssessmentsActivity.this, "Due date outside of course dates.", Toast.LENGTH_SHORT).show();
            } else {
                datesvalid = (dateNum <= courseDates.get(1));
                if (!datesvalid){
                    dataInvalid = true;
                    Toast.makeText(AssessmentsActivity.this, "Due date outside of course dates.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //enter data into database
        if (!dataInvalid){
            //get courseID
            int course_id = db.getCourseID(course);

            //insert data
            boolean isInserted = db.insertAssessment(name, code, description, course_id, type, status, dateNum);

            if (isInserted){
                Toast.makeText(AssessmentsActivity.this, "Assessment added.", Toast.LENGTH_SHORT).show();

                finish();
            } else {
                Toast.makeText(AssessmentsActivity.this, "Assessment not added.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //onClick method for cancel button
    public void cancelAssessmentAdd(View v){
        //clear fields
        assessmentHeader.setText("");
        assessmentDescription.setText("");
        assessmentCode.setText("");
        courseSpinner_AA.setSelection(0);
        statusSpinner_AA.setSelection(0);
        typeSpinner_AA.setSelection(0);
        dueDate.setText("");

        assessmentListView_AA.setVisibility(View.VISIBLE);
        addButton_AA.setVisibility(View.VISIBLE);
        assessmentEntryView.setVisibility(View.GONE);
    }

    public ArrayList<List> getAssessments(){
        Cursor res = db.getAllAssessments();

        ArrayList<List> assessmentList = new ArrayList<>();

        while (res.moveToNext()){
            String id = res.getString(0);
            String name = res.getString(3);
            String type = res.getString(7);

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(name);
            data.add(type);
            assessmentList.add(data);
        }

        return assessmentList;
    }

    //DatePicker update method
    private void updateLabel(Calendar chosenCalendar){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String newDate = sdf.format(chosenCalendar.getTime());
        dueDate.setText(newDate);
    }

    //create navigation menu
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navmenu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.termscreen:
                startActivity(new Intent(this, TermsActivity.class));
                return true;
            case R.id.coursesscreen:
                startActivity(new Intent(this, CoursesActivity.class));
                return true;
            case R.id.assessmentsscreen:
                startActivity(new Intent(this, AssessmentsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
