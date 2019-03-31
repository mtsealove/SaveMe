package kr.ac.gachon.www.SaveMe.Setting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kr.ac.gachon.www.SaveMe.HelpActivity;
import kr.ac.gachon.www.SaveMe.R;

public class SettingActivity extends AppCompatActivity {
    //TextView saveTV;
    ListView topLV, bottomLV;
    Switch emergencySW;
    File sirenFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sirenFile=new File(getFilesDir()+"Siren.dat");

        //뷰 매칭
        topLV=findViewById(R.id.SettingTopLV);
        bottomLV=findViewById(R.id.bottomLV);
        emergencySW=findViewById(R.id.emergencySW);

        init();
    }
    private void init() {   //초기화
        ArrayList<String> top=new ArrayList<>(); //사이렌 상단 레이아웃
        ArrayList<String> bottom=new ArrayList<>(); //사이렌 하단 레이아웃
        top.add("메세지");
        top.add("TTS");
        bottom.add("사용법");

        //Adapter 객체를 통해 화면에 표시
        topLV.setAdapter(new ArrayAdapter<>(SettingActivity.this, R.layout.main_drop_down, top));
        topLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0: //메세지 설정을 누르면
                        Message_Setting();
                        break;
                    case 1: //TTS 설정을 누르면
                        TTS_Setting();
                        break;
                }
            }
        });
        bottomLV.setAdapter(new ArrayAdapter<>(SettingActivity.this, R.layout.main_drop_down, bottom));
        bottomLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0: //도움말을 누르면
                        Help();
                        break;
                }
            }
        });

        if(getSiren()) emergencySW.setChecked(true);    //사이렌활성화면  사이렌 체크
        else emergencySW.setChecked(false);

        if(getTTS()) {  //TTS활성화면
            emergencySW.setClickable(false);    //사이렌 불가
            emergencySW.setChecked(false);
        }
        emergencySW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setSiren(b);
            }
        });
    }
    private void TTS_Setting() {    //TTS 설정 액티비티로 이동
        Intent intent=new Intent(SettingActivity.this, TtsSettingActivity.class);
        startActivity(intent);
    }
    private void Message_Setting() {    //메세지 설정 액티비티로 이동
        Intent intent=new Intent(SettingActivity.this, MessageSettingActivity.class);
        startActivity(intent);
    }
    private void Help() {   //도움말 액티비티로 이동
        Intent intent=new Intent(SettingActivity.this, HelpActivity.class);
        startActivity(intent);
    }


    private void setSiren(boolean activate) {   //사이렌 활성 여부 작성
        if(getTTS())
            Toast.makeText(SettingActivity.this, "TTS설정을 해지하셔야 합니다", Toast.LENGTH_SHORT).show();
        else {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(sirenFile));
                bw.write(Boolean.toString(activate));
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean getSiren() {    //사이렌 활성 여부 읽음
        try {
            boolean result=false;
            BufferedReader br=new BufferedReader(new FileReader(sirenFile));
            result=Boolean.parseBoolean(br.readLine());
            br.close();
            if(result) return true;
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean getTTS() { //TTS활성 여부 체크
        File TTSEnableFile=new File(getFilesDir()+"TTSenable.dat");
        try {
            boolean result=false;
            BufferedReader br=new BufferedReader(new FileReader(TTSEnableFile));
            result=Boolean.parseBoolean(br.readLine());
            br.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close(View v){
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

}
