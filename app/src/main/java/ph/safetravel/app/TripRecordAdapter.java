package ph.safetravel.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class TripRecordAdapter extends RecyclerView.Adapter<TripRecordAdapter.MyViewHolder> {

    private Context context;
    private List<TripRecord> tripRecordsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView origin;
        public TextView originLat;
        public TextView originLng;
        public TextView destination;
        public TextView destinationLat;
        public TextView destinationLng;
        public TextView mode;
        public TextView purpose;
        public TextView vehicleId;
        public TextView vehicleDetails;
        public TextView tripDate;

        public MyViewHolder(View view) {
            super(view);
            vehicleId = view.findViewById(R.id.tv_VehicleId);
            origin = view.findViewById(R.id.tv_Origin);
            destination = view.findViewById(R.id.tv_Destination);
            tripDate = view.findViewById(R.id.tv_Tripdate);
        }
    }

    public TripRecordAdapter(Context context, List<TripRecord> tripRecordsList) {
        this.context = context;
        this.tripRecordsList = tripRecordsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_triprecord, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final TripRecord tripRecord = tripRecordsList.get(position);

        holder.vehicleId.setText(tripRecord.getVehicleId());
        holder.origin.setText(tripRecord.getOrigin());
        holder.destination.setText(tripRecord.getDestination());
        holder.tripDate.setText(tripRecord.getTripDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, tripRecord.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripRecordsList.size();
    }
}