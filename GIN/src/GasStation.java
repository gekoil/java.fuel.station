import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GasStation extends Thread{
    private static final Logger log = Logger.getLogger(GasStation.class.getName());
    private final String FILE_HANDLER = GasStation.class.getName() + ".txt";

    private CarWash carWash;
    private ArrayList<Pump> fuelPumps;
    private ArrayDeque<Car> incomingCars;
    private Integer fuelReserve;
    private int maxFuelCapacity;
    private Double profits = 0.0;
    private boolean paying = false;
    private boolean isWorking;
    private double fuelCost;

    public GasStation(CarWash carWash, ArrayList<Pump> fuelPumps, int fuelReserve, int fuelCapacity, double fuelCost) {
    	initLog();
        this.carWash = carWash;
        carWash.setStation(this);
        this.fuelPumps = fuelPumps;
        this.fuelReserve = fuelReserve;
        this.maxFuelCapacity = fuelCapacity;
        incomingCars = new ArrayDeque<Car>();
        isWorking = true;
        this.fuelCost = fuelCost;
    }

    @Override
    public void run() {
    	log.info("The Station is open.");
    	carWash.start();
    	for(Pump p : fuelPumps)
    		p.start();
        while(isWorking || !incomingCars.isEmpty()) {
        	if(!incomingCars.isEmpty())
        		organizer(incomingCars.pollFirst());
        }
        log.info("Start a closing procedure.");
        try {
			carWash.join();
			for(Pump p : fuelPumps) {
				p.join();
			}
		} catch (InterruptedException e) {
			log.info(e.getMessage());
		}
        log.info("The profits are: " + profits + ".");
    }
    
    private void initLog() {
    	FileHandler handler;
    	try {
    		handler = new FileHandler(FILE_HANDLER);
    		handler.setFormatter(new CustomFormatter());
    		log.addHandler(handler);
    		log.setUseParentHandlers(false);
    	} catch (SecurityException | IOException e) {
			e.getMessage();
		}
    }
    
    private void organizer(Car next) {
    	if(!next.isNeedFuel() && !next.isNeedWash()) {
    		next.leaveStation();
    	}
    	else if(next.isNeedFuel() && !next.isNeedWash())
    		fuelPumps.get(next.getPumpNumber()).addCar(next);
    	else if(next.isNeedWash() && !next.isNeedFuel())
    		carWash.addCar(next);
    	else if(next.isNeedFuel() && next.isNeedWash())
    		if(carWash.getWaitingTime() < fuelPumps.get(next.getPumpNumber()).getWatingTime())
    			carWash.addCar(next);
    		else
    			fuelPumps.get(next.getPumpNumber()).addCar(next);
    }
    
    public synchronized void payForServise(double money) {
    	while(paying) {
    		try {
				wait();
			} catch (InterruptedException e) {
				e.getMessage();
			}
    	}
		profits += money;
		paying = false;
		notifyAll();
    }

    public CarWash getCarWash() {
        return carWash;
    }

    public ArrayList<Pump> getFuelPumps() {  // I don't think its a good design to let someone out side the class
        return fuelPumps;					// to touch the fuelPumps.
    }

    public void addFuelPump(Pump fuelPump) {
        fuelPumps.add(fuelPump);
    }

    public void addCar(Car car) throws Exception {
    	if(isWorking)
    		incomingCars.addLast(car);
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

	public boolean requestFuel(int fuelRequest) throws InterruptedException {
        synchronized(fuelReserve) {
            if (fuelReserve > fuelRequest) {
                fuelReserve -= fuelRequest;
                log.info("The fule reserve have now: " + fuelReserve + " liters.");
                if(fuelReserve < maxFuelCapacity * 0.2)
                	addFuel();
                notifyAll();
                return true;
            } else {
                return false;
            }
        }
    }

    public void addFuel() {
        synchronized (fuelReserve) {
            fuelReserve = maxFuelCapacity;
            try {
				sleep(200);
			} catch (InterruptedException e) {
				e.getMessage();
			} finally {
				log.info("The fule reserve in Full capacity of " + fuelReserve + " liters.");
				fuelReserve.notifyAll();
			}
        }
    }
    
}
