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
 * Diese Klasse checkt, ob es eine neue Version der App gibt und benachrichtigt den Benutzer gegebenenfalls.
 * Dies wird in der Regel alle 7 Tage ausgeführt, falls die Option dazu eingeschaltet ist.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CheckForAppUpdate extends Service{
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	boolean setTimer = intent.getBooleanExtra("setTimer", false);
    	boolean checkNow = intent.getBooleanExtra("checkNow", false);
    		
		Intent i = new Intent(this, de.hero.vertretungsplan.CheckForAppUpdate.class);
		i.putExtra("checkNow",true);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager am =  (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		am.cancel(pi);
		Log.d("CheckForAppUpdate","Alarm canceled");
		if (setTimer) {
			
			long lngInterval = AlarmManager.INTERVAL_DAY * 7;
			Log.d("CheckForAppUpdate","Alarm set");
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY * 6, lngInterval, pi);
			//For debugging: after 10 seconds
			//am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 25000 , AlarmManager.INTERVAL_HOUR , pi);

		}
    		
    		
    	if (checkNow) {
    		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
		    	try {
					new HtmlWorkAppUpdate(this).execute(new URI(getString(R.string.htmlAdresseAktuelleVersion)));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
    	}
		return START_NOT_STICKY;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class HtmlWorkAppUpdate extends AsyncTask<URI,Void,StringBuffer> {

		private Context context;
		
		public HtmlWorkAppUpdate(Context pContext) {
			context = pContext;
		}
		
		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(StringBuffer strResult) {
			PackageManager manager = context.getPackageManager();
			   PackageInfo info;
			try {
				info = manager.getPackageInfo(context.getPackageName(), 0);

				if (Integer.parseInt(strResult.toString()) > info.versionCode) {
					showAppUpdateNotification();
				} else {
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/**
		 * Diese Funktion zeigt eine Benachrichtigung, welche den Benutzer 
		 * darüber informiert, dass es eine neue Version der App gibt.
		 * Beim Klicken der Benachrichtigung wird die Download-Webseite geöffnet.
		 */
		
		private void showAppUpdateNotification() {

	    	String ns = Context.NOTIFICATION_SERVICE;
	    	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
	    	
	    	int icon = R.drawable.ic_stat_vertretungsplan;
	    	long when = System.currentTimeMillis();
	
	        String title = "Es gibt ein App-Update";
	    	CharSequence contentTitle = title;
	    	CharSequence contentText = "Besuche die Webseite zum Herunterladen";

			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.webAdresseAppDownload)));
	    	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, browserIntent, 0);
			Notification notification = new NotificationCompat.Builder(context)
										.setDefaults(Notification.DEFAULT_ALL)
										.setAutoCancel(true)
										.setContentIntent(contentIntent)
										.setContentTitle(contentTitle)
										.setContentText(contentText)
										.setWhen(when)
										.setSmallIcon(icon)
										.build();
	    	mNotificationManager.notify(1, notification);
		}
		
		@Override
		protected StringBuffer doInBackground(URI... uriHtmlList) {


			URI strHtml = uriHtmlList[0];
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(strHtml);
			HttpResponse response;
			try {
				response = httpClient.execute(httpGet, localContext);
				StringBuffer strResult = new StringBuffer();
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "ISO-8859-1"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.length() > 0 && line.charAt(0) != '#') {
						strResult.append(line);
					}
				}
				return strResult;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
