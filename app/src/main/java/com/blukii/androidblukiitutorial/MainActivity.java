package com.blukii.androidblukiitutorial;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.BlukiiManagerService;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.Profile;
import com.blukii.android.blukiilibrary.TemperatureProfile;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PAGES_COUNT = 13;
    private int lastSelectedPage = -99;

    //Variables for Fragments
    MenuFragment menuFragment;
    AccelerometerFragment accelerometerFragment;
    AltimeterFragment altimeterFragment;
    BasicSensorsFragment basicSensorsFragment;
    DeviceInfoFragment deviceInfoFragment;
    DirectometerFragment directometerFragment;
    InformationFragment informationFragment;
    LightFragment lightFragment;
    RecordingConfigFragment recordingConfigFragment;
    RecordingDataParamsFragment recordingDataParamsFragment;
    RecordingDataValuesFragment recordingDataValuesFragment;
    SelectBlukiiFragment selectBlukiiFragment;
    TemperatureFragment temperatureFragment;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private BluetoothAdapter mBluetoothAdapter;
    private BlukiiManagerService mService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BlukiiManagerService.LocalBinder binder = (BlukiiManagerService.LocalBinder) service;
            MainActivity.this.mService = binder.getService();

            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }

            Log.d(TAG, "Connected to BlukiiManagerService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected from BlukiiManagerService");
            mService = null;
        }
    };

    public static Profile getProfileById(Context context, final String id) {
        if (!(context instanceof MainActivity)) {
            return null;
        }

        BlukiiManagerService service = ((MainActivity) context).getBlukiiManagerService();
        if (service == null) {
            return null;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String blukiiAddress = sp.getString(SelectBlukiiFragment.PREF_SELECTED_BLUKII, "");
        Blukii blukii = service.getBlukiiByAddress(blukiiAddress);

        if (blukii == null || !blukii.isConnected()) {
            return null;
        }

        Profile profile = blukii.getProfileById(id);
        return profile != null ? profile : null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(PAGES_COUNT);



        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                if ( lastSelectedPage >= 0 ) {
                    switch (lastSelectedPage) {
                        case 0:
                            //Menu
                            break;
                        case 1:
                            //select blukii
                            break;
                        case 2:
                            //directometer
                            break;
                        case 3:
                            //basic sensors
                            break;
                        case 4:
                            //accelerometer
                            //if (accelerometerFragment != null) {
                            //    accelerometerFragment.disableProfile();
                            //}
                            break;
                        case 5:
                            //altimeter
                            break;
                        case 6:
                            //light
                            break;
                        case 7:
                            //device info
                            break;
                        case 8:
                            //temperature
                            break;
                        case 9:
                            //recording config
                            break;
                        case 10:
                            //recording data params
                            break;
                        case 11:
                            //recording data values
                            break;
                        case 12:
                            //information
                            break;
                    }
                }

                Log.d("debug", ""+lastSelectedPage);

                lastSelectedPage = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float arg1, int arg2) {}
        });


        // Initializes a Bluetooth adapter.  For API level 18 and above, valueOf a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Error. Bluetooth LE not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind to BlukiiManagerService
        Intent gattServiceIntent = new Intent(this, BlukiiManagerService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // der Benutzer wollte Bluetooth nicht anschalten, also beenden wir die App
                if (resultCode == Activity.RESULT_CANCELED) {
                    finish();
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
        mService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public BlukiiManagerService getBlukiiManagerService() {
        return mService;
    }

    public void selectMenuPage(View v) {
        mViewPager.setCurrentItem(0);
    }

    public void selectBlukiiPage(View v) {
        mViewPager.setCurrentItem(1);
    }

    public void selectDirectometerPage(View v) {
        mViewPager.setCurrentItem(2);
    }

    public void selectBasicSensorsPage(View v) {
        mViewPager.setCurrentItem(3);
    }

    public void selectAccelerometerPage(View v) {
        mViewPager.setCurrentItem(4);
    }

    public void selectAltimeterPage(View v) {
        mViewPager.setCurrentItem(5);
    }

    public void selectLightPage(View v) {
        mViewPager.setCurrentItem(6);
    }

    public void selectDeviceInfoPage(View v) {
        mViewPager.setCurrentItem(7);
    }

    public void selectTemperaturePage(View v) {
        mViewPager.setCurrentItem(8);
    }

    public void selectRecordingPage(View v) {
        mViewPager.setCurrentItem(9);
    }

    public void selectRecordingPageParams(View v) {
        mViewPager.setCurrentItem(10);
    }

    public void selectRecordingPageValues(View v) {
        mViewPager.setCurrentItem(11);
    }

    public void selectInfoPage(View v) {
        mViewPager.setCurrentItem(12);
    }

    //handler for all enabler stati
    public static void handleEnablerStatus(Context context, EnablerStatus status, String profile) {
        switch (status) {
                case Deactivated:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_deactivated).toString());
                    break;
                case Activated:
                    MainActivity.toast(context,  profile + ": " + context.getText(R.string.enablerStatus_activated).toString());
                    break;
                case CommunicationErrorAccelerometer:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_CommunicationErrorAccelerometer).toString());
                    break;
                case CommunicationErrorAirpressureSensor:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_CommunicationErrorAirpressureSensor).toString());
                    break;
                case CommunicationErrorHumiditySensor:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_CommunicationErrorHumiditySensor).toString());
                    break;
                case CommunicationErrorLightsensor:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_CommunicationErrorLightsensor).toString());
                    break;
                case CommunicationErrorMagneticfieldSensor:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_CommunicationErrorMagneticField).toString());
                    break;
                case CommunicationErrorTemperatureSensor:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_CommunicationErrorTemperatureSensor).toString());
                    break;
                case ProfileAlreadyFree:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_ProfileAlreadyFree).toString());
                    break;
                case BlockedByProfileRecording:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_BlockedByProfileRecording).toString());
                    break;
                case BlockedByProfileAccelerometer:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_BlockedByProfileAccelerometer).toString());
                    break;
                case BlockedByProfileTemperature:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_BlockedByProfileTemperature).toString());
                    break;
                case BlockedByProfileDirectometer:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_BlockedByProfileDirectometer).toString());
                    break;
                case BlockedByProfileHumidity:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_BlockedByProfileHumidity).toString());
                    break;
                case BlockedByProfileLight:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_BlockedByProfileLight).toString());
                    break;
                case MagneticFieldSensorNeedsCalibration:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_MagnetiFieldSensorNeedsCalibration).toString());
                    break;
                case EnablerGeneralError:
                    MainActivity.toast(context, profile + ": " + context.getText(R.string.enablerStatus_GeneralError).toString());
                    break;
            default:
                //MainActivity.toast(context, "EnablerStatus: " + status);
                break;
        }
    }

    private static void toast(Context context, String toast) {
        Log.d(TAG, toast);
        Toast.makeText( context, toast, Toast.LENGTH_LONG ).show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    menuFragment = MenuFragment.newInstance();
                    return menuFragment;
                case 1:
                    selectBlukiiFragment = SelectBlukiiFragment.newInstance();
                    return selectBlukiiFragment;
                case 2:
                    directometerFragment = DirectometerFragment.newInstance();
                    return directometerFragment;
                case 3:
                    basicSensorsFragment = BasicSensorsFragment.newInstance();
                    return basicSensorsFragment;
                case 4:
                    accelerometerFragment = AccelerometerFragment.newInstance();
                    return accelerometerFragment;
                case 5:
                    altimeterFragment = AltimeterFragment.newInstance();
                    return altimeterFragment;
                case 6:
                    lightFragment = LightFragment.newInstance();
                    return lightFragment;
                case 7:
                    deviceInfoFragment = DeviceInfoFragment.newInstance();
                    return deviceInfoFragment;
                case 8:
                    temperatureFragment = TemperatureFragment.newInstance();
                    return temperatureFragment;
                case 9:
                    recordingConfigFragment = RecordingConfigFragment.newInstance();
                    return recordingConfigFragment;
                case 10:
                    recordingDataParamsFragment = RecordingDataParamsFragment.newInstance();
                    return recordingDataParamsFragment;
                case 11:
                    recordingDataValuesFragment = RecordingDataValuesFragment.newInstance();
                    return recordingDataValuesFragment;
                case 12:
                    informationFragment = InformationFragment.newInstance();
                    return informationFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGES_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] titles = getResources().getStringArray(R.array.page_titles);
            if (position < titles.length) {
                return titles[position];
            }
            return null;
        }
    }
}
