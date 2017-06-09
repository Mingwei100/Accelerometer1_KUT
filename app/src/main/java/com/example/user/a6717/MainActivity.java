package com.example.user.a6717;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity implements SensorEventListener {
    TextView one, two, three;
    double offsetX, offsetY, offsetZ;
    double[] gravity;
    ArrayList<double[]> history;
    Sensor mySensor;
    SensorManager mySensorManager;
    ArrayList<acPacket> printing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mySensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        gravity = new double[3];
        one = (TextView) findViewById(R.id.xAx);
        two = (TextView) findViewById(R.id.yAx);
        three = (TextView) findViewById(R.id.zAx);
        history = new ArrayList<double[]>();
        printing = new ArrayList<acPacket>();
    }

    public void calibrate(View v) {
        gravity[0] = offsetX;
        gravity[1] = offsetY;
        gravity[2] = offsetZ;

    }

    public double fix(int axis) {
        double temp = 0;
        for (int k = 0; k < history.size(); k++) {
            temp += history.get(k)[axis];
        }
        temp = temp / history.size();
        return Math.round(temp * 100.0) / 100.0;
    }

    public double filter(double x) {
        double ans = x;
        if (x < 0.5) {
            double temp = Math.pow(2, 12.044 * ans);
            ans = (double) (temp) * (1 / 128);
        }
        return ans;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        offsetX = event.values[0];
        offsetY = event.values[1];
        offsetZ = event.values[2];
        double[] temp = new double[3];
        temp[0] = (event.values[0] - (gravity[0]));
        temp[1] = (event.values[1] - (gravity[1]));
        temp[2] = (event.values[2] - (gravity[2]));
        history.add(0, temp);
        if (history.size() > 10) {
            history.remove(10);
        }
        one.setText("X: " + fix(0));
        two.setText("Y: " + fix(1));
        three.setText("Z: " + fix(2));

        printing.add(new acPacket(fix(0),fix(1),fix(2)));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void makeText(View v) {
        Calendar c = Calendar.getInstance();
        String filePath = "Accelerometer_" + c.get(Calendar.YEAR) + "-"
                + (c.get(Calendar.MONTH) + 1) + "-"
                + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR)
                + "-" + c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
                + "_Device_data.txt";
        File dir = new File(Environment.getExternalStorageDirectory()
                + File.separator + "Accelerometer" + File.separator
                + "Logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();

            } catch (IOException e) {
                System.out.println(e);
            }
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            String str = new String("");
            for(int i = 0; i<printing.size(); i++) {
                str = str+ "[" + printing.get(i).getX() + ";" + printing.get(i).getY() + ";" + printing.get(i).getZ()+"],";
                bw.write(str);
                bw.flush();
            }
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
