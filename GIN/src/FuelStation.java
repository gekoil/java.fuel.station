import java.util.ArrayList;
import java.util.logging.Logger;

public class FuelStation extends Thread{
    private static final Logger log = Logger.getLogger(FuelStation.class.getName());

    private ArrayList<CarWash> carWashes;
    private ArrayList<Pump> fuelPumps;
    private ArrayList<Car> incomingCars;
    private Integer fuelReserve;
    private int maxFuelCapacity;
    private boolean isWorking;

    public FuelStation(ArrayList<CarWash> carWashes, ArrayList<Pump> fuelPumps, int fuelReserve, int fuelCapacity) {
        this.carWashes = carWashes;
        this.fuelPumps = fuelPumps;
        this.fuelReserve = fuelReserve;
        this.maxFuelCapacity = fuelCapacity;
        incomingCars = new ArrayList<>();
        isWorking = true;
    }

    @Override
    public void run() {
        while(isWorking) {
            // TODO: Add looping on incoming cars until shutdown is called
        }
    }

    public ArrayList<CarWash> getCarWashes() {
        return carWashes;
    }

    public void addCarWash(CarWash carWash) {
        carWashes.add(carWash);
    }

    public ArrayList<Pump> getFuelPumps() {
        return fuelPumps;
    }

    public void addFuelPump(Pump fuelPump) {
        fuelPumps.add(fuelPump);
    }

    public void addCar(Car car) {
        incomingCars.add(car);
    }

    public boolean requestFuel(int fuelRequest) throws InterruptedException {
        synchronized(fuelReserve) {
            while(fuelReserve < maxFuelCapacity * 0.2) {
                wait();
            }
            if (fuelReserve > fuelRequest) { // Do we really need to get into this?
                fuelReserve -= fuelRequest;
                return true;
            } else {
                return false;
            }
        }
    }

    public void addFuel(int fuelRefill) {
        synchronized (fuelReserve) {
            fuelReserve += fuelRefill;
        }
        fuelReserve.notify();
    }
}
