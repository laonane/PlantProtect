package org.huaiangg.mt.plantprotect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * @description: mainactivity
 * @author: HuaiAngg
 * @create: 2019-03-20 15:31
 */
public class MainActivity extends AppCompatActivity {
	private boolean setOnclick = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏代码
		setMainBtnAffairs();// 配置按钮功能
		// if(setOnclick){
		// Log.e("加载", "加载");
		// loadActivity(Main.this, Settings.class);
		// }
	}

	/**
	 * 载入其他Activity的通用方法
	 * 
	 * @param context
	 *            当前上下文
	 * @param c
	 *            要跳转到哪个Activity类
	 */
	private void loadActivity(Context context, Class<?> c) {
		Intent intent = new Intent();
		intent.setClass(context, c);
		startActivity(intent);
		overridePendingTransition(R.anim.bottom_in, R.anim.bottom_out); // 随便加的切换效果
	}

	/**
	 * 配置按钮功能
	 */
	private void setMainBtnAffairs() {
		// 主界面按钮
		final Button btnConn = (Button) this.findViewById(R.id.btnConn);// 进入控制小车界面的按钮
		final Button btnSet = (Button) this.findViewById(R.id.btnSet);// 进入配置
		btnConn.setOnClickListener(new OnClickListener() {
			@SuppressLint("WrongConstant")
			@Override
			public void onClick(View arg0) {
				if(isConnectedWifi()){
					loadActivity(MainActivity.this, Control.class);// 载入控制小车界面
				}else{
					Toast.makeText(MainActivity.this, "没有连接wifi", 0).show();
				}
				
			}
		});
		btnSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadActivity(MainActivity.this, Settings.class);// 载入配置界面,注意配置界面没有写好.
				setOnclick = false;
			}
		});

	}

	private boolean isConnectedWifi() {
		ConnectivityManager connectivityManager = (ConnectivityManager) MainActivity.this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo.isConnected()) {
			return true;
		}
		return false;
	}

}
