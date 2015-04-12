import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CarWash extends Thread {
	private static final Logger log = Logger.getLogger(Global.PROJECT_LOG_NAME);

	private static int workerCount = 0;
	private static int carWashCount = 0;
	private int numWorkers;
	private int autoCleanTime;
	private final int id;
	private double washCost;
	private GasStation station;
	private ArrayList<Cleaner> cleaners = new ArrayList<>();
	private Tunnel tunnel = new Tunnel();
	private BlockingQueue<Car> waitingToIntern;
	private BlockingQueue<Car> waitingToWash;
	private boolean endOfDay = false;
	private boolean goHome = false;
	private String logId;
	private boolean pollingCar;

	public CarWash(int numWorkers, double washCost, int autoCleanTime) {
		this.id = carWashCount++;
		this.numWorkers = numWorkers;
		this.autoCleanTime = autoCleanTime;
		this.washCost = washCost;
		this.pollingCar = false;
		waitingToWash = new LinkedBlockingDeque<>();
		waitingToIntern =  new LinkedBlockingDeque<>();
		initLog();
		for(int i = 0; i < numWorkers; i++)
			cleaners.add(new Cleaner());
		log.info(logId + "is ready to Work.");
	}
	
	private void initLog() {
		FileHandler theHandler;
		logId = "CarWash no." + id + " ";
		try {
			theHandler = new FileHandler("logs\\wash_" + id + ".txt");
			theHandler.setFormatter(new CustomFormatter());
			theHandler.setFilter(new FilesFilter(logId));
			log.addHandler(theHandler);
			log.setUseParentHandlers(false);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		tunnel.start();
		for(Cleaner cln : cleaners) {
			cln.start();
		}
		try {
			tunnel.join();
			goHome = true;
			for(Cleaner cln : cleaners)
				cln.join();
			log.info(logId + "is close.");
		} catch (InterruptedException e) {
			log.info(logId + e.toString());
		}
	}

	public GasStation getStation() {
		return station;
	}

	public void setStation(GasStation station) {
		this.station = station;
	}

	public void addCar(Car c) {
		try {
			waitingToWash.put(c);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setEndOfDay() {
		endOfDay = true;
	}

	public int getNumWorkers() {
		return numWorkers;
	}

	public boolean isEndOfDay() {
		return endOfDay;
	}

	public int getWaitingTime() {
		int time = autoCleanTime * waitingToWash.size();
		int timeIn = 0;
		for(Cleaner cln : cleaners)
			timeIn += cln.getEfficiency();
		time += waitingToIntern.size() * (timeIn/numWorkers);
		return time;
	}
	
	private Car internCar() {
		synchronized (waitingToIntern) {
			while(pollingCar) {
				try {
					wait();
				} catch (InterruptedException e) {
					log.info(e.toString());
				}
			}
			pollingCar = true;
			Car c = waitingToIntern.poll();
			pollingCar = false;
			waitingToIntern.notifyAll();
			return c;
		}
	}

	class Tunnel extends Thread {

		@Override
		public void run() {
			while(!endOfDay || !waitingToWash.isEmpty()) {
				try {
					Car c = waitingToWash.poll();
					if(c == null)
						continue;
					sleep(autoCleanTime * 500);
					log.info(logId + " Car " + c.getId() + " get out from the tunnel.");
					waitingToIntern.put(c);
				} catch (InterruptedException | NullPointerException e) {
					e.printStackTrace();
				}
			}
			log.info(logId + "The tunnel is closed.");
		}
	}

	class Cleaner extends Thread {

		private final int EFFICIENCY;
		private final int WORKER_ID;
		
		public Cleaner() {
			WORKER_ID = workerCount++;
			EFFICIENCY = 500 + (int)(Math.random() * 501);
		}

		public int getWORKER_ID() {
			return WORKER_ID;
		}

		public int getEfficiency() {
			return EFFICIENCY;
		}

		public void run() {
			log.info(logId + "Worker no." + WORKER_ID + "start working.");
			while(!goHome || !waitingToIntern.isEmpty()) {
				try {
					Car c = internCar();
					if(c == null)
						continue;
					sleep(EFFICIENCY);
					c.setWashed(WORKER_ID);
					log.info(logId + "done clean Car " + c.getId() + " by worker " + WORKER_ID);
					station.payForWash(washCost);
					station.addCar(c);
					log.info(logId + "Car " + c.getId() + " leave the wash station.");
				} catch (Exception e) {
					e.getMessage();
				}
			}
			log.info(logId + "Worker " + WORKER_ID + " go home.");
		}
	}
}
