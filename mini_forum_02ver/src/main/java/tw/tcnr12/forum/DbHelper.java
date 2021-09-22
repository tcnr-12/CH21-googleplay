package tw.tcnr12.forum;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

////----------------------------------------------------------
//建構式參數說明：
//context 可以操作資料庫的內容本文，一般可直接傳入Activity物件。
//name 要操作資料庫名稱，如果資料庫不存在，會自動被建立出來並呼叫onCreate()方法。
//factory 用來做深入查詢用，入門時用不到。
//version 版本號碼。
////-----------------------

public class DbHelper extends SQLiteOpenHelper {

    public String sCreateTableCommand;
    public static final int VERSION = 1;// ---料庫版本，資料結構改變的時候要更改這個數字，通常是+1
    String TAG = "tcnr12=>";
    //-----------------------資料庫名稱
    private static final String DB_FILE = "Mini_Forum.db";

    //-----------------------資料表名稱料
    private static final String DB_TABLE_F0100 = "F0100";
    private static final String DB_TABLE_F0101 = "F0101";

    //-----------------------庫物件，固定的欄位變數
    private static SQLiteDatabase database;

    //======================================================
    private static final String Creat_Table_F0100 = "CREATE TABLE " + DB_TABLE_F0100
            + "(" + " ID INTEGER PRIMARY KEY," + "Email TEXT," + "FirstName TEXT,"
            + "LastName TEXT," + "UserImage TEXT," + "Message TEXT," + "PostTime TEXT);";

    private static final String Creat_Table_F0101 = "CREATE TABLE " + DB_TABLE_F0101
            + "(" + " ID INTEGER PRIMARY KEY," + "Today TEXT," + "TodayTopic TEXT,"
            + "Yesterday TEXT,"+ "YesterdayTopic TEXT," + "YesterdayAnswer TEXT);";

    // 需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new DbHelper(context, DB_FILE, null, VERSION)
                    .getWritableDatabase();
        }
        return database;
    }

    public DbHelper(@Nullable Context context, @Nullable String name
            , @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_FILE, null, 1);
        sCreateTableCommand = "";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 執行 新增 DB TABLE 命令
        db.execSQL(Creat_Table_F0100);
        db.execSQL(Creat_Table_F0101);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + DB_TABLE_F0100);
        db.execSQL(" DROP TABLE IF EXISTS " + DB_TABLE_F0101);

        onCreate(db);
    }

//================================F0100=============================
    //*****F0100抓資料*****
    public ArrayList<String> getRecSet_F0100() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE_F0100 +" ORDER BY id ASC";
        Cursor recSet = db.rawQuery(sql, null);
        ArrayList<String> recAry = new ArrayList<String>();
        //----------------------------
        //Log.d(TAG, "recSet=" + recSet);
        int columnCount = recSet.getColumnCount();
        recSet.moveToFirst();
        String fldSet = "";
        if (recSet.getCount() != 0){//-----------判斷資料如果 不是0比 才執行抓資料
            for (int i = 0; i < columnCount; i++)
                fldSet += recSet.getString(i) + "#"; // 欄位跟欄位 用 # 做區隔
            recAry.add(fldSet);
        }
        while (recSet.moveToNext()) {
            fldSet = "";
            for (int i = 0; i < columnCount; i++) {
                fldSet += recSet.getString(i) + "#"; // 欄位跟欄位 用 # 做區隔
            }
            recAry.add(fldSet);
        }
        //------------------------
        recSet.close();
        db.close();
        //Log.d(TAG, "recAry=" + recAry);
        return recAry;
    }
    //*****F0100新增一筆*****
    public long insertRec_F0100(String b_email, String b_firstname,String e_lastname, String b_userimage, String b_message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rec = new ContentValues();

        rec.put("Email", b_email);
        rec.put("FirstName", b_firstname);
        rec.put("LastName", e_lastname);
        rec.put("UserImage", b_userimage);
        rec.put("Message", b_message);
        rec.put("PostTime", "0");
        long rowID = db.insert(DB_TABLE_F0100, null, rec); // SQLite 新增寫法
        db.close();
        return rowID;
    }
    //*****F0100刪除一筆*****
    public Object deleteRec_F0100(String b_id) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE_F0100;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0){
            String whereClause = "id = '" + b_id + "'";
            int rowsAffected = db.delete(DB_TABLE_F0100, whereClause, null);
            recSet.close();
            db.close();
            return rowsAffected;
        } else  {
            recSet.close();
            db.close();
            return -1;
        }
    }
    //*****F0100清空*****
    public int clearRec_F0100() {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE_F0100;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            //			String whereClause = "id < 0";
            int rowsAffected = db.delete(DB_TABLE_F0100, "1", null);
            // From the documentation of SQLiteDatabase delete method:
            // To remove all rows and get a count pass "1" as the whereClause.
            recSet.close();
            db.close();
            return rowsAffected;
        } else {
            recSet.close();
            db.close();
            return -1;
        }
    }
    public ArrayList<String> getRecSet_user_F0100(String user) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + DB_TABLE_F0100 +
                " WHERE Email LIKE ? ORDER BY id ASC";
        String[] args = new String[]{"%" + user+ "%"};

        Cursor recSet = db.rawQuery(sql, args);
        ArrayList<String> recAry = new ArrayList<String>();
        //----------------------------
        int columnCount = recSet.getColumnCount();
        while (recSet.moveToNext()) {
            String fldSet = "";
            for (int i = 0; i < columnCount; i++)
                fldSet += recSet.getString(i) + "#";
            recAry.add(fldSet);
        }
        //------------------------
        recSet.close();
        db.close();
        return recAry;
    }
    //*****F0100寫入SQLite*****
    public long insertRec_m_F0100(ContentValues rec) {
        SQLiteDatabase db = getWritableDatabase();
        long rowID = db.insert(DB_TABLE_F0100, null, rec);
        db.close();
        return rowID;
    }

