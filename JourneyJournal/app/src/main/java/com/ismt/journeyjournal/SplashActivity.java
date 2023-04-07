package com.ismt.journeyjournal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(isNetworkAvailable()){
            SharedPreferences sharedPreferences = getSharedPreferences("logged_in_user", MODE_PRIVATE);
            String uid = sharedPreferences.getString("user_id", String.valueOf(MODE_PRIVATE));
            boolean check = sharedPreferences.getBoolean("login_counter", Boolean.valueOf(String.valueOf(MODE_PRIVATE)));

            new Handler().postDelayed(() -> {
                if (check){
                    startActivity(new Intent(this, MainActivity.class).putExtra("uid", uid));
                }
                else{
                    startActivity(new Intent(this, LoginActivity.class));
                }
                finish();
            }, 3000);
        }
        else{
            new Handler().postDelayed(()->{
                Toast.makeText(this, "Check Your Connection", Toast.LENGTH_SHORT).show();
                finish();
            }, 2500);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}