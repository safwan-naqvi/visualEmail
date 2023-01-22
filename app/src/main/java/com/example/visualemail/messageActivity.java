package com.example.visualemail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import io.paperdb.Paper;

public class messageActivity extends AppCompatActivity {
    private static final int REQ_CODE = 5;
    private static final int REPLACE_CODE = 99;
    private Intent intent;
    private static int swipesNumber = 3;
    private int swipeStep = 0;
    private EditText to, subject, etMessage;
    private TextToSpeech textToSpeech;
    private Button btnAssist, editBtn;
    private RelativeLayout gestureBox;
    private SwiperListener swiperListener;
    ScaleGestureDetector scaleGestureDetector;
    private String fromEmail = "amna.rabia2022@gmail.com";
    private String fromEmailPass = "project2022";
    List<String> messageBody = new ArrayList<String>();
    private DatabaseReference sentMessageRef;
    String sendTO, sendSubject, sendMessage;

    String messageRandomKey, saveCurrentDate, saveCurrentTime;


    int index;
    String input;
    int cursorPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initWidget();
        sentMessageRef = FirebaseDatabase.getInstance().getReference().child("Sent");
        swiperListener = new SwiperListener(gestureBox);
        scaleGestureDetector = new ScaleGestureDetector(this, new editListener());
        textToSpeech = new TextToSpeech(messageActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result;
                result = textToSpeech.setLanguage(new Locale("en", "in"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported");
                    textToSpeech.setLanguage(new Locale("en", "us"));
                } else {
                    textToSpeech.setLanguage(new Locale("en", "in"));
                }

            }
        });
        mainWorking();
        clickAssistant();

    }

    private void initWidget() {
        to = findViewById(R.id.email_To);
        subject = findViewById(R.id.email_Subject);
        etMessage = findViewById(R.id.email_message);
        gestureBox = findViewById(R.id.gesture_area);
        btnAssist = findViewById(R.id.assist);
        editBtn = findViewById(R.id.edit_assist);
    }

    private void mainWorking() {

        switch (swipeStep) {
            case 0:
                textToSpeech.speak("Enter receiver email", TextToSpeech.QUEUE_FLUSH, null, null);
                to.requestFocus();
                break;
            case 1:
                subject.requestFocus();
                textToSpeech.speak("Subject", TextToSpeech.QUEUE_FLUSH, null, null);
                break;
            case 2:
                etMessage.requestFocus();
                textToSpeech.speak("Your Message", TextToSpeech.QUEUE_FLUSH, null, null);
                break;
        }

    }

    private void clickAssistant() {
        btnAssist.setOnClickListener(view -> {
            if (swipeStep == 0) {
                textToSpeech.speak("Speak out your username, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);
            } else if (swipeStep == 1) {
                textToSpeech.speak("Speak out your password, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak("Speak out your Message, Right after the beep", TextToSpeech.QUEUE_FLUSH, null, null);

            }
            delayLong();
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            try {
                startActivityForResult(intent, 1);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        btnAssist.setOnLongClickListener(view -> {
            sendTO = to.getText().toString().trim();
            sendSubject = subject.getText().toString().trim();
            sendMessage = etMessage.getText().toString().trim();

            if (TextUtils.isEmpty(sendTO)) {
                Toast.makeText(messageActivity.this, "Receiver Email is Required", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(sendSubject)) {
                Toast.makeText(messageActivity.this, "Subject is Required", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(sendMessage)) {
                Toast.makeText(messageActivity.this, "Message is Required to be sent", Toast.LENGTH_SHORT).show();
            } else {
                delayShort();
                textToSpeech.speak("Sending", TextToSpeech.QUEUE_FLUSH, null, null);
                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");
                properties.put("mail.smtp.ssl.trust", "*");

                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, fromEmailPass);
                    }
                });

                try {
                    //Init Message
                    Message message = new MimeMessage(session);
                    //Sender
                    message.setFrom(new InternetAddress("amna.rabia2022@gmail.com"));
                    //Recipient
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.getText().toString().trim()));
                    //Subject Setting
                    message.setSubject(subject.getText().toString());
                    //Message Body
                    message.setText(etMessage.getText().toString());
                    //Sending mail
                    new messageActivity.SendMail().execute(message);

                } catch (MessagingException e) {
                    Log.e("issue", e.toString());
                }
            }
            return true;
        });


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
                    input = to.getText().toString().trim().toLowerCase();
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
                                            } else {
                                                index = (index + 1) % input.length();
                                            }
                                            to.setSelection(index);
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
                                                    startActivityForResult(intent, REQ_CODE);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                input = charRemoveAt(input, index);
                                                to.setText(input);
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
                } else if (swipeStep == 1) {
                    index = 0;
                    input = subject.getText().toString().trim();
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
                                            } else {
                                                index = (index + 1) % input.length();
                                            }
                                            subject.setSelection(index);
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
                                                    startActivityForResult(intent, REQ_CODE);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                input = charRemoveAt(input, index);
                                                subject.setText(input);
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
                    input = etMessage.getText().toString().trim();
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
                                            etMessage.setSelection(index);
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
                                                    startActivityForResult(intent, REQ_CODE);
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                input = charRemoveAt(input, index);
                                                etMessage.setText(input);
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
                        to.setText(registerActivity.purifyText(input) + "@gmail.com"); //By Removing space
                    } else if (swipeStep == 1) {
                        subject.setText(input);
                    } else {
                        messageBody.add(input + ".");
                        String txt = "";
                        for (int i = 0; i < messageBody.size(); i++) {
                            txt += messageBody.get(i); //prints element i
                        }
                        String previous = etMessage.getText().toString().trim();
                        previous+=txt;
                        etMessage.setText(previous);
                    }
                    if (containsWords(search, new String[]{"inbox"})) {
                        Intent intent = new Intent(messageActivity.this, messageInbox.class);
                        startActivity(intent);
                    } else if (containsWords(search, new String[]{"logout"}) || containsWords(search, new String[]{"log out"})) {

                        Intent intent = new Intent(messageActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                return;
            case REQ_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String editText_input;
                    String match = result.get(0).toLowerCase();
                    StringBuilder stringBuilder;
                    if (swipeStep == 0) {
                        editText_input = to.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("dot")) {
                            stringBuilder.setCharAt(index + 1, '.');
                        } else if (match.equalsIgnoreCase("at the rate")) {
                            stringBuilder.setCharAt(index + 1, '@');
                        } else {
                            stringBuilder.insert(index + 1, match.charAt(0));

                        }
                        to.setText(stringBuilder);
                    } else if (swipeStep == 1) {
                        editText_input = subject.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);

                        stringBuilder.insert(index + 1, match.charAt(0));

                        subject.setText(stringBuilder);
                        input = String.valueOf(stringBuilder);
                    } else {
                        editText_input = etMessage.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);

                        stringBuilder.insert(index + 1, match.charAt(0));

                        etMessage.setText(stringBuilder);
                        input = String.valueOf(stringBuilder);
                    }
                }
                return;


            case REPLACE_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String editText_input;
                    StringBuilder stringBuilder;
                    String match = result.get(0).toLowerCase();
                    if (swipeStep == 0) {
                        editText_input = to.getText().toString().trim().toLowerCase();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("dot")) {
                            stringBuilder.setCharAt(index, '.');
                        } else if (match.equalsIgnoreCase("at the rate")) {
                            stringBuilder.setCharAt(index, '@');
                        } else {
                            stringBuilder.insert(index, match.charAt(0));
                        }
                        to.setText(stringBuilder);
                    } else if (swipeStep == 1) {
                        editText_input = subject.getText().toString().trim();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("capital")) {
                            match.replace("capital", "");
                            stringBuilder.setCharAt(index, match.toUpperCase().charAt(0));
                        } else {
                            stringBuilder.insert(index, match.charAt(0));
                        }
                        subject.setText(stringBuilder);
                        input = String.valueOf(stringBuilder);
                    } else {
                        editText_input = etMessage.getText().toString().trim();
                        stringBuilder = new StringBuilder(editText_input);
                        if (match.contains("capital")) {
                            match.replace("capital", "");
                            stringBuilder.setCharAt(index, match.toUpperCase().charAt(0));
                        } else {
                            stringBuilder.insert(index, match.charAt(0));
                        }
                        etMessage.setText(stringBuilder);
                        input = String.valueOf(stringBuilder);
                    }

                    index = 0;
                }
                return;
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
    public void onDestroy() {
        //Don't forget to shut down text to speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        Paper.book().destroy();
        super.onDestroy();
    }

    class SendMail extends AsyncTask<Message, String, String> {

        //init Progress Dialog
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(messageActivity.this, "Please Wait", "Activation", true, false);
        }

        @Override
        protected String doInBackground(Message... messages) {

            try {
                //When Success
                Log.i("issue", String.valueOf(messages[0]));
                Transport.send(messages[0]);

                //Saving sent message to firebase account
                //It will help blinds to see what they have sent.


                return "Success";
            } catch (MessagingException e) {
                Log.i("issue", e.toString());
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
                StoreSentMessageOfUser();
                to.setText("");
                subject.setText("");
                etMessage.setText("");
                Toast.makeText(messageActivity.this, "Sent Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(messageActivity.this, "Issue Occur", Toast.LENGTH_SHORT).show();
            }


        }

        private void StoreSentMessageOfUser() {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
            saveCurrentTime = currentTime.format(calendar.getTime());

            messageRandomKey = saveCurrentDate + saveCurrentTime;

            saveSentInfoToDB();

        }

        private void saveSentInfoToDB() {
            HashMap<String, Object> sentMessageMap = new HashMap<>();
            sentMessageMap.put("sent_id", messageRandomKey);
            sentMessageMap.put("date", saveCurrentDate);
            sentMessageMap.put("time", saveCurrentTime);
            sentMessageMap.put("to", sendTO);
            sentMessageMap.put("subject", sendSubject);
            sentMessageMap.put("message", sendMessage);

            String encodedLoggedInAccount = registerActivity.encodeUserEmail(fromEmail);

            sentMessageRef.child(encodedLoggedInAccount).child(messageRandomKey).updateChildren(sentMessageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(messageActivity.this, "Message is stored in DB ALSO SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(messageActivity.this, "Issue Occur while saving sent message to DB", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

}