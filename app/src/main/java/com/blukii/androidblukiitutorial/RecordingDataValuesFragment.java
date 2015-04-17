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
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.RecordedDataset;
import com.blukii.android.blukiilibrary.RecordedValue;
import com.blukii.android.blukiilibrary.RecordingProfile;
import com.blukii.android.blukiilibrary.RecordingReadAction;


public class RecordingDataValuesFragment extends AbstractFragment implements View.OnClickListener {

    private final static String TAG = "REC";

    private long lastError = 0l;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);

            RecordingProfile profile = (RecordingProfile) MainActivity.getProfileById(getActivity(), RecordingProfile.ID);
            ToggleButton btnEnabled = getToggleButton(R.id.btn_recording_enabled_set);

            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateBlukiiStatus(R.string.blukii_disconnected);
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateBlukiiStatus(R.string.blukii_connected);
                    ViewHelper.setEnabledOfAllControls(true, getView());
                    break;
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateBlukiiStatus(R.string.blukii_disconnected);
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_DATA:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        RecordedDataset dataset = (RecordedDataset) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_DATASET);
                        ListView listView = (ListView)getView().findViewById(android.R.id.list);
                        CustomAdapter adapter = (CustomAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
                        if (dataset != null) {
                            adapter.addSectionHeaderItem("#" + dataset.getIndex());
                            for (Object v : dataset) {
                                adapter.addItem(v.toString());
                            }
                        }
                    }
                    break;
            }

            if (intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE) != null) {
                if (System.currentTimeMillis() - lastError > 1000) {
                    toast(intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE));
                    lastError = System.currentTimeMillis();
                }
            }
        }
    };

    public static RecordingDataValuesFragment newInstance() {
        RecordingDataValuesFragment fragment = new RecordingDataValuesFragment();
        return fragment;
    }

    public RecordingDataValuesFragment() {
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


        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(new CustomAdapter(getActivity()));

        View header = inflater.inflate(R.layout.header_recording_data_values, null);

        listView.addHeaderView(header);

        listView.findViewById(R.id.btn_recording_data_read_all).setOnClickListener(this);
        listView.findViewById(R.id.btn_recording_data_read_offset).setOnClickListener(this);
        listView.findViewById(R.id.btn_recording_data_read_one).setOnClickListener(this);
        listView.findViewById(R.id.btn_recording_data_clear).setOnClickListener(this);

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
            case R.id.btn_recording_data_read_all:
                recProfile.readData(RecordingReadAction.AllDatasets, 0);
                break;
            case R.id.btn_recording_data_read_offset:
                int offsetFrom = Integer.parseInt(getEditText(R.id.et_recording_data_read_offset).getText().toString());
                recProfile.readData(RecordingReadAction.AllDatasetsFrom, offsetFrom);
                break;
            case R.id.btn_recording_data_read_one:
                int offset = Integer.parseInt(getEditText(R.id.et_recording_data_read_offset).getText().toString());
                recProfile.readData(RecordingReadAction.SingleDataset, offset);
                break;
            case R.id.btn_recording_data_clear:
                ListView listView = (ListView) getView().findViewById(android.R.id.list);
                CustomAdapter adapter = (CustomAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
                adapter.clear();
                break;
        }
    }


}
