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
 * Diese Klasse bewirkt, dass der Hintergrund der Einträge hervorgehoben wird, die 
 * in den Optionen unter Klassen eingetragen sind.
 */

import java.util.Map;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MyListAdapter extends SimpleAdapter {
	private Set<String> strFilter;
	public MyListAdapter(Context context, List<Map<String, String>> items, Set<String> pStrFilter) {
		super(context, items, R.layout.custom_row_view,
        		new String[] {"stunde","fach1","vertreter","fach2","klassen","raum","text"},
        		new int[] {R.id.textStunde,R.id.textFach1,R.id.textVertreter,R.id.textFach2,R.id.textKlassen,R.id.textRaum,R.id.textText});
		strFilter = pStrFilter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		// Hole dir den String mit den Klassen aus dem Vertretungstext
		String strKlasse =((TextView)view.findViewById(R.id.textKlassen)).getText().toString();
		// Falls es mehrere Klassen Betrifft, z.B. "10A, 10B, 10D" wird der String hier aufgesplittet
		String[] arrKlasse = strKlasse.split(",");
		for (String klasse : arrKlasse) {
			if (strFilter.contains(klasse.trim())) {
				// setze den hervorgehobenen Hintergrund
				view.setBackgroundColor(Color.parseColor("#C7C7C7"));
				return view;
			}
		}
		view.setBackgroundColor(Color.WHITE);
		return view;
	}
}
