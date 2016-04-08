package com.season.drag;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.season.drag.core.DragAdapter;
import com.season.drag2.R;

public class TestAdapter extends DragAdapter<PackageInfo> implements OnClickListener {
 
	public TestAdapter(Context mContext, List<PackageInfo> list) {
		super(mContext, list); 
	} 


	private int mWidthHeight;
	/**
	 * 获取宽高
	 * @return
	 */
	public int getWidthHeight(){
		if(mWidthHeight <=0 ){
			mWidthHeight = LayoutParams.WRAP_CONTENT;
		} 
		return mWidthHeight;
	} 
	
	@Override
	public View getView(int position) {
		
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.griditem_main, null);

		@SuppressWarnings("deprecation")
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.FILL_PARENT , getWidthHeight());
		convertView.setLayoutParams(params); 
		
		convertView.findViewById(R.id.g_one).setBackgroundColor(mDraging?0x66000000: 0xff000000);
		convertView.findViewById(R.id.imageView_del).setVisibility(mDraging?View.VISIBLE:View.GONE);
		
		convertView.findViewById(R.id.imageView_del).setTag(position);
		convertView.findViewById(R.id.imageView_del).setOnClickListener(this);
		
		PackageInfo info = getItem(position);
		
		ImageView	iconView = (ImageView) convertView.findViewById(R.id.imageView_ItemImage);
		iconView.setImageDrawable(context.getPackageManager().getApplicationIcon(info.applicationInfo));
		TextView txtAge = (TextView) convertView.findViewById(R.id.txt_userAge);
		txtAge.setText(context.getPackageManager().getApplicationLabel(info.applicationInfo).toString());

		return convertView;
	}

	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.imageView_del){
			int position = (Integer) v.getTag();
			deleteItemInPage(position);
		}
	}

}













