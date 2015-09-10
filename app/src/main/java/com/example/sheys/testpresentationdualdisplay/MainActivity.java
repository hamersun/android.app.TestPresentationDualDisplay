package com.example.sheys.testpresentationdualdisplay;

import android.app.Activity;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRouter;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.android.apis.graphics.CubeRenderer;

public class MainActivity extends Activity {

    private final String TAG = "PresentationWithMediaRouterActivity";

    private MediaRouter mMediaRouter;
    private DemoPresentation mPresentation;
    private GLSurfaceView mSurfaceView;
    private TextView mInfoTextView;
    private boolean mPaused;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the media router service
        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);

        setContentView(R.layout.activity_main);

        // Set up the surface view for visual interest
        mSurfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.setRenderer(new CubeRenderer(false));

        // Get a text view where we will show information about what's happening
        mInfoTextView = (TextView) findViewById(R.id.info);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listen for changes to media routes
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);

        // Update the presentation based on the currently selected route.
        mPaused = false;
        updatePresentation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop listening for changes to media routes
        mMediaRouter.removeCallback(mMediaRouterCallback);

        // Pause rendering
        mPaused = true;
        updateContents();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Dismiss the presentation when the activity is not visible
        if (mPresentation != null) {
            Log.i(TAG, "Dismissing presentation because the activity is no longer visible");
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) mediaRouteMenuItem.getActionProvider();
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

        // Return true to show the menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_media_route) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updatePresentation() {
        // Get the current route and its presentation display
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            mPresentation = new DemoPresentation(this, presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation! Display was removed in the meantime.", ex);
                mPresentation = null;
            }
        }

        // Update the content playing in this activity
        updateContents();
    }

    private void updateContents() {
        // Show either the content in the main activity or the content in the presentation
        // along with some descriptive text about what is happening.
        if (mPresentation != null) {
            Log.d(TAG, "showing on remote display" +  mPresentation.getDisplay().getName());
            mInfoTextView.setText(getResources().getString(R.string.presentation_with_media_router_now_playing_remotely,
                    mPresentation.getDisplay().getName()));
            mSurfaceView.setVisibility(View.INVISIBLE);
            mSurfaceView.onPause();
            if (mPaused) {
                mPresentation.getSurfaceView().onPause();
            } else {
                mPresentation.getSurfaceView().onResume();
            }
        } else {
            Log.d(TAG, "showing on built-in display: " + getWindowManager().getDefaultDisplay().getName());
            mInfoTextView.setText(getResources().getString(R.string.presentation_with_media_router_now_playing_locally,
                    getWindowManager().getDefaultDisplay().getName()));
            mSurfaceView.setVisibility(View.VISIBLE);
            if (mPaused) {
                mSurfaceView.onPause();
            } else {
                mSurfaceView.onResume();
            }
        }
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback = new MediaRouter.SimpleCallback() {
        @Override
        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
            updatePresentation();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
            updatePresentation();
        }

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
            updatePresentation();
        }
    };

    /**
     * Listens for when presentations are dismissed!
     */
    private final DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (dialog == mPresentation) {
                Log.i(TAG, "Presentation was dismissed.");
                mPresentation = null;
                updateContents();
            }
        }
    };

}
