package ph.safetravel.app;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TripHistoryDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TripHistoryDB";
    public final static String DATABASE_PATH ="/data/data/ph.safetravel.app/databases/";
    public static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "TripRecord";
    private static final String KEY_ID = "id";
    private static final String KEY_ORIG = "origin";
    private static final String KEY_ORIGLAT = "origin_lat";
    private static final String KEY_ORIGLNG = "origin_lng";
    private static final String KEY_DEST = "destination";
    private static final String KEY_DESTLAT = "destination_lat";
    private static final String KEY_DESTLNG = "destination_lng";
    private static final String KEY_MODE = "mode";
    private static final String KEY_PURPOSE = "purpose";
    private static final String KEY_VEHID = "vehicle_id";
    private static final String KEY_VEHDETAILS = "vehicle_details";
    private static final String KEY_TRIPDATE = "trip_date";
    private static final String[] COLUMNS = { KEY_ID, KEY_ORIG , KEY_ORIGLAT, KEY_ORIGLNG, KEY_DEST, KEY_DESTLAT, KEY_DESTLNG,
            KEY_MODE, KEY_PURPOSE, KEY_VEHID, KEY_VEHDETAILS, KEY_TRIPDATE };

    public TripHistoryDBHelper(Context context) {
        super(context, DATABASE_PATH+DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE TripRecord ( "
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "origin TEXT, " + "origin_lat TEXT, " + "origin_lng TEXT, "
                + "destination TEXT, " + "destination_lat TEXT, " + "destination_lng TEXT, " + "mode TEXT, " + "purpose TEXT, "
                + "vehicle_id TEXT, " + "vehicle_details TEXT, "  + "trip_date TEXT )";

        db.execSQL(CREATION_TABLE);
    } // onCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    } // onUpgrade

    public TripRecord getTripRecord(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,            // a. table
                COLUMNS,                                // b. column names
                " id = ?",                     // c. selections
                new String[] { String.valueOf(id) },    // d. selections args
                null,                          // e. group by
                null,                           // f. having
                null,                          // g. order by
                null);                            // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        TripRecord tripRecord = new TripRecord();
        tripRecord.setId(Integer.parseInt(cursor.getString(0)));
        tripRecord.setOrigin(cursor.getString(1));
        tripRecord.setOriginLat(cursor.getString(2));
        tripRecord.setOriginLng(cursor.getString(3));
        tripRecord.setDestination(cursor.getString(4));
        tripRecord.setDestinationLat(cursor.getString(5));
        tripRecord.setDestinationLng(cursor.getString(6));
        tripRecord.setMode(cursor.getString(7));
        tripRecord.setPurpose(cursor.getString(8));
        tripRecord.setVehicleId(cursor.getString(9));
        tripRecord.setVehicleDetails(cursor.getString(10));
        tripRecord.setTripDate(cursor.getString(11));

        return tripRecord;
    } // getTripRecord

    public List<TripRecord> allTripRecords() {
        //List<TripRecord> tripRecords = new LinkedList<TripRecord>();
        List<TripRecord> tripRecords = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        TripRecord tripRecord = null;

        if (cursor.moveToFirst()) {
            do {
                tripRecord = new TripRecord();
                tripRecord.setId(Integer.parseInt(cursor.getString(0)));
                tripRecord.setOrigin(cursor.getString(1));
                tripRecord.setOriginLat(cursor.getString(2));
                tripRecord.setOriginLng(cursor.getString(3));
                tripRecord.setDestination(cursor.getString(4));
                tripRecord.setDestinationLat(cursor.getString(5));
                tripRecord.setDestinationLng(cursor.getString(6));
                tripRecord.setMode(cursor.getString(7));
                tripRecord.setPurpose(cursor.getString(8));
                tripRecord.setVehicleId(cursor.getString(9));
                tripRecord.setVehicleDetails(cursor.getString(10));
                tripRecord.setTripDate(cursor.getString(11));
                tripRecords.add(tripRecord);
            } while (cursor.moveToNext());
        }

        db.close();
        return tripRecords;
    } // allTripRecords

    public void addTripRecord(TripRecord tripRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ORIG, tripRecord.getOrigin());
        values.put(KEY_ORIGLAT, tripRecord.getOriginLat());
        values.put(KEY_ORIGLNG, tripRecord.getOriginLng());
        values.put(KEY_DEST, tripRecord.getDestination());
        values.put(KEY_DESTLAT, tripRecord.getDestinationLat());
        values.put(KEY_DESTLNG, tripRecord.getDestinationLng());
        values.put(KEY_MODE, tripRecord.getMode());
        values.put(KEY_PURPOSE, tripRecord.getPurpose());
        values.put(KEY_VEHID, tripRecord.getVehicleId());
        values.put(KEY_VEHDETAILS, tripRecord.getVehicleDetails());
        values.put(KEY_TRIPDATE, tripRecord.getTripDate());
        // insert
        db.insert(TABLE_NAME,null, values);
        db.close();
    } // addTripRecord

    public int updateTripRecord(TripRecord tripRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ORIG, tripRecord.getOrigin());
        values.put(KEY_ORIGLAT, tripRecord.getOriginLat());
        values.put(KEY_ORIGLNG, tripRecord.getOriginLng());
        values.put(KEY_DEST, tripRecord.getDestination());
        values.put(KEY_DESTLAT, tripRecord.getDestinationLat());
        values.put(KEY_DESTLNG, tripRecord.getDestinationLng());
        values.put(KEY_MODE, tripRecord.getMode());
        values.put(KEY_PURPOSE, tripRecord.getPurpose());
        values.put(KEY_VEHID, tripRecord.getVehicleId());
        values.put(KEY_VEHDETAILS, tripRecord.getVehicleDetails());
        values.put(KEY_TRIPDATE, tripRecord.getTripDate());

        int i = db.update(TABLE_NAME,            // table
                values,                          // column/value
                "id = ?",            // selections
                new String[] { String.valueOf(tripRecord.getId()) });

        db.close();

        return i;
    } // updateTripRecord

}