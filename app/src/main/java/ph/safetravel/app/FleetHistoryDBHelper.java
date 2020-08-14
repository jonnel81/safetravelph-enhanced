package ph.safetravel.app;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class FleetHistoryDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FleetHistoryDB";
    public final static String DATABASE_PATH ="/data/data/ph.safetravel.app/databases/";
    public static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "FleetRecord";
    private static final String KEY_ID = "id";
    private static final String KEY_ROUTE = "route";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CAPACITY = "capacity";
    private static final String KEY_VEHID = "vehicle_id";
    private static final String KEY_VEHDETAILS = "vehicle_details";
    private static final String KEY_TRIPDATE = "trip_date";
    private static final String[] COLUMNS = { KEY_ID, KEY_ROUTE, KEY_TYPE, KEY_CAPACITY, KEY_VEHID, KEY_VEHDETAILS, KEY_TRIPDATE };

    public FleetHistoryDBHelper(Context context) {
        super(context, DATABASE_PATH+DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE FleetRecord ( "
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "route TEXT, " + "type TEXT, " + "capacity TEXT, "
                + "vehicle_id TEXT, " + "vehicle_details TEXT, "  + "trip_date TEXT )";

        db.execSQL(CREATION_TABLE);
    } // onCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    } // onUpgrade

    public FleetRecord getFleetRecord(int id) {
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

        FleetRecord fleetRecord = new FleetRecord();
        fleetRecord.setId(Integer.parseInt(cursor.getString(0)));
        fleetRecord.setRoute(cursor.getString(1));
        fleetRecord.setType(cursor.getString(2));
        fleetRecord.setCapacity(cursor.getString(3));
        fleetRecord.setVehicleId(cursor.getString(4));
        fleetRecord.setVehicleDetails(cursor.getString(5));
        fleetRecord.setTripDate(cursor.getString(6));

        return fleetRecord;
    } // getFleetRecord

    public List<FleetRecord> allFleetRecords() {
        List<FleetRecord> fleetRecords = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        FleetRecord fleetRecord = null;

        if (cursor.moveToFirst()) {
            do {
                fleetRecord = new FleetRecord();
                fleetRecord.setId(Integer.parseInt(cursor.getString(0)));
                fleetRecord.setRoute(cursor.getString(1));
                fleetRecord.setType(cursor.getString(2));
                fleetRecord.setCapacity(cursor.getString(3));
                fleetRecord.setVehicleId(cursor.getString(4));
                fleetRecord.setVehicleDetails(cursor.getString(5));
                fleetRecord.setTripDate(cursor.getString(6));
                fleetRecords.add(fleetRecord);
            } while (cursor.moveToNext());
        }

        db.close();
        return fleetRecords;
    } // allTripRecords

    public void addTripRecord(FleetRecord fleetRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE, fleetRecord.getRoute());
        values.put(KEY_TYPE, fleetRecord.getType());
        values.put(KEY_CAPACITY, fleetRecord.getCapacity());
        values.put(KEY_VEHID, fleetRecord.getVehicleId());
        values.put(KEY_VEHDETAILS, fleetRecord.getVehicleDetails());
        values.put(KEY_TRIPDATE, fleetRecord.getTripDate());
        // insert
        db.insert(TABLE_NAME,null, values);
        db.close();
    } // addTripRecord

    public int updateFLeetRecord(FleetRecord fleetRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE, fleetRecord.getRoute());
        values.put(KEY_TYPE, fleetRecord.getType());
        values.put(KEY_CAPACITY, fleetRecord.getCapacity());
        values.put(KEY_VEHID, fleetRecord.getVehicleId());
        values.put(KEY_VEHDETAILS, fleetRecord.getVehicleDetails());
        values.put(KEY_TRIPDATE, fleetRecord.getTripDate());

        int i = db.update(TABLE_NAME,            // table
                values,                          // column/value
                "id = ?",            // selections
                new String[] { String.valueOf(fleetRecord.getId()) });

        db.close();

        return i;
    } // updateTripRecord

}