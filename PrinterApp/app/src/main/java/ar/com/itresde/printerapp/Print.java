package ar.com.itresde.printerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Print extends AppCompatActivity {

    ImageView mImageView;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    //private static String address = null;
    //-------------------------------------------


    int flag = 0;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);

        System.out.println("*********** PRINT ************");

        //Mantengo la pantalla simpre prendida
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //aumento automaticamente el brillo
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1F;
        getWindow().setAttributes(layout);

        //Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_print);
        //BT

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        flag = 1;
                        //IdBufferIn.setText("Dato: " + dataInPrint);//<-<- PARTE A MODIFICAR >->->
                        System.out.println("***** FIN DE GIRO ****" + dataInPrint);
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();


        //BT connection
        //Consigue la direccion MAC desde DeviceListActivity via intent
        //Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        //String projectPath = getIntent().getStringExtra("projectPath");
        String address = getIntent().getStringExtra("EXTRA_DEVICE_ADDRESS");//<-<- PARTE A MODIFICAR >->->
        System.out.println("*******   address: " + address);
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        System.out.println(device.getName());
        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            System.out.println("@@@ fallo createBluetoothSocket(device) "+ e.toString());
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                System.out.println("@@@@@ fallo btSocket.connect: "+ e.toString());
                btSocket.close();
                btSocket = createBluetoothSocket2(device);
                btSocket.connect();
            } catch (IOException e2) {
                System.out.println("@@@@@ fallo 2 btSocket.connect: "+ e.toString());
                try {
                    btSocket.close();
                }catch (IOException e3) {}
            }
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();

    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        System.out.println("@@@@ createBluetoothSocket");
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private BluetoothSocket createBluetoothSocket2(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        System.out.println("@@@@ createBluetoothSocket2");
        try {
            return (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
        }
        catch (Exception e2) {
            System.out.println("Couldn't establish Bluetooth connection!");
        }
        System.out.println("@@@@ return null");
        return null;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        mImageView = (ImageView) findViewById(R.id.imageViewID);

        String projectPath = getIntent().getStringExtra("projectPath");

        int printTime = Integer.parseInt(getIntent().getStringExtra("printTime"));
        final Bitmap imageBase = BitmapFactory.decodeFile(projectPath + "/tools/base.png");
        final File slicesFolder = new File(projectPath + "/slices/");
        final File[] slicesFiles = slicesFolder.listFiles();

        String[] slices = new String[slicesFiles.length];
        for (int x=0; x < slicesFiles.length; x++) {
            slices[x] = slicesFiles[x].getName();
        }
        Arrays.sort(slices);

        Handler handler = new Handler();
        mImageView.setImageBitmap(imageBase);

        try {
            final File fileconf = new File(projectPath + "/conf.txt");
            //BufferedReader br = new BufferedReader(new FileReader(fileconf));
            BufferedReader br = new BufferedReader(new FileReader(projectPath + "/conf.txt"));
            float step = Float.parseFloat(br.readLine());
            br.close();
            System.out.println("@@@@@@@@@@@@" + step + "@@@@@@@@@@@@");
        } catch (IOException e) {

            e.printStackTrace();
        }

        //TODO: Incorporar el step al movimiento del motor

        for (int x = 0, z = 0, ext = 0; x < slices.length; x++) {
            final String fname = projectPath + "/slices/" + slices[x];

            Runnable showLayer =  new Runnable(){
                public void run() {
                    Bitmap image = BitmapFactory.decodeFile(fname);
                    mImageView.setImageBitmap(image);
                    //mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    System.out.println(fname);
                }
            };
            Runnable showBase =  new Runnable(){
                public void run() {
                    mImageView.setImageBitmap(imageBase);
                    //mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            };

            Runnable move =  new Runnable(){
                public void run() {

                    MyConexionBT.write("200#F");
                    MyConexionBT.write("196#B");

                    System.out.println("mover Motor");
                }
            };

            handler.post(showBase);
            if ( x==0 ) { ext = (printTime); } else { ext = 0; }

            z = z + 9000;
            handler.postDelayed(showLayer, z);
            z = z + printTime*1000 + ext*1000;
            handler.postDelayed(showBase, z);
            z = z + 5;
            handler.postDelayed(move, z);
        }

        mImageView.setImageBitmap(imageBase);
        System.out.println("----- Fin -----");
    }


    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
            System.out.println("####################### cierre Destroy Print #############");
        } catch (IOException e2) {}
    }


    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}