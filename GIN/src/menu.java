import java.io.File;
import java.util.Scanner;


public class menu {

	public static void main(String[] args) {
		String fileName = "starter.xml";
		File xmlFile = new File(fileName);
		SetupStation starter = new SetupStation(xmlFile);
		GasStation gasSt = starter.getStation();
		boolean endOfDay = false;
		char choose;
		Scanner in = new Scanner(System.in);
		System.out.println("\tWelcome to The Gas station!\n\n");
		while(!endOfDay) {
			System.out.println("What do you want to do now?\n"
					+ "1) Add car to the gas station.\n"
					+ "2) Add fuel to the gas station.\n"
					+ "3) Closed the Gas Station.\n");
			choose = in.nextLine().charAt(0);
			switch(choose) {
			case '1': addCar(gasSt);
				break;
			case '2':  System.out.println("case 2.");
				break;
			case '3': endOfDay = true;
				gasSt.setWorking(false);
				break;
			}
		}
		try {
			gasSt.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("goodbye!");
	}
	
	public static void addCar(GasStation gas) {
		Scanner in = new Scanner(System.in);
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
		Car c = new Car(needWash, fuel, pumpNum-1);
		try {
			gas.addCar(c);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
