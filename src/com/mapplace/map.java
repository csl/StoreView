package com.mapplace; 

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.io.IOException;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle; 
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; 
import android.widget.Button; 
import android.widget.TextView;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.Activity;
import android.os.Environment;

import java.io.File;

public class map extends MapActivity
{
  private TextView mTextView; 
  private Button mButton01;
  private Button mButton02;
  private Button mButton03;
  private Button mButton04;
  private Button mButton05;

  private Bundle bunde;
  private Intent intent;
  
  private MapView mMapView;
  private MapController mMapController; 
  private LocationManager mLocationManager;
  private Location mLocation;
  private String mLocationPrivider="";
  private int zoomLevel=0;
  private GeoPoint gp1;
  private GeoPoint gp2;
  private boolean _run=false;  
  private double distance=0;
  
  private File sdcardDir;
  private String fn;
  private String Filename;
  private String actionUrl;
  
  //KML FIle use
  private java.io.BufferedWriter bw;
  
  private String name;
  private String desc;
  
  private String coordinates;
  //private String start_point;
  //private String end_point;

  @Override 
  public void onCreate(Bundle savedInstanceState) 
  { 
    super.onCreate(savedInstanceState); 
    setContentView(R.layout.addplace); 

    //save KML file
    fn = "map.kml";    
    sdcardDir = Environment.getExternalStorageDirectory();
    Filename  = sdcardDir + java.io.File.separator + fn;    
    coordinates="";
    
    //input URL
    actionUrl="http://IP/upload.php";
    
    //fetch data form new_track
    intent=this.getIntent();
    bunde = intent.getExtras();
    
    name = bunde.getString("name");
    desc = bunde.getString("desc");
    
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController = mMapView.getController();
    
    mTextView = (TextView)findViewById(R.id.textView01);
    mButton01 = (Button)findViewById(R.id.trButton1);
    mButton02 = (Button)findViewById(R.id.trButton2);
    mButton03 = (Button)findViewById(R.id.trButton3);
    mButton04 = (Button)findViewById(R.id.trButton4);
    mButton05 = (Button)findViewById(R.id.trButton5);
    
    zoomLevel = 17; 
    mMapController.setZoom(zoomLevel); 
   
    mLocationManager = (LocationManager)
                       getSystemService(Context.LOCATION_SERVICE); 
    getLocationPrivider();
    if(mLocation!=null)
    {
      gp1=getGeoByLocation(mLocation); 
      gp2=gp1;
      refreshMapView();
      mLocationManager.requestLocationUpdates(mLocationPrivider,
          2000, 10, mLocationListener);   
    }
    else
    {
      new AlertDialog.Builder(map.this).setTitle("")
      .setMessage(getResources().getString(R.string.str_message))
      .setNegativeButton("",new DialogInterface.OnClickListener()
       {
         public void onClick(DialogInterface dialog, int which)
         {
           map.this.finish();
         }
       })
       .show();
    }
    
    mButton01.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        gp1=gp2;
        resetOverlay();
        setStartPoint();
        refreshMapView();
        distance=0;
        mTextView.setText("");
        _run=true;
      } 
    });
    
    mButton02.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        setEndPoint();
        refreshMapView();
        _run=false;

       //Write KML File
       try{
         //create KML file
         bw = new java.io.BufferedWriter(new java.io.FileWriter(
                                         new java.io.File(Filename)));
         
         String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

         bw.write(header, 0, header.length());
         bw.newLine();

         String kml_header = "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n" 
                         + "<Document><name>map.kml</name>";

         bw.write(kml_header, 0, kml_header.length());
         bw.newLine();

         String style_map = "<StyleMap id=\"msn_ylw-pushpin_copy7\">\n"
                   + "<Pair>\n"
                         + "<key>normal</key>\n" 
                         + "<styleUrl>#sn_ylw-pushpin_copy7</styleUrl></Pair>\n"
                         + "<Pair>" 
                         + "<key>highlight</key>\n" 
                         + "<styleUrl>#sh_ylw-pushpin_copy7</styleUrl></Pair>\n" 
                         + "</StyleMap>";

         bw.write(style_map, 0, style_map.length());
         bw.newLine();
         
         String line_style = "<Style id=\"style\"><IconStyle><Icon>\n"
                         + "<href>http://maps.google.com/mapfiles/kml/pal5/icon14.png</href></Icon>\n" 
                         + "</IconStyle>\n"
                         + "<LineStyle>\n"
                         + "<color>7f00ffff</color>\n"
                         + "<width>4</width></LineStyle></Style>";

         bw.write(line_style, 0, line_style.length());
         bw.newLine();
          
         String map_icon = "<Style id=\"sn_ylw-pushpin_copy7\">\n" 
           + "<IconStyle><scale>1.1</scale><Icon>\n"
           + "<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>\n"
           + "</Icon>\n"
           + "<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>\n"
           + "</IconStyle>\n"
           + "</Style>";

         bw.write(map_icon, 0, map_icon.length());
         bw.newLine();
         
         String GPS_Data_Header = "<Folder>\n" 
           + "<name>GPS map</name>\n"
           + "<open>1</open>\n"
           + "<Placemark>\n"
           + "<name>" + name + "</name>\n"
           + "<description>" + desc + "</description>\n"
           + "<styleUrl>#style</styleUrl>\n"
           + "<LineString>\n"
           + "<coordinates>";

         bw.write(GPS_Data_Header, 0, GPS_Data_Header.length());
         bw.newLine();
                  
         String GPS_Date_Tail = coordinates + "</coordinates>\n"
           + "</LineString></Placemark>" 
/*           
           + "<Folder>\n" 
           + "<Placemark>\n" 
           + "<name>WalkStart</name>\n" 
           + "<styleUrl>#msn_ylw-pushpin_copy7</styleUrl>\n" 
           + "<Point><coordinates>" + start_point + "</coordinates></Point>\n" 
           + "</Placemark>\n" 
           + "<Placemark>\n" 
           + "<name>End</name>" 
           + "<styleUrl>#msn_ylw-pushpin_copy7</styleUrl>" 
           + "<Point><coordinates>" + end_point + "</coordinates></Point>"
           + "</Placemark>" 
           + "</Folder>"
*/          
           + "</Folder>" 
           + "</Document></kml>";
         
         bw.write(GPS_Date_Tail, 0, GPS_Date_Tail.length());
         bw.newLine();
         bw.close();
        }
       catch(IOException e)
        {
        e.printStackTrace();
        }
       
       openOptionsDialog("create kml file " + fn + " success.");
      } 
    }); 
    
    mButton03.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        zoomLevel--; 
        if(zoomLevel<1) 
        { 
          zoomLevel = 1; 
        } 
        mMapController.setZoom(zoomLevel); 
      } 
    }); 
    
    mButton04.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        zoomLevel++; 
        if(zoomLevel>mMapView.getMaxZoomLevel()) 
        { 
          zoomLevel = mMapView.getMaxZoomLevel(); 
        } 
        mMapController.setZoom(zoomLevel); 
      }
    }); 
    
    mButton05.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        uploadFile(Filename);
      }
    });     
}
  
  private void uploadFile(String uploadFile)
  {
    String end = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    
    
    try
    {
      URL url =new URL(actionUrl);
      HttpURLConnection con=(HttpURLConnection)url.openConnection();
      con.setDoInput(true);
      con.setDoOutput(true);
      con.setUseCaches(false);

      //method
      con.setRequestMethod("POST");
      /* setRequestProperty */
      con.setRequestProperty("Connection", "Keep-Alive");
      con.setRequestProperty("Charset", "UTF-8");
      con.setRequestProperty("Content-Type",
                         "multipart/form-data;boundary="+boundary);

      DataOutputStream ds = 
        new DataOutputStream(con.getOutputStream());
      
      ds.writeBytes(twoHyphens + boundary + end);
      ds.writeBytes("Content-Disposition: form-data; " +
                    "name=\"file1\";filename=\"" +
                    fn +"\"" + end);
      ds.writeBytes(end);   

      FileInputStream fStream = new FileInputStream(uploadFile);

      int bufferSize = 1024;
      byte[] buffer = new byte[bufferSize];

      int length = -1;

      while((length = fStream.read(buffer)) != -1)
      {
        ds.write(buffer, 0, length);
      }
      
      ds.writeBytes(end);
      ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

      /* close streams */
      fStream.close();
      ds.flush();
      
      InputStream is = con.getInputStream();
      int ch;
      StringBuffer b =new StringBuffer();
      while( ( ch = is.read() ) != -1 )
      {
        b.append( (char)ch );
      }

      //openOptionsDialog(b.toString().trim());

      ds.close();
      openOptionsDialog("upload kml file " + fn + " success.");

    }
    catch(Exception e)
    {
      openOptionsDialog(""+e);
    }
  }  
  
  
  public final LocationListener mLocationListener = 
    new LocationListener() 
  { 
    public void onLocationChanged(Location location) 
    { 
      if(_run)
      {
        gp2=getGeoByLocation(location);
        
        double geoLatitude = (int)gp2.getLatitudeE6()/1E6;
        double geoLongitude = (int)gp2.getLongitudeE6()/1E6;
        double geoaltitude = 0;
         
        coordinates = coordinates +  geoLongitude + "," 
                                  + geoLatitude  + "," 
                                  + geoaltitude + " ";
        
        setRoute();
        refreshMapView();
        distance+=GetDistance(gp1,gp2);
        mTextView.setText(":"+format(distance)+"M"); 

        gp1=gp2;
      }  
    } 
     
    public void onProviderDisabled(String provider) 
    { 
    } 
    public void onProviderEnabled(String provider) 
    { 
    } 
    public void onStatusChanged(String provider,int status,
                                Bundle extras) 
    { 
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
  
  public void getLocationPrivider() 
  { 
    Criteria mCriteria01 = new Criteria();
    mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
    mCriteria01.setAltitudeRequired(false); 
    mCriteria01.setBearingRequired(false); 
    mCriteria01.setCostAllowed(true); 
    mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
    
    mLocationPrivider = mLocationManager
                        .getBestProvider(mCriteria01, true); 
    mLocation = mLocationManager
                .getLastKnownLocation(mLocationPrivider); 
  }
  
  private void setStartPoint() 
  {  
    int mode=1;
    //MyOverLay mOverlay = new MyOverLay(gp1,gp2,mode); 
    //List<Overlay> overlays = mMapView.getOverlays(); 
    //overlays.add(mOverlay);
  }

  private void setRoute() 
  {  
    int mode=2;
    //MyOverLay mOverlay = new MyOverLay(gp1,gp2,mode); 
    //List<Overlay> overlays = mMapView.getOverlays(); 
    //overlays.add(mOverlay);
  }

  private void setEndPoint() 
  {  
    int mode=3;
    //MyOverLay mOverlay = new MyOverLay(gp1,gp2,mode); 
    //List<Overlay> overlays = mMapView.getOverlays(); 
    //overlays.add(mOverlay);
  }

  private void resetOverlay() 
  {
    List<Overlay> overlays = mMapView.getOverlays(); 
    overlays.clear();
  } 

  public void refreshMapView() 
  { 
    mMapView.displayZoomControls(true); 
    MapController myMC = mMapView.getController(); 
    myMC.animateTo(gp2); 
    myMC.setZoom(zoomLevel); 
    mMapView.setSatellite(false); 
  } 
  

  public double GetDistance(GeoPoint gp1,GeoPoint gp2)
  {
    double Lat1r = ConvertDegreeToRadians(gp1.getLatitudeE6()/1E6);
    double Lat2r = ConvertDegreeToRadians(gp2.getLatitudeE6()/1E6);
    double Long1r= ConvertDegreeToRadians(gp1.getLongitudeE6()/1E6);
    double Long2r= ConvertDegreeToRadians(gp2.getLongitudeE6()/1E6);

    double R = 6371;
    double d = Math.acos(Math.sin(Lat1r)*Math.sin(Lat2r)+
               Math.cos(Lat1r)*Math.cos(Lat2r)*
               Math.cos(Long2r-Long1r))*R;
    return d*1000;
  }

  private double ConvertDegreeToRadians(double degrees)
  {
    return (Math.PI/180)*degrees;
  }
  

  public String format(double num)
  {
    NumberFormat formatter = new DecimalFormat("###");
    String s=formatter.format(num);
    return s;
  }
  
  @Override
  protected boolean isRouteDisplayed()
  {
    return false;
 
  }
  
  //error message
  private void openOptionsDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("Tsi_flora")
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
