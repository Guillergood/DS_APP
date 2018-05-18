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

import com.ugr.gbv.farmacia.data.MedicationContract;

import java.util.ArrayList;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.NumberViewHolder> {

    private Context mContext;

    private Cursor mCursor;
    private int first;
    private int last;

    public MedicationAdapter(Context pContext, Cursor pCursor, MedicationAdapterOnClickHandler pHandler) {
        mContext = pContext;
        mCursor = pCursor;
        mClickHandler = pHandler;
    }

    final private MedicationAdapterOnClickHandler mClickHandler;


    interface MedicationAdapterOnClickHandler{
        void onClick(String text, int first_pos, int last_pos, int la_pos);
        void goToArticle(int position);
        void readArticles(ArrayList<Integer> realIds);
    }




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
        refreshFirstAndLast();
        notifyDataSetChanged();
    }


    void setFilter (Cursor pCursor){
        swapCursor(pCursor);
    }



    void refreshFirstAndLast() {
        if(mCursor != null && mCursor.getCount() != 0) {
            mCursor.moveToLast();
            last = mCursor.getInt(
                    mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
            mCursor.moveToFirst();
            first = mCursor.getInt(
                    mCursor.getColumnIndex(MedicationContract.MedicationEntry._ID));
        }
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
            goto_item = menu.findItem(R.id.action_goto);
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
                case R.id.action_goto:
                    int code = realIds.get(0);
                    mClickHandler.goToArticle(code);
                    if(code > 0) code -=1;
                    selectedItems.set(0,code);
                    notifyItemChanged(code);
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
            goto_item = null;
            notifyDataSetChanged();
        }
    };

    private boolean multiSelect = false;
    private ArrayList<Integer> selectedItems;
    private ArrayList<Integer> realIds;
    private MenuItem goto_item;
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

                if(rows_selected > 1) {
                    goto_item.setVisible(false);
                }
                else{
                    goto_item.setVisible(true);
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
                    refreshFirstAndLast();
                    mCursor.moveToPosition(adapterPosition);

                    String text = mCursor.getString
                            (mCursor.getColumnIndex(MedicationContract.MedicationEntry.COLUMN_MED_TEXT));
                    mClickHandler.onClick(text, first, last, adapterPosition);
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
                return true;
            }
            else{
                return false;
            }
        }




    }
}
