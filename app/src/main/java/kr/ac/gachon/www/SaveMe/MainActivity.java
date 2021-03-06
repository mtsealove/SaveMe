package kr.ac.gachon.www.SaveMe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
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
    private String PhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent getIntent=getIntent();
        PhoneNumber=getIntent.getStringExtra("PhoneNumber");

        //뷰 매칭
        locationTv = findViewById(R.id.locationTv);
        addressTv=findViewById(R.id.addressTv);
        StartStopBtn = findViewById(R.id.start_stopBtn);
        settingBtn = findViewById(R.id.settingBtn);

        //시작버튼 누르면
        StartStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStart();
            }
        });

        setLocationManager();

        toggleBtnText();
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleBtnText();
    }

    private void toggleBtnText() {  //버튼
        if(isServiceRunning()) StartStopBtn.setText("Stop");    //서비스가 동작중이면 Stop 출력
        else StartStopBtn.setText("Start"); //동작중이지 않으면 Start 출력
    }

    @SuppressLint("MissingPermission")
    private void setLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //위치정보를 이용할 로케이션 매니저
        //위치 얻기

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                getLatLon(location);    //위치에서 위경도를 얻음
                setMainList();  //메인 화면에 출력될 리스트
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
    }

    //현재 상태와 위치 설정
    private void setMainList() {
        locationTv.setText("현위치:\n위도: "+latitude+"\n경도: "+longitude);   //위경도 출력
        //address.replace("대한민국", "");   //어차피 한국이니 대한민국은 제거한다.
        addressTv.setText("주소: "+address);  //주소 출력
    }

    public void goSetting(View v) { //설정 액티비티로 이동
        Intent intent=new Intent(MainActivity.this, SettingActivity.class); //설정으로 가는 인텐트
        intent.putExtra("PhoneNumber", PhoneNumber);
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



    private void getLatLon(Location location){  //위경도 및 주소 설정
        if(location!=null) {
            longitude = location.getLongitude();  //위치에서 경도
            latitude = location.getLatitude();    //위도를 얻어오고
            Geocoder geocoder = new Geocoder(this);   //지오코딩을 위한 객체를 만들어서
            try {
                List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1); //위경도를 기반으로 주소 따고
                address = addressList.get(0).getAddressLine(0);   //주소 라인만 따온다(원래 세세한 정보가 많아요)
            } catch (IOException e) {
                e.printStackTrace();
            }
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
