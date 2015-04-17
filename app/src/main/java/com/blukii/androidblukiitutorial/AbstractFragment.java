package com.blukii.androidblukiitutorial;

import android.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.BlukiiConstants;

public abstract class AbstractFragment extends Fragment {

    protected CheckBox getCheckBox(int id) {
        return (CheckBox) getView().findViewById(id);
    }

    protected EditText getEditText(int id) {
        return (EditText) getView().findViewById(id);
    }

    protected TextView getTextView(int id) {
        return (TextView) getView().findViewById(id);
    }

    protected Spinner getSpinner(int id) {
        return (Spinner) getView().findViewById(id);
    }

    protected ToggleButton getToggleButton(int id) {
        return (ToggleButton) getView().findViewById(id);
    }

    protected void initSpinner(int spinnerId, int arrayId, AdapterView.OnItemSelectedListener listener) {
        Spinner spinner = (Spinner) getView().findViewById(spinnerId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                arrayId, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(listener);
    }

    protected void toast(String toast, int status) {
        if (status != BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
            toast = String.format("%s: %s (%d)", toast, "FAILED", status);
        }
        Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
    }

    protected void toast(String toast) {
        toast(toast, BlukiiConstants.BLUKII_DEVICE_STATUS_OK);
    }

    protected void updateBlukiiStatus(int status) {
        getTextView(R.id.tv_blukii_status).setText(status);
    }

    protected void updateConnectionStatus(String status) {
        View v = (getActivity()).findViewById(R.id.statusbar);
        ((TextView) v).setText(status);
    }
}

