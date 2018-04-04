package ee.ttu.messanger.loggedinactivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import ee.ttu.messanger.BottomNavigationViewHelper;
import ee.ttu.messanger.R;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by Kristjan on 15/03/2017.
 */

public class AddTopicActivity extends Activity {


    private EditText topicInput;
    private Button addTopicButton;
    private Button addChatPictureButton;
    private ImageView chatPicture;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;

    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private Uri imageUri;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_topic);
        setMenu();

        topicInput = (EditText) findViewById(R.id.add_topic_input);
        addTopicButton = (Button) findViewById(R.id.add_topic_button);
        addChatPictureButton = (Button) findViewById(R.id.add_chat_picture_button);
        chatPicture = (ImageView) findViewById(R.id.chat_picture_image);

        enableImageAdding();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        enableTopicAdding();
    }

    private void setMenu() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_menu1:
                                startActivity(new Intent(getApplicationContext(), TopicsActivity.class));
                                break;
                            case R.id.action_menu2:
                                break;
                            case R.id.action_menu3:
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                                break;
                        }
                        return false;
                    }
                });
    }

    private void enableImageAdding() {
        addChatPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(chatPicture);
            }
        });
    }

    private void enableTopicAdding() {
        addTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (imageUri != null && !topicInput.getText().toString().equals("")) {
                if (!topicInput.getText().toString().equals("")) {
                    uploadPicture();
                    String topicName = topicInput.getText() + "";
                    myRef.child(topicName).child("1").setValue("Chat by " + user.getEmail());
                    chatPicture.setImageBitmap(null);
                    topicInput.setText("");

                    Bundle extras = new Bundle();
                    extras.putString(ChatActivity.NAME, "" + topicName);
                    Intent intent = new Intent(AddTopicActivity.this, ChatActivity.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            }
        });
    }

    public void chooseImage(View view) {
        final String[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (items[which]) {
                    case "Take Photo":
                        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
                        break;
                    case "Choose from Library":
                        if (ContextCompat.checkSelfPermission(AddTopicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(AddTopicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_STORAGE_PERMISSION);
                            dialog.dismiss();
                        } else {
                            openPhotoSelect();
                        }
                        break;
                    default:
                        dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openPhotoSelect();
            }
        }
    }

    private void openPhotoSelect() {
        Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        choosePhotoIntent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(choosePhotoIntent, "Select file"),
                SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                image = Bitmap.createScaledBitmap(image, 200, 200, true);
                imageUri = data.getData();
                chatPicture.setImageBitmap(image);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
//                imageUri = selectedImageUri;
                String imagePath = getRealPathFromUri(selectedImageUri);
                Bitmap image = BitmapFactory.decodeFile(imagePath);
                Bitmap resized = Bitmap.createScaledBitmap(image, 200, 200, true);
                imageUri = bitmapToUriConverter(resized);
                chatPicture.setImageBitmap(resized);
            }
        }
    }

    private String getRealPathFromUri(Uri contentUri) {
        String selectedImagePath = null;
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            selectedImagePath = cursor.getString(column_index);
        }
        cursor.close();
        return selectedImagePath;
    }

    private void uploadPicture() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference pictureRef = storageRef.child("topics/" + topicInput.getText());
            pictureRef.putFile(imageUri);
        }
    }

    public Uri bitmapToUriConverter(Bitmap mBitmap) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 100, 100,
                    true);
            File file = new File(this.getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out = this.openFileOutput(file.getName(),
                    Context.MODE_WORLD_READABLE);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;
    }
}


