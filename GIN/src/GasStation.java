import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GasStation extends Thread {
    private static final Logger log = Logger.getLogger(Global.PROJECT_LOG_NAME);
    private static int stationCount = 0;

    private CarWash carWash;
    private ArrayList<Pump> fuelPumps;
    private BlockingQueue<Car> incomingCars;
    private Integer fuelReserve;
    private int id;
    private int maxFuelCapacity;
    private int workingPumps;
    private int carsWashed;
    private int carsFueled;
    private Double fuelProfits;
    private Double washProfits;
    private boolean payingForWash;
    private boolean payingForFuel;
    private boolean isWorking;
    private double fuelCost;
    private String logId;

    public GasStation(CarWash carWash, ArrayList<Pump> fuelPumps, int fuelReserve, int fuelCapacity, double fuelCost) {
        this.id = stationCount++;
        this.carWash = carWash;
        carWash.setStation(this);
        this.fuelPumps = fuelPumps;
        for(Pump pump : this.fuelPumps) {
            pump.setStation(this);
        }
        this.fuelReserve = fuelReserve;
        this.maxFuelCapacity = fuelCapacity;
        incomingCars = new LinkedBlockingDeque<Car>();
        isWorking = true;
        this.fuelCost = fuelCost;
        fuelProfits = 0.0;
        washProfits = 0.0;
        payingForWash = false;
        payingForFuel = false;
        initLog();
    }

    @Override
    public void run() {
        log.info("The Station is open.");
        carWash.start();
        for(Pump p : fuelPumps)
            p.start();
        while(isWorking || !incomingCars.isEmpty()) {
            try {
				Car next = incomingCars.take();
				if(next != null)
	            	organizer(next);
			} catch (InterruptedException e) {
				log.info(e.toString());
			}
        }
        log.info("Start a closing procedure.");
        try {
            carWash.join();
            for(Pump p : fuelPumps) {
                p.shutDown();
            }
            while (workingPumps > 0) {
                wait();
            }
            for(Pump p : fuelPumps) {
                p.join();
            }
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        }
        getStats();
    }

    private void initLog() {
        FileHandler handler;
        try {
            handler = new FileHandler("logs\\gasStation_" + id + ".txt");
            handler.setFormatter(new CustomFormatter());
            log.addHandler(handler);
            log.setUseParentHandlers(false);
            logId = "Gas Station " + id + ":";
        } catch (SecurityException | IOException e) {
            e.getMessage();
        }
    }

    public Report getStats() {
        log.info(logId + "Statistics report:" +
                "\nTotal cars washed: " + carsWashed +
                "\nThe profits from car washing are: " + washProfits +
                "\nTotal cars fueled: " + carsFueled +
                "\nThe profits from fueling are: " + fuelProfits);
        return new Report(washProfits, fuelProfits, carsWashed, carsFueled);
    }

    private void organizer(Car next) {
        if(!next.isNeedFuel() && !next.isNeedWash()) {
            next.leaveStation();
        } else if(next.isNeedFuel() && !next.isNeedWash()) {
            fuelPumps.get(next.getPumpNumber()).addCar(next);
            carsFueled++;
        } else if(next.isNeedWash() && !next.isNeedFuel()) {
            carWash.addCar(next);
            carsWashed++;
        } else if(next.isNeedFuel() && next.isNeedWash())
            if(carWash.getWaitingTime() < fuelPumps.get(next.getPumpNumber()).getWaitingTime()) {
                carWash.addCar(next);
                carsWashed++;
            } else {
                fuelPumps.get(next.getPumpNumber()).addCar(next);
                carsFueled++;
            }
    }

    public void payForWash(double money) {
        synchronized (washProfits) {
            while (payingForWash) {
                try {
                    washProfits.wait();
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
            payingForWash = true;
            washProfits += money;
            payingForWash = false;
            washProfits.notifyAll();
        }
    }

    public void payForFuel(double money) {
        synchronized (fuelProfits) {
            while (payingForFuel) {
                try {
                    fuelProfits.wait();
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
            payingForFuel = true;
            fuelProfits += money;
            payingForFuel = false;
            fuelProfits.notifyAll();
        }
    }

    public CarWash getCarWash() {
        return carWash;
    }

    public ArrayList<Pump> getFuelPumps() {  // I don't think its a good design to let someone out side the class
        return fuelPumps;					// to touch the fuelPumps.
    }

    public void addFuelPump(Pump fuelPump) {
        fuelPump.setStation(this);
        fuelPumps.add(fuelPump);
    }

    public void addCar(Car car) throws Exception {
        if(isWorking)
            incomingCars.put(car);
        else
            throw new Exception("Cant add more cars today.");
    }

    public double getFuelCost() {
        return fuelCost;
    }

    public void setFuelCost(double fuelCost) {
        this.fuelCost = fuelCost;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
        carWash.setEndOfDay();
        for(Pump p : fuelPumps)
            p.shutDown();
    }

    public boolean requestFuel(int fuelRequest) {
    	synchronized(fuelReserve) {
            if (fuelReserve > fuelRequest) {
                fuelReserve -= fuelRequest;
                log.info("The fule reserve have now: " + fuelReserve + " liters.");
                if(fuelReserve < maxFuelCapacity * 0.2) {
                    log.severe(logId + "Fuel reserve is running low, must refill");
                    while(fuelReserve < maxFuelCapacity * 0.2) {
                        try {
                            fuelReserve.wait();
                        } catch (InterruptedException e) {
                            log.severe(e.getMessage());
                        }
                    }
                }
                fuelReserve.notifyAll();
                return true;
            } else {
                return false;
            }
        }
    }

    public void addFuel(int fuel) {
        synchronized (fuelReserve) {
            if (fuel <= (maxFuelCapacity - fuelReserve)) {
                try {
                    sleep(200 * fuel);
                    fuelReserve += fuel;
                } catch (InterruptedException e) {
                    log.severe(e.getMessage());
                } finally {
                    log.info(logId + "The fuel reserve capacity is now " + fuelReserve + " Liters.");
                    fuelReserve.notifyAll();
                }
            }
        }
    }

    public void reportPumpClosed(int pumpId) {
        for (Pump pump : fuelPumps) {
            if (pump.getId() == pumpId) {
                workingPumps--;
                notify();
                return;
            }
        }
    }

    public synchronized int getFuelReserve() {
        return fuelReserve;
    }

    public int getMaxFuelCapacity() {
        return maxFuelCapacity;
    }
}
