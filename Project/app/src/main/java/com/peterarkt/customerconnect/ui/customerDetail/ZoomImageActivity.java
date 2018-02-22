package com.peterarkt.customerconnect.ui.customerDetail;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.peterarkt.customerconnect.R;
import com.squareup.picasso.Picasso;

public class ZoomImageActivity extends AppCompatActivity {

    private static final String PHOTO_PATH = "PHOTO_PATH";

    private String photoPath = "";

    /* -----------------------------------------------------------------
     * Launch Helper
     * -----------------------------------------------------------------*/
    public static void launch(Context context, String photoPath) {
        context.startActivity(launchIntent(context, photoPath));
    }

    private static Intent launchIntent(Context context, String photoPath) {
        Class destinationActivity = ZoomImageActivity.class;
        Intent intent = new Intent(context, destinationActivity);

        Bundle bundle = new Bundle();
        bundle.putString(PHOTO_PATH,photoPath);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image_zoom);

        PhotoView photoView = (PhotoView) findViewById(R.id.customer_image_view);

        Intent receivedIntent = getIntent();
        if(receivedIntent.hasExtra(PHOTO_PATH))
            photoPath = receivedIntent.getStringExtra(PHOTO_PATH);

        if(photoPath == null || photoPath.isEmpty()){

            Toast.makeText(this,R.string.no_image_path_is_found,Toast.LENGTH_SHORT).show();
        }else{
            Picasso.with(this)
                    .load("file://" + photoPath)
                    .error(R.drawable.ic_material_error_gray)
                    .fit()
                    .centerInside()
                    .into(photoView);
        }

    }
}
