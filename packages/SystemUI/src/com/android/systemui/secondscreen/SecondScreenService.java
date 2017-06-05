package com.android.systemui.secondscreen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.secondscreen.SecondScreenApp;
import android.secondscreen.SecondScreenManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import com.android.systemui.R;

public class SecondScreenService extends Service {

    private ArrayList<SecondScreenApp> ssas = new ArrayList<>();
    private LinearLayout fakeRootView;
    private SecondScreenView rootView;
    private ViewsPagerAdapter viewsPagerAdapter;
    private ViewPager viewPager;
    private WindowManager wm;
    private SecondScreenManager ssm;
    private LayoutInflater inflater;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        ssm = (SecondScreenManager) this.getSystemService(Context.SECOND_SCREEN_SERVICE);

        fakeRootView = (LinearLayout) inflater.inflate(R.layout.second_screen_root, null);

        rootView = (SecondScreenView) fakeRootView.findViewById(R.id.secondscreenview);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 400;
        params.y = 0;
        params.width = 1040;
        params.height = 160;
        wm.addView(fakeRootView, params);

        viewPager = (ViewPager) fakeRootView.findViewById(R.id.second_screen_viewpager);
        viewsPagerAdapter = new ViewsPagerAdapter(this);
        viewPager.setAdapter(viewsPagerAdapter);

        pollWidgets();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mcswainsoftware.secondscreen.APP_ADDED");
        this.registerReceiver(new SecondScreenBroadcastReceiver(), filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fakeRootView != null) {
            wm.removeView(fakeRootView);
            fakeRootView = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int rotation = wm.getDefaultDisplay().getRotation();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        switch(rotation) {
            // Rotated <-
            case Surface.ROTATION_90:
                params.x = 0;
                params.y = 0;
                params.width = 160;
                params.height = 1040;
                break;
            // Rotated ->
            case Surface.ROTATION_270:
                params.x = 2400;
                params.y = 400;
                params.width = 160;
                params.height = 1040;
                break;
            // Rotated Upside Down
            case Surface.ROTATION_180:
                params.x = 2400;
                params.y = 0;
                params.width = 1040;
                params.height = 160;
                break;
            // Neutral
            case Surface.ROTATION_0:
                params.x = 400;
                params.y = 0;
                params.width = 1040;
                params.height = 160;
                break;
            default:
                break;
        }
        wm.updateViewLayout(fakeRootView, params);
    }

    private void pollWidgets() {
        ssas = ssm.getSecondScreenApps();
        if(this.viewPager != null && this.viewsPagerAdapter != null) {
            viewsPagerAdapter = new ViewsPagerAdapter(this);
            viewPager.setAdapter(viewsPagerAdapter);
        }
    }

    public class SecondScreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SecondScreenService.this.pollWidgets();
        }
    }

    class ViewsPagerAdapter extends PagerAdapter {
        private Context mContext;

        public ViewsPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View view = (View) SecondScreenService.this.ssas.get(position).getView();
            collection.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return SecondScreenService.this.ssas.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
