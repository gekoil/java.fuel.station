import java.util.ArrayDeque;
import java.util.concurrent.ArrayBlockingQueue;

public class CarWash extends Thread {

	private final int NUM_WORKERS = 3;
	private final int TUNEL_TIME = 100;
	Cleaner[] cleaner = new Cleaner[NUM_WORKERS];
	Tunnel tunnel = new Tunnel();
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

	public int getTimeWaiting() {
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
					sleep(100);
					waitingToIntern.addLast(c);
					wakeUP();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (NullPointerException e1) { }
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
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (NullPointerException e1) { }
			}
		}
		
	}
	
}
