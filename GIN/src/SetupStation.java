import java.util.ArrayList;

public class SetupStation {

    public static void main(String[] args) {
        ArrayList<Pump> pumps = new ArrayList<>();
        CarWash wash = new CarWash();
        // TODO: Read the XML file provided in args variable
        GasStation station = new GasStation(wash, pumps, 0, 1500);
        station.start();
        try {
        	station.setWorking(false);
            station.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
