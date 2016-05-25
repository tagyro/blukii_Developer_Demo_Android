package com.blukii.androidblukiitutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.RecordingParams;
import com.blukii.android.blukiilibrary.RecordingProfile;
import com.blukii.android.blukiilibrary.RecordingReadAction;


public class RecordingDataParamsFragment extends AbstractFragment implements View.OnClickListener {

    private final static String TAG = "REC";

    public static final String PREF_BLUKII_PARAMS_RECORDED_VALUES  = "pref_key_params_recorded_values";
    public static final String PREF_BLUKII_PARAMS_INTERVAL         = "pref_key_params_interval";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);

            RecordingProfile profile = (RecordingProfile) MainActivity.getProfileById(getActivity(), RecordingProfile.ID);
            ToggleButton btnEnabled = getToggleButton(R.id.btn_recording_enabled_set);

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
                case RecordingProfile.ACTION_READ_RECORDING_DATA_STATE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int rec = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_VALUE, 0);
                        int max = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_MAX, 0);

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        sp.edit().putInt(PREF_BLUKII_PARAMS_RECORDED_VALUES, rec).commit();

                        getTextView(R.id.tv_recording_data_state_recorded).setText(String.valueOf(rec));
                        getTextView(R.id.tv_recording_data_state_maximum).setText(String.valueOf(max));
                    }
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_DATA:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        RecordingParams params = (RecordingParams) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_PARAMS);
                        ListView listView = (ListView) getView().findViewById(android.R.id.list);
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) ((HeaderViewListAdapter) listView.getAdapter()).getWrappedAdapter();
                        adapter.clear();
                        if (params != null) {
                            Log.d(TAG, params.toString());
                            adapter.addAll(params.toStringArray());

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            sp.edit().putInt(PREF_BLUKII_PARAMS_INTERVAL, params.recordingInterval).commit();

                        }
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }

            if (intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE) != null) {
                toast(intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE));
            }
        }
    };

    public static RecordingDataParamsFragment newInstance() {
        RecordingDataParamsFragment fragment = new RecordingDataParamsFragment();
        return fragment;
    }

    public RecordingDataParamsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recording_data, container, false);


        ListView listView = (ListView)view.findViewById(android.R.id.list);
        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1));

        View header = inflater.inflate(R.layout.header_recording_data_params, null);

        listView.addHeaderView(header);

        listView.findViewById(R.id.btn_recording_data_state_read).setOnClickListener(this);
        listView.findViewById(R.id.btn_recording_data_read_params).setOnClickListener(this);

        ViewHelper.setEnabledOfAllControls(false, listView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        RecordingProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {

        RecordingProfile recProfile = (RecordingProfile) MainActivity.getProfileById(getActivity(), RecordingProfile.ID);
        if (recProfile == null) return;

        switch (v.getId()) {
            case R.id.btn_recording_data_state_read:
                recProfile.readDataState();
                break;
            case R.id.btn_recording_data_read_params:
                recProfile.readData(RecordingReadAction.ParameterAndInformation, 0);
                break;
        }
    }
}
