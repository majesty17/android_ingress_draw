package com.majesty.ingress.drawhelper;

import java.util.Timer;
import java.util.TimerTask;

import com.majesty.ingress.drawhelper.R;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FxService extends Service {

    // ���帡�����ڲ���
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    // ���������������ò��ֲ����Ķ���
    WindowManager mWindowManager;
    DisplayMetrics metric = new DisplayMetrics();

    // ��Ҫ�ؼ�
    Button btn_drawboard, btn_reset, btn_hide, btn_show;
    LinearLayout ll_drawarea, ll_leftbutton, ll_thumbs;
    ImageView iv_drawboard;

    // ������
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
        Timer timer=new Timer();
        timer.schedule(task, 1000, 3000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        // ��ȡWindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metric);
        // ����window type
        wmParams.type = LayoutParams.TYPE_PHONE;
        // ����ͼƬ��ʽ��Ч��Ϊ����͸��
        wmParams.format = PixelFormat.RGBA_8888;
        // �����ڿ��Ի�ý��㣨û������ FLAG_NOT_FOCUSALBE
        // ѡ�ʱ����Ȼ�����ڷ�Χ֮��ĵ��豸�¼�����ꡢ�����������͸�����Ĵ��ڴ���
        // ����������ռ���еĵ��豸�¼��������������ǲ��Ƿ����ڴ��ڷ�Χ��
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;// FLAG_NOT_TOUCH_MODAL;

        // ������������ʾ��ͣ��λ��Ϊ����ö�
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;

        // �����������ڳ�������
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        // ��ȡ����������ͼ���ڲ���
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        // ���mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);

        // �������ڰ�ť,ll
        btn_drawboard = (Button) mFloatLayout.findViewById(R.id.btn_drawboard);
        btn_reset = (Button) mFloatLayout.findViewById(R.id.btn_reset);
        btn_hide = (Button) mFloatLayout.findViewById(R.id.btn_hide);
        btn_show = (Button) mFloatLayout.findViewById(R.id.btn_show);
        btn_show.setVisibility(View.GONE);
        ll_drawarea = (LinearLayout) mFloatLayout.findViewById(R.id.ll_drawarea);
        ll_leftbutton = (LinearLayout) mFloatLayout.findViewById(R.id.ll_leftbutton);
        ll_thumbs = (LinearLayout) mFloatLayout.findViewById(R.id.ll_thumbs);
        iv_drawboard = (ImageView) mFloatLayout.findViewById(R.id.iv_drawboard);

        // mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
        // View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0,
        // View.MeasureSpec.UNSPECIFIED));

        // �����С
        ViewGroup.LayoutParams lp = ll_drawarea.getLayoutParams();
        lp.height = metric.widthPixels;
        ll_drawarea.setLayoutParams(lp);

        // ���������ػ���
        btn_drawboard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_drawarea.getVisibility() == View.VISIBLE) {
                    ll_drawarea.setVisibility(View.GONE);
                    btn_drawboard.setTextColor(Color.WHITE);
                } else {
                    ll_drawarea.setVisibility(View.VISIBLE);
                    btn_drawboard.setTextColor(0xFFFF0000);

                }

            }
        });

        // ����
        btn_reset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (ll_thumbs != null) {
                    Toast.makeText(mFloatLayout.getContext(), "����", Toast.LENGTH_SHORT).show();
                    ll_thumbs.removeAllViews();
                }
            }
        });

        // ����
        btn_hide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ll_drawarea.setVisibility(View.GONE);
                ll_thumbs.setVisibility(View.GONE);
                ll_leftbutton.setVisibility(View.GONE);
                btn_show.setVisibility(View.VISIBLE);
            }
        });
        // ��ʾ
        btn_show.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ll_drawarea.setVisibility(View.VISIBLE);
                btn_drawboard.setTextColor(0x00FF0000);
                ll_thumbs.setVisibility(View.VISIBLE);
                ll_leftbutton.setVisibility(View.VISIBLE);
                btn_show.setVisibility(View.GONE);
            }
        });

        // ����
        // ����һ�ſհ�ͼƬ
        baseBitmap = Bitmap.createBitmap(metric.widthPixels, metric.widthPixels, Bitmap.Config.ARGB_4444);
        // ����һ�Ż���
        canvas = new Canvas(baseBitmap);

        // ��������
        paint = new Paint();
        paint.setColor(0x99FFff00);
        paint.setStrokeWidth(25);
        // ��ʼ��canvas
        resetCanvas(canvas, paint);

        iv_drawboard.setImageBitmap(baseBitmap);

        iv_drawboard.setOnTouchListener(new OnTouchListener() {
            int startX;
            int startY;

            @SuppressLint("NewApi")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("fingure:", "" + event.getAction());
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // ��ȡ�ְ���ʱ������
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // ��ȡ���ƶ��������
                    int stopX = (int) event.getX();
                    int stopY = (int) event.getY();
                    // �ڿ�ʼ�ͽ�������仭һ����
                    canvas.drawLine(startX, startY, stopX, stopY, paint);
                    // ʵʱ���¿�ʼ����
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    iv_drawboard.setImageBitmap(baseBitmap);
                    break;
                case MotionEvent.ACTION_UP:

                    // ����
                    ImageView imageView = new ImageView(mFloatLayout.getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
                    lp.height = 170;
                    lp.width = 170;
                    lp.setMarginEnd(5);
                    imageView.setLayoutParams(lp);
                    Bitmap bitmap_dump = Bitmap.createBitmap(baseBitmap);
                    imageView.setImageBitmap(bitmap_dump);
                    ll_thumbs.addView(imageView);
                    imageView.setScaleType(ScaleType.CENTER_CROP);

                    // ���
                    resetCanvas(canvas, paint);

                    iv_drawboard.setImageBitmap(baseBitmap);
                    break;
                }

                return true;
            }
        });

    } //end of create view

    void resetCanvas(Canvas canvas, Paint paint) {
        Paint paint_clear = new Paint();
        paint_clear.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        canvas.drawPaint(paint_clear);

        int h = canvas.getHeight();
        int w = canvas.getWidth();

        canvas.drawLine(0, 0, w, 0, paint);
        canvas.drawLine(0, h, w, h, paint);
        canvas.drawLine(0, 0, 0, h, paint);
        canvas.drawLine(w, 0, w, h, paint);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                Toast.makeText(FxService.this, "haha", Toast.LENGTH_SHORT).show();
                break;
            }
            super.handleMessage(msg);
        }
    };
    TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };
}
