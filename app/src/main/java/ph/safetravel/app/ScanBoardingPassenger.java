package ph.safetravel.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanBoardingPassenger extends AppCompatActivity implements ZXingScannerView.ResultHandler {
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

        // Decrypt the result
        String decrypted_result = "";
        try {
            decrypted_result = AESUtils.decrypt(rawResult.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Extract Boarding Passenger Details
        FleetBoardingFragment.txtBoardingPassengerDetails.setText(decrypted_result);

        // Extract Boarding Passenger Id
        String str = decrypted_result;
        Pattern pattern = Pattern.compile("Username:(.*?)\\*\\*\\*");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            FleetBoardingFragment.txtBoardingPassengerId.setText(matcher.group(1));
        }
        onBackPressed();
    }

}
