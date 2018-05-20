package com.ugr.gbv.farmacia;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;


import com.ugr.gbv.farmacia.data.MedicationContract;
import com.ugr.gbv.farmacia.databinding.ActivityDetailMedBinding;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailMed extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    public static final int INDEX_MED_NAME = 0;
    public static final int INDEX_MED_TEXT = 1;



    private String searchedWord;
    private boolean searchingElement;
    private ActivityDetailMedBinding mDetailBinding;

    private static final int ID_DETAIL_LOADER = 995;

    /* El URI para acceder al articulo en escogido */
    private Uri mUri;

    //private ActivityDetailArticleBinding mDetailBinding;


    public static final String[] DETAIL_MED_PROJECTION = {
            MedicationContract.MedicationEntry.COLUMN_MED_NAME,
            MedicationContract.MedicationEntry.COLUMN_MED_TEXT,
            MedicationContract.MedicationEntry._ID
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail_med);
        // Añadido para que al darle atras vuelva al menu
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        mUri = getIntent().getData();
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("la_palabra"))
                searchedWord = getIntent().getExtras().getString("la_palabra");
            else
                searchedWord = "";
            if (getIntent().hasExtra("is_searching"))
                searchingElement = getIntent().getExtras().getBoolean("is_searching");
        }

        if (mUri == null) throw new NullPointerException(
                "URI para la actividad DetailArticle no puede ser nulo ");



        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);


    }



    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {

            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        DETAIL_MED_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {


        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        String nombre = null;
        String datos = null;



        nombre = data.getString(INDEX_MED_NAME);
        datos = data.getString(INDEX_MED_TEXT);




        mDetailBinding.nombreMed.setText(nombre);
        if (datos != null) {
            if (searchingElement) {
                highlightText(datos);
            } else {
                mDetailBinding.texto.setText(datos);
            }
        }


    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.forceLoad();
    }


    public void highlightText(String data) {

        //Text from cursor in which search will perform
        //Spannable string to highlight matching searched words
        SpannableString spannableStringSearch = null;

        if ((searchedWord != null) && (!TextUtils.isEmpty(searchedWord))) {

            spannableStringSearch = new SpannableString(data);

            //compile the pattern of input text
            Pattern pattern = Pattern.compile(searchedWord,
                    Pattern.CASE_INSENSITIVE);

            //giving the compliled pattern to matcher to find matching pattern in cursor text
            Matcher matcher = pattern.matcher(data);
            spannableStringSearch.setSpan(new BackgroundColorSpan(
                            Color.TRANSPARENT), 0, spannableStringSearch.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            while (matcher.find()) {
                //highlight all matching words in cursor with white background(since i have a colorfull background image)
                spannableStringSearch.setSpan(new BackgroundColorSpan(
                                Color.YELLOW), matcher.start(), matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (spannableStringSearch != null)
            mDetailBinding.texto.setText(spannableStringSearch);
        else
            mDetailBinding.texto.setText(data);

    }
}
