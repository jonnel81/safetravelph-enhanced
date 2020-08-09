package ph.safetravel.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.hsalf.smilerating.SmileRating;
import com.hsalf.smileyrating.SmileyRating;

public class TripRatingFragment extends Fragment {
    Button closeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_triprating, container, false);

        SmileRating smileRating;

        smileRating.setTitle( SmileyRating.Type.GREAT, "Awesome");
        smileRating.setFaceColor(SmileyRating.Type.GREAT, Color.BLUE);
        smileRating.setFaceBackgroundColor(SmileyRating.Type.GREAT, Color.RED);

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