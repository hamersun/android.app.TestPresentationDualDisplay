package com.example.sheys.testpresentationdualdisplay;

import android.app.Presentation;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;

import com.example.android.apis.graphics.CubeRenderer;

/**
 * Created by SHEYS on 2015/9/10.
 */
public class DemoPresentation extends Presentation {
    private static final String TAG = "DemoPresentation";

    private GLSurfaceView mSurfaceView;

    public DemoPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the resource for the context of the presentation.
        // Notice that we are getting the resources from the context of the presentation.
        Resources r = getContext().getResources();

        // Inflate the layout
        setContentView(R.layout.presentation_with_media_router_content);

        // Set up the surface view for visual interest
        mSurfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.setRenderer(new CubeRenderer(false));
    }

    public GLSurfaceView getSurfaceView() {
        return mSurfaceView;
    }
}
