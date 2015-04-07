import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Car {
	private static final Logger log = Logger.getLogger(Car.class.getName());

    private static int carCount = 0;
    private int id;
	private int fuel;
	private final int pumpNumber;
	private boolean needWash = false;
	private boolean needFuel = false;
	
	Car() {
		if(Math.random()%2 == 0)
			needWash = true;
		fuel = (int) Math.random() * 60;
		if(fuel > 10) {
			needFuel = true;
			pumpNumber = (int) Math.random() * 4;
		} else {
			needFuel = false;
			fuel = 0;
			pumpNumber = 0;
		}
        id = carCount++;

		initLog();
	}

	Car(int id, boolean needFuel, boolean needWash, int fuel, int pumpNumber) {
		this.id = id;
		this.needFuel = needFuel;
		this.needWash = needWash;
		this.fuel = fuel;
		this.pumpNumber = pumpNumber;

		initLog();
	}

	private void initLog() {
		FileHandler theHandler;
		try {
			theHandler = new FileHandler("car_" + id + ".txt");
			theHandler.setFormatter(new CustomFormatter());
			log.addHandler(theHandler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public int getFuel() {
		return fuel;
	}

	public void addFuel(int fuel) {
		this.fuel += fuel;
	}

	public boolean isNeedWash() {
		return needWash;
	}

	public void setWash(boolean wash) {
		this.needWash = wash;
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
	
}
