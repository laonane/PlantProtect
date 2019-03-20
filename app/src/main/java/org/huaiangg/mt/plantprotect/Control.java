package org.huaiangg.mt.plantprotect;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @description: control类
 * @author: HuaiAngg
 * @create: 2019-03-20 15:31
 */
public class Control extends AppCompatActivity {
	private String mIp, mSpeed, MD_SD;
	int mPort;
	private Socket socket = null;
	DataInputStream in = null;
	int status;
	String result;
	// 接受土壤温湿度
	static BufferedReader mBufferedReaderClient = null;
	char[] buffer = new char[1024];
	int count = 0;
	private String recvMessageClient = "";
	TextView mHumitureTextView;
	TextView mReset;

	// 指令发出 数据缓存
	private static PrintWriter printWriter = null;
	private static BufferedReader bufferedReader = null;// 把内容放进来后可以用来判断连接是否异常,不必要就给注释了

	private boolean isConnect = false;// 是否连接小车.用于销毁时处理事件的判断条件
	private Thread thread = null;// 控制小车线程

	// private RadioButton radioConn;// 连接控制按钮的变量,只是为了以后方便
	private Button btnForward;// 前
	private Button btnBack;// 后
	private Button btnLeft;// 左
	private Button btnRight;// 右
	private Button engineForward;// 舵机前
	private Button engineBack;// 舵机后
	private Button engineLeft;// 舵机左
	private Button engineRight;// 舵机右

	private Button mPenWu;// 喷雾开关
	private boolean isPenChecked = true;
	private Switch change;

	int i = 0;// i:0时为小车驱动；i:1时为舵机驱动

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {

			}
			if (msg.what == 0) {
				mHumitureTextView.setText(recvMessageClient);
			}
			if (msg.what == 3) {
				mHumitureTextView.setText(recvMessageClient);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ctr);
		// =========================================
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏代码
		mIp = Settings.mIP;
		mPort = Integer.valueOf(Settings.mPORT);
		mSpeed = Settings.mSPEED;
		MD_SD = "MD_SD " + mSpeed + " " + mSpeed + "\r";
		// 连接socket通信
		isConnect = true;
		// 开启控制线程(注意视频连接会在进入控制界面时直接开启,这里只响应控制小车的线程)
		thread = new Thread(runnable);
		thread.start();

