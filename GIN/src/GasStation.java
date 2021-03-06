import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class GasStation extends Thread {
    private static final Logger log = Logger.getLogger(Global.PROJECT_LOG_NAME);
    private static int stationCount = 0;

    private CarWash carWash;
    private ArrayList<Pump> fuelPumps;
    private BlockingQueue<Car> incomingCars;
    private int id;
    private int carsWashed;
    private int carsFueled;
    private Double fuelProfits;
    private Double washProfits;
    private boolean isWorking;
    private double fuelCost;
    private String logId;
    private FuelTank tank;

    public GasStation(CarWash carWash, ArrayList<Pump> fuelPumps, int fuelReserve, int fuelCapacity, double fuelCost) {
        this.id = stationCount++;
        this.carWash = carWash;
        carWash.setStation(this);
        this.fuelPumps = fuelPumps;
        for(Pump pump : this.fuelPumps) {
            pump.setStation(this);
        }
        tank = new FuelTank(fuelReserve, fuelCapacity);
        incomingCars = new LinkedBlockingDeque<>();
        isWorking = true;
        this.fuelCost = fuelCost;
        fuelProfits = 0.0;
        washProfits = 0.0;
        initLog();
    }

    @Override
    public void run() {
        log.info(logId + "The Station is open.");
        carWash.start();
        for(Pump p : fuelPumps)
            p.start();
        while(isWorking || !incomingCars.isEmpty()) {
			Car next = incomingCars.poll();
			if(next != null)
            	organizer(next);
        }
        log.info(logId + "Start a closing procedure.");
        try {
            for(Pump p : fuelPumps) {
                p.shutDown();
            }
            for(Pump p : fuelPumps) {
                p.join();
            }
        } catch (InterruptedException e) {
            log.info(logId + e.getStackTrace());
        }
        getStats();
    }

    private void initLog() {
        FileHandler handler;
        try {
            logId = "Gas Station " + id + ":";
            handler = new FileHandler("logs\\gasStation_" + id + ".txt");
            handler.setFormatter(new CustomFormatter());
            handler.setFilter(new FilesFilter(logId));
            log.addHandler(handler);
            log.setUseParentHandlers(false);
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
            washProfits += money;
        }
    }

    public void payForFuel(double money) {
        synchronized (fuelProfits) {
            fuelProfits += money;
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
        fuelPump.start();
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

    public void setWorkingOff() {
    	carWash.setEndOfDay();
        try {
            carWash.join();
		} catch (InterruptedException e) {
			log.info(e.toString());
		}
        this.isWorking = false;
    }

    public void requestFuel(int fuelRequest) {
    	synchronized(tank) {
    		while(tank.getFuel() < fuelRequest) {
    			try {
    				if(tank.getFuel() < tank.getLow())
    					tank.setFull();
					tank.wait();
				} catch (InterruptedException e) {
					log.severe(logId + e.getStackTrace());
				}
    		}
            try {
				tank.setFuel(-fuelRequest);
			} catch (Exception e) {
				log.severe(logId + e.getStackTrace());
				tank.setFull();
			}
            log.info("The fuel reserve have now: " + tank.getFuel() + " liters.");
            tank.notifyAll();
        }
    }

    public synchronized int getFuelReserve() {
        return tank.getFuel();
    }

    public int getMaxFuelCapacity() {
        return tank.getMax();
    }

    public void addFuel(int fuel) {
    	requestFuel(fuel);
    }

    class FuelTank {
    	private int fuel;
    	private final int MAX;

    	public FuelTank(int fuel,int max) {
    		this.fuel = fuel;
    		this.MAX = max;
    	}

    	public synchronized void setFull() {
    		try {
				sleep(300);
			} catch (InterruptedException e) {
				log.info(logId + e.getStackTrace());
			}
    		fuel = MAX;
    	}

		public int getFuel() {
			return fuel;
		}
		public void setFuel(int reqFuel) throws Exception {
			this.fuel += reqFuel;
			log.info(logId + "The fuel reserve capacity is now " + fuel + " Liters.");
			if(fuel < getLow()) {
				throw new Exception("The fuel reserved is less then 20%");
			}
		}
		public int getMax() {
			return MAX;
		}

		public int getLow() {
			return (int)( MAX*0.2);
		}

    }
}
