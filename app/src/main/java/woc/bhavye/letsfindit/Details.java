package woc.bhavye.letsfindit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Details extends AppCompatActivity {

    String objectId;
    private TextView cat;
    private TextView des;
    private TextView rep;

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private DatabaseReference ref;

    private Button found;
    private Button notFound;
    private Button openChat;

    private String reportedby;

    FirebaseUser user;

    String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        objectId = intent.getStringExtra("OBJECT_ID");

        user = FirebaseAuth.getInstance().getCurrentUser();

        cat = (TextView) findViewById(R.id.cat);
        des = (TextView) findViewById(R.id.des);
        rep = (TextView) findViewById(R.id.reported);

        found = (Button) findViewById(R.id.found);
        notFound = (Button) findViewById(R.id.not_found);
        openChat = (Button) findViewById(R.id.open_chat);

        database = FirebaseDatabase.getInstance();
        mRef = database.getInstance().getReference("object_information").child(objectId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cat.setText(dataSnapshot.child("category").getValue().toString());
                des.setText(dataSnapshot.child("description").getValue().toString());
                if(Boolean.parseBoolean(dataSnapshot.child("reported").getValue().toString())) {
                    reportedby = dataSnapshot.child("reportedby").getValue().toString();
                    objectReported(reportedby);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(listener);
    }

    public void objectReported(String reporter) {
        found.setVisibility(View.VISIBLE);
        notFound.setVisibility(View.VISIBLE);
        openChat.setVisibility(View.VISIBLE);
        ref = database.getReference("users").child(reporter);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    String msg = "reported by: \n".concat(dataSnapshot.child("Name").getValue().toString()).concat("\n").concat("contact number: ").concat(dataSnapshot.child("Contact").getValue().toString());
                    rep.setText(msg);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ref.addValueEventListener(listener);
    }

    public void onClickFound(View view){
        mRef.child("found").setValue("true");
        mRef.child("reported").setValue("true");
        FirebaseDatabase.getInstance().getReference("chats").child(chatID).removeValue();
        Intent intent = new Intent(Details.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void onClickNotFound(View view){
        mRef.child("reported").setValue("false");
        mRef.child("reportedby").removeValue();
        String msg = "Not Reported...Yet!";
        rep.setText(msg);
        found.setVisibility(View.INVISIBLE);
        notFound.setVisibility(View.INVISIBLE);
        openChat.setVisibility(View.INVISIBLE);
        rep.setText("");
        FirebaseDatabase.getInstance().getReference("chats").child(chatID).removeValue();
    }

    public void onClickOpenChat(View view){
        Intent intent = new Intent(Details.this, MessageActivity.class);
        chatID = user.getUid().concat(reportedby);
        intent.putExtra("CHAT_ID", chatID);
        startActivity(intent);
    }
}
