package ee.ttu.messanger.loggedinactivities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import ee.ttu.messanger.Chat;
import ee.ttu.messanger.R;

import static android.content.ContentValues.TAG;

/**
 * Created by Kristjan on 15/03/2017.
 */

public class ChatActivity extends Activity {

    public static final String NAME = "NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ChatView chatView = (ChatView) findViewById(R.id.chat_view);
        TextView heading = (TextView) findViewById(R.id.chat_heading);


        String message = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString(NAME);
        }

        new Chat(message, chatView, heading);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), TopicsActivity.class));
    }
}
