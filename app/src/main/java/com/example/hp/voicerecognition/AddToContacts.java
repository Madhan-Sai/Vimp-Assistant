package com.example.hp.voicerecognition;


import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.os.Build;
import  android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class AddToContacts extends AppCompatActivity {
    TextToSpeech t1;
    public static final int Code = 900;
    public String name;
    public String num;

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_contacts);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        name = null;
        num = null;
        promptSpeechInput();
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private void addToContacts() {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, num)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try
        {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            t1.speak(getResources().getString(R.string.contact_success) , TextToSpeech.QUEUE_FLUSH, null);
            while(t1.isSpeaking());
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        while(t1.isSpeaking());
        Intent main=new Intent(this.getApplicationContext(),VoiceRecognizerActivity.class);
        startActivity(main);
    }

    public boolean isNumeric(String string) {
        String regxp="^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[6789]\\d{9}$";
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
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.say_command));
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,Long.valueOf(10000));

        try {
            startActivityForResult(i, Code);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(AddToContacts.this, getResources().getString(R.string.sorry), Toast.LENGTH_LONG).show();
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
                num = result;
                if(name==null) {
                    t1.speak("number received.Say Contact name to proceed", TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    promptSpeechInput();
                }
                if(num!=null && name!=null)
                    addToContacts();
            }
            else if(result.length()>1 && name==null)
            {
                name=result;
                if(num==null)
                {
                    t1.speak("name received.Say Contact number to proceed", TextToSpeech.QUEUE_FLUSH, null);
                    while(t1.isSpeaking());
                    promptSpeechInput();
                }
                if(num!=null && name!=null)
                    addToContacts();
            }
            else if((!isNumeric(result.replaceAll("\\s+", "")))){
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

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent main=new Intent(this.getApplicationContext(),VoiceRecognizerActivity.class);
        startActivity(main);
    }
}
