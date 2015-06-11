/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitmastro.playMusicAlarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

/**
 * AlarmClock application.
 */
public class AlarmClock extends Activity implements OnItemClickListener {

    static final String PREFERENCES = "AlarmClock";

    /**
     * This must be false for production. If true, turns on logging, test code,
     * etc.
     */
    static final boolean DEBUG = false;

    // private SharedPreferences mPrefs;
    private LayoutInflater mFactory;

    private ListView mAlarmsList;

    private Cursor mCursor;

    private void updateAlarm(boolean enabled, Alarm alarm) {
        Alarms.enableAlarm(this, alarm.id, enabled);
        if (enabled) {
            SetAlarm.popAlarmSetToast(this, alarm.hour, alarm.minutes,
                    alarm.daysOfWeek);
        }
    }

    private class AlarmTimeAdapter extends CursorAdapter {

        public AlarmTimeAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View ret = mFactory.inflate(R.layout.alarm_time, parent, false);

            DigitalClock digitalClock = (DigitalClock) ret
                    .findViewById(R.id.digitalClock);
            digitalClock.setLive(false);
            return ret;
        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            final Alarm alarm = new Alarm(cursor);

            View indicator = view.findViewById(R.id.indicator);

            // Set the initial state of the clock "checkbox"
            final CheckBox clockOnOff = (CheckBox) indicator
                    .findViewById(R.id.clock_onoff);
            clockOnOff.setChecked(alarm.enabled);

            // Clicking outside the "checkbox" should also change the state.
            indicator.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    clockOnOff.toggle();
                    updateAlarm(clockOnOff.isChecked(), alarm);
                }
            });

            final View delete_indicator = view.findViewById(R.id.clock_delete);

            // Clicking outside the "checkbox" should also change the state.
            delete_indicator.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle(getString(R.string.delete_alarm))
                            .setMessage(
                                    getString(R.string.delete_alarm_confirm))
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d,
                                                int w) {
                                            Alarms.deleteAlarm(AlarmClock.this,
                                                    alarm.id);
                                        }
                                    })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
            });

            DigitalClock digitalClock = (DigitalClock) view
                    .findViewById(R.id.digitalClock);

            // set the alarm value
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, alarm.hour);
            c.set(Calendar.MINUTE, alarm.minutes);
            digitalClock.updateTime(c);

            // Set the repeat value or leave it blank if it does not repeat.
            TextView daysOfWeekView = (TextView) digitalClock
                    .findViewById(R.id.daysOfWeek);
            final String daysOfWeekStr = alarm.daysOfWeek.toString(
                    AlarmClock.this, false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
                daysOfWeekView.setText(daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }

        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mFactory = LayoutInflater.from(this);
        // mPrefs = getSharedPreferences(PREFERENCES, 0);
        mCursor = Alarms.getAlarmsCursor(getContentResolver());
        updateLayout();
    }

    private void updateLayout() {
        setContentView(R.layout.alarm_clock);
        mAlarmsList = (ListView) findViewById(R.id.alarms_list);
        AlarmTimeAdapter adapter = new AlarmTimeAdapter(this, mCursor);
        mAlarmsList.setAdapter(adapter);
        mAlarmsList.setVerticalScrollBarEnabled(true);
        mAlarmsList.setOnItemClickListener(this);
    }

    private void addNewAlarm() {
        startActivity(new Intent(this, SetAlarm.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastMaster.cancelToast();
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_item_about:
//                startActivity(new Intent(this, SettingsLicenseActivity.class));
                return true;
            case R.id.menu_item_add_alarm:
                addNewAlarm();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.alarm_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(pos);
        final Alarm alarm = new Alarm(c);
        Intent intent = new Intent(this, SetAlarm.class);
        intent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        startActivity(intent);
    }


}
