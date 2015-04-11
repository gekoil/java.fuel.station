
public class Report {
    public double washProfits;
    public double fuelProfits;
    public int washedCars;
    public int fueledCars;

    Report(double washProfits, double fuelProfits, int washedCars, int fueledCars) {
        this.washProfits = washProfits;
        this.fuelProfits = fuelProfits;
        this.washedCars = washedCars;
        this.fueledCars = fueledCars;
    }

    @Override
    public String toString() {
        return "Total cars washed: " + washedCars + Global.LINE_SEPARATOR +
                "The profits from car washing are: " + washProfits + Global.LINE_SEPARATOR +
                "Total cars fueled: " + fueledCars + Global.LINE_SEPARATOR +
                "The profits from fueling are: " + fuelProfits;
    }
}