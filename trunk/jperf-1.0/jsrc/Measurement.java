public class Measurement {

  protected double start;
  protected double end;
  protected double value;
  protected String units;

  public Measurement(double s, double e, double v, String u) {
    start = s;
    end = e;
    value = v;
    units = u;
  }

  double startTime() {
    return start;
  }

  double endTime() {
    return end;
  }

  double getValue() {
    return value;
  }
  
  String getUnits() {
    return units;
  }

  void print() {
    System.out.println("Start Time: " + start);
    System.out.println("End Time: " + end);
    System.out.println("Value: " + value);
    System.out.println("Units: " + units);
  }
 
}
