package com.ugr.gbv.farmacia.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ugr.gbv.farmacia.data.MedicationContract;

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
}
