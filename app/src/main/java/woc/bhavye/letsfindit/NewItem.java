package woc.bhavye.letsfindit;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
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

    private final static int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        editCategory = (EditText) findViewById(R.id.category);
        editDescription = (EditText) findViewById(R.id.object_description);

        database = FirebaseDatabase.getInstance();

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
        refOb.child("reported").setValue(false);
        refOb.child("found").setValue(false);
        refOb.child("proximity").setValue(false);

        Intent intent = new Intent(NewItem.this, ProfileActivity.class);
        startActivity(intent);
    }
}
