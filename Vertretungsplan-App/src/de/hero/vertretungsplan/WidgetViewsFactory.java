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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

@SuppressLint("NewApi")
public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private ArrayList<HashMap<String, String>> lstEintraege;

	private Context context = null;
	//private int appWidgetId;

	public WidgetViewsFactory(Context ctxt, Intent intent) {
		this.context = ctxt;
		//appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	public void onCreate() {
		Log.d("WidgetViewsFactory", "onCreate");
		lstEintraege = getListe();
	}

	public void onDestroy() {
		// no-op
	}

	public int getCount() {
		checkListe();
		return lstEintraege.size();
	}

	public RemoteViews getViewAt(int position) {

		checkListe();

		HashMap<String, String> eintrag = lstEintraege.get(position);

		RemoteViews row = new RemoteViews(context.getPackageName(),
				R.layout.custom_row_view);
		row.setTextViewText(R.id.textFach1, (CharSequence) eintrag.get("fach1"));
		row.setTextViewText(R.id.textFach2, (CharSequence) eintrag.get("fach2"));
		row.setTextViewText(
				R.id.textStunde,
				((CharSequence) eintrag.get("stunde")).subSequence(0,
						((CharSequence) eintrag.get("stunde")).length() - 4)
						+ ".");
		row.setTextViewText(R.id.textVertreter,
				(CharSequence) eintrag.get("vertreter"));
		row.setTextViewText(R.id.textKlassen,
				(CharSequence) eintrag.get("klassen"));
		row.setTextViewText(R.id.textRaum, (CharSequence) eintrag.get("raum"));
		row.setTextViewText(R.id.textText, (CharSequence) eintrag.get("text"));
		return (row);
	}

	public RemoteViews getLoadingView() {
		return (null);
	}

	public int getViewTypeCount() {
		return (1);
	}

	public long getItemId(int position) {
		return (position);
	}

	public boolean hasStableIds() {
		return (true);
	}

	public void onDataSetChanged() {

		lstEintraege = getListe();
		Log.d("WidgetViewsFactory", "onDataSetChanged()");
		SharedPreferences prefs = context.getSharedPreferences(
				context.getString(R.string.preferencesName), 0);
		String strAktualisierungsText = prefs.getString(
				context.getString(R.string.aktualisierungsTextKey), "");
		StringBuffer str = new StringBuffer(strAktualisierungsText);

		RemoteViews widget = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		widget.setTextViewText(R.id.textTitel,
				str.subSequence(0, str.indexOf("-")));
		widget.setTextViewText(R.id.textUntertitel,
				str.subSequence(str.indexOf("-") + 2, str.length()));
		AppWidgetManager appManager = AppWidgetManager.getInstance(context);
		ComponentName name = new ComponentName(context, WidgetProvider.class);

		appManager.partiallyUpdateAppWidget(appManager.getAppWidgetIds(name),
				widget);

	}

	private ArrayList<HashMap<String, String>> getListe() {
		ArrayList<HashMap<String, String>> lstEintraege = null;
		try {
			FileInputStream fis;
			fis = context.openFileInput(context.getString(R.string.filename));
			ObjectInputStream ois = new ObjectInputStream(fis);
			lstEintraege = (ArrayList<HashMap<String, String>>) ois.readObject();
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lstEintraege;
	}

	private void checkListe() {
		if (lstEintraege == null) {
			Log.d("WidgetViewsFactory", "checkListe()");
			lstEintraege = getListe();
		}
	}
}
