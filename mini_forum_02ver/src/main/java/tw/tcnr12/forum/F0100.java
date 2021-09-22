package tw.tcnr12.forum;


import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class F0100 extends AppCompatActivity implements View.OnClickListener{

    private Intent intent = new Intent();
    String TAG = "tcnr12=>";
    private Button b001;
    private EditText e001;
    private TextView t004, t005;
    private ImageButton img004;
    private String e_email, e_firstname, e_lastname, e_userimage, e_message;
    private String tid, s_id, msg;
    private int view01;
    private Menu menu;
    //------------------------Google 會員登入---------------
    private GoogleSignInAccount ACC;
    //------------------------DataBase---------------------
    private DbHelper dbHper;
    private static final String DB_FILE = "Mini_Forum.db";
    private static final int DB_version = 1;
    private ArrayList<String> recSet_F0100, recSet_F0101;
    private String[] f0100;
    //------------------------現在時間----------------------
    private String str;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    //------------------------執行續------------------------
    private Handler handler = new Handler();
    int autotime = 10;//---要幾秒的時間，更新匯入MySQL資料
    int update_time = 0;
    private boolean runAuto_flag = false;  //Runable updateTime 狀態


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableStrictMode(this);//---------------------使用暫存堆疊，需要用此方法
        super.onCreate(savedInstanceState);
        setContentView(R.layout.f0100);
        initDB();
        setupViewComponent();
    }

    //-------------抓取遠端資料庫設定執行續---------------
    private void enableStrictMode(Context context) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().
                detectDiskReads().
                detectDiskWrites().
                detectNetwork().
                penaltyLog().
                build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().
                detectLeakedSqlLiteObjects().
                penaltyLog().
                penaltyDeath().
                build());
    }

    //---------------------檢查登入狀態----------------------
    private void check_SignIn() {
        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            ACC =account;
        } else {
            ACC = null;
        }
    }

    //-----------------------SQLite------------------------
    private void initDB() {
        if (dbHper == null){
            dbHper = new DbHelper(this, DB_FILE, null, DB_version);
        }
        dbmysql_F0100();
        dbmysql_F0101();
    }

    //------------------設定Layout元件------------------------
    private void setupViewComponent() {
        b001 = (Button)findViewById(R.id.f0100_b001);
        e001 = (EditText)findViewById(R.id.f0100_e001);
        t004 = (TextView)findViewById(R.id.f0100_t004);
        t005 = (TextView)findViewById(R.id.f0100_t005);
        img004 = (ImageButton)findViewById(R.id.f0100_img004);

        b001.setOnClickListener(this);
        img004.setOnClickListener(this);
        //--------------------抓取螢幕尺寸-----------------------
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //--------------------動態設定長度------------------------
        e001.getLayoutParams().width = displayMetrics.widthPixels / 30 * 22;
        b001.getLayoutParams().width = displayMetrics.widthPixels / 30 * 6;

        showTopic();

        //====================設執行緒=======================
        if ( runAuto_flag = false){
            handler.postDelayed(updateTimer, 100);  // 設定Delay的時間
            runAuto_flag = true;
        }

        //----顯示最後面----
        last_scroll();
    }

    //==========================設定執行續========================
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, autotime * 1000); // 真正延遲的時間
            //--------更新時間
            java.sql.Date curDate = new java.sql.Date(System.currentTimeMillis()); //獲取當前時間
            str = formatter.format(curDate);
            t005.setText(str+" (10秒更新1次)");
            //--------執行匯入MySQL
            dbmysql_F0100();
            recSet_F0100 = dbHper.getRecSet_F0100();
            showRec();
            //----------------
            ++update_time;
        }
    };

    private void showTopic() {
        java.sql.Date curDate = new java.sql.Date(System.currentTimeMillis()); //獲取當前時間
        str = formatter.format(curDate);

        if (str != null){
            String day = str.substring(0, 10);
            recSet_F0101 = dbHper.getRecSet_query_F0101(day);

            //---有資料才執行---
            if (recSet_F0101.size() != 0){
                //---擷取SQLite資料---
                String[] f0101 = recSet_F0101.get(0).split("#");
                t004.setText(getString(R.string.f0100_t004)+"\n"+f0101[2]);
            }
        }
    }

    private void showRec() {
        //---有資料才執行---
        if (recSet_F0100.size() != 0){
            //---宣告---
            TextView objt001 = (TextView) findViewById(R.id.f0100_t001);
            TextView objt002 = (TextView) findViewById(R.id.f0100_t002);
            TextView objt003 = (TextView) findViewById(R.id.f0100_t003);
            TableRow objt004 = (TableRow) findViewById(R.id.f0100_tab001);
            TableRow objt005 = (TableRow) findViewById(R.id.f0100_tab002);
            CircleImgView objt006 = (CircleImgView) findViewById(R.id.f0100_img002);
            LinearLayout objt007 = (LinearLayout) findViewById(R.id.f0100_lay002);
            ImageButton objt008 = (ImageButton)findViewById(R.id.f0100_img003);

            //---清空layput---
            LinearLayout lay01 = (LinearLayout)findViewById(R.id.f0100_lay001);
            lay01.removeAllViews();

            //======================巨集=========================
            try {
                for (int i = 1; i <=recSet_F0100.size(); i++){
                    // ---產生新的TextView---
                    TextView tv01 = new TextView(this);
                    TextView tv02 = new TextView(this);
                    TextView tv03 = new TextView(this);
                    TableRow tab01 = new TableRow(this);
                    TableRow tab02 = new TableRow(this);
                    CircleImgView img01 = new CircleImgView(this);
                    LinearLayout lay02 = new LinearLayout(this);
                    ImageButton img02 = new ImageButton(this);
                    // ---設定新TextView的ID---
                    tv01.setId(i);
                    tv02.setId(i);
                    tv03.setId(i);
                    tab01.setId(i);
                    tab02.setId(i);
                    img01.setId(i);
                    lay02.setId(i);
                    img02.setId(i);
                    //---擷取SQLite資料---
                    String[] f0100 = recSet_F0100.get(i-1).split("#");
                    String id00 = f0100[0];//---ID
                    String id01 = f0100[1];//---Email
                    String id02 = f0100[2];//---FirstName
                    String id03 = f0100[3];//---LastName
                    //------------------網址轉圖片--------------------
                    String id04 = f0100[4];//---UesrImage
                    //-----------------------------------------------
                    String id05 = f0100[5];//---Message
                    //------------------時區轉換--------------------
                    String timeStr = f0100[6]; // 主機時間
                    SimpleDateFormat timeZone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timeZone.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date date = timeZone.parse(timeStr);  // 將字符串時間按時間解析成Date對象

                    SimpleDateFormat timeZone_TW = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    timeZone_TW.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                    String date_TW = timeZone_TW.format(date);
                    //-----------------------------------------------

                    //---設定TextView屬性
                    tv01.setText(id05);
                    if (ACC != null){
                        String user_email = ACC.getEmail();
                        if(user_email.equals(id01)){
                            tv01.setTextColor(getColor(R.color.teal));
                        }else{
                            tv01.setTextColor(objt001.getCurrentTextColor());
                        }
                    }else {
                        tv01.setTextColor(objt001.getCurrentTextColor());
                    }
                    tv01.setGravity(objt001.getGravity());
                    tv01.setTextSize(TypedValue.COMPLEX_UNIT_PX, objt001.getTextSize());
                    tv01.setBackground(objt001.getBackground());
                    tv01.setLayoutParams(objt001.getLayoutParams());
                    //---
                    tv02.setText(id02);
                    tv02.setTextColor(objt002.getCurrentTextColor());
                    tv02.setGravity(objt002.getGravity());
                    tv02.setTextSize(TypedValue.COMPLEX_UNIT_PX, objt002.getTextSize());
                    tv02.setBackground(objt002.getBackground());
                    tv02.setLayoutParams(objt002.getLayoutParams());
                    //---
                    tv03.setText(date_TW+" 留言");
                    tv03.setTextColor(objt003.getCurrentTextColor());
                    tv03.setGravity(objt003.getGravity());
                    tv03.setTextSize(TypedValue.COMPLEX_UNIT_PX, objt003.getTextSize());
                    tv03.setBackground(objt003.getBackground());
                    tv03.setLayoutParams(objt003.getLayoutParams());
                    //---
                    tab01.setLayoutParams(objt004.getLayoutParams());
                    tab02.setLayoutParams(objt005.getLayoutParams());
                    //---
                    img01.setLayoutParams(objt006.getLayoutParams());
                    img01.setImageResource(R.drawable.icon_people);
                    //---
                    lay02.setLayoutParams(objt007.getLayoutParams());
                    //---
                    img02.setBackground(objt008.getBackground());
                    img02.setLayoutParams(objt008.getLayoutParams());
                    //---
                    lay01.addView(tab01);
                    lay01.addView(tab02);
                    tab01.addView(lay02);
                    tab01.addView(tv01);
                    tab02.addView(tv02);
                    tab02.addView(img02);
                    tab02.addView(tv03);
                    lay02.addView(img01);

                    img02.setOnClickListener(delOn);

                    switch (view01){
                        case 0://------------------------------取消編輯、預設值
                            img02.setVisibility(View.GONE);
                            img004.setVisibility(View.VISIBLE);
                            b001.setVisibility(View.VISIBLE);
                            e001.setVisibility(View.VISIBLE);
                            break;
                        case 1://------------------------------使用編輯
                            img02.setVisibility(View.VISIBLE);
                            img004.setVisibility(View.GONE);
                            b001.setVisibility(View.GONE);
                            e001.setVisibility(View.GONE);
                            break;
                    }
                }
            }catch (Exception e){
                return;
            }
        }else{
            Toast.makeText(getApplicationContext(), "無資料", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f0100_b001:
                if (ACC != null){//---是否為已登入
                    e_message = e001.getText().toString().trim();//---取得留言
                    if (e_message.equals("")){//---檢查輸入欄位是否空白
                        Toast.makeText(getApplicationContext(), "留言不可空白 !", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //-----取出會員資料-----
                    e_email =  ACC.getEmail();//---Email
                    e_firstname = ACC.getGivenName();//---Firstname
                    e_lastname = ACC.getFamilyName();//---Last name
                    e_userimage = ACC.getPhotoUrl().toString().trim();//---頭像
                    //--------執行SQL-------------
                    long rowID = dbHper.insertRec_F0100(e_email, e_firstname, e_lastname, e_userimage, e_message);
                    //--------執行MySQL-----------
                    mysql_insert_F0100();

                    if (rowID != -1) {//---寫入是否成功
                        msg = "新增成功 !";
                        e001.setText("");
                    } else {
                        msg = "新增失敗 !";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();//---顯示msg
                    //-------------------------------------
                    dbmysql_F0100();//---抓取MySQL，寫入SQLite
                    recSet_F0100 = dbHper.getRecSet_F0100();//---讀取SQLite
                    showRec();//---渲染畫面
                    last_scroll();//---ScrollView顯示最下面
                    //-------------------------------------
                }else {
                    new AlertDialog.Builder(F0100.this)
                            .setTitle(getString(R.string.f0100_dialog_title))
                            .setMessage(getString(R.string.f0100_dialog_message))
                            .setCancelable(false)
                            .setIcon(android.R.drawable.btn_radio)
                            .setPositiveButton(getString(R.string.f0100_dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                    //Toast.makeText(getApplicationContext(), "請登入會員 !", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.f0100_img004:
                last_scroll();
                break;
        }
    }

    //-----scroll last
    private void last_scroll() {
        ScrollView scroll_v = (ScrollView)findViewById(R.id.scrollView);
        scroll_v.post(new Runnable() {
            @Override
            public void run() {
                scroll_v.scrollTo(0,10000);
            }
        });
    }

    //-------刪除對話盒
    private View.OnClickListener delOn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int iii = (v.getId());//---取得Button id
            f0100 = recSet_F0100.get(iii-1).split("#");//---取得該筆資料

            MyAlertDialog myAltDlg = new MyAlertDialog(F0100.this);
            myAltDlg.getWindow().setBackgroundDrawableResource(R.color.Yellow);
            myAltDlg.setTitle("刪除留言");
            myAltDlg.setMessage("留言刪除無法復原\n確定要刪除嗎?");
            myAltDlg.setCancelable(false);
            myAltDlg.setIcon(android.R.drawable.ic_delete);
            myAltDlg.setButton(BUTTON_POSITIVE, "確定刪除", del_choose);
            myAltDlg.setButton(BUTTON_NEGATIVE, "取消刪除", del_choose);
            myAltDlg.show();
        }
    };

    //---------是否刪除
    private  DialogInterface.OnClickListener del_choose = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case BUTTON_POSITIVE://---確定刪除
                    tid = f0100[0];//-----取得該筆資料id
                    // ----------執行刪除----------
                    int rowsAffected = (int) dbHper.deleteRec_F0100(tid);//---執行SQLite刪除
                    mysql_del_F0100();//---執行MySQL刪除
                    dbmysql_F0100();//---更新SQLite
                    //----------是否刪除成功--------
                    if (rowsAffected == -1) {
                        msg = "留言不存在, 無法刪除 !";
                    } else if (rowsAffected == 0) {
                        msg = "留言不存在, 無法刪除 !";
                    } else {
                        msg = "留言已刪除 !";
                        //-------------渲染畫面-------------
                        recSet_F0100 = dbHper.getRecSet_F0100();//---讀取SQLite
                        view01 = 0;//---設預設值
                        showRec();//---渲染畫面
                        handler.post(updateTimer);//---開啟執行續
                        runAuto_flag = true;
                        //--------Menu群組-----------
                        menu.setGroupVisible(R.id.f0100_group01, true);
                        menu.setGroupVisible(R.id.f0100_group02, false);
                    }
                    Toast.makeText(F0100.this, msg, Toast.LENGTH_SHORT).show();
                    break;
                case BUTTON_NEGATIVE:
                    msg = "放棄刪除 !";
                    Toast.makeText(F0100.this, msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void dbmysql_F0100() {
        String sqlctl = "SELECT * FROM F0100";
        ArrayList<String> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(sqlctl);
        try {
            String result = DBConnector.executeQuery_F0100(nameValuePairs);

            JSONArray jsonArray = new JSONArray(result);
            // ------
            if (jsonArray.length() > 0) { // ------------------------------MySQL 連結成功有資料
                int rowsAffected = dbHper.clearRec_F0100();// --------匯入前,刪除所有SQLite資料
                // 處理JASON 傳回來的每筆資料
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    ContentValues newRow = new ContentValues();
                    // --(1) 自動取的欄位 --取出 jsonObject 每個欄位("key","value")
                    Iterator itt = jsonData.keys();
                    while (itt.hasNext()) {
                        String key = itt.next().toString();
                        String value = jsonData.getString(key); // 取出欄位的值
                        if (value == null) {
                            continue;
                        } else if ("".equals(value.trim())) {
                            continue;
                        } else {
                            jsonData.put(key, value.trim());
                        }
                        // ------------------------------------------------------------------
                        newRow.put(key, value.toString()); // 動態找出有幾個欄位
                        // -------------------------------------------------------------------
                    }
                    // ---(2) 使用固定已知欄位---------------------------
                    // newRow.put("id", jsonData.getString("id").toString());
                    // newRow.put("name",
                    // jsonData.getString("name").toString());
                    // newRow.put("grp", jsonData.getString("grp").toString());
                    // newRow.put("address", jsonData.getString("address")
                    // -------------------加入SQLite----------------------
                    long rowID = dbHper.insertRec_m_F0100(newRow);

                }
                //Toast.makeText(getApplicationContext(), "共匯入 " + Integer.toString(jsonArray.length()) + " 筆資料", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void dbmysql_F0101() {
        String sqlctl = "SELECT * FROM F0101";
        ArrayList<String> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(sqlctl);
        try {
            String result = DBConnector.executeQuery_F0100(nameValuePairs);

            JSONArray jsonArray = new JSONArray(result);
            // ------
            if (jsonArray.length() > 0) { // ------------------------------MySQL 連結成功有資料
                int rowsAffected = dbHper.clearRec_F0101();// --------匯入前,刪除所有SQLite資料
                // 處理JASON 傳回來的每筆資料
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    ContentValues newRow = new ContentValues();
                    // --(1) 自動取的欄位 --取出 jsonObject 每個欄位("key","value")
                    Iterator itt = jsonData.keys();
                    while (itt.hasNext()) {
                        String key = itt.next().toString();
                        String value = jsonData.getString(key); // 取出欄位的值
                        if (value == null) {
                            continue;
                        } else if ("".equals(value.trim())) {
                            continue;
                        } else {
                            jsonData.put(key, value.trim());
                        }
                        // ------------------------------------------------------------------
                        newRow.put(key, value.toString()); // 動態找出有幾個欄位
                        // -------------------------------------------------------------------
                    }
                    // ---(2) 使用固定已知欄位---------------------------
                    // newRow.put("id", jsonData.getString("id").toString());
                    // newRow.put("name",
                    // jsonData.getString("name").toString());
                    // newRow.put("grp", jsonData.getString("grp").toString());
                    // newRow.put("address", jsonData.getString("address")
                    // -------------------加入SQLite----------------------
                    long rowID = dbHper.insertRec_m_F0101(newRow);

                }
                //Toast.makeText(getApplicationContext(), "共匯入 " + Integer.toString(jsonArray.length()) + " 筆資料", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void mysql_insert_F0100() {
        //        sqlctl = "SELECT * FROM member ORDER BY id ASC";
        ArrayList<String> nameValuePairs = new ArrayList<>();
        //        nameValuePairs.add(sqlctl);
        nameValuePairs.add(e_email);
        nameValuePairs.add(e_firstname);
        nameValuePairs.add(e_lastname);
        nameValuePairs.add(e_userimage);
        nameValuePairs.add(e_message);

        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //-----------------------------------------------
        String result = DBConnector.executeInsert_F0100(nameValuePairs);  //真正執行新增
        //-----------------------------------------------
    }

    private void mysql_del_F0100() {
        s_id = tid;
        ArrayList<String> nameValuePairs = new ArrayList<>();
        //nameValuePairs.add(sqlctl);
        nameValuePairs.add(s_id);
        try {
            Thread.sleep(100); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //-----------------------------------------------
        String result = DBConnector.executeDelet_F0100(nameValuePairs);   //執行刪除
        //-----------------------------------------------
    }

    public static Bitmap getBitmapFromURL(String imageUrl) {
        try{
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }  catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //===========================生命週期==========================
    @Override
    protected void onResume() {
        super.onResume();
        if (view01 == 0){
            handler.post(updateTimer);//---開啟執行續
            runAuto_flag = true;
        }else{
            recSet_F0100 = dbHper.getRecSet_F0100();
            view01 = 0;
            showRec();
            handler.post(updateTimer);//---開啟執行續
            runAuto_flag = true;
            menu.setGroupVisible(R.id.f0100_group01, true);
            menu.setGroupVisible(R.id.f0100_group02, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        check_SignIn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTimer);//---關閉執行續
        runAuto_flag = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateTimer);//---關閉執行續
        runAuto_flag = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimer);//---關閉執行續
        runAuto_flag = false;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();  禁用返回鍵
    }

    //===========================Menu==========================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.f0100, menu);
        this.menu = menu;
        menu.setGroupVisible(R.id.f0100_group01, true);
        menu.setGroupVisible(R.id.f0100_group02, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.f0100_login:
                intent.putExtra("class_title",getString(R.string.f0100_login));
                intent.setClass( F0100.this, F0200.class);
                startActivity(intent);
                break;
            case R.id.f0100_answer:
                //---有資料才執行---
                if (recSet_F0101.size() != 0){
                    //---擷取SQLite資料---
                    String[] f0101 = recSet_F0101.get(0).split("#");
                    String yesterday = f0101[3];
                    String title = f0101[4];
                    String answer = f0101[5];

                    new AlertDialog.Builder(F0100.this)
                            .setTitle(yesterday)
                            .setMessage("題目："+title + "\n\n" + answer)
                            .setCancelable(false)
                            .setIcon(android.R.drawable.btn_radio)
                            .setPositiveButton(getString(R.string.f0100_menu_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }else{
                    Toast.makeText(getApplicationContext(), "連線異常，請稍後再試。", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.f0100_delete:
                if (ACC != null){

                    if (runAuto_flag == true){
                        handler.removeCallbacks(updateTimer);//-----關閉執行續
                        runAuto_flag = false;
                    }
                    //---------------------
                    String user = ACC.getEmail();
                    msg = null;
                    recSet_F0100 = dbHper.getRecSet_user_F0100(user);
                    Toast.makeText(getApplicationContext(), "共 " + recSet_F0100.size()
                            + " 筆留言", Toast.LENGTH_SHORT).show();
                    //---------------------
                    view01 = 1;
                    showRec();
                    menu.setGroupVisible(R.id.f0100_group01, false);
                    menu.setGroupVisible(R.id.f0100_group02, true);
                }else {
                    new AlertDialog.Builder(F0100.this)
                            .setTitle(getString(R.string.f0100_dialog_title))
                            .setMessage(getString(R.string.f0100_dialog_message))
                            .setCancelable(false)
                            .setIcon(android.R.drawable.btn_radio)
                            .setPositiveButton(getString(R.string.f0100_dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
                break;
            case R.id.f0100_no_delete:
                recSet_F0100 = dbHper.getRecSet_F0100();
                view01 = 0;
                showRec();
                handler.post(updateTimer);//---開啟執行續
                runAuto_flag = true;
                menu.setGroupVisible(R.id.f0100_group01, true);
                menu.setGroupVisible(R.id.f0100_group02, false);
                break;
            case R.id.f0100_menu_about:
                new AlertDialog.Builder(F0100.this)
                        .setTitle(getString(R.string.f0100_menu_about))
                        .setMessage(getString(R.string.f0100_menu_message))
                        .setCancelable(false)
                        .setIcon(android.R.drawable.btn_radio)
                        .setPositiveButton(getString(R.string.f0100_menu_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                break;
            case R.id.f0100_action_settings:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}