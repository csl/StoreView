package com.mapplace;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class ShowLocation extends Activity 
{ 
  public final String TAG = "";

  private TextView icontent;
  private Button mSubmit;
  
  private String type_id = null;
  private String type_id_str = null;
  private String name = null;
  private String telephone = null;
  private String website = null;
  private String address = null;
  private double latitude = 0.0;
  private double longitude = 0.0;  

  private String LocationInfo;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      // TODO Auto-generated method stub
      super.onCreate(savedInstanceState);
      setContentView(R.layout.showlocation);
      
      findViews();
      setListensers();
/*
      type_id = MyGoogleMap.mSelectedMapLocation.getLocationStruct().gettypeid();
      name = MyGoogleMap.mSelectedMapLocation.getLocationStruct().getname();
      telephone = MyGoogleMap.mSelectedMapLocation.getLocationStruct().gettelephone();
      website = MyGoogleMap.mSelectedMapLocation.getLocationStruct().getwebsite();
      address = MyGoogleMap.mSelectedMapLocation.getLocationStruct().getaddress();
      latitude = MyGoogleMap.mSelectedMapLocation.getLocationStruct().getlatitude();
      longitude = MyGoogleMap.mSelectedMapLocation.getLocationStruct().getlongitude();

      String uriAPI = "http://2.raywebstory.appspot.com/search_type.jsp?type_id=" +  type_id;
      
      //XML Parser
      URL url = null;
      try{
          url = new URL(uriAPI);
          
          SAXParserFactory spf = SAXParserFactory.newInstance();
          SAXParser sp = spf.newSAXParser();
                
          XMLReader xml_reader = sp.getXMLReader();
                
          //Using handler for XML
          TypeXMLHandler TypeXML = new TypeXMLHandler();
          xml_reader.setContentHandler(TypeXML);
          
          //open connection
          xml_reader.parse(new InputSource(url.openStream()));
          typeXMLs = TypeXML.getXMLStruct();
       }
      catch(Exception e)
       {
        e.printStackTrace();
       }      
      
      type_id_str = typeXMLs.gettype();
      
      LocationInfo = "\n\n景點: " + type_id_str + "\n\n" +
                     "景點名稱：" + name + "\n\n" +
                     "電話：" + telephone + "\n\n" +
                     "網址： "+ website + "\n\n" +
                     "地址： "+ address + "\n\n" +
                     "GPS： "+ latitude + "," + longitude + "\n";
      
      icontent.setText(LocationInfo);
      */
  }
  
  private void findViews() 
  {
    mSubmit = (Button) findViewById(R.id.lnew_exit);
    icontent = (TextView) findViewById(R.id.lcontent);
  }
  
  private void setListensers()
  {
    // TODO Auto-generated method stub
    mSubmit.setOnClickListener(bexit);
  }
  
  private Button.OnClickListener bexit = new Button.OnClickListener() {
    public void onClick(View v) 
    {
        finish();
    }
  };  

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
           finish();
         }
         }
        )
    .show();
  }

  
}
  
  
