package ph.safetravel.app;


import android.content.Context;
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

public class DBHelper extends SQLiteOpenHelper {
    private SQLiteDatabase myDataBase;
    private final Context myContext;
    private static final String DB_NAME = "safetravelph.db";
    public final static String DB_PATH ="/data/data/ph.safetravel.app/databases/";
    public static final int DB_VERSION = 1;

    // Constructor
    public DBHelper(Context context) {
        super(context, DB_NAME,null, DB_VERSION);
        this.myContext=context;
        boolean dbexist = checkdatabase();
        if (dbexist) {
            System.out.println("Database exists");
            opendatabase();
        } else {
            System.out.println("Database doesn't exist");
            createdatabase();
        }
    }

    public void createdatabase() {
        boolean dbExist = checkdatabase();
        if(dbExist) {
            System.out.println(" Database exists.");
        } else {
            this.getReadableDatabase();
            try {
                copydatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkdatabase() {
        // Check the database
        boolean checkDb = false;
        try {
            String myPath = DB_PATH + DB_NAME;
            File dbfile = new File(myPath);
            checkDb = dbfile.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkDb;
    }

    private void copydatabase() throws IOException {
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0)
        {
            myOutput.write(buffer, 0, length);
        }

        myInput.close();
        myOutput.flush();
        myOutput.close();
        Log.v("Database", "Database copied.");
    }

    public void opendatabase() throws SQLException {
        // Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void close() {
        if(myDataBase != null) {
            myDataBase.close();
        }
        super.close();
    }

    // Delete database
    public void databasedelete()
    {
        File file = new File(DB_PATH + DB_NAME);
        if(file.exists())
        {
            file.delete();
            System.out.println("Delete database file.");
        }
    }

    public synchronized void closedataBase()throws SQLException
    {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    public void onCreate(SQLiteDatabase db) {
        createdatabase();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (newVersion > oldVersion)
        {
            Log.v("Database upgrade", "Database version higher than old.");
            databasedelete();
        }
    }

}