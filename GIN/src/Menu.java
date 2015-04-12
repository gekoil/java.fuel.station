import java.io.File;
import java.util.Scanner;


public class Menu {

    File xmlFile = null;

    public static void main(String[] args) {
        String fileName = "resources\\starter.xml";
        File xmlFile = new File(fileName);
        Scanner in = new Scanner(System.in);
        SetupStation starter = new SetupStation();
        GasStation gasSt = starter.setupStationFromFile(xmlFile);
        boolean endOfDay = false;
        char choose;
        System.out.println("\tWelcome to The Gas station!" + Global.LINE_SEPARATOR);
        while(!endOfDay) {
            System.out.println("What do you want to do now?" + Global.LINE_SEPARATOR
                    + "1) Add car to the gas station." + Global.LINE_SEPARATOR
                    + "2) Add fuel to the gas station." + Global.LINE_SEPARATOR
                    + "3) Get Gas Station stats" + Global.LINE_SEPARATOR
                    + "4) Close the Gas Station.");
            choose = in.next().charAt(0);
            switch(choose) {
                case '1': addCar(gasSt, in);
                    break;
                case '2':  addFuelReserve(gasSt, in);
                    break;
                case '4':
                    endOfDay = true;
                    gasSt.setWorking(false);
                    try {
                        gasSt.join();
                    } catch (InterruptedException e) {
                    	System.out.println("There is a problem in the closing process.");
                    }
                case '3': // Getting the end of day report is also part of the shut down sequence
                    Report report = gasSt.getStats();
                    System.out.println(gasSt.getId() + ":\n" + report);
                    break;
                default:
                    System.out.println("Wrong input, please try again!");
            }
        }
        System.out.println("goodbye!");
    }

    public static void addCar(GasStation gas, Scanner in) {
        int id = 0;
        boolean needFuel = true;
        System.out.println("How much fuel the car need? (liters)");
        int fuel = (int) in.nextDouble();
        System.out.println("Do you want the car to get washed? (y/any char)");
        char wash = in.next().charAt(0);
        boolean needWash = false;
        if(wash == 'y')
            needWash = true;
        int pumps = gas.getFuelPumps().size();
        System.out.printf("Choose your gas pump. (between 1-%d)\n",pumps);
        int pumpNum = Integer.parseInt(in.next());
        if(pumpNum > pumps || pumpNum < 1)
            pumpNum = pumps;
        Car c = new Car(id, needWash, needFuel, fuel, pumpNum-1);
        try {
            gas.addCar(c);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addFuelReserve(GasStation station, Scanner in) {
        int fuelReserve = station.getFuelReserve();
        int maxCapacity = station.getMaxFuelCapacity();
        System.out.println("Gas station currently has " + fuelReserve + " Liters and can store " + maxCapacity);
        System.out.println("How much would you like to add?");
        int fuel = (int) in.nextInt();
        if(fuel <= maxCapacity - fuelReserve) {
            station.addFuel();
        }
    }

}
