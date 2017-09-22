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
 * Dies ist die Activity des Hauptbildschirms. Es ist eine ListActivity, da die Liste das zentrale Element
 * dieses Bildschirmes ist.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends ListActivity {

	private View mRefreshIndeterminateProgressView; // save inflated layout for
													// reference
	private MenuItem refreshItem; // reference to actionbar menu item we want to
									// swap

	SharedPreferences sharedPreferences;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mRefreshIndeterminateProgressView == null) {
				LayoutInflater lInflater = (LayoutInflater) this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mRefreshIndeterminateProgressView = lInflater.inflate(
						R.layout.actionbar_indeterminate_progress, null);
				refreshItem = menu.findItem(R.id.menu_aktualisieren);
				Log.d("MainActivity",
						"onCreateOptionsMenu: refreshItem created");
			}
		}
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_open_web:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.webAdresse)));
			startActivity(browserIntent);
			return true;
		case R.id.preferences_button:

			Intent settingsActivity = new Intent(getBaseContext(),
					PrefsActivity.class);
			startActivity(settingsActivity);
			return true;
		case R.id.menu_aktualisieren:

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				if (mRefreshIndeterminateProgressView == null) {
					LayoutInflater inflater = (LayoutInflater) this
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					mRefreshIndeterminateProgressView = inflater.inflate(
							R.layout.actionbar_indeterminate_progress, null);
					refreshItem = item;
				}
				refreshItem.setActionView(mRefreshIndeterminateProgressView); // replace
																				// actionbar
																				// menu
																				// item
																				// with
																				// progress
				// doing refreshItem.setActionView(null) removes the animation
			}
			doAll();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
				new IntentFilter("update_list"));
		LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
				new Intent("update_list"));

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		int lastRunVersion = sharedPreferences.getInt("lastRunVersion", 0);

		PackageManager manager = getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(getPackageName(), 0);

			if (lastRunVersion != info.versionCode) {
				if (sharedPreferences.getBoolean("prefs_check_for_app_updates",
						true)) {
					Intent i = new Intent(this, CheckForAppUpdate.class);
					i.putExtra("setTimer", true);
					i.putExtra("checkNow", true);
					startService(i);
				}
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("lastRunVersion", info.versionCode);
				editor.commit();

			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (sharedPreferences.getBoolean("prefs_akt_on_start", false)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
					&& refreshItem != null) {
				refreshItem.setActionView(mRefreshIndeterminateProgressView);
			}
			doAll();
		}
	}

	public static void setNewAlarm(Context context, boolean set, String interval) {
		Intent i = new Intent(context,
				de.hero.vertretungsplan.CheckForUpdates.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		am.cancel(pi); // cancel any existing alarms
		Log.d("MainActivity", "cancelAlarm");
		if (set) {

			long lngInterval;
			switch (interval) {
				case "1/2":
					lngInterval = AlarmManager.INTERVAL_HALF_HOUR;
					break;
				case "1":
					lngInterval = AlarmManager.INTERVAL_HOUR;
					break;
				case "6":
					lngInterval = AlarmManager.INTERVAL_HALF_DAY / 2;
					break;
				case "3":
				default:
					lngInterval = AlarmManager.INTERVAL_HOUR * 3;
					break;
			}
			Log.d("MainActivity", "setAlarm " + interval + " Stunden");
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime()
							+ AlarmManager.INTERVAL_HALF_HOUR / 3, lngInterval,
					pi);
			// For debugging after 10 seconds
			// am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
			// SystemClock.elapsedRealtime() + 25000 ,
			// AlarmManager.INTERVAL_HOUR , pi);

		}
	}

	public static void setNewAlarm(Context context, boolean set,
			SharedPreferences sharedPreferences) {
		String interval = sharedPreferences.getString(
				"prefs_benachrichtigungsintervall", "1");
		setNewAlarm(context, set, interval);
	}

	@SuppressLint("NewApi")
	public void updateWidget() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			AppWidgetManager appManager = AppWidgetManager.getInstance(this);
			ComponentName name = new ComponentName(this, WidgetProvider.class);
			appManager.notifyAppWidgetViewDataChanged(
					appManager.getAppWidgetIds(name), R.id.words);
		}
	}

	@SuppressLint("NewApi")
	public void doAll() {
		ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
			new HtmlWorkAndShow(this);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
				&& refreshItem != null) {
			refreshItem.setActionView(null);
		}
	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				ArrayList<Map<String, String>> lstEintraege;
				FileInputStream fis = openFileInput(getString(R.string.filename));
				ObjectInputStream ois = new ObjectInputStream(fis);
				lstEintraege = (ArrayList<Map<String, String>>) ois.readObject();
				MyListAdapter adapter = new MyListAdapter(getBaseContext(),lstEintraege, getKlasse());
				setListAdapter(adapter);
				String datumZuletztAktualisiert = sharedPreferences.getString(getString(R.string.datumZuletztAktualisiert), "");
				TextView textUntereLeiste = (TextView) findViewById(R.id.textUntereLeiste);
				textUntereLeiste.setText(getString(R.string.zuletztAktualisiertAm) + " " + datumZuletztAktualisiert);

				String datumVertretungsplan = sharedPreferences.getString(getString(R.string.datumVertretungsplan), "");
				TextView textDatum = (TextView) findViewById(R.id.textDatum);

				DateFormat dfm = new SimpleDateFormat("dd.MM.yy");
				Date dat = new Date();
				dat = dfm.parse(datumVertretungsplan);
				textDatum.setText(datumVertretungsplan);

				Date heute = new Date(System.currentTimeMillis());
				Calendar cal = Calendar.getInstance();
				cal.setTime(dat);
				if (heute.before(dat)) {
					textDatum.setBackgroundResource(R.drawable.green_shape);
				} else if (heute.getDate() == dat.getDate()
						&& heute.getMonth() == dat.getMonth()
						&& heute.getYear() == dat.getYear()) {

					textDatum.setBackgroundResource(R.drawable.yellow_shape);
				} else {
					textDatum.setBackgroundResource(R.drawable.red_shape);

				}
				String aOderBWoche = sharedPreferences.getString("aOderBWoche", "Woche");
				TextView textWoche = (TextView) findViewById(R.id.textWoche);
				textWoche.setText(aOderBWoche);

				ois.close();
				fis.close();
			} catch (FileNotFoundException e) {
				doAll();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (refreshItem != null
					&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				refreshItem.setActionView(null);
			}
			updateWidget();
		}
	};

	public boolean[] loadBooleanArray(String arrayName) {
		return PrefsActivity.loadBooleanArray(sharedPreferences, arrayName);
	}

	public Set<String> getKlasse() {
		String[] strArray = this.getResources().getStringArray(R.array.str_klassen);
		boolean[] bolArray = loadBooleanArray("KlassenBoolean");

		Set<String> strKlasse = new HashSet<String>();
		for (int i = 0; i < strArray.length; i++) {
			if (bolArray[i]) {
				strKlasse.add(strArray[i]);
			}
		}
		return strKlasse;
	}

	public class HtmlWorkAndShow extends HtmlWork {
		private Context context;

		public HtmlWorkAndShow(Context pContext) {
			super(pContext);
			context = pContext;
		}

		@Override
		public void onPostExecute(StringBuffer str) {
			super.onPostExecute(str);

			LocalBroadcastManager.getInstance(context).sendBroadcast(
					new Intent("update_list"));
		}
	}

}