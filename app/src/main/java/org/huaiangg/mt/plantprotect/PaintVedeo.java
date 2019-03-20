package org.huaiangg.mt.plantprotect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @description: 网络摄像头
 * @author: HuaiAngg
 * @create: 2019-03-20 15:31
 */
@SuppressLint("NewApi")
public class PaintVedeo extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    //屏幕大小变量
    private static int screenWidth;//获得屏幕尺寸保存在这里
    private static int screenHeight;
    //连接与线程变量
    private boolean runFlag = false;//可以作为连接条件.暂时没有对此变量起实际作用
    private static SurfaceHolder holder;//赋值实际的surfaceView holder地址.只是便于调用
    private HttpURLConnection conn;//URL HTTP地址连接类,只能作为类变量.便于销毁view时关闭
    private Thread thread;//访问资源并绘图的线程变量,在surfaceView状态改变时 启用或关闭连接

    public PaintVedeo(Context context, AttributeSet attrs) {
        super(context, attrs);//例行公务,别管他
        screenValue();//屏幕尺寸赋值
        holder = this.getHolder();
        holder.addCallback(this);//启用自带的回调函数
    }

    // ========================================

    /**
     * 获得屏幕像素值
     */
    private void screenValue() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        runFlag = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
//		change事件会在切换横竖屏时出现.还有初始化出现一次
//		runFlag = true;
//		thread = new Thread(this);
//		thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        runFlag = false;
        conn.disconnect();
    }

    @Override
    public void run() {
        Canvas c;
        Bitmap bmp;
        InputStream is;
        URL videoURL = null;
//		Paint p = new Paint(); // 创建画笔,流图像可以不需要
        String imageURL = "http://192.168.8.1:8083/?action=snapshot";//视频地址,注意访问流媒体的action.
        try {
            videoURL = new URL(imageURL);
            ;
        } catch (Exception e) {
        }
        //绘图质量配置
        BitmapFactory.Options o = new BitmapFactory.Options();// 配置原图缩放值
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;// 高质量
        while (runFlag) {
            c = null;
            try {
                synchronized (holder) {
                    c = holder.lockCanvas();// 锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
                    // ===========================================================
                    //	连接后的请求,与流都只能用一次,需要重新连接并获得流
                    conn = (HttpURLConnection) videoURL.openConnection();//此方法会new HttpURLConnection并调用connect()
//					conn.connect();//getInputStream会自动调用此方法.此方法一般用在new HttpURLConnection之后.(new的时候没有发送连接请求)
                    is = conn.getInputStream(); //获取流
                    bmp = BitmapFactory.decodeStream(is, null, o);
                    bmp = Bitmap.createScaledBitmap(bmp, screenWidth,
                            screenHeight, true);// 把图片根据屏幕尺寸进行缩放
                    c.drawBitmap(bmp, 0, 0, null);

                    Thread.sleep(30);// 间隔时间,建议把这个参数放进配置里.肉眼约能分别42毫秒间隔的图像.
                }
            } catch (Exception e) {
//				System.out.println(e.getMessage());
            } finally {
                try {
                    if (null != holder) {
                        holder.unlockCanvasAndPost(c);// 解锁画图并提交
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

    }
}

