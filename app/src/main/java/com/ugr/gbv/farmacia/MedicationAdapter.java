package com.ugr.gbv.farmacia;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ugr.gbv.farmacia.data.MedicationContract;

import java.util.ArrayList;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.NumberViewHolder> {

    private Context mContext;

    private Cursor mCursor;

    public MedicationAdapter(Context pContext, Cursor pCursor, MedicationAdapterOnClickHandler pHandler) {
        mContext = pContext;
        mCursor = pCursor;
        mClickHandler = pHandler;
    }

    final private MedicationAdapterOnClickHandler mClickHandler;


    interface MedicationAdapterOnClickHandler{
        void onClick(String text);
    }


    public static final int INDEX_MED_ID = 0;
    public static final int INDEX_MED_NAME = 1;
    public static final int INDEX_MED_PRICE = 2;



    @Override
    public NumberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.number_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        NumberViewHolder viewHolder = new NumberViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(NumberViewHolder holder, int position) {
        //Comprueba si donde se ha movido el cursor hay informacion valida
        if(!mCursor.moveToPosition(position))
            return;
        //Si la hay...

        holder.itemView.setLongClickable(true);
        //Capta el nombre del articulo
        String articleName = mCursor.getString(
                mCursor.getColumnIndex(MedicationContract.MedicationEntry.COLUMN_MED_NAME));

        //Se introduce en la lista el nombre del articulo
        holder.articleName.setText(articleName);

        if(multiSelect){
            if(selectedItems.contains(position)){
                holder.linearLayout.setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.colorAccent));
            }
            else {
                holder.linearLayout.setBackgroundColor(Color.WHITE);
            }
        }
        else {
            holder.linearLayout.setBackgroundColor(Color.WHITE);
        }

    }


    @Override
    public int getItemCount() {
        int number;
        if(mCursor != null){
            number = mCursor.getCount();
        }
        else{
            number = 0;
        }
        return number;
    }

    void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }


    void setFilter (Cursor pCursor){
        swapCursor(pCursor);
    }



    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback(){

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            selectedItems = new ArrayList<>();
            realIds = new ArrayList<>();
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_context, menu);
            actionMode = mode;
            actionMode.setTitle(selectedItems.size() + " " +
                    mContext.getString(R.string.fils_selected));
            buy_item = menu.findItem(R.id.action_buy);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch (id){
                case R.id.action_buy:
                    int actual = mCursor.getPosition();
                    double precio = 0.0;
                    for(int x:selectedItems){
                        mCursor.moveToPosition(x);
                        String nombre = mCursor.getString(INDEX_MED_NAME);
                        precio += mCursor.getInt(INDEX_MED_PRICE);
                    }
                    mCursor.moveToPosition(actual);
                    Toast.makeText(mContext, "COMPRA REALIZADA PRECIO = " + precio + " EUROS" ,Toast.LENGTH_LONG).show();

                    break;
                case R.id.action_clear:
                    selectedItems.clear();
                    realIds.clear();
                    actionMode.setTitle(
                            selectedItems.size()+ " " +
                                    mContext.getString(R.string.fils_selected));
                    notifyDataSetChanged();
                    break;
                default:
                    mode.finish();
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItems.clear();
            realIds.clear();
            buy_item = null;
            notifyDataSetChanged();
        }
    };

    private boolean multiSelect = false;
    private ArrayList<Integer> selectedItems;
    private ArrayList<Integer> realIds;
    private MenuItem buy_item;
    private ActionMode actionMode;





    /**
     * Clase interna para manejar un sólo objeto en el recyclerView
     */
    class NumberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener{

        TextView articleName;
        LinearLayout linearLayout;



        NumberViewHolder(View itemView) {
            super(itemView);


            articleName =  itemView.findViewById(R.id.tv_item_number);
            linearLayout = itemView.findViewById(R.id.linear_item_layout);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }



        void selectItem(Integer item, Integer id) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    realIds.remove(id);
                    linearLayout.setBackgroundColor(Color.WHITE);
                } else {
                    selectedItems.add(item);
                    realIds.add(id);
                    linearLayout.setBackgroundColor(
                            ContextCompat.getColor(mContext, R.color.colorAccent));
                }

                int rows_selected = selectedItems.size();


                if(rows_selected == 1){
                    actionMode.setTitle(
                            rows_selected + " " +  mContext.getString(R.string.one_fil_selected));
                }
                else {
                    actionMode.setTitle(
                            rows_selected + " " +  mContext.getString(R.string.fils_selected));
                }

            }
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if(adapterPosition >= 0) {
                if (multiSelect) {
                    mCursor.moveToPosition(adapterPosition);
                    String text = mCursor.getString
                            (mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
                    int number = Integer.parseInt(text);
                    selectItem(adapterPosition, number);
                    notifyItemChanged(adapterPosition);
                } else {
                    mCursor.moveToPosition(adapterPosition);

                    String text = mCursor.getString
                            (mCursor.getColumnIndex(MedicationContract.MedicationEntry.COLUMN_MED_TEXT));
                    mClickHandler.onClick(text);
                }
            }

        }

        @Override
        public boolean onLongClick(View view) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition >= 0) {
                if (multiSelect) {
                    mCursor.moveToPosition(adapterPosition);
                } else {
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallbacks);
                }
                mCursor.moveToPosition(adapterPosition);
                String text = mCursor.getString
                        (mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
                int number = Integer.parseInt(text);
                selectItem(adapterPosition, number);
                notifyItemChanged(adapterPosition);


                return !(selectedItems.size() < 1);
            }
            else{
                return false;
            }
        }




    }
}
