package ph.safetravel.app;

public class TripRecord {
    int id;
    String origin;
    String originLat;
    String originLng;
    String destination;
    String destinationLat;
    String destinationLng;
    String mode;
    String purpose;
    String vehicleId;
    String vehicleDetails;
    String tripDate;

    public TripRecord() {

    }

    public TripRecord(int id, String origin, String originLat, String originLng, String destination, String destinationLat, String destinationLng,
                      String mode, String purpose, String vehicleId, String vehicleDetails, String tripDate) {
        this.id = id;
        this.origin = origin;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destination = destination;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.mode = mode;
        this.purpose = purpose;
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

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginLat() {
        return originLat;
    }

    public void setOriginLat(String originLat) {
        this.originLat = originLat;
    }

    public String getOriginLng() {
        return originLat;
    }

    public void setOriginLng(String originLng) {
        this.originLng = originLng;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(String destinationLat) {
        this.destinationLat = destinationLat;
    }

    public String getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(String destinationLng) {
        this.destinationLng = destinationLng;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
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
        this.vehicleId = vehicleDetails;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    @Override
    public String toString() {
        return "TripRecord{" +
                "id=" + id +
                ", origin='" + origin + '\'' +
                ", originLat='" + originLat + '\'' +
                ", originLng='" + originLng + '\'' +
                ", destination='" + destination + '\'' +
                ", destinationLat='" + destinationLat + '\'' +
                ", destinationLng='" + destinationLng + '\'' +
                ", mode='" + mode + '\'' +
                ", purpose='" + purpose + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", vehicleDetails='" + vehicleDetails + '\'' +
                ", dateTrip='" + tripDate + '\'' +
                '}';
    }

}
