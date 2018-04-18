/*
 * Copyright (C) 2014 singwhatiwanna(任玉刚) <singwhatiwanna@gmail.com>
 *
 * collaborator:田啸,宋思宇,Mr.Simple
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ryg.dynamicload;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;

import com.ryg.dynamicload.internal.DLAttachable;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLProxyImpl;

public class DLProxyActivity extends Activity implements DLAttachable {

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

//    @Override
//    public String getPackageName() {
//        return impl.getPackageName() == null ? super.getPackageName() : impl.getPackageName();
//    }

    @Override
    public Resources getResources() {
        return impl.getResources() == null ? super.getResources() : impl.getResources();
    }

    @Override
    public Theme getTheme() {
        return impl.getTheme() == null ? super.getTheme() : impl.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        return impl.getClassLoader();
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
    	try {
    		if(DLProxyApplication.instance != null){
    			DLProxyApplication.instance.deleteActivity(this);
    		}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
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
        mRemoteActivity.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mRemoteActivity.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        mRemoteActivity.onBackPressed();
        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return mRemoteActivity.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        return mRemoteActivity.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowAttributesChanged(LayoutParams params) {
        mRemoteActivity.onWindowAttributesChanged(params);
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mRemoteActivity.onWindowFocusChanged(hasFocus);
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mRemoteActivity.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mRemoteActivity.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

}
