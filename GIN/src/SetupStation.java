import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SetupStation {

    public GasStation setupStationFromFile(File xmlFile)  {
        ArrayList<Pump> pumps = new ArrayList<>();
        Document doc = readXmlDoc(xmlFile);
        CarWash wash;
        GasStation station;
        if(doc != null) {
            // generate pumps
            Node stationNode = doc.getElementsByTagName("GasStation").item(0);
            int numOfPumps = Integer.parseInt(stationNode.getAttributes().getNamedItem("numOfPumps").getNodeValue());
            for (int i = 0; i < numOfPumps; ++i) {
                pumps.add(new Pump());
            }
            double pricePerLiter = Double.parseDouble(stationNode.getAttributes().getNamedItem("pricePerLiter").getNodeValue());

            // generate washing service
            Node washNode = doc.getElementsByTagName("CleaningService").item(0);
            int numOfTeams = Integer.parseInt(washNode.getAttributes().getNamedItem("numOfTeams").getNodeValue());
            double washPrice = Double.parseDouble(washNode.getAttributes().getNamedItem("price").getNodeValue());
            int autoCleanTime = Integer.parseInt(washNode.getAttributes().getNamedItem("secondsPerAutoClean").getNodeValue());
            wash = new CarWash(numOfTeams, washPrice, autoCleanTime);

            // generate station
            Node reserveNode = doc.getElementsByTagName("MainFuelPool").item(0);
            int fuelReserve = Integer.parseInt(reserveNode.getAttributes().getNamedItem("currentCapacity").getNodeValue());
            int fuelCapacity = Integer.parseInt(reserveNode.getAttributes().getNamedItem("maxCapacity").getNodeValue());
            station = new GasStation(wash, pumps, fuelReserve, fuelCapacity, pricePerLiter);

            // generate cars
            NodeList cars = doc.getElementsByTagName("Car");
            int numOfCars = cars.getLength();
            for(int i = 0; i < numOfCars; ++i) {
                Node carNode = cars.item(i);
                int id = Integer.parseInt(carNode.getAttributes().getNamedItem("id").getNodeValue());
                boolean wantWash = carNode.getAttributes().getNamedItem("wantCleaning") != null &&
                                   Boolean.parseBoolean(carNode.getAttributes().getNamedItem("wantCleaning").getNodeValue());
                boolean wantFuel = false;
                int pump = 0;
                int fuel = 0;
                NodeList childNodes = carNode.getChildNodes();
                for(int j = 0;  j <childNodes.getLength(); ++j)
                {
                    Node fuelNode = carNode.getChildNodes().item(j);
                    if (fuelNode.getNodeName() == "WantsFuel") {
                        wantFuel = true;
                        fuel = Integer.parseInt(fuelNode.getAttributes().getNamedItem("numOfLiters").getNodeValue());
                        pump = Integer.parseInt(fuelNode.getAttributes().getNamedItem("pumpNum").getNodeValue());
                    }
                    try {
                        station.addCar(new Car(id, wantWash, wantFuel, fuel, pump));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else { // start with some default values
            for (int i = 0; i < 4; ++i) {
                pumps.add(new Pump());
            }
            wash = new CarWash(3, 50.0, 1000);
            station = new GasStation(wash, pumps, 0, 1500, 6.25);
        }
        station.start();
        for(int i = 0; i < 10; i++) {
            try {
                station.addCar(new Car());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return station;
    }

    public Document readXmlDoc(File xmlFile) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return doc;
    }
}