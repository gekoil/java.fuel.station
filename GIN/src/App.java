import java.util.ArrayList;

public class App {

    public static void main(String[] args) {
        ArrayList<Pump> pumps = new ArrayList<>();
        CarWash wash = new CarWash();
        // TODO: Read the XML file provided in args variable
        GasStation station = new GasStation(wash, pumps, 0, 1500);
        for(int i = 0; i <= 6; i++)
        	station.addFuelPump(new Pump(station));
        station.start();
        try {
            station.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
