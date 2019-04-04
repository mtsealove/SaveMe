package kr.ac.gachon.www.SaveMe.Member;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kr.ac.gachon.www.SaveMe.R;

public class EditMemberActivity extends AppCompatActivity {
    String PhoneNumber;
    EditText phoneET, nameET, etcET, addressET, birthET;
    Button confirmBtn, cancelBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_member);
        Intent intent=getIntent();
        PhoneNumber=intent.getStringExtra("PhoneNumber");

        phoneET=findViewById(R.id.phoneET);
        nameET=findViewById(R.id.nameET);
        etcET=findViewById(R.id.etcET);
        addressET=findViewById(R.id.addressET);
        confirmBtn=findViewById(R.id.confirmBtn);
        cancelBtn=findViewById(R.id.cancelBtn);
        birthET=findViewById(R.id.birthET);

        getInfo();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setInfo();
            }
        });
    }

    private void getInfo(){ //정보를 읽어와 화면에 표시
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference().child("Members").child(PhoneNumber);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String address=dataSnapshot.child("Address").getValue(String.class);    //주소
                String etc=dataSnapshot.child("Etc").getValue(String.class);    //특이사항
                String Name=dataSnapshot.child("Name").getValue(String.class);  //이름
                String birth=dataSnapshot.child("Birth").getValue(String.class);    //생년월일
                phoneET.setText(PhoneNumber);
                nameET.setText(Name);
                etcET.setText(etc);
                addressET.setText(address);
                birthET.setText(birth);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setInfo() {    //정보를 데이터베이스에 저장
        String name=nameET.getText().toString();
        String etc=etcET.getText().toString();
        String address=addressET.getText().toString();
        String birth=birthET.getText().toString();

        //입력 확인
        if(name.length()==0) Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
        else if(address.length()==0) Toast.makeText(this, "주소를 입력하세요", Toast.LENGTH_SHORT).show();
        else if(birth.length()==0) Toast.makeText(this, "생년월일을 입력하세요", Toast.LENGTH_SHORT).show();
        else if(birth.length()!=6) Toast.makeText(this, "생년월일을 확인해 주세요", Toast.LENGTH_SHORT).show();
        else {
            FirebaseDatabase database=FirebaseDatabase.getInstance();
            DatabaseReference reference=database.getReference().child("Members").child(PhoneNumber);
            //데이터베이스에 삽입
            reference.child("Address").setValue(address);
            reference.child("Etc").setValue(etc);
            reference.child("Name").setValue(name);
            reference.child("Birth").setValue(birth);
            AlertDialog.Builder builder=new AlertDialog.Builder(this)   //확인 다이얼로그 생성
                    .setMessage("정보가 변경되었습니다")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            AlertDialog dialog=builder.create();
            dialog.show();
        }
    }
}
