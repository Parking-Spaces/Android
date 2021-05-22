package com.example.parkingspaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
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

        button_sing_up.setOnClickListener(v -> verifyRegister());

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

    private void verifyRegister() {

        String checkUser = reg_username.getText().toString().trim();
        String checkEmail = email.getText().toString().trim();
        String checkPass = reg_password.getText().toString().trim();
        String checkReType = reType_password.getText().toString().trim();

        if (TextUtils.isEmpty(checkUser)){
            reg_username.setError("Please Enter Username");
            reg_username.requestFocus();
        }

        else if(TextUtils.isEmpty(checkEmail)) {
            email.setError("Please Enter Email");
            email.requestFocus();
        }

        else if (TextUtils.isEmpty(checkPass)) {
            reg_password.setError("Please Enter Password");
            reg_password.requestFocus();

        }

        else {
            if (TextUtils.isEmpty(checkReType)) {
                reType_password.setError("Please Enter Re-Type Password");
                reType_password.requestFocus();
            }

            else {
                verifyData(checkUser, checkEmail, checkPass, checkReType);
            }
        }
    }

    //Verify data with Raspberry
    private void verifyData(String checkUser, String checkEmail, String checkPass, String checkReType) {
        /*
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(checkEmail);
        */

        // Username


        // Email
        // Or "!matcher.matches()"
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(checkEmail).matches()) {
            email.setError("Please Enter A Valid Email");
            email.requestFocus();
        }

        // Password
        else {
            if(!checkPass.equals(checkReType)){
                reType_password.setError("Re-Type Password Don´t Match With Password");
                reType_password.requestFocus();
            }
        }
    }
}
