package com.ugr.gbv.farmacia;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.ugr.gbv.farmacia.data.MedicationContract;
import com.ugr.gbv.farmacia.data.MedicationDbHelper;
import com.ugr.gbv.farmacia.fast_scroller.FastScroller;
import com.ugr.gbv.farmacia.fast_scroller.ScrollingLinearLayoutManager;
import com.ugr.gbv.farmacia.utilities.DataBaseUtils;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        MedicationAdapter.MedicationAdapterOnClickHandler,
        SearchView.OnQueryTextListener{


    public static final String[] MAIN_ARTICLE_PROJECTION = {
            MedicationContract.MedicationEntry.COLUMN_MED_NAME,
            MedicationContract.MedicationEntry.COLUMN_MED_TEXT,
            MedicationContract.MedicationEntry._ID
    };



    private MedicationAdapter medicationsAdapter;
    private RecyclerView recyclerView;
    private SQLiteDatabase mDb;
    private int mPosition = RecyclerView.NO_POSITION;
    private FastScroller fastScroller;
    private String searchedText;


    private boolean isNumberSearch = false;
    private boolean isSearching = false;



    private ProgressBar mLoadingIndicator;


    private static final int ID_MED_LOADER = 95;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_menu);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setElevation(0f);

        //Asignar la vista al RecyclerView
        recyclerView = findViewById(R.id.rv_articles);

        //Asignar la barra de cargado
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        //Generar una interfaz y asignarsela al RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Generar un asistente de la base de datos
        MedicationDbHelper dbHelper = new MedicationDbHelper(this);

        //Asignar una base de datos legible
        mDb = dbHelper.getReadableDatabase();

        //Obtiene todos los articulos para la posterior visualizacion
        Cursor cursor = DataBaseUtils.getAllArticles(mDb);

        //Optimiza la vista para que vaya mas fluida
        recyclerView.setHasFixedSize(true);

        //Se pasa al adaptador de la vista la informacion que procesa para ser visualizada
        medicationsAdapter = new MedicationAdapter(this, cursor, this);

        //Se vincula el adaptador al recyclerView para que se visualice
        recyclerView.setAdapter(medicationsAdapter);

        int duration = getResources().getInteger(R.integer.scroll_duration);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false, duration));
        fastScroller = findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(recyclerView);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), layoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);

        showLoading();

        getSupportLoaderManager().initLoader(ID_MED_LOADER, null, this);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch(loaderId){


            case ID_MED_LOADER:
                Uri articlesQueryUri = MedicationContract.MedicationEntry.CONTENT_URI;
                String sortOrder = MedicationContract.MedicationEntry._ID;

                return new CursorLoader(this,
                        articlesQueryUri,
                        MAIN_ARTICLE_PROJECTION,
                        null,
                        null,
                        sortOrder);


            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);

        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(searchedText != null && !searchedText.isEmpty()) {
            if (isNumberSearch){
                data = DataBaseUtils.getAllArticleNumbersMatch(searchedText, mDb);
            }
            else {
                data = DataBaseUtils.getAllArticlesMatch(searchedText, mDb);
            }
        }


        medicationsAdapter.swapCursor(data);
        if(mPosition <= RecyclerView.NO_POSITION){
            mPosition = 0;
        }

        recyclerView.scrollToPosition(mPosition);
        
        if(data.getCount() > 0){
            showData();
        }



    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        medicationsAdapter.swapCursor(null);
    }
    /*  Cuando se presiona sobre un articulo se muestra con una nueva actividad mostrando el texto
     * */
    @Override
    public void onClick(String text, int first_pos, int last_pos, int la_pos) {
        Intent intent = new Intent(this, DetailArticle.class);
        Uri uriForTextClicked = MedicationContract.MedicationEntry.buildUriWithText(text);

        intent.setData(uriForTextClicked);
        intent.putExtra("la_palabra", searchedText);
        intent.putExtra("is_searching", isSearching);
        intent.putExtra("is_number_searching", isNumberSearch);
        intent.putExtra("la_pos", la_pos);
        intent.putExtra("first_pos",first_pos);
        intent.putExtra("last_pos",last_pos);
        startActivity(intent);

    }




    @Override
    public void goToArticle(int id){
        Cursor cursor;
        cursor = DataBaseUtils.getAllArticles(mDb);
        medicationsAdapter.setFilter(cursor);
        id+=3;
        if(id > cursor.getCount()){
            cursor.moveToLast();
            id = cursor.getPosition();
        }
        recyclerView.scrollToPosition(id);
    }





    private void showLoading() {
        recyclerView.setVisibility(View.INVISIBLE);
        fastScroller.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);

    }


    private void showData() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        fastScroller.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchItem.setOnActionExpandListener(
                new MenuItem.OnActionExpandListener() {

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        isSearching = true;
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        isSearching = false;
                        isNumberSearch = false;
                        return true;
                    }
                }
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            //Cuando se pulsa el botón atrás del IU no se muestran palabras
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Cursor cursor;
        if(!TextUtils.isEmpty(newText)){
            searchedText = newText;
            cursor = DataBaseUtils.getAllArticlesMatch(newText, mDb);
            if(isNumberSearch)
                isNumberSearch = false;
        } else{
            cursor = DataBaseUtils.getAllArticles(mDb);
        }

        medicationsAdapter.setFilter(cursor);

        return true;
    }


}
