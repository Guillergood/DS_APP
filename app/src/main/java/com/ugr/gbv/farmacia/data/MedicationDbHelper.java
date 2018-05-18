package com.ugr.gbv.farmacia.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MedicationDbHelper  extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "medication_gbv_DS.db";
    private static final int DATABASE_VERSION = 1;
    public MedicationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_ARTICLES_TABLE = "CREATE TABLE " +
                MedicationContract.MedicationEntry.TABLE_NAME + " (" +
                MedicationContract.MedicationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MedicationContract.MedicationEntry.COLUMN_MED_NAME + " TEXT NOT NULL, " +
                MedicationContract.MedicationEntry.COLUMN_MED_TEXT + " TEXT NOT NULL " +
                ");";

        db.execSQL(SQL_CREATE_ARTICLES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Elimina la base de datos si se actualiza
        db.execSQL("DROP TABLE IF EXISTS " + MedicationContract.MedicationEntry.TABLE_NAME);
        //Vuelve a crear la base de datos
        onCreate(db);

    }

}
