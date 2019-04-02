package kr.ac.gachon.www.SaveMe.SignUp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kr.ac.gachon.www.SaveMe.MainActivity;
import kr.ac.gachon.www.SaveMe.R;

public class SignUpActivity extends AppCompatActivity {
    EditText phoneET, nameET, addressET, etcET;
    Button confirmBtn, cancelBtn;


    private String PhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        phoneET=findViewById(R.id.phoneET);
        nameET=findViewById(R.id.nameET);
        addressET=findViewById(R.id.addressET);
        etcET=findViewById(R.id.etcET);
        confirmBtn=findViewById(R.id.confirmBtn);
        cancelBtn=findViewById(R.id.cancelBtn);

        PhoneNumber=getPhoneNumber();   //전화번호 읽어서 저장
        phoneET.setText(PhoneNumber);   //화면에 표시
        phoneET.setEnabled(false);

        confirmBtn.setOnClickListener(new View.OnClickListener() {  //확인 버튼 설정
            @Override
            public void onClick(View view) {
                SignUp();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {   //취소 버튼 설정
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private String getPhoneNumber(){    //전화번호 읽어오기
        String Number = null;   //
        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try{
            Number=mgr.getLine1Number();
            Number = Number.replace("+82", "0");
            System.out.println("회원가입 전화번호: "+Number);
            return Number;
        }catch(Exception e){
            return null;
        }
    }

    private void SignUp() { //회원가입
        final String name=nameET.getText().toString();
        final String address=addressET.getText().toString();
        final String etc=etcET.getText().toString();
        //입력을 모두 다 했는지 체크
        if(name.length()==0) Toast.makeText(SignUpActivity.this, "이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
        else if(address.length()==0) Toast.makeText(SignUpActivity.this, "집주소를 입력해 주세요", Toast.LENGTH_SHORT).show();
        else {  //모든 내용을 입력했으면
            final AlertDialog.Builder builder=new AlertDialog.Builder(SignUpActivity.this);
            builder.setTitle("확인")
                    .setMessage("회원가입하시겠습니까?")
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    InsertData(name, address, etc);
                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();
        }
    }

    private void InsertData(String name, String address, String etc) {  //데이터 삽입 후 종료
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("Members").child(PhoneNumber);
        reference.child("Name").setValue(name); //이름 입력
        reference.child("Address").setValue(address);   //주소 입력
        reference.child("Etc").setValue(etc);   //기타 사항 입력
        reference.child("PhoneNumber").setValue(PhoneNumber);   //전화번호 입력

        Toast.makeText(SignUpActivity.this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
        TermsActivity termsActivity=(TermsActivity)TermsActivity.termsActivity; //이전 액티비티 종료을 위해 받아옴
        Intent intent=new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);  //메인 액티비티로 이동
        termsActivity.finish(); //이전 액티비티 종료
        finish();
    }
}
