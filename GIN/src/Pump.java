import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Pump extends Thread {
    private final Logger log = Logger.getLogger(Global.PROJECT_LOG_NAME);
    private static final int SECONDS_PER_LITER = 50;

    private static int pumpCount = 0;
    private String logId;
    private int id;
    private GasStation station;
    private volatile boolean isRunning = true;
    private BlockingQueue<Car> cars;

    public  Pump() {
        id = pumpCount++;
        cars = new LinkedBlockingDeque<>();
        initLog();
        log.info(logId + "is ready to Work.");
    }
    
    private void initLog() {
    	FileHandler theHandler;
        logId = "Pump no." + id + " ";
        try {
            theHandler = new FileHandler("logs\\pump_" + id + ".txt");
            theHandler.setFormatter(new CustomFormatter());
            theHandler.setFilter(new FilesFilter(logId));
            log.addHandler(theHandler);
            log.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setStation(GasStation station) {
        this.station = station;
    }

    public void addCar(Car car) {
        try {
            cars.put(car);
            log.info(logId + "Added car " + car.getId() + " to waiting line");
        } catch (InterruptedException e) {
            log.severe(e.toString());
        }
    }

    public void shutDown() {
    	if(cars.isEmpty())
    		log.info(logId + "is closing down, handling remaining cars");
    	else
    		log.info(logId + "is closing down.");
        this.isRunning = false;
    }

    public int getWaitingTime() {
        int time = 0;
        for(Car car : cars) {
            time += car.getFuel() * SECONDS_PER_LITER;
        }
        return time;
    }

    @Override
    public void run() {
        while(isRunning || !cars.isEmpty()) {
            try {
                Car car = cars.poll();
                if(car == null)
                	continue;
                int fuelRequest = car.getFuel();
                log.info(logId + "requesting " + fuelRequest + " Liters from station");
                station.requestFuel(fuelRequest);
                car.addFuel(fuelRequest);
                sleep(SECONDS_PER_LITER * fuelRequest);
                double price = station.getFuelCost() * fuelRequest;
                station.payForFuel(price);
                log.info(logId + "got fuel for car " + car.getId() + ", now charging money");
                if(isRunning) {
                    station.addCar(car);
                } else {
                    car.leaveStation();
                }
                log.info(logId + "car " + car.getId() + "returned to station");
            } catch (Exception e) {
                log.severe(logId + e.getStackTrace());
            }
        }
        log.info(logId + "is close.");
    }
}