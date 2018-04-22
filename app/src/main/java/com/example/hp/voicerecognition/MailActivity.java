package com.example.hp.voicerecognition;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class MailActivity extends AppCompatActivity {

    TextToSpeech t1;
    public static final int Code = 900;
    public String to,sub,msg;
    public boolean isto,isSub,isMsg;

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        to=null;
        sub=null;
        msg=null;
        promptSpeechInput("Say to e-mail address");
    }

    public void promptSpeechInput(String say) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the command!");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,Long.valueOf(10000));

        isto=say.contains("to");
        isSub=say.contains("subject");
        isMsg=say.contains("message");
        try {
            startActivityForResult(i, Code);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MailActivity.this, "Sorry This doesn't work for your device", Toast.LENGTH_LONG).show();
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
            else if(isto){
                if(result.contains("@") && result.contains(".")){
                    to=result;
                    t1.speak("Say Subject",TextToSpeech.QUEUE_FLUSH,null);
                    while(t1.isSpeaking());
                    promptSpeechInput("say subject");
                }
                else{
                    t1.speak("Invalid mail address Try again",TextToSpeech.QUEUE_FLUSH,null);
                    while(t1.isSpeaking());
                    promptSpeechInput("Say to e-mail address");
                }
            }
            else if(isSub){
                sub=result;
                t1.speak("Say Message",TextToSpeech.QUEUE_FLUSH,null);
                while(t1.isSpeaking());
                promptSpeechInput("Say message");
            }
            else if(isMsg){
                msg=result;
                Intent emailIntent=new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                to=to.replace(" ","");
                msg=msg.replace("dot","");
                emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[] {to});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,sub);
                emailIntent.putExtra(Intent.EXTRA_TEXT,msg);
                startActivity(emailIntent);
                t1.speak("E-Mail sent successfully",TextToSpeech.QUEUE_FLUSH,null);
                while(t1.isSpeaking());
            }
            else if(result.equals("close") || result.equals("exit")){
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
