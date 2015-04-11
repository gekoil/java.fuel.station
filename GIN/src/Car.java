import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Car {
	private final static Logger log = Logger.getLogger(Global.PROJECT_LOG_NAME);

	private static int carCount = 0;
	private String logId;
	private int id;
	private int fuel;
	private final int pumpNumber;
	private boolean needWash = false;
	private boolean needFuel = false;

	Car() {
		if(((int)(Math.random()*2)) > 0)
			needWash = true;
		fuel = (int)(Math.random()*69);
		if(fuel >= 10) {
			needFuel = true;
			pumpNumber = (int)(Math.random()*4);
		} else {
			needFuel = false;
			fuel = 0;
			pumpNumber = 0;
		}
		id = carCount++;
		initLog();
	}

	Car(int id, boolean needWash, boolean needFuel,int fuel, int pumpNumber) {
		if(id >= 0) {
			this.id = id;
			carCount++;
		} else {
			this.id = carCount++;
		}
		this.needWash = needWash;
		this.fuel = fuel;
		this.needFuel = needFuel;
		this.pumpNumber = pumpNumber;
		initLog();
	}

	private String fileName() {
		return "logs\\car_" + id + ".txt";
	}

	private void initLog() {
		FileHandler theHandler;
		logId = "Car no." + id;
		try {
			theHandler = new FileHandler(fileName());
			theHandler.setFormatter(new CustomFormatter());
			theHandler.setFilter(new FilesFilter(logId));
			log.addHandler(theHandler);
			log.setUseParentHandlers(false);
			log.info("\n" + logId + " entered the station. "
					+ "\tFuel needed: " + fuel + ". "
					+ "\tPump number: " + pumpNumber + ". "
					+ "\tNeed wash: " + needWash + ". ");
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public int getFuel() {
		return fuel;
	}

	public void addFuel(int fuel) {
		this.fuel -= fuel;
		log.info(logId + " Fueled " + fuel + " liters.");
		setNeedFuel(false);
	}

	public boolean isNeedWash() {
		return needWash;
	}

	public void setWashed(int id) {
		this.needWash = false;
		log.info(logId +" get washed by worker no." + id +".");
	}

	public int getId() {
		return id;
	}

	public boolean isNeedFuel() {
		return needFuel;
	}

	public void setNeedFuel(boolean needFuel) {
		this.needFuel = needFuel;
	}

	public int getPumpNumber() {
		return pumpNumber;
	}

	public void leaveStation() {
		log.info(logId + " Leave station.");
	}

}
