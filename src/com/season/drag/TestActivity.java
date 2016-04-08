package com.season.drag;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.season.drag.core.DragAdapter;
import com.season.drag.core.DragController;
import com.season.drag.core.DragGridView;
import com.season.drag.core.DragScrollView;
import com.season.drag2.R;

public class TestActivity extends Activity implements OnItemClickListener{ 
	private int NUM_COLUMNS = 3;
	private int NUM_LINES = 6;
	private DragScrollView mContainer;  
	private TextView mPageView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main); 
		DragController.getInstance().disableDelFunction();
		mPageView = (TextView) findViewById(R.id.page);
		mContainer = (DragScrollView) findViewById(R.id.views);  
		mContainer.setPageListener(new DragScrollView.PageListener() {
			@Override
			public void page(int page) {
				mPageView.setText(""+page);
			}
		});
		fillGrid();
	} 
	
	private void fillGrid(){  
		List<PackageInfo> appInfos = getAllApps(getApplicationContext()); 
		mContainer.setAdapter(appInfos, new DragScrollView.ICallback<PackageInfo>() {
			
			@Override
			public int getColumnNumber() {
				
				return NUM_COLUMNS;
			}
			
			@Override
			public DragAdapter<PackageInfo> getAdapter(List<PackageInfo> data) {
				
				return (DragAdapter<PackageInfo>) new TestAdapter(TestActivity.this, data);
			}

			@Override
			public DragGridView<PackageInfo> getItemView() {
				
				@SuppressWarnings("unchecked")
				DragGridView<PackageInfo> view = (DragGridView<PackageInfo>) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_main, null);
				view.setOnItemClickListener(TestActivity.this);
				return view;
			}

			@Override
			public int getLineNumber() {
				
				return NUM_LINES;
			}
		});
	} 
	
	/**
	 * 查询手机内非系统应用
	 * @param context
	 * @return
	 */
	public static List<PackageInfo> getAllApps(Context context) {
		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = context.getPackageManager();
		//获取手机内所有应用
		List<PackageInfo> paklist = pManager.getInstalledPackages(0);
		for (int i = 0; i < paklist.size(); i++) {
			PackageInfo pak = (PackageInfo) paklist.get(i);
			//判断是否为非系统预装的应用程序
			if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				// customs applications
				apps.add(pak);
			}
		}
		return apps;
	}
	@Override
	public void onBackPressed() {
		
		if(DragController.getInstance().cancelDragMode()){
			super.onBackPressed();
		} 
	} 

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		DragController.getInstance().clear();
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		 
		Toast.makeText(TestActivity.this, ""+arg2, Toast.LENGTH_SHORT).show();
		try { 
			Uri uri = Uri.parse("weixin://qr/NnUxKR-E_kKFrVtK9yAk");
			Intent intent = new Intent(Intent.ACTION_VIEW,uri);
			startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	 

}



