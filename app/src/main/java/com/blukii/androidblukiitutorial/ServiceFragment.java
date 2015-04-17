package com.blukii.androidblukiitutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.ServiceMagnetometerCalibration;
import com.blukii.android.blukiilibrary.ServiceProfile;
import com.blukii.android.blukiilibrary.BlukiiConstants;

/**
 * Created by bolatov on 26.01.2015.
 */
public class ServiceFragment extends AbstractFragment implements View.OnClickListener {

    private final static String TAG = ServiceFragment.class.getSimpleName();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ServiceProfile profile = getServiceProfile();

            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, -1);

            // die Verbindung zum Blukii wurde getrennt
            switch (action) {

                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;
                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateConnectionStatus(getText(R.string.blukii_connected).toString());
                    ViewHelper.setEnabledOfAllControls(true, getView());
                    break;
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                // MAGNETOMETER CALIBRATION
                case ServiceProfile.ACTION_SET_NOTIFY_SERVICE_MAGNETOMETER_CALIBRATION:
                    toast("service magnetometer calibration set", status);
                case ServiceProfile.ACTION_READ_SERVICE_MAGNETOMETER_CALIBRATION:
                    ServiceMagnetometerCalibration smc = (ServiceMagnetometerCalibration) intent.getSerializableExtra(ServiceProfile.EXTRA_SERVICE_MAGNETOMETER_CALIBRATION);
                    getTextView(R.id.tv_service_magnetometer_calibration).setText(String.valueOf(smc));
                    break;

            }

            if (intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE) != null) {
                String toast = intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE);
                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public ServiceFragment() {
        // Required empty public constructor
    }

    public static ServiceFragment newInstance() {
        ServiceFragment fragment = new ServiceFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ViewHelper.registerListener(this, getView());
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        ServiceProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {
        ServiceProfile profile = getServiceProfile();
        if (profile == null) return;

        switch (v.getId()) {

            // MAGNETOMETER CALIBRATION
            case R.id.btn_service_read_magnetometer_calibration:
                profile.readMagnetometerCalibration();
            break;

            case R.id.btn_service_notify_magnetometer_calibration:
                profile.notifyMagnetometerCalibration(((ToggleButton) v).isChecked());
                break;
        }
    }

    private ServiceProfile getServiceProfile() {
        return (ServiceProfile) MainActivity.getProfileById(getActivity(), ServiceProfile.ID);
    }
}
