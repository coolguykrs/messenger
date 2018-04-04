package ee.ttu.messanger.loggedinactivities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import ee.ttu.messanger.BottomNavigationViewHelper;
import ee.ttu.messanger.ChatTableDataAdapter;
import ee.ttu.messanger.R;

/**
 * Created by Kristjan on 15/03/2017.
 */

public class TopicsActivity extends Activity {

    private List<String> topics;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private Button searchButton;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);
        setMenu();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        addTopics("");

        searchButton = (Button) findViewById(R.id.search_button);
        searchInput = (EditText) findViewById(R.id.search_input);
        enableSearch();
    }

    private void addTopics(final String search) {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                topics = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    if (postSnapshot.getKey().contains(search)) {
                        topics.add(postSnapshot.getKey());
                    }
                }
                writeTopics();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void enableSearch() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchInput.getText() != null) {
                    addTopics(searchInput.getText() + "");
                    searchInput.setText("");
                } else {
                    addTopics("");
                }
            }
        });
    }

    private void writeTopics() {
        String[][] topicsTexts = new String[topics.size()][2];
        for (int i = 0; i < topics.size(); i++) {
            topicsTexts[i][0] = ((i + 1) + ".");
            topicsTexts[i][1] = topics.get(i);
        }

        SortableTableView<String> tableView = (SortableTableView<String>) findViewById(R.id.tableView);

        TableColumnWeightModel columnModel = new TableColumnWeightModel(4);
        tableView.setColumnModel(columnModel);
        tableView.setColumnComparator(1, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });


        tableView.setDataAdapter(new ChatTableDataAdapter(this, topics));



        tableView.addDataClickListener(new TableDataClickListener<String>() {
            @Override
            public void onDataClicked(int rowIndex, String clickedData) {
                String chatName =  topics.get(rowIndex);
                Bundle extras = new Bundle();
                extras.putString(ChatActivity.NAME, "" + chatName);
                Intent intent = new Intent(TopicsActivity.this, ChatActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    private void setMenu() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_menu1:
                            break;
                        case R.id.action_menu2:
                            startActivity(new Intent(getApplicationContext(), AddTopicActivity.class));
                            break;
                        case R.id.action_menu3:
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            break;
                    }
                    return false;
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }



}