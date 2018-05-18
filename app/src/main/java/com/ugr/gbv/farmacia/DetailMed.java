package com.ugr.gbv.farmacia;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.ugr.gbv.farmacia.data.MedicationContract;
import com.ugr.gbv.farmacia.data.MedicationDbHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailMed extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    public static final int INDEX_MED_NAME = 0;
    public static final int INDEX_MED_TEXT = 1;
    public static final int INDEX_MED_ID = 2;

    private int firstElem;
    private int mPos = 0;
    private int mIndexPos;
    private int endElem;
    private Cursor mCursor;

    private SQLiteDatabase mDb;

    private Button prev_button;
    private Button next_button;

    private String searchedWord;

    private boolean searchingElement = false;
    private boolean searchingNumberElement = false;
    private boolean isSavedStateOn = false;

    private static final int ID_DETAIL_LOADER = 995;

    /* El URI para acceder al articulo en escogido */
    private Uri mUri;

    private ActivityDetailArticleBinding mDetailBinding;


    public static final String[] DETAIL_MED_PROJECTION = {
            MedicationContract.MedicationEntry.COLUMN_MED_NAME,
            MedicationContract.MedicationEntry.COLUMN_MED_TEXT,
            MedicationContract.MedicationEntry._ID
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail_article);
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
            if (getIntent().hasExtra("is_number_searching"))
                searchingNumberElement = getIntent().getExtras().getBoolean("is_number_searching");
            if (getIntent().hasExtra("first_pos"))
                firstElem = getIntent().getExtras().getInt("first_pos");
            if (getIntent().hasExtra("last_pos"))
                endElem = getIntent().getExtras().getInt("last_pos");
            if (getIntent().hasExtra("la_pos") && savedInstanceState == null)
                mIndexPos = getIntent().getExtras().getInt("la_pos");
            else {
                mIndexPos = savedInstanceState.getInt("the_index_pos");
                isSavedStateOn = true;
            }
        }

        prev_button = findViewById(R.id.button_ant);
        next_button = findViewById(R.id.button_sig);

        prev_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor != null)
                    moveToPrev();
            }
        });

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor != null)
                    moveToNext();
            }
        });

        if (mUri == null) throw new NullPointerException(
                "URI para la actividad DetailArticle no puede ser nulo ");


        MedicationDbHelper dbHelper = new  MedicationDbHelper(this);
        //Asignar una base de datos legible
        mDb = dbHelper.getReadableDatabase();



        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);


    }

    private void moveToNext() {
        mCursor.moveToNext();
        if (!mCursor.isAfterLast() && mPos <= endElem) {
            mPos = mCursor.getInt(mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
            refreshData();
            mIndexPos++;
        } else {
            mCursor.moveToLast();
            mPos = mCursor.getInt(mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
            mIndexPos = mCursor.getColumnCount() - 1;
        }

    }

    private void moveToPrev() {
        mCursor.moveToPrevious();
        if (!mCursor.isBeforeFirst() && mPos >= firstElem) {
            mPos = mCursor.getInt(mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
            mIndexPos--;
            refreshData();
        } else {
            mCursor.moveToFirst();
            mPos = mCursor.getInt(mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
            mIndexPos = 0;
        }


    }


    private void refreshData() {
        String nombre = mCursor.getString(mCursor.getColumnIndex(MedicationContract.MedicationEntry.COLUMN_MED_NAME));
        String datos = mCursor.getString(mCursor.getColumnIndex(MedicationContract.MedicationEntry.COLUMN_MED_TEXT));

        if (TextUtils.equals(datos, nombre)) {
            nombre = " ";
        }

        checkIfFirst();
        checkIfLast();


        mDetailBinding.nombreArt.setText(nombre);
        if (searchingElement && datos != null) {
            highlightText(datos);
        } else {
            mDetailBinding.texto.setText(datos);
        }
    }

    private void checkIfLast() {
        if (mPos == endElem) {
            next_button.setVisibility(View.INVISIBLE);
        } else if (mPos < endElem && next_button.getVisibility() == View.INVISIBLE) {
            next_button.setVisibility(View.VISIBLE);
        }

    }

    private void checkIfFirst() {
        if (mPos == firstElem) {
            prev_button.setVisibility(View.INVISIBLE);
        } else if (mPos > firstElem && prev_button.getVisibility() == View.INVISIBLE) {
            prev_button.setVisibility(View.VISIBLE);
        }

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
        mCursor.moveToPosition(mIndexPos);

        if (!isSavedStateOn) {
            nombre = data.getString(INDEX_MED_NAME);
            datos = data.getString(INDEX_MED_TEXT);
            mPos = data.getInt(INDEX_MED_ID);
        } else {
            nombre = mCursor.getString(INDEX_MED_TEXT);
            datos = mCursor.getString(INDEX_MED_ID);
            mPos = mCursor.getInt(INDEX_MED_NAME);
            isSavedStateOn = false;
        }

        if (TextUtils.equals(datos, nombre)) {
            nombre = " ";
        }


        checkIfFirst();
        checkIfLast();

        mDetailBinding.nombreArt.setText(nombre);
        if (datos != null) {
            if (searchingElement) {
                highlightText(datos);
            } else {
                mDetailBinding.texto.setText(datos);
            }
        } else {
            mDetailBinding.texto.setText(" ");
        }


    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.forceLoad();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            /* Share menu item clicked */
            case R.id.action_share:
                Intent shareIntent = createShareArticleIntent();
                startActivity(shareIntent);
                return true;

            //Cuando se pulsa el botón atrás del IU no se muestran palabras
            case android.R.id.home:
                //TODO MODIFICAR
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    private Intent createShareArticleIntent() {
        String comparte = mDetailBinding.nombreArt.getText() + "\n\n" + mDetailBinding.texto.getText();
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(comparte + " " + getString(R.string.hastag_code))
                .getIntent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return shareIntent;
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
