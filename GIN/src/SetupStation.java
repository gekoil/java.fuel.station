import java.io.File;
import java.util.ArrayList;

public class SetupStation {
	
	private GasStation station;
	private CarWash wash;
	private ArrayList<Pump> pumps;

    public SetupStation(File xmlFile)  {
        pumps = new ArrayList<>();
        wash = new CarWash();
        // TODO: Read the XML file provided in args variable
        station = new GasStation(wash, pumps, 0, 1500);
        for(int i = 0; i < 4; i++)
        	station.addFuelPump(new Pump(station));
        station.start();
    }

	public GasStation getStation() {
		return station;
	}
    
}
