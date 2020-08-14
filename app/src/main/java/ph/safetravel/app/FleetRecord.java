package ph.safetravel.app;

public class FleetRecord {
    int id;
    String route;
    String type;
    String capacity;
    String vehicleId;
    String vehicleDetails;
    String tripDate;

    public FleetRecord() {

    }

    public FleetRecord(int id, String route, String type, String capacity, String vehicleId, String vehicleDetails, String tripDate) {
        this.id = id;
        this.route = route;
        this.type = type;
        this.capacity = capacity;
        this.vehicleId = vehicleId;
        this.vehicleDetails = vehicleDetails;
        this.tripDate = tripDate;
    }

    public int getId() {
        return id;
    }

    public int setId(int id) {
        id++;
        this.id = id;
        return id;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

   public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(String vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    @Override
    public String toString() {
        return "FleetRecord{" +
                "id=" + id +
                ", route='" + route + '\'' +
                ", type='" + type + '\'' +
                ", capacity='" + capacity + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", vehicleDetails='" + vehicleDetails + '\'' +
                ", dateTrip='" + tripDate + '\'' +
                '}';
    }

}
