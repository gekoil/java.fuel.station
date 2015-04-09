import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CarWash extends Thread {
	private static final Logger log = Logger.getLogger(CarWash.class.getName());

	private final int TUNEL_TIME = 100;
	
	private static int workerCount = 0;
	private int numWorkers;
	private static int carWashCount = 0;
	private final int id;
	private GasStation station;
	private ArrayList<Cleaner> cleaners = new ArrayList<CarWash.Cleaner>();
	private Tunnel tunnel = new Tunnel();
	private ArrayDeque<Car> waitingToIntern = new ArrayDeque<Car>();
	private ArrayDeque<Car> waitingToWash = new ArrayDeque<Car>();
	private boolean endOfDay = false;
	private boolean goHome = false;
	private String logId;
	
	public CarWash(int numWorkers) {
		this.id = carWashCount++;
		this.numWorkers = numWorkers;
		for(int i = 0; i < numWorkers; i++)
			cleaners.add(new Cleaner());
		FileHandler theHandler;
        logId = "CarWash no." + id + " ";
        try {
            theHandler = new FileHandler("wash_" + id + ".txt");
            theHandler.setFormatter(new CustomFormatter());
            theHandler.setFilter(new FilesFilter(logId));
            log.addHandler(theHandler);
            log.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        log.info(logId + "is ready to Work.");
	}
	
	@Override
	public void run() {
		tunnel.start();
		for(Cleaner cln : cleaners)
			cln.start();
		try {
			tunnel.join();
			goHome = true;
			for(Cleaner cln : cleaners) {
				wakeUP();
				cln.join();
			}
			log.info(logId + "is close.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public GasStation getStation() {
		return station;
	}

	public void setStation(GasStation station) {
		this.station = station;
	}


	public void addCar(Car c) {
		waitingToWash.addLast(c);
		wakeUP();
	}
	
	public void setEndOfDay() {
		endOfDay = true;
		wakeUP();
	}
	
	public int getNumWorkers() {
		return numWorkers;
	}

	public boolean isEndOfDay() {
		return endOfDay;
	}
	
	public int getWaitingTime() {
		int time = TUNEL_TIME * waitingToWash.size();
		int timeIn = 0;
		for(Cleaner cln : cleaners)
			timeIn += cln.getEfficiency();
		time += waitingToIntern.size() * (timeIn/numWorkers);
		return time;
	}
	
	public synchronized Car getCarOut() {
		while(waitingToWash.isEmpty() && !endOfDay) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return waitingToWash.pollFirst();
	}

	public synchronized Car getCarIn() {
		while(waitingToIntern.isEmpty() && !goHome) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return waitingToIntern.pollFirst();
	}
	
	public synchronized void wakeUP() {
		notifyAll();
	}
	
	class Tunnel extends Thread {

		@Override
		public void run() {
			while(!endOfDay || !waitingToWash.isEmpty()) {
				Car c = getCarOut();
				if(c != null)
					try {
						sleep(1000);
						waitingToIntern.addLast(c);
						wakeUP();
					} catch (InterruptedException | NullPointerException e) {
						e.printStackTrace();
					}
			}
			log.info(logId + "The tunnel close.");
		}
		
	}
	
	class Cleaner extends Thread {

		private int efficiency = 500 + (int)Math.random() * (1001);
		private final int workerId = workerCount++;

		public int getEfficiency() {
			return efficiency;
		}

		public void run() {
			while(!goHome || !waitingToIntern.isEmpty()) {
				Car c = getCarIn();
				if(c != null)
					try {
						sleep(efficiency);
						c.setWashed(workerId);
						station.payForServise(50.0);
						station.addCar(c);
					} catch (InterruptedException | NullPointerException e) {
						e.getMessage();
					} catch (Exception e1) {
						e1.getMessage();
					}
			}
			log.info(logId + "Worker " + workerId + " go home.");
		}

	}
	
}
