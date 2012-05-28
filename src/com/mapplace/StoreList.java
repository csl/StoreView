package com.mapplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.HashMap;

import com.sqlite.SQLiteHelper;
import com.sqlite.store_item;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class StoreList extends Activity {

	
	private ArrayList<HashMap<String, Object>> cstore_list;
  private ArrayList<store_item> list_id;

	private ListView show_view;

    private static int DB_VERSION = 1;
    
	private SQLiteDatabase db;
	private SQLiteHelper dbHelper;
	private Cursor cursor;
	
	String TAG = "StoreList";
		
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storelist);
        
        MyGoogleMap.my.myDialog.dismiss();
        
         list_id = new ArrayList<store_item>(); 
	    
        try{
            dbHelper = new SQLiteHelper(this, SQLiteHelper.DB_NAME, null, DB_VERSION);
            db = dbHelper.getWritableDatabase();
          }
          catch(IllegalArgumentException e){
            e.printStackTrace();
            ++ DB_VERSION;
            dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
          }        
                
        
        //Display: create ListView class
        show_view = (ListView)findViewById(R.id.listview);
        
        cstore_list = getStoreList(); 
    
        
        if (cstore_list != null)
        {
	        SimpleAdapter listitemAdapter=new SimpleAdapter(this,  
	        		cstore_list, 
	    										R.layout.no_listview_style,
	    										new String[]{"ItemTitle","ItemText"}, 
	    										new int[]{R.id.topTextView,R.id.bottomTextView}  
	    										);  
	    
	        show_view.setAdapter(listitemAdapter);
	        show_view.setOnItemClickListener(new OnItemClickListener() 
	        {          
	    	   @Override  
	    	   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
	    	     long arg3) 
	    	   {
	             store_item addr = list_id.get(arg2);

	             Bundle bundle = new Bundle();
	    	       bundle.putString("name", addr.name);
               bundle.putString("time", addr.time);
               bundle.putString("phone", addr.phone);
               bundle.putString("addr", addr.addr);
               bundle.putString("commit", addr.commit);
	    	          
    	    		Intent intent = new Intent();
    	    		intent.setClass(StoreList.this, MapLocationView.class);
    	    		intent.putExtras(bundle);
    	    		startActivity(intent);
    	    		//finish();
	    	   }  
	        });
        }
        else{
        	finish();
        }
	}
	
	public ArrayList<HashMap<String, Object>> getStoreList() 
	{
		int i=0;
		int search_list_size = MyGoogleMap.my.search_list.size();
		ArrayList<HashMap<String, Object>> listitem = new ArrayList<HashMap<String,Object>>();
 
		list_id.clear();
		
		
		
		 for (MapLocation item : MyGoogleMap.my.search_list)
	   {
	      Log.i("toDO", Double.toString(item.getDist()));
	   }
	   Collections.sort(MyGoogleMap.my.search_list, new Comparator<MapLocation>() {
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
		
		if (search_list_size > 10)
		{
      for (int j=search_list_size-1; j>=search_list_size-10; j--)
      {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Log.i("VALUE", MyGoogleMap.my.search_list.get(j).getStoreItem().id);
        map.put("ItemTitle", MyGoogleMap.my.search_list.get(j).getStoreItem().name);
        map.put("ItemText", "營業時間: " + MyGoogleMap.my.search_list.get(j).getStoreItem().time + ", 地址：" + MyGoogleMap.my.search_list.get(j).getStoreItem().addr);
        listitem.add(map);
        list_id.add(MyGoogleMap.my.search_list.get(j).getStoreItem());
      }
		}
		else
		{
      for (int j=search_list_size-1; j>=0; j--)
  		{
  			HashMap<String, Object> map = new HashMap<String, Object>();
  			Log.i("VALUE", MyGoogleMap.my.search_list.get(j).getStoreItem().id);
  			map.put("ItemTitle", MyGoogleMap.my.search_list.get(j).getStoreItem().name);
  			map.put("ItemText", "營業時間: " + MyGoogleMap.my.search_list.get(j).getStoreItem().time + ", 地址：" + MyGoogleMap.my.search_list.get(j).getStoreItem().addr);
  			listitem.add(map);			
        list_id.add(MyGoogleMap.my.search_list.get(j).getStoreItem());
  		}
		}
		  
		return listitem;
	}

	//error message
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
	        	finish();
	         }
	         }
	        )
	    .show();
	}	
}