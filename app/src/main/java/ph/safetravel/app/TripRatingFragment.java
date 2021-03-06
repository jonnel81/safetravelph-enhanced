package ph.safetravel.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;

public class TripRatingFragment extends Fragment {
    Button closeButton;
    Button sendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_triprating, container, false);

        // SmileyRating settings
        final SmileRating smileyRating_vehiclecondtion = view.findViewById(R.id.rating_vehiclecondition);
        final SmileRating smileyRating_ridecomfort = view.findViewById(R.id.rating_ridecomfort);
        final SmileRating smileyRating_serviceadequacy = view.findViewById(R.id.rating_serviceadequacy);
        final SmileRating smileyRating_stopaccessibility = view.findViewById(R.id.rating_stopaccessibility);
        final SmileRating smileyRating_infoprovision = view.findViewById(R.id.rating_infoprovision);
        final SmileRating smileyRating_serviceavailability = view.findViewById(R.id.rating_serviceavailability);
        final SmileRating smileyRating_routeconnectivity = view.findViewById(R.id.rating_routeconnectivity);
        final SmileRating smileyRating_overall = view.findViewById(R.id.rating_overall);

        //smileyRating_vehiclecondtion.setAngryColor(Color.RED);
        //smileyRating_vehiclecondtion.setDrawingColor(Color.WHITE);

        //smileyRating_vehiclecondtion.setOnSmileySelectionListener(new SmileRating.OnSmileySelectionListener() {
        //    @Override
        //    public void onSmileySelected(@BaseRating.Smiley int smiley, boolean reselected) {
        //        // Retrieve the value of the bar dinamically
        //        // level is from 1 to 5
        //        // Will return 0 if NONE selected
        //        int level = smileyRating_vehiclecondtion.getRating();
        //        // reselected is false when user selects different smiley that previously selected one
        //        // true when the same smiley is selected.
        //        // Except if it first time, then the value will be false.
        //        switch (smiley) {
        //            case SmileRating.BAD:
        //                Toast.makeText(getContext(), "Bad.", Toast.LENGTH_SHORT).show();
        //                //Log.i(TAG, "Bad");
        //                break;
        //            case SmileRating.GOOD:
        //                Toast.makeText(getContext(), "Good.", Toast.LENGTH_SHORT).show();
        //                //Log.i(TAG, "Good");
        //                break;
        //            case SmileRating.GREAT:
        //                Toast.makeText(getContext(), "Great.", Toast.LENGTH_SHORT).show();
        //                //Log.i(TAG, "Great");
        //                break;
        //            case SmileRating.OKAY:
        //                Toast.makeText(getContext(), "Okay.", Toast.LENGTH_SHORT).show();
        //                //Log.i(TAG, "Okay");
        //                break;
        //            case SmileRating.TERRIBLE:
        //                Toast.makeText(getContext(), "Terrible.", Toast.LENGTH_SHORT).show();
        //                //Log.i(TAG, "Terrible");
        //                break;
        //        }
        //    }
        //});

        // Send rating
        sendButton = (Button) view.findViewById(R.id.btnSendRating);
        View.OnClickListener scanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if broker is connected
                if(((Trip) getActivity()).connected()) {
                    ((Trip) getActivity()).sendRating();
                }
            }
        };
        sendButton.setOnClickListener(scanClickListener);

        // Close button
        closeButton = (Button) view.findViewById(R.id.btnClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close fragment view
                container.removeView(view);
                container.setVisibility(View.GONE);
                // Restore Fab
                ((Trip) getActivity()).restoreFab();
            }
        });

        return view;

    } // onCreateView

}