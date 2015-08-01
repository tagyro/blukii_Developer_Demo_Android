package com.blukii.androidblukiitutorial;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ViewHelper {

    public static void setEnabledOfAllControls(boolean enable, View v) {
        if (v instanceof ListView) {
            View h = v.findViewById(R.id.list_header);
            if (h != null) {
                setEnabledOfAllControls(enable, h);
            }
        }
        else if (v instanceof Spinner) {
            v.setEnabled(enable);
        } else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                setEnabledOfAllControls(enable, vg.getChildAt(i));
            }
        } else if (v instanceof Button || v instanceof ImageButton || v instanceof CheckBox || v instanceof EditText) {
            v.setEnabled(enable);
        }
    }

    public static void clearAllControls(boolean enable, View v) {
        setEnabledOfAllControls(false, v);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for ( int i = 0; i < vg.getChildCount(); i++) {
                clearAllControls(enable, vg.getChildAt(i));
            }
        } else if (v instanceof EditText) {
            ((EditText) v).setText("");
        } else if (v instanceof ToggleButton) {
            ((ToggleButton) v).setChecked(enable);
        } else if (v instanceof Spinner) {
            ((Spinner) v).setSelection(0);
        }
    }


    public static void registerListener(View.OnClickListener l, View v) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                registerListener(l, vg.getChildAt(i));
            }
        } else if (v instanceof Button) {
            Button b = (Button) v;
            b.setOnClickListener(l);
        } else if (v instanceof ImageButton) {
            ImageButton b = (ImageButton) v;
            b.setOnClickListener(l);
        }
    }
}
