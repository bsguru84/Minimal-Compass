package developersnotes.net.minimalcompass;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Gururaj on 1/9/2016.
 */
public class MyGLSurfaceView extends GLSurfaceView{

    //private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context){
        super(context);

        setEGLContextClientVersion(2);

        //mRenderer = new MyGLRenderer();
        //setRenderer(mRenderer);

        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


    }
}
