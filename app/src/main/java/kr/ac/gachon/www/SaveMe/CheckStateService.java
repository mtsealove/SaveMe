package kr.ac.gachon.www.SaveMe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import kr.ac.gachon.www.SaveMe.Entity.XYZ;

public class CheckStateService extends Service {    //백그라운드에서 작동(서비스)
    private final int HUMAN_FALL_DOWN=1, CELLPHONE_DROP=2;  //사람이 넘어졌을 때, 핸드폰이 떨어졌을 떄
    private SensorManager sensorManager=null;
    private SensorEventListener sensorEventListener;
    private Sensor sensor=null;
    private Notification notification;
    ArrayList<XYZ> xyzArrayList;    //좌표의 값을 가질 리스트
    private int sensitivity;
    @Override
    public IBinder onBind(Intent intent){
        return  null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensitivity=getSensitivity();
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener=new AccelListener();    //움직임 판단 리스너
        xyzArrayList=new ArrayList<>(); //계속 움직임을 감지할 리스트
        Toast.makeText(getApplicationContext(), "실시간 보호가 시작되었습니다", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //서비스가 시작될 때
        sensorManager.registerListener(sensorEventListener,sensor, SensorManager.SENSOR_DELAY_UI);
        if(Build.VERSION.SDK_INT>26)
            CreateNotificationHigh();   //노티 만들기
        else CreateNotificationLow();

        return  super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {   //종료되었을 때
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "실시간 보호가 중지되었습니다", Toast.LENGTH_SHORT).show();

        sensorManager.unregisterListener(sensorEventListener);  //리스너 해제
    }

    private class AccelListener implements  SensorEventListener {   //움직임 감지 리스너
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double x=sensorEvent.values[0];
            double y=sensorEvent.values[1];
            double z=sensorEvent.values[2];

            xyzArrayList.add(new XYZ(x, y, z));
            if(xyzArrayList.size()>2) { //만약 움직임이 감지되었다면
                System.out.println("차이값: "+xyzArrayList.get(0).getDiff(xyzArrayList.get(1)));
                if(xyzArrayList.get(0).getDiff(xyzArrayList.get(1))>sensitivity) {//두 이동 기록의 차이를 대조하여 떨어지거나 넘어짐이 감지되면// 임의로 민감도 30 설정
                    stopSelf(); //서비스 종료
                    Intent intent=new Intent(getApplicationContext(), EmergencyActivity.class); //긴급상황 액티비티로 이동하는 인텐트
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);  //액티비티 실행
                }
            }
            if(xyzArrayList.size()>2)
                xyzArrayList.remove(0); //메모리 소모를 막기 위해 계속 크기를 유지해줌
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)   //안드로이드 8.0이상에서 작동
    public void CreateNotificationHigh() {  //알림 만들기
        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);    //노티(알림) 매니저
        String channelId="notify";  //채널 ID
        CharSequence channelName="알림";  //채널 이름
        String description="일반 알림입니다";  //채널 설명
        int importance=NotificationManager.IMPORTANCE_HIGH; //중요도 높음
        //안드로이드 8.0 이상부터는 알림 채널을 설정해야 알림 표시가 가능하다
        NotificationChannel notificationChannel=new NotificationChannel(channelId, channelName, importance);    //알림 채널
        notificationChannel.setDescription(description);    //채널 설명 설정
        notificationManager.createNotificationChannel(notificationChannel); //채널 생성

        Intent intent=new Intent(CheckStateService.this, MainActivity.class);   //메인 액티비티로 가는 인텐트(화면 전환 객체)
        PendingIntent pendingIntent=PendingIntent.getActivity(CheckStateService.this,1, intent, PendingIntent.FLAG_UPDATE_CURRENT); //펜딩 인텐트, 알림을 클릭하면 위의 인텐트 실행

        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(CheckStateService.this)   //알림 만들기
                .setContentTitle("구해줘") //제목
                .setContentText("구해줘가 실행중입니다")  //내용
                .setChannelId("notify") //채널 ID를 기반으로 위에서 만든 채널을 할당
                .setSmallIcon(R.drawable.siren) //아이콘 설정
                .setContentIntent(pendingIntent)    //클릭시 메인으로 가는 인텐트 설정
                .setOngoing(true);  //상단바에 띄우기
        notification=notificationBuilder.build();   //실제 노티 빌드
        startForeground(001, notification); //항상 실행
    }

    public void CreateNotificationLow() {   //안드로이드 8.0 미만에서 작동
        Intent intent=new Intent(CheckStateService.this, MainActivity.class);   //메인 액티비티로 가는 인텐트(화면 전환 객체)
        PendingIntent pendingIntent=PendingIntent.getActivity(CheckStateService.this,1, intent, PendingIntent.FLAG_UPDATE_CURRENT); //펜딩 인텐트, 알림을 클릭하면 위의 인텐트 실행

        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(CheckStateService.this)   //알림 만들기
                .setContentTitle("구해줘") //제목
                .setContentText("구해줘가 실행중입니다")  //내용
                .setSmallIcon(R.drawable.siren) //아이콘 설정
                .setContentIntent(pendingIntent)    //클릭시 메인으로 가는 인텐트 설정
                .setOngoing(true);  //상단바에 띄우기
        notification=notificationBuilder.build();   //실제 노티 빌드
        startForeground(001, notification); //항상 실행
    }

    private int getSensitivity(){   //민감도 구하기
        final int VeryHigh=40, High=60, Mid=80, Low=100, VeryLow=120;
        int sensitivity=Mid;
        File sensitivityFile=new File(getFilesDir()+"sensitivity.dat");
        try {
            BufferedReader br=new BufferedReader(new FileReader(sensitivityFile));
            int tmp=Integer.parseInt(br.readLine());
            switch (tmp) {
                case 0: sensitivity=VeryLow;
                break;
                case 1: sensitivity=Low;
                break;
                case 2: sensitivity=Mid;
                break;
                case 3: sensitivity=High;
                break;
                case 4: sensitivity=VeryHigh;
                break;
            }
        } catch (Exception e) {
            sensitivity=Mid;
            e.printStackTrace();
        } finally {
            return sensitivity;
        }
    }
}
