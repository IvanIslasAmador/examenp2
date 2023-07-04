package com.example.controlbotones;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private Button btnConnectBluetooth;
    private Button btnUp;
    private Button btnDown;
    private Button btnLeft;
    private Button btnRight;
    private Button btnDetener;

    private TextView txtX;
    private TextView txtY;
    private TextView txtZ;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> deviceList;

    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private Handler repeatHandler;
    private boolean repeatFlag = false;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID para SPP (Serial Port Profile)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnectBluetooth = findViewById(R.id.btnConnectBluetooth);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnDetener = findViewById(R.id.btnDetener);
        //txtX = findViewById(R.id.txtX);
        //txtY = findViewById(R.id.txtY);
        //txtZ = findViewById(R.id.txtZ);

        // Inicializar el SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Verificar si el dispositivo tiene acelerómetro
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // Obtener el acelerómetro del dispositivo
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            // El dispositivo no tiene acelerómetro
            // Realizar alguna acción o mostrar un mensaje de error
        }

        // Inicializar el adaptador Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Verificar si el dispositivo soporta Bluetooth
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            // Realizar alguna acción aquí, si es necesario
        }

        // Verificar si los permisos de Bluetooth están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // Si no se tienen los permisos, solicitarlos al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_PERMISSION);
        }

        // Verificar si el Bluetooth está habilitado
        if (!bluetoothAdapter.isEnabled()) {
            // Si el Bluetooth está deshabilitado, solicitar al usuario que lo habilite
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Asignar acciones a los botones
        btnConnectBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectBluetooth();
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("UP");
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("DOWN");
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("LEFT");
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("RIGHT");
            }
        });

        btnDetener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("STOP");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar el SensorEventListener para el acelerómetro
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistrar el SensorEventListener para el acelerómetro
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Obtener los valores del acelerómetro
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Actualizar los TextView con los valores del acelerómetro
        txtX.setText("X: " + x);
        txtY.setText("Y: " + y);
        txtZ.setText("Z: " + z);

        // Acción basada en los valores del acelerómetro

        // Ejemplo: Cambiar el color del botón Up si el valor de X es mayor a 5
        if (x > 5) {
            btnUp.setBackgroundColor(Color.RED);
        } else {
            btnUp.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No es necesario implementar este método para el acelerómetro
    }

    private void connectBluetooth() {
        // Obtener los dispositivos Bluetooth emparejados
        pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList = new ArrayList<>();

        // Verificar si hay dispositivos emparejados
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
            }

            // Mostrar un cuadro de diálogo con la lista de dispositivos emparejados
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Selecciona un dispositivo");
            builder.setCancelable(true);

            // Obtener los nombres de los dispositivos emparejados
            CharSequence[] deviceNames = new CharSequence[deviceList.size()];
            for (int i = 0; i < deviceList.size(); i++) {
                deviceNames[i] = deviceList.get(i).getName();
            }

            // Configurar el cuadro de diálogo con los nombres de los dispositivos emparejados
            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Obtener el dispositivo seleccionado
                    BluetoothDevice selectedDevice = deviceList.get(which);

                    // Iniciar una conexión Bluetooth con el dispositivo seleccionado
                    try {
                        bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();
                        outputStream = bluetoothSocket.getOutputStream();
                        Toast.makeText(MainActivity.this, "Conexión establecida", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al establecer la conexión", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Mostrar el cuadro de diálogo
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "No hay dispositivos emparejados", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendBluetoothCommand(String command) {
        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al enviar el comando Bluetooth", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay conexión Bluetooth establecida", Toast.LENGTH_SHORT).show();
        }
    }
}
