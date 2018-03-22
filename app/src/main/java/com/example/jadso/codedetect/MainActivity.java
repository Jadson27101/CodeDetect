package com.example.jadso.codedetect;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import android.Manifest;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    SurfaceView cameraPreview;
    TextView txtResult;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    LinearLayout qrScanLayout, logoLayout;
    final int RequestCameraPermissionID = 1001;
    FragmentMain fragmentMain;
    FragmentQrCreate fragmentQrCreate;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview =(SurfaceView) findViewById(R.id.preview_camera);
        qrScanLayout = (LinearLayout) findViewById(R.id.qrScanLayout);
        txtResult = (TextView) findViewById(R.id.scannedMessage);
        logoLayout = (LinearLayout) findViewById(R.id.logoLayout);
        fragmentMain = new FragmentMain();
        fragmentQrCreate = new FragmentQrCreate();
        fragmentTransaction = getFragmentManager().beginTransaction();
        qrScanLayout.setVisibility(View.GONE);
        fragmentTransaction.add(R.id.frgmentContainer, fragmentMain);
        fragmentTransaction.commit();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation1);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.remove(fragmentMain);
                        switch (item.getItemId()) {
                            case R.id.action_map:
                                fragmentTransaction.remove(fragmentQrCreate);
                                qrScanLayout.setVisibility(View.VISIBLE);
                                scanQr();
                                break;
                            case R.id.action_dial:
                                qrScanLayout.setVisibility(View.GONE);
                                fragmentTransaction.replace(R.id.frgmentContainer,fragmentQrCreate);
                                break;
                            case R.id.action_mail:

                                break;
                        }
                        fragmentTransaction.commit();
                        return false;
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }
    protected void scanQr(){
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();

            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if(qrcodes.size() != 0) {
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            txtResult.setText(qrcodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
    }

    public void Error1() {
        AlertDialog.Builder AlertDialog = new AlertDialog.Builder(this);
        AlertDialog.setTitle("Error 1!").setMessage("Sorry. Your device can't support camera");
        AlertDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface AlertDialog, int i) {
                AlertDialog.cancel();
            }
        });
        AlertDialog alert = AlertDialog.create();
        alert.show();
    }


}
