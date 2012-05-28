package com.mapplace;

//import java.util.ArrayList;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List; 
import java.util.Locale; 

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context; 
import android.content.DialogInterface;
import android.content.Intent; 
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.graphics.drawable.Drawable;
import android.location.Address; 
import android.location.Criteria; 
import android.location.Geocoder; 
import android.location.Location; 
import android.location.LocationListener; 
import android.location.LocationManager; 
import android.os.Bundle; 
import android.os.Message;
//import android.util.Log;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; 
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import com.google.android.maps.GeoPoint; 
//import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapController; 
import com.google.android.maps.MapView; 
import com.sqlite.SQLiteHelper;
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;
import com.sqlite.store_item;

public class MapLocationView extends MapActivity 
{
  private String TAG ="MapLocationView";
  static public MapLocationView my;
  
  private static final int MENU_MAP_INC = Menu.FIRST;
  private static final int MENU_MAP_DEC = Menu.FIRST + 1;
  private static final int MENU_EXIT = Menu.FIRST + 2;
  
  private MapLocationView mMyGoogleMap = this;
  private String strLocationProvider = ""; 
  
  private LocationManager mLocationManager01; 
  private Location mLocation01; 
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private MapLocationViewOverLay overlay;
  private List<MapLocation> mapLocations;
  
  public List<store_item> search_list;

  private int intZoomLevel=0;//geoLatitude,geoLongitude; 
  public GeoPoint nowGeoPoint;
  
  private static int DB_VERSION = 1;
  
  private SQLiteDatabase db;
  private SQLiteHelper dbHelper;
  private Cursor cursor;

  public ProgressDialog myDialog;
  
  public static  MapLocation mSelectedMapLocation;  

  public String name;
  public String time;
  public String phone;
  public String addr;
  public String commit;
  
  private Bundle bunde;
  private Intent intent;
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 

    
    my = this;
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 
     
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    intZoomLevel = 18; 
    mMapController01.setZoom(intZoomLevel); 
     
    //Fetch data form Inquire    
    intent = this.getIntent();
    bunde = intent.getExtras();
    
    if (bunde != null)
    {
      name = bunde.getString("name");
      time = bunde.getString("time");
      phone = bunde.getString("phone");
      addr = bunde.getString("addr");
      commit = bunde.getString("commit");
    }

    //���o�n�w�쪺��m
    nowGeoPoint = getGeoByAddress(addr);
    
    if (nowGeoPoint != null)
    {
      refreshMapViewByGeoPoint(nowGeoPoint, 
          mMapView, intZoomLevel);
    }
    
    overlay = new MapLocationViewOverLay(this);
    mMapView.getOverlays().add(overlay);
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());

  }
  
  public boolean onCreateOptionsMenu(Menu menu)
  {

    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_MAP_INC, 0 ,R.string.str_button2);
    menu.add(0 , MENU_MAP_DEC, 0 ,R.string.str_button3);
    menu.add(0 , MENU_EXIT, 0 ,R.string.msg_exit);
    
    return true;  
  }

  public boolean onOptionsItemSelected(MenuItem item)
  {
    Intent intent = new Intent() ;
    
    switch (item.getItemId())
    {
      case MENU_MAP_INC:
        
        intZoomLevel++; 
        if(intZoomLevel>mMapView.getMaxZoomLevel()) 
        { 
          intZoomLevel = mMapView.getMaxZoomLevel(); 
        } 
        mMapController01.setZoom(intZoomLevel); 
        
        break;
      case MENU_MAP_DEC:

        intZoomLevel--; 
        if(intZoomLevel<1) 
        { 
          intZoomLevel = 1; 
        } 
        mMapController01.setZoom(intZoomLevel); 
        
        break;
      case MENU_EXIT:
        finish();
        break;
    }  
    
    return true;
   }
  
  

  private GeoPoint getGeoByLocation(Location location) 
  { 
    GeoPoint gp = null; 
    try 
    { 
      if (location != null) 
      { 
        double geoLatitude = location.getLatitude()*1E6; 
        double geoLongitude = location.getLongitude()*1E6; 
        gp = new GeoPoint((int) geoLatitude, (int) geoLongitude); 
      } 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
    return gp; 
  } 
   
  private GeoPoint getGeoByAddress(String strSearchAddress) 
  { 
    GeoPoint gp = null; 
    try 
    { 
      if(strSearchAddress!="") 
      { 
        Geocoder mGeocoder01 = new Geocoder 
        (MapLocationView.this, Locale.getDefault()); 
         
        List<Address> lstAddress = mGeocoder01.getFromLocationName
                           (strSearchAddress, 10);
        if (!lstAddress.isEmpty()) 
        { 
          /*for (int i = 0; i < lstAddress.size(); ++i)
          {
            Address adsLocation = lstAddress.get(i);
            //Log.i(TAG, "Address found = " + adsLocation.toString()); 
            double geoLatitude = adsLocation.getLatitude();
            double geoLongitude = adsLocation.getLongitude();
          } */
          Address adsLocation = lstAddress.get(0); 
          double geoLatitude = adsLocation.getLatitude()*1E6; 
          double geoLongitude = adsLocation.getLongitude()*1E6; 
          gp = new GeoPoint((int) geoLatitude, (int) geoLongitude); 
        }
        
      } 
    } 
    catch (Exception e) 
    {  
      e.printStackTrace();  
    } 
    return gp; 
  } 
   
  public static void refreshMapViewByGeoPoint 
  (GeoPoint gp, MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(gp); 
      myMC.setZoom(zoomLevel); 
      //mapview.setSatellite(false);
      
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  } 
   
  public static void refreshMapViewByCode 
  (double latitude, double longitude, 
      MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      GeoPoint p = new GeoPoint((int) latitude, (int) longitude); 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(p); 
      myMC.setZoom(zoomLevel); 
      mapview.setSatellite(false); 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  } 
   
  private String GeoPointToString(GeoPoint gp) 
  { 
    String strReturn=""; 
    try 
    { 
      if (gp != null) 
      { 
        double geoLatitude = (int)gp.getLatitudeE6()/1E6; 
        double geoLongitude = (int)gp.getLongitudeE6()/1E6; 
        strReturn = String.valueOf(geoLatitude)+","+
          String.valueOf(geoLongitude); 
      } 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
    return strReturn; 
  }
  


  public String getIEMI()
  {
    return  ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
  }
   
  @Override 
  protected boolean isRouteDisplayed() 
  { 
    // TODO Auto-generated method stub 
    return false; 
  } 
  
  protected void onDestroy() {
    super.onDestroy();
    if (dbHelper != null) {
      dbHelper.close();
    }
}  
  
  //show message
  public void openOptionsDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
         }
         }
        )
    .show();
  }
}
