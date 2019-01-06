package com.example.pmendes.directionapp;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements Subscriber {

    private ImageView mImageView;

    private ImageView mArrowUp;
    private ImageView mArrowRight;
    private ImageView mArrowDown;
    private ImageView mArrowLeft;

    private SensorManager mSensorManager;
    private OrientationSource mSensor;

    private TCPServer mServer;
    private OrientationEventListener mOrientationEventListener;

    @Override
    public void Update(OrientationAnalyzer.DIRECTION direction) {
        setArrowsInvisible();
        if(mode == ORIENTATION.PORTRAIT){
            if(direction == OrientationAnalyzer.DIRECTION.DOWN)
                mArrowDown.setVisibility(View.VISIBLE);
            if(direction == OrientationAnalyzer.DIRECTION.LEFT)
                mArrowLeft.setVisibility(View.VISIBLE);
            if(direction == OrientationAnalyzer.DIRECTION.TOP)
                mArrowUp.setVisibility(View.VISIBLE);
            if(direction == OrientationAnalyzer.DIRECTION.RIGHT)
                mArrowRight.setVisibility(View.VISIBLE);
        }else{
            if(direction == OrientationAnalyzer.DIRECTION.DOWN)
                mArrowRight.setVisibility(View.VISIBLE);
            if(direction == OrientationAnalyzer.DIRECTION.LEFT)
                mArrowUp.setVisibility(View.VISIBLE);
            if(direction == OrientationAnalyzer.DIRECTION.TOP)
                mArrowLeft.setVisibility(View.VISIBLE);
            if(direction == OrientationAnalyzer.DIRECTION.RIGHT)
                mArrowDown.setVisibility(View.VISIBLE);
        }
    }

    private void setArrowsInvisible(){
        mArrowLeft.setVisibility(View.INVISIBLE);
        mArrowDown.setVisibility(View.INVISIBLE);
        mArrowRight.setVisibility(View.INVISIBLE);
        mArrowUp.setVisibility(View.INVISIBLE);
    }

    public enum ORIENTATION {
        PORTRAIT,
        LANDSCAPE
    }
    private ORIENTATION mode = ORIENTATION.PORTRAIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mServer = new TCPServer(9000);

        final OrientationAnalyzer orientationAnalyzer = new OrientationAnalyzer(ORIENTATION.PORTRAIT);
        orientationAnalyzer.Add(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = new OrientationSource(mSensorManager, orientationAnalyzer);
        mSensor.Start();

        mArrowDown = findViewById(R.id.downArrow);
        mArrowUp = findViewById(R.id.topArrow);
        mArrowRight = findViewById(R.id.rightArrow);
        mArrowLeft = findViewById(R.id.leftArrow);

        final Button connectionButton = findViewById(R.id.connectionButton);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleConnectionButton(view);
            }
        });
        final Button ChangeOrientation = findViewById(R.id.backgroundButton);
        ChangeOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == ORIENTATION.PORTRAIT){
                    mImageView.setRotation(-90);
                    mode = ORIENTATION.LANDSCAPE;
                    ChangeOrientation.setText("To Portrait");
                }else{
                    mImageView.setRotation(0);
                    mode = ORIENTATION.PORTRAIT;
                    ChangeOrientation.setText("To Landscape");
                }
                orientationAnalyzer.setOrientation(mode);
            }
        });

        mImageView = findViewById(R.id.phone);
        mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL)
        {
            @Override
            public void onOrientationChanged(int orientation)
            {
                int angle = 90*Math.round(orientation / 90);
                Log.d("Angle","Angle: "+angle);
            }
        };

        if(mOrientationEventListener.canDetectOrientation())
        {
            mOrientationEventListener.enable();
        }
    }

    private void ToggleConnectionButton(View view) {
        try{
            if(!mServer.IsConnected()){
                ((Button)view).setText("Waiting connection");
                mServer.SetupConnection();
                ((Button)view).setText("Connected!");
            }else{
                ((Button)view).setText("Disconnecting");
                mServer.CloseConnection();
                ((Button)view).setText("Accept Connections");
            }
        }catch (Exception e){
            for (StackTraceElement stackTraceElement:
            e.getStackTrace()) {
                Log.e("Exception", stackTraceElement.toString());
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mSensor != null){
            if(mSensor.IsReceivingData()){
                mSensor.Stop();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensor.Start();
    }
}