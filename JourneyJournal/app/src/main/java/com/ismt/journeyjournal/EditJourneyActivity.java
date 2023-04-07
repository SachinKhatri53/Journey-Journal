package com.ismt.journeyjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class EditJourneyActivity extends AppCompatActivity {

    TextInputEditText editTextTitle, editTextDescription, editTextDate, editTextLocation;
    TextInputLayout labelLocation, labelTitle, labelDescription, labelDate;
    AppCompatButton btnUpdate;
    AppCompatImageView imageViewThumbnail;
    AlertDialog.Builder alertDialogBuilder;
    AppCompatImageButton btnDatePicker, btnCamera, btnLocation, btnBackEditJourney;
    final String emptyMessage = "required*";
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    Dialog mapDialog;
    Uri filepath;
    Bitmap bitmap;
    Double findLatitude, findLongitude;

    ProgressDialog imageProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journey);

        btnBackEditJourney = findViewById(R.id.btnBackEditJourney);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextLocation = findViewById(R.id.editTextLocation);
        labelTitle= findViewById(R.id.labelTitle);
        labelDescription = findViewById(R.id.labelDescription);
        labelDate = findViewById(R.id.labelDate);
        labelLocation = findViewById(R.id.labelLocation);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnCamera = findViewById(R.id.btnCamera);
        btnLocation = findViewById(R.id.btnLocation);
        imageViewThumbnail = findViewById(R.id.imageViewThumbnail);



        editTextDate.setEnabled(false);
        editTextLocation.setEnabled(false);
        findLatitude = Double.valueOf(getIntent().getStringExtra("findLatitude"));
        findLongitude = Double.valueOf(getIntent().getStringExtra("findLongitude"));
        editTextTitle.setText(getIntent().getStringExtra("title"));
        editTextDescription.setText(getIntent().getStringExtra("description"));
        editTextDate.setText(getIntent().getStringExtra("date"));
        editTextLocation.setText(getIntent().getStringExtra("location"));
        Glide.with(imageViewThumbnail.getContext()).load(JourneyDetailActivity.imageName).into(imageViewThumbnail);
        //Calendar Date Picker
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                int year, month, day;
                year = datePicker.getYear();
                month = datePicker.getMonth();
                day = datePicker.getDayOfMonth();
                editTextDate.setText((month+1)+"/"+day+"/"+year);
            }
        }, year, month, day);

        btnDatePicker.setOnClickListener(view -> {
            dialog.show();
        });

        btnBackEditJourney.setOnClickListener(view -> {
            startActivity(new Intent(this, JourneyDetailActivity.class)
            .putExtra("uid", MainActivity.uid)
            .putExtra("title", getIntent().getStringExtra("title"))
            .putExtra("description", getIntent().getStringExtra("description"))
            .putExtra("image", JourneyDetailActivity.imageName)
            .putExtra("date", getIntent().getStringExtra("date"))
            .putExtra("location", getIntent().getStringExtra("location")));
            finish();
        });

        btnUpdate.setOnClickListener(view -> {
            if (validation()){
                updateImageAndJournalFirebase();
            }

        });


        mapDialog = new Dialog(this);
        mapDialog.setContentView(R.layout.map_dialog);
        mapDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mapDialog.setCancelable(true);
        btnLocation.setOnClickListener(view -> {
            supportMapFragment = (SupportMapFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragMap);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            Dexter.withContext(getApplicationContext())
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            getMyLocation();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
            mapDialog.show();
        });

        btnCamera.setOnClickListener(view -> {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Image File"), 1);
                        }
                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        }
                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        });

        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                labelTitle.setHint("Title");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                labelDescription.setHint("Description");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editTextDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                labelDate.setHint("Date");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                labelLocation.setHint("Location");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    private boolean validation() {
        if(editTextTitle.getText().toString().isEmpty()){
            labelTitle.setHint(emptyMessage);
            labelTitle.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextDescription.getText().toString().isEmpty()){
            labelDescription.setHint(emptyMessage);
            labelDescription.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextDate.getText().toString().isEmpty()){
            labelDate.setHint(emptyMessage);
            labelDate.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        if(editTextLocation.getText().toString().isEmpty()){
            labelLocation.setHint(emptyMessage);
            labelLocation.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.maroon)));
            return false;
        }
        return true;
    }
    private void getMyLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng latLng = new LatLng(findLatitude, findLongitude);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        googleMap.addMarker(markerOptions);
                        markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(findLatitude, findLongitude, 1);
                            String cityName = addresses.get(0).getAddressLine(0);
                            String stateName = addresses.get(0).getAddressLine(1);
                            String countryName = addresses.get(0).getAddressLine(2);
                            editTextLocation.setText(cityName);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
