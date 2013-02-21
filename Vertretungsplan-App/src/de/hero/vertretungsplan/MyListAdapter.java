package de.hero.vertretungsplan;

/**
 * Diese Klasse bewirkt, dass die der Hintergrund der Einträge hervorgehoben wird, die 
 * in den Optionen unter Klassen eingetragen sind.
 */

import java.util.HashMap;
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
	public MyListAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to, Set<String> pStrFilter) {
		super(context, items, resource, from, to);
		strFilter = pStrFilter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		String strKlasse =((TextView)view.findViewById(R.id.textKlassen)).getText().toString();
		String[] arrKlasse = strKlasse.split(",");
		for (int i = 0; i < arrKlasse.length; i ++) {
			if (strFilter.contains(arrKlasse[i].trim())) {
				view.setBackgroundColor(Color.parseColor("#C7C7C7"));
				return view;
			}
		}
		
		view.setBackgroundColor(Color.WHITE);
		return view;
	}
}
