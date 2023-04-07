package com.ismt.journeyjournal;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ismt.journeyjournal.model.UserModel;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;

    FirebaseAuth fAuth;
    TextInputEditText editTextLoginUsername, editTextLoginPassword;
    Button btnLogin;
    TextInputLayout username, password;
    SharedPreferences sharedPreferences;
    final String emptyMessage="required*";
    TextView linkRegistration;
    AppCompatCheckBox checkBoxRemember;
    ProgressDialog progressDialog;
    SignInButton btnGoogleSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextLoginUsername = findViewById(R.id.editTextLoginUsername);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        linkRegistration = findViewById(R.id.linkRegistration);
        checkBoxRemember = findViewById(R.id.checkBoxRemember);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        fAuth = FirebaseAuth.getInstance();

        linkRegistration.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        btnLogin.setOnClickListener(view -> {

            if(isNetworkAvailable()){
                login();
            }
            else{
                Toast.makeText(this, "Check your connection.", Toast.LENGTH_SHORT).show();
            }

        });

        editTextLoginUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                username.setHint("Email");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextLoginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password.setHint("Password");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void login(){
        if(validation()){

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            String email = editTextLoginUsername.getText().toString().trim();
            String password = editTextLoginPassword.getText().toString().trim();



            fAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                if (checkBoxRemember.isChecked()) {
                                    savePreferences();
                                }
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                .putExtra("uid", fAuth.getUid()));
                                finish();
                            }
                        }
                    })
            .addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validation() {
        if (editTextLoginUsername.getText().toString().isEmpty()){
            username.setHint(emptyMessage);
            username.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if (editTextLoginPassword.getText().toString().isEmpty()){
            password.setHint(emptyMessage);
            password.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        return true;
    }

    //Network Status Check
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void savePreferences(){
        sharedPreferences = getSharedPreferences("logged_in_user", MODE_PRIVATE);
        SharedPreferences.Editor editPreferences = sharedPreferences.edit();
        editPreferences.putBoolean("login_counter", true)
        .putString("user_id", fAuth.getUid());
        editPreferences.apply();
    }


}