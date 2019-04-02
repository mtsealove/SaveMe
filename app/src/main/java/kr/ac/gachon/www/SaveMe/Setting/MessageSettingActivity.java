package kr.ac.gachon.www.SaveMe.Setting;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kr.ac.gachon.www.SaveMe.R;

public class MessageSettingActivity extends AppCompatActivity { //메시지 설정 액티비티
    LinearLayout contactLayout, messageLayout;
    RelativeLayout titleLayout;
    EditText phoneET, messageET;
    TextView saveTV;
    ListView phoneLV;
    Button addPhoneBtn;
    private String PhoneNumber;

    private ArrayList<String> phoneList;
    private String message="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_setting);

        titleLayout=findViewById(R.id.titleLayout);
        contactLayout=findViewById(R.id.contactLayout);
        phoneET=findViewById(R.id.phoneET);
        phoneET.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        messageET=findViewById(R.id.messageET);
        phoneLV=findViewById(R.id.phoneLV);
        saveTV=findViewById(R.id.saveTV);
        addPhoneBtn=findViewById(R.id.phoneAddBtn);
        addPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhoneNumber();
            }
        });

        Intent getIntent=getIntent();
        PhoneNumber=getIntent.getStringExtra("PhoneNumber");    //자신의 전화번호 읽어오기
        ReadPhoneNumber();
        ReadMessage();
        phoneLV.setOnItemLongClickListener(phoneLongClickListener); //전화번호 길게 눌렀을 떄 할 작업
        saveTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    //저장 버튼 누르면
                WriteMessage(); //메세지 저장
                WritePhoneNumber(); //전화번호 저장
                finish();   //액티비티 종료
            }
        });
    }

    public void close(View v) {
        finish();
    }

    File phoneFile;
    File messageFile;//=new File(getFilesDir()+"message.dat");

    private void ReadPhoneNumber() {    //저장되어 있는 전화번호 읽기
        phoneList=new ArrayList<>();
        try {
            phoneFile=new File(getFilesDir()+"phones.dat");
            BufferedReader br=new BufferedReader(new FileReader(phoneFile));
            String tmp;
            while ((tmp=br.readLine())!=null) {
                phoneList.add(tmp);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }

        //화면에 표시
        updatePhoneList();
    }

    private void addPhoneNumber() { //리스트에 전화번호 추가하기
        if(phoneList.size()>4) {    //전화번호 개수 확인
            Toast.makeText(MessageSettingActivity.this, "전화번호는 5개를 초과할 수 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        String NewPhoneNum=phoneET.getText().toString();

        int count=0;
        for(int i=0; i<NewPhoneNum.length(); i++) { //전화번호 형식 확인
            if(NewPhoneNum.charAt(i)=='-') count++;
        }
        if(count!=2) {
            Toast.makeText(MessageSettingActivity.this, "올바른 전화번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exist=false;
        for(int i=0; i<phoneList.size(); i++) { //이미 존재하는 전화번호인지 확인
            if(NewPhoneNum.equals(phoneList.get(i))) {  //리스트 검색
                exist=true; //값 변경
            }
        }
        if(!exist) {    //존재하지 않으면
            phoneList.add(NewPhoneNum); //리스트에 추가하고
            phoneET.setText("");    //입력란 초기화
        }
        else Toast.makeText(MessageSettingActivity.this, "이미 저장한 전화번호 입니다", Toast.LENGTH_SHORT).show();
        updatePhoneList();
    }

    private void WritePhoneNumber() {   //전화번호 저장
        //파일에 저장
        try {
            BufferedWriter bw=new BufferedWriter(new FileWriter(phoneFile, false)); //기존 파일내용은 없앤다
            for(int i=0; i<phoneList.size(); i++) { //전화번호의 개수만큼
                bw.write(phoneList.get(i));
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //데이터베이스에 저장
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference().child("Members").child(PhoneNumber).child("FriendNumbers");
        reference.setValue(null);   //기존 데이터는 제거
        for(int i=0; i<phoneList.size(); i++)
            reference.child("friend"+i).setValue(phoneList.get(i));
    }

    private void updatePhoneList() {    //전화번호 리스트 업데이트
        ArrayAdapter adapter=new ArrayAdapter(MessageSettingActivity.this, R.layout.main_drop_down, phoneList);
        phoneLV.setAdapter(adapter);
    }

    private void ReadMessage() {    //저장된 메세지 읽기
        try {
            messageFile=new File(getFilesDir()+"message.dat");
            BufferedReader br=new BufferedReader(new FileReader(messageFile));
            String tmp;
            while((tmp=br.readLine())!=null) {
                message+=tmp+"\n";  //내용 모두 읽기
            }
            br.close();
            message=message.substring(0, message.length()-1);
            messageET.setText(message); //화면에 표시
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    private void WriteMessage() {   //메세지 저장
        message=messageET.getText().toString();
        try {
            BufferedWriter bw=new BufferedWriter(new FileWriter(messageFile, false));   //내용 없애기
            bw.write(message);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    AdapterView.OnItemLongClickListener phoneLongClickListener=new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {  //길게 눌렀을 때 작동하는 리스너
            //삭제 다이얼로그 출력
            AlertDialog.Builder builder=new AlertDialog.Builder(MessageSettingActivity.this);   //다이얼로그 빌더
            ListView listView=new ListView(MessageSettingActivity.this);    //항목을 표시할 리스트뷰
            ArrayList<String> arrayList=new ArrayList<>();  //리스트뷰에 들어갈 내용
            arrayList.add("삭제");
            ArrayAdapter adapter=new ArrayAdapter(MessageSettingActivity.this, R.layout.main_drop_down, arrayList); //리스트뷰를 설정해주는 어댑터
            listView.setAdapter(adapter);   //리스트뷰에 어댑터 적용
            builder.setView(listView);  //빌더에 리스트 표시
            final AlertDialog dialog=builder.create();  //다이얼로그 생성
            dialog.show();  //다이얼로그 출력
            final int position=i;   //final을 이용하여 리스터 안에서 사용
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { //삭제 버튼을 누르면
                    switch (i) {
                        case 0:
                            dialog.cancel();    //리스트가 표시된 다이얼로그 닫기
                            phoneList.remove(position); //전화 리스트에서 포지션에 해당하는 전화번호 삭제
                            updatePhoneList();  //폰 리스트 업데이트
                            break;
                    }
                }
            });

            return false;
        }
    };

}
