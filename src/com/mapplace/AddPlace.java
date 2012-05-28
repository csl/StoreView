package com.mapplace;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.google.android.maps.GeoPoint;
import com.sqlite.SQLiteHelper;
import com.sqlite.store_item;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddPlace extends Activity 
{ 
  private String TAG = "AddPlace";
  
  private Button mSubmit;
  private Button mCancel;
  private Spinner type_id;
  private EditText name;
  private EditText stime;
  private EditText telephone;
  private EditText address;
  private EditText commit;
  
  static AddPlace ap;
  
  TextView tv;
  EditText et;
  //private TextView gps_data;
    
  private double geoLatitude;
  private double geoLongitude;
  
  private Bundle bunde;
  private Intent intent;
  private int modify;
  
  //display use
  private ArrayList<HashMap<String, Object>> Location_list;
  private ListView booking_view;
  
  private static final int MENU_DELETE = Menu.FIRST;
  private static final int MENU_IMPORT = Menu.FIRST + 1;
  private static final int MENU_EXPORT = Menu.FIRST + 2;

  private int cindex;
  
  private static int DB_VERSION = 1;

  private SQLiteDatabase db;
  private SQLiteHelper dbHelper;
  private Cursor cursor;

  private File vSDCard = null;
  
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.addplace);

    
    ap = this;
    
    try{
      dbHelper = new SQLiteHelper(this, SQLiteHelper.DB_NAME, null, DB_VERSION);
      db = dbHelper.getWritableDatabase();
    }
    catch(IllegalArgumentException e){
      e.printStackTrace();
      ++ DB_VERSION;
      dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
    }            
    
    
    modify = 0;

    //get GPS data
    geoLatitude = 0;
    geoLongitude = 0;
    
    //Fetch data form Inquire    
    intent = this.getIntent();
    bunde = intent.getExtras();
    
    if (bunde != null)
    {
      modify = bunde.getInt("modify");
    }
 
    if (modify == 1)
    {
      //gps_data = (TextView) findViewById(R.id.show);
      //gps_data.setText("修改資料");
    }
    else
    {
      geoLatitude = bunde.getDouble("geoLatitude");
      geoLongitude = bunde.getDouble("geoLongitude");
    }
    
    findViews();
    setListensers();      
  }
  
  
  private void setListensers()
  {
    // TODO Auto-generated method stub
    mSubmit.setOnClickListener(add_place);
    mCancel.setOnClickListener(cancel_place);
  }


  private void findViews() 
  {
    mSubmit = (Button) findViewById(R.id.new_submit);
    mCancel = (Button) findViewById(R.id.new_cancel);

    name = (EditText) findViewById(R.id.name);
    stime = (EditText) findViewById(R.id.stime);
    telephone = (EditText) findViewById(R.id.telephone);
    address = (EditText) findViewById(R.id.address);
    commit = (EditText) findViewById(R.id.commit);
    //gps_data = (TextView) findViewById(R.id.gps);
    
    if (modify == 1)
    {
      /*      
      type_id.setSelection(Integer.valueOf(LocationListview.lxs.gettypeid())-1);
      name.setText(LocationListview.lxs.getname());
      telephone.setText(LocationListview.lxs.gettelephone());
      website.setText(LocationListview.lxs.getwebsite());
      address.setText(LocationListview.lxs.getaddress());
      gps_data.setText(LocationListview.lxs.getlatitude() + "," + LocationListview.lxs.getlongitude());
      geoLatitude = LocationListview.lxs.getlatitude();
      geoLongitude = LocationListview.lxs.getlongitude();
      mDelete.setEnabled(true);
      */
    }
    else
    {
      //gps_data.setText(geoLatitude + "," + geoLongitude);
      //fetch address
    }
    
  }
  
  public boolean onCreateOptionsMenu(Menu menu)
  {

    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_DELETE, 0 ,R.string.new_delete);
    menu.add(0 , MENU_IMPORT, 0 ,R.string.new_import);
    menu.add(0 , MENU_EXPORT, 0 ,R.string.new_export);
    
    return true;  
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
  
    switch (item.getItemId())
    { 
          case MENU_DELETE:
             openOptionsDialog(1);
             break;
          case MENU_IMPORT:
            openOptionsDialog(2);
           break ;         
          case MENU_EXPORT:

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("設定");
            alert.setMessage("請輸入檔名");
            
            ScrollView sv = new ScrollView(this);
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            sv.addView(ll);
            tv = new TextView(this);
            tv.setText("檔名: (.txt)");
            et = new EditText(this);
            et.setText("db");
            ll.addView(tv);
            ll.addView(et);
            
            // Set an EditText view to get user input 
            alert.setView(sv);
            
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
              String fp = et.getText().toString();
              if (fp.equals("")) return;
              
              try {
                // 判斷 SD Card 有無插入
                if( Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) )
                {
                  openOptionsDialog("sdcard error");
                  return;
                }
                else
                {
                   // 取得 SD Card 位置
                   vSDCard = Environment.getExternalStorageDirectory();
                }
                
                // 判斷目錄是否存在
                File vPath = new File( vSDCard.getParent() + "/" +vSDCard.getName() + "/StoreMap" );
                if( !vPath.exists() )
                   vPath.mkdirs();
                
                FileWriter vFile = new FileWriter( vSDCard.getParent() + "/" + vSDCard.getName() 
                                                                       + "/StoreMap/" + fp + ".txt");
                
                Log.i(TAG, vSDCard.getParent() + "/" + vSDCard.getName() + "/StoreMap/db.txt");

                BufferedWriter bw = new BufferedWriter(vFile); 
                try{
                  cursor = db.query(SQLiteHelper.TB_NAME, null, null, null, null, null, null);

                  cursor.moveToFirst();
                  
                  //no data
                  if (cursor.isAfterLast())
                  {
                    openOptionsDialog("查無data, 請更新database");
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

                    vFile.write(sitem.name + "," + sitem.time + "," + sitem.phone 
                                                   + "," + sitem.addr + "," + sitem.commit + "\n");    
                    
                    Log.i(TAG, sitem.name + "," + sitem.time + "," + sitem.phone 
                        + "," + sitem.addr + "," + sitem.commit + "\n");
                    
                    cursor.moveToNext();
                  }   
                }catch(IllegalArgumentException e){
                  e.printStackTrace();
                  ++ DB_VERSION;
                  dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
                }

                vFile.close();
                openOptionsDialog("匯出成功");
             } catch (Exception e) {
                // 錯誤處理
               e.printStackTrace();
             }            
               
            }
            });
          
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                 
                }
              });
          
              alert.show();              
            break ;
      }
    
  return true ;
  }

  public String toUnicode(String str) {

    StringBuffer result = new StringBuffer();

    for (int i = 0; i < str.length(); i++) {

      char chr1 = (char) str.charAt(i);

      result.append("&#" + Integer.toString((int) chr1) + ";");

    }
    return result.toString();
  }
  
  private Button.OnClickListener add_place = new Button.OnClickListener() {
    public void onClick(View v) 
    {
      int error = 0;
      
      try 
      {
          String sname = (name.getText().toString());
          String ctime = (stime.getText().toString());
          String stelephone = (telephone.getText().toString());
          String saddress = (address.getText().toString());
          String scommit = (commit.getText().toString());

          if (sname.equals("") || ctime.equals("") ||stelephone.equals("") ||saddress.equals(""))
            {
              Toast.makeText(AddPlace.this,
              getString(R.string.new_name_null),
              Toast.LENGTH_SHORT).show();
            }
          else 
          {
            ContentValues contentValues = new ContentValues();
            contentValues.put(store_item.NAME, sname);
            contentValues.put(store_item.TIME, ctime);
            contentValues.put(store_item.PHONE, stelephone);
            contentValues.put(store_item.ADDR, saddress);
            contentValues.put(store_item.COMMIT, scommit);
            Log.i(TAG, sname + "," +  saddress);
            
            db.insert(SQLiteHelper.TB_NAME, null, contentValues);
          }
      } 
      
      catch (Exception err) 
      {
            Toast.makeText(AddPlace.this, getString(R.string.new_fail),
            Toast.LENGTH_SHORT).show();
            
            error = 1;
      }
      
      if (error == 0)
      {
        if (modify == 0)
          openOptionsDialog("新增成功");
        else
          openOptionsDialog("Modify Location OK");
      }
    }
  };

  private Button.OnClickListener cancel_place = new Button.OnClickListener() {
    public void onClick(View v) 
    {
      
      Intent intent = new Intent();
      intent.setClass( AddPlace.this, MyGoogleMap.class);
      startActivity(intent);
      finish();
    }
  };
  
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
      Intent intent = new Intent();
      intent.setClass(AddPlace.this, MyGoogleMap.class);
      startActivity(intent); 
      finish();
      return true;
    }
  
    return super.onKeyDown(keyCode, event);  
  }

  private void openOptionsDialog(int index) 
  {
    cindex = index;
    
    new AlertDialog.Builder(this)
      .setTitle("message")
      .setMessage("是否刪除有table的data?")
      .setNegativeButton("No",
          new DialogInterface.OnClickListener() {
          
            public void onClick(DialogInterface dialoginterface, int i) 
            {
            }
      }
      )
   
      .setPositiveButton("Yes",
          new DialogInterface.OnClickListener() {
          @SuppressWarnings("unused")
          public void onClick(DialogInterface dialoginterface, int i) 
          {
            try
            {
              db.delete(SQLiteHelper.TB_NAME, null, null);
              
            }
            catch (Exception e)
            {
                  e.printStackTrace();
            }
            
            if (cindex == 2)
            {
              AlertDialog.Builder alert = new AlertDialog.Builder(ap);

              alert.setTitle("設定");
              alert.setMessage("請輸入檔名");
              
              ScrollView sv = new ScrollView(ap);
              LinearLayout ll = new LinearLayout(ap);
              ll.setOrientation(LinearLayout.VERTICAL);
              sv.addView(ll);
              tv = new TextView(ap);
              tv.setText("檔名: (.txt)");
              et = new EditText(ap);
              et.setText("db");
              ll.addView(tv);
              ll.addView(et);
              
              // Set an EditText view to get user input 
              alert.setView(sv);
              
              alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) 
              {
                String fp = et.getText().toString();
                if (fp.equals("")) return;
                
                // 判斷 SD Card 有無插入
                if( Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) )
                {
                  openOptionsDialog("sdcard error");
                  return;
                }
                else
                {
                   // 取得 SD Card 位置
                   vSDCard = Environment.getExternalStorageDirectory();
                }
                
                try
                {
                  // 判斷目錄是否存在
                  File vPath = new File( vSDCard.getParent() + vSDCard.getName() + "/StoreMap" );
                  if( !vPath.exists() )
                     vPath.mkdirs();
                  
                  FileReader rFile = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() 
                                                                         + "/StoreMap/" + fp + ".txt" );
                  
                  if (rFile == null)
                  {
                    openOptionsDialog("no import data");
                    return;
                  }
                  
                  BufferedReader br = new BufferedReader(rFile);
                  String line = null;

                  while((line = br.readLine()) !=null)
                  {
                    
                    StringTokenizer stoken = new StringTokenizer( line, "," );
                    Log.i(TAG, line);
                    String sname = "";
                    String ctime = "";
                    String stelephone = "";
                    String saddress = "";
                    String scommit = "";

                    int count=0;
                    while( stoken.hasMoreTokens() )
                    {
                      switch (count)
                      {
                        case 0:
                          sname = stoken.nextToken();
                          break;
                        case 1:
                          ctime = stoken.nextToken();
                          break;
                        case 2:
                          stelephone = stoken.nextToken();
                          break;
                        case 3:
                          saddress = stoken.nextToken();
                          break;
                        case 4:
                          scommit = stoken.nextToken();
                          break;
                      }
                      count++;
                    }                  
                    
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(store_item.NAME, sname);
                    contentValues.put(store_item.TIME, ctime);
                    contentValues.put(store_item.PHONE, stelephone);
                    contentValues.put(store_item.ADDR, saddress);
                    contentValues.put(store_item.COMMIT, scommit);
                    Log.i(TAG, sname + "," +  saddress);
                    
                    db.insert(SQLiteHelper.TB_NAME, null, contentValues);
                  }
                  
                  rFile.close();
                  openOptionsDialog("匯入成功");
               } catch (Exception e) {
                    // 錯誤處理
                    e.printStackTrace();
               }            
                
              }
              });
            
              alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) 
                  {
                   
                  }
                });
            
                alert.show();              
              
              
              
            }
            else
              openOptionsDialog("刪除所有資料表");
          }
      }
      )
      
      .show();
  }  
  
  
  //show message
  private void openOptionsDialog(String info)
{
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
           //if (modify == 1) finish();
         }
         }
        )
    .show();
}

  
}
  
  
