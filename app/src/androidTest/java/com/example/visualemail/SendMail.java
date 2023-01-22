package com.example.visualemail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.Toast;

import javax.mail.Message;

import javax.mail.MessagingException;
import javax.mail.Transport;

public class SendMail extends AsyncTask<Message, String, String> {
    //init Progress Dialog
    private ProgressDialog progressDialog;
    private Activity activity;

    public SendMail(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(activity, "Please Wait", "Activation", true, false);
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
            //When Success

            //Init AlertBuilder
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
        }else{
            Toast.makeText(activity, "Issue Occur", Toast.LENGTH_SHORT).show();
        }


    }
}
