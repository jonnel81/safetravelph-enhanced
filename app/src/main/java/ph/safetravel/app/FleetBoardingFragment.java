package ph.safetravel.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


public class FleetBoardingFragment extends Fragment {
    SharedPreferences myPrefs;
    Button closeButton;
    Button sendButton;
    ImageView imageBoardingStatus;
    ImageButton scanButton;
    TextView txtBoardingStatus;
    public static TextView txtCommuterDetails;
    public static TextView txtCommuterId;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fleetboarding, container, false);

        // Initialize screen
        imageBoardingStatus = (ImageView) view.findViewById(R.id.imageBoardingStatus);
        imageBoardingStatus.setVisibility(View.INVISIBLE);

        txtBoardingStatus = (TextView) view.findViewById(R.id.txtBoardingStatus);
        txtBoardingStatus.setVisibility(View.INVISIBLE);

        txtCommuterId = (TextView) view.findViewById(R.id.txtCommuterId);
        txtCommuterDetails = (TextView) view.findViewById(R.id.txtCommuterDetails);

        // Scan button
        //sendButton = (Button) view.findViewById(R.id.btnSend);
        //View.OnClickListener sendClickListener = new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        int secs = 5; // Delay in seconds
//
        //        Utils.delay(secs, new Utils.DelayCallback() {
        //            @Override
        //            public void afterDelay() {
        //                // Do something after delay
        //                Toast.makeText(getContext(), "Sending message", Toast.LENGTH_SHORT).show();
        //                imageBoardingStatus.setVisibility(View.VISIBLE);
        //                imageBoardingStatus.setImageResource(R.drawable.sign_correct);
        //            }
        //        });
        //    }
        //};
        //sendButton.setOnClickListener(sendClickListener);

        // Scan results
        scanButton = (ImageButton) view.findViewById(R.id.btnScan);
        View.OnClickListener scanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ScanPassenger.class));
                int secs = 3; // Delay in seconds

                Utils.delay(secs, new Utils.DelayCallback() {
                    @Override
                    public void afterDelay() {
                        // Do something after delay
                        // Check if broker is connected
                        if(((Fleet) getActivity()).connected()) {
                            ((Fleet) getActivity()).sendBoarding();
                            //Toast.makeText(getContext(), "Sending message", Toast.LENGTH_SHORT).show();
                            //imageBoardingStatus.setVisibility(View.VISIBLE);
                            //imageBoardingStatus.setImageResource(R.drawable.sign_correct);
                        }
                        // Clear screen
                        txtCommuterId.setText("");
                        txtCommuterDetails.setText("");
                    }
                });
            }
        };
        scanButton.setOnClickListener(scanClickListener);

        // Close button
        closeButton = (Button) view.findViewById(R.id.btnClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close fragment view
                container.removeView(view);
                container.setVisibility(View.GONE);
                // Restore Fab
                ((Fleet) getActivity()).restoreFleetFab();
            }
        });

        // Get shared preferences
        //myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        //String username = myPrefs.getString("username", "");
        //String userId = "";
        //// Decrypt username
        //try {
        //    String decrypted_username = AESUtils.decrypt(username);
        //    userId = decrypted_username;
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        //txtUserId.setText(myPrefs.getString("vehicleId",""));
        //txtUserDetails.setText(myPrefs.getString("vehicleDetails",""));

        return view;

    } // onCreateView

}