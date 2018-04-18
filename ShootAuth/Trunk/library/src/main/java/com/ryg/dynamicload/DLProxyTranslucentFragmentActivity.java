package com.ryg.dynamicload;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.ryg.dynamicload.internal.DLAttachable;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLProxyImpl;

/**
 * Created by Administrator on 2015/5/18.
 */
public class DLProxyTranslucentFragmentActivity extends FragmentActivity implements DLAttachable {

    protected DLPlugin mRemoteActivity;
    private DLProxyImpl impl = new DLProxyImpl(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            if (savedInstanceState != null) {
                impl.onRecoverInstanceState(savedInstanceState, getIntent());
            }
            impl.initEnv(getIntent());
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                Intent intent = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
            impl.onCreate(savedInstanceState);
            if(DLProxyApplication.instance != null){
            	DLProxyApplication.instance.addActivity(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void attach(DLPlugin remoteActivity, DLPluginManager pluginManager) {
        mRemoteActivity = remoteActivity;
    }

    @Override
    public AssetManager getAssets() {
        return impl.getAssets() == null ? super.getAssets() : impl.getAssets();
    }

    @Override
    public Resources getResources() {
        return impl.getResources() == null ? super.getResources() : impl.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        return impl.getTheme() == null ? super.getTheme() : impl.getTheme();
    }


    @Override
    public ClassLoader getClassLoader() {
        return impl.getClassLoader() == null ? super.getClassLoader() : impl.getClassLoader();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mRemoteActivity.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        if (mRemoteActivity != null) {
            mRemoteActivity.onPostCreate(savedInstanceState);
        }
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        if (mRemoteActivity != null) {
            mRemoteActivity.onStart();
        }
        super.onStart();
    }

    @Override
    protected void onRestart() {
        if (mRemoteActivity != null) {
            mRemoteActivity.onRestart();
        }
        super.onRestart();
    }

    @Override
    protected void onResume() {
        if (mRemoteActivity != null) {
            mRemoteActivity.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mRemoteActivity != null) {
            mRemoteActivity.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mRemoteActivity != null) {
            mRemoteActivity.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
    	if(DLProxyApplication.instance != null){
    		DLProxyApplication.instance.deleteActivity(this);
    	}
        if (mRemoteActivity != null) {
            mRemoteActivity.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        impl.onSaveInstanceState(outState);
        mRemoteActivity.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mRemoteActivity != null) {
            mRemoteActivity.onRestoreInstanceState(savedInstanceState);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mRemoteActivity != null) {
            mRemoteActivity.onNewIntent(intent);
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (mRemoteActivity != null) {
            mRemoteActivity.onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRemoteActivity != null) {
            super.onTouchEvent(event);
            return mRemoteActivity.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mRemoteActivity != null) {
            super.onKeyUp(keyCode, event);
            return mRemoteActivity.onKeyUp(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (mRemoteActivity != null) {
            mRemoteActivity.onWindowAttributesChanged(params);
        }
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mRemoteActivity != null) {
            mRemoteActivity.onWindowFocusChanged(hasFocus);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mRemoteActivity != null) {
            mRemoteActivity.onCreateOptionsMenu(menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mRemoteActivity != null) {
            mRemoteActivity.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

}