//
//
//                                currentLocation = latLng;
                                markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                                googleMap.clear();
                                googleMap.addMarker(new MarkerOptions().position(latLng));

                                findLatitude = location.getLatitude();
                                findLongitude = location.getLongitude();

                                try {
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                    String cityName = addresses.get(0).getAddressLine(0);
                                    String stateName = addresses.get(0).getAddressLine(1);
                                    String countryName = addresses.get(0).getAddressLine(2);
                                    editTextLocation.setText(cityName);
                                }
                                catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

            }
        });
    }

    public void updateImageAndJournalFirebase(){

        imageProgressDialog = new ProgressDialog(this);
        imageProgressDialog.setTitle("Image Uploader");
        imageProgressDialog.show();

       FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("JourneyLists").child(MainActivity.uid+new Random().nextInt(50));
        uploader.putFile(filepath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String title = editTextTitle.getText().toString();
                                title = title.substring(0, 1).toLowerCase() + title.substring(1);
                                Map<String,Object> map = new HashMap<>();
                                map.put("title", title);
                                map.put("description", editTextDescription.getText().toString());
                                map.put("date", editTextDate.getText().toString());
                                map.put("location", editTextLocation.getText().toString());
                                map.put("image", uri.toString());
                                map.put("findLatitude", findLatitude.toString());
                                map.put("findLongitude", findLongitude.toString());

                                FirebaseDatabase.getInstance().getReference().child("JourneyLists")
                                        .child(MainActivity.uid).child(getIntent().getStringExtra("position")).updateChildren(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                imageProgressDialog.dismiss();

                                                alertDialogBuilder = new AlertDialog.Builder(EditJourneyActivity.this)
                                                        .setTitle("Congratulations")
                                                        .setMessage("Journal updated successfully.")
                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
//                                                                dialogInterface.cancel();
                                                                startActivity(new Intent(EditJourneyActivity.this, JourneyDetailActivity.class)
                                                        .putExtra("title", editTextTitle.getText().toString())
                                                        .putExtra("description", editTextDescription.getText().toString())
                                                        .putExtra("date", editTextDate.getText().toString())
                                                        .putExtra("location", editTextLocation.getText().toString())
                                                        .putExtra("image", uri.toString())
                                                                .putExtra("uid", MainActivity.uid));
                                                                finish();
                                                            }
                                                        });
                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                imageProgressDialog.dismiss();
                                                Toast.makeText(EditJourneyActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float percent = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                imageProgressDialog.setMessage("Uploading: "+ (int)percent+ "%...\n" + "Please wait.");
                imageProgressDialog.setCancelable(false);
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(data.getData() != null){
            filepath = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageViewThumbnail.setImageBitmap(bitmap);

            }
            catch (Exception e) {}
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity( new Intent(EditJourneyActivity.this, JourneyDetailActivity.class)
        .putExtra("uid", MainActivity.uid)
        .putExtra("title", getIntent().getStringExtra("title"))
        .putExtra("description", getIntent().getStringExtra("description"))
        .putExtra("image", JourneyDetailActivity.imageName)
        .putExtra("date", getIntent().getStringExtra("date"))
        .putExtra("location", getIntent().getStringExtra("location")));
        finish();
    }
}