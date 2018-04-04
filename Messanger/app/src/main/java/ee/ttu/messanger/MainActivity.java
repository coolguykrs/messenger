package ee.ttu.messanger;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ee.ttu.messanger.loggedinactivities.TopicsActivity;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private EditText userName;
    private EditText passWord;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(getApplicationContext(), TopicsActivity.class));
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        System.exit(0);
    }

    public void logIn(View view) {
        userName = (EditText) findViewById(R.id.name_input);
        passWord = (EditText) findViewById(R.id.password_input);

        if (checkInputs(userName, passWord)) {
            mAuth.signInWithEmailAndPassword(userName.getText().toString(), passWord.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                            } else {
                                startActivity(new Intent(getApplicationContext(), TopicsActivity.class));
                        }
                        }
                    });
        }

    }

    public void register(View view) {
        userName = (EditText) findViewById(R.id.name_input);
        passWord = (EditText) findViewById(R.id.password_input);

        if (checkInputs(userName, passWord)) {
            mAuth.createUserWithEmailAndPassword(userName.getText().toString(), passWord.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            } else {
                                startActivity(new Intent(getApplicationContext(), TopicsActivity.class));
                            }
                        }
                    });
        }
    }

    private boolean checkInputs(EditText userName, EditText passWord) {
        return !userName.getText().toString().equals("") && !passWord.getText().toString().equals("");
    }

}
