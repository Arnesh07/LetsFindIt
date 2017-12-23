package woc.bhavye.letsfindit;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class NewItem extends AppCompatActivity {

    EditText editCategory;
    EditText editDescription;

    String category;
    String description;
    String uid;
    String objectId;

    FirebaseDatabase database;
    DatabaseReference refOb;

    LatLng location;

    FirebaseUser user;

    private GeofencingClient mGeofencingClient;
    
    Geofence geofence;

    PendingIntent mGeofencePendingIntent;


    private final static int PLACE_PICKER_REQUEST = 1;
    private final static int GEOFENCE_RADIUS = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        editCategory = (EditText) findViewById(R.id.category);
        editDescription = (EditText) findViewById(R.id.object_description);

        database = FirebaseDatabase.getInstance();

        mGeofencingClient = LocationServices.getGeofencingClient(this);
    }



    public void onClickLocation(View view) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(NewItem.this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {

                Place place = PlacePicker.getPlace(NewItem.this, data);
                location = place.getLatLng();

            }
        }
    }

    public void onClickSubmit(View view)
    {
        category = editCategory.getText().toString();
        description = editDescription.getText().toString();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Random rand = new Random();

        uid = user.getUid();
        objectId = uid.concat(category).concat(Integer.toString(rand.nextInt(100)));

        refOb = database.getReference("object_information").child(objectId);
        refOb.child("latitude").setValue(Double.toString(location.latitude));
        refOb.child("longitude").setValue(Double.toString(location.longitude));
        refOb.child("owner").setValue(uid);
        refOb.child("category").setValue(category);
        refOb.child("description").setValue(description);
        refOb.child("reported").setValue("false");
        refOb.child("found").setValue("false");
        refOb.child("geofenceRequestId").setValue(objectId);

        geofence = new Geofence.Builder().setRequestId(objectId)
                .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        addGeofences();

        Intent intent = new Intent(NewItem.this, ProfileActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_LONG).show();
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "object successfully posted1", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "failed to add object", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
}
