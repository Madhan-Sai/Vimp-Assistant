package com.example.hp.voicerecognition;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class CallActivity extends AppCompatActivity {

    TextToSpeech t1;
    public static final int Code = 900;
    public String num;
    int PERMISSIONS_REQUEST_READ_CONTACTS=100;
    boolean multiplenum=false;
    public String name;

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        num = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        promptSpeechInput();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //TODO
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isNumeric(String string) {
        String regxp="^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[789]\\d{9}$";
        if(! string.matches(regxp))
            return false;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) < '0' || string.charAt(i) > '9')
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
            Toast.makeText(CallActivity.this, "Sorry This doesn't work for your device", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public String getPhoneNumber(String name, Context context, int count) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like '%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if(c.getCount() > 1 && count==0) return "ask num";
        if(count==0){
            if (c.moveToFirst()) {
                ret = c.getString(0);
            }
        }
        else
        {
            c.move(count);
            ret=c.getString(0);
        }
        c.close();
        if(ret==null)
            ret = "Unsaved";
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onActivityResult(int req_code, int result_stat, Intent i) {
        super.onActivityResult(req_code, result_stat, i);
        if (result_stat == RESULT_OK && req_code == Code) {
            ArrayList<String> res = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result = res.get(0);
            if (result.toLowerCase().contains("cancel")) {
                Intent main = new Intent(this.getApplicationContext(), VoiceRecognizerActivity.class);
                startActivity(main);
            }
            else if (isNumeric(result.replaceAll("\\s+", ""))) {
                    num = result;
                    t1.speak("Calling", TextToSpeech.QUEUE_FLUSH, null);
                    while (t1.isSpeaking()) ;
                    num = "tel:" + num;
                    Intent call = new Intent(Intent.ACTION_CALL);
                    call.setData(Uri.parse(num));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        startActivity(call);
                        finish();
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            }
            else if(multiplenum){
                if(result.equals("to")) result="2";
                num=getPhoneNumber(name,getApplicationContext(),Integer.parseInt(result));
                t1.speak("Calling", TextToSpeech.QUEUE_FLUSH, null);
                while(t1.isSpeaking());
                num = "tel:" + num;
                Intent call = new Intent(Intent.ACTION_CALL);
                call.setData(Uri.parse(num));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    startActivity(call);
                    finish();
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            else if(result!=null && name==null){
                name=result;
                num=getPhoneNumber(name,getApplicationContext(),0);
                if(num.equals("Unsaved")){
                    t1.speak("No Contact exists.Try again",TextToSpeech.QUEUE_FLUSH,null);
                    while(t1.isSpeaking());
                    promptSpeechInput();
                }
                else if(! num.equals("ask num")) {
                    t1.speak("Calling", TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    num = "tel:" + num;
                    Intent call = new Intent(Intent.ACTION_CALL);
                    call.setData(Uri.parse(num));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        startActivity(call);
                        finish();
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    t1.speak("Multiple numbers found.Say which number",TextToSpeech.QUEUE_FLUSH,null);
                    while(t1.isSpeaking());
                    multiplenum=true;
                    promptSpeechInput();
                }
            }
            else if(! isNumeric(result.replaceAll("\\s+", ""))){
                t1.speak("Invalid number Try again", TextToSpeech.QUEUE_FLUSH, null);
                while(t1.isSpeaking());
                promptSpeechInput();
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
