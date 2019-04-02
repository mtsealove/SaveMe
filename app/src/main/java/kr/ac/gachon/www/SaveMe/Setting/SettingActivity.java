package kr.ac.gachon.www.SaveMe.Setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kr.ac.gachon.www.SaveMe.HelpActivity;
import kr.ac.gachon.www.SaveMe.R;
import kr.ac.gachon.www.SaveMe.SignUp.TermsActivity;

public class SettingActivity extends AppCompatActivity {
    //TextView saveTV;
    ListView topLV, bottomLV;
    Switch emergencySW;
    File sirenFile;
    private String PhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sirenFile=new File(getFilesDir()+"Siren.dat");

        //뷰 매칭
        topLV=findViewById(R.id.SettingTopLV);
        bottomLV=findViewById(R.id.bottomLV);
        emergencySW=findViewById(R.id.emergencySW);

        PhoneNumber=getPhoneNumber();
        init();
    }
    private void init() {   //초기화
        ArrayList<String> top=new ArrayList<>(); //사이렌 상단 레이아웃
        ArrayList<String> bottom=new ArrayList<>(); //사이렌 하단 레이아웃
        top.add("메세지");
        top.add("TTS");
        top.add("민감도 설정");
        bottom.add("사용법");
        bottom.add("회원정보 수정");
        bottom.add("회원탈퇴");

        //Adapter 객체를 통해 화면에 표시
        //비상벨 상단 리스트
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
                    case 2:
                        Sensitivity_Setting();
                        break;
                }
            }
        });

        //비상벨 하단 리스트
        bottomLV.setAdapter(new ArrayAdapter<>(SettingActivity.this, R.layout.main_drop_down, bottom));
        bottomLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0: //도움말을 누르면
                        Help();
                        break;
                    case 2: //회원탈퇴를 누르면
                        CreateSignOutDialog();
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
        intent.putExtra("PhoneNumber", PhoneNumber);
        startActivity(intent);
    }
    private void Help() {   //도움말 액티비티로 이동
        Intent intent=new Intent(SettingActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    private void Sensitivity_Setting() {    //민감도 설정 액티비티로 이동
        Intent intent=new Intent(SettingActivity.this, SensitivitySettingActivity.class);
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

    private void CreateSignOutDialog() {    //회원탈퇴 다이얼로그 생성
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("회원 탈퇴")   //제목
                .setMessage("탈퇴하시겠습니까?")    //메세지
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {   //취소
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).setPositiveButton("네", new DialogInterface.OnClickListener() {   //확인
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SignOut();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void SignOut() {    //데이터베이스에서 삭제
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Members").child(PhoneNumber);
        reference.setValue(null);   //모든 정보 삭제
        Toast.makeText(this, "회원탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show();   //안내 메시지
        Intent intent=new Intent(SettingActivity.this, TermsActivity.class);
        startActivity(intent);  //약관 화면으로 이동
        finish();
    }

    @SuppressLint("MissingPermission")
    private String getPhoneNumber(){    //전화번호 읽어오기
        String Number = null;   //
        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try{
            Number=mgr.getLine1Number();
            Number = Number.replace("+82", "0");
            System.out.println("전화번호: "+Number);
            return Number;
        }catch(Exception e){
            return null;
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
