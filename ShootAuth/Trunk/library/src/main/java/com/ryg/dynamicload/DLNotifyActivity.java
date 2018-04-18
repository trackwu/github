package com.ryg.dynamicload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.utils.DLConstants;

/**
 * Created by Ben on 2015/4/15.
 */
public class DLNotifyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        DLIntent dlIntent = new DLIntent(intent.getStringExtra(DLConstants.EXTRA_PACKAGE), intent.getStringExtra(DLConstants.EXTRA_CLASS));
        dlIntent.putExtra(DLConstants.EXTRA_RESERVE, intent.getStringExtra(DLConstants.EXTRA_RESERVE));
        dlIntent.putExtras(intent.getExtras());

        try {
        	String reserve = intent.getStringExtra(DLConstants.EXTRA_RESERVE);
        	if(reserve != null && reserve.equals("frompush")){
        		
        	}else{
        		if(DLProxyApplication.instance != null){
        			DLProxyApplication.instance.closeActivities();
        		}
        	}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        DLPluginManager.getInstance(this).startPluginActivity(this, dlIntent);
        finish();
    }
}
