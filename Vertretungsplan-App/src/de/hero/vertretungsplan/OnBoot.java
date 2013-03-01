package de.hero.vertretungsplan;

/* 
    Vertretungsplan-App
    Copyright (C) 2013  Hendrik Rosendahl

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * Diese Klasse wird beim Hochfahren des Android-Smartphones aufgerufen.
 * Es setzt verschiedene Timer, falls diese eingeschaltet sind.
 */

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


