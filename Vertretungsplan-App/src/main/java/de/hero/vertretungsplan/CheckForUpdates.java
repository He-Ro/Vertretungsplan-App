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
 * Diese Klasse wird aufgerufen, wenn in den Optionen die Benachrichtigungen eingeschaltet sind.
 * Dies wird in dem eingestellten Intervall geschehen.
 * Dazu wird HtmlWork erweitert zu HtmlWorkAndNotify, welches die Webseite erneut runterlädt und 
 * diese auf Updates überprüft.
 * Falls dies der Fall ist wird dem Benutzer eine Benachrichtigung gezeigt.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CheckForUpdates extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	new HtmlWorkAndNotify(this);
    	return Service.START_NOT_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
   	
	public class HtmlWorkAndNotify extends HtmlWork {
	
		private Context context;
		
		public HtmlWorkAndNotify(Context pContext) {
			super(pContext);
			Log.d("HtmlWorkAndNotify", "Constructor");
			context = pContext;
		}

		@Override
		public void onDataChange() {
			super.onDataChange();
			showNotification();
		}
		
		void showNotification() {
	    	String ns = Context.NOTIFICATION_SERVICE;
	    	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
	    	
	    	int icon = R.drawable.ic_stat_vertretungsplan;
	    	long when = System.currentTimeMillis();
	
	
	    	SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preferencesName), 0);
			String strAktualisierungsText =prefs.getString(context.getString(R.string.datumVertretungsplan), "" );

	    	CharSequence contentText = context.getString(R.string.planFuerDen) + strAktualisierungsText.substring(0, strAktualisierungsText.indexOf("-") - 1);
	    	Intent notificationIntent = new Intent(context, MainActivity.class);
	    	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	
			Notification notification = new NotificationCompat.Builder(context)
										.setDefaults(Notification.DEFAULT_ALL)
										.setAutoCancel(true)
										.setContentIntent(contentIntent)
					.setContentTitle(context.getString(R.string.aktualisierterV_Plan))
										.setContentText(contentText)
										.setWhen(when)
										.setSmallIcon(icon)
										.build();
			
	    	mNotificationManager.notify(1, notification);
		}
	}
}