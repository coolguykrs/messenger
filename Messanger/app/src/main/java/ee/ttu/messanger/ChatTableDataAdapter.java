package ee.ttu.messanger;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;

/**
 * Created by Kristjan on 21/03/2017.
 */

public class ChatTableDataAdapter extends TableDataAdapter<String> {

    private Context context;
    private List<String> data;
    private StorageReference storageRef;
    private FirebaseStorage database;

    public ChatTableDataAdapter(Context context, List<String> data) {
        super(context, data);
        this.context = context;
        this.data = data;
        database = FirebaseStorage.getInstance();
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        TextView textView = null;
        switch (columnIndex) {
            case 0:
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setScaleX(1);
                storageRef = database.getReference("topics/" + data.get(rowIndex));
                Glide.with(context)
                        .using(new FirebaseImageLoader())
                        .load(storageRef)
                        .into(imageView);
                return imageView;
            case 1:
                textView = new TextView(context);
                textView.setText(data.get(rowIndex));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                textView.setMinimumHeight(200);
                return textView;
        }
        return textView;
    }
}
