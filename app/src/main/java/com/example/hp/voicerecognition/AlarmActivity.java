package com.example.hp.voicerecognition;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    TextToSpeech t1;
    public static final int Code = 900;
    public int hr;
    public int min;
    boolean ishr=false,ismin=false,isam=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        promptSpeechInput("Say time in Hours");
    }
    public void promptSpeechInput(String say) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.say_command));
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,Long.valueOf(10000));
        ishr=say.contains("Hours");
        ismin=say.contains("Minutes");
        isam=say.contains("AM");
        try {
            startActivityForResult(i, Code);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(AlarmActivity.this, getResources().getString(R.string.sorry), Toast.LENGTH_LONG).show();
        }
    }
    public boolean isNumeric(String string) {
        for(int i=0;i<string.length();i++)
        {
            if(string.charAt(i)<'0' || string.charAt(i)>'9')
                return false;
        }
        return true;
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
            else if(ishr){
                if(isNumeric(result)) {
                    hr=Integer.parseInt(result);
                    ishr=false;
                }
                else ishr=true;
                if(ishr){
                    t1.speak(getResources().getString(R.string.invalid_hrs), TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    promptSpeechInput(getResources().getString(R.string.invalid_hrs));
                }
                else {
                    t1.speak(getResources().getString(R.string.prompt_mins), TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    promptSpeechInput(getResources().getString(R.string.prompt_mins));
                }
            }
            else if(ismin){
                if(isNumeric(result)) {
                    min=Integer.parseInt(result);
                    ismin=false;
                }
                else ismin=true;
                if(ismin){
                    t1.speak(getResources().getString(R.string.invalid_mins), TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    promptSpeechInput(getResources().getString(R.string.invalid_mins));
                }
                else {
                    t1.speak(getResources().getString(R.string.am_pm), TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    promptSpeechInput(getResources().getString(R.string.am_pm));
                }
            }
            else if(isam){
                if(result.toLowerCase().equals("pm") && hr<=12){
                    hr+=12;
                }
                Intent alarm=new Intent(AlarmClock.ACTION_SET_ALARM);
                alarm.putExtra(AlarmClock.EXTRA_SKIP_UI,true);
                alarm.putExtra(AlarmClock.EXTRA_HOUR,hr);
                alarm.putExtra(AlarmClock.EXTRA_MINUTES,min);
                alarm.putExtra(AlarmClock.EXTRA_MESSAGE,getResources().getString(R.string.alarm_name));
                startActivity(alarm);

                t1.speak(getResources().getString(R.string.alarm_success), TextToSpeech.QUEUE_FLUSH, null);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent main=new Intent(this.getApplicationContext(),VoiceRecognizerActivity.class);
                startActivity(main);
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
