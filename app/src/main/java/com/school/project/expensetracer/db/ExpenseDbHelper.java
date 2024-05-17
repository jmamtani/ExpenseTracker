package com.school.project.expensetracer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.school.project.expensetracer.R;
import com.school.project.expensetracer.activities.ReportModel;
import com.school.project.expensetracer.providers.ExpensesContract.Categories;
import com.school.project.expensetracer.providers.ExpensesContract.Expenses;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "expense_tracer.db";

    public static final String USER_TABLE_NAME = "user";
    public static final String CATEGORIES_TABLE_NAME = "categories";
    public static final String INCOME_EXPENSES_TABLE_NAME = "expenses";

    private Context mContext;

    public ExpenseDbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CategoriesTable.CREATE_TABLE_QUERY);
        // Fill the table with predefined values
        CategoriesTable.fillTable(db, mContext);

        db.execSQL(ExpenseIncomeTable.CREATE_TABLE_QUERY);

        db.execSQL(UserTable.CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* Temporary (dummy) upgrade policy */
        db.execSQL(ExpenseIncomeTable.DELETE_TABLE_QUERY);
        db.execSQL(CategoriesTable.DELETE_TABLE_QUERY);
        db.execSQL(UserTable.DELETE_TABLE_QUERY);
        onCreate(db);
    }

    private static final class CategoriesTable
    {
        public static final String CREATE_TABLE_QUERY =
                "CREATE TABLE " + CATEGORIES_TABLE_NAME + " (" +
                Categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Categories.NAME + " TEXT NOT NULL);";

        public static final String DELETE_TABLE_QUERY =
                "DROP TABLE IF EXISTS " + CATEGORIES_TABLE_NAME + ";";

        public static void fillTable(SQLiteDatabase db, Context ctx) {
            String[] predefinedNames = ctx.getResources().getStringArray(R.array.predefined_categories);
            ContentValues values = new ContentValues();
            for (String name : predefinedNames) {
                values.put(Categories.NAME, name);
                db.insert(CATEGORIES_TABLE_NAME, null, values);
            }
        }
    }

    private static final class ExpenseIncomeTable {
        public static final String CREATE_TABLE_QUERY =
                "CREATE TABLE " + INCOME_EXPENSES_TABLE_NAME + " (" +
                Expenses._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Expenses.VALUE + " FLOAT NOT NULL, " +
                Expenses.DATE + " DATE NOT NULL, " +
                Expenses.TYPE + " TEXT NOT NULL, " +
                Expenses.CATEGORY_ID + " INTEGER NOT NULL);";

        public static final String DELETE_TABLE_QUERY =
                "DROP TABLE IF EXISTS " + INCOME_EXPENSES_TABLE_NAME + ";";
    }

    private  static String uId= "_id";
    private  static String uName= "_name";
    private  static String uLimit= "_limit";

    private static final class UserTable {

        public static final String CREATE_TABLE_QUERY =
                "CREATE TABLE " + USER_TABLE_NAME + " (" +
                        uId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        uName + " TEXT NOT NULL, " +
                        uLimit + " FLOAT NOT NULL ); ";

        public static final String DELETE_TABLE_QUERY =
                "DROP TABLE IF EXISTS " + USER_TABLE_NAME + ";";
    }


    public List<ReportModel> fetchDetails(String type)
    {
        List<ReportModel> rAList = new ArrayList<ReportModel>();

        // Invoice Details
        SQLiteDatabase sdbItem = this.getWritableDatabase();



        String sIPrdQuery = "";
        sIPrdQuery += "SELECT * FROM " + INCOME_EXPENSES_TABLE_NAME;
        sIPrdQuery += " WHERE " +  Expenses.TYPE + " = '" + type + "'";
        sIPrdQuery += " ORDER BY "+ Expenses._ID+" DESC " ;




        Cursor mICursor = sdbItem.rawQuery(sIPrdQuery, null);

        if (mICursor!=null && mICursor.getCount()>0)
        {
            mICursor.moveToFirst();
            do {
                ReportModel rModel = new ReportModel();
                rModel.setDate(mICursor.getString(mICursor.getColumnIndex( Expenses.DATE )));
                rModel.setCategory(fetchCategpry(mICursor.getString(mICursor.getColumnIndex(Expenses.CATEGORY_ID))));
                rModel.setAmount(mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)));


                rAList.add(rModel);

            } while (mICursor.moveToNext());

        }
        if (mICursor!=null)
        { mICursor.close(); }





        return rAList;
    }

    public List<ReportModel> fetchDetails(String type,String mCode)
    {
        List<ReportModel> rAList = new ArrayList<ReportModel>();

        // Invoice Details
        SQLiteDatabase sdbItem = this.getWritableDatabase();



        String sIPrdQuery = "";
        sIPrdQuery += "SELECT * FROM " + INCOME_EXPENSES_TABLE_NAME;
        sIPrdQuery += " WHERE " +  Expenses.TYPE + " = '" + type + "'";
        sIPrdQuery += " AND " ;
        sIPrdQuery += Expenses.DATE+" LIKE '"+mCode+"/%'";
        sIPrdQuery += " ORDER BY "+ Expenses._ID+" DESC " ;




        Cursor mICursor = sdbItem.rawQuery(sIPrdQuery, null);

        if (mICursor!=null && mICursor.getCount()>0)
        {
            mICursor.moveToFirst();
            do {
                ReportModel rModel = new ReportModel();
                rModel.setDate(mICursor.getString(mICursor.getColumnIndex( Expenses.DATE )));
                rModel.setCategory(fetchCategpry(mICursor.getString(mICursor.getColumnIndex(Expenses.CATEGORY_ID))));
                rModel.setAmount(mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)));


                rAList.add(rModel);

            } while (mICursor.moveToNext());

        }
        if (mICursor!=null)
        { mICursor.close(); }





        return rAList;
    }
    private String fetchCategpry(String _id)
    {
        String tName="";
        SQLiteDatabase iDb = getReadableDatabase();

        String sQuery = "SELECT * FROM "+CATEGORIES_TABLE_NAME+ " WHERE "
                +Categories._ID+ " = '"+_id+"'";


        Cursor mcursor = iDb.rawQuery(sQuery, null);
        mcursor.moveToLast();
        tName =  mcursor.getString(mcursor.getColumnIndex(Categories.NAME));
        mcursor.close();


        return tName;

    }

    public double _TotalFilter(String type,String mCode)
    {
        double rAmt = 0.0;

        // Invoice Details
        SQLiteDatabase sdbItem = this.getWritableDatabase();


        //" AND "+;+" LIKE '%"+stMnthYear+"%'"+

        String sIPrdQuery = "";
        sIPrdQuery += "SELECT * FROM " + INCOME_EXPENSES_TABLE_NAME;
        sIPrdQuery += " WHERE " +  Expenses.TYPE + " = '" + type + "'";
        sIPrdQuery += " AND " ;
        sIPrdQuery += Expenses.DATE+" LIKE '"+mCode+"/%'";





        Cursor mICursor = sdbItem.rawQuery(sIPrdQuery, null);

        if (mICursor!=null && mICursor.getCount()>0)
        {
            mICursor.moveToFirst();
            do {
                if (!mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)).isEmpty())
                {
                    rAmt+=Double.valueOf(mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)));

                }

            } while (mICursor.moveToNext());

        }
        if (mICursor!=null)
        { mICursor.close(); }





        return rAmt;
    }

    public double _Total(String type)
    {
        double rAmt = 0.0;

        // Invoice Details
        SQLiteDatabase sdbItem = this.getWritableDatabase();



        String sIPrdQuery = "";
        sIPrdQuery += "SELECT * FROM " + INCOME_EXPENSES_TABLE_NAME;
        sIPrdQuery += " WHERE " +  Expenses.TYPE + " = '" + type + "'";





        Cursor mICursor = sdbItem.rawQuery(sIPrdQuery, null);

        if (mICursor!=null && mICursor.getCount()>0)
        {
            mICursor.moveToFirst();
            do {
                if (!mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)).isEmpty())
                {
                    rAmt+=Double.valueOf(mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)));

                }

            } while (mICursor.moveToNext());

        }
        if (mICursor!=null)
        { mICursor.close(); }





        return rAmt;
    }

    public double _Total(String type,String catId)
    {
        double rAmt = 0.0;

        // Invoice Details
        SQLiteDatabase sdbItem = this.getWritableDatabase();



        String sIPrdQuery = "";
        sIPrdQuery += "SELECT * FROM " + INCOME_EXPENSES_TABLE_NAME;
        sIPrdQuery += " WHERE " +  Expenses.TYPE + " = '" + type + "'";
        sIPrdQuery += " AND ";
        sIPrdQuery +=  Expenses.CATEGORY_ID + " = '" + catId + "'";





        Cursor mICursor = sdbItem.rawQuery(sIPrdQuery, null);

        if (mICursor!=null && mICursor.getCount()>0)
        {
            mICursor.moveToFirst();
            do {
                if (!mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)).isEmpty())
                {
                    rAmt+=Double.valueOf(mICursor.getString(mICursor.getColumnIndex( Expenses.VALUE)));

                }

            } while (mICursor.moveToNext());

        }
        if (mICursor!=null)
        { mICursor.close(); }





        return rAmt;
    }
    /*
        private  static String uId= "_id";
    private  static String uName= "_name";
    private  static String uLimit= "_limit";
     */
    public String newUser(String stName, String stLimit )
    {
        String luId="";
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values=new ContentValues();

        values.put(uName, stName);

        values.put(uLimit, stLimit);


        db.insert(USER_TABLE_NAME, null, values);

        db.close();

        SQLiteDatabase ldb = this.getReadableDatabase();
        Cursor cursor = ldb.rawQuery("SELECT  * FROM " + USER_TABLE_NAME, null);

        if(cursor!=null && cursor.moveToLast()){
            luId = ""+cursor.getInt(cursor.getColumnIndex(uId));
            //--get other cols values
        }
        if(cursor!=null)
        {
            cursor.close();
        }


        return luId;
    }

    public String userName(String luId)
    {
        String luName="";
        String  sQuery = "SELECT * FROM " + USER_TABLE_NAME +" WHERE "
                + uId + " = '" +luId+"'";

        SQLiteDatabase ldb = this.getReadableDatabase();
        Cursor cursor = ldb.rawQuery(sQuery, null);

        if(cursor!=null && cursor.moveToLast()){
            luName = ""+cursor.getString(cursor.getColumnIndex(uName));
            //--get other cols values
        }
        if(cursor!=null)
        {
            cursor.close();
        }


        return luName;
    }



    public float userLimit(String luId)
    {
        float luLimit=0;
        String  sQuery = "SELECT * FROM " + USER_TABLE_NAME +" WHERE "
                + uId + " = '" +luId+"'";

        SQLiteDatabase ldb = this.getReadableDatabase();
        Cursor cursor = ldb.rawQuery(sQuery , null);

        if(cursor!=null && cursor.moveToLast()){
            luLimit =cursor.getFloat(cursor.getColumnIndex(uLimit));
            //--get other cols values
        }
        if(cursor!=null)
        {
            cursor.close();
        }


        return luLimit;
    }

    public String catName(String catId)
    {
        String luName="";
        String  sQuery = "SELECT * FROM " + CATEGORIES_TABLE_NAME +" WHERE "
                + Categories._ID + " = '" +catId+"'";

        SQLiteDatabase ldb = this.getReadableDatabase();
        Cursor cursor = ldb.rawQuery(sQuery, null);

        if(cursor!=null && cursor.moveToLast()){
            luName = ""+cursor.getString(cursor.getColumnIndex(Categories.NAME));
            //--get other cols values
        }
        if(cursor!=null)
        {
            cursor.close();
        }


        return luName;
    }
}
