package kr.ac.gachon.www.SaveMe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import kr.ac.gachon.www.SaveMe.SignUp.TermsActivity;

public class LoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        FirebaseApp.initializeApp(this);
        //권한들이 활성화 되어 있지 않을 경우
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getPermission();
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermission();
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            getPermission();
        } else ReadPhoneNumber();
        //권한을 얻는 다이얼로그 출력


    }

    private void getPermission() {  //퍼미션 얻기
        //TedPeramission은 권한을 쉽게 얻기 위한 라이브러리
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("위치 정보를 이용하려면 권한을 수락해야 됩니다")      //안내 메세지
                .setDeniedMessage("[설정]>[권한]에서 권한을 허용할 수 있습니다") //거부 메세지
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE)   //허가받을 권한들
                .check();
    }

    PermissionListener permissionListener=new PermissionListener() {
        @Override
        public void onPermissionGranted() { //권한이 허용되면
            Toast.makeText(LoadActivity.this, "권한이 허용되었습니다", Toast.LENGTH_SHORT).show();
            ReadPhoneNumber();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {   //권한이 허용되지 않으면
            Toast.makeText(LoadActivity.this, "권한이 허용되지 않았습니다\n잠시 후 애플리케이션이 종료됩니다", Toast.LENGTH_LONG).show();
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {    //1초 뒤에 프로그램 종료
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 1000);
        }
    };

    private void MoveMain(String number) {   //홈화면 이동 메서드
                Intent intent=new Intent(LoadActivity.this, MainActivity.class);
                intent.putExtra("PhoneNumber", number);
                startActivity(intent);
                finish();
    }

    @SuppressLint("MissingPermission")
    private void ReadPhoneNumber() {
        String Number = null;
        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try{
            Number = mgr.getLine1Number();
            Number = Number.replace("+82", "0");
            System.out.println("전화번호: "+Number);
        }catch(Exception e){}

        final String myNumber=Number;

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference().child("Members");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(myNumber).exists()) { //현재 계정이 존재하면
                    MoveMain(myNumber);
                } else {    //계정이 존재하지 않으면
                    MoveSignUp(myNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void MoveSignUp(String number) {
        Intent intent=new Intent(LoadActivity.this, TermsActivity.class);
        startActivity(intent);
        intent.putExtra("PhoneNumber", number);
        finish();
    }

}
