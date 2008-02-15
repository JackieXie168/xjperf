import java.util.Vector;

public class JperfStreamResult {

  protected int ID;
  protected Vector Bandwidth;
  protected Vector Jitter;

  public JperfStreamResult(int i) {
    ID = i;
    Bandwidth = new Vector();
    Jitter = new Vector();
  }

  void addBW(Measurement M) {
    Bandwidth.add(M);
  }

  void addJitter(Measurement M) {
    Jitter.add(M);
  }

  int getID() {
    return ID;
  }

  void print() {
    for(int i=0; i<Bandwidth.size(); ++i)
      ((Measurement)Bandwidth.get(i)).print();
  }

  Vector getBW() {
    return Bandwidth;
  }
  Vector getJitter() {
    return Jitter;
  }
}
