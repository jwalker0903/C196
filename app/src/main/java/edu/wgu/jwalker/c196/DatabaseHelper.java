package edu.wgu.jwalker.c196;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    //database properties
    private static final String DATABASE_NAME = "wgucoursetracker.db";
    private static final int DATABASE_VERSION = 1;

    //table names
    private static final String TABLE_TERMS = "terms";
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_ASSESSMENTS = "assessments";
    private static final String TABLE_COURSE_NOTES = "courseNotes";
    private static final String TABLE_ASSESSMENT_NOTES = "assessmentNotes";

    //TERMS table column names
    private static final String TERMS_ID = "id";
    private static final String TERMS_NAME = "name";
    private static final String TERMS_STARTDATE = "startdate";
    private static final String TERMS_ENDDATE = "enddate";

    //COURSES table column names
    private static final String COURSES_ID = "id";
    private static final String COURSES_TERM_ID = "termId";
    private static final String COURSES_NAME = "name";
    private static final String COURSES_DESCRIPTION = "description";
    private static final String COURSES_STARTDATE = "startdate";
    private static final String COURSES_ENDDATE = "enddate";
    private static final String COURSES_MENTORNAME = "mentorname";
    private static final String COURSES_MENTORPHONE = "mentorphone";
    private static final String COURSES_MENTOREMAIL = "mentoremail";
    private static final String COURSES_STATUS = "status";

    //ASSESSMENTS table column names
    private static final String ASSESSMENTS_ID = "id";
    private static final String ASSESSMENTS_NAME = "name";
    private static final String ASSESSMENTS_COURSE_ID = "courseId";
    private static final String ASSESSMENTS_CODE = "code";
    private static final String ASSESSMENTS_DESCRIPTION = "description";
    private static final String ASSESSMENTS_TYPE = "type";
    private static final String ASSESSMENTS_DATE = "date";
    private static final String ASSESSMENTS_STATUS = "status";

    //COURSE_NOTES table column names
    private static final String COURSE_NOTES_ID = "id";
    private static final String COURSE_NOTES_COURSE_ID = "courseId";
    private static final String COURSE_NOTES_TEXT = "text";
    private static final String COURSE_NOTES_CREATED = "created";

    //ASSESSEMENT_NOTES table column names
    private static final String ASSESSMENT_NOTES_ID = "id";
    private static final String ASSESSMENT_NOTES_COURSE_ID = "assessmentId";
    private static final String ASSESSMENT_NOTES_TEXT = "text";
    private static final String ASSESSMENT_NOTES_CREATED = "created";

    //Create strings for tables

    private static final String TERMS_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_TERMS + " (" +
                    TERMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TERMS_NAME + " TEXT, " +
                    TERMS_STARTDATE + " DATE, " +
                    TERMS_ENDDATE + " DATE, " +
                    "CONSTRAINT constraint_name UNIQUE (" + TERMS_NAME + ")" +
                    ")";

    private static final String COURSES_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_COURSES + " (" +
                    COURSES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSES_NAME + " TEXT, " +
                    COURSES_TERM_ID + " INTEGER, " +
                    COURSES_DESCRIPTION + " TEXT, " +
                    COURSES_STARTDATE + " DATE, " +
                    COURSES_ENDDATE + " DATE, " +
                    COURSES_MENTORNAME + " TEXT, " +
                    COURSES_MENTORPHONE + " TEXT, " +
                    COURSES_MENTOREMAIL + " TEXT," +
                    COURSES_STATUS + " TEXT, " +
                    "CONSTRAINT fk_terms FOREIGN KEY (" + COURSES_TERM_ID + ")" +
                    " REFERENCES " + TABLE_TERMS + "(" + TERMS_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    private static final String ASSESSMENTS_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_ASSESSMENTS + " (" +
                    ASSESSMENTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENTS_COURSE_ID + " INTEGER, " +
                    ASSESSMENTS_CODE + " TEXT, " +
                    ASSESSMENTS_NAME + " TEXT, " +
                    ASSESSMENTS_DESCRIPTION + " TEXT, " +
                    ASSESSMENTS_TYPE + " TEXT, " +
                    ASSESSMENTS_DATE + " DATE, " +
                    ASSESSMENTS_STATUS + " TEXT, " +
                    "CONSTRAINT fk_courses " +
                    "FOREIGN KEY(" + ASSESSMENTS_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COURSES_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    private static final String COURSE_NOTES_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_COURSE_NOTES + " (" +
                    COURSE_NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSE_NOTES_COURSE_ID + " INTEGER, " +
                    COURSE_NOTES_TEXT + " TEXT, " +
                    COURSE_NOTES_CREATED + "TEXT default CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_courses_2 " +
                    "FOREIGN KEY(" + COURSE_NOTES_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COURSES_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    private static final String ASSESSMENT_NOTES_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_ASSESSMENT_NOTES + " (" +
                    ASSESSMENT_NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENT_NOTES_COURSE_ID + " INTEGER, " +
                    ASSESSMENT_NOTES_TEXT + " TEXT, " +
                    ASSESSMENT_NOTES_CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_assessments " +
                    "FOREIGN KEY(" + ASSESSMENT_NOTES_CREATED + ") REFERENCES " + TABLE_ASSESSMENTS + "(" + ASSESSMENTS_ID + ")" +
                    " ON DELETE CASCADE" +
                    ")";

    //constructor
    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //create method
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(TERMS_TABLE_CREATE);
        db.execSQL(COURSES_TABLE_CREATE);
        db.execSQL(ASSESSMENTS_TABLE_CREATE);
        db.execSQL(COURSE_NOTES_TABLE_CREATE);
        db.execSQL(ASSESSMENT_NOTES_TABLE_CREATE);
    }

    //onOpen method to ensure cascade deletion functions
    public void onOpen(SQLiteDatabase db){
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    //upgrade method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENT_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
    }

    //insert to TERMS
    public boolean insertTermData(String name, long start, long end){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(TERMS_NAME, name);
        contentValues.put(TERMS_STARTDATE, start);
        contentValues.put(TERMS_ENDDATE, end);
        long result = db.insert(TABLE_TERMS, null, contentValues);

        if (result == -1)
            return false;
        else
            return true;
    }

    //retrieve all from TERMS
    public Cursor getAllTerms(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_TERMS +
                                        " ORDER BY " + TERMS_STARTDATE , null);
        return res;
    }

    //retrieve one term from TERMS by id
    public Cursor getTerm(String termID){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TERMS +
                                " WHERE " + TERMS_ID + " = " + termID;

        Cursor res = db.rawQuery(selectQuery, null);

        return res;
    }

    //delete row from TERMS
    public void deleteTerm(String termID){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM " + TABLE_TERMS +
                                " WHERE " + TERMS_ID + " = " + termID;
        db.execSQL(selectQuery);
    }

    //get all Term names for spinner and unique check
    public List<String> getTermNames(){

        //get names from database
        Cursor res = getAllTerms();

        //create and populate list
        List<String> termNames = new ArrayList<>();
        while(res.moveToNext()){
            termNames.add(res.getString(res.getColumnIndex("name")));
        }
        res.close();

        //return the list
        return termNames;
    }

    public String getTermName(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_TERMS +
                                        " WHERE " + TERMS_ID + " = '" + id + "'", null);

        res.moveToFirst();
        String name = res.getString(1);
        res.close();
        return name;
    }

    //return termId from termName
    public int getTermId(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_TERMS +
                                        " WHERE " + TERMS_NAME + " = '" + name + "'", null);

        res.moveToFirst();
        int id = res.getInt(0);
        res.close();
        return id;
    }

    //check to see if the Terms table is empty
    public boolean isTermsEmpty(){

        boolean empty = true;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TERMS, null);
        if (res != null && res.moveToFirst()){
            empty = (res.getInt(0) == 0);
        }

        res.close();

        return empty;
    }

    //check if a Term has assigned courses
    public boolean ifTermHasCourses(String termID){
        boolean empty = true;
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_COURSES +
                                " WHERE " + COURSES_TERM_ID + " = " + termID;

        Cursor res = db.rawQuery(selectQuery, null);

        if (res != null && res.moveToFirst()){
            empty = (res.getInt(0) == 0);
        }

        return empty;
    }

    //get term start and end dates
    public List<Long> getTermDates(String termName){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TERMS +
                                " WHERE " + TERMS_NAME + " = '" + termName + "'";
        Cursor res = db.rawQuery(selectQuery, null);

        List<Long> termDates = new ArrayList<>();
        res.moveToFirst();
        termDates.add(res.getLong(2));
        termDates.add(res.getLong(3));

        return termDates;
    }

    //retrieve all from Courses
    public Cursor getAllCourses(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES;

        Cursor res = db.rawQuery(selectQuery, null);

        return res;
    }

    //get all course names
    public List<String> getCourseNames(){
        //get names from database
        Cursor res = getAllCourses();

        //create and populate list
        List<String> courseNames = new ArrayList<>();
        while(res.moveToNext()){
            courseNames.add(res.getString(res.getColumnIndex("name")));
        }
        res.close();

        //return the list
        return courseNames;
    }

    //retrieve one course from COURSES by id
    public Cursor getCourse(String courseID){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES +
                " WHERE " + COURSES_ID + " = " + courseID;

        Cursor res = db.rawQuery(selectQuery, null);

        return res;
    }

    //return only the Course name when given course ID
    public String getCourseName(String courseID){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES +
                " WHERE " + COURSES_ID + " = " + courseID;

        Cursor res = db.rawQuery(selectQuery, null);
        res.moveToFirst();
        return res.getString(1);
    }

    //retreive course id from COURSES by name
    public int getCourseID(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COURSES_ID + " FROM " + TABLE_COURSES +
                                " WHERE " + COURSES_NAME + " = " + name;
//        Cursor res = db.rawQuery(selectQuery, null);

        String[] columnsToReturn = {COURSES_ID};
        String selection = COURSES_NAME + " = ?";
        String[] selectionArgs = {name};
        Cursor res = db.query(TABLE_COURSES, columnsToReturn, selection, selectionArgs, null, null, null);


        res.moveToFirst();

        return res.getInt(0);
    }

    //retrieve all courses from one term
    public Cursor getCoursesFromTerm(String termID){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES +
                                " WHERE " + COURSES_TERM_ID + " = " + termID +
                                " ORDER BY " + COURSES_STARTDATE;

        Cursor res = db.rawQuery(selectQuery, null);
        return res;
    }

    public List<Long> getCourseDates(String courseName){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COURSES_STARTDATE + ", " + COURSES_ENDDATE + " FROM " + TABLE_COURSES +
                " WHERE " + COURSES_NAME + " = '" + courseName + "'";
        Cursor res = db.rawQuery(selectQuery, null);

        List<Long> courseDates = new ArrayList<>();
        res.moveToFirst();
        courseDates.add(res.getLong(0));
        courseDates.add(res.getLong(1));

        return courseDates;
    }

    //insert into Courses
    public boolean insertCourseData(String name, String term, String description, long start, long end,
                                    String status, String menName, String menPhone, String menMail){

        //get termId, if term entered; otherwise, put a zero
        int termId;

        if (term == "none"){
            termId = 0;
        } else {
            termId = getTermId(term);
        }

        //get database and ContentValues container
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COURSES_NAME, name);
        contentValues.put(COURSES_TERM_ID, termId);
        contentValues.put(COURSES_DESCRIPTION, description);
        contentValues.put(COURSES_STARTDATE, start);
        contentValues.put(COURSES_ENDDATE, end);
        contentValues.put(COURSES_MENTORNAME, menName);
        contentValues.put(COURSES_MENTORPHONE, menPhone);
        contentValues.put(COURSES_MENTOREMAIL, menMail);
        contentValues.put(COURSES_STATUS, status);
        long result = db.insert(TABLE_COURSES, null, contentValues);

        if (result == -1)
            return false;
        else
            return true;
    }

    //delete course
    public void deleteCourse(String courseID){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_COURSES + " WHERE " + COURSES_ID + " = " + courseID;
        db.execSQL(query);
    }

    //update course
    public boolean updateCourse(String courseID, String name, int term, String description,
                             long numStart, long numEnd, String status, String menName,
                             String menPhone, String menMail){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COURSES_NAME, name);
        contentValues.put(COURSES_TERM_ID, term);
        contentValues.put(COURSES_DESCRIPTION, description);
        contentValues.put(COURSES_STARTDATE, numStart);
        contentValues.put(COURSES_ENDDATE, numEnd);
        contentValues.put(COURSES_STATUS, status);
        contentValues.put(COURSES_MENTORNAME, menName);
        contentValues.put(COURSES_MENTORPHONE, menPhone);
        contentValues.put(COURSES_MENTOREMAIL, menMail);
        String where = COURSES_ID + "=?";
        String[] whereArgs = new String[] {String.valueOf(courseID)};

        long result = db.update(TABLE_COURSES, contentValues, where, whereArgs);

        if (result == -1)
            return false;
        else
            return true;
    }

    //check if courses table if empty
    public boolean isCoursesEmpty(){
        boolean empty = true;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_COURSES, null);
        if (res != null && res.moveToFirst()){
            empty = (res.getInt(0) == 0);
        }

        res.close();

        return empty;
    }

    //retrieve all assessments
    public Cursor getAllAssessments(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSESSMENTS +
                                " GROUP BY " + ASSESSMENTS_COURSE_ID +
                                " ORDER BY " + ASSESSMENTS_DATE;

        return db.rawQuery(selectQuery, null);
    }

    //retrieve assessments for single course
    public Cursor getAssessmentsForOneCourse(String courseID){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSESSMENTS +
                                " WHERE " + ASSESSMENTS_COURSE_ID + " = " + courseID;

        return db.rawQuery(selectQuery, null);
    }

    //get all assessment names
    public List<String> getAssessmentNames(){
        //get names from database
        Cursor res = getAllAssessments();

        //create and populate list
        List<String> assessmentNames = new ArrayList<>();
        while(res.moveToNext()){
            assessmentNames.add(res.getString(res.getColumnIndex("name")));
        }
        res.close();

        //return the list
        return assessmentNames;
    }

    //retreive assessment
    public Cursor getAssessmentData(String assessmentID){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASSESSMENTS + " WHERE " + ASSESSMENTS_ID + " = " + assessmentID;
        return db.rawQuery(selectQuery, null);
    }

    //insert new Assessment into ASSESSMENTS
    public boolean insertAssessment(String name, String code, String description, int course, String type,
                            String status, long dueDate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ASSESSMENTS_NAME, name);
        contentValues.put(ASSESSMENTS_CODE, code);
        contentValues.put(ASSESSMENTS_DESCRIPTION, description);
        contentValues.put(ASSESSMENTS_COURSE_ID, course);
        contentValues.put(ASSESSMENTS_TYPE, type);
        contentValues.put(ASSESSMENTS_DATE, dueDate);
        contentValues.put(ASSESSMENTS_STATUS, status);

        long result = db.insert(TABLE_ASSESSMENTS, null, contentValues);

        return result != -1;
    }

    //update Assessment entry
    public boolean updateAssessment(String id, String name, String code, String description, int course, String type,
                                 String status, long dueDate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ASSESSMENTS_NAME, name);
        contentValues.put(ASSESSMENTS_CODE, code);
        contentValues.put(ASSESSMENTS_DESCRIPTION, description);
        contentValues.put(ASSESSMENTS_COURSE_ID, course);
        contentValues.put(ASSESSMENTS_TYPE, type);
        contentValues.put(ASSESSMENTS_DATE, dueDate);
        contentValues.put(ASSESSMENTS_STATUS, status);

        String where = ASSESSMENTS_ID + "=?";
        String[] whereArgs = new String[] {String.valueOf(id)};

        long result = db.update(TABLE_ASSESSMENTS, contentValues, where, whereArgs);

        return result != -1;

    }

    //method to ensure selected date does not fall inside start dates of other terms or courses
    public boolean dateCompares(String table, long start, long end){
        SQLiteDatabase db = this.getReadableDatabase();
        boolean overlap = false;
        String tableStart, tableEnd;
        int resStartCount, resEndCount;

        if (table.equals("terms")){
            table = TABLE_TERMS;
            tableStart = TERMS_STARTDATE;
            tableEnd = TERMS_ENDDATE;
            resStartCount = 3;
            resEndCount = 4;
        } else {
            table = TABLE_COURSES;
            tableStart = COURSES_STARTDATE;
            tableEnd = COURSES_ENDDATE;
            resStartCount = 4;
            resEndCount = 5;
        }

        String selectQuery = "SELECT * FROM " + table + " WHERE " +
                                tableStart + " BETWEEN " + start + " AND " + end;

        Cursor res = db.rawQuery(selectQuery, null);

        //trigger the overlap flag, unless the date is equal
        if (res.getCount() != 0){
            res.moveToFirst();
            if (start != res.getLong(resStartCount)) {
                overlap = true;
            }
        }
        res.close();

        //same as above, but with end date
        if (!overlap){
            selectQuery = "SELECT * FROM " + table + " WHERE " +
                    tableEnd + " BETWEEN " + start + " AND " + end;

            res = db.rawQuery(selectQuery, null);

            if (res.getCount() != 0){
                res.moveToFirst();
                if (end != res.getLong(resEndCount)) {
                    overlap = true;
                }
            }
        }
        res.close();

        return overlap;
    }

    //create new Course note
    public boolean insertCourseNote(String courseID, String noteText){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COURSE_NOTES_COURSE_ID, courseID);
        contentValues.put(COURSE_NOTES_TEXT, noteText);

        long result = db.insert(TABLE_COURSE_NOTES, null, contentValues);
        db.close();

        return result != -1;
    }

    //delete course note
    public void deleteCourseNote(String noteID){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM " + TABLE_COURSE_NOTES + " WHERE " + COURSE_NOTES_ID + " = " + noteID;
        db.execSQL(selectQuery);
    }

    public Cursor getCourseNotes(String courseID){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSE_NOTES +
                                " WHERE " + COURSE_NOTES_COURSE_ID + " = " + courseID;

        return db.rawQuery(selectQuery, null);
    }

    public boolean insertAssessmentNote(String assessmentID, String noteText){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COURSE_NOTES_COURSE_ID, assessmentID);
        contentValues.put(COURSE_NOTES_TEXT, noteText);

        long result = db.insert(TABLE_ASSESSMENT_NOTES, null, contentValues);

        return result != -1;
    }


    public void deleteEverything(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_COURSES);
        db.execSQL("vacuum");
        db.execSQL("DELETE FROM " + TABLE_TERMS);
        db.execSQL("vacuum");
    }
}


