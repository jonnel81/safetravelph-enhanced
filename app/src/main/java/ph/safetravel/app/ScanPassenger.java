package ph.safetravel.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanPassenger extends AppCompatActivity implements ZXingScannerView.ResultHandler {
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

        FleetPassengerFragment.txtUserDetails.setText(rawResult.getText());

        // Extract User Id
        String str = rawResult.getText();
        Pattern pattern = Pattern.compile("Username:(.*?)\\*\\*\\*");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            FleetPassengerFragment.txtUserId.setText(matcher.group(1));
            //Log.d("Veh Id", matcher.group(1));
        }
        onBackPressed();
    }

}
