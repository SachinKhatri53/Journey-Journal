package com.ismt.journeyjournal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ismt.journeyjournal.model.UserModel;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword;
    Button btnRegister;
    AppCompatImageButton btnBack;
    TextView linkLogin;
    TextInputLayout textViewUsername, textViewEmail, textViewPassword, textViewConfirmPassword;
    final String emptyMessage = "required*";
    AlertDialog.Builder alertDialogBuilder;
    ProgressDialog progressDialog;

    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnBack = findViewById(R.id.btnBack);
        btnRegister = findViewById(R.id.btnRegister);
        linkLogin = findViewById(R.id.linkLogin);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPassword = findViewById(R.id.textViewPassword);
        textViewConfirmPassword = findViewById(R.id.textViewConfirmPassword);


        btnBack.setOnClickListener(view -> {
            redirectToLogin();
        });

        linkLogin.setOnClickListener(view -> {
            redirectToLogin();
        });


        btnRegister.setOnClickListener(view -> {
            if (checkValidation()){
                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                signUp();
            }
        });
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textViewEmail.setHint("Email");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textViewUsername.setHint("Username");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textViewPassword.setHint("Password");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textViewConfirmPassword.setHint("Confirm Password");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //Redirect to Login
    public void redirectToLogin(){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    //Validation Check
    public boolean checkValidation(){

        if(editTextPassword.getText().toString().length()<=5){
            editTextPassword.setText("");
            textViewPassword.setHint("Password should be more than 5 digits");
            textViewPassword.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextConfirmPassword.getText().toString().length()<=5){
            editTextConfirmPassword.setText("");
            textViewConfirmPassword.setHint("Password should be more than 5 digits");
            textViewConfirmPassword.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextUsername.getText().toString().isEmpty()){
            textViewUsername.setHint(emptyMessage);
            textViewUsername.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextEmail.getText().toString().isEmpty()){
            textViewEmail.setHint(emptyMessage);
            textViewEmail.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextPassword.getText().toString().isEmpty()){
            textViewPassword.setHint(emptyMessage);
            textViewPassword.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextConfirmPassword.getText().toString().isEmpty()){
            textViewConfirmPassword.setHint(emptyMessage);
            textViewConfirmPassword.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if (!editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())){
            editTextPassword.setText("");
            editTextConfirmPassword.setText("");
            textViewPassword.setHint("Does not match");
            textViewConfirmPassword.setHint("Does not match");
            textViewConfirmPassword.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        return true;
    }


    //User SignUp

    public void signUp(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();

        fAuth = FirebaseAuth.getInstance();

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            UserModel userModel = new UserModel(username, email, password, fAuth.getUid());
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference node = firebaseDatabase.getReference("RegisteredUsers");
                            node.push().setValue(userModel);
                            progressDialog.dismiss();
                            alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle("Congratulations")
                                    .setMessage("New user registered successfully.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                        else{
                            progressDialog.dismiss();
                            checkEmail();
                        }
                    }
                });
    }
    public void checkEmail(){
        fAuth.fetchSignInMethodsForEmail(editTextEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if(!task.getResult().getSignInMethods().isEmpty()){
                            alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("Registration Failed")
                            .setMessage("Email already registered. Try with new email")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }
                });
    }
}