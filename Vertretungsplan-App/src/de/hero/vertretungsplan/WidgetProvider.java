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


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

@SuppressLint("NewApi")
@Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                        int[] appWidgetIds) {
    for (int i=0; i<appWidgetIds.length; i++) {
      Intent svcIntent=new Intent(context, WidgetService.class);
      
      svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
      svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
      RemoteViews widget=new RemoteViews(context.getPackageName(), R.layout.widget);
      Intent intent = new Intent(context, de.hero.vertretungsplan.WidgetUpdate.class);
	  PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
      widget.setOnClickPendingIntent(R.id.aktButton, pi);
      Intent startIntent = new Intent(context, MainActivity.class);
      
	  PendingIntent pi2 = PendingIntent.getActivity(context, 0, startIntent, 0);
      widget.setOnClickPendingIntent(R.id.icon, pi2);
      widget.setRemoteAdapter(appWidgetIds[i], R.id.words, svcIntent);
      widget.setTextViewText(R.id.textTitel, context.getString(R.string.app_name));
      appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
    }
    
    super.onUpdate(context, appWidgetManager, appWidgetIds);
  }
}