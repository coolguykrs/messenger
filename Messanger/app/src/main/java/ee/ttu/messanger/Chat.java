package ee.ttu.messanger;


import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

/**
 * Created by Kristjan on 17/03/2017.
 */

public class Chat {

    private final int TIME = 10000;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ChatView chatView;

    public Chat(String chatName, ChatView chatView, TextView heading) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(chatName);
        this.chatView = chatView;
        heading.setText(chatName);

        sendText();
        upDateTexts();

    }

    private void upDateTexts() {
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                chatView.destroyDrawingCache();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!dataSnapshot.getKey().equals("1") && (System.currentTimeMillis() - Long.parseLong(dataSnapshot.getKey()) > TIME)) {
                    myRef.child(dataSnapshot.getKey()).removeValue();
                } else if (user != null && !dataSnapshot.getKey().equals("1") && (System.currentTimeMillis() - Long.parseLong(dataSnapshot.getKey()) < TIME)) {
                    if (!dataSnapshot.getValue().toString().contains(user.getEmail())) {
                        chatView.addMessage(new ChatMessage(dataSnapshot.getValue().toString(), Long.parseLong(dataSnapshot.getKey()), ChatMessage.Type.RECEIVED));
                    } else {
                        chatView.addMessage(new ChatMessage(dataSnapshot.getValue().toString(), Long.parseLong(dataSnapshot.getKey()), ChatMessage.Type.SENT));
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendText() {
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener(){
            @Override
            public boolean sendMessage(ChatMessage chatMessage){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String email = user.getEmail();
                    myRef.child(System.currentTimeMillis() + "").setValue(email + ": " + chatMessage.getMessage().replaceAll(censorWords("cunt", "bitch", "fuck", "shit"), "*"));
                    EditText editText = (EditText) chatView.findViewById(R.id.input_edit_text);
                    editText.setText("");
                }
                return false;
            }
        });
    }

    private static String censorWords(String... words) {
        String re = "";
        for (String w : words)
            for (int i = 0; i < w.length(); i++)
                re += String.format("|((?<=%s)%s(?=%s))",
                        w.substring(0, i), w.charAt(i), w.substring(i + 1));
        return re.substring(1);
    }

}
