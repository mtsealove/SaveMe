package kr.ac.gachon.www.SaveMe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kr.ac.gachon.www.SaveMe.Entity.TTS;


public class EmergencyActivity extends AppCompatActivity {
    TextView locationTv, TimeTv;
    private String address=null;
    private double latitude=0, longitude=0;
    LocationManager locationManager;    //위치에 접근
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    private int counter=0;
    File sirenFile;
    private TextToSpeech textToSpeech;

    Timer timer;
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        sirenFile=new File(getFilesDir()+"Siren.dat");  //사이렌 활성 여부를 읽을 파일

        //뷰 매칭
        locationTv=findViewById(R.id.locationTv);
        TimeTv=findViewById(R.id.timeTV);

        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);    //지도를 얻기 위한 객체
        getLatLon(location);    //위경도 얻음


        locationTv.setText("현주소:\n"+address+"\n위도: "+latitude+"\n경도: "+longitude);  //현재 주소와 위경도 출력

        powerManager=(PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock=powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE, "WAKELOCK");

        wakeLock.acquire(); //화면 깨우기

        if(getSiren()) Siren();
        //시간 경과
        timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                counter++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //안드로이드에 UI스레드가 있고 타이머와는 따로 실행해야 됨
                        int maxTime=60; //최대 시간
                        TimeTv.setText(60-counter+"초 내로 상황을 종료하지 않을 경우\n메세지가 전송됩니다");  //화면 업데이트
                        if(counter==maxTime) {  //시간 초과
                            timer.cancel(); //타이머 종료
                            TimeTv.setText("메세지가 전송되었습니다");
                            ReadNSendSMS(); //메세지 읽고 보내기
                        }
                    }
                });
            }
        };

        timer.schedule(timerTask, 0, 1000); //1초마다 실행

        TTS tts=getTTS();   //TTS의 볼륨과 내용을 읽음
        if(tts!=null) makeTTS(tts); //정상적으로 읽어 왔다면 TTS출력
    }

    private void getLatLon(Location location){  //위경도 및 주소 설정
        longitude=location.getLongitude();  //위치에서 경도를 얻음
        latitude=location.getLatitude();    //위치에서 위도를 얻음
        Geocoder geocoder=new Geocoder(this);   //지오코딩을 위한 객체
        try {
            List<Address> addressList=geocoder.getFromLocation(latitude, longitude, 1); //위경도를 넣어 하나의 주소를 갖는 리스트 생성
            address=addressList.get(0).getAddressLine(0);   //하나의 주소를 얻음
            address=address.replace("대한민국 ", "");   //주소에서 대한민국 제거(어차피 한국이니까)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void EmergencyFinish(View v) {   //액티비티 종료
        //메인 화면으로 이동
        //Intent intent=new Intent(EmergencyActivity.this, MainActivity.class);
        //startActivity(intent);
        timer.cancel();
        if(mediaPlayer!=null)
        mediaPlayer.release();
        finish();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(EmergencyActivity.this, "상황을 종료하려면\n'상황해제' 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
    }

    File phoneFile;
    File messageFile;
    private void ReadNSendSMS() {    //메세지 보내기
        ArrayList<String> phones=new ArrayList<>();
        String message="";
        try {   //전화번호 읽기
            phoneFile=new File(getFilesDir()+"phones.dat");
            BufferedReader br=new BufferedReader(new FileReader(phoneFile));
            String tmp;
            while((tmp=br.readLine())!=null) {
                phones.add(tmp);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("전화번호 읽기 실패");
            e.printStackTrace();
        }
        try {   //메시지 읽기
            messageFile=new File(getFilesDir()+"message.dat");
            BufferedReader br=new BufferedReader(new FileReader(messageFile));
            String tmp;
            while((tmp=br.readLine())!=null) {
                message+=tmp+"\n";
            }

            message=message.substring(0, message.length()-1);
            message="주소: "+address+"\n" //메세지 내용
                    +message;
        }catch (Exception e) {
            System.out.println("메세지 읽기 실패");
            e.printStackTrace();
        }
        for(int i=0; i<phones.size(); i++) {    //저장된 전화번호 개수만큼
            try {
                System.out.println("전화번호" + phones.get(i));
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phones.get(i).replace("-", ""), null, message, null, null);  //메세지 보내기
            } catch (Exception e) { //오류
                System.out.println("메세지 전송 실패");
                e.printStackTrace();
            }
        }

    }

    MediaPlayer mediaPlayer;    //비상벨을 재생하기 위한 미디어 플레이어
    public void Siren() {   //비상벨
        mediaPlayer=MediaPlayer.create(EmergencyActivity.this, R.raw.warning_siren);    //소리 파일을 넣어 객체로 생성
        mediaPlayer.setLooping(true);   //반복재생
        mediaPlayer.setVolume(1, 1);    //볼륨 최대로
        AudioManager am= (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 100);
        am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        am.getStreamVolume(AudioManager.STREAM_MUSIC);  //모두 최대로
        mediaPlayer.start();    //재생
    }

    private boolean getSiren() {    //사이렌 활성 여부 읽음
        try {
            BufferedReader br=new BufferedReader(new FileReader(sirenFile));
            if(Boolean.parseBoolean(br.readLine())) return true;
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private TTS getTTS() {  //TTS객체 반환
        TTS result=null;
        File enableFile=new File(getFilesDir()+"TTSenable.dat");
        boolean enable=false;
        try {   //활성화 됬는지 읽기
            BufferedReader br=new BufferedReader(new FileReader(enableFile));
            enable=Boolean.parseBoolean(br.readLine());
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(enable) {    //활성화되어 있을 경우
            File volumeFile=new File(getFilesDir()+"TTSVolume.dat");
            File IndexFile=new File(getFilesDir()+"TTSList.dat");
            int volume=0, index=0;
            try {   //볼륨 읽기
                BufferedReader br=new BufferedReader(new FileReader(volumeFile));
                volume=Integer.parseInt(br.readLine());
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {   //인덱스 읽기
                BufferedReader br=new BufferedReader(new FileReader(IndexFile));
                index=Integer.parseInt(br.readLine());
                br.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
            String msg=getResources().getStringArray(R.array.TTS_content)[index];   //인덱스에 해당하는 메세지 선택
            result=new TTS(volume,msg); //객체 초기화
        }

        return result;
    }

    private void makeTTS(final TTS tts) { //TTS 출력
        textToSpeech=new TextToSpeech(EmergencyActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR) { //에러가 발생하지 않았다면
                    textToSpeech.setLanguage(Locale.KOREAN);    //한국어 설정
                    textToSpeech.setSpeechRate(0.8f);   //속도 0.8배
                    Bundle bundle=new Bundle();
                    float volume=(float)(tts.getVolume())/100;  //설정한 볼류
                    bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume);  //볼륨 설정
                    textToSpeech.speak(tts.getMsg(), TextToSpeech.QUEUE_FLUSH, bundle, "1");    //내용 읽기
                }
            }
        });

    }
}
