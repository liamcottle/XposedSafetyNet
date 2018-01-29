package com.liamcottle.xposed.safetynet.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import com.afollestad.materialdialogs.MaterialDialog;
import com.liamcottle.xposed.safetynet.R;
import com.liamcottle.xposed.safetynet.preferences.Preferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if api key is not set
        if(TextUtils.isEmpty(Preferences.API_KEY.withinSelf().stringValue())){
            showAPIKeyDialog();
        }

    }

    private void showAPIKeyDialog() {

        new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.title_api_key)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(null, Preferences.API_KEY.withinSelf().stringValue(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                        // get api key from dialog
                        String apiKey = input.toString();

                        // set api key
                        Preferences.API_KEY.withinSelf().set(apiKey);

                    }
                }).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            case R.id.action_api_key: {
                showAPIKeyDialog();
                return true;
            }

        }

        return false;

    }

}
