import java.util.ArrayList;

public class App {

    public static void main(String[] args) {
        ArrayList<Pump> pumps = new ArrayList<>();
        ArrayList<CarWash> washes = new ArrayList<>();
        // TODO: Read the XML file provided in args variable
        FuelStation station = new FuelStation(washes, pumps, 0, 1500);
        station.run();
        try {
            station.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
