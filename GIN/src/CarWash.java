import java.util.ArrayDeque;
import java.util.logging.Logger;

public class CarWash extends Thread {
	private static final Logger log = Logger.getLogger(CarWash.class.getName());

	private final int NUM_WORKERS = 3;
	private final int TUNEL_TIME = 100;
	
	private GasStation station;
	private Cleaner[] cleaner = new Cleaner[NUM_WORKERS];
	private Tunnel tunnel = new Tunnel();
	private ArrayDeque<Car> waitingToIntern = new ArrayDeque<Car>();
	private ArrayDeque<Car> waitingToWash = new ArrayDeque<Car>();
	private boolean endOfDay = false;
	private boolean goHome = false;
	
	@Override
	public void run() {
		tunnel.start();
		for(int i = 0; i < NUM_WORKERS; i++) {
			cleaner[i] = new Cleaner();
			cleaner[i].start();
		}
		try {
			tunnel.join();
			goHome = true;
			for(Cleaner cln : cleaner) {
				wakeUP();
				cln.join();
			}
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
	
	public void setEndOfDay(boolean end) {
		endOfDay = end;
		wakeUP();
	}
	
	public int getNUM_WORKERS() {
		return NUM_WORKERS;
	}

	public boolean isEndOfDay() {
		return endOfDay;
	}
	
	public void allLeft() throws InterruptedException {
		int bye = 0;
		while(bye < NUM_WORKERS) {
			wakeUP();
			cleaner[bye].join();
			bye++;
		}
	}

	public int getWaitingTime() {
		int time = TUNEL_TIME * waitingToWash.size();
		int timeIn = 0;
		for(Cleaner cln : cleaner)
			timeIn += cln.getEfficiency();
		time += waitingToIntern.size() * (timeIn/NUM_WORKERS);
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
				try {
					sleep(1000);
					waitingToIntern.addLast(c);
					wakeUP();
				} catch (InterruptedException | NullPointerException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class Cleaner extends Thread {

		private int efficiency = 500 + (int)Math.random() * (1001);

		public int getEfficiency() {
			return efficiency;
		}

		public void run() {
			while(!goHome || !waitingToIntern.isEmpty()) {
				Car c = getCarIn();
				try {
					sleep(efficiency);
					c.setWash(false);
					station.addCar(c);
				} catch (InterruptedException | NullPointerException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
}
