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
    private int id;
    private int carsWashed;
    private int carsFueled;
    private Double fuelProfits;
    private Double washProfits;
    private boolean isWorking;
    private double fuelCost;
    private String logId;
    private FuelTank tank;
	private boolean payingWash;
	private boolean payingFuel;
	private boolean noMoreCars;

    public GasStation(CarWash carWash, ArrayList<Pump> fuelPumps, int fuelReserve, int fuelCapacity, double fuelCost) {
        this.id = stationCount++;
        this.carWash = carWash;
        carWash.setStation(this);
        this.fuelPumps = fuelPumps;
        for(Pump pump : this.fuelPumps) {
            pump.setStation(this);
        }
        tank = new FuelTank(fuelReserve, fuelCapacity);
        incomingCars = new LinkedBlockingDeque<Car>();
        isWorking = true;
        this.fuelCost = fuelCost;
        fuelProfits = 0.0;
        washProfits = 0.0;
        payingWash = false;
        payingFuel = false;
        initLog();
    }

    @Override
    public void run() {
        log.info("The Station is open.");
        carWash.start();
        for(Pump p : fuelPumps)
            p.start();
        while(isWorking || !incomingCars.isEmpty()) {
			Car next = incomingCars.poll();
			if(next != null)
            	organizer(next);
        }
        log.info("Start a closing procedure.");
        try {
            for(Pump p : fuelPumps) {
                p.shutDown();
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
        	while(payingWash) {
        		try {
					washProfits.wait();
				} catch (InterruptedException e) {
					log.info(e.toString());
				}
        	}
        	payingWash = true;
        	washProfits += money;
        	payingWash = false;
        	washProfits.notifyAll();
        }
    }

    public void payForFuel(double money) {
        synchronized (fuelProfits) {
        	while(payingFuel) {
        		try {
					fuelProfits.wait();
				} catch (InterruptedException e) {
					log.info(e.toString());
				}
        	}
        	payingFuel = true;
            fuelProfits += money;
            payingFuel = false;
            washProfits.notifyAll();
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
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
            try {
				tank.setFuel(-fuelRequest);
			} catch (Exception e) {
				log.info(e.getMessage());
				tank.setFull();
			}
            log.info("The fule reserve have now: " + tank.getFuel() + " liters.");
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
				log.info(e.toString());
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
