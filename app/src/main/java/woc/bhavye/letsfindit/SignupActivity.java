package woc.bhavye.letsfindit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editPassword;
    EditText editName;
    EditText editContact;

    String email, password, name, contact;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser user;

    FirebaseDatabase database;
    DatabaseReference mRef;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editContact = (EditText) findViewById(R.id.contact);
        editEmail = (EditText) findViewById(R.id.mail);
        editName = (EditText) findViewById(R.id.name);
        editPassword = (EditText) findViewById(R.id.password);


        mAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        mProgress = new ProgressDialog(this);

        database = FirebaseDatabase.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    public void signupClicked(View view)
    {
        email = editEmail.getText().toString();
        password = editPassword.getText().toString();
        name = editName.getText().toString();
        contact = editContact.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(contact) || TextUtils.isEmpty(name))
        {
            Toast.makeText(SignupActivity.this, "Fill All Fields!", Toast.LENGTH_LONG).show();
        }
        else
        {
            mProgress.setMessage("Signing Up...");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                mProgress.dismiss();
                                Toast.makeText(SignupActivity.this, "Check Email or Password; Password might be too short!", Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                 user = FirebaseAuth.getInstance().getCurrentUser();
                                 String uid = user.getUid();
                                 mRef = database.getReference("users").child(uid);
                                 mRef.child("Name").setValue(name);
                                 mRef.child("Contact").setValue(contact);
                            }
                        }
                    });
        }

    }
}
