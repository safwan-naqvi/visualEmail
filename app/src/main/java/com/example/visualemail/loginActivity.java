package com.example.visualemail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.visualemail.Model.Prevalent;
import com.example.visualemail.Model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import io.paperdb.Paper;

public class loginActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1;
    private static final int INSERT_CODE = 98;
    private static final int REPLACE_CODE = 99;
    private Intent intent;
    private RelativeLayout gestureBox;
    private EditText loginEmail, loginPassword;
    private loginActivity.SwiperListener swiperListener;
    private static int swipesNumber = 2;
    private int swipeStep = 0;
    private TextToSpeech textToSpeech;
    private Button btnAssist, editBtn;
    ScaleGestureDetector scaleGestureDetector;
    private ProgressDialog progressDialog;


    private String parentDBName = "Users";

    boolean rightSwipe = true, leftSwipe;
    int index;
    String input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initWidget();

        swiperListener = new loginActivity.SwiperListener(gestureBox);
        scaleGestureDetector = new ScaleGestureDetector(this, new loginActivity.editListener());
        progressDialog = new ProgressDialog(this);

        mainWorking();
        textToSpeech = new TextToSpeech(loginActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result;
                result = textToSpeech.setLanguage(new Locale("en", "uk"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported");
                    textToSpeech.setLanguage(new Locale("en", "uk"));
                } else {
                    textToSpeech.setLanguage(new Locale("ur", "pk"));
                }

            }
        });
        Paper.init(this);

        clickAssistant();
    }

    private void initWidget() {
        gestureBox = findViewById(R.id.gesture_area);
        btnAssist = findViewById(R.id.assist);
        editBtn = findViewById(R.id.edit_assist);
        loginEmail = findViewById(R.id.login_email_l);
        loginPassword = findViewById(R.id.login_password_l);
    }


    private void clickAssistant() {
        btnAssist.setOnClickListener(view -> {
            if (swipeStep == 0) {
                textToSpeech.speak("Speak out your username, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak("Speak out your password, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);
            }
            delayLong();
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "How may I help you?");
            try {
                startActivityForResult(intent, REQ_CODE);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        btnAssist.setOnLongClickListener(view -> {
            String emailx, passwordx;
            emailx = loginEmail.getText().toString().trim();
            passwordx = loginPassword.getText().toString().trim();
            if (TextUtils.isEmpty(emailx)) {
                Toast.makeText(loginActivity.this, "Email is required!", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(passwordx)) {
                Toast.makeText(loginActivity.this, "Password is required!", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.setTitle("Loggin in");
                progressDialog.setMessage("Please Wait while loggin in");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                AllowAccess(emailx, passwordx);
            }

            delayShort();
            return false;
        });

    }


    public static boolean containsWords(String inputString, String[] items) {
        boolean found = true;
        for (String item : items) {
            if (!inputString.contains(item)) {
                found = false;
                break;
            }
        }
        Log.i("app", String.valueOf(found));
        return found;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result =
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String input = result.get(0);
                    String search = input.toLowerCase().trim();
                    if (swipeStep == 0) {
                        loginEmail.setText(purifyText(input) + "@gmail.com"); //By Removing space
                    } else {
                        loginPassword.setText(purifyText(input));
                    }
                    if (containsWords(search, new String[]{"inbox"})) {
                        Intent intent = new Intent(loginActivity.this, messageInbox.class);
                        startActivity(intent);
                    } else if (containsWords(search, new String[]{"logout"})) {
                        Paper.book().destroy();
                        Intent intent = new Intent(loginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
                return;
            case INSERT_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String editText_input;
                    String match = result.get(0).toLowerCase();
                    StringBuilder stringBuilder;
                    if (swipeStep == 0) {
                        editText_input = loginEmail.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        if (leftSwipe == true) {
                            if (match.contains("dot")) {
                                stringBuilder.insert(index, '.');
                            } else if (match.equalsIgnoreCase("at the rate")) {
                                stringBuilder.insert(index, '@');
                            } else {
                                stringBuilder.insert(index, match.charAt(0));
                            }
                        } else {
                            if (index == 0) {
                                if (match.contains("dot")) {
                                    stringBuilder.insert(index, '.');
                                } else if (match.equalsIgnoreCase("at the rate")) {
                                    stringBuilder.insert(index, '@');
                                } else {
                                    stringBuilder.insert(index, match.charAt(0));
                                }
                            } else {
                                if (match.contains("dot")) {
                                    stringBuilder.insert(index + 1, '.');
                                } else if (match.equalsIgnoreCase("at the rate")) {
                                    stringBuilder.insert(index + 1, '@');
                                } else {
                                    stringBuilder.insert(index + 1, match.charAt(0));
                                }
                            }

                        }

                        loginEmail.setText(stringBuilder);
                    } else {
                        editText_input = loginPassword.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        if (leftSwipe == true) {
                            if (match.contains("capital")) {
                                match.replace("capital", "");
                                stringBuilder.insert(index, match.toUpperCase().charAt(0));
                            } else {
                                stringBuilder.insert(index, match.charAt(0));
                            }
                        } else {
                            if (match.contains("capital")) {
                                match.replace("capital", "");
                                stringBuilder.insert(index + 1, match.toUpperCase().charAt(0));
                            } else {
                                stringBuilder.insert(index + 1, match.charAt(0));
                            }
                        }


                        loginPassword.setText(stringBuilder);
                    }
                    input = String.valueOf(stringBuilder);


                }
                return;


            case REPLACE_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String editText_input;
                    StringBuilder stringBuilder;
                    String match = result.get(0).toLowerCase();
                    if (swipeStep == 0) {
                        editText_input = loginEmail.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("dot")) {
                            stringBuilder.setCharAt(index, '.');
                        } else if (match.equalsIgnoreCase("at the rate")) {
                            stringBuilder.setCharAt(index, '@');
                        } else {
                            stringBuilder.setCharAt(index, match.charAt(0));
                        }
                        loginEmail.setText(stringBuilder);
                    } else {
                        editText_input = loginPassword.getText().toString().trim();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("capital")) {
                            match.replace("capital", "");
                            stringBuilder.setCharAt(index, match.toUpperCase().charAt(0));
                        } else {
                            stringBuilder.setCharAt(index, match.charAt(0));
                        }
                        loginPassword.setText(stringBuilder);
                    }
                    input = String.valueOf(stringBuilder);

                    index = 0;
                }
                return;
        }
    }


    private void mainWorking() {

        switch (swipeStep) {
            case 0:
                loginEmail.requestFocus();
                break;
            case 1:
                loginPassword.requestFocus();
                break;

        }

    }


    private class SwiperListener implements View.OnTouchListener {
        GestureDetector gestureDetector;

        //Making Constructor
        public SwiperListener(View view) {
            //Required Variables init
            int threshold = 100;
            int velocity_threshold = 100;

            //Init Simple Gesture Listener
            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e1.getY();

                    try {
                        //Checking conditions
                        if (Math.abs(xDiff) > Math.abs(yDiff)) {
                            //Checking Conditions
                            if (Math.abs(xDiff) > threshold
                                    && Math.abs(velocityX) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (xDiff > 0) {
                                    swipeStep = ((swipeStep - 1) % swipesNumber + swipesNumber) % swipesNumber; // to handle Negative value
                                } else {
                                    swipeStep = (swipeStep + 1) % swipesNumber;
                                }
                                textToSpeech.speak("Currently Edit mode is off", TextToSpeech.QUEUE_FLUSH, null, null);
                                btnAssist.setVisibility(View.VISIBLE);
                                editBtn.setVisibility(View.INVISIBLE);
                                mainWorking();
                            }
                        } else {
                            if (Math.abs(yDiff) > threshold
                                    && Math.abs(velocityY) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (yDiff > 0) {
                                    Toast.makeText(getApplicationContext(), "Down", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Up", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };
            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);

        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //return Gesture event
            return gestureDetector.onTouchEvent(motionEvent);
        }
    }

    public class editListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float gestureFactor = detector.getScaleFactor();
            if (gestureFactor > 1) {
                textToSpeech.speak("Editing Mode is turned on", TextToSpeech.QUEUE_FLUSH, null, null);
                btnAssist.setVisibility(View.INVISIBLE);
                editBtn.setVisibility(View.VISIBLE);
                index = 0;
                if (swipeStep == 0) {
                    input = loginEmail.getText().toString().trim().toLowerCase();
                    editBtn.setOnTouchListener(new View.OnTouchListener() {
                        GestureDetector detector = new GestureDetector(getApplicationContext(), new GestureDetector.OnGestureListener() {
                            @Override
                            public boolean onDown(MotionEvent motionEvent) {

                                return false;
                            }

                            @Override
                            public void onShowPress(MotionEvent motionEvent) {

                            }

                            @Override
                            public boolean onSingleTapUp(MotionEvent motionEvent) {

                                return false;
                            }

                            @Override
                            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                                return false;
                            }

                            @Override
                            public void onLongPress(MotionEvent motionEvent) {
                                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                                try {
                                    startActivityForResult(intent, REPLACE_CODE);
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                                float xDiff = motionEvent1.getX() - motionEvent.getX();
                                float yDiff = motionEvent1.getY() - motionEvent.getY();

                                try {
                                    //Checking conditions
                                    if (Math.abs(xDiff) > Math.abs(yDiff)) {
                                        //Checking Conditions
                                        if (Math.abs(xDiff) > 100
                                                && Math.abs(v) > 100) {
                                            //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                            if (xDiff < 0) {
                                                index = ((index - 1) % input.length() + input.length()) % input.length(); // to handle Negative value
                                                leftSwipe = true;
                                                rightSwipe = false;
                                                Log.i("check", String.valueOf(rightSwipe));
                                            } else {
                                                index = (index + 1) % input.length();

                                                rightSwipe = true;
                                                leftSwipe = false;

                                                Log.i("check", String.valueOf(rightSwipe));
                                            }
                                            textToSpeech.speak(String.valueOf(input.charAt(index)), TextToSpeech.QUEUE_ADD, null, null);
                                            if (index == input.length() - 1) {
                                                textToSpeech.speak("End of line reached", TextToSpeech.QUEUE_ADD, null, null);
                                                delayShort();
                                            }
                                            loginEmail.setSelection(index);
                                        }
                                    } else {
                                        if (Math.abs(yDiff) > 100
                                                && Math.abs(v1) > 100) {
                                            //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                            if (yDiff > 0) {
                                                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                                                try {
                                                    startActivityForResult(intent, INSERT_CODE);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                if (input.length() > 0) {
                                                    input = charRemoveAt(input, index);
                                                    loginEmail.setText(input);
                                                }

                                            }
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                return false;
                            }
                        });

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            detector.onTouchEvent(motionEvent);
                            return false;
                        }
                    });
                } else {
                    index = 0;
                    input = loginPassword.getText().toString().trim();
                    editBtn.setOnTouchListener(new View.OnTouchListener() {
                        GestureDetector detector = new GestureDetector(getApplicationContext(), new GestureDetector.OnGestureListener() {
                            @Override
                            public boolean onDown(MotionEvent motionEvent) {

                                return false;
                            }

                            @Override
                            public void onShowPress(MotionEvent motionEvent) {

                            }

                            @Override
                            public boolean onSingleTapUp(MotionEvent motionEvent) {

                                return false;
                            }

                            @Override
                            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                                return false;
                            }

                            @Override
                            public void onLongPress(MotionEvent motionEvent) {
                                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                                try {
                                    startActivityForResult(intent, REPLACE_CODE);
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                                float xDiff = motionEvent1.getX() - motionEvent.getX();
                                float yDiff = motionEvent1.getY() - motionEvent.getY();

                                try {
                                    //Checking conditions
                                    if (Math.abs(xDiff) > Math.abs(yDiff)) {
                                        //Checking Conditions
                                        if (Math.abs(xDiff) > 100
                                                && Math.abs(v) > 100) {
                                            //When X differ is greater than threshold and X velocity is greater than velocity threshold

                                            if (xDiff < 0) {
                                                index = ((index - 1) % input.length() + input.length()) % input.length(); // to handle Negative value
                                                leftSwipe = true;
                                                rightSwipe = false;
                                                Log.i("check", String.valueOf(rightSwipe));
                                            } else {
                                                index = (index + 1) % input.length();
                                                leftSwipe = false;
                                                rightSwipe = true;
                                                Log.i("check", String.valueOf(rightSwipe));
                                            }
                                            textToSpeech.speak(String.valueOf(input.charAt(index)), TextToSpeech.QUEUE_ADD, null, null);
                                            if (index == input.length()) {
                                                textToSpeech.speak("End of line reached", TextToSpeech.QUEUE_ADD, null, null);
                                                delayShort();
                                            }
                                            loginPassword.setSelection(index);
                                        }
                                    } else {
                                        if (Math.abs(yDiff) > 100
                                                && Math.abs(v1) > 100) {
                                            //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                            if (yDiff > 0) {
                                                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                                                try {
                                                    startActivityForResult(intent, INSERT_CODE);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                if (input.length() > 1) {
                                                    input = charRemoveAt(input, index);
                                                    loginPassword.setText(input);

                                                }
                                            }
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                return false;
                            }
                        });

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            detector.onTouchEvent(motionEvent);
                            return false;
                        }
                    });


                }
            } else {
                textToSpeech.speak("Editing Mode is turned off", TextToSpeech.QUEUE_FLUSH, null, null);
                btnAssist.setVisibility(View.VISIBLE);
                editBtn.setVisibility(View.INVISIBLE);
            }

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    public static String purifyText(String name) {
        String Name = name.toLowerCase().trim();
        Name = Name.replace(" ", "");
        Name = Name.replace("dot", ".");
        Log.i("test", Name);
        return Name;
    }

    @Override
    public void onDestroy() {
        //Don't forget to shut down text to speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    public void delayLong() {
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void delayShort() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Function To Remove character
    public static String charRemoveAt(String str, int p) {
        return str.substring(0, p) + str.substring(p + 1);
    }

    private void AllowAccess(String emailx, String passwordx) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        String convertedEmail = registerActivity.encodeUserEmail(emailx);
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(parentDBName).child(convertedEmail).exists()) {
                    Users userData = snapshot.child(parentDBName).child(convertedEmail).getValue(Users.class);
                    if (userData.getEmail().equals(emailx)) {
                        if (userData.getPassword().equals(passwordx)) {
                            //Logging into the account
                            progressDialog.dismiss();

                            Paper.book().write(Prevalent.email, emailx);
                            Paper.book().write(Prevalent.password, passwordx);

                            Intent intent = new Intent(loginActivity.this, messageActivity.class);
                            startActivity(intent);

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(loginActivity.this, "Password is wrong", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(loginActivity.this, "Email is wrong", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(loginActivity.this, "Account is not created", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}