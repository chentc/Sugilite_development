package edu.cmu.hcii.sugilite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.cmu.hcii.sugilite.dao.db.SugiliteScriptDbContract;
import edu.cmu.hcii.sugilite.dao.db.SugiliteScriptDbHelper;
import edu.cmu.hcii.sugilite.model.block.SugiliteBlock;
import edu.cmu.hcii.sugilite.model.block.SugiliteStartingBlock;

/**
 * @author toby
 * @date 6/15/16
 * @time 4:04 PM
 */
public class SugiliteScriptDao {
    private SugiliteScriptDbHelper sugiliteScriptDbHelper;
    private Gson gson = new Gson();

    public SugiliteScriptDao(Context context){
        sugiliteScriptDbHelper = new SugiliteScriptDbHelper(context);
    }

    /**
     * save sugiliteBlock into the db (note: no duplicated script name allowed, new ones will replace old ones with the same name)
     * @param sugiliteBlock
     * @return row id
     * @throws Exception
     */
    public long save(SugiliteStartingBlock sugiliteBlock) throws Exception{
        SQLiteDatabase db = sugiliteScriptDbHelper.getWritableDatabase();
        Calendar c = Calendar.getInstance();
        ContentValues values = new ContentValues();
        if(sugiliteBlock == null || sugiliteBlock.getScriptName() == null){
            throw new Exception("null block");
        }
        delete(sugiliteBlock.getScriptName());
        values.put(SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_NAME, sugiliteBlock.getScriptName());
        values.put(SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_BODY, SerializationUtils.serialize(sugiliteBlock));
        values.put(SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_ADDED_TIME, c.getTimeInMillis());
        long newRowId;
        newRowId = db.insert(
                SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME,
                null,
                values);
        db.close();
        return newRowId;
    }

    /**
     *
     * @return # of rows in DB
     */
    public long size(){
        SQLiteDatabase db = sugiliteScriptDbHelper.getReadableDatabase();
        SQLiteStatement statement = db.compileStatement("select count (*) from " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME + ";");
        return statement.simpleQueryForLong();    }

    /**
     *
     * @return path of the ".db" file
     */
    public String getPath(){
        SQLiteDatabase db = sugiliteScriptDbHelper.getReadableDatabase();
        return db.getPath();
    }

    /**
     *
     * @param key
     * @return the script with name = key, null if there's no such script
     */
    public SugiliteStartingBlock read(String key){
        SQLiteDatabase db = sugiliteScriptDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME + " WHERE " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_NAME + " = \'" + key + "\';", null);
        if(cursor.getCount() == 0)
            return null;
        cursor.moveToFirst();
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_BODY));
        SugiliteStartingBlock block = (SugiliteStartingBlock)SerializationUtils.deserialize(blob);
        return block;
    }

    public SugiliteStartingBlock read(long id){
        SQLiteDatabase db = sugiliteScriptDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME + " WHERE " + SugiliteScriptDbContract.SugiliteScriptRecordEntry._ID + " = \'" + id + "\';", null);
        if(cursor.getCount() == 0)
            return null;
        cursor.moveToFirst();
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_BODY));
        SugiliteStartingBlock block = (SugiliteStartingBlock)SerializationUtils.deserialize(blob);
        return block;
    }

    /**
     * Delete the row with script name = key from DB
     * @param key
     * @return the number of rows deleted
     */
    public int delete(String key){
        SQLiteDatabase db = sugiliteScriptDbHelper.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement("DELETE FROM " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME + " WHERE " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_NAME + " = \'" + key + "\';");
        return statement.executeUpdateDelete();
    }

    /**
     * Clear the DB
     * @return the number of rows deleted
     */

    public int clear(){
        SQLiteDatabase db = sugiliteScriptDbHelper.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement("DELETE FROM " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME + ";");
        return statement.executeUpdateDelete();
    }

    /**
     *
     * @return the list of all script names in DB
     */
    public List<String> getAllNames(){
        SQLiteDatabase db = sugiliteScriptDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME + ";", null);
        cursor.moveToFirst();
        List<String> names = new ArrayList<>();
        while (!cursor.isAfterLast()){
            String name = cursor.getString(cursor.getColumnIndex(SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_NAME));
            names.add(name);
            cursor.moveToNext();
        }
        return names;
    }

    /**
     *
     * @return the list of all scripts in DB
     */
    public List<SugiliteStartingBlock> getAllScripts(){
        SQLiteDatabase db = sugiliteScriptDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SugiliteScriptDbContract.SugiliteScriptRecordEntry.TABLE_NAME + ";", null);
        cursor.moveToFirst();
        List<SugiliteStartingBlock> scripts = new ArrayList<>();
        while (!cursor.isAfterLast()){
            byte[] blob = cursor.getBlob(cursor.getColumnIndex(SugiliteScriptDbContract.SugiliteScriptRecordEntry.COLUMN_NAME_SCRIPT_BODY));
            SugiliteStartingBlock block = (SugiliteStartingBlock)SerializationUtils.deserialize(blob);
            scripts.add(block);
            cursor.moveToNext();
        }
        return scripts;
    }





}