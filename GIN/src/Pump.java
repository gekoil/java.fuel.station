import java.util.logging.Logger;

public class Pump extends Thread{
    private static final Logger log = Logger.getLogger(Pump.class.getName());

    private FuelStation station;
	
    public  Pump(FuelStation station) {
        this.station = station;
    }

    public void setStation(FuelStation station) {
        this.station = station;
    }

    @Override
    public void run() {
        // TODO: Add execution here
    }
}
