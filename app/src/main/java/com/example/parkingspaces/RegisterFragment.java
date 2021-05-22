package com.example.parkingspaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterFragment extends AppCompatActivity {

    EditText reg_username, email, reg_password, reType_password;
    Button button_sing_up;
    TextView back_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);

        reg_username = findViewById(R.id.reg_username);
        email = findViewById(R.id.email);
        reg_password = findViewById(R.id.reg_password);
        reType_password = findViewById(R.id.re_type_password);
        button_sing_up = findViewById(R.id.button_register);
        back_login = findViewById(R.id.back_login);


        String text = "Already Have An Account? Sing In";
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent registerOpen = new Intent(RegisterFragment.this, LoginFragment.class);
                startActivity(registerOpen);
            }
        };

        SpannableString s = new SpannableString(text);
        s.setSpan(span, 25, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        back_login.setText(s);
        back_login.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
