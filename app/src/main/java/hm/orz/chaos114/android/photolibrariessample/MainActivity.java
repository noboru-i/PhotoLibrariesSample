package hm.orz.chaos114.android.photolibrariessample;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.laevatein.Laevatein;
import com.laevatein.MimeType;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LAEVATEIN = 1;

    private ImageView[] images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        images = new ImageView[4];
        images[0] = (ImageView) findViewById(R.id.image1);
        images[1] = (ImageView) findViewById(R.id.image2);
        images[2] = (ImageView) findViewById(R.id.image3);
        images[3] = (ImageView) findViewById(R.id.image4);

        initLaevatein();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LAEVATEIN:
                handleLaevatein(resultCode, data);
                break;
        }
    }

    private void initLaevatein() {
        Button button = (Button) findViewById(R.id.laevatein);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Laevatein.from(MainActivity.this)
                        .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))
                        .count(1, 4)
                        .forResult(REQUEST_CODE_LAEVATEIN);
            }
        });
    }

    private void handleLaevatein(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Uri> selected = Laevatein.obtainResult(data);
        showImages(selected);
    }

    private void showImages(List<Uri> uris) {
        for (int i = 0; i < uris.size(); i++) {
            Picasso.with(this).load(uris.get(i)).into(images[i]);
        }
    }
}
