package woc.bhavye.letsfindit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseUser user;

    String uid;

    private FirebaseDatabase database;
    private DatabaseReference mRef;

    private  ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        database = FirebaseDatabase.getInstance();
        mRef = database.getInstance().getReference("object_information");

        myList = (ListView) findViewById(R.id.lostNearby);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);

        final List<String[]> lost =new LinkedList<String[]>();

        final ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(this, android.R.layout.simple_list_item_2, android.R.id.text1, lost){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view= super.getView(position, convertView, parent);
                String[] entry = lost.get(position);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry[0]);
                text2.setText(entry[1]);
                return view;
            }
        };

        myList.setAdapter(adapter);

        ValueEventListener listener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lost.clear();
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    Map<String, String> map  = (Map) snap.getValue();
                    String reported = map.get("reported");
                    String proximity = map.get("proximity");
                    if(!(map.get("owner").equals(uid)) && (Boolean.parseBoolean(reported) == false) && (Boolean.parseBoolean(proximity) == true)) {
                        String category = map.get("category");
                        String distance = "approximately ".concat(map.get("distance")).concat(" meters from you");
                        lost.add(new String[]{category, distance});
                     }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("error", databaseError.toString());
            }
        };
        mRef.addValueEventListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_ac)
        {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        }

        if(item.getItemId() == R.id.options)
        {
            View menuItemView = findViewById(R.id.options);
            PopupMenu popup = new PopupMenu(HomeActivity.this, menuItemView);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.settings) {

                    }

                    if(item.getItemId() == R.id.logout) {
                        FirebaseAuth.getInstance().signOut();
                    }
                    return true;
                }
            });

            popup.show();
        }


        return super.onOptionsItemSelected(item);
    }

    /**private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371;

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return (dist*1000); // output distance, in meters
    } */
}