//================================F0101=============================
    //*****F0101抓資料*****
    public ArrayList<String> getRecSet_F0101() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE_F0101 +" ORDER BY id ASC";
        Cursor recSet = db.rawQuery(sql, null);
        ArrayList<String> recAry = new ArrayList<String>();
        //----------------------------
        //Log.d(TAG, "recSet=" + recSet);
        int columnCount = recSet.getColumnCount();
        recSet.moveToFirst();
        String fldSet = "";
        if (recSet.getCount() != 0){//-----------判斷資料如果 不是0比 才執行抓資料
            for (int i = 0; i < columnCount; i++)
                fldSet += recSet.getString(i) + "#"; // 欄位跟欄位 用 # 做區隔
            recAry.add(fldSet);
        }
        while (recSet.moveToNext()) {
            fldSet = "";
            for (int i = 0; i < columnCount; i++) {
                fldSet += recSet.getString(i) + "#"; // 欄位跟欄位 用 # 做區隔
            }
            recAry.add(fldSet);
        }
        //------------------------
        recSet.close();
        db.close();
        //Log.d(TAG, "recAry=" + recAry);
        return recAry;
    }
    public ArrayList<String> getRecSet_query_F0101(String t_day) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + DB_TABLE_F0101 +
                " WHERE Today LIKE ? ORDER BY id ASC";
        String[] args = new String[]{"%" + t_day+ "%"};

        Cursor recSet = db.rawQuery(sql, args);
        ArrayList<String> recAry = new ArrayList<String>();
        //----------------------------
        int columnCount = recSet.getColumnCount();
        while (recSet.moveToNext()) {
            String fldSet = "";
            for (int i = 0; i < columnCount; i++)
                fldSet += recSet.getString(i) + "#";
            recAry.add(fldSet);
        }
        //------------------------
        recSet.close();
        db.close();
        return recAry;
    }
    //*****F0100清空*****
    public int clearRec_F0101() {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE_F0101;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            //			String whereClause = "id < 0";
            int rowsAffected = db.delete(DB_TABLE_F0101, "1", null);
            // From the documentation of SQLiteDatabase delete method:
            // To remove all rows and get a count pass "1" as the whereClause.
            recSet.close();
            db.close();
            return rowsAffected;
        } else {
            recSet.close();
            db.close();
            return -1;
        }
    }
    //*****F0101寫入SQLite*****
    public long insertRec_m_F0101(ContentValues rec) {
        SQLiteDatabase db = getWritableDatabase();
        long rowID = db.insert(DB_TABLE_F0101, null, rec);
        db.close();
        return rowID;
    }


}

