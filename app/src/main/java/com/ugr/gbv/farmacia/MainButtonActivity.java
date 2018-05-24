package com.ugr.gbv.farmacia;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ugr.gbv.farmacia.data.MedicationContract;
import com.ugr.gbv.farmacia.data.MedicationDbHelper;
import com.ugr.gbv.farmacia.data.MedicationPreferences;
import com.ugr.gbv.farmacia.sync.MedicationsSyncTask;

public class MainButtonActivity extends RuntimePermission {

    private static final int REQUEST_PERMISSION = 151;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_button);

        ImageView meds_button = findViewById(R.id.meds_button);
        ImageView map_button = findViewById(R.id.map_button);
        ImageView share_button = findViewById(R.id.share_logo);



        requestAppPermissions(new String[]{Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE},
                R.string.message_permission,REQUEST_PERMISSION);

        meds_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),MapsActivity.class);
                startActivity(intent);
            }
        });

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = createShareMedIntent();
                startActivity(intent);
            }
        });




    }

    private Intent createShareMedIntent() {
        String comparte = "Únete a la aplicación " + getString(R.string.app_name) +" en tu dispositivo favorito\n";
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(comparte + " " + getString(R.string.hastag_code))
                .getIntent();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        else{
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return shareIntent;
    }



    @Override
    public void onPermissionsGranted(int requestCode) {

        Resources res = this.getResources();
        final XmlResourceParser xrp = res.getXml(R.xml.valores_offline);
        final Context context = getBaseContext();

        if(MedicationPreferences.getFirstTimeLaunch(context)) {
            AsyncTask<Void, Void, Void> mFetchMedsTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    MedicationsSyncTask.syncMedications(context, xrp);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                }
            };
            mFetchMedsTask.execute();
        }


        MedicationPreferences.saveFirstTimeLaunch(context,false);
    }
}
