package edu.wgu.jwalker.c196;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TermsActivity extends AppCompatActivity {


    //variables
    EditText termHeader;
    TextView editStart, editEnd;
    public Button saveButton, addButton, cancelButton;
    DatabaseHelper db;
    Calendar myCalendar = Calendar.getInstance();
    TableLayout table;
    ScrollView termListView, termEntry;
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //to generate DatePicker in date fields
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
        populateTerms();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DatabaseHelper(this);

        getSupportActionBar().setTitle("Terms");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //cast buttons and text fields
        editStart = (TextView) findViewById(R.id.termStart);
        editEnd = (TextView) findViewById(R.id.termEnd);
        saveButton = (Button) findViewById(R.id.saveTermButton);
        addButton = (Button) findViewById(R.id.addTermButton);
        cancelButton = (Button) findViewById(R.id.cancelTermButton);
        table = (TableLayout) findViewById(R.id.termTable);
        termListView = (ScrollView) findViewById(R.id.termListView);
        termEntry = (ScrollView) findViewById(R.id.termEntry);
        termHeader = (EditText) findViewById(R.id.termHeader);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        //DatePickers
        editStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(TermsActivity.this, datePick, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        termListView.setVisibility(View.VISIBLE);

        //fill the table with existing Terms
        populateTerms();
    }

    //populate the list of Terms
    public void populateTerms(){

        //clear existing view
        table.removeAllViews();

        //get existing Terms from Database
        ArrayList<List> allTerms = getTerms();

        //fill TableLayout with existing Terms
        for (List<String> list : allTerms){

            TableRow row = new TableRow(TermsActivity.this);
            TextView idCol = new TextView(TermsActivity.this);
            TextView nameCol = new TextView(TermsActivity.this);
            TextView startCol = new TextView(TermsActivity.this);
            TextView spaceCol = new TextView(TermsActivity.this);
            TextView endCol = new TextView(TermsActivity.this);
            Button detailBtn = new Button(TermsActivity.this);

            //convert dates
            long start = Long.valueOf(list.get(2));
            long end = Long.valueOf(list.get(3));

            idCol.setText(list.get(0));
            idCol.setMinWidth(120);
            final String idNum = idCol.getText().toString();
            nameCol.setText(list.get(1));
            nameCol.setMinWidth(250);
            startCol.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(start)));
            startCol.setMinWidth(250);
            spaceCol.setMinWidth(45);
            endCol.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(end)));
            endCol.setMinWidth(250);

            detailBtn.setText("Details");
            detailBtn.setPadding(15,0, 0, 0);
            detailBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    //put term data into Shared Preferences to send to next activity
                    editor.putString("term", idNum);
                    editor.commit();

                    //switch to Details activity
                    Intent intent = new Intent(v.getContext(), TermDetailsActivity.class);
                    v.getContext().startActivity(intent);
                }
            });

            row.addView(idCol);
            row.addView(nameCol);
            row.addView(startCol);
            row.addView(spaceCol);
            row.addView(endCol);
            row.addView(detailBtn);
            table.addView(row);
        }
    }

    //onClick method for addTerm button
    public void addTerm(View v){
        termListView.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        termEntry.setVisibility(View.VISIBLE);

        if (db.isTermsEmpty()){
            termHeader.setText("Term 1");
        } else {
            fillInTerm();
        }

    }

    //fill in most recent term
    public void fillInTerm(){
        Cursor res = db.getAllTerms();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        res.moveToLast();
        String name = "Term " + (1 + res.getInt(0));
        long startNum = res.getLong(2);
        long endNum = res.getLong(3);
        String start = sdf.format(startNum);
        String end = sdf.format(endNum);

        //fill in new dates
        try {

            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(end));
            c.add(Calendar.DATE, 1);
            start = sdf.format(c.getTime());
            c.add(Calendar.MONTH, 6);
            c.add(Calendar.DATE, -1);
            end = sdf.format(c.getTime());
        } catch (ParseException e){
            Toast.makeText(TermsActivity.this, "There was a problem with your dates.", Toast.LENGTH_SHORT).show();
        }

        termHeader.setText(name);
        editStart.setText(start);
        editEnd.setText(end);
    }

    //onClick method for save button
    public void addData(View v) {
        boolean dataInvalid = false;
        String name = termHeader.getText().toString();
        String start = editStart.getText().toString();
        String end = editEnd.getText().toString();
        long numStart = 1;
        long numEnd = 1;

        //DATA VALIDATION METHODS

        //check that name is not blank
        if (name.isEmpty()){
            Toast.makeText(TermsActivity.this, "Name cannot be blank.", Toast.LENGTH_SHORT).show();
            dataInvalid = true;
        }

        //check that start date has been selected
        if (!dataInvalid) {
            if (start.isEmpty()) {
                Toast.makeText(TermsActivity.this, "Select start date.", Toast.LENGTH_SHORT).show();
                dataInvalid = true;
            }
        }

        //check that Term name is unique
        if (!dataInvalid) {
            List<String> terms = db.getTermNames();
            int temp = terms.contains(name) ? 1 : 2;
            if (temp == 1){
                dataInvalid = true;
                Toast.makeText(TermsActivity.this, "Term name already exists.", Toast.LENGTH_SHORT).show();
            }
            }


        //date validation
        if (!dataInvalid) {

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date startDate, endDate;

            //convert dates to long
            try {
                startDate = sdf.parse(start);
                numStart = startDate.getTime();
                endDate = sdf.parse(end);
                numEnd = endDate.getTime();

                //ensure that terms do not overlap
                boolean overlap = db.dateCompares("terms", numStart, numEnd);
                if (overlap) {
                    dataInvalid = true;
                    Toast.makeText(TermsActivity.this, "Term dates cannot overlap with other terms.", Toast.LENGTH_SHORT).show();
                }

            } catch (ParseException e) {
                dataInvalid = true;
                Toast.makeText(TermsActivity.this, "There was a problem with your dates.", Toast.LENGTH_SHORT).show();
            }
        }

        //insert data
        if (!dataInvalid) {
            boolean isInserted = db.insertTermData(name, numStart, numEnd);

            if (isInserted) {
                Toast.makeText(TermsActivity.this, "Term added.", Toast.LENGTH_SHORT).show();

                //repopulate the table
                populateTerms();

                //switch views
                termEntry.setVisibility(View.GONE);
                termListView.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);
            }
            else
                Toast.makeText(TermsActivity.this, "Term not added.", Toast.LENGTH_SHORT).show();
        }
    }

    //cancel Button
    public void cancelButton(View v){
        termEntry.setVisibility(View.GONE);
        termListView.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
    }

    //DatePicker update method
    private void updateLabel(Calendar chosenCalendar){
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        String newDate = sdf.format(chosenCalendar.getTime());
        chosenCalendar.add(Calendar.MONTH, 6);
        chosenCalendar.add(Calendar.DAY_OF_MONTH, -1);
        String endDate = sdf.format(chosenCalendar.getTime());
        editStart.setText(newDate);
        editEnd.setText(endDate);
    }

    //get existing Terms
    public ArrayList<List> getTerms(){
        Cursor res = db.getAllTerms();

        ArrayList<List> termList = new ArrayList<>();

        while (res.moveToNext()){
            String id = res.getString(res.getColumnIndex("id"));
            String name = res.getString(res.getColumnIndex("name"));
            String start = res.getString(res.getColumnIndex("startdate"));
            String end = res.getString(res.getColumnIndex("enddate"));

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(name);
            data.add(start);
            data.add(end);
            termList.add(data);
        }
        return termList;
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

