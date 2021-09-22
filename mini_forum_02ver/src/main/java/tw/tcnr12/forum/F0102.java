package tw.tcnr12.forum;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class F0102 extends AppCompatActivity implements View.OnClickListener{

    private Intent intent = new Intent();
    private RelativeLayout lay01;
    private ImageView img01;
    // -----------------------------------
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.f0102);
        setupViewComponent();
        //設定隱藏標題
        getSupportActionBar().hide();
        //設定隱藏狀態
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void setupViewComponent() {
        lay01 = (RelativeLayout)findViewById(R.id.f0102__lay01);
        img01 = (ImageView)findViewById(R.id.f0102_img001);

        lay01.setOnClickListener(this);
        //---開機動畫---
        img01.setAnimation(AnimationUtils.loadAnimation(this, R.anim.q0101_anim_alpha_in_01));

        //====================設執行緒=======================
        handler.postDelayed(updateTimer, 3000);  // 設定Delay的時間
        //-------------------------
    }

    //==========================設定執行續========================
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            intent.putExtra("class_title", getString(R.string.app_name));
            intent.setClass(F0102.this, F0100.class);
            startActivity(intent);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f0102__lay01:
                //-----------------
                handler.removeCallbacks(updateTimer);
                //-----------------
                intent.putExtra("class_title", getString(R.string.app_name));
                intent.setClass(F0102.this, F0100.class);
                startActivity(intent);
                break;
        }
    }

    //===========================生命週期==========================
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();  禁用返回鍵
    }
}
