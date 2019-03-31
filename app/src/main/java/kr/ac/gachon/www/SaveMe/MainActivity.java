package kr.ac.gachon.www.SaveMe;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.ac.gachon.www.SaveMe.Setting.SettingActivity;

public class MainActivity extends AppCompatActivity {
    Button StartStopBtn, settingBtn;
    TextView locationTv, addressTv, statusTv;
    private double longitude = 0, latitude = 0;
    LocationManager locationManager;
    private String address="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //뷰 매칭
        locationTv = findViewById(R.id.locationTv);
        addressTv=findViewById(R.id.addressTv);
        statusTv=findViewById(R.id.statusTv);
        StartStopBtn = findViewById(R.id.start_stopBtn);
        settingBtn = findViewById(R.id.settingBtn);

        //시작버튼 누르면
        StartStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStart();
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //위치정보를 이용할 로케이션 매니저
        //권한들이 활성화 되어 있지 않을 경우
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getPermission();
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermission();
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            getPermission();
        }
        //권한을 얻는 다이얼로그 출력

        //위치 얻기
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        getLatLon(location);    //위치에서 위경도를 얻음
        setMainList();  //메인 화면에 출력될 리스트

        if(isServiceRunning()) StartStopBtn.setText("Stop");    //서비스가 동작중이면 Stop 출력
        else StartStopBtn.setText("Start");
    }

    //현재 상태와 위치 설정
    private void setMainList() {
        locationTv.setText("현위치:\n위도: "+latitude+"\n경도: "+longitude);   //위경도 출력
        addressTv.setText("주소: "+address);  //주소 출력
    }

    public void goSetting(View v) { //설정 액티비티로 이동
        Intent intent=new Intent(MainActivity.this, SettingActivity.class); //설정으로 가는 인텐트
        startActivity(intent);  //액티비티 이동
    }

    Intent service; //서비스를 시작하고 중지할 인텐트
    private void toggleStart() {    //서비스 토글
        String status=StartStopBtn.getText().toString();    //현재 상태 얻기
        if(status.equals("Start")){//서비스가 시작하지 않았으면
            StartStopBtn.setText("Stop");   //종료출력
            service=new Intent(getApplicationContext(), CheckStateService.class);   //인텐트에 서비스를 넣어줌
            startService(service);  //서비스 시작
        }
        else { //서비스가 시작했으면
            StartStopBtn.setText("Start");  //시작 출력
            service=new Intent(getApplicationContext(), CheckStateService.class);   //서비스 얻어와서
            stopService(service);   //종료
        }
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
            Toast.makeText(MainActivity.this, "권한이 허용되었습니다", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {   //권한이 허용되지 않으면
            Toast.makeText(MainActivity.this, "권한이 허용되지 않았습니다", Toast.LENGTH_SHORT).show();
        }
    };

    private void getLatLon(Location location){  //위경도 및 주소 설정
        longitude=location.getLongitude();  //위치에서 경도
        latitude=location.getLatitude();    //위도를 얻어오고
        Geocoder geocoder=new Geocoder(this);   //지오코딩을 위한 객체를 만들어서
        try {
            List<Address> addressList=geocoder.getFromLocation(latitude, longitude, 1); //위경도를 기반으로 주소 따고
            address=addressList.get(0).getAddressLine(0);   //주소 라인만 따온다(원래 세세한 정보가 많아요)
            address.replace("대한민국 ", "");   //어차피 한국이니 대한민국은 제거한다.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //서비스 실행중인지 확인
    public boolean isServiceRunning() {
        ActivityManager activityManager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(CheckStateService.class.getName().equals(serviceInfo.service.getClassName())) return true;
        }
        return  false;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
