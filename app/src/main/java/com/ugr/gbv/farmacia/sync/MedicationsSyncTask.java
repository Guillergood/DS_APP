package com.ugr.gbv.farmacia.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Pair;

import com.ugr.gbv.farmacia.data.MedicationContract;
import com.ugr.gbv.farmacia.utilities.DataBaseUtils;

public class MedicationsSyncTask {
    synchronized public static void syncMedications(Context context, XmlResourceParser xrp){

        try{


            DataBaseUtils.retrieveMedicationContent(context, xrp);





        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
