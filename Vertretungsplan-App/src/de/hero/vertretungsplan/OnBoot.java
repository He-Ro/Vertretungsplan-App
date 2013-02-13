package de.hero.vertretungsplan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class OnBoot extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent arg1) {

		SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(((ContextWrapper) context).getBaseContext());

		if (mySharedPreferences.getBoolean("prefs_benachrichtigungen", false)) {
			
			MainActivity.setNewAlarm(context,true);
		}
		if (mySharedPreferences.getBoolean("prefs_check_for_app_updates", false)) {

			Intent i = new Intent(((ContextWrapper) context).getBaseContext(),CheckForAppUpdate.class);
			i.putExtra("setTimer",true);
			i.putExtra("checkNow",true);
			((ContextWrapper) context).getBaseContext().startService(i);
		}
	}
}


