package ar.com.itresde.printerapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;


public class Calibrate extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Chronometer chronometer = (Chronometer) findViewById(R.id.chronID);
        mImageView = (ImageView) findViewById(R.id.imageCalViewID);

        //String imgpath = getExternalFilesDir(null) + File.separator;
        String projectPath = getIntent().getStringExtra("projectPath");

        System.out.println(projectPath);
        final Bitmap area = BitmapFactory.decodeFile(projectPath + "/tools/area.png");
        final Bitmap calibre = BitmapFactory.decodeFile(projectPath + "/tools/calibre.png");
        final Bitmap imageBase = BitmapFactory.decodeFile(projectPath + "/tools/base.png");
        Handler handler = new Handler();
        mImageView.setImageBitmap(area);

        int valor = Integer.parseInt(getIntent().getStringExtra("calibrateTime"));
        System.out.println(valor);

        Runnable run1 = new Runnable() {
            public void run() {
                mImageView.setImageBitmap(calibre);
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        };

        Runnable run2 = new Runnable() {
            public void run() {
                mImageView.setImageBitmap(imageBase);
            }
        };

        chronometer.start();
        handler.postDelayed(run1, 5000);
        handler.postDelayed(run2, valor * 1000 + 5000);
    }
}