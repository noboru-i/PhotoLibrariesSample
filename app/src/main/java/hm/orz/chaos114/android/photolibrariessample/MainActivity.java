package hm.orz.chaos114.android.photolibrariessample;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;
import com.laevatein.Laevatein;
import com.laevatein.MimeType;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_LAEVATEIN = 1;
    private static final int REQUEST_CODE_MULTI_IMAGE_SELECTOR = 2;
    private static final int REQUEST_CODE_TED_PICKER = 3;
    private static final int REQUEST_CODE_SIMPLE_CROP_VIEW = 4;

    private ImageView[] images;
    private ImageView croppedImage;

    private List<Uri> imagePaths;
    private Uri destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        images = new ImageView[4];
        images[0] = (ImageView) findViewById(R.id.image1);
        images[1] = (ImageView) findViewById(R.id.image2);
        images[2] = (ImageView) findViewById(R.id.image3);
        images[3] = (ImageView) findViewById(R.id.image4);

        croppedImage = (ImageView) findViewById(R.id.cropped_image);

        initLaevatein();
        initMultiImageSelector();
        initTedPicker();

        initAndroidCrop();
        initSimpleCropView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LAEVATEIN:
                handleLaevatein(resultCode, data);
                break;
            case REQUEST_CODE_MULTI_IMAGE_SELECTOR:
                handleMultiImageSelector(resultCode, data);
                break;
            case REQUEST_CODE_TED_PICKER:
                handleTedPicker(resultCode, data);
                break;
            case Crop.REQUEST_CROP:
                handleAndroidCrop(resultCode, data);
                break;
            case REQUEST_CODE_SIMPLE_CROP_VIEW:
                handleSimpleCropView(resultCode, data);
                break;
        }
    }

    private void initLaevatein() {
        Button button = (Button) findViewById(R.id.laevatein);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.openLaevateinWithCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void openLaevatein() {
        Laevatein.from(MainActivity.this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))
                .count(1, 4)
                .forResult(REQUEST_CODE_LAEVATEIN);
    }

    private void handleLaevatein(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Uri> selected = Laevatein.obtainResult(data);
        showImages(selected);
    }

    private void initMultiImageSelector() {
        Button button = (Button) findViewById(R.id.multi_image_selector);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.openMultiImageSelectorWithCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void openMultiImageSelector() {
        MultiImageSelector.create()
                .showCamera(false)
                .count(4)
                .multi()
                .start(MainActivity.this, REQUEST_CODE_MULTI_IMAGE_SELECTOR);
    }

    private void handleMultiImageSelector(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> selected = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
        Log.d(TAG, "selected = " + selected);
        List<Uri> uris = new ArrayList<>();
        for (String s : selected) {
            uris.add(Uri.fromFile(new File(s)));
        }
        showImages(uris);
    }

    private void initTedPicker() {
        Button button = (Button) findViewById(R.id.ted_picker);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.openTedPickerWithCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void openTedPicker() {
        Config config = new Config();
        config.setSelectionMin(1);
        config.setSelectionLimit(4);

        ImagePickerActivity.setConfig(config);

        Intent intent = new Intent(this, ImagePickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_TED_PICKER);
    }

    private void handleTedPicker(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Uri> selected = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
        Log.d(TAG, "selected = " + selected);
        List<Uri> uris = new ArrayList<>();
        for (Uri u : selected) {
            uris.add(Uri.fromFile(new File(u.getPath())));
        }
        showImages(uris);
    }

    private void initAndroidCrop() {
        Button button = (Button) findViewById(R.id.android_crop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.openAndroidCropWithCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void openAndroidCrop() {
        if (!hasPhoto()) {
            Toast.makeText(this, "pick photo first.", Toast.LENGTH_SHORT).show();
            return;
        }

        destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(imagePaths.get(0), destination).asSquare().start(this);
    }

    private void handleAndroidCrop(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "extras = " + data.getExtras().toString());

        Picasso.with(this).load(destination).into(croppedImage);
    }

    private void initSimpleCropView() {
        Button button = (Button) findViewById(R.id.simple_crop_view);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.openSimpleCropViewWithCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void openSimpleCropView() {
        if (!hasPhoto()) {
            Toast.makeText(this, "pick photo first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = SimpleCropViewActivity.createIntent(this, imagePaths.get(0));
        startActivityForResult(intent, REQUEST_CODE_SIMPLE_CROP_VIEW);
    }

    private void handleSimpleCropView(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled.", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = data.getParcelableExtra("result_uri");

        Picasso.with(this).load(uri).into(croppedImage);
    }

    private void showImages(List<Uri> uris) {
        Log.d(TAG, "uris = " + uris);
        imagePaths = uris;
        for (ImageView image : images) {
            image.setImageResource(0);
        }
        for (int i = 0; i < uris.size(); i++) {
            Picasso.with(this).load(uris.get(i)).into(images[i]);
        }
    }

    private boolean hasPhoto() {
        return imagePaths != null && imagePaths.get(0) != null;
    }
}
