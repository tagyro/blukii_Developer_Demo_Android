package com.blukii.androidblukiitutorial;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blukii.android.blukiilibrary.BatteryProfile;
import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.DeviceInfoProfile;
import com.blukii.android.blukiilibrary.Profile;

import java.util.Date;

public class InformationFragment extends Fragment {

    private final static String TAG = InformationFragment.class.getSimpleName();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);

            switch (action) {
                //connection was disconnected
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateStatus(getText(R.string.blukii_disconnected).toString());
                    break;
                //blukii is ready, there is a connection and all services loaded
                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateStatus(getText(R.string.blukii_connected).toString());
                    break;
                //there was an error while loading the services
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateStatus(getText(R.string.blukii_disconnected).toString() + ". " + getText(R.string.lbl_error_LoadServices).toString());
                    break;

                //BATTERY
                case BatteryProfile.ACTION_READ_BATTERY_LEVEL:
                    //if status is good, read the level and update the value
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int level = intent.getIntExtra(BatteryProfile.EXTRA_BATTERY_VALUE, 0);
                        updateBatteryStatus(level + "%");
                    } else {
                        updateBatteryStatus(getText(R.string.lbl_status_readError).toString());
                    }
                    break;

                // FIRMWARE REVISION
                /*case (DeviceInfoProfile.ACTION_READ_DEVICE_INFO_FIRMWARE_REVISION:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        DeviceInfoFirmwareReleaseRevision releaseRevision = (DeviceInfoFirmwareReleaseRevision) intent.getSerializableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_RELEASE_REVISION);
                        DeviceInfoFirmwareBlukiiRevision blukiiRevision = (DeviceInfoFirmwareBlukiiRevision) intent.getSerializableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_BLUKII_REVISION);
                        String feature = intent.getStringExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_FEATURE_REVISION);
                        String bug = intent.getStringExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_BUG_REVISION);
                        updateFirmwareStatus(blukiiRevision.getDescription());
                    } else {
                        updateFirmwareStatus(getText(R.string.lbl_status_readError).toString());
                    }
                    break;*/
            }
        }
    };

    public InformationFragment() {
        // Required empty public constructor
    }

    public static InformationFragment newInstance() {
        InformationFragment fragment = new InformationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateVersion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_information, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //add profiles to intentFilter
        IntentFilter intentFilter = new IntentFilter();
        Profile.addDefaultActions(intentFilter);
        BatteryProfile.addActions(intentFilter);
        DeviceInfoProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        //if this fragment is visible to user
        if (isVisibleToUser) {

            //get profile
            BatteryProfile batteryProfile = (BatteryProfile) MainActivity.getProfileById(getActivity(), BatteryProfile.ID);

            //if there's no problem, read and update the battery level
            if (batteryProfile != null) {
                batteryProfile.readBatteryLevel();
                updateBatteryStatus(getText(R.string.lbl_status_read).toString());
            }

            /*DeviceInfoProfile deviceProfile = (DeviceInfoProfile) MainActivity.getProfileById(getActivity(), DeviceInfoProfile.ID);
            if (deviceProfile != null) {
                updateFirmwareStatus(getText(R.string.lbl_status_read).toString());

                Log.d(TAG, "in device profile read if");

                deviceProfile.readFirmwareRevision();

            }*/
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    //PRIVATE METHODS

    //update status and values

    private void updateStatus(String newStatus) {
       /* View v = (getActivity()).findViewById(R.id.statusbar);
        ((TextView) v).setText(newStatus);*/
    }

    private void updateBatteryStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_battery)).setText(getText(R.string.lbl_stateOfCharge).toString() + newStatus);
    }

    /*private void updateFirmwareStatus(String blukii) {
        ((TextView) getView().findViewById(R.id.tv_connected_device)).setText(getText(R.string.lbl_connected_device).toString() + blukii);
    }*/

    private void updateVersion() {
        //get version from gradle
        String version = BuildConfig.VERSION_NAME;
        //get versionCode from gradle
        int buildVersion = BuildConfig.VERSION_CODE;

        String versionName = version.split("-")[0];
        String versionBuild = version.split("-")[1];


        //get label text
        String lblVersion = getText(R.string.lbl_version).toString();
        //get label text
        String lblCode = getText(R.string.lbl_build).toString();


        //write both in credentials
        ((TextView) getView().findViewById(R.id.lbl_version)).setText(lblVersion + versionName);

        //write both in credentials
        ((TextView) getView().findViewById(R.id.lbl_build)).setText(lblCode + versionBuild);
    }
}
