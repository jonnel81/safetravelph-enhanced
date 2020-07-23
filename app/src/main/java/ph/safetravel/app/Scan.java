package ph.safetravel.app;

import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import android.os.Bundle;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scan extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                        // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() { super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        TripInfoFragment.txtVehDetails.setText(rawResult.getText());

        // Extract vehicle ID
        String str = rawResult.getText();
        Pattern pattern = Pattern.compile("Plate:(.*?)\\*\\*\\*");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            TripInfoFragment.txtVehId.setText(matcher.group(1));
        }

        //Log.v("tag", rawResult.getText()); // Prints scan results
        //Log.v("tag", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        //BarcodeReader.tvresult.setText(rawResult.getText());
        onBackPressed();
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

}
