import java.awt.event.*;

public class JperfWindowListen extends WindowAdapter{
  public JperfWindowListen() {};
  
  public void windowClosing(WindowEvent e) {
    System.exit(0);
  }
}
