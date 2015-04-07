import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.Logger;

public class GasStation extends Thread{
    private static final Logger log = Logger.getLogger(GasStation.class.getName());

    private CarWash carWash;
    private ArrayList<Pump> fuelPumps;
    private ArrayDeque<Car> incomingCars;
    private Integer fuelReserve;
    private int maxFuelCapacity;
    private boolean isWorking;

    public GasStation(CarWash carWash, ArrayList<Pump> fuelPumps, int fuelReserve, int fuelCapacity) {
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
        while(isWorking || !incomingCars.isEmpty()) {
            // TODO: Add looping on incoming cars until shutdown is called
        	if(!incomingCars.isEmpty()) {
	        	Car next = incomingCars.pollFirst();
	        	if(next.isNeedFuel() && !next.isNeedWash())
	        		fuelPumps.get(next.getPumpNumber()).addCar(next);
	        	else if(next.isNeedWash() && !next.isNeedFuel())
	        		carWash.addCar(next);
	        	else if(next.isNeedFuel() && next.isNeedWash())
	        		if(carWash.getWaitingTime() <= fuelPumps.get(next.getPumpNumber()).getWatingTime())
	        			carWash.addCar(next);
	        		else
	        			fuelPumps.get(next.getPumpNumber()).addCar(next);
        	}
        }
        try {
			carWash.join();
			for(Pump p : fuelPumps) {
				p.join();
			}
		} catch (InterruptedException e) {
			e.getMessage();
		}
    }

    public CarWash getCarWash() {
        return carWash;
    }

    public ArrayList<Pump> getFuelPumps() {
        return fuelPumps;
    }

    public void addFuelPump(Pump fuelPump) {
        fuelPumps.add(fuelPump);
    }

    public void addCar(Car car) {
        incomingCars.addLast(car);
    }

    public boolean isWorking() {
		return isWorking;
	}

	public void setWorking(boolean isWorking) {
		this.isWorking = isWorking;
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
