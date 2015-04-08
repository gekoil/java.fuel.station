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
    private Double profits = 0.0;
    private boolean pay = false;
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
        while(!incomingCars.isEmpty() || isWorking) {
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

    public ArrayList<Pump> getFuelPumps() {
        return fuelPumps;
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
