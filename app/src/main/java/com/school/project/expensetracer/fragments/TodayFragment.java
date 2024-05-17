package com.school.project.expensetracer.fragments;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.school.project.expensetracer.R;
import com.school.project.expensetracer.activities.IncomeEditActivity;
import com.school.project.expensetracer.adapters.SimpleExpenseAdapter;
import com.school.project.expensetracer.db.ExpenseDbHelper;
import com.school.project.expensetracer.utils.Utils;
import com.school.project.expensetracer.activities.ExpenseEditActivity;
import com.school.project.expensetracer.providers.ExpensesContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class TodayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int SUM_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;

    private ListView mExpensesView;
    private View mProgressBar;
    private SimpleExpenseAdapter mAdapter;
    private TextView mTotalExpSumTextView;


    PieChart pieChart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    private int collection[]  = new int[2];
    private String[] xData = {"Income","Expense"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_today, container, false);

        mExpensesView = (ListView) rootView.findViewById(R.id.expenses_list_view);
        mProgressBar = rootView.findViewById(R.id.expenses_progress_bar);
        mTotalExpSumTextView = (TextView) rootView.findViewById(R.id.total_expense_sum_text_view);

        mExpensesView.setEmptyView(rootView.findViewById(R.id.expenses_empty_list_view));
        mExpensesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             //   prepareExpenseToEdit(id);
            }
        });


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefCurrency = sharedPref.getString(SettingsFragment.KEY_PREF_CURRENCY, "");
        mTotalExpSumTextView.setText(Utils.formatToCurrency(0.0f)+" "+prefCurrency);
        registerForContextMenu(mExpensesView);


        /// Pie Chart

        pieChart = (PieChart) rootView.findViewById(R.id.pie_chart);
        setDateinChart();


        return rootView;
    }

    private void setDateinChart()
    {

        ExpenseDbHelper expenseDbHelper = new ExpenseDbHelper(getActivity());

        double income = expenseDbHelper._Total("income");
        double expense = expenseDbHelper._Total("expense");
        double avlAmt = income-expense;


        collection[0] = (int)income;
        collection[1] = (int)expense;

        pieChart.getDescription().setEnabled(false);

        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Balance:"+"\n"+avlAmt);
        pieChart.setCenterTextColor(Color.parseColor("#00AAF7"));
        pieChart.setCenterTextSize(10);
        pieChart.setDrawEntryLabels(true);
        pieChart.animateX(1500, Easing.EasingOption.EaseOutBounce);
        addDataSet();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
            }

            @Override
            public void onNothingSelected()
            {

            }
        });
    }



    private void addDataSet() {
        ArrayList<PieEntry> yEntry = new ArrayList<>();


        for(int i = 0;i<collection.length;i++)
        {

            if(collection[i] != 0)
            {
                yEntry.add(new PieEntry(collection[i],xData[i]));
            }
        }

        PieDataSet pieDataSet = new PieDataSet(yEntry,"");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colorsList = new ArrayList<>();

        colorsList.add(Color.rgb(255,70,51));
        colorsList.add(Color.rgb(255,153,51));



        pieDataSet.setColors(colorsList);

        pieDataSet.setValueLinePart1OffsetPercentage(80.f);
        pieDataSet.setValueLinePart1Length(0.2f);
        pieDataSet.setValueLinePart2Length(0.4f);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(pieDataSet);
        //data.setValueFormatter(new CPFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set default values for preferences (settings) on startup
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        mAdapter = new SimpleExpenseAdapter(getActivity());
        mExpensesView.setAdapter(mAdapter);

        // Initialize the CursorLoaders
        getLoaderManager().initLoader(SUM_LOADER_ID, null, this);
        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadExpenseData();
        reloadSharedPreferences();
        setDateinChart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_today, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sessionPreferences =
                getActivity().getSharedPreferences("ExpenseSession", MODE_PRIVATE);

        switch (item.getItemId()) {
            case R.id.new_income:

                if (sessionPreferences.getString("_id","").isEmpty())
                {
                    popUpLogin();
                }

                else {
                    prepareIncomeToCreate();
                }

                return true;

            case R.id.new_expense:

                if (sessionPreferences.getString("_id","").isEmpty())
                {
                    popUpLogin();
                }

                else {
                    prepareExpenseToCreate();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.expense_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_expense_menu_item:
                deleteExpense(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        switch (id) {
            case SUM_LOADER_ID:
                uri = ExpensesContract.ExpensesWithCategories.SUM_DATE_CONTENT_URI;
                break;
            case LIST_LOADER_ID:
                uri = ExpensesContract.ExpensesWithCategories.DATE_CONTENT_URI;
                break;
        }

        // Retrieve today's date string
        String today = Utils.getDateString(new Date());
        String[] selectionArgs = { today };

        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case SUM_LOADER_ID:
                int valueSumIndex = data.getColumnIndex(ExpensesContract.Expenses.VALUES_SUM);
                data.moveToFirst();
                float valueSum = data.getFloat(valueSumIndex);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String prefCurrency = sharedPref.getString(SettingsFragment.KEY_PREF_CURRENCY, "");
                mTotalExpSumTextView.setText(Utils.formatToCurrency(valueSum)+" "+prefCurrency);
                break;

            case LIST_LOADER_ID:
                // Hide the progress bar
                mProgressBar.setVisibility(View.GONE);

                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case SUM_LOADER_ID:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String prefCurrency = sharedPref.getString(SettingsFragment.KEY_PREF_CURRENCY, "");
                mTotalExpSumTextView.setText(Utils.formatToCurrency(0.0f)+ " "+prefCurrency);
                break;
            case LIST_LOADER_ID:
                mAdapter.swapCursor(null);
                break;
        }
    }

    private void reloadSharedPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefCurrency = sharedPref.getString(SettingsFragment.KEY_PREF_CURRENCY, "");

        mAdapter.setCurrency(prefCurrency);
    }

    private void reloadExpenseData() {
        setDateinChart();
        // Show the progress bar
        mProgressBar.setVisibility(View.VISIBLE);
        // Reload data by restarting the cursor loaders
        getLoaderManager().restartLoader(LIST_LOADER_ID, null, this);
        getLoaderManager().restartLoader(SUM_LOADER_ID, null, this);
    }

    private int deleteSingleExpense(long expenseId) {
        Uri uri = ContentUris.withAppendedId(ExpensesContract.Expenses.CONTENT_URI, expenseId);

        // Defines a variable to contain the number of rows deleted
        int rowsDeleted;

        // Deletes the expense that matches the selection criteria
        rowsDeleted = getActivity().getContentResolver().delete(
                uri,        // the URI of the row to delete
                null,       // where clause
                null        // where args
        );

        showStatusMessage(getResources().getString(R.string.expense_deleted));
        reloadExpenseData();

        return rowsDeleted;
    }

    private void deleteExpense(final long expenseId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_expense)
                .setMessage(R.string.delete_exp_dialog_msg)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteSingleExpense(expenseId);
                    }
                })
                .show();
    }

    private void showStatusMessage(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    private void prepareExpenseToCreate() {
        startActivity(new Intent(getActivity(), ExpenseEditActivity.class));
    }

    private void prepareIncomeToCreate() {
        startActivity(new Intent(getActivity(), IncomeEditActivity.class));
    }

    private void prepareExpenseToEdit(long id) {
        Intent intent = new Intent(getActivity(), ExpenseEditActivity.class);
        intent.putExtra(ExpenseEditFragment.EXTRA_EDIT_EXPENSE, id);
        startActivity(intent);
    }

    // Sudesh Code
    private void popUpLogin()
    {
        final Dialog dialog;
        LayoutInflater li = LayoutInflater.from(getActivity());
        View popView = li.inflate(R.layout.login_popup, null);

        dialog = new Dialog(getActivity(), R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
        dialog.setCancelable(true);

     /*   SharedPreferences sessionPreferences = getSharedPreferences("App_Session", MODE_PRIVATE);
        final String stEml = sessionPreferences.getString("user_Email", "");
        String stMobile = sessionPreferences.getString("user_Mobile", "");
*/
        final EditText uName,uLimit;
        Button submit;
        TextView title;

        uName = (EditText) dialog.findViewById(R.id.u_name);
        uLimit = (EditText) dialog.findViewById(R.id.u_limit);
        submit = (Button) dialog.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                String stuName,stuLimit;
                stuName = uName.getText().toString().trim();
                stuLimit = uLimit.getText().toString().trim();

                if (stuName.isEmpty())
                {uName.setError("User name is blank");}

                else  if (stuLimit.isEmpty())
                {uLimit.setError("Category limli is blank");}


                else {
                    dialog.dismiss();


                    ExpenseDbHelper expenseDbHelper = new ExpenseDbHelper(getActivity());
                    String uId= expenseDbHelper.newUser(stuName,stuLimit);

                    SharedPreferences sessionPreferences =
                            getActivity().getSharedPreferences("ExpenseSession", MODE_PRIVATE);
                    SharedPreferences.Editor seditor = sessionPreferences.edit();
                    seditor.putString("_id",uId);
                    seditor.putString("_name",expenseDbHelper.userName(uId));
                    seditor.putFloat("_limit",expenseDbHelper.userLimit(uId));
                    seditor.apply();

                    //Log.e("UserData","::"+uId+"::"+expenseDbHelper.userName(uId)+"::"+expenseDbHelper.userLimit(uId));


                }
            }
        });







    }


}
