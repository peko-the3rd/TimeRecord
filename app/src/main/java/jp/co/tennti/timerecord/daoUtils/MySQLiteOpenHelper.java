package jp.co.tennti.timerecord.daoUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.co.tennti.timerecord.commonUtils.TimeUtils;
import jp.co.tennti.timerecord.contacts.Constants;

/**
 * Created by TENNTI on 2016/04/09.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    //private static final String DB_DIRECTORY = Environment.getExternalStorageDirectory() + "/time_record/db/";
    private static final String DB_NAME      = Constants.DB_FULL_NAME;// + "time_record_db.db";
    static final int DB_VERSION              = 1;
    private static final String TABLE_COLUMN_NAME = "( basic_date text not null primary key ," +
                                                    " leaving_date text not null ," +
                                                    " overtime text ," +
                                                    " week text ," +
                                                    " holiday_flag text ," +
                                                    " user_cd text);";

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //if (isExistDatabase(mContext) == true){
        //DBがあれば削除
        //mContext.deleteDatabase("NameAgeDB");
        //}
        //テーブル名作成
        StringBuilder builder = new StringBuilder();
        builder.append("time_record_");
        builder.append(TimeUtils.getCurrentYearAndMonth());
        final TimeUtils timeUtil = new TimeUtils();

        final Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;",  new String[]{timeUtil.getCurrentTableName().toString()});
        try {
            if (cursor.moveToNext()) {
                cursor.moveToFirst();
                if(cursor.getString(0).equals("0")){
                    db.execSQL("CREATE TABLE "+timeUtil.getCurrentTableName().toString()+TABLE_COLUMN_NAME);
                }
            }
        } catch (SQLException ex) {
            Log.e("SQLException", ex.toString());
        } finally {
            cursor.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * テーブル月更新再作成用
     * @param db SQLiteDatabase DBアクセッサ
     */
    public void reloadOnFire(SQLiteDatabase db) {
        //テーブル名作成
        StringBuilder builder = new StringBuilder();
        builder.append("time_record_");
        builder.append(TimeUtils.getCurrentYearAndMonth());
        final TimeUtils timeUtil = new TimeUtils();

        final Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;",  new String[]{timeUtil.getCurrentTableName().toString()});
        try {
            if (cursor.moveToNext()) {
                cursor.moveToFirst();
                if(cursor.getString(0).equals("0")){
                    db.execSQL("CREATE TABLE " + timeUtil.getCurrentTableName().toString() + TABLE_COLUMN_NAME);
                }
            }
        } catch (SQLException e) {
            Log.e("SQLException ERROR", e.toString());
        } finally {
            cursor.close();
        }
    }

    /**
     * テーブル存在判定
     * テーブルがあればresultは1、なければ0になるのでそれを利用してbooleanで返す。
     * @param  db DBアクセッサ SQLiteDatabase
     * @param  targetTable テーブル名 String
     * @return boolean exitFlag 判定結果
     */
    public boolean isTargetTable(SQLiteDatabase db, String targetTable) {
        boolean exitFlag = false;
        final Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;",  new String[]{targetTable});
        try {
            cursor.moveToFirst();
            if(cursor.getString(0).equals("1")){
                exitFlag = true;
            }
        } catch (SQLException e) {
            Log.e("SELECT COUNT(*) ERROR", e.toString());
        } finally {
            cursor.close();
        }
        return exitFlag;
    }
    /**
     * テーブル名取得
     * @param  db DBアクセッサ SQLiteDatabase
     * @return List<String>  list 対象のテーブル名
     * */
    public List<String> getTableName(SQLiteDatabase db) {
        List<String> list = new ArrayList<>();
        final Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;",  new String[]{});
        try {
            cursor.moveToFirst();
            do {
                Log.d("result", cursor.getString(0));
                if(!cursor.getString(0).isEmpty() && cursor.getString(0).matches(".*time_record_.*")){
                    list.add(cursor.getString(0));
                }
            } while (cursor.moveToNext());
        } catch (SQLException e) {
            Log.e("SELECT table ERROR", e.toString());
        } finally {
            cursor.close();
        }
        return list;
    }
    /**
     * DROPテーブル
     * @param  db SQLiteDatabase DBアクセッサ
     * @param  list List<String> 対象のテーブル名
     * */
    public void dropTableAll(SQLiteDatabase db,List<String> list) {
        try {
            //db.setTransactionSuccessful();
            for (String tableName : list) {
                //Log.d("tableName", tableName);
                db.execSQL("drop table " + tableName); //, new String[]{tableName});
            }
        } catch (SQLException e) {
            Log.e("SQLException ", e.toString());
        }
        /*} finally {
            db.endTransaction();
        }*/
    }

    /**
     * テーブルデータ数取得
     * 対象月のレコード数を返す。
     * @param  db SQLiteDatabase DBアクセッサ
     * @param  targetTable String テーブル名
     * @param  targetMonth String
     * @return int exitFlag 判定結果
     */
    public int countTargetMonthData(SQLiteDatabase db,String targetTable,String targetMonth) {
        int tableDataNum = 0;
        final Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "+targetTable+" WHERE basic_date LIKE \""+targetMonth+"%\";",  new String[]{});
        try {
            cursor.moveToFirst();
            if(cursor.getString(0).equals("1")){
                tableDataNum =Integer.parseInt(cursor.getString(0));
            }
        } catch (SQLException e) {
            Log.e("SQLException COUNT", e.toString());
        } finally {
            cursor.close();
        }
        return tableDataNum;
    }

    /**
     * テーブル存在判定後の作成
     * @param  db  SQLiteDatabase DBアクセッサ
     * @param  targMonthTable String テーブル名
     */
    public void createMonthTable(SQLiteDatabase db,String targMonthTable) {
        try {
            db.execSQL("CREATE TABLE " + targMonthTable + TABLE_COLUMN_NAME);
        } catch (SQLException e) {
            Log.e("SQLException CREATE", e.toString());
        }
    }
    /**
     * 対象日のデータが存在するか判定する。あった場合TRUE
     * @param  db SQLiteDatabase DBアクセッサ
     * @param  tagetTableName String テーブル名
     * @param  targetDate String 対象日付
     */
    public boolean isCurrentDate(SQLiteDatabase db ,String tagetTableName  , String targetDate) {
        //テーブルを削除した時用に新規テーブル判定と作成を行う
        if(!isTargetTable(db,tagetTableName)){
            createMonthTable(db,tagetTableName);
        }
        boolean exitFlag = false;
        final Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "+ tagetTableName +" WHERE basic_date = ?", new String[]{targetDate});
        try {
            cursor.moveToFirst();
            if(cursor.getString(0).equals("1")){
                exitFlag = true;
            }
        } catch (SQLException e) {
            Log.e("SQLException COUNT", e.toString());
        } finally {
            cursor.close();
        }
        return exitFlag;
    }

    /**
     * 対象年のテーブルデータを取得する。
     * @param  db SQLiteDatabase DBアクセッサ
     * @param  tagetTableName String テーブル名
     */
    public static Cursor getCurrentList(SQLiteDatabase db ,String tagetTableName ) {
        Cursor cursor = null;
        try {
            db.beginTransaction();
            cursor = db.rawQuery("SELECT * FROM "
                    + tagetTableName +
                    " ORDER BY basic_date ASC;", new String[]{});
            // WHERE year_month_date=? timeUtil.getCurrentYearMonthHyphen()
            //System.out.println(cursor.getCount());
        } catch (SQLException e) {
            Log.e("SQLException SELECT", e.toString());
        } finally {
            db.endTransaction();
        }
        return cursor;
    }
}