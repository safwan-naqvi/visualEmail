package com.example.visualemail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    //For testing creating intro activity with two buttons login,sign up Later we'll upgrade it
    private Button btnLogin, btnSignUP;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Paper.init(this);
        progressDialog = new ProgressDialog(this);
        btnLogin = findViewById(R.id.login);
        btnSignUP = findViewById(R.id.register);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, loginActivity.class));
            }
        });


        btnSignUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, registerActivity.class));
            }
        });

        String UserEmailKey = "Paper.book().read(Prevalent.email)";
        String UserPasswordKey = "Paper.book().read(Prevalent.password)";

        if (!TextUtils.isEmpty(UserEmailKey) && !TextUtils.isEmpty(UserPasswordKey)) {
            AllowAccess(UserEmailKey, UserPasswordKey);
            progressDialog.setTitle("Loggin in");
            progressDialog.setMessage("Already Logged in");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

    }

    private void AllowAccess(String emailx, String passwordx) {

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        String convertedEmail = registerActivity.encodeUserEmail(emailx);
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Users").child(convertedEmail).exists()) {
                    Users userData = snapshot.child("Users").child(convertedEmail).getValue(Users.class);
                    if (userData.getEmail().equals(emailx)) {
                        if (userData.getPassword().equals(passwordx)) {
                            //Logging into the account
                            progressDialog.dismiss();

                            Intent intent = new Intent(MainActivity.this, messageActivity.class);
                            startActivity(intent);

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Password is wrong", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Email is wrong", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Account is not created", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}