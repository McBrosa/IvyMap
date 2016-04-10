package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by Nathan on 4/9/2016.
 */
public class NavbarActivity extends Activity {
    @Override
    public  void onCreate (Bundle savedInstanceState)  {
        super.onCreate (savedInstanceState) ;
        setContentView (R.layout.activity_main) ;

        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnAll);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnPicture);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnVideo);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnFile);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Toast.makeText(NavbarActivity.this, buttonView.getText(), Toast.LENGTH_SHORT).show();
            }
        }
    };
}
