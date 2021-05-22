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
import android.widget.Toast;

public class LoginFragment extends AppCompatActivity {

    EditText username,password;
    Button button_login;
    TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        button_login = findViewById(R.id.button_login);
        register = findViewById(R.id.create_account);

        button_login.setOnClickListener(v -> {
            if(username.getText().toString().equals("admin") && username.getText().toString().equals("admin")) {
                Toast.makeText(getApplicationContext(),"Login Accepted!",Toast.LENGTH_SHORT).show();
                Intent registerOpen = new Intent(LoginFragment.this, MainActivity.class);
                startActivity(registerOpen);
            }

            else {
                Toast.makeText(getApplicationContext(), "Wrong Credentials",Toast.LENGTH_SHORT).show();
            }
        });


        String text = "Don`t Have An Account? Create one";
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent registerOpen = new Intent(LoginFragment.this, RegisterFragment.class);
                startActivity(registerOpen);
            }
        };

        SpannableString s = new SpannableString(text);
        s.setSpan(span, 23, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        register.setText(s);
        register.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
