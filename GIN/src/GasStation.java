import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GasStation extends Thread{
    private static final Logger log = Logger.getLogger(GasStation.class.getName());
    private final String FILE_HANDLER = GasStation.class.getName() + ".xml";

    private CarWash carWash;
    private ArrayList<Pump> fuelPumps;
    private ArrayDeque<Car> incomingCars;
    private Integer fuelReserve;
    private int maxFuelCapacity;
    private Double profits = 0.0;
    private boolean pay = false;
    private boolean isWorking;

    public GasStation(CarWash carWash, ArrayList<Pump> fuelPumps, int fuelReserve, int fuelCapacity) {
    	initLog();
        this.carWash = carWash;
        carWash.setStation(this);
        this.fuelPumps = fuelPumps;
        this.fuelReserve = fuelReserve;
        this.maxFuelCapacity = fuelCapacity;
        incomingCars = new ArrayDeque<Car>();
        isWorking = true;
    }

    @Override
    public void run() {
    	log.log(Level.INFO, "The Station is open.");
    	carWash.start();
        while(isWorking || !incomingCars.isEmpty()) {
        	if(!incomingCars.isEmpty())
        		organizer(incomingCars.pollFirst());
        }
        log.log(Level.INFO, "Start a closing procedure.");
        try {
			carWash.join();
			for(Pump p : fuelPumps) {
				p.join();
			}
		} catch (InterruptedException e) {
			log.log(Level.WARNING, e.getMessage());
		}
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
    	if(next.isNeedFuel() && !next.isNeedWash())
    		fuelPumps.get(next.getPumpNumber()).addCar(next);
    	else if(next.isNeedWash() && !next.isNeedFuel())
    		carWash.addCar(next);
    	else if(next.isNeedFuel() && next.isNeedWash())
    		if(carWash.getWaitingTime() < fuelPumps.get(next.getPumpNumber()).getWatingTime())
    			carWash.addCar(next);
    		else
    			fuelPumps.get(next.getPumpNumber()).addCar(next);
    }
    
    public void payForServise(double money) {
    	synchronized (profits) {
			if(pay)
				try {
					wait();
				} catch (InterruptedException e) {
					e.getMessage();
				}
			pay = true;
			profits += money;
			notifyAll();
		}
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
        fuelReserve.notifyAll();
    }
    
}
