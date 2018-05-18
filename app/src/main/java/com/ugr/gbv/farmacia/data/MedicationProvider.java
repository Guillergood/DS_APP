package com.ugr.gbv.farmacia.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MedicationProvider extends ContentProvider {


        public static final int CODE_ARTICLE = 100;
        public static final int CODE_ARTICLE_WITH_TEXT = 101;
        public static final int CODE_ARTICLE_WITH_NUMBER = 102;

        private static final UriMatcher sUriMatcher = buildUriMatcher();
        private MedicationDbHelper mOpenHelper;

        public static UriMatcher buildUriMatcher() {
            final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
            final String authority = MedicationContract.CONTENT_AUTHORITY;

            matcher.addURI(authority, MedicationContract.PATH_ARTICLE, CODE_ARTICLE);

            matcher.addURI(authority,MedicationContract.PATH_ARTICLE + "/*", CODE_ARTICLE_WITH_TEXT);


            return matcher;

        }


        @Override
        public boolean onCreate() {
            mOpenHelper = new MedicationDbHelper(getContext());
            return true;
        }

        @Override
        public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

            switch (sUriMatcher.match(uri)){
                case CODE_ARTICLE:
                    db.beginTransaction();
                    int rowsInserted = 0;
                    try {
                        for (ContentValues value : values){
                            String name =
                                    value.getAsString(MedicationContract.MedicationEntry.COLUMN_MED_NAME);

                            if (name.isEmpty()){
                                throw new IllegalArgumentException("Nombre Vacío");
                            }

                            long _id = db.insert(MedicationContract.MedicationEntry.TABLE_NAME, null, value);

                            if(_id != -1){
                                rowsInserted++;
                            }
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    if(rowsInserted > 0){
                        getContext().getContentResolver().notifyChange(uri, null);
                    }

                    return rowsInserted;

                default:
                    return super.bulkInsert(uri, values);
            }
        }

        @Nullable
        @Override
        public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

            Cursor cursor;

            switch (sUriMatcher.match(uri)){
                case CODE_ARTICLE_WITH_TEXT:
                    String textArticle = uri.getLastPathSegment();

                    String[] selectionArguments = new String[]{textArticle};

                    cursor = mOpenHelper.getReadableDatabase().query(
                            MedicationContract.MedicationEntry.TABLE_NAME,
                            projection,
                            MedicationContract.MedicationEntry.COLUMN_MED_TEXT + " = ? ",
                            selectionArguments,
                            null,
                            null,
                            sortOrder);

                    break;

                case CODE_ARTICLE_WITH_NUMBER:
                    String numberArticle = uri.getLastPathSegment();

                    String[] selecArgs = new String[]{numberArticle};

                    cursor = mOpenHelper.getReadableDatabase().query(
                            MedicationContract.MedicationEntry.TABLE_NAME,
                            projection,
                            MedicationContract.MedicationEntry._ID + " = ? ",
                            selecArgs,
                            null,
                            null,
                            sortOrder);

                    break;

                case CODE_ARTICLE:
                    cursor = mOpenHelper.getReadableDatabase().query(
                            MedicationContract.MedicationEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);

                    break;


                default:
                    throw new UnsupportedOperationException("Uri desconocido: " + uri);
            }

            cursor.setNotificationUri(getContext().getContentResolver(), uri);

            return cursor;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            throw new RuntimeException("No implementado.");
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
            throw new RuntimeException("No implementado.");
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            int numberRowsDeleted;

            if (null == selection) selection = "1";

            switch (sUriMatcher.match(uri)){
                case CODE_ARTICLE:
                    numberRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                            MedicationContract.MedicationEntry.TABLE_NAME,
                            selection,
                            selectionArgs);
                    break;

                default:
                    throw new UnsupportedOperationException("Uri desconocido : " + uri);
            }

            if (numberRowsDeleted != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return  numberRowsDeleted;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            throw new RuntimeException("No implementado.");
        }
}
