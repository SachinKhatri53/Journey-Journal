package com.ismt.journeyjournal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.ismt.journeyjournal.model.JourneyModel;

import java.util.ArrayList;


public class JourneyDetailActivity extends AppCompatActivity {

    AppCompatImageButton btnBackJourneyDetail, btnDeleteJourney, btnEditJourney, btnShare;

    AppCompatImageView imageViewPicture;
    AppCompatTextView textViewJourneyName, textViewJourneyDate, textViewDescription, textViewLocation;
    Dialog mapDialog;
    static String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_detail);

        btnDeleteJourney = findViewById(R.id.btnDeleteJourney);
        btnEditJourney = findViewById(R.id.btnEditJourney);
        btnBackJourneyDetail = findViewById(R.id.btnBackJourneyDetail);
        imageViewPicture = findViewById(R.id.imageViewPicture);
        textViewJourneyName = findViewById(R.id.textViewJourneyName);
        textViewJourneyDate = findViewById(R.id.textViewJourneyDate);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewLocation = findViewById(R.id.textViewLocation);
        btnShare = findViewById(R.id.btnShare);

imageName = getIntent().getStringExtra("image");
        textViewJourneyName.setText(getIntent().getStringExtra("title"));
        textViewDescription.setText(getIntent().getStringExtra("description"));
        textViewJourneyDate.setText(getIntent().getStringExtra("date"));
        textViewLocation.setText(getIntent().getStringExtra("location"));
        Glide.with(imageViewPicture.getContext()).load(getIntent().getStringExtra("image")).into(imageViewPicture);
        btnDeleteJourney.setOnClickListener(view -> {
            showDeleteDialog();
        });

        btnEditJourney.setOnClickListener(view -> {
            startActivity(new Intent(this, EditJourneyActivity.class)
                    .putExtra("title", textViewJourneyName.getText())
                    .putExtra("description", textViewDescription.getText())
                    .putExtra("date", textViewJourneyDate.getText())
                    .putExtra("location", textViewLocation.getText())
                    .putExtra("image", getIntent().getStringExtra("image"))
                    .putExtra("position", getIntent().getStringExtra("position"))
                    .putExtra("findLatitude", getIntent().getStringExtra("findLatitude"))
                    .putExtra("findLongitude", getIntent().getStringExtra("findLongitude"))
            );
            finish();
        });

        btnBackJourneyDetail.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class)
            .putExtra("uid", MainActivity.uid));
            finish();
        });


        mapDialog = new Dialog(this);

        mapDialog.setContentView(R.layout.map_dialog);
        mapDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mapDialog.setCancelable(true);


        //Share Button

        btnShare.setOnClickListener(view -> {
            String body = getIntent().getStringExtra("title") + "\n" +
                    getIntent().getStringExtra("date") + "\n" + "\n" +
                    "Location:" + "\n" +
                    getIntent().getStringExtra("location") + "\n" + "\n" +
                    "Description:" + "\n" +
                    getIntent().getStringExtra("description") + "\n"+ "\n" +
                    imageName;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });

    }

    private void showDeleteDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase.getInstance().getReference("JourneyLists").child(MainActivity.uid)
                                .child(getIntent().getStringExtra("position")).removeValue();
                        startActivity(new Intent(JourneyDetailActivity.this, MainActivity.class)
                        .putExtra("uid", MainActivity.uid));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(JourneyDetailActivity.this, MainActivity.class)
        .putExtra("uid", MainActivity.uid));
        finish();
    }
}