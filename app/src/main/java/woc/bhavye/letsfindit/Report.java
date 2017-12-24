package woc.bhavye.letsfindit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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


public class Report extends AppCompatActivity {

    String objectId;
    String belongsto;

    private TextView text1;
    private TextView text2;

    private Button openChat;

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private DatabaseReference ref;

    private FirebaseUser user;

    private String uid;

    private Button rep;
    private TextView textReported;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        Intent intent = getIntent();
        objectId = intent.getStringExtra("OBJECT_ID");

        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);

        openChat = (Button) findViewById(R.id.openChat);

        database = FirebaseDatabase.getInstance();
        mRef = database.getInstance().getReference("object_information").child(objectId);


        rep = (Button) findViewById(R.id.report);
        textReported = (TextView) findViewById(R.id.textReported);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    text1.setText(dataSnapshot.child("category").getValue().toString());
                    text2.setText(dataSnapshot.child("description").getValue().toString());
                    belongsto = dataSnapshot.child("owner").getValue().toString();
                Map<String, String> map = (Map) dataSnapshot.getValue();
                    if(!TextUtils.isEmpty(map.get("reportedby"))) {
                        if (dataSnapshot.child("reportedby").getValue().toString().equals(uid)) {
                            rep.setVisibility(View.INVISIBLE);
                            openChat.setVisibility(View.VISIBLE);
                            textReported.setVisibility(View.VISIBLE);
                        }
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.addValueEventListener(listener);
    }

    public void onClickReport(View view) {
        mRef.child("reportedby").setValue(uid);
        mRef.child("reported").setValue("true");
        rep.setVisibility(View.INVISIBLE);
        openChat.setVisibility(View.VISIBLE);
        textReported.setVisibility(View.VISIBLE);
    }

    public void openChat(View view) {

        String chatID = belongsto.concat(uid);
        Intent intent = new Intent(Report.this, MessageActivity.class);
        intent.putExtra("CHAT_ID", chatID);
        intent.putExtra("USER_ID", uid);
        startActivity(intent);
    }
}
