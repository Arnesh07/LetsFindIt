package woc.bhavye.letsfindit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;

    String uid;

    private FirebaseDatabase database;
    private DatabaseReference mRef;

    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        database = FirebaseDatabase.getInstance();
        mRef = database.getInstance().getReference("object_information");

        mList = (ListView) findViewById(R.id.list);

        final List<String[]> objects =new LinkedList<String[]>();

        final ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(this, android.R.layout.simple_list_item_2, android.R.id.text1, objects){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view= super.getView(position, convertView, parent);
                String[] entry = objects.get(position);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry[0]);
                text2.setText(entry[1]);
                return view;
            }
        };

        mList.setAdapter(adapter);

        ValueEventListener listener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                        Map<String, String> map = (Map) snap.getValue();
                        if(map.get("owner").equals(uid)) {
                            String category = map.get("category");
                            String description = map.get("description");
                            objects.add(new String[]{category, description});
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

        getMenuInflater().inflate(R.menu.profile_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_home)
        {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    protected void addNewObject(View view)
    {
        Intent intent = new Intent(ProfileActivity.this, NewItem.class);
        startActivity(intent);
    }
}
