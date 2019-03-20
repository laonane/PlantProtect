package org.huaiangg.mt.plantprotect;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

/**
 * @description: 网络摄像头
 * @author: HuaiAngg
 * @create: 2019-03-20 15:31
 */
public class Settings extends AppCompatActivity {
	private EditText mIPEditText,mPortEditText,mSpeedEditText;
	public static String mIP="192.168.8.1",mPORT="2001",mSPEED="255";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set);
		// =========================================
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏代码
		initView();
	}

	private void initView() {
		mIPEditText=(EditText) findViewById(R.id.editCtrIP);
		mPortEditText=(EditText) findViewById(R.id.editPort);
		mSpeedEditText=(EditText) findViewById(R.id.editSpeed);
		mIP=mIPEditText.getText().toString();
		mPORT=mPortEditText.getText().toString();
		mSPEED=mSpeedEditText.getText().toString();
	}

}
