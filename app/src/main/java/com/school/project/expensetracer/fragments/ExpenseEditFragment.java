package com.school.project.expensetracer.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.school.project.expensetracer.db.ExpenseDbHelper;
import com.school.project.expensetracer.providers.ExpensesContract.Categories;
import com.school.project.expensetracer.providers.ExpensesContract.Expenses;
import com.school.project.expensetracer.R;
import com.school.project.expensetracer.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class ExpenseEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_EDIT_EXPENSE = "com.school.project.expensetracer.edit_expense";

    private static final int EXPENSE_LOADER_ID = 1;
    private static final int CATEGORIES_LOADER_ID = 0;

    private EditText mExpValueEditText;
    private AppCompatSpinner mCategorySpinner;
    private SimpleCursorAdapter mAdapter;
    private View mCatProgressBar;
    private long mExtraValue;
    private long mExpenseCategoryId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_expense_edit, container, false);

        mExpValueEditText = (EditText) rootView.findViewById(R.id.expense_value_edit_text);
        mCatProgressBar = rootView.findViewById(R.id.cat_select_progress_bar);
        mCategorySpinner = (AppCompatSpinner) rootView.findViewById(R.id.category_choose_spinner);

        setEditTextDefaultValue();

        // Set listener on Done (submit) button on keyboard clicked
        mExpValueEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    checkValueFieldForIncorrectInput();
                    return true;
                }
                return false;
            }
        });

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mExpenseCategoryId = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                null,
                new String[] { Categories.NAME },
                new int[] { android.R.id.text1 },
                0);
        // Specify the layout to use when the list of choices appears
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(mAdapter);

        mExtraValue = getActivity().getIntent().getLongExtra(EXTRA_EDIT_EXPENSE, -1);
        // Create a new expense
        if (mExtraValue < 1) {
            getActivity().setTitle(R.string.add_expense);
            loadCategories();

            // Edit existing expense
        } else {
            getActivity().setTitle(R.string.edit_expense);
            loadExpenseData();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_expense_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences sessionPreferences =
                getActivity().getSharedPreferences("ExpenseSession", MODE_PRIVATE);
        ExpenseDbHelper expenseDbHelper = new ExpenseDbHelper(getActivity());
        double totalExpense=0.0;
        double dbExpense = expenseDbHelper._Total("expense",""+mExpenseCategoryId);
        double cExp=0.0;

        if (!mExpValueEditText.getText().toString().trim().equals(""))
        {
            cExp = Double.valueOf(mExpValueEditText.getText().toString().trim());
        }

        totalExpense = dbExpense+cExp;
        if (item.getItemId() == R.id.done_expense_edit_menu_item) {
            if (checkForIncorrectInput()) {
                // Create a new expense
                if (totalExpense > sessionPreferences.getFloat("_limit", 0)) {

                    String msg = "Dear "+ sessionPreferences.getString("_name","")+", You are crossing your expense limit for "+expenseDbHelper.catName(""+mExpenseCategoryId);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setTitle("Alert Limit!!");
                    builder.setMessage(msg);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            insertNewExpense();
                            getActivity().finish();


                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //if user select "No", just cancel this dialog and continue with app
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();



                    // Toast.makeText(getActivity(), "You are crossing your expense limit!", Toast.LENGTH_LONG).show();


                }
               else  if (mExtraValue < 1) {
                    insertNewExpense();
                     getActivity().finish();
                    // Edit existing expense
                }

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkForIncorrectInput() {
        if (!checkValueFieldForIncorrectInput()) {
            mExpValueEditText.selectAll();
            return false;
        }
        // Future check of other fields

        return true;
    }

    private boolean checkValueFieldForIncorrectInput() {
        String etValue = mExpValueEditText.getText().toString();
        try {
            if (etValue.length() == 0) {
                mExpValueEditText.setError(getResources().getString(R.string.error_empty_field));
                return false;
            } else if (Float.parseFloat(etValue) == 0.00f) {
                mExpValueEditText.setError(getResources().getString(R.string.error_zero_value));
                return false;
            }
        } catch (Exception e) {
            mExpValueEditText.setError(getResources().getString(R.string.error_incorrect_input));
            return false;
        }
        return true;
    }

    private void loadCategories() {
        // Show the progress bar next to category spinner
        mCatProgressBar.setVisibility(View.VISIBLE);

        getLoaderManager().initLoader(CATEGORIES_LOADER_ID, null, this);
    }

    private void loadExpenseData() {
        getLoaderManager().initLoader(EXPENSE_LOADER_ID, null, this);
        loadCategories();
    }

    private void setEditTextDefaultValue() {
        mExpValueEditText.setText(String.valueOf(0));
        mExpValueEditText.selectAll();
    }

    @Override
    public CursorLoader onCreateLoader(int id, Bundle args) {
        String[] projectionFields = null;
        Uri uri = null;
        switch (id) {
            case EXPENSE_LOADER_ID:
                projectionFields = new String[] {
                        Expenses._ID,
                        Expenses.VALUE,
                        Expenses.CATEGORY_ID
                };

                uri = ContentUris.withAppendedId(Expenses.CONTENT_URI, mExtraValue);
                break;
            case CATEGORIES_LOADER_ID:
                projectionFields = new String[] {
                        Categories._ID,
                        Categories.NAME
                };

                uri = Categories.CONTENT_URI;
                break;
        }

        return new CursorLoader(getActivity(),
                uri,
                projectionFields,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case EXPENSE_LOADER_ID:
                int expenseValueIndex = data.getColumnIndex(Expenses.VALUE);
                int expenseCategoryIdIndex = data.getColumnIndex(Expenses.CATEGORY_ID);

                data.moveToFirst();
                mExpenseCategoryId = data.getLong(expenseCategoryIdIndex);
                updateSpinnerSelection();

                mExpValueEditText.setText(String.valueOf(data.getFloat(expenseValueIndex)));
                mExpValueEditText.selectAll();
                break;
            case CATEGORIES_LOADER_ID:
                // Hide the progress bar next to category spinner
                mCatProgressBar.setVisibility(View.GONE);

                if (null == data || data.getCount() < 1) {
                    mExpenseCategoryId = -1;
                    // Fill the spinner with default values
                    ArrayList<String> defaultItems = new ArrayList<>();
                    defaultItems.add(getResources().getString(R.string.no_categories_string));

                    ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_spinner_item,
                            defaultItems);
                    mCategorySpinner.setAdapter(tempAdapter);
                    // Disable the spinner
                    mCategorySpinner.setEnabled(false);
                } else {
                    // Set the original adapter
                    mCategorySpinner.setAdapter(mAdapter);
                    // Update spinner data
                    mAdapter.swapCursor(data);
                    // Enable the spinner
                    mCategorySpinner.setEnabled(true);
                    updateSpinnerSelection();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EXPENSE_LOADER_ID:
                mExpenseCategoryId = -1;
                setEditTextDefaultValue();
                break;
            case CATEGORIES_LOADER_ID:
                mAdapter.swapCursor(null);
                break;
        }
    }

    private void updateSpinnerSelection() {
        mCategorySpinner.setSelection(0);
        for (int pos = 0; pos < mAdapter.getCount(); ++pos) {
            if (mAdapter.getItemId(pos) == mExpenseCategoryId) {
                // Set spinner item selected according to the value from db
                mCategorySpinner.setSelection(pos);
                break;
            }
        }
    }

    private void insertNewExpense() {
        ContentValues insertValues = new ContentValues();
        insertValues.put(Expenses.VALUE, Float.parseFloat(mExpValueEditText.getText().toString()));
        insertValues.put(Expenses.DATE, Utils.getDateString(new Date())); // Put current date (today)
        insertValues.put(Expenses.TYPE, "expense");
        insertValues.put(Expenses.CATEGORY_ID, mExpenseCategoryId);

        getActivity().getContentResolver().insert(
                Expenses.CONTENT_URI,
                insertValues
        );

        Toast.makeText(getActivity(),
                getResources().getString(R.string.expense_added),
                Toast.LENGTH_SHORT).show();
    }

    private void updateExpense(long id) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Expenses.VALUE, Float.parseFloat(mExpValueEditText.getText().toString()));
        updateValues.put(Expenses.CATEGORY_ID, mExpenseCategoryId);

        Uri expenseUri = ContentUris.withAppendedId(Expenses.CONTENT_URI, id);

        getActivity().getContentResolver().update(
                expenseUri,
                updateValues,
                null,
                null
        );

        Toast.makeText(getActivity(),
                getResources().getString(R.string.expense_updated),
                Toast.LENGTH_SHORT).show();
    }
}