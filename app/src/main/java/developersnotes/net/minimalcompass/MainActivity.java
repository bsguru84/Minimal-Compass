package developersnotes.net.minimalcompass;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer,SensorEventListener {

    private TextureRenderer mTexturerenderer;
    private Bitmap mCompassBitmap;
    private int[] mTexture = new int[1];
    private boolean mInitialized = false;

    private float[] accelVals = null;
    private float[] compassVals = null;

    private double mRotationInDegress;

    private Sensor accelerometer;
    private Sensor magnetometer;
    private SensorManager mSensorManager;

    private android.support.v7.app.ActionBar mActionBar;

    GLSurfaceView mSurfaceView;
    TextView mAngle;

    static final float ALPHA = 0.05f;

    private String TAG ="compass";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "onCreate Call!");

        super.onCreate(savedInstanceState);

        mActionBar =  getSupportActionBar();
        mActionBar.hide();

        setContentView(R.layout.activity_main);

        mSurfaceView = (GLSurfaceView)findViewById(R.id.surface);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mAngle = (TextView)findViewById(R.id.angle);

        mTexturerenderer = new TextureRenderer();

        // Get a reference to the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get a reference to the accelerometer
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Get a reference to the magnetometer
        magnetometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Exit unless both sensors are available
        if (null == accelerometer || null == magnetometer)
            finish();

    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);

        mSensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_GAME);

        //mSurfaceView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
       //mSurfaceView.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Acquire accelerometer event data

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //mGravity = new float[3];
            //System.arraycopy(event.values, 0, mGravity, 0, 3);
            accelVals = lowPass( event.values.clone(), accelVals );

        }

        // Acquire magnetometer event data

        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

           // mGeomagnetic = new float[3];
            //System.arraycopy(event.values, 0, mGeomagnetic, 0, 3);
            compassVals = lowPass( event.values.clone(), compassVals );

        }

        // If we have readings from both sensors then
        // use the readings to compute the device's orientation
        // and then update the display.

        if (accelVals != null && compassVals != null) {

            float rotationMatrix[] = new float[9];

            // Users the accelerometer and magnetometer readings
            // to compute the device's rotation with respect to
            // a real world coordinate system

            boolean success = SensorManager.getRotationMatrix(rotationMatrix,
                    null, accelVals, compassVals);

            if (success) {

                float orientationMatrix[] = new float[3];

                // Returns the device's orientation given
                // the rotationMatrix

                SensorManager.getOrientation(rotationMatrix, orientationMatrix);

                // Get the rotation, measured in radians, around the Z-axis
                // Note: This assumes the device is held flat and parallel
                // to the ground

                float rotationInRadians = orientationMatrix[0];

                // Convert from radians to degrees
                mRotationInDegress = Math.toDegrees(rotationInRadians);

                if(mRotationInDegress < 0.0)
                    mRotationInDegress += 360.0;

                if(mTexturerenderer != null)
                    mTexturerenderer.setRotation((float) mRotationInDegress);
                if(mSurfaceView != null)
                    mSurfaceView.requestRender();

                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.CEILING);
                mAngle.setText(df.format(mRotationInDegress)+"\u00B0");

                // Reset sensor event data arrays
                //mGravity = mGeomagnetic = null;

                //Log.i("compass" , "Rotation in Degrees : " + Math.toDegrees(orientationMatrix[0]) + "  " + Math.toDegrees(orientationMatrix[1])  + "  " + Math.toDegrees(orientationMatrix[2]));

            }
        }

    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG,"onSurfaceCreated Call!");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //Log.i(TAG,"onSurfaceChanged Call!");

        mTexturerenderer.updateViewSize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //Log.i(TAG, "onDrawFrame Call!");

        if(!mInitialized){
            mTexturerenderer.init();

            mCompassBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.compass);
            mTexturerenderer.updateTextureSize(mCompassBitmap.getWidth(), mCompassBitmap.getHeight());

            GLES20.glGenTextures(1, mTexture, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture[0]);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mCompassBitmap, 0);

            //Log.i(TAG, "onDrawFrame : Bitmap Width " + mCompassBitmap.getWidth() + "Height : " + mCompassBitmap.getHeight());

            GLToolbox.initTexParams();
            mInitialized = true;
        }
        mTexturerenderer.renderTexture(mTexture[0]);
    }
}
