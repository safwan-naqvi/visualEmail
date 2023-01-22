package com.example.visualemail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class registerActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQ_CODE = 5;
    private static final int REPLACE_CODE = 99;
    private Intent intent;
    private RelativeLayout gestureBox;
    private EditText loginEmail, loginPassword, loginName;
    private SwiperListener swiperListener;
    private static int swipesNumber = 3;
    private int swipeStep = 0;
    private TextToSpeech textToSpeech;
    private Button btnAssist, editBtn;
    String[] perms = {};
    ScaleGestureDetector scaleGestureDetector;
    private ProgressDialog progressDialog;
    String email, password, name;


    int index;
    String input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initWidget();

        swiperListener = new SwiperListener(gestureBox);
        scaleGestureDetector = new ScaleGestureDetector(this, new editListener());
        progressDialog = new ProgressDialog(this);

        mainWorking();
        textToSpeech = new TextToSpeech(registerActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result;
                result = textToSpeech.setLanguage(new Locale("en", "in"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported");
                    textToSpeech.setLanguage(new Locale("en", "us"));
                }

            }
        });

        clickAssistant();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void clickAssistant() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            btnAssist.setOnClickListener(view -> {
                if (swipeStep == 1) {
                    textToSpeech.speak("Speak out your username, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);
                } else if (swipeStep == 2) {
                    textToSpeech.speak("Speak out your password, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    textToSpeech.speak("Speak out your Name, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);
                }
                delayLong();
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "How may I help you?");
                try {
                    startActivityForResult(intent, 1);
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


            btnAssist.setOnLongClickListener(view -> {
                email = loginEmail.getText().toString().trim();
                password = loginPassword.getText().toString().trim();
                name = loginName.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(registerActivity.this, "EMail is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(registerActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(name)) {
                    Toast.makeText(registerActivity.this, "Name is required", Toast.LENGTH_SHORT).show();
                } else {
                    sendingMessageToAdmin(email, password, name);
                }

                return true;
            });

        } else {
            textToSpeech.speak("Permissions are required", TextToSpeech.QUEUE_FLUSH, null, null);
            EasyPermissions.requestPermissions(this, "Must Required Permissions to Run this app", REQ_CODE, perms);
        }
    }

    private void sendingMessageToAdmin(String emailx, String passwordx, String namex) {
        delayShort();
        textToSpeech.speak("Verifying Account", TextToSpeech.QUEUE_FLUSH, null, null);
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
        try {
            //Init Message
            Message message = new MimeMessage(session);
            //Sender
            message.setFrom(new InternetAddress(email));
            //Recipient
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("safwanabbas2020@gmail.com"));
            //Subject Setting
            message.setSubject("User Registration Email");
            //Message Body
            message.setText("New Account registered " + emailx + " Name: " + namex);
            //Sending mail
            new SendMail().execute(message);


        } catch (MessagingException e) {

        }


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
                    if (swipeStep == 1) {
                        loginEmail.setText(purifyText(input) + "@gmail.com"); //By Removing space
                    } else if (swipeStep == 2) {
                        loginPassword.setText(purifyText(input));
                    } else {
                        loginName.setText(input.toUpperCase());
                    }
                    if (containsWords(search, new String[]{"inbox"})) {
                        Intent intent = new Intent(registerActivity.this, messageInbox.class);
                        startActivity(intent);
                    }
                }
                return;
            case 98:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String editText_input;
                    String match = result.get(0).toLowerCase();
                    StringBuilder stringBuilder;
                    if (swipeStep == 1) {
                        editText_input = loginEmail.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("dot")) {
                            stringBuilder.insert(index, '.');
                        } else if (match.equalsIgnoreCase("at the rate")) {
                            stringBuilder.insert(index, '@');
                        } else {
                            stringBuilder.insert(index, match.charAt(0));
                        }
                        loginEmail.setText(stringBuilder);

                    } else if (swipeStep == 2) {
                        editText_input = loginPassword.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("capital")) {
                            match.replace("capital", "");
                            stringBuilder.insert(index, match.toUpperCase().charAt(-1));
                        }
                        loginPassword.setText(stringBuilder);
                    } else {
                        editText_input = loginName.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        stringBuilder.insert(index, match.charAt(0));
                        loginName.setText(stringBuilder);
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
                    if (swipeStep == 1) {
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
                    } else if (swipeStep == 2) {
                        editText_input = loginPassword.getText().toString().trim();
                        stringBuilder = new StringBuilder(editText_input);
                       if (match.contains("capital")) {
                            match.replace("capital", "");
                            stringBuilder.setCharAt(index, match.toUpperCase().charAt(0));
                        } else {
                            stringBuilder.setCharAt(index, match.charAt(0));
                        }
                        loginPassword.setText(stringBuilder);
                    } else {
                        editText_input = loginName.getText().toString().trim();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("capital")) {
                            match.replace("capital", "");
                            stringBuilder.setCharAt(index, match.toUpperCase().charAt(-1));
                        } else {
                            stringBuilder.setCharAt(index, match.charAt(0));
                        }
                        loginName.setText(stringBuilder);
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
                loginName.requestFocus();
                break;
            case 1:
                loginEmail.requestFocus();
                break;
            case 2:
                loginPassword.requestFocus();
                break;
        }

    }

    private void initWidget() {
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginName = findViewById(R.id.login_username);
        gestureBox = findViewById(R.id.gesture_area);
        btnAssist = findViewById(R.id.assist);
        editBtn = findViewById(R.id.edit_assist);
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
                if (swipeStep == 1) {
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
                                                if (index == input.length() - 1) {
                                                    textToSpeech.speak("End of line reached", TextToSpeech.QUEUE_ADD, null, null);
                                                    delayShort();
                                                }
                                                index = ((index - 1) % input.length() + input.length()) % input.length(); // to handle Negative value
                                            } else {
                                                index = (index + 1) % input.length();
                                            }
                                            textToSpeech.speak(String.valueOf(input.charAt(index - 1)), TextToSpeech.QUEUE_ADD, null, null);

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
                                                    startActivityForResult(intent, 98);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                input = charRemoveAt(input, index);
                                                loginEmail.setText(input);
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
                } else if (swipeStep == 2) {
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
                                    startActivityForResult(intent, 99);
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
                                                if (index == input.length() - 1) {
                                                    textToSpeech.speak("End of line reached", TextToSpeech.QUEUE_ADD, null, null);
                                                    delayShort();
                                                }
                                                index = ((index - 1) % input.length() + input.length()) % input.length(); // to handle Negative value
                                            } else {
                                                index = (index + 1) % input.length();
                                            }
                                            textToSpeech.speak(String.valueOf(input.charAt(index)), TextToSpeech.QUEUE_ADD, null, null);

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
                                                    startActivityForResult(intent, 98);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                input = charRemoveAt(input, index);
                                                loginPassword.setText(input);
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
                    input = loginName.getText().toString().trim();
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
                                    startActivityForResult(intent, 99);
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
                                                if (index == input.length() - 1) {
                                                    textToSpeech.speak("End of line reached", TextToSpeech.QUEUE_ADD, null, null);
                                                    delayShort();
                                                }
                                                index = ((index - 1) % input.length() + input.length()) % input.length(); // to handle Negative value
                                            } else {
                                                index = (index + 1) % input.length();
                                            }
                                            textToSpeech.speak(String.valueOf(input.charAt(index)), TextToSpeech.QUEUE_ADD, null, null);

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
                                                    startActivityForResult(intent, 98);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                input = charRemoveAt(input, index);
                                                loginName.setText(input);
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


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NotNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
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

    class SendMail extends AsyncTask<Message, String, String> {


        //init Progress Dialog
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(registerActivity.this, "Please Wait", "Activation", true, false);
        }

        @Override
        protected String doInBackground(Message... messages) {

            try {
                //When Success
                Transport.send(messages[0]);

                return "Success";
            } catch (MessagingException e) {
                e.printStackTrace();
                return "Issue Occur please check Email/Password";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Dismiss Progress
            progressDialog.dismiss();
            if (s.equals("Success")) {
                //Init AlertBuilder
                AlertDialog.Builder builder = new AlertDialog.Builder(registerActivity.this);
                builder.setCancelable(false);
                builder.setTitle(Html.fromHtml("<font color='#509324'>Success</font>"));
                builder.setMessage("Mail Send Successfully");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //Clear all edit text

                    }
                });
                builder.show();
                createAccount(email, password, name);

            } else {
                Toast.makeText(registerActivity.this, "Issue Occur", Toast.LENGTH_SHORT).show();
            }


        }
    }

    public void createAccount(String emailx, String passwordx, String namex) {
        progressDialog.setTitle("Creating Account in our DB");
        progressDialog.setMessage("Please Wait while we setup your account");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        emailx = encodeUserEmail(emailx);
        verificationDone(emailx, passwordx, namex);
    }

    //Encodeing Email to be act as parent in Firebase DATABASE
    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    //--------------------------------------------
    //Decoding Email to be inserted into email field
    static String decodeUserEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    //--------------------------------------------
    private void verificationDone(String email, String password, String name) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        String convertedEmail = decodeUserEmail(email);
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.child("Users").child(email).exists())) {
                    //Verifying if email already exists or not
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("name", name);
                    userDataMap.put("email", convertedEmail);
                    userDataMap.put("password", password);


                    RootRef.child("Users").child(email).updateChildren(userDataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        //Later add speech instead of toast
                                        Toast.makeText(registerActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();

                                        Intent intent = new Intent(registerActivity.this, loginActivity.class);
                                        startActivity(intent);

                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(registerActivity.this, "Network issue occur", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                } else {
                    progressDialog.dismiss();
                    //Later we will add speech here
                    Toast.makeText(registerActivity.this, "Account " + email + " Already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
