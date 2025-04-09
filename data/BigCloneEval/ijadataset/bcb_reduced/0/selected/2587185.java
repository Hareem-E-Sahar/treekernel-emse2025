package com.androidapp.seniorproject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UniversityDataStorage extends SQLiteOpenHelper {

    private static UniversityDataStorage singleton;

    public static Context m_context;

    private static final String DB_NAME = "UniversityDataStorage";

    private static final int DB_VERSION = 1;

    private final String DB_TABLE_UNIVERSITY_CLASS = "university_class";

    private final ArrayList<String> DB_COLS_UNIVERSITY_CLASS = new ArrayList<String>();

    private final String DB_TABLE_UNIV_CLASS_EVENT = "univ_class_event";

    private final String[] DB_COLS_UNIV_CLASS_EVENT = { "univ_class_event_id", "parent_univ_class_id", "name", "recurring_type", "start_date", "due_date" };

    private final String DB_TABLE_ELEARNING_USER = "elearning_user";

    public static final String FORMAT_DATE = "MM-dd-yy";

    public static final String FORMAT_DATE_TIME = "MM-dd-yy hh:mm";

    public static UniversityDataStorage getSingleton() {
        if (singleton == null) {
            singleton = new UniversityDataStorage(m_context);
        }
        return singleton;
    }

    private UniversityDataStorage(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d("DATABASE", "instantiating UniversityDataStorage");
        DB_COLS_UNIVERSITY_CLASS.add("university_class_id");
        DB_COLS_UNIVERSITY_CLASS.add("prefix");
        DB_COLS_UNIVERSITY_CLASS.add("class_number");
        DB_COLS_UNIVERSITY_CLASS.add("class_section");
        DB_COLS_UNIVERSITY_CLASS.add("semester");
        DB_COLS_UNIVERSITY_CLASS.add("title");
    }

    public boolean ResetDB() {
        Log.i("DATABASE", "Resetting the database!");
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DROP TABLE " + DB_TABLE_UNIVERSITY_CLASS);
            db.execSQL("DROP TABLE " + DB_TABLE_UNIV_CLASS_EVENT);
            db.execSQL("DROP TABLE " + DB_TABLE_ELEARNING_USER);
        } catch (Exception e) {
            Log.e("DATABASE", "ResetDB: error dropping tables: " + e.getMessage());
        }
        try {
            onCreate(null);
        } catch (Exception ex) {
            Log.e("DATABASE", "ResetDB: error creating db: " + ex.toString());
            return false;
        }
        return true;
    }

    public boolean ResetDB_demo() {
        if (ResetDB()) {
            try {
                UniversityClass uc = new UniversityClass();
                uc.Prefix = "CS";
                uc.ClassNumber = "4347";
                uc.ClassSection = ".002";
                uc.Semester = "S11";
                uc.Title = "Database Systems";
                SaveUniversityClass(uc);
                uc = new UniversityClass();
                uc.Prefix = "SE";
                uc.ClassNumber = "4352";
                uc.ClassSection = ".501";
                uc.Semester = "S11";
                uc.Title = "Software Design & Architecture";
                SaveUniversityClass(uc);
                uc = new UniversityClass();
                uc.Prefix = "AIM";
                uc.ClassNumber = "3320";
                uc.ClassSection = ".004";
                uc.Semester = "S11";
                uc.Title = "Intermediate Accounting";
                SaveUniversityClass(uc);
                uc = new UniversityClass();
                uc.Prefix = "AIM";
                uc.ClassNumber = "3320";
                uc.ClassSection = ".004";
                uc.Semester = "S11";
                uc.Title = "Intermediate Accounting";
                SaveUniversityClass(uc);
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern(FORMAT_DATE);
                UnivClassEvent uce = new UnivClassEvent();
                uce.ParentUnivClassId = 1;
                uce.dueDate = sdf.parse("05-12-11");
                uce.name = "Register for graduation";
                SaveUnivClassEvent(uce);
                uce = new UnivClassEvent();
                uce.ParentUnivClassId = 2;
                uce.dueDate = sdf.parse("05-12-11");
                uce.name = "Final Exam";
                SaveUnivClassEvent(uce);
                uce = new UnivClassEvent();
                uce.ParentUnivClassId = 2;
                uce.dueDate = sdf.parse("05-10-11");
                uce.name = "Database Design Project";
                uce.RecurringType = 2;
                SaveUnivClassEvent(uce);
                uce = new UnivClassEvent();
                uce.ParentUnivClassId = 4;
                uce.dueDate = sdf.parse("05-14-11");
                uce.name = "Final Exam";
                SaveUnivClassEvent(uce);
                uce = new UnivClassEvent();
                uce.ParentUnivClassId = 4;
                uce.dueDate = sdf.parse("05-06-11");
                uce.name = "Group Project";
                uce.RecurringType = 1;
                SaveUnivClassEvent(uce);
                uce = new UnivClassEvent();
                uce.ParentUnivClassId = 4;
                uce.dueDate = sdf.parse("05-13-11");
                uce.name = "Essay Proposal";
                SaveUnivClassEvent(uce);
            } catch (Exception e) {
                Log.e("DATABASE", "ResetDB_demo error: " + e.toString());
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("DATABASE", "in on create db method, test to see if db exists with test query");
        try {
            if (db == null) db = this.getWritableDatabase();
            db.execSQL("select * from " + DB_TABLE_UNIVERSITY_CLASS + " where 1=0");
        } catch (Exception e) {
            Log.i("DATABASE", "exception on test query, must be first time, so create database");
            db.execSQL("CREATE TABLE " + DB_TABLE_UNIVERSITY_CLASS + " (university_class_id INTEGER PRIMARY KEY AUTOINCREMENT, prefix STRING, " + "class_number STRING, class_section STRING, semester STRING, title STRING)");
            db.execSQL(" CREATE TABLE univ_class_event (univ_class_event_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "parent_univ_class_id INTEGER, name STRING, recurring_type INTEGER, start_date STRING, due_date STRING) ");
            db.execSQL(" CREATE TABLE " + DB_TABLE_ELEARNING_USER + " ( login STRING, pass STRING, last_sync STRING ) ");
            ContentValues values = new ContentValues();
            values.put("login", "");
            values.put("pass", "");
            values.put("last_sync", "1-1-2011 8:00");
            db.insert(DB_TABLE_ELEARNING_USER, null, values);
            UniversityClass univClass = new UniversityClass();
            univClass.Title = "Misc Events";
            univClass.ClassNumber = " ";
            univClass.ClassSection = " ";
            univClass.Prefix = " ";
            univClass.Semester = " ";
            SaveUniversityClass(univClass);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DATABASE", "about to upgrade table");
    }

    public long SaveUniversityClass(UniversityClass univClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        long returnVal = -1;
        String sWhere = "prefix = '" + univClass.Prefix + "' ";
        sWhere += "AND class_number = '" + univClass.ClassNumber + "' ";
        sWhere += "AND class_section = '" + univClass.ClassSection + "' ";
        String sCols[] = { "university_class_id" };
        Cursor c = db.query(DB_TABLE_UNIVERSITY_CLASS, sCols, sWhere, null, null, null, null);
        int rowCount = c.getCount();
        if (rowCount == 1) {
            Log.d("DATABASE", "SaveUniversityClass: class is already in database" + univClass.Title);
            c.moveToFirst();
            returnVal = c.getLong(0);
        } else if (rowCount > 1) {
            Log.e("DATABASE", "SaveUniversityClass: found more than one instance of this class " + univClass.Title);
        } else {
            Log.i("DATABASE", "SaveUniversityClass: insert class into database: " + univClass.Title);
            ContentValues insertValues = new ContentValues();
            insertValues.put("prefix", univClass.Prefix);
            insertValues.put("class_number", univClass.ClassNumber);
            insertValues.put("class_section", univClass.ClassSection);
            insertValues.put("semester", univClass.Semester);
            insertValues.put("title", univClass.Title);
            returnVal = db.insert(DB_TABLE_UNIVERSITY_CLASS, null, insertValues);
            if (returnVal == -1) Log.e("DATABASE", "SaveUniversityClass: error inserting class: " + univClass.Title);
        }
        return returnVal;
    }

    public long SaveUnivClassEvent(UnivClassEvent event) {
        long returnVal = -1;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            if (event.ParentUnivClassId < 0) {
                Log.e("DATABASE", "SaveUnivClassEvent: the event passed in doesn't have a parent univ class id");
                return (long) -1;
            }
            String cols[] = { "univ_class_event_id", "due_date" };
            String sWhere = "parent_univ_class_id = " + event.ParentUnivClassId;
            sWhere += " AND name = '" + event.name + "' ";
            Cursor result = db.query(DB_TABLE_UNIV_CLASS_EVENT, cols, sWhere, null, null, null, null);
            if (result.getCount() > 1) {
                Log.e("DATABASE", "SaveUnivClassEvent: multiple events with the same name: " + event.name);
            } else if (result.getCount() == 1) {
                Log.d("DATABASE", "SaveUnivClassEvent: event is already in db: " + event.name);
                result.moveToFirst();
                returnVal = result.getLong(0);
                SimpleDateFormat s = new SimpleDateFormat();
                s.applyPattern(FORMAT_DATE);
                Date dateInDB = s.parse(result.getString(1));
                if (dateInDB.compareTo(event.dueDate) != 0) {
                    Log.d("DATABASE", "SaveUnivClassEvent: event due_date is different, so updating it.");
                    ContentValues values = new ContentValues();
                    values.put("due_date", s.format(event.dueDate));
                    db.update(DB_TABLE_UNIV_CLASS_EVENT, values, "univ_class_event_id = " + returnVal, null);
                }
            } else {
                SimpleDateFormat s = new SimpleDateFormat();
                s.applyPattern(FORMAT_DATE);
                ContentValues insertValues = new ContentValues();
                insertValues.put("parent_univ_class_id", event.ParentUnivClassId);
                insertValues.put("name", event.name);
                insertValues.put("recurring_type", event.RecurringType);
                insertValues.put("due_date", s.format(event.dueDate));
                returnVal = db.insert("univ_class_event", null, insertValues);
                if (returnVal == -1) {
                    Log.e("DATABASE", "SaveUnivClassEvent: error inserting the event: " + event.name);
                } else {
                    Log.d("DATABASE", "SaveUnivClassEvent: successfully saved event id: " + String.valueOf(returnVal) + " name: " + event.name + " due: " + s.format(event.dueDate));
                }
            }
        } catch (Exception e) {
            Log.e("DATABASE", "SaveUnivClassEvent error: " + e.getMessage() + e.toString());
        }
        return returnVal;
    }

    public void updateLastSync() {
        try {
            Log.i("DATABASE", "updating the last sync value");
            ContentValues values = new ContentValues();
            SimpleDateFormat df = new SimpleDateFormat();
            df.applyPattern(FORMAT_DATE_TIME);
            values.put("last_sync", df.format(new Date()));
            this.getWritableDatabase().update(DB_TABLE_ELEARNING_USER, values, null, null);
        } catch (Exception e) {
            Log.e("DATABASE", "updateLastSync error: " + e.getMessage());
        }
    }

    public ArrayList<UniversityClass> GetUniversityClasses() {
        Log.i("DATABASE", "call to GetUniversityClasses");
        ArrayList<UniversityClass> listClasses = new ArrayList<UniversityClass>();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String[] a = { "" };
            String[] cols = DB_COLS_UNIVERSITY_CLASS.toArray(a);
            Cursor result = db.query(DB_TABLE_UNIVERSITY_CLASS, cols, null, null, null, null, null);
            if (result.getCount() > 0) {
                result.moveToFirst();
                do {
                    UniversityClass univClass = new UniversityClass();
                    univClass.UniversityClassId = result.getLong(DB_COLS_UNIVERSITY_CLASS.indexOf("university_class_id"));
                    univClass.Prefix = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("prefix"));
                    univClass.ClassNumber = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("class_number"));
                    univClass.ClassSection = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("class_section"));
                    univClass.Semester = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("semester"));
                    univClass.Title = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("title"));
                    listClasses.add(univClass);
                } while (result.moveToNext());
            } else {
                Log.i("DATABASE", "GetUniversityClasses: no classes found");
            }
        } catch (Exception e) {
            Log.e("DATABASE", "GetUniversityClasses error: " + e.toString() + e.getMessage());
        }
        return listClasses;
    }

    public UniversityClass GetUniversityClass_byID(long university_class_id) {
        Log.i("DATABASE", "call to GetUniversityClass_byID");
        UniversityClass univClass = new UniversityClass();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String[] a = { "" };
            String[] cols = DB_COLS_UNIVERSITY_CLASS.toArray(a);
            Cursor result = db.query(DB_TABLE_UNIVERSITY_CLASS, cols, "university_class_id = " + university_class_id, null, null, null, null);
            if (result.getCount() == 1) {
                result.moveToFirst();
                univClass.UniversityClassId = result.getLong(DB_COLS_UNIVERSITY_CLASS.indexOf("university_class_id"));
                univClass.Prefix = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("prefix"));
                univClass.ClassNumber = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("class_number"));
                univClass.ClassSection = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("class_section"));
                univClass.Semester = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("semester"));
                univClass.Title = result.getString(DB_COLS_UNIVERSITY_CLASS.indexOf("title"));
            } else {
                Log.i("DATABASE", "GetUniversityClass_byID: no class found");
            }
        } catch (Exception e) {
            Log.e("DATABASE", "GetUniversityClass_byID error: " + e.toString() + e.getMessage());
        }
        return univClass;
    }

    public ArrayList<UnivClassEvent> GetUnivClassEvents() {
        Log.i("DATABASE", "call to GetUnivClassEvents");
        ArrayList<UnivClassEvent> listEvents = new ArrayList<UnivClassEvent>();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor result = db.query(DB_TABLE_UNIV_CLASS_EVENT, DB_COLS_UNIV_CLASS_EVENT, null, null, null, null, null);
            if (result.getCount() > 0) {
                result.moveToFirst();
                do {
                    UnivClassEvent event = new UnivClassEvent();
                    event.UnivClassEventId = result.getLong(0);
                    event.ParentUnivClassId = result.getLong(1);
                    event.name = result.getString(2);
                    event.RecurringType = result.getInt(3);
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.applyPattern(FORMAT_DATE);
                    String sStartDate = result.getString(4);
                    if (sStartDate != null) event.startDate = df.parse(sStartDate);
                    String sDueDate = result.getString(5);
                    if (sDueDate != null) event.dueDate = df.parse(sDueDate);
                    listEvents.add(event);
                } while (result.moveToNext());
                Collections.sort(listEvents, new Comparator<UnivClassEvent>() {

                    public int compare(UnivClassEvent o1, UnivClassEvent o2) {
                        return o1.dueDate.compareTo(o2.dueDate);
                    }
                });
            } else {
                Log.i("DATABASE", "GetUnivClassEvents: no events found");
            }
        } catch (Exception e) {
            Log.e("DATABASE", "GetUnivClassEvents error: " + e.toString() + e.getMessage());
        }
        Log.i("DATABASE", "end of GetUnivClassEvents - returning " + listEvents.size() + " events");
        return listEvents;
    }

    public UnivClassEvent GetUnivClassEvent_byID(long univ_class_event_id) {
        Log.i("DATABASE", "call to GetUnivClassEvent_byID");
        UnivClassEvent univClassEvent = new UnivClassEvent();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor result = db.query(DB_TABLE_UNIV_CLASS_EVENT, DB_COLS_UNIV_CLASS_EVENT, "univ_class_event_id = " + univ_class_event_id, null, null, null, null);
            if (result.getCount() == 1) {
                result.moveToFirst();
                univClassEvent.UnivClassEventId = result.getLong(0);
                univClassEvent.ParentUnivClassId = result.getLong(1);
                univClassEvent.name = result.getString(2);
                univClassEvent.RecurringType = result.getInt(3);
                SimpleDateFormat df = new SimpleDateFormat();
                df.applyPattern(FORMAT_DATE);
                String sStartDate = result.getString(4);
                if (sStartDate != null) univClassEvent.startDate = df.parse(sStartDate);
                String sDueDate = result.getString(5);
                if (sDueDate != null) univClassEvent.dueDate = df.parse(sDueDate);
            } else {
                Log.i("DATABASE", "GetUnivClassEvent_byID: no event found");
            }
        } catch (Exception e) {
            Log.e("DATABASE", "GetUnivClassEvent_byID error: " + e.toString() + e.getMessage());
        }
        return univClassEvent;
    }

    public String getLastSync() {
        String lastSync = "";
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String[] cols = { "last_sync" };
            Cursor result = db.query(this.DB_TABLE_ELEARNING_USER, cols, null, null, null, null, null);
            result.moveToFirst();
            lastSync = result.getString(0);
        } catch (Exception e) {
            Log.e("DATABASE", "getLastSync error: " + e.getMessage());
        }
        return lastSync;
    }

    public boolean IsUserNameSaved() {
        Log.d("DATABASE", "calling IsUserNameSaved");
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String[] cols = { "login", "pass" };
            Cursor result = db.query(this.DB_TABLE_ELEARNING_USER, cols, null, null, null, null, null);
            if (result.getCount() == 1) {
                result.moveToFirst();
                String username = result.getString(0);
                if (username.length() > 0) return true; else return false;
            } else return false;
        } catch (Exception e) {
            Log.e("DATABASE", "IsUserNameSaved error: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<String> GetUserPass() {
        ArrayList<String> listUP = new ArrayList<String>();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String cols[] = { "login", "pass" };
            Cursor result = db.query(DB_TABLE_ELEARNING_USER, cols, null, null, null, null, null);
            if (result.getCount() == 1) {
                result.moveToFirst();
                listUP.add(result.getString(0));
                listUP.add(result.getString(1));
            } else {
                Log.e("DATABASE", "GetUserPass: wrong number of rows returned");
            }
        } catch (Exception e) {
            Log.e("DATABASE", "GetUserPass error: " + e.toString());
        }
        return listUP;
    }

    public void SaveUserPass(String username, String password) {
        Log.i("DATABASE", "Call to SaveUserPass");
        try {
            ResetDB();
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("login", username);
            values.put("pass", password);
            db.update(DB_TABLE_ELEARNING_USER, values, null, null);
        } catch (Exception e) {
            Log.e("DATABASE", "SaveUserPass error: " + e.toString());
        }
    }
}
