package hm.orz.chaos114.android.photolibrariessample;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.io.File;

public class SimpleCropViewActivity extends AppCompatActivity {
    private static final String TAG = SimpleCropViewActivity.class.getSimpleName();

    ProgressDialog dialog;

    private Uri source;

    static Intent createIntent(Context context, Uri source) {
        Intent intent = new Intent(context, SimpleCropViewActivity.class);
        intent.putExtra("data", source);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_crop_view);

        if (getIntent() != null) {
            source = getIntent().getParcelableExtra("data");
            Log.d(TAG, "source = " + source);
        }

        final CropImageView cropImageView = (CropImageView) findViewById(R.id.crop_image_view);
        cropImageView.setCropMode(CropImageView.CropMode.SQUARE);

        showProgress();
        cropImageView.startLoad(source, new LoadCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "success");
                dismissProgress();
            }

            @Override
            public void onError() {
                Log.d(TAG, "error");
                dismissProgress();
            }
        });

        Button okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
                cropImageView.startCrop(destination, new CropCallback() {
                    @Override
                    public void onSuccess(Bitmap cropped) {

                    }

                    @Override
                    public void onError() {

                    }
                }, new SaveCallback() {
                    @Override
                    public void onSuccess(Uri outputUri) {
                        dismissProgress();
                        Intent intent = new Intent();
                        intent.putExtra("result_uri", outputUri);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onError() {
                        dismissProgress();
                    }
                });
            }
        });
    }

    private void showProgress() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");
        dialog.show();
    }

    private void dismissProgress() {
        if (dialog == null) {
            return;
        }

        dialog.dismiss();
    }
}
