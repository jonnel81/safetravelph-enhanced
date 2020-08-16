package ph.safetravel.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FleetRecordAdapter extends RecyclerView.Adapter<FleetRecordAdapter.MyViewHolder> {

    private Context context;
    private List<FleetRecord> fleetRecordsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView route;
        public TextView type;
        public TextView capacity;
        public TextView vehicleId;
        public TextView vehicleDetails;
        public TextView tripDate;

        public MyViewHolder(View view) {
            super(view);
            vehicleId = view.findViewById(R.id.tv_VehicleId);
            //type = view.findViewById(R.id.tv_Type);
            route = view.findViewById(R.id.tv_Route);
            tripDate = view.findViewById(R.id.tv_Tripdate);
        }
    }

    public FleetRecordAdapter(Context context, List<FleetRecord> fleetRecordsList) {
        this.context = context;
        this.fleetRecordsList = fleetRecordsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_fleetrecord, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final FleetRecord fleetRecord = fleetRecordsList.get(position);

        holder.vehicleId.setText(fleetRecord.getVehicleId());
        //holder.type.setText(fleetRecord.getType());
        holder.route.setText(fleetRecord.getRoute());
        holder.tripDate.setText(fleetRecord.getTripDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, fleetRecord.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return fleetRecordsList.size();
    }
}