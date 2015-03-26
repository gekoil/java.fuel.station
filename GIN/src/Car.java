
public class Car {
	
	private int fuel;
	private boolean needWash = false;
	
	Car() {
		if(Math.random()%2 == 0)
			needWash = true;
		fuel = (int) Math.random()*41;
	}

	public int getFuel() {
		return fuel;
	}

	public void setFuel(int fuel) {
		this.fuel = fuel;
	}

	public boolean isNeedWash() {
		return needWash;
	}

	public void setWash(boolean wash) {
		this.needWash = wash;
	}
	
}