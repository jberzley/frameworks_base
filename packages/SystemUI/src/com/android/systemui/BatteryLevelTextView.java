/*
Copyright (c) 2016, The Linux Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.android.systemui;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.ArraySet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;

public class BatteryLevelTextView extends TextView implements
        BatteryController.BatteryStateChangeCallback, TunerService.Tunable{

    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = 
            Settings.Secure.STATUS_BAR_SHOW_BATTERY_PERCENT;

    private static final String STATUS_BAR_BATTERY_STYLE =
            Settings.Secure.STATUS_BAR_BATTERY_STYLE;

    private BatteryController mBatteryController;
    private boolean mShow;
    private boolean mBatteryCharging = false;
    private boolean mBatteryEnabled;
    private boolean mBatteryPct;
    private final String mSlotBattery;


    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            loadShowBatteryTextSetting();
            setBatteryVisibility();
        }
    };

    private boolean mRequestedVisibility;

    public BatteryLevelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSlotBattery = context.getString(
                com.android.internal.R.string.status_bar_battery);
        mBatteryPct = context.getResources().getBoolean(
                R.bool.config_showBatteryPercentage);
        loadShowBatteryTextSetting();
        setBatteryVisibility();
    }

    private void loadShowBatteryTextSetting() {
        mShow = 0 != Settings.System.getInt(getContext().getContentResolver(),
            STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
    }

    private void setBatteryVisibility() {
        setVisibility( mBatteryEnabled
            && (mBatteryCharging || (mBatteryPct && mShow)) ? View.VISIBLE : View.GONE);
    }

    public void setBatteryCharging(boolean isCharging){
        mBatteryCharging = isCharging;
        setBatteryVisibility();
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        setText(getResources().getString(R.string.battery_level_template, level));
    }

    public void setBatteryController(BatteryController batteryController) {
        if(batteryController != null){
            mBatteryController = batteryController;
            mBatteryController.addStateChangedCallback(this);
            TunerService.get(getContext()).addTunable(this,
                    STATUS_BAR_SHOW_BATTERY_PERCENT, STATUS_BAR_BATTERY_STYLE);
        }
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor(
                STATUS_BAR_SHOW_BATTERY_PERCENT), false, mObserver);
        TunerService.get(getContext()).addTunable(this, StatusBarIconController.ICON_BLACKLIST);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
        }
        TunerService.get(getContext()).removeTunable(this);
    }

    @Override
    public void onTuningChanged(String key, String newValue) {

        switch (key) {
            case STATUS_BAR_SHOW_BATTERY_PERCENT:
                mRequestedVisibility = newValue != null && Integer.parseInt(newValue) == 2;
                setVisibility(mRequestedVisibility ? View.VISIBLE : View.GONE);
                break;
            case STATUS_BAR_BATTERY_STYLE:
                final int value = newValue == null ?
                        BatteryMeterDrawable.BATTERY_STYLE_PORTRAIT : Integer.parseInt(newValue);
                switch (value) {
                    case BatteryMeterDrawable.BATTERY_STYLE_TEXT:
                        setVisibility(View.VISIBLE);
                        break;
                    case BatteryMeterDrawable.BATTERY_STYLE_HIDDEN:
                        setVisibility(View.GONE);
                        break;
                    default:
                        setVisibility(mRequestedVisibility ? View.VISIBLE : View.GONE);
                        break;
                }
                break;
            default:
                break;
        }
    }
}
