package com.lionuncle.redirecting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_CODE = 111;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1035;
    private Switch forwardSwitchCall,forwardSwitchSms;
    private Button saveBtnCall,saveBtnSms;
    private EditText forwardToPhoneNumberTextCall,forwardToPhoneNumberTextSms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); forwardSwitchCall = findViewById(R.id.isForwardingSwitchCall);
        saveBtnCall = findViewById(R.id.SaveBtnCall);
        forwardToPhoneNumberTextCall = findViewById(R.id.PhoneNumberTextCall);
        forwardSwitchSms = findViewById(R.id.isForwardingSwitchSms);
        saveBtnSms = findViewById(R.id.saveBtnSms);
        forwardToPhoneNumberTextSms = findViewById(R.id.phoneNumberTextSms);
        checkAndRequestPermissions();
        requestCallPermission();

        String smsnumber = getSharedPreferences("data", Context.MODE_PRIVATE).getString("number", null);
        String callnumber = getSharedPreferences("data", Context.MODE_PRIVATE).getString("callnumber", null); //SETTING PHONE NUMBERS IN EDIT TEXT
        if (smsnumber != null){
            forwardToPhoneNumberTextSms.setText(smsnumber);
        }
        if (callnumber != null){
            forwardToPhoneNumberTextCall.setText(callnumber);
        }

        SharedPreferences sp = getSharedPreferences("check", MODE_PRIVATE);
        boolean isCall = sp.getBoolean("isCall", false);
        boolean isSms = sp.getBoolean("isSms", false);

        forwardSwitchCall.setChecked(isCall);
        forwardSwitchSms.setChecked(isSms);

        if (isCall){
            forwardToPhoneNumberTextCall.setVisibility(View.VISIBLE);
            saveBtnCall.setVisibility(View.VISIBLE);
        }else {
            forwardToPhoneNumberTextCall.setVisibility(View.GONE);
            saveBtnCall.setVisibility(View.GONE);
        }
        if (isSms){
            forwardToPhoneNumberTextSms.setVisibility(View.VISIBLE);
            saveBtnSms.setVisibility(View.VISIBLE);
        }
        else {
            forwardToPhoneNumberTextSms.setVisibility(View.GONE);
            saveBtnSms.setVisibility(View.GONE);
        }

        saveBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestCallPermission();
                    return;
                }
                if (forwardToPhoneNumberTextCall.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Please provide phone number to forward calls", Toast.LENGTH_SHORT).show();
                    return;
                }
                String callnumber = forwardToPhoneNumberTextCall.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                editor.putString("callnumber", callnumber);
                editor.commit();
                callforward("*21*"+callnumber+"#");
            }
        });
        forwardSwitchCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestCallPermission();
                    return;
                }
                if (isChecked){
                    forwardToPhoneNumberTextCall.setVisibility(View.VISIBLE);
                    saveBtnCall.setVisibility(View.VISIBLE);
                }
                else {
                    forwardToPhoneNumberTextCall.setVisibility(View.GONE);
                    saveBtnCall.setVisibility(View.GONE);
                    callforward("#21#");
                }
                SharedPreferences sp =getSharedPreferences("check", MODE_PRIVATE);
                SharedPreferences.Editor et = sp.edit();
                et.putBoolean("isCall", isChecked);
                et.commit();
            }
        });
        saveBtnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkAndRequestPermissions()){
                    return;
                }
                if (forwardToPhoneNumberTextSms.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Please provide Phone number to forward SMS to", Toast.LENGTH_SHORT).show();
                    return;
                }
                String number = forwardToPhoneNumberTextSms.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                editor.putString("number", number);
                editor.commit();
                Toast.makeText(MainActivity.this, "SMS forwarding successfully set!", Toast.LENGTH_SHORT).show();
            }
        });

        forwardSwitchSms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(!checkAndRequestPermissions()){
//                    return;
//                }
                if (isChecked){
                    forwardToPhoneNumberTextSms.setVisibility(View.VISIBLE);
                    saveBtnSms.setVisibility(View.VISIBLE);
                }
                else {
                    forwardToPhoneNumberTextSms.setVisibility(View.GONE);
                    saveBtnSms.setVisibility(View.GONE);
                }
                SharedPreferences sp =getSharedPreferences("check", MODE_PRIVATE);
                SharedPreferences.Editor et = sp.edit();
                et.putBoolean("isSms", isChecked);
                et.commit();
            }
        });

    }

    private void callforward(String callForwardString)
    {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Please provide permissions", Toast.LENGTH_SHORT).show();
            requestCallPermission();
            return;
        }


        PhoneCallListener phoneListener = new PhoneCallListener(MainActivity.this);
        TelephonyManager telephonyManager = (TelephonyManager)
                this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        Intent intentCallForward = new Intent(Intent.ACTION_CALL);
        Uri mmiCode = Uri.fromParts("tel", callForwardString, "#");
        intentCallForward.setData(mmiCode);
        startActivity(intentCallForward);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALL_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void requestCallPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
        }

    }//reqCall
    private boolean checkAndRequestPermissions()
    {
        int sms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if (sms != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}