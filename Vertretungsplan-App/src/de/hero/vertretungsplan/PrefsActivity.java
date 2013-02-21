package de.hero.vertretungsplan;

/**
 * Dies ist die Activity für die Optionen, hier werden die Aktionen ausgeführt,
 * welche durch Ändern der Optionen erforderlich sind, weitere Infos:
 * https://developer.android.com/guide/topics/ui/settings.html
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        ListPreference listPref = (ListPreference) findPreference("prefs_benachrichtigungsintervall");
        listPref.setSummary(getSummaryInterval(mySharedPreferences.getString("prefs_benachrichtigungsintervall", "1")));
        listPref.setOnPreferenceChangeListener(new ListPreference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				preference.setSummary(getSummaryInterval(newValue.toString()));

				MainActivity.setNewAlarm(getBaseContext(), true, newValue.toString());
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
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (preference.getKey().equals("prefs_benachrichtigungen")) {
					MainActivity.setNewAlarm(getBaseContext(), !((CheckBoxPreference) preference).isChecked());
				}
				return true;
			}
		});
        
        CheckBoxPreference ckBxPrefAppUpdate = (CheckBoxPreference) findPreference("prefs_check_for_app_updates");
        ckBxPrefAppUpdate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (preference.getKey().equals("prefs_check_for_app_updates")) {
					Intent i = new Intent(getBaseContext(), CheckForAppUpdate.class);
					i.putExtra("setTimer", !((CheckBoxPreference) preference).isChecked());
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
			    
			    TextView dialogText = (TextView) ueberDialog.findViewById(R.id.text_in_dialog);
			    dialogText.setTextColor(Color.BLACK);
			    dialogText.setAutoLinkMask(Linkify.ALL);
			    
			    dialogText.setText(String.format(getString(R.string.ueberTextFormated), getString(R.string.version_nr)));
			    ueberDialog.show();
				return true;
			}
		});
        

        CheckBoxPreference ckBxPrefDebugging = (CheckBoxPreference) findPreference("prefs_debug");
        ckBxPrefDebugging.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (preference.getKey().equals("prefs_debug")) {
					Log.d("PrefsActivity","new");
					if (!((CheckBoxPreference) preference).isChecked()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());

						builder.setMessage(getString(R.string.debugText))
						       .setTitle(getString(R.string.debugTitel));

						AlertDialog dialog = builder.create();
						dialog.show();
					}
				}
				return true;
			}
		});
    }
	/**
	 * Hier wollte ich eine Custom ListPreference erstellen, die genutzt wird um die Klassen anzuzeigen
	 * Diese können geändert werden, wenn es z.B. keine 13 mehr gibt.
	 * Jedoch wird es nicht genutzt und zeigt so einfach nur eine Liste an Klassen aus und speichert die  
	 * Daten im KlassenBoolean Array, weitere Infos:
	 * https://developer.android.com/guide/topics/ui/settings.html#Custom
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {

	            case R.string.dialog_klasse:
	            	String[] strKlasse = loadStringArray("KlassenArray", this);
	            	if (strKlasse.length == 0) {
	            		strKlasse = new String[24];
	            		strKlasse[0]="Q13";
	            		strKlasse[1]="Q12";
	            		strKlasse[2]="Q11";
	            		strKlasse[3]="EPH 10";
	            		int j = 4;
	            		for (int i = 9;i>4;i--) {

		            		strKlasse[j]=i + "A";
		            		j++;
		            		strKlasse[j]=i + "B";
		            		j++;
		            		strKlasse[j]=i + "C";
		            		j++;
		            		strKlasse[j]=i + "D";
		            		j++;
	            		}
	            	}
	            	saveStringArray(strKlasse,"KlassenArray",this);
	            	
	                CharSequence[] chSqKlasse = new CharSequence[strKlasse.length];
	                for (int i = 0; i < strKlasse.length; i ++) {
	                	chSqKlasse[i] = strKlasse[i];
	                }

	                final boolean[] blKlasse = loadBooleanArray("KlassenBoolean",this , strKlasse.length);
	                final boolean[] blKlasseCopy = blKlasse.clone();
	                return new AlertDialog.Builder(this).setTitle(
	                        getString(R.string.klassenAuswaehlen)).setMultiChoiceItems(
	                        chSqKlasse, blKlasse,
	                        new DialogInterface.OnMultiChoiceClickListener() {
	                            public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
	                                blKlasse[whichButton] = isChecked;
	                            }
	                        }).setPositiveButton(getString(R.string.ok),
	                        new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
	                                saveBooleanArray( blKlasse,"KlassenBoolean", getBaseContext());
	                                removeDialog(R.string.dialog_klasse);

	                                SharedPreferences aktualisieren = getSharedPreferences(getString(R.string.preferencesName), 0);
	                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent("update_list").putExtra(getString(R.string.aktualisiertKey), aktualisieren.getString(getString(R.string.aktualisiertKey), "Noch nicht aktualisiert")));
	                            }
	                        }).setNegativeButton(getString(R.string.abbrechen),
	                        new DialogInterface.OnClickListener() {
	                            public void onClick(DialogInterface dialog, int whichButton) {
	                                saveBooleanArray( blKlasseCopy,"KlassenBoolean", getBaseContext());
	                                removeDialog(R.string.dialog_klasse);
	                            }
	                        }).create();
	                default: return null;
	    }
	}

	public boolean saveStringArray(String[] array, String arrayName, Context mContext) {   
	    SharedPreferences prefs = mContext.getSharedPreferences("prefs", 0);  
	    SharedPreferences.Editor editor = prefs.edit();  
	    editor.putInt(arrayName +"_size", array.length);  
	    for(int i=0;i<array.length;i++)  
	        editor.putString(arrayName + "_" + i, array[i]);  
	    return editor.commit();  
	} 
	
	public String[] loadStringArray(String arrayName, Context mContext) {  
	    SharedPreferences prefs = mContext.getSharedPreferences("prefs", 0);  
	    int size = prefs.getInt(arrayName + "_size", 0);  
	    String array[] = new String[size];  
	    for(int i=0;i<size;i++)  
	        array[i] = prefs.getString(arrayName + "_" + i, null);  
	    return array;  
	}

	public boolean saveBooleanArray(boolean[] array, String arrayName, Context mContext) {   
	    SharedPreferences prefs = mContext.getSharedPreferences("prefs", 0);  
	    SharedPreferences.Editor editor = prefs.edit();  
	    editor.putInt(arrayName +"_size", array.length);  
	    for(int i=0;i<array.length;i++)  
	        editor.putBoolean(arrayName + "_" + i, array[i]);  
	    return editor.commit();  
	} 
	
	public boolean[] loadBooleanArray(String arrayName, Context mContext, int laenge) {  
		boolean[] array = loadBooleanArray(arrayName,mContext);
		if (array.length != laenge) {
	    	return new boolean[laenge];
	    }
		return array;
	}
	
	public boolean[] loadBooleanArray(String arrayName, Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences("prefs", 0);  
	    int size = prefs.getInt(arrayName + "_size", 0);
	    boolean[] array = new boolean[size];  
	    for(int i=0;i<size;i++)  
	        array[i] = prefs.getBoolean(arrayName + "_" + i, false);  
	    return array;  
	}
	


}
