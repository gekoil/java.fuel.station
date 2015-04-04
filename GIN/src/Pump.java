import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pump extends Thread{
    private static final Logger log = Logger.getLogger(Pump.class.getName());
    private static int pumpCount;

    private int id;
    private FuelStation station;
    private boolean isRunning;
    private BlockingQueue<Car> cars;
	
    public  Pump(FuelStation station) {
        id = pumpCount++;
        setStation(station);
        cars = new LinkedBlockingDeque<>();

        FileHandler theHandler;
        try {
            theHandler = new FileHandler("pump_" + id + ".txt");
            theHandler.setFormatter(new CustomFormatter());
            log.addHandler(theHandler);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setStation(FuelStation station) {
        this.station = station;
    }

    public void addCar(Car car) {
        try {
            cars.put(car);
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, e.toString());
        }
    }

    public void shutDown() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        // TODO: Add execution here
        isRunning = true;
        while(isRunning) {
            try {
                Car car = cars.take();
                int fuelRequest = car.getFuel();
                station.requestFuel(fuelRequest);
                car.setNeedFuel(false);
                car.addFuel(fuelRequest);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, e.toString());
            }
        }
    }
}
