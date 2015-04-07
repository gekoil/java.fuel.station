import java.util.ArrayList;

public class App {

    public static void main(String[] args) {
        ArrayList<Pump> pumps = new ArrayList<>();
        CarWash wash = new CarWash();
        // TODO: Read the XML file provided in args variable
        FuelStation station = new FuelStation(wash, pumps, 0, 1500);
        station.run();
        try {
            station.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
