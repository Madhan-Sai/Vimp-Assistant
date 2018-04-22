package com.example.hp.voicerecognition;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class SMSActivity extends AppCompatActivity {

    TextToSpeech t1;
    public static final int Code = 900;
    public String msg;
    public String num;

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        msg = null;
        num = null;
        promptSpeechInput();
    }
    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    private void sendSMS() {
        msg=msg.replace("dot","");
        SmsManager sms=SmsManager.getDefault();
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
        sms.sendTextMessage(num,null,msg,sentIntent,deliveredIntent);
        t1.speak("Message sent successfully", TextToSpeech.QUEUE_FLUSH, null);
        while(t1.isSpeaking());
        Intent main=new Intent(this.getApplicationContext(),VoiceRecognizerActivity.class);
        startActivity(main);
    }
    public boolean isNumeric(String string) {
        String regxp="^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[789]\\d{9}$";
        if(! string.matches(regxp))
            return false;
        for(int i=0;i<string.length();i++)
        {
            if(string.charAt(i)<'0' || string.charAt(i)>'9')
                return false;
        }
        return true;
    }
    public void promptSpeechInput() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the command!");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,Long.valueOf(10000));

        try {
            startActivityForResult(i, Code);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(SMSActivity.this, "Sorry This doesn't work for your device", Toast.LENGTH_LONG).show();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onActivityResult(int req_code, int result_stat, Intent i) {
        super.onActivityResult(req_code, result_stat, i);
        if (result_stat == RESULT_OK && req_code == Code) {
            ArrayList<String> res = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result = res.get(0);
            if(result.toLowerCase().contains("cancel")){
                Intent main=new Intent(this.getApplicationContext(),VoiceRecognizerActivity.class);
                startActivity(main);
            }
            else if (isNumeric(result.replaceAll("\\s+", ""))) {
                num = result.replaceAll("\\s+", "");
                if(msg==null) {
                    t1.speak("number received.Say the Message to proceed", TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    Toast.makeText(getApplicationContext(), num, Toast.LENGTH_SHORT).show();

                        promptSpeechInput();
                }
                if(num!=null && msg!=null)
                    sendSMS();
            }
            else if(result.length()>1 && msg==null) {
                msg=result;
                if(num==null)
                {
                    t1.speak("Message received.Say Contact number to proceed", TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    promptSpeechInput();
                }
                if(num!=null && msg!=null)
                    sendSMS();
            }
            else if(! isNumeric(result.replaceAll("\\s+", ""))){
                t1.speak("Invalid number Try again", TextToSpeech.QUEUE_FLUSH, null);
                while(t1.isSpeaking());
                promptSpeechInput();
            }
            else if(result.toLowerCase().contains("cancel")){
                Intent got = new Intent(SMSActivity.this,VoiceRecognizerActivity.class);
                startActivity(got);
            }
            else if(result.contains("close") || result.contains("exit")){
                finishAffinity();
                System.exit(0);
            }
        }
    }

    protected void onRestart() {
        super.onRestart();
        Intent main=new Intent(this.getApplicationContext(),VoiceRecognizerActivity.class);
        startActivity(main);
    }
}
