package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lxp
 * @date 19-3-28
 */
public abstract class BaseDao<T> {
    protected MovieDBHelper mDbHelper;
    protected Context mContext;
    protected String mTable;


    public BaseDao(Context context, String table) {
        this.mContext = context;
        mDbHelper = MovieDBHelper.getInstance(this.mContext);
        this.mTable = table;
    }


    public long insert(ContentValues Value) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        long rowId = db.insertOrThrow(mTable, null, Value);
        db.setTransactionSuccessful();
        db.endTransaction();
        Log.v("db", "insert a row!rowId: " + rowId);
        return rowId;
    }


    /**
     * 修改一条数据 用例: ContentValues values = new ContentValues();
     * values.put("feild1", "data1"); values.put("feild2", "data2");
     * values.put("feild3", "data3"); update(values,"id=?",new String[]{5});
     *
     * @param Value
     * @param whereCaluse
     * @param whereArgs
     * @return
     */
    public int update(ContentValues Value, String whereCaluse,
                       String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        int rowId = db.update(mTable, Value, whereCaluse,
                whereArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowId;
    }

    /**
     * 用例 delete("id=? and title=?",new String[]{String.valueof(1),"测试"}
     *
     * @param whereClause 删除条件,为空则会删除所有行
     * @param whereArgs   参数列表
     */
    public int delete(String whereClause, String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        int rowcount = db.delete(mTable, whereClause,
                whereArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
        Log.v("db", "delet some rows!count " + rowcount);
        return rowcount;
    }

    public int deleteAll(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        int rowcount = db.delete(mTable, null,
                null);
        db.setTransactionSuccessful();
        db.endTransaction();
        Log.v("db", "delet some rows!count " + rowcount);
        return rowcount;
    }

    /**
     * @param fields      列名称数组
     * @param whereClause 条件字句
     * @param whereArgs   条件字句，参数数组
     * @param groupBy     分组列
     * @param having      分组条件
     * @param orderBy     排序列
     * @param limit       分页查询限制
     */
    public Cursor select(String[] fields, String whereClause,
                         String[] whereArgs, String groupBy, String having, String orderBy,
                         String limit) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(mTable, fields,
                whereClause, whereArgs, groupBy, having, orderBy, limit);
        return cursor;
    }

    /**
     * @param fields      列名称数组
     * @param whereClause 条件字句
     * @param whereArgs   条件字句，参数数组
     * @param groupBy     分组列
     * @param having      分组条件
     * @param orderBy     排序列
     */
    public Cursor select(String[] fields, String whereClause,
                         String[] whereArgs, String groupBy, String having, String orderBy) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(mTable, fields,
                whereClause, whereArgs, groupBy, having, orderBy);
        return cursor;
    }

    /**
     * 根据条件选出指定列的数据
     *
     * @param fields      列名称数组
     * @param whereClause 条件字句
     * @param whereArgs   条件字句，参数数组
     * @return
     */
    public Cursor select(String[] fields, String whereClause, String[] whereArgs) {
        Cursor cursor = this.select(fields, whereClause, whereArgs, null, null,
                null, null);
        return cursor;
    }

    /**
     * 根据条件选出指定分页的指定列的数据
     *
     * @param fields      列名称数组
     * @param whereClause 条件字句
     * @param whereArgs   条件字句，参数数组
     * @param limit       分页查询限制
     * @return
     */
    public Cursor select(String[] fields, String whereClause,
                         String[] whereArgs, String limit) {
        Cursor cursor = this.select(fields, whereClause, whereArgs, null, null,
                null, limit);
        return cursor;
    }

    /**
     * 根据条件选择所有列的数据
     *
     * @param whereClause
     * @param whereArgs
     * @param limit
     * @return
     */
    public Cursor select(String whereClause, String[] whereArgs, String limit) {
        Cursor cursor = this.select(null, whereClause, whereArgs, null, null,
                null, limit);
        return cursor;
    }


    /**
     * 选择整个表的数据
     *
     * @return
     */
    public Cursor selectAll() {
        Cursor cursor = this.select("", null, null);
        return cursor;
    }

    public abstract List<T> parseList(Cursor cursor);
    public abstract ContentValues parseContentValues(T objcet);

}
