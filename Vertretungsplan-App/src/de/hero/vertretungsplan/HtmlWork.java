package de.hero.vertretungsplan;

/**
 * HtmlWork ist die zentrale Klasse, welche den Vertretungsplan von 
 * http://www.gymnasium-sankt-michael.de/vertretungsplan/vertretungsplan.html
 * herunterlädt und die Tabelle extrahiert, speichert sie und überprüft, ob sie sich verändert hat.
 * 
 * Zu erwähnen ist, dass es sich hierbei um einen AsyncTask handelt.
 * Dies wird von Android gefordert, damit die App flüssig läuft.
 * https://developer.android.com/reference/android/os/AsyncTask.html
 */

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
 */

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class HtmlWork extends AsyncTask<URI,Void,StringBuffer> {

	private Context context;
	private boolean dataChanged = false;
	
	public HtmlWork(Context pContext) {
		context = pContext;
		Log.d("HtmlWork","Construktor");
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
			try {
				SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				
				if (mySharedPreferences.getBoolean("prefs_debug", false)) {
					execute(new URI(context.getString(R.string.debug_htmlAdresse)));
				} else {
					execute(new URI(context.getString(R.string.htmlAdresse)));
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onPostExecute(StringBuffer strResult) {
		try {
			extractTable(strResult);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SharedPreferences aktualisieren = context.getSharedPreferences(context.getString(R.string.preferencesName), 0);
		
		SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		Date date = new Date();
		String dateString = context.getString(R.string.zuletztAktualisiertAm) + " " + fmt.format(date);
		
        SharedPreferences.Editor editor = aktualisieren.edit();
        Log.d("jetzt",dateString);
        editor.putString(context.getString(R.string.aktualisiertKey), dateString);
		editor.commit();
		
		//Widget Stuff
		updateWidget(false);
	}
	
	@SuppressLint("NewApi")
	private void updateWidget(boolean before) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			RemoteViews widget=new RemoteViews(context.getPackageName(), R.layout.widget);
		  	AppWidgetManager appManager = AppWidgetManager.getInstance(context);	
		  	ComponentName name = new ComponentName(context,WidgetProvider.class);
			if(before) {
				widget.setImageViewResource(R.id.aktButton, R.drawable.ic_action_aktualisieren_pressed);
			} else {
				widget.setImageViewResource(R.id.aktButton, R.drawable.aktualisieren_drawable);
				if (dataChanged) {
					appManager.notifyAppWidgetViewDataChanged(appManager.getAppWidgetIds(name), R.id.words);	
				}
			}
		  	appManager.partiallyUpdateAppWidget(appManager.getAppWidgetIds(name), widget);	
        }
	}
	
	@SuppressLint("NewApi")
	@Override
	protected StringBuffer doInBackground(URI... uriHtmlList) {
		
		//Widget Stuff
		updateWidget(true);
		
		
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
				strResult.append(line);
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
	


	public void extractTable(StringBuffer strHtml) throws IOException {

		ArrayList<HashMap<String,String>> lstEintraege = new ArrayList<HashMap<String,String>>();
		
		
		HashMap<String,String> hmEintrag = new HashMap<String,String>();
		
	    int intIndex = strHtml.indexOf("<table");
	    strHtml.delete(0, intIndex - 1);
	    intIndex = strHtml.indexOf("</table");
	    strHtml.delete(intIndex, strHtml.length() - 1);
	    DateFormat dfm = new SimpleDateFormat("dd.MM.yyyy");
    	Date dat = new Date();
    	try {
			dat = dfm.parse(strHtml.substring(strHtml.indexOf(":") + 2));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int date = dat.getDate();
		int month = dat.getMonth() + 1;
    	
		String strAktualisierungsText =  ((date < 10) ? "0" + date : date)  + "." + ((month < 10) ? "0" + month : month) + "." + (dat.getYear() + 1900) + " - " + strHtml.substring(strHtml.indexOf("<td>") + 4,strHtml.indexOf("</td"));
        Log.d("HtmlWork",strAktualisierungsText);
	    SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preferencesName), 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.aktualisierungsTextKey), strAktualisierungsText);
		editor.commit();

	    intIndex = strHtml.indexOf("<tr>");
	    int intIndex2 = strHtml.indexOf("</tr>");
	    strHtml.delete(0, intIndex2 + 4);
	    intIndex = strHtml.indexOf("<tr>");
	    intIndex2 = strHtml.indexOf("</tr>");
	    
	    strHtml.delete(0, intIndex2 + 4);
	    intIndex2 = strHtml.indexOf("</tr>");
	    strHtml.delete(0, intIndex2 + 4);
	    intIndex = strHtml.indexOf("<tr>");
	    intIndex2 = strHtml.indexOf("</tr>");
	    while (intIndex != -1) {
	      StringBuffer strZeile = new StringBuffer(strHtml.substring(intIndex + 4, intIndex2));
	      int intIndex4 = strZeile.indexOf("</t");
	      strZeile.delete(0,intIndex4 + 5);
	      int intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");	  

	      String stunde = strZeile.substring(intIndex3 + 4, intIndex4);
	      hmEintrag.put("stunde",  stunde + ((stunde.endsWith(".")) ? " Stunde" : ". Stunde"));
	      
	      strZeile.delete(intIndex3, intIndex4 + 4);
	      intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");	    
	      hmEintrag.put("fach1", strZeile.substring(intIndex3 + 4, intIndex4));
	      
	      strZeile.delete(intIndex3, intIndex4 + 4);
	      intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");	    
	      hmEintrag.put("vertreter", strZeile.substring(intIndex3 + 4, intIndex4));
	      
	      strZeile.delete(intIndex3, intIndex4 + 4);
	      intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");
	      hmEintrag.put("fach2", strZeile.substring(intIndex3 + 4, intIndex4));
	      
	      strZeile.delete(intIndex3, intIndex4 + 4);
	      intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");
	      hmEintrag.put("klassen", strZeile.substring(intIndex3 + 4, intIndex4));
	      
	      strZeile.delete(intIndex3, intIndex4 + 4);
	      intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");	    
	      hmEintrag.put("raum", strZeile.substring(intIndex3 + 4, intIndex4));
	      
	      strZeile.delete(intIndex3, intIndex4 + 4);
	      intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");	
	      
	      String strTemp = strZeile.substring(intIndex3 + 4, intIndex4);
	      
	      strZeile.delete(intIndex3, intIndex4 + 4);
	      intIndex3 = strZeile.indexOf("<t");
	      intIndex4 = strZeile.indexOf("</t");	
	      
	      hmEintrag.put("text", strTemp + " " + strZeile.substring(intIndex3 + 4, intIndex4));
	      
	      lstEintraege.add( hmEintrag);
	      
	      hmEintrag = new HashMap<String,String>();
	      
	      strHtml.delete(0,intIndex2 + 2);
	      intIndex = strHtml.indexOf("<tr>");
	      intIndex2 = strHtml.indexOf("</tr>");
	    }
	    FileOutputStream fos = context.openFileOutput(context.getString(R.string.filename), Context.MODE_PRIVATE);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    

		int oldHash = prefs.getInt(context.getString(R.string.v_plan_hash_value), 0);
	    
	    oos.writeObject(lstEintraege);
	    oos.close();
	    fos.close();
	    int newHash = lstEintraege.hashCode();
	    editor.putInt(context.getString(R.string.v_plan_hash_value), newHash);
	    editor.commit();

		Log.d("HtmlWork","oldHash: " + oldHash + "; newHash: " + newHash);
		if (oldHash != newHash) {
			onDataChange();
		}
	}
	
	public void onDataChange() {
		dataChanged = true;
	}
}
