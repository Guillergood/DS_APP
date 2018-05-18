package com.ugr.gbv.farmacia.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MedicationContract {

    public static final String CONTENT_AUTHORITY = "com.ugr.gbv.farmacia";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ARTICLE = "medication";

    public static final class MedicationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ARTICLE)
                .build();

        public static final String TABLE_NAME = "articmedications";
        public static final String COLUMN_MED_NAME = "medicationName";
        public static final String COLUMN_MED_TEXT = "medicationText";


        public static Uri buildUriWithText(String text) {
            return CONTENT_URI.buildUpon()
                    .appendPath(text)
                    .build();
        }


    }
}
