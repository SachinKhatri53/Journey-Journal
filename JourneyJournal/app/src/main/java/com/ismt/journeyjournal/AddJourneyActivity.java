package com.ismt.journeyjournal;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ismt.journeyjournal.model.JourneyModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AddJourneyActivity extends AppCompatActivity {
    AppCompatImageButton btnDatePicker, btnBackAddJourney, btnLocation, btnCamera;
    AppCompatButton btnAddJourney;
    TextInputEditText editTextDate, editTextTitle, editTextDescription, editTextLocation;
    TextInputLayout labelTitle, labelDescription, labelDate, labelLocation;
    AppCompatImageView imageViewThumbnail;
    final String emptyMessage = "required*";
    AlertDialog.Builder alertDialogBuilder;
    ProgressDialog imageProgressDialog;

    Dialog mapDialog;
    Uri filepath;
    Bitmap bitmap;
    String findLatitude, findLongitude;

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journey);
        //currentLocation.
        btnDatePicker = findViewById(R.id.btnDatePicker);
        editTextDate = findViewById(R.id.editTextDate);
        btnBackAddJourney = findViewById(R.id.btnBackAddJourney);
        btnAddJourney = findViewById(R.id.btnAddJourney);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLocation = findViewById(R.id.editTextLocation);
        labelTitle = findViewById(R.id.labelTitle);
        labelDescription = findViewById(R.id.labelDescription);
        labelDate = findViewById(R.id.labelDate);
        labelLocation = findViewById(R.id.labelLocation);
        btnLocation = findViewById(R.id.btnLocation);
        btnCamera = findViewById(R.id.btnCamera);
        imageViewThumbnail = findViewById(R.id.imageViewThumbnail);

        editTextDate.setEnabled(false);
        editTextLocation.setEnabled(false);


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
                editTextDate.setText((month + 1) + "/" + day + "/" + year);
            }
        }, year, month, day);

        btnDatePicker.setOnClickListener(view -> {
            dialog.show();
        });

        btnBackAddJourney.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class).putExtra("uid", MainActivity.uid));
            finish();
        });

        btnAddJourney.setOnClickListener(view -> {
            if(validation()) {
                uploadImageAndStoreJournalInFirebase();
            }
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

        //Camera

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
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        googleMap.addMarker(markerOptions);
                        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                        findLatitude = String.valueOf(latLng.latitude);
                        findLongitude = String.valueOf(latLng.longitude);

                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
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
                                currentLocation = latLng;
                                markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                                googleMap.clear();
                                googleMap.addMarker(new MarkerOptions().position(latLng));

                                try {
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                    String cityName = addresses.get(0).getAddressLine(0);
                                    String stateName = addresses.get(0).getAddressLine(1);
                                    String countryName = addresses.get(0).getAddressLine(2);
                                    editTextLocation.setText(cityName);
                                    findLatitude = String.valueOf(latLng.latitude);
                                    findLongitude = String.valueOf(latLng.longitude);
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

    public void uploadImageAndStoreJournalInFirebase(){

        imageProgressDialog = new ProgressDialog(this);
        imageProgressDialog.setTitle("Image Uploader");
        imageProgressDialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("JourneyLists").child(MainActivity.uid + new Random().nextInt(50));
        uploader.putFile(filepath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String title, description, image, date, location;
                                title = editTextTitle.getText().toString().toLowerCase(Locale.ROOT);
//                                title = title.substring(0, 1).toUpperCase() + title.substring(1);
                                description= editTextDescription.getText().toString();
                                date = editTextDate.getText().toString();
                                location = editTextLocation.getText().toString();
                                image = uri.toString();

                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                JourneyModel journeyModel = new JourneyModel(title, description, image, date, location, findLatitude, findLongitude);
                                DatabaseReference node = firebaseDatabase.getReference("JourneyLists").child(MainActivity.uid);
                                node.push().setValue(journeyModel);
                                imageProgressDialog.dismiss();
                                alertDialogBuilder = new AlertDialog.Builder(AddJourneyActivity.this)
                                        .setTitle("Congratulations")
                                        .setMessage("New journal added successfully.")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                                startActivity(new Intent(AddJourneyActivity.this, MainActivity.class).putExtra("uid", MainActivity.uid));
                                                finish();
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        imageProgressDialog.dismiss();
                        Toast.makeText(AddJourneyActivity.this, "Something is Error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot)
                    {
                        float percent = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        imageProgressDialog.setMessage("Uploading: "+ (int)percent+ "%...\n" + "Please wait");
                        imageProgressDialog.setCancelable(false);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class).putExtra("uid", MainActivity.uid));
        finish();
    }
}