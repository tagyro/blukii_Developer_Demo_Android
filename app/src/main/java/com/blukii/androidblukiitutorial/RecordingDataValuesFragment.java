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
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.RecordedDataset;
import com.blukii.android.blukiilibrary.RecordedValue;
import com.blukii.android.blukiilibrary.RecordingParams;
import com.blukii.android.blukiilibrary.RecordingProfile;
import com.blukii.android.blukiilibrary.RecordingReadAction;
import com.blukii.blukiichartlibrary.BlukiiChartEntry;
import com.blukii.blukiichartlibrary.BlukiiChartEntrySet;
import com.blukii.blukiichartlibrary.BlukiiChartHelper;
import com.blukii.blukiichartlibrary.BlukiiLineChart;
import com.blukii.blukiichartlibrary.BlukiiLineData;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;


public class RecordingDataValuesFragment extends AbstractFragment implements View.OnClickListener {

    private final static String TAG = "REC";

    private long lastError = 0l;


    private BlukiiLineChart chartAccelerometer = null;
    private BlukiiLineChart chartAltimeter = null;
    private BlukiiLineChart chartBattery = null;
    private BlukiiLineChart chartHumidity = null;
    private BlukiiLineChart chartLight = null;
    private BlukiiLineChart chartMagnetometer = null;
    private BlukiiLineChart chartStepDetection = null;
    private BlukiiLineChart chartTemperature = null;


    //Vectors for entries
    private Vector<BlukiiChartEntry> chartEntriesAcceX = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesAcceY = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesAcceZ = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesAlti = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesBatt = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesHumi = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesLigh = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesMagnX = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesMagnY = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesMagnZ = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesStep = new Vector<BlukiiChartEntry>();
    private Vector<BlukiiChartEntry> chartEntriesTemp = new Vector<BlukiiChartEntry>();


    // EntrySetList
    private Vector<BlukiiChartEntrySet> chartEntrySetAcce = new Vector<>();
    private Vector<BlukiiChartEntrySet> chartEntrySetAlti = new Vector<>();
    private Vector<BlukiiChartEntrySet> chartEntrySetBatt = new Vector<>();
    private Vector<BlukiiChartEntrySet> chartEntrySetHumi = new Vector<>();
    private Vector<BlukiiChartEntrySet> chartEntrySetLigh = new Vector<>();
    private Vector<BlukiiChartEntrySet> chartEntrySetMagn = new Vector<>();
    private Vector<BlukiiChartEntrySet> chartEntrySetStep = new Vector<>();
    private Vector<BlukiiChartEntrySet> chartEntrySetTemp = new Vector<>();

    //Label Array
    private final String UNIT_ACCEL_X = "Acceleration X [g]";
    private final String UNIT_ACCEL_Y = "Acceleration Y [g]";
    private final String UNIT_ACCEL_Z = "Acceleration Z [g]";
    private final String UNIT_ALTI = "Altimeter [m] [Pa]";
    private final String UNIT_BATT = "Battery [%]";
    private final String UNIT_HUMI = "Humidity [%]";
    private final String UNIT_LIGH = "Light [lx]";
    private final String UNIT_MAGN_X = "Magnetic Field X []";
    private final String UNIT_MAGN_Y = "Magnetic Field Y []";
    private final String UNIT_MAGN_Z = "Magnetic Field Z []";
    private final String UNIT_STEP = "Step Counts";
    private final String UNIT_TEMP = "Temperature [°C]";

    // Werte vom gerade bekommenen Dataset
    private Double[] accelerometer = new Double[3];
    private float altimeter = 0.0f;
    private float battery = 0.0f;
    private float humidity = 0.0f;
    private float light = 0.0f;
    private Double[] magnetometer = new Double[3];
    private int stepdetection = 0;
    private float temperature = 0.0f;

    private boolean acceFlag = false;
    private boolean altiFlag = false;
    private boolean battFlag = false;
    private boolean humiFlag = false;
    private boolean lighFlag = false;
    private boolean magnFlag = false;
    private boolean stepFlag = false;
    private boolean tempFlag = false;

    private String datasetIndex = "";

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
                case RecordingProfile.ACTION_READ_RECORDING_DATA:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
                        TextView progressTextView = (TextView) getView().findViewById(R.id.progressTextView);

                        int progress = 0;
                        int recordedDataSets        = sp.getInt(RecordingDataParamsFragment.PREF_BLUKII_PARAMS_RECORDED_VALUES, 0);
                        int prefDeviceInterval      = sp.getInt(RecordingDataParamsFragment.PREF_BLUKII_PARAMS_INTERVAL, 0);
                        long prefDeviceStarttime    = sp.getLong(RecordingConfigFragment.PREF_BLUKII_PARAMS_DEVICE_STARTTIME, 0);


