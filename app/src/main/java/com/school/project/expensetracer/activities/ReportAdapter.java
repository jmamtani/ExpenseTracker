package com.school.project.expensetracer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.school.project.expensetracer.R;

import java.util.List;




/**
 * Created by Sudesh on 3/9/2017.
 */

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Context mContext;
    private List<ReportModel> mArryList;




    public ReportAdapter(Context mContext, List<ReportModel> mArryList)
    {
        super();
        this.mContext = mContext;
        this.mArryList = mArryList;

    }
    @Override
    public int getItemCount()
    {
        return mArryList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType){
            case 0:{
                View v = inflater.inflate(R.layout.report_menu, parent, false);
                viewHolder = new ContentViewHolder(v);
                break;
            }

        }
        return viewHolder;
    }


    public class ContentViewHolder extends RecyclerView.ViewHolder
    {

        TextView t_date,t_category,t_amount;


        public ContentViewHolder(View parent)
        {
            super(parent);


            t_date = (TextView) parent.findViewById(R.id.t_date);
            t_category = (TextView) parent.findViewById(R.id.t_category);
            t_amount = (TextView) parent.findViewById(R.id.t_amount);






        }


    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder vHolder, final int position)
    {

        final ReportModel tModel = mArryList.get(position);




        switch(vHolder.getItemViewType()){
            case 0:{
                final ContentViewHolder holder = (ContentViewHolder) vHolder;


                holder.t_date.setText(tModel.getDate());
                holder.t_category.setText(tModel.getCategory());
                holder.t_amount.setText(tModel.getAmount());

                }





                break;
            }

        }



}
