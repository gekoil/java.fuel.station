import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pump extends Thread{
    private final Logger log = Logger.getLogger(Pump.class.getName());
    
    private static int pumpCount = 0;
    private String logId;
    private int id;
    private GasStation station;
    private boolean isRunning = true;
    private BlockingQueue<Car> cars;
	
    public  Pump(GasStation station) {
        id = pumpCount++;
        setStation(station);
        cars = new LinkedBlockingDeque<>();

        FileHandler theHandler;
        logId = "Pump no." + id + " ";
        try {
            theHandler = new FileHandler("pump_" + id + ".txt");
            theHandler.setFormatter(new CustomFormatter());
            theHandler.setFilter(new FilesFilter(logId));
            log.addHandler(theHandler);
            log.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        log.info(logId + "is ready to Work.");
    }

    public void setStation(GasStation station) {
        this.station = station;
    }

    public void addCar(Car car) {
        try {
            cars.put(car);
        } catch (InterruptedException e) {
            log.severe(e.toString());
        }
    }

    public void shutDown() {
        this.isRunning = false;
    }
    
    public int getWatingTime() {
    	return cars.size()*100;
    }

    @Override
    public void run() {
        while(!cars.isEmpty() || isRunning) {
            try {
                Car car = cars.take();
                int fuelRequest = car.getFuel();
                station.requestFuel(fuelRequest);
                car.addFuel(fuelRequest);
                sleep(5*fuelRequest);
                double price = station.getFuelCost()*fuelRequest;
                station.payForServise(price);
                station.addCar(car);
            } catch (InterruptedException e) {
                log.severe(e.toString());
            } catch (Exception e1) {
            	log.severe(e1.toString());
            }
        }
        log.info(logId + "is close.");
    }
}
