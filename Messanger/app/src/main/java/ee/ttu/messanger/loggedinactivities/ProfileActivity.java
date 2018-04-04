package ee.ttu.messanger.loggedinactivities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import ee.ttu.messanger.BottomNavigationViewHelper;
import ee.ttu.messanger.DownloadImageTask;
import ee.ttu.messanger.MainActivity;
import ee.ttu.messanger.R;

import static android.content.ContentValues.TAG;

/**
 * Created by Kristjan on 15/03/2017.
 */

public class ProfileActivity extends Activity {

    private ImageView imageView;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }


        imageView = (ImageView) findViewById(R.id.profile_picture);

        setMenu();
        setUserInfo();

        changePicture();
        enableLoggingOut();
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
//                            startActivity(intent1);
                                startActivity(new Intent(getApplicationContext(), TopicsActivity.class));
                                break;
                            case R.id.action_menu2:
                                startActivity(new Intent(getApplicationContext(), AddTopicActivity.class));
                                break;
                            case R.id.action_menu3:
                                break;
                        }
                        return false;
                    }
                });
    }

    private void enableLoggingOut() {
        Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    private void changePicture() {
        Button changePictureButton = (Button) findViewById(R.id.change_picture_button);
        changePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(imageView);
            }
        });
    }

    private void setUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            Log.d("PHOTOURL: ", photoUrl + "");

            if (photoUrl != null) {
                downloadImage(photoUrl);
//                imageView.setImageURI(photoUrl);
            }

            TextView username = (TextView) findViewById(R.id.username);
            username.setText(email);
        }
    }

    private void downloadImage(Uri photoUrl) {
        new DownloadImageTask(imageView).execute(photoUrl + "");
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
                        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
                image = Bitmap.createScaledBitmap(image, 1200, 1200, true);
                imageUri = data.getData();
                imageView.setImageBitmap(image);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String imagePath = getRealPathFromUri(selectedImageUri);
                Bitmap image = BitmapFactory.decodeFile(imagePath);
                Bitmap resized = Bitmap.createScaledBitmap(image, 1200, 1200, true);
                imageUri = bitmapToUriConverter(resized);
                imageView.setImageBitmap(resized);
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference pictureRef = storageRef.child("images/" + user.getEmail());
            pictureRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUrl)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                                                Toast.makeText(ProfileActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
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

    public Uri bitmapToUriConverter(Bitmap mBitmap) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 1000, 1000,
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

