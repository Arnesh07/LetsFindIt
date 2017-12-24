package woc.bhavye.letsfindit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    private ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent inten = new Intent(this, GeofenceTransitionsIntentService.class);
        startService(inten);

        mAuth = FirebaseAuth.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
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

        final List<String[]> lost = new LinkedList<String[]>();

        final ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(this, android.R.layout.simple_list_item_2, android.R.id.text1, lost) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String[] entry = lost.get(position);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry[0]);
                text2.setText(entry[1]);
                return view;
            }
        };

        myList.setAdapter(adapter);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lost.clear();
                adapter.clear();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Map<String, String> map = (Map) snap.getValue();
                    if (!TextUtils.isEmpty(map.get("owner")) && !TextUtils.isEmpty(map.get(uid))) {
                        if ((!uid.equals(map.get("owner")) && Boolean.parseBoolean(map.get(uid))) && (!Boolean.parseBoolean(map.get("reported")) || uid.equals(map.get("reportedby")))) {
                            Log.v("entered","object");
                            String category = map.get("category");
                            String description = map.get("description");
                            String obId = map.get("geofenceRequestId");
                            lost.add(new String[]{category, description, obId});
                        }
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

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] obj = lost.get(i);
                Intent intent = new Intent(HomeActivity.this, Report.class);
                intent.putExtra("OBJECT_ID", obj[2]);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_ac) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        }

        if (item.getItemId() == R.id.options) {
            View menuItemView = findViewById(R.id.options);
            PopupMenu popup = new PopupMenu(HomeActivity.this, menuItemView);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.settings) {

                    }

                    if (item.getItemId() == R.id.logout) {
                        FirebaseAuth.getInstance().signOut();
                    }
                    return true;
                }
            });

            popup.show();
        }


        return super.onOptionsItemSelected(item);
    }
}
