package kr.ac.gachon.www.SaveMe.Setting;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kr.ac.gachon.www.SaveMe.R;

public class TtsSettingActivity extends AppCompatActivity { //TTS 설정 액티비티
    TextView saveTV;
    SeekBar volumeSB;
    ListView TTS_List;
    CheckBox TTS_CB;
    int volume, checkedList;
    File volumeFile, enableFile, checkedListFile;

    final int err=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_setting);

        volumeFile=new File(getFilesDir()+"TTSVolume.dat"); //볼륨 파일 초기화
        enableFile=new File(getFilesDir()+"TTSenable.dat"); //온오프 파일 초기화
        checkedListFile=new File(getFilesDir()+"TTSList.dat");
        volumeSB=findViewById(R.id.volumeSB);   //볼륨 Seekbar
        TTS_List=findViewById(R.id.TTS_List);   //TTS 목록
        TTS_CB=findViewById(R.id.ttsCB);    //TTS 활성설정 체크박스
        saveTV=findViewById(R.id.saveTV);   //저장 버튼

        //좀 예쁘게
        volumeSB.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
        volumeSB.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);

        initList(); //리스트 초기화


        //사이렌이 설정되어 있다면
        if(getSiren()) {    //클릭 불가 및 체크 해제
            TTS_CB.setChecked(false);
            TTS_CB.setClickable(false);
        }
        //체크 이벤트
        TTS_CB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        TTS_List.setVisibility(View.VISIBLE);   //TTS로 설정할 수 있는 리스트를 화면에 표시
                    } else TTS_List.setVisibility(View.GONE);   //감추기
            }
        });
        TTS_CB.setChecked(ReadEnable());

        //볼륨 읽기
        volume=ReadVolume();
        volumeSB.setProgress(volume);   //화면에 표시
        volumeSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { //seekbar 변경 리스너
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                volume=seekBar.getProgress();   //값이 변경되면 변수에 저장
                System.out.println("볼륨: "+volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saveTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save();
            }
        }); //저장
    }

    public void close(View v) {
        finish();
    }

    private void initList() {   //TTS종류 설정
        final ArrayList<String> TtsArrayList=new ArrayList<>();   //TTS의 종류
        String[] TTSArray=getResources().getStringArray(R.array.TTS);
        for(int i=0; i<TTSArray.length; i++) {
            TtsArrayList.add(TTSArray[i]);
        }

        if(ReadCheckedList()!=err) {    //저장되어 있는 리스트가 에러가 아니면
            checkedList=ReadCheckedList();  //
            String str=TtsArrayList.get(ReadCheckedList())+" - 선택됨";    //선택된 항목에 추가
            TtsArrayList.remove(ReadCheckedList());
            TtsArrayList.add(ReadCheckedList(), str);
            //삭제 후 교체
        }

        TTS_List.setAdapter(new ArrayAdapter<>(TtsSettingActivity.this, R.layout.main_drop_down, TtsArrayList));
        TTS_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { //항목 선택 시 발생
               for(int j=0; j<TtsArrayList.size(); j++) {
                   String newString=TtsArrayList.get(j);
                   newString=newString.replace(" - 선택됨", "");   //선택됨 모두 지우기
                   TtsArrayList.remove(j);
                   TtsArrayList.add(j, newString);
                   //교체
               }

              String selected=TtsArrayList.get(i)+" - 선택됨";//선택됨 추가
              TtsArrayList.remove(i);
              TtsArrayList.add(i, selected);
              //교체
              TTS_List.setAdapter(new ArrayAdapter(TtsSettingActivity.this, R.layout.main_drop_down, TtsArrayList));
              //리스트뷰에 표시
              checkedList=i;    //체크 리스트 설정
            }
        });
    }


    private int ReadVolume() {  //파일에서 볼륨 읽기
        try {
            BufferedReader br=new BufferedReader(new FileReader(volumeFile));
            int volume=Integer.parseInt(br.readLine()); //볼륨을 읽어서 정수형으로 변환
            br.close();
            return volume;  //값 반환
        } catch (Exception e) { //없으면 0 반환
            e.printStackTrace();
            return 0;
        }
    }

    private boolean ReadEnable() {  //온오프 파일에서 읽기
        try {
            BufferedReader br=new BufferedReader(new FileReader(enableFile));
            boolean enable=Boolean.parseBoolean(br.readLine()); //선택을 true/false로 읽기
            return enable;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int ReadCheckedList() { //리스트 인덱스 파일 읽기
        try {
            BufferedReader br=new BufferedReader(new BufferedReader(new FileReader(checkedListFile)));
            int result=Integer.parseInt(br.readLine()); //정수형으로 인덱 읽기
            br.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return err;
        }
    }

    private void Save() {   //전체 저장
        SaveVolume();
        SaveEnable();
        if(SaveCheckedList(TTS_CB.isChecked())) //체크박스가 활성화되어 있을 경우 내용이 선택되면 종료
            finish();
    }

    private void SaveVolume() {   //볼륨 저장
        try {
            BufferedWriter bw=new BufferedWriter(new FileWriter(volumeFile));   //파일라이터
            bw.write(Integer.toString(volume)); //볼륨을 저장
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SaveEnable() { //온오프 저장
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(enableFile));
                bw.write(Boolean.toString(TTS_CB.isChecked()));
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private boolean getSiren() {    //비상벨이 설정되어 있는지 읽기
        File sirenFile=new File(getFilesDir()+"Siren.dat");
        try {
            BufferedReader br=new BufferedReader(new FileReader(sirenFile));
            boolean result=Boolean.parseBoolean(br.readLine());
            br.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean SaveCheckedList(boolean enable) {
        if(enable) {    //TTS가 설정되었는데
            try {   //내용이 설정됨
                BufferedWriter bw = new BufferedWriter(new FileWriter(checkedListFile));
                bw.write(Integer.toString(checkedList));
                bw.flush();
                bw.close();
                return true;
            } catch (Exception e) { //내용이 설정이 안되어 있음
                Toast.makeText(TtsSettingActivity.this, "TTS 내용을 설정해 주세요", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return false;
            }
        }
        else return true;
    }
}
