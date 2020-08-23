package ph.safetravel.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class FleetAlightingFragment extends Fragment {
    Button closeButton;
    ImageView imageAlightingStatus;
    ImageButton scanButtonAlightingPassenger;
    TextView txtAlightingStatus;
    public static TextView txtAlightingPassengerDetails;
    public static TextView txtAlightingPassengerId;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fleetalighting, container, false);

        // Initialize screen
        imageAlightingStatus = (ImageView) view.findViewById(R.id.imageAlightingStatus);
        imageAlightingStatus.setVisibility(View.INVISIBLE);

        txtAlightingStatus = (TextView) view.findViewById(R.id.txtAlightingStatus);
        txtAlightingStatus.setVisibility(View.INVISIBLE);

        txtAlightingPassengerId = (TextView) view.findViewById(R.id.txtAlightingPassengerId);
        txtAlightingPassengerDetails = (TextView) view.findViewById(R.id.txtAlightingPassengerDetails);

        // Scan results
        scanButtonAlightingPassenger = (ImageButton) view.findViewById(R.id.btnScanAlightingPassenger);
        View.OnClickListener scanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtAlightingPassengerId.setText("");
                txtAlightingPassengerDetails.setText("");

                startActivity(new Intent(getContext(), ScanAlightingPassenger.class));
                int secs = 2; // Delay in seconds
                Utils.delay(secs, new Utils.DelayCallback() {
                    @Override
                    public void afterDelay() {
                        if(((Fleet) getActivity()).connected()) {
                            ((Fleet) getActivity()).sendBoarding();
                        }
                        // Clear screen
                        txtAlightingPassengerId.setText("");
                        txtAlightingPassengerDetails.setText("");
                    }
                });
            }
        };
        scanButtonAlightingPassenger.setOnClickListener(scanClickListener);

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

        return view;

    } // onCreateView

}