		setBtnAffairs();
	}

	/**
	 * 配置按钮功能
	 */
	@SuppressLint("ClickableViewAccessibility")
	private void setBtnAffairs() {
		// 主界面按钮
		// radioConn = (RadioButton) this.findViewById(R.id.radioConn);//
		// 点击后单选按钮后才可以控制小车
		btnForward = (Button) this.findViewById(R.id.btnForward);
		btnBack = (Button) this.findViewById(R.id.btnBack);
		btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnRight = (Button) this.findViewById(R.id.btnRight);
		// 舵机按钮
		engineForward = (Button) this.findViewById(R.id.engineForward);
		engineBack = (Button) this.findViewById(R.id.engineBack);
		engineLeft = (Button) this.findViewById(R.id.engineLeft);
		engineRight = (Button) this.findViewById(R.id.engineRight);

		mPenWu = (Button) this.findViewById(R.id.penwu);

		mHumitureTextView = (TextView) findViewById(R.id.humiture);
		mReset = (TextView) findViewById(R.id.Reset);

		change = (Switch) findViewById(R.id.change);

		btnForward.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchHandle(event, "MD_Qian\r", "前");
				return true;
			}
		});
		btnBack.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchHandle(event, "MD_Hou\r", "后");
				return true;
			}
		});
		btnLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchHandle(event, "MD_Zuo\r", "左");
				return true;
			}
		});
		btnRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchHandle(event, "MD_You\r", "右");
				return true;
			}
		});
		engineForward.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				i = 1;
				touchHandle(event, "DJ_Shang\r", "舵机上");
				return true;
			}
		});
		engineBack.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				i = 1;
				touchHandle(event, "DJ_Xia\r", "舵机下");
				return true;
			}
		});
		engineLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				i = 1;
				touchHandle(event, "DJ_Zuo\r", "舵机左");
				return true;
			}
		});
		engineRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				i = 1;
				touchHandle(event, "DJ_You\r", "舵机右");
				return true;
			}
		});
		mReset.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				i = 1;
				touchHandle(event, "DJ_Zhong\r", "舵机复位");
				return true;
			}
		});

		change.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean ischecked) {
				if (ischecked) {// 手动
					ctrOrder("ON\r", "手动");
				} else {// 自动
					ctrOrder("OFF\r", "自动");
				}
			}
		});
		mPenWu.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				i = 1;
				touchHandle(event, "K\r", "作业开始");
				return true;
			}
		});

	}

	/**
	 * @param event
	 */
	private void touchHandle(MotionEvent event, String orderStr, String tips) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (i == 1) {

			} else {
				ctrOrder(MD_SD, tips);

			}
			Log.e("传递的命令", orderStr + "");
			ctrOrder(orderStr, tips);
			break;
		case MotionEvent.ACTION_UP:
			if (i == 1) {
				ctrOrder("G\r", "停");
			} else {
				ctrOrder("MD_Ting\r", "停");
			}

			i = 0;
			break;
		default:
			break;
		}
	}

	private void print(String msg) {
		Log.d(TAG, msg);
	}

	/**
	 * 给小车发送指令,
	 * 
	 * @param orderStr
	 * @param tips
	 *            提示
	 */
	@SuppressLint("WrongConstant")
	private void ctrOrder(final String orderStr, String tips) {

		if (socket.isConnected()) {

			new Thread() {
				@Override
				public void run() {

					Log.e("查看结果", orderStr + "！");
					printWriter.print(orderStr);
					printWriter.flush();

				}
			}.start();
		} else {
			Toast.makeText(Control.this, "连接失败", 0).show();
		}

	}

	public static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}

	private static final String TAG = "TEST";
	// ============================================
	// 线程mRunnable启动
	private Runnable runnable = new Runnable() {
		public void run() {
			try {
				Log.e("1", "1");
				// 连接服务器
				socket = new Socket(mIp, mPort); // 小车ip,端口
				socket.setKeepAlive(true);
				Log.e("bb:", socket.isConnected() + "");
				Log.e("aa:", socket.isClosed() + "");
				// 取得输入、输出流（土壤温湿度获取）
				mBufferedReaderClient = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));

				printWriter = new PrintWriter(socket.getOutputStream(), true);//
				// 根据新建的sock建立

				OutputStream os = socket.getOutputStream();
				printWriter = new PrintWriter(os, true);//
			} catch (Exception e) {
				print("连接异常");
			}
			while (true) {
				try {
					if ((count = mBufferedReaderClient.read(buffer)) > 0) {
						recvMessageClient = getInfoBuff(buffer, count);// 消息换行
						Thread.sleep(500);
						Message msg = new Message();
						msg.what = 3;
						mHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					recvMessageClient = "接收异常:" + e.getMessage() + "\n";// 消息换行
					Message msg = new Message();
					msg.what = 0;
					mHandler.sendMessage(msg);
				}
			}
		}
	};

	// 接收处理
	private String getInfoBuff(char[] buff, int count) {
		char[] temp = new char[count];
		for (int i = 0; i < count; i++) {
			temp[i] = buff[i];
		}
		return new String(temp);
	}


	@Override
	protected void onPause() {
		super.onPause();
		if (isConnect) {
			isConnect = false;
			try {
				if (socket != null) {
					socket.close();
					socket = null;
					printWriter.close();
					printWriter = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			thread.interrupt();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isConnect) {
			isConnect = false;
			try {
				if (socket != null) {
					socket.close();
					socket = null;
					printWriter.close();
					printWriter = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			thread.interrupt();
		}
	}
}
