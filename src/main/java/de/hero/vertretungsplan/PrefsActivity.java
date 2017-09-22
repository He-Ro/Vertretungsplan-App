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
 * Dies ist die Activity für die Optionen, hier werden die Aktionen ausgeführt,
 * welche durch Ändern der Optionen erforderlich sind, weitere Infos:
 * https://developer.android.com/guide/topics/ui/settings.html
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

public class PrefsActivity extends PreferenceActivity {

	SharedPreferences mySharedPreferences;

	private String getSummaryInterval(String value) {
		if (value.equals("1/2")) {
			return (getString(R.string.halbstuendlich));
		} else if (value.equals("1")) {
			return (getString(R.string.stuendlich));
		} else if (value.equals("3")) {
			return (getString(R.string.alleDreiStunden));
		} else if (value.equals("6")) {
			return (getString(R.string.alleSechsStunden));
		}
		return "";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		mySharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		ListPreference listPref = (ListPreference) findPreference("prefs_benachrichtigungsintervall");
		listPref.setSummary(getSummaryInterval(mySharedPreferences.getString(
				"prefs_benachrichtigungsintervall", "1")));
		listPref.setOnPreferenceChangeListener(new ListPreference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				preference.setSummary(getSummaryInterval(newValue.toString()));

				MainActivity.setNewAlarm(getBaseContext(), true,
						newValue.toString());
				return true;
			}

		});

		Preference klasse = findPreference("prefs_klasse");
		klasse.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				showDialog(R.string.dialog_klasse);
				return true;
			}
		});

		CheckBoxPreference ckBxPref = (CheckBoxPreference) findPreference("prefs_benachrichtigungen");
		ckBxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (preference.getKey().equals("prefs_benachrichtigungen")) {
					MainActivity.setNewAlarm(getBaseContext(),
							!((CheckBoxPreference) preference).isChecked(),
							mySharedPreferences);
				}
				return true;
			}
		});

		CheckBoxPreference ckBxPrefAppUpdate = (CheckBoxPreference) findPreference("prefs_check_for_app_updates");
		ckBxPrefAppUpdate
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (preference.getKey().equals(
								"prefs_check_for_app_updates")) {
							Intent i = new Intent(getBaseContext(),
									CheckForAppUpdate.class);
							i.putExtra("setTimer",
									!((CheckBoxPreference) preference)
											.isChecked());
							if (!((CheckBoxPreference) preference).isChecked()) {
								i.putExtra("checkNow", true);
							}
							getBaseContext().startService(i);
						}
						return true;
					}
				});

		Preference ueber = findPreference("ueber");
		ueber.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Dialog ueberDialog = new Dialog(PrefsActivity.this);

				ueberDialog.setContentView(R.layout.ueber_dialog_layout);
				ueberDialog.setTitle(getString(R.string.ueber));
				ueberDialog.setCancelable(true);
				ueberDialog.setCanceledOnTouchOutside(true);

				TextView dialogText = (TextView) ueberDialog
						.findViewById(R.id.text_in_dialog);
				dialogText.setTextColor(Color.BLACK);
				dialogText.setAutoLinkMask(Linkify.ALL);

				dialogText.setText(String.format(
						getString(R.string.ueberTextFormated),
						getString(R.string.version_nr),
						getString(R.string.emailAdresseEntwickler),
						getString(R.string.webAdresseAppDownload),
						getString(R.string.GNU_GPLwebadresse)));
				ueberDialog.show();
				return true;
			}
		});

		CheckBoxPreference ckBxPrefDebugging = (CheckBoxPreference) findPreference("prefs_debug");
		ckBxPrefDebugging
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (preference.getKey().equals("prefs_debug")) {
							Log.d("PrefsActivity", "new");
							if (!((CheckBoxPreference) preference).isChecked()) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										preference.getContext());

								builder.setMessage(
										getString(R.string.debugText))
										.setTitle(
												getString(R.string.debugTitel));

								AlertDialog dialog = builder.create();
								dialog.show();
							}
						}
						return true;
					}
				});
	}

	/**
	 * Multiple choice Liste fuer die Auswahl der Klassen
	 * https://developer.android.com/guide/topics/ui/settings.html#Custom
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case R.string.dialog_klasse:
			final boolean[] blKlasse = loadBooleanArray(mySharedPreferences, "KlassenBoolean");
			final boolean[] blKlasseCopy = blKlasse.clone();
			return new AlertDialog.Builder(this)
					.setTitle(getString(R.string.klassenAuswaehlen))
					.setMultiChoiceItems(R.array.str_klassen, blKlasse,
							new DialogInterface.OnMultiChoiceClickListener() {
								public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
									blKlasse[whichButton] = isChecked;
								}
							})
					.setPositiveButton(getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									saveBooleanArray(mySharedPreferences, "KlassenBoolean", blKlasse);
									removeDialog(R.string.dialog_klasse);

									SharedPreferences aktualisieren = getSharedPreferences(
											getString(R.string.preferencesName),
											0);
									LocalBroadcastManager
											.getInstance(getBaseContext())
											.sendBroadcast(new Intent("update_list").putExtra(
													getString(R.string.datumZuletztAktualisiert),aktualisieren.getString(
															getString(R.string.datumZuletztAktualisiert),"Noch nicht aktualisiert")));
								}
							})
					.setNegativeButton(getString(R.string.abbrechen),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									saveBooleanArray(mySharedPreferences, "KlassenBoolean", blKlasseCopy);
									removeDialog(R.string.dialog_klasse);
								}
							}).create();
		default:
			return null;
		}
	}	
	
	/**
	 * Saves the array <b>array</b> to the preferences
	 * @param key - name of the preference
	 * @param array - boolean Array with a maximum size of 32 entries
	 * @return true - if successfully written, false otherwise
	 */
	public static boolean saveBooleanArray(SharedPreferences prefs, String key, boolean[] array) {
		SharedPreferences.Editor editor = prefs.edit();
		int bitArray = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i]) {
				bitArray += (int) Math.pow(2.0, i);
			}
		}
		editor.putInt(key, bitArray);
		return editor.commit();
	}

	/** Load a boolean Array, from an integer, which has first been encoded with <b>saveBooleanArray</b>
	 * @param key - the name of the preference
	 * @return the boolean Array
	 */
	public static boolean[] loadBooleanArray(SharedPreferences prefs, String key) {
		int bitArray = prefs.getInt(key, 0);
		boolean[] array = new boolean[32];
		for (int i = 0; i < 32; i++) {
			int rightMostBit = bitArray & 0x01;
			array[i] = rightMostBit == 1 ? true: false;;
			bitArray >>>= 1;
		}
		return array;
	}
}
