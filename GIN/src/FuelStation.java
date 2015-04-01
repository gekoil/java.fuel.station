import java.util.ArrayList;
import java.util.logging.Logger;

public class FuelStation extends Thread{
    private static final Logger log = Logger.getLogger(FuelStation.class.getName());

    private ArrayList<CarWash> carWashes;
    private ArrayList<Pump> fuelPumps;
    private ArrayList<Car> incomingCars;

    public FuelStation(ArrayList<CarWash> carWashes, ArrayList<Pump> fuelPumps) {
        this.carWashes = carWashes;
        this.fuelPumps = fuelPumps;
        incomingCars = new ArrayList<>();
    }

    @Override
    public void run() {

    }

    public ArrayList<CarWash> getCarWashes() {
        return carWashes;
    }

    public void addCarWash(CarWash carWash) {
        this.carWashes.add(carWash);
    }

    public ArrayList<Pump> getFuelPumps() {
        return fuelPumps;
    }

    public void addFuelPump(Pump fuelPump) {
        this.fuelPumps.add(fuelPump);
    }

    public void addCar(Car car) {
        this.incomingCars.add(car);
    }
}
