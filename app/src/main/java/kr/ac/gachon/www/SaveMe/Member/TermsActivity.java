package kr.ac.gachon.www.SaveMe.Member;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import kr.ac.gachon.www.SaveMe.R;

public class TermsActivity extends AppCompatActivity {
    CheckBox cb1, cb2;  //약관의 동의할 체크박스
    Button confirmBtn, cancelBtn;
    private String PhoneNumber;
    public static Activity termsActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        termsActivity=TermsActivity.this;
        Intent get=getIntent();
        PhoneNumber=get.getStringExtra("PhoneNumber");

        cb1=findViewById(R.id.termCB1);
        cb2=findViewById(R.id.termCB2);
        confirmBtn=findViewById(R.id.confirmBtn);
        cancelBtn=findViewById(R.id.cancelBtn);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoveSignUp();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
    }

    private void MoveSignUp() {
        if(cb2.isChecked()&&cb1.isChecked()) {
            Intent intent=new Intent(TermsActivity.this, SignUpActivity.class);
            intent.putExtra("PhoneNumber", PhoneNumber);
            startActivity(intent);
        } else {
            Toast.makeText(TermsActivity.this, "약관을 모두 동의해 주세요", Toast.LENGTH_LONG).show();
        }
    }
}
