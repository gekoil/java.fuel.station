import java.util.ArrayList;

public class App {

    public static void main(String[] args) {
        ArrayList<Pump> pumps = new ArrayList<>();
        ArrayList<CarWash> washes = new ArrayList<>();

        FuelStation station = new FuelStation(washes, pumps);
        station.run();
        try {
            station.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
