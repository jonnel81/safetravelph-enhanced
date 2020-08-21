package ph.safetravel.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRCode extends AppCompatActivity {
    SharedPreferences myPrefs;
    private Toolbar toolbar;
    public final static int QRcodeWidth = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        // Tollbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Get shared preferences
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        String encrypted_username = myPrefs.getString("username", null);
        String username = "";
        // Decrypt username
        try {
            username = AESUtils.decrypt(encrypted_username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView userQRCode = (TextView) findViewById(R.id.userQRCode);
        userQRCode.setText(username);

        // Generate QR code
        username = "nctiglao";
        String firstname = "Noriel";
        String lastname = "Tiglao";
        String contactnumber = "123-4567";

        StringBuilder qrString = new StringBuilder();
        qrString.append("Username:" + username);
        qrString.append("***Firstname:" + firstname);
        qrString.append("***Lastname:" + lastname);
        qrString.append("***Contactnumber:" + contactnumber);
        String forEncryption = qrString.toString();

        //String qrString = "Username:nctiglao***Firstname:Noriel***Lastname:Tiglao***Contactnumber:123-4567";
        String encrypted = "";
        try {
            encrypted = AESUtils.encrypt(forEncryption);
            Log.d("Encrypt", encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView imageQRCode = (ImageView) findViewById(R.id.imageQRCode);
        try {
            Bitmap bitmap = encodeAsBitmap(encrypted);
            imageQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    } // OnCreate

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private Bitmap encodeAsBitmap(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(Value, BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null);

        } catch (IllegalArgumentException Illegalargumentexception) {
            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, QRcodeWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    } // encodeAsBitmap

}
