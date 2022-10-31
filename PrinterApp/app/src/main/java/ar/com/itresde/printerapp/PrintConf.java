package ar.com.itresde.printerapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
//import ar.com.itresde.printerapp.PathUtil;

import java.io.File;
import java.net.URISyntaxException;

import static android.os.Environment.getExternalStorageDirectory;

public class PrintConf extends AppCompatActivity {

    Button buttonPrint;
    Button buttonCalibrate;
    Button button_selectPath;

    private int READ_REQUEST_CODE = 1;
    String projectPath;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_conf);

        buttonPrint = (Button) findViewById(R.id.button_print);
        final EditText editText_printTime = findViewById(R.id.editText_printTime);

        buttonCalibrate = (Button) findViewById(R.id.button_calibrate);
        final EditText editText_calibrateTime = findViewById(R.id.editText_calibrateTime);

        button_selectPath = (Button) findViewById(R.id.button_selectPath);

        Intent intent = getIntent();
        address = intent.getStringExtra(BTDevices.EXTRA_DEVICE_ADDRESS);
        //System.out.println("*******   address: " + address);

        buttonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String printTime = editText_printTime.getText().toString();
                Intent i = new Intent(PrintConf.this, PrePrint.class);
                i.putExtra("printTime", printTime);
                i.putExtra("projectPath", projectPath);
                i.putExtra("EXTRA_DEVICE_ADDRESS", address);
                startActivity(i);
            }
        });

        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String calibrateTime = editText_calibrateTime.getText().toString();
                Intent i = new Intent(PrintConf.this, Calibrate.class);
                i.putExtra("calibrateTime", calibrateTime);
                i.putExtra("projectPath", projectPath);
                startActivity(i);
            }
        });

        button_selectPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, READ_REQUEST_CODE);
            }

        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EditText editText_projectPath = findViewById(R.id.editText_pathSelected);
        TextView editText_projectName = findViewById(R.id.textView_projectName);

        if (resultCode == RESULT_CANCELED) {
            //Cancelado por el usuario
        }
        if ((resultCode == RESULT_OK) && (requestCode == READ_REQUEST_CODE)) {
            //Procesar el resultado
            Uri uri = data.getData(); //obtener el uri content

            //File filePath=FileUtils.getFile(this, uri);

            System.out.println("getExternalFilesDir: " + getExternalFilesDir(null));
            System.out.println("_____________________________");
            //System.out.println("filePath: " + filePath);
            System.out.println("getExternalStorageDirectory: " + getExternalStorageDirectory());
            System.out.println("Seleccion: " + uri.toString());
            System.out.println("Seleccion: " + uri.getPath());
            System.out.println("_____________________________");
            //System.out.println("real: " + getRealPathFromURI(MainActivity.this, uri));

            String arr[] = uri.getPath().split(":");
            System.out.println(arr[0]);
            //System.out.println(uri.getHost());

            System.out.println(uri.getLastPathSegment());

            projectPath = getExternalStorageDirectory() + "/" + arr[1];

            System.out.println("projectPath: " + projectPath);
            System.out.println("*******************************");

            editText_projectPath.setText(projectPath);
            editText_projectName.setText(arr[1]);

        }
    }

}