                        RecordedDataset dataset = (RecordedDataset) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_DATASET);

                        ListView listView = (ListView)getView().findViewById(android.R.id.list);
                        CustomAdapter adapter = (CustomAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
                        progressTextView.setText("0/" + recordedDataSets);

                        if (dataset != null) {
                            adapter.addSectionHeaderItem("#" + dataset.getIndex());

                            Log.d(TAG, dataset.getIndex()+ " " + recordedDataSets);

                            float percentage =  ((Float.parseFloat(dataset.getIndex()+"")/Float.parseFloat(recordedDataSets+"")) * 100);
                            Log.d(TAG, "loadingstate: " + percentage);
                            progressBar.setProgress((int) percentage);
                            progressTextView.setText( dataset.getIndex() + "/" + recordedDataSets);

                            long timestamp = prefDeviceStarttime + (dataset.getIndex()-1) * prefDeviceInterval;
                            Log.d(TAG, "timestamp " + timestamp);
                            adapter.addItem("timestamp: " + timestamp);
                            adapter.addItem("datetime: " + new Date(timestamp).toString());

                            datasetIndex = new Date(timestamp).toString();

                            for (Object v : dataset) {
                                adapter.addItem(v.toString());
                                RecordedValue recValue = (RecordedValue) v;
                                switch (recValue.getCode()) {
                                    case Accelerometer:
                                        accelerometer = (Double[]) recValue.getData();
                                        acceFlag = true;
                                        break;
                                    case Altimeter:
                                        altimeter = Float.parseFloat(recValue.getData()+"");
                                        altiFlag= true;
                                        break;
                                    case Battery:
                                        battery = Float.parseFloat(recValue.getData()+"");
                                        battFlag = true;
                                        break;
                                    case Humidity:
                                        humidity = Float.parseFloat(recValue.getData()+"");
                                        humiFlag = true;
                                        break;
                                    case Light:
                                        light = Float.parseFloat(recValue.getData()+"");
                                        lighFlag = true;
                                        break;
                                    case Magnetometer:
                                        magnetometer = (Double[]) recValue.getData();
                                        magnFlag = true;
                                        break;
                                    case StepDetection:
                                        stepdetection = (int) recValue.getData();
                                        stepFlag = true;
                                    case Temperature:
                                        temperature = Float.parseFloat(recValue.getData()+"");
                                        tempFlag = true;
                                        break;
                                }
                            }

                            if (acceFlag) {

                                chartEntriesAcceX.add(new BlukiiChartEntry(datasetIndex, Float.parseFloat(accelerometer[0] + "")));
                                chartEntriesAcceY.add(new BlukiiChartEntry(datasetIndex, Float.parseFloat(accelerometer[1] + "")));
                                chartEntriesAcceZ.add(new BlukiiChartEntry(datasetIndex, Float.parseFloat(accelerometer[2] + "")));

                                //Log.d(TAG, "INDEX: " + dataset.getIndex() + "" + " RECORDED: " + recordedDataSets);
                                if ( dataset.getIndex() ==  recordedDataSets ) {
                                    chartEntrySetAcce.add(new BlukiiChartEntrySet(chartEntriesAcceX, UNIT_ACCEL_X, 0));
                                    chartEntrySetAcce.add(new BlukiiChartEntrySet(chartEntriesAcceY, UNIT_ACCEL_Y, 1));
                                    chartEntrySetAcce.add(new BlukiiChartEntrySet(chartEntriesAcceZ, UNIT_ACCEL_Z, 2));

                                    chartAccelerometer.setData(chartEntrySetAcce);
                                    chartAccelerometer.render();
                                    (getView().findViewById(R.id.chartAccelerometer)).setVisibility(View.VISIBLE);
                                }
                            }

                            if (altiFlag) {
                                chartEntriesAlti.add(new BlukiiChartEntry(datasetIndex, altimeter));

                                if (dataset.getIndex() == recordedDataSets ) {
                                    chartEntrySetAlti.add(new BlukiiChartEntrySet(chartEntriesAlti, UNIT_ALTI, 0));
                                    chartAltimeter.setData(chartEntrySetAlti);
                                    chartAltimeter.render();
                                    (getView().findViewById(R.id.chartAltimeter)).setVisibility(View.VISIBLE);
                                }
                            }

                            if (battFlag) {
                                chartEntriesBatt.add(new BlukiiChartEntry(datasetIndex, battery));

                                if (dataset.getIndex() == recordedDataSets ) {
                                    chartEntrySetBatt.add(new BlukiiChartEntrySet(chartEntriesBatt, UNIT_BATT, 0));
                                    chartBattery.setData(chartEntrySetBatt);
                                    chartBattery.render();

                                    (getView().findViewById(R.id.chartBattery)).setVisibility(View.VISIBLE);
                                }
                            }

                            if (humiFlag) {
                                chartEntriesHumi.add(new BlukiiChartEntry(datasetIndex, humidity));

                                if ( dataset.getIndex() == recordedDataSets ) {
                                    chartEntrySetHumi.add(new BlukiiChartEntrySet(chartEntriesHumi, UNIT_HUMI, 0));
                                    chartHumidity.setData(chartEntrySetHumi);
                                    chartHumidity.render();
                                    (getView().findViewById(R.id.chartHumidity)).setVisibility(View.VISIBLE);
                                }
                            }

                            if (lighFlag) {
                                chartEntriesLigh.add(new BlukiiChartEntry(datasetIndex + "", light));

                                if ( dataset.getIndex() == recordedDataSets ) {
                                    chartEntrySetLigh.add(new BlukiiChartEntrySet(chartEntriesLigh, UNIT_LIGH, 0));
                                    chartLight.setData(chartEntrySetLigh);
                                    chartLight.render();
                                    (getView().findViewById(R.id.chartLight)).setVisibility(View.VISIBLE);
                                }
                            }

                            if (magnFlag) {
                                chartEntriesMagnX.add(new BlukiiChartEntry(datasetIndex, Float.parseFloat(magnetometer[0] + "")));
                                chartEntriesMagnY.add(new BlukiiChartEntry(datasetIndex, Float.parseFloat(magnetometer[1] + "")));
                                chartEntriesMagnZ.add(new BlukiiChartEntry(datasetIndex, Float.parseFloat(magnetometer[2] + "")));

                                if ( dataset.getIndex() == recordedDataSets ) {
                                    chartEntrySetMagn.add(new BlukiiChartEntrySet(chartEntriesMagnX, UNIT_MAGN_X, 0));
                                    chartEntrySetMagn.add(new BlukiiChartEntrySet(chartEntriesMagnY, UNIT_MAGN_Y, 1));
                                    chartEntrySetMagn.add(new BlukiiChartEntrySet(chartEntriesMagnZ, UNIT_MAGN_Z, 2));
                                    chartMagnetometer.setData(chartEntrySetMagn);
                                    chartMagnetometer.render();
                                    (getView().findViewById(R.id.chartMagnetometer)).setVisibility(View.VISIBLE);
                                }
                            }

                            if (stepFlag) {
                                chartEntriesStep.add(new BlukiiChartEntry(datasetIndex + "", (float) stepdetection));

                                if ( dataset.getIndex() == recordedDataSets ) {
                                    chartEntrySetStep.add(new BlukiiChartEntrySet(chartEntriesStep, UNIT_STEP, 0));
                                    chartStepDetection.setData(chartEntrySetStep);
                                    chartStepDetection.render();
                                    (getView().findViewById(R.id.chartStepDetection)).setVisibility(View.VISIBLE);
                                }
                            }

                            if (tempFlag) {
                                chartEntriesTemp.add(new BlukiiChartEntry(datasetIndex + "", temperature));

                                if ( dataset.getIndex() == recordedDataSets ) {
                                    chartEntrySetTemp.add(new BlukiiChartEntrySet(chartEntriesTemp, UNIT_TEMP, 0));
                                    chartTemperature.setData(chartEntrySetTemp);
                                    chartTemperature.render();
                                    (getView().findViewById(R.id.chartTemperature)).setVisibility(View.VISIBLE);
                                }
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


        // Linecharts initialized by xml
        chartAccelerometer = (BlukiiLineChart) getView().findViewById(R.id.chartAccelerometer);
        chartAltimeter = (BlukiiLineChart) getView().findViewById(R.id.chartAltimeter);
        chartBattery = (BlukiiLineChart) getView().findViewById(R.id.chartBattery);
        chartHumidity = (BlukiiLineChart) getView().findViewById(R.id.chartHumidity);
        chartLight = (BlukiiLineChart) getView().findViewById(R.id.chartLight);
        chartMagnetometer = (BlukiiLineChart) getView().findViewById(R.id.chartMagnetometer);
        chartStepDetection = (BlukiiLineChart) getView().findViewById(R.id.chartStepDetection);
        chartTemperature = (BlukiiLineChart) getView().findViewById(R.id.chartTemperature);


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

                /* TODO:
                 - clear all Charts and reset view
                  */
                break;
        }
    }


}
