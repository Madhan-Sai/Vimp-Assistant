package com.example.hp.voicerecognition;

import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class VoiceRecognizerActivity extends AppCompatActivity {

    TextToSpeech t1;
    public static final int Code=900;
    public AudioManager audio;
    private BluetoothAdapter ba;
    private InputStream f;
    private String com;
    Database dbi;
    private Map<String,String> commands=new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vimp);
        dbi=new Database(this);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        audio= (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        ba=BluetoothAdapter.getDefaultAdapter();
            f=this.getResources().openRawResource(R.raw.data);
            Scanner sc=new Scanner(f);
            while(sc.hasNextLine()){
                boolean first=true,second=true;
                String line=sc.nextLine();
                String key=null,value=null,function=null;
                StringTokenizer stk=new StringTokenizer(line,":");
                while(stk.hasMoreTokens()){
                    if(first) {
                        key = stk.nextToken();
                        first=false;
                    }
                    else if(second){
                        value=stk.nextToken();
                        second=false;
                    }
                    else
                        function=stk.nextToken();
                }
                commands.put(key,value);
                dbi.add(key,value,function);
            }
        promptSpeechInput();
    }

    public void promptSpeechInput()
    {
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE , Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say the command!");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,Long.valueOf(10000));

        try {
            startActivityForResult(i, Code);
        }
        catch(ActivityNotFoundException a)
        {
            Toast.makeText(VoiceRecognizerActivity.this,"Sorry This doesn't work for your device",Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onActivityResult(int req_code, int result_stat, Intent i)
    {
        super.onActivityResult(req_code,result_stat,i);
                if(result_stat == RESULT_OK  && req_code==Code){
                    ArrayList<String> res=i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(res.get(0).trim().toLowerCase().contains("time"))
                    {
                        Date d=new Date();
                        String hrs=String.valueOf(d.getHours()>12?d.getHours()-12:d.getHours());
                        String mins=String.valueOf(d.getMinutes());
                        String ampm=d.getHours()>12?"PM":"AM";
                        t1.speak("The time is "+hrs+" "+mins+" "+ampm, TextToSpeech.QUEUE_FLUSH, null);
                        while(t1.isSpeaking());
                        promptSpeechInput();
                    }
                    Set<String> arr=commands.keySet();
                    Iterator<String> itr=arr.iterator();
                    boolean yes=false;
                    while(itr.hasNext()) {
                        com=itr.next();
                        if (res.get(0).contains(com)) {
                            t1.speak(commands.get(com), TextToSpeech.QUEUE_FLUSH, null);
                            while (t1.isSpeaking()) ;
                            yes=true;
                            try {
                                if (dbi.getFunction(com).equals("promptSpeechInput")) {

                                    Method instance = VoiceRecognizerActivity.class.getDeclaredMethod(dbi.getFunction(com));
                                    instance.invoke(this);

                                    break;
                                }
                                else {
                                        Method instance = VoiceRecognizerActivity.class.getDeclaredMethod("moveIntent", String.class);
                                        instance.invoke(this, dbi.getFunction(com));
                                    break;
                                }
                            }
                            catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    if(res.get(0).toLowerCase().contains("silent")){
                        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        promptSpeechInput();
                    }
                    else if(res.get(0).toLowerCase().contains("vibrate")){
                        audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        promptSpeechInput();
                    }
                    else if(res.get(0).toLowerCase().contains("normal")){
                        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        promptSpeechInput();
                    }
                    else if(res.get(0).toLowerCase().contains("volume")){
                        if(res.get(0).toLowerCase().contains("increase")){
                            audio.adjustVolume(AudioManager.ADJUST_RAISE,AudioManager.FLAG_PLAY_SOUND);
                        }
                        else if(res.get(0).toLowerCase().contains("decrease")){
                            audio.adjustVolume(AudioManager.ADJUST_LOWER,AudioManager.FLAG_PLAY_SOUND);
                        }
                        promptSpeechInput();
                    }
                    else if(res.get(0).toLowerCase().contains("bluetooth")){
                        if(res.get(0).toLowerCase().contains("on")){
                            if(! ba.isEnabled()){
                            Intent bluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(bluetooth,0);
                                t1.speak("Bluetooth turned on", TextToSpeech.QUEUE_FLUSH, null);
                                while(t1.isSpeaking());
                            }
                            else {
                                t1.speak("Bluetooth is already on", TextToSpeech.QUEUE_FLUSH, null);
                                while(t1.isSpeaking());
                            }
                        }
                        else if(res.get(0).toLowerCase().contains("off")){
                            ba.disable();
                            t1.speak("Bluetooth turned off", TextToSpeech.QUEUE_FLUSH, null);
                            while(t1.isSpeaking());
                        }
                        promptSpeechInput();
                    }
                    else if(res.get(0).toLowerCase().contains("exit") || res.get(0).toLowerCase().contains("close")){
                        finishAffinity();
                        System.exit(0);
                    }
                    else if(! yes){
                        t1.speak("Invalid command. Try again",TextToSpeech.QUEUE_FLUSH,null);
                        while(t1.isSpeaking());
                        promptSpeechInput();
                    }

                }
    }
    public void moveIntent(String in){
        Intent im;
        switch(in){
            case "AddToContacts": im=new Intent(this,AddToContacts.class);
                break;
            case "SMSActivity": im=new Intent(this,SMSActivity.class);
                break;
            case "AlarmActivity": im=new Intent(this,AlarmActivity.class);
                break;
            case "CallActivity" : im=new Intent(this,CallActivity.class);
                break;
            case "MailActivity" :im=new Intent(this,MailActivity.class);
                break;
            default : im=null;
        }
        if(im!=null)
            startActivity(im);
    }
    protected void onRestart() {
        super.onRestart();
        Intent main=new Intent(this.getApplicationContext(),VoiceRecognizerActivity.class);
        startActivity(main);
    }
}
