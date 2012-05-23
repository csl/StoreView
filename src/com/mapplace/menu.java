package com.mapplace;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class menu extends ListActivity 
{ 
  private static final int MENU_START = Menu.FIRST  ;
  private static final int MENU_EXIT = Menu.FIRST +1 ;
  public final String TAG = "";
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      // TODO Auto-generated method stub
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      
      //Checking Status
      if (CheckInternet(3))
      {
          String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
          if(provider == null)
          {
           openOptionsDialog("NO GPS");
          }
      }
      else
      {
        openOptionsDialog("NO Internet");
      } 
  }
  
  
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_START, 0 ,R.string.menu_start).setIcon(R.drawable.start)
    .setAlphabeticShortcut('S');
    menu.add(0 , MENU_EXIT, 1 ,R.string.menu_exit).setIcon(R.drawable.exit)
    .setAlphabeticShortcut('E');
  return true;  
  }
  
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Intent intent = new Intent() ;
    
    switch (item.getItemId())
      { 
          case MENU_START:    
             intent.setClass(menu.this,MyGoogleMap.class);
             startActivity(intent);
             return true;
      
          case MENU_EXIT:
             openOptionsDialog();
    
             break ;
      }
    
  return true ;
  }
  
  private boolean CheckInternet(int retry)
  {
    boolean has = false;
    for (int i=0; i<=retry; i++)
    {
      has = HaveInternet();
      if (has == true) break;       
    }
    
  return has;
  }
  
  private boolean HaveInternet()
  {
     boolean result = false;
     
     ConnectivityManager connManager = (ConnectivityManager) 
                                getSystemService(Context.CONNECTIVITY_SERVICE); 
      
     NetworkInfo info = connManager.getActiveNetworkInfo();
     
     if (info == null || !info.isConnected())
     {
       result = false;
     }
     else 
     {
       if (!info.isAvailable())
       {
         result =false;
       }
       else
       {
         result = true;
       }
   }
  
   return result;
  }
  
  private void openOptionsDialog() {
    
    new AlertDialog.Builder(this)
      .setTitle(R.string.msg_exit)
      .setMessage(R.string.str_exit_msg)
      .setNegativeButton(R.string.str_exit_no,
          new DialogInterface.OnClickListener() {
          
            public void onClick(DialogInterface dialoginterface, int i) {
              
            }
      }
      )
   
      .setPositiveButton(R.string.str_exit_ok,
          new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialoginterface, int i) {
            
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
           finish();
         }
         }
        )
    .show();
  }

  
}
  
  
