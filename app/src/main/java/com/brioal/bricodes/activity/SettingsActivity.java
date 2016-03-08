package com.brioal.bricodes.activity;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.brioal.bricodes.R;
import com.brioal.bricodes.view.SwipeBackLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private ListPreference listPreference;
    @Bind(R.id.activity_setting_swipelayout)
    SwipeBackLayout swipeBackLayout ;
    @Bind(R.id.setting_rea)
    RelativeLayout relativeLayout ;
    @Bind(android.R.id.list)
    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        addPreferencesFromResource(R.xml.pref_general);
        initPre();
        swipeBackLayout.setCallback(new SwipeBackLayout.Callback(){
            @Override
            public void onShouldFinish() {
                finish();
                overridePendingTransition(R.anim.no_anim, R.anim.out_tp_right);
            }
        });
    }

    private void initPre() {
        listPreference = (ListPreference) findPreference("style_list");
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    preference.setSummary(listPreference.getValue()
                    );
                }
                if (preference instanceof SwitchPreference) {
                    SwitchPreference switchPreference = (SwitchPreference) preference;
                    preference.setSummary(switchPreference.isChecked()+"");
                }
                return true;
            }
        });
    }


}
