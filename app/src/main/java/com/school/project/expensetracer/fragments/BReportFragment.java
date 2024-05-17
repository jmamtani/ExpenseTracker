package com.school.project.expensetracer.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.school.project.expensetracer.R;
import com.school.project.expensetracer.activities.ReportAdapter;
import com.school.project.expensetracer.activities.ReportModel;
import com.school.project.expensetracer.db.ExpenseDbHelper;

import java.util.ArrayList;
import java.util.List;

public class BReportFragment extends Fragment implements OnChartValueSelectedListener {

    ExpenseDbHelper expenseDbHelper;
    private List<ReportModel> tRArrayList;
    private ReportAdapter tRAdapter;
    private RecyclerView tRrecyclerView;
    private  RecyclerView.LayoutManager tRlayoutManager;
    RadioGroup rgTax;
    Spinner mSpinner;
    String sMCode="";
    ArrayList<String> mNameList,mCodeList;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    String rType="income";
    View rootView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.b_fragment_report, container, false);

        expenseDbHelper = new ExpenseDbHelper(getActivity());

        tRrecyclerView = (RecyclerView) rootView.findViewById(R.id.report);
        tRrecyclerView.setHasFixedSize(true);
        tRlayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        tRrecyclerView.setLayoutManager(tRlayoutManager);

        mSpinner = (Spinner) rootView.findViewById(R.id.month_spinner);
        mNameList = new ArrayList<>();
        mCodeList = new ArrayList<>();

        rgTax = (RadioGroup)rootView.findViewById(R.id.rg_tax);

        rgTax.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {

                    case R.id.rb_1:
                        rType = "income";
                        report(rType,sMCode);

                        break;



                    case R.id.rb_2:
                        rType = "expense";
                        report(rType,sMCode);


                        break;





                    default:
                        break;
                }
            }


        });

        //_Total



        monthSpinner();



        return rootView;
    }









    // For Month Spinner
    private void monthSpinner()
    {

        mCodeList.clear();
        mNameList.clear();

        for (int i = 0; i <= 12; i++)
        {
            String temp = "";
            if(i<10) { temp = "0"+i; }
            else { temp = ""+i; }

            mCodeList.add(temp);


        }

        mNameList.add("All");
        mNameList.add("Jan");
        mNameList.add("Feb");
        mNameList.add("Mar");
        mNameList.add("Apr");
        mNameList.add("May");
        mNameList.add("Jun");
        mNameList.add("Jul");
        mNameList.add("Aug");
        mNameList.add("Sep");
        mNameList.add("Oct");
        mNameList.add("Nov");
        mNameList.add("Dec");





        ArrayAdapter<String> varientAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, mNameList);
        varientAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinner.setAdapter(varientAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                sMCode="";
                sMCode = mCodeList.get(arg2);

                totalData(sMCode);
                report(rType,sMCode);


            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });



    }

    // For Total
    private void totalData(String mCode)
    {
        TextView  in_amt = (TextView) rootView.findViewById(R.id.in_amt);
        TextView   exp_amt = (TextView)  rootView.findViewById(R.id.exp_amt);
        TextView   tBalanceAmt = (TextView)  rootView.findViewById(R.id.balance_amt);


        if (mCode.equals("00"))
        {
            double income = expenseDbHelper._Total("income");
            double expense = expenseDbHelper._Total("expense");
            double avlAmt = income-expense;

            in_amt.setText(""+income);
            exp_amt.setText(""+expense);
            tBalanceAmt.setText(""+avlAmt);
        }
        else {
            double income = expenseDbHelper._TotalFilter("income",mCode);
            double expense = expenseDbHelper._TotalFilter("expense",mCode);
            double avlAmt = income-expense;

            in_amt.setText(""+income);
            exp_amt.setText(""+expense);
            tBalanceAmt.setText(""+avlAmt);
        }


    }


    private void report(String type,String mCode) {
       // Log.e("Data","::"+type+"::"+mCode);
        tRArrayList = new ArrayList<ReportModel>();
        if (mCode.equals("00"))
        {
          //  Log.e("Data","::"+type+"::"+mCode);

            tRArrayList = new ArrayList<ReportModel>(expenseDbHelper.fetchDetails(type));
            tRAdapter = new ReportAdapter(getActivity(),tRArrayList);
            tRrecyclerView.setAdapter(tRAdapter);
        }
        else {
            tRArrayList = new ArrayList<ReportModel>(expenseDbHelper.fetchDetails(type,mCode));
            tRAdapter = new ReportAdapter(getActivity(),tRArrayList);
            tRrecyclerView.setAdapter(tRAdapter);
        }



    }

    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {
       /* IMarker marker = new BarcharPopup(getActivity(),R.layout.barchart_menu);
        mChart.setMarker(marker);*/
    }

    @Override
    public void onNothingSelected() {

    }
}
