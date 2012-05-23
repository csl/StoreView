package com.mapplace; 

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class PictureInfo 
{
	public static final int ReadOnly = 1;
	public static final int ReadWrite = 2;
	public static final int InvalidDevice =0;
	
	public static final int UseGridView = 0x01;
	public static final int UseGalleryView = 0x02;
	
	
	private Context context;
	private SharedPreferences sharedpreferences;
	private String DefaultPath;

	public int ExtenalStorageState;
	
	public PictureInfo(Context mcontext)
	{
		context = mcontext;
	    sharedpreferences = context.getSharedPreferences("review_information", Context.MODE_PRIVATE);	     
	    ExtenalStorageState();
	   
	}
	private void ExtenalStorageState()
	{		
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED))		
			ExtenalStorageState =  ReadWrite;
		else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))		
			ExtenalStorageState = ReadOnly;		
		else
			ExtenalStorageState = InvalidDevice;
		
		 DefaultPath = Environment.getExternalStorageDirectory().getPath();
		 if (DefaultPath == null) 
		    	DefaultPath ="/";
	}
	public int GetExtenalStorageState()
	{
		return ExtenalStorageState;
	}
	public int GetViewMode()
	{
		 return sharedpreferences.getInt("ViewMode",UseGridView);    
	}
	public boolean SetViewMode(int SetViewMode)
	{
		Editor editor = context.getSharedPreferences("review_information",Context.MODE_PRIVATE).edit();
        editor.putInt("ViewMode",SetViewMode);  
        return editor.commit();
	}
	public boolean SetImagePath(String ImagePath)
	{
		Editor editor = context.getSharedPreferences("review_information",Context.MODE_PRIVATE).edit();
        editor.putString("ImagePath",ImagePath);  
        return editor.commit();
	}
	public String GetImagePath()
	{   
		return sharedpreferences.getString("ImagePath", DefaultPath);        
	}
}
