package kr.ac.gachon.www.SaveMe.Setting;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

import kr.ac.gachon.www.SaveMe.R;

public class SensitivitySettingActivity extends AppCompatActivity {
    SeekBar sensitivitySB;
    TextView saveTV;
    File sensitivityFile;
    int sensitivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensitivity_setting);

        sensitivitySB=findViewById(R.id.sensitivitySB);
        saveTV=findViewById(R.id.saveTV);
        //좀 예쁘게
        sensitivitySB.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
        sensitivitySB.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
        sensitivitySB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { //seekbar 변경 리스너
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sensitivity=seekBar.getProgress();
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
        });

        sensitivityFile=new File(getFilesDir()+"sensitivity.dat");
        sensitivity=getSensitivity();
        sensitivitySB.setProgress(sensitivity);    //민감도 표시
    }

    private int getSensitivity() {  //파일에서 민감도를 읽어오기
        int result=2;
        try {
            BufferedReader br=new BufferedReader(new FileReader(sensitivityFile));
            result=Integer.parseInt(br.readLine());
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return  result;
        }
    }

    private void setSensitivity(int sensitivity) {  //파일에 민감도 저장
        try {
            BufferedWriter bw=new BufferedWriter(new FileWriter(sensitivityFile, false));
            bw.write(Integer.toString(sensitivity));
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Save() {
        setSensitivity(sensitivity);
        Toast.makeText(SensitivitySettingActivity.this, "변경한 설정은 다시 시작해야 적용됩니다", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void close(View v) {
        finish();
    }
}
