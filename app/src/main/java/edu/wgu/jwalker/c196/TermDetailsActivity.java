package edu.wgu.jwalker.c196;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TermDetailsActivity extends AppCompatActivity {

    //variables
    TextView termDetailHeader;
    TextView termDetailStart, termDetailEnd;
    Button deleteTermBtn;
    DatabaseHelper db;
    TableLayout innerCoursesTable;
    ScrollView termDetails;
    String termID = null;

    //to generate DatePicker
//    DatePickerDialog.OnDateSetListener datePick = new DatePickerDialog.OnDateSetListener() {
//        @Override
//        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//            myCalendar.set(Calendar.YEAR, year);
//            myCalendar.set(Calendar.MONTH, month);
//            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
////            updateLabel(myCalendar);
//            myCalendar = Calendar.getInstance();
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //cast fields and buttons
        db = new DatabaseHelper(this);
        termDetails = (ScrollView) findViewById(R.id.termDetails);
        termDetailHeader = (TextView) findViewById(R.id.termDetailHeader);
        termDetailStart = (TextView) findViewById(R.id.termDetailStart);
        termDetailEnd = (TextView) findViewById(R.id.termDetailEnd);
        deleteTermBtn = (Button) findViewById(R.id.deleteTermDetail);
        innerCoursesTable = (TableLayout) findViewById(R.id.innerCoursesTable);

        //get data from previous activity
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        termID = sharedPreferences.getString("term", "");

        //fill in fields
        fillInTerm(termID);

        populateCourseTable(termID);

    }

    public void fillInTerm(String termID){
        Cursor res = db.getTerm(termID);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        //get term data
        res.moveToFirst();
        String name = res.getString(1);
        long startNum = res.getLong(2);
        long endNum = res.getLong(3);
        String start = sdf.format(startNum);
        String end = sdf.format(endNum);

        //fill in fields
        termDetailHeader.setText(name);
        termDetailStart.setText(start);
        termDetailEnd.setText(end);

        //set activity title
        getSupportActionBar().setTitle(name);
    }

    public void populateCourseTable(String termID){
        //clear table
        innerCoursesTable.removeAllViews();

        //get courses
        Cursor res = db.getCoursesFromTerm(termID);
        ArrayList<List> coursesList = new ArrayList<>();

        while (res.moveToNext()){
            String id = res.getString(0);
            String name = res.getString(1);

            List<String> data = new ArrayList<>();
            data.add(id);
            data.add(name);

            coursesList.add(data);
        }

        for (List<String> list : coursesList){
            final TableRow row = new TableRow(TermDetailsActivity.this);
            TextView idCol = new TextView(TermDetailsActivity.this);
            TextView nameCol = new TextView(TermDetailsActivity.this);
            Button removeCourseBtn = new Button(TermDetailsActivity.this);

            //fill in data
            idCol.setText(list.get(0));
            idCol.setMinWidth(120);
            final String idNum = idCol.getText().toString();
            nameCol.setText(list.get(1));
            nameCol.setPadding(0,0, 5, 0);
            nameCol.setMaxEms(17);
            nameCol.setSingleLine();
            nameCol.setEllipsize(TextUtils.TruncateAt.END);

            //create remove button
            removeCourseBtn.setText("Delete");
            removeCourseBtn.setPadding(8, 0, 0, 0);
            removeCourseBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    db.deleteCourse(idNum);
                    row.setVisibility(View.GONE);
                }
            });

            row.addView(idCol);
            row.addView(nameCol);
            row.addView(removeCourseBtn);
            innerCoursesTable.addView(row);
        }
    }

    //delete the term
    public void deleteTerm(View v){
        //check that term has no enrolled courses
        boolean empty = db.ifTermHasCourses(termID);

        if(empty){
            //delete term
            db.deleteTerm(termID);
            Toast.makeText(TermDetailsActivity.this, "Term deleted.", Toast.LENGTH_SHORT).show();

            //return to terms list
            Intent intent = new Intent(v.getContext(), TermsActivity.class);
            v.getContext().startActivity(intent);
        } else {
            Toast.makeText(TermDetailsActivity.this, "Term cannot be deleted while courses are assigned.", Toast.LENGTH_LONG).show();
        }
    }



}
