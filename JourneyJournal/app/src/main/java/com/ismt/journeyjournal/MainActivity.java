package com.ismt.journeyjournal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.ismt.journeyjournal.model.JourneyModel;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    AppCompatImageButton btnLogout;
    AppCompatEditText search;
    ExtendedFloatingActionButton btnAddJournal;
    AlertDialog.Builder alertDialogBuilder;
    RecyclerView recycler;
    AppCompatTextView textViewMessage;
    JourneyAdapter journeyAdapter;
    static String uid;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddJournal = findViewById(R.id.btnAddJournal);
        btnLogout = findViewById(R.id.btnLogout);
        recycler = findViewById(R.id.recycler);
        search = findViewById(R.id.search);
        textViewMessage = findViewById(R.id.textViewMessage);
        checkUserStatus();

        uid = getIntent().getStringExtra("uid");

        btnAddJournal.setOnClickListener(view -> {
            startActivity(new Intent(this, AddJourneyActivity.class)
                    .putExtra("uid", uid));
        });

        btnLogout.setOnClickListener(view -> {

            alertDialogBuilder = new AlertDialog.Builder(this).setTitle("Log Out")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sharedPreferences = getSharedPreferences("logged_in_user", MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean("login_counter", false).putString("user_id", String.valueOf(MODE_PRIVATE)).apply();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    dialogInterface.dismiss();
                    finish();
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        });


        //JOURNEY LISTS FROM FIREBASE
        recycler.setLayoutManager(new LinearLayoutManager(this));

        fetchDataFromFirebase();

//            if (journeyAdapter.getItemCount()<1){
//                textViewMessage.setText("Ops! You journal is empty.");
//            }

        //SEACRH JOURNEY TITLE
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    processSearch(s.toString().toLowerCase(Locale.ROOT));
                }
                catch (Exception e){
                    fetchDataFromFirebase();
                    }
                }
        });

    }

    @Override
    public void onBackPressed() {

        alertDialogBuilder = new AlertDialog.Builder(this).setTitle("Confirmation")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.super.onBackPressed();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
//FIREBASE RECYCLERVIEW
    @Override
    protected void onStart() {
        super.onStart();
        journeyAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        journeyAdapter.stopListening();
    }

    //SEARCH ITEMS IN RECYCLER VIEW
    private void processSearch(String s) {
        FirebaseRecyclerOptions<JourneyModel> options =
                new FirebaseRecyclerOptions.Builder<JourneyModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("JourneyLists").child(MainActivity.uid).orderByChild("title").startAt(s).endAt(s+"\uf8ff"), JourneyModel.class)
                        .build();

        journeyAdapter = new JourneyAdapter(options);
        journeyAdapter.startListening();
        recycler.setAdapter(journeyAdapter);
    }

    void checkUserStatus(){
        sharedPreferences = getSharedPreferences("logged_in_user", MODE_PRIVATE);
        sharedPreferences.getBoolean("login_counter", true);
        uid = sharedPreferences.getString("user_id", String.valueOf(MODE_PRIVATE));
    }
void fetchDataFromFirebase(){
    FirebaseRecyclerOptions<JourneyModel> options =
            new FirebaseRecyclerOptions.Builder<JourneyModel>()
                    .setQuery(FirebaseDatabase.getInstance().getReference().child("JourneyLists").child(uid), JourneyModel.class)
                    .build();
    journeyAdapter = new JourneyAdapter(options);
    recycler.setAdapter(journeyAdapter);
}

}