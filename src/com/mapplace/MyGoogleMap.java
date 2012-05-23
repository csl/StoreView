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

public class MyGoogleMap extends MapActivity 
{
  private String TAG ="MyGoogleMap";
  static public MyGoogleMap my;
  
  private static final int MENU_ADD = Menu.FIRST;
  private static final int MENU_MAP_INC = Menu.FIRST + 1;
  private static final int MENU_MAP_DEC = Menu.FIRST + 2;
  private static final int MENU_MAP_SWITCH = Menu.FIRST + 3;
  private static final int MENU_SEARCH = Menu.FIRST + 4 ;
  
  private MyGoogleMap mMyGoogleMap = this;
  private String strLocationProvider = ""; 

  
  private LocationManager mLocationManager01; 
  private Location mLocation01; 
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private MyOverLay overlay;
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

  private TextView tv;
  private EditText et;
  
  private int str;
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
    WindowManager.LayoutParams.FLAG_FULLSCREEN);    
    
    my = this;
    
    try{
      dbHelper = new SQLiteHelper(this, SQLiteHelper.DB_NAME, null, DB_VERSION);
      db = dbHelper.getWritableDatabase();
    }
    catch(IllegalArgumentException e){
      e.printStackTrace();
      ++ DB_VERSION;
      dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
    }            

    search_list = new ArrayList<store_item>();
    
    str = 1;
   
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 
     
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    intZoomLevel = 18; 
    mMapController01.setZoom(intZoomLevel); 
     
    mLocationManager01 =  
    (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
     
    getLocationProvider(); 
     
    //取得現在位置
    nowGeoPoint = getGeoByLocation(mLocation01); 
    
    if (nowGeoPoint == null)
    {
      //openOptionsDialog("no GPS rec");
      //預設的位置
      nowGeoPoint = new GeoPoint(23, 120);
    }

    refreshMapViewByGeoPoint(nowGeoPoint, 
        mMapView, intZoomLevel);

    //GPS更新資料
    mLocationManager01.requestLocationUpdates 
    (strLocationProvider, 2000, 10, mLocationListener01); 
     
    getMapLocations(true);
    
    overlay = new MyOverLay(this);
    mMapView.getOverlays().add(overlay);
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());

  }
  
  public boolean onCreateOptionsMenu(Menu menu)
  {

    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_ADD, 0 ,R.string.str_button1);
    menu.add(0 , MENU_MAP_INC, 0 ,R.string.str_button2);
    menu.add(0 , MENU_MAP_DEC, 0 ,R.string.str_button3);
    menu.add(0 , MENU_MAP_SWITCH, 0 ,R.string.str_button4);
    menu.add(0 , MENU_SEARCH, 0 ,R.string.str_button4);
    
    return true;  
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Intent intent = new Intent() ;
    
    switch (item.getItemId())
    { 
          case MENU_ADD:
            //nowGeoPoint
            double geoLatitude = 0.0; 
            double geoLongitude = 0.0; 

            if (nowGeoPoint != null)
            {
              geoLatitude = (int)nowGeoPoint.getLatitudeE6()/1E6; 
              geoLongitude = (int)nowGeoPoint.getLongitudeE6()/1E6;
            }
            
            Bundle bundle = new Bundle();
            bundle.putInt("modify", 0);
            bundle.putDouble("geoLatitude", geoLatitude);
            bundle.putDouble("geoLongitude", geoLongitude);

            intent = new Intent();
            intent.setClass(MyGoogleMap.this, AddPlace.class);
            intent.putExtras(bundle);

            startActivity(intent);
            break;
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
          case MENU_MAP_SWITCH:
            
            if (str == 1)
            {
             str = 2;
             mMapView.setStreetView(false);
             mMapView.setSatellite(true);
             mMapView.setTraffic(false);
            }
            else
            {
              str = 1;
              mMapView.setStreetView(true);
              mMapView.setSatellite(false);
              mMapView.setTraffic(false);
            }
            break;
          case MENU_SEARCH:
            AlertDialog.Builder alert = new AlertDialog.Builder(mMyGoogleMap);

            alert.setTitle("Search");
            alert.setMessage("請輸入keyword");
            
            ScrollView sv = new ScrollView(mMyGoogleMap);
            LinearLayout ll = new LinearLayout(mMyGoogleMap);
            ll.setOrientation(LinearLayout.VERTICAL);
            sv.addView(ll);
            tv = new TextView(mMyGoogleMap);
            tv.setText("KEYWORD: ");
            et = new EditText(mMyGoogleMap);
            et.setText("");
            ll.addView(tv);
            ll.addView(et);
            
            // Set an EditText view to get user input 
            alert.setView(sv);
            
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
              //Progress
              myDialog = ProgressDialog.show
              (
                  MyGoogleMap.this,
                  "Search",
                  "",
                  true
              );
              
              new Thread()
              {
                public void run()
                {
                  search_list.clear();
                  String keyword = et.getText().toString();
                  
                  try{
                    cursor = db.query(SQLiteHelper.TB_NAME, null, null, null, null, null, null);

                    cursor.moveToFirst();
                    
                    //no data
                    if (cursor.isAfterLast())
                    {
                      //openOptionsDialog("查無data, 請更新database");
                      myDialog.dismiss();
                      return;
                    }
                    
                    while(!cursor.isAfterLast())
                    {
                      store_item sitem = new store_item();
                      sitem.id = cursor.getString(0);
                      sitem.name = cursor.getString(1);
                      sitem.time = cursor.getString(2);
                      sitem.phone = cursor.getString(3);
                      sitem.addr = cursor.getString(4);
                      sitem.commit = cursor.getString(5);

                      //check
                      int namex = sitem.name.indexOf(keyword);
                      int timex = sitem.time.indexOf(keyword);
                      int phonex = sitem.phone.indexOf(keyword);
                      int addrx = sitem.addr.indexOf(keyword);
                      int commitx = sitem.commit.indexOf(keyword);

                      Log.i(TAG, "found " + namex + "," + timex + "," 
                                                  + phonex + "," + addrx + "," + commitx );

                      if (namex == -1 && timex == -1 && phonex == -1 
                          && addrx == -1 && phonex == -1 && commitx == -1 )
                      {
                        Log.i(TAG, "not found");
                      }
                      else
                      {
                        Log.i(TAG, "found");
                        search_list.add(sitem);
                      }
                      cursor.moveToNext();
                    }   
                  }catch(IllegalArgumentException e){
                    e.printStackTrace();
                    ++ DB_VERSION;
                    dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
                  }         
                  
                  if (search_list.size() != 0)
                  {
                    Intent intent = new Intent();
                    intent.setClass(MyGoogleMap.this, StoreList.class);
                    startActivity(intent);
                  }
                  else
                  {
                    openOptionsDialog("keyword " + keyword + " not found.");
                  }
                 }                 
               }.start();         
                        
            }
            });
          
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                 
                }
              });
          
              alert.show();              
            
            break;
      }
    
  return true ;
  }
  
  
  public List<MapLocation> getMapLocations(boolean doit) 
  {
    if (mapLocations == null || doit == true) 
    {
      mapLocations = new ArrayList<MapLocation>();

      try{
        cursor = db.query(SQLiteHelper.TB_NAME, null, null, null, null, null, null);

        cursor.moveToFirst();
        
        //no data
        if (cursor.isAfterLast())
        {
          //openOptionsDialog("查無data, 請更新database");
          return null;
        }
        
        while(!cursor.isAfterLast())
        {
          store_item sitem = new store_item();
          sitem.id = cursor.getString(0);
          sitem.name = cursor.getString(1);
          sitem.time = cursor.getString(2);
          sitem.phone = cursor.getString(3);
          sitem.addr = cursor.getString(4);
          sitem.commit = cursor.getString(5);
          MapLocation ml = new MapLocation(sitem.name, sitem);
          
          GeoPoint StoreGeoPoint = getGeoByAddress(sitem.addr);
          ml.setPoint(StoreGeoPoint);
          mapLocations.add(ml);
          cursor.moveToNext();
        }   
      }catch(IllegalArgumentException e){
        e.printStackTrace();
        ++ DB_VERSION;
        dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
      }
/*      
      for (MapLocation item : mapLocations)
      {
        Log.i(TAG, Double.toString(item.getDist()));
      }
      Collections.sort(mapLocations, new Comparator<MapLocation>() {
        public int compare(MapLocation o1, MapLocation o2) 
        {
          if (o1.getDist() > o2.getDist())
            return 1;
          else if (o1.getDist() == o2.getDist())
            return 0;
          else
            return -1;
          
        }
      });
  
      //show place
      int count = 0;
      for (MapLocation item : mapLocations)
      {
        //if (showPoint < count) break;
        mapLocations.add(item);
        //openOptionsDialog(count + ", " + Double.toString(item.getDist()));
        count++;
      }
      */    
    }
    return mapLocations;
  }

 
  public final LocationListener mLocationListener01 =  
  new LocationListener() 
  { 
    public void onLocationChanged(Location location) 
    { 
      // TODO Auto-generated method stub 
       
      mLocation01 = location; 
      nowGeoPoint = getGeoByLocation(location); 
      refreshMapViewByGeoPoint(nowGeoPoint, 
            mMapView, intZoomLevel); 
    } 

    public void onProviderDisabled(String provider) 
    { 
      // TODO Auto-generated method stub 
      mLocation01 = null; 
    } 
     
    public void onProviderEnabled(String provider) 
    { 
      // TODO Auto-generated method stub 
       
    } 
     
    public void onStatusChanged(String provider, 
                int status, Bundle extras) 
    { 
      // TODO Auto-generated method stub 
       
    } 
  }; 
   
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
        (MyGoogleMap.this, Locale.getDefault()); 
         
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
  


  public void showPlaced()
  {
    Intent intent = new Intent();
    intent.setClass(MyGoogleMap.this, ShowLocation.class);
    //intent.putExtras(bundle);

    startActivity(intent);

  }


  
  public String getIEMI()
  {
    return  ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
  }
   
  public void getLocationProvider() 
  { 
    try 
    { 
      Criteria mCriteria01 = new Criteria(); 
      mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
      mCriteria01.setAltitudeRequired(false); 
      mCriteria01.setBearingRequired(false); 
      mCriteria01.setCostAllowed(true); 
      mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
      strLocationProvider =  
      mLocationManager01.getBestProvider(mCriteria01, true); 
       
      mLocation01 = mLocationManager01.getLastKnownLocation (strLocationProvider); //?
    } 
    catch(Exception e) 
    { 
      //mTextView01.setText(e.toString()); 
      e.printStackTrace(); 
    } 
  }
  
 /* private class MyItemOverlay extends ItemizedOverlay<OverlayItem>
  {
    private List<OverlayItem> items = new ArrayList<OverlayItem>();
    public MyItemOverlay(Drawable defaultMarker , GeoPoint gp)
    {
      super(defaultMarker);
      items.add(new OverlayItem(gp,"Title","Snippet"));
      populate();
    }
    
    @Override
    protected OverlayItem createItem(int i)
    {
      return items.get(i);
    }
    
    @Override
    public int size()
    {
      return items.size();
    }
    
    @Override
    protected boolean onTap(int pIndex)
    {
      Toast.makeText
      (
        Flora_Expo.this,items.get(pIndex).getSnippet(),
        Toast.LENGTH_LONG
      ).show();
      return true;
    }
  }*/
   
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
  
  public boolean onKeyDown(int keyCode, KeyEvent event) 
  {
    if(keyCode==KeyEvent.KEYCODE_BACK)
    {  
      openOptionsDialog();
      return true;
    }
  
    return super.onKeyDown(keyCode, event);  
  }
  
  private void openOptionsDialog() {
    
    new AlertDialog.Builder(this)
      .setTitle("Exit?")
      .setMessage("Exit?")
      .setNegativeButton("No",
          new DialogInterface.OnClickListener() {
          
            public void onClick(DialogInterface dialoginterface, int i) 
            {
            }
      }
      )
   
      .setPositiveButton("Yes",
          new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialoginterface, int i) 
          {
            android.os.Process.killProcess(android.os.Process.myPid());           
            finish();
          }
          
      }
      )
      
      .show();
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
