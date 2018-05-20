package com.ugr.gbv.farmacia.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.ugr.gbv.farmacia.data.MedicationContract;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;

public class DataBaseUtils {
    // Metodo que permite retirar todos los articulos de la base de datos


    public static Cursor getAllArticles (SQLiteDatabase mDb){

        // Devuelve todos los articulos de la base de datos ordenados por el id

        return mDb.query(MedicationContract.MedicationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MedicationContract.MedicationEntry._ID
        );


    }


    public static Cursor getAllArticlesMatch(String text, SQLiteDatabase mDb){

        Cursor cursor=null;
        if(!text.isEmpty()) {
            try {
                cursor = mDb.rawQuery("SELECT * FROM " + MedicationContract.MedicationEntry.TABLE_NAME +
                                " WHERE " + MedicationContract.MedicationEntry.COLUMN_MED_TEXT
                                + " LIKE  '%" + text + "%'"
                        , null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return cursor;

    }


    public static Cursor getSelectedItems(ArrayList<Integer> articlesId, SQLiteDatabase mDb) {
        Cursor cursor = null;
        StringBuilder a = new StringBuilder();
        a.append("(");
        for (int i : articlesId) {
            a.append("'");
            a.append(i);
            a.append("', ");
        }
        a.replace(a.length()-2, a.length(),")");
        String query = a.toString();
        try {
            cursor = mDb.rawQuery("SELECT * FROM " + MedicationContract.MedicationEntry.TABLE_NAME +
                            " WHERE " + MedicationContract.MedicationEntry._ID
                            + " IN " + query
                            + " ORDER BY " + MedicationContract.MedicationEntry._ID
                    , null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public static void retrieveMedicationContent(Context context,XmlResourceParser xrp) throws XmlPullParserException, IOException {
/*
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
/*
        URLConnection urlCon = XmlUrlSource.openConnection();
        urlCon.setDoOutput(true);
        urlCon.setRequestProperty("Content-type", "text/plain");
*/

        /* We will parse the XML content looking for the "<title>" tag which appears inside the "<item>" tag.
         * However, we should take in consideration that the rss feed name also is enclosed in a "<title>" tag.
         * As we know, every feed begins with these lines: "<channel><title>Feed_Name</title>...."
         * so we should skip the "<title>" tag which is a child of "<channel>" tag,
         * and take in consideration only "<title>" tag which is a child of "<item>"
         *
         * In order to achieve this, we will make use of a boolean variable.
         */


        // xpp.setInput(, "UTF_8");


        // Returns the type of current event: START_TAG, END_TAG, etc..
        int eventType = xrp.getEventType();


        ArrayList<String> meds = new ArrayList<String>();


        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xrp.getName().equalsIgnoreCase("name")) {
                    String clase = xrp.getAttributeValue(null, "name");
                    meds.add(xrp.nextText());
                } else if (xrp.getName().equalsIgnoreCase("price")) {
                    String clase = xrp.getAttributeValue(null, "price");
                    meds.add(xrp.nextText());
                } else if (xrp.getName().equalsIgnoreCase("description")) {
                    String clase = xrp.getAttributeValue(null, "description");
                    meds.add(xrp.nextText());
                }
            }

            eventType = xrp.next(); //Siguiente elemento
        }
        ContentValues[] medContentValues = new ContentValues[meds.size()/3];

        int k = 0;


        for (int i = 0; i < meds.size(); i += 3, k++) {
            ContentValues medValue = new ContentValues();
            medValue.put(MedicationContract.MedicationEntry.COLUMN_MED_NAME, meds.get(i));
            medValue.put(MedicationContract.MedicationEntry.COLUMN_MED_PRICE, meds.get(i + 1));
            medValue.put(MedicationContract.MedicationEntry.COLUMN_MED_TEXT, meds.get(i + 2));
            medContentValues[k] = medValue;
        }


        ContentResolver medicationContentResolver = context.getContentResolver();


        medicationContentResolver.bulkInsert(
                MedicationContract.MedicationEntry.CONTENT_URI,
                medContentValues
        );



    }
}
