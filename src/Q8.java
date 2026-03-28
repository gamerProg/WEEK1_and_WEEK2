import java.util.*;

class ParkingSpot {
    String license;
    long entryTime;
    Status2 status;

    ParkingSpot() {
        status = Status2.EMPTY;
    }
}

enum Status2 {
    EMPTY,
    OCCUPIED,
    DELETED
}

public class Q8 {

    static class ParkingSystem {

        private ParkingSpot[] table;
        private int capacity;
        private int occupied;
        private Map<String,Integer> vehicleIndex;
        private int totalProbes;
        private Map<Integer,Integer> hourlyCount;

        public ParkingSystem(int size) {
            capacity = size;
            table = new ParkingSpot[size];
            for(int i=0;i<size;i++) table[i] = new ParkingSpot();
            vehicleIndex = new HashMap<>();
            hourlyCount = new HashMap<>();
        }

        private int hash(String plate) {
            int h = 0;
            for(char c : plate.toCharArray()) h = 31*h + c;
            return Math.abs(h) % capacity;
        }

        public int parkVehicle(String plate) {
            int index = hash(plate);
            int probes = 0;

            while(table[index].status == Status2.OCCUPIED) {
                index = (index + 1) % capacity;
                probes++;
            }

            table[index].license = plate;
            table[index].entryTime = System.currentTimeMillis();
            table[index].status = Status2.OCCUPIED;

            vehicleIndex.put(plate,index);

            occupied++;
            totalProbes += probes;

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            hourlyCount.put(hour,hourlyCount.getOrDefault(hour,0)+1);

            return index;
        }

        public double exitVehicle(String plate) {
            if(!vehicleIndex.containsKey(plate)) return -1;

            int index = vehicleIndex.get(plate);
            ParkingSpot spot = table[index];

            long duration = System.currentTimeMillis() - spot.entryTime;
            double hours = duration / (1000.0 * 60 * 60);

            double fee = hours * 5;

            spot.status = Status2.DELETED;
            vehicleIndex.remove(plate);
            occupied--;

            return fee;
        }

        public int findNearestSpot() {
            for(int i=0;i<capacity;i++) {
                if(table[i].status != Status2.OCCUPIED) return i;
            }
            return -1;
        }

        public Map<String,Object> getStatistics() {
            Map<String,Object> stats = new HashMap<>();

            double occupancy = (occupied * 100.0) / capacity;
            double avgProbes = occupied == 0 ? 0 : (double)totalProbes / occupied;

            int peakHour = -1;
            int max = 0;

            for(Map.Entry<Integer,Integer> e : hourlyCount.entrySet()) {
                if(e.getValue() > max) {
                    max = e.getValue();
                    peakHour = e.getKey();
                }
            }

            stats.put("occupancy", occupancy);
            stats.put("avgProbes", avgProbes);
            stats.put("peakHour", peakHour);

            return stats;
        }
    }

    public static void main(String[] args) {

        ParkingSystem ps = new ParkingSystem(500);

        int s1 = ps.parkVehicle("ABC-1234");
        int s2 = ps.parkVehicle("ABC-1235");
        int s3 = ps.parkVehicle("XYZ-9999");

        System.out.println("Spot: " + s1);
        System.out.println("Spot: " + s2);
        System.out.println("Spot: " + s3);

        double fee = ps.exitVehicle("ABC-1234");
        System.out.println("Fee: " + fee);

        System.out.println(ps.getStatistics());
    }
}