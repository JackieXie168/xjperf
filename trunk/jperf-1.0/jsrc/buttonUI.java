import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import java.net.URL;

public class buttonUI extends BasicButtonUI {

  private static ImageIcon b1;
  private static ImageIcon b2; 
  private static ImageIcon b3; 
  private static ImageIcon b4;
  
  private static ImageIcon b1pressed;
  private static ImageIcon b2pressed; 
  private static ImageIcon b3pressed;
  private static ImageIcon b4pressed;
  
  private static ImageIcon b1disabled;
  private static ImageIcon b2disabled;
  private static ImageIcon b3disabled;
  private static ImageIcon b4disabled;


  public buttonUI() {
    b1 = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton.gif"));
    b2 = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton2.gif")); 
    b3 = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton3.gif"));
    b4 = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton4.gif"));
    
    b1pressed = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-pressed.gif"));
    b2pressed = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-pressed2.gif"));
    b3pressed = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-pressed3.gif"));
    b4pressed = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-pressed4.gif"));
  
    b1disabled = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-disable.gif"));
    b2disabled = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-disable2.gif"));
    b3disabled = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-disable3.gif"));
    b4disabled = new ImageIcon(buttonUI.class.getResource("buttons/jperfbutton-disable4.gif"));
  }

  private static final buttonUI frameworkButton = new buttonUI();
  
  public static ComponentUI createUI(JComponent c){
    return frameworkButton; 
  }
  
  public void installUI(JComponent comp) {
    super.installUI(comp);
    AbstractButton b = (AbstractButton)comp;
    if((b instanceof JButton) && !(b instanceof MetalComboBoxButton) && !(b instanceof BasicArrowButton) && !(b instanceof MetalScrollButton))
	    {
		b.setBorderPainted(false);
		b.setHorizontalTextPosition(SwingConstants.CENTER);
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
		setIcons(b);
	    }
    }

    public void setIcons(AbstractButton butt) {
	int strWidth = butt.getFontMetrics(butt.getFont()).stringWidth(butt.getText());
	if(strWidth == 0);

	else if(strWidth < 30) {
	    //System.out.println(strWidth);
	    butt.setIcon(b1);
	    butt.setPressedIcon(b1pressed);
	    butt.setDisabledIcon(b1disabled);
	    butt.setPreferredSize(new Dimension(b1.getIconWidth(),b1.getIconHeight()));
	}
	else if(strWidth < 46) {
	    butt.setIcon(b2);
	    butt.setPressedIcon(b2pressed);
	     butt.setDisabledIcon(b2disabled);
	    butt.setPreferredSize(new Dimension(b2.getIconWidth(),b2.getIconHeight()));
	}
	else if(strWidth < 65) {
	    butt.setIcon(b3);
	    butt.setPressedIcon(b3pressed);
	    butt.setDisabledIcon(b3disabled);
	    butt.setPreferredSize(new Dimension(b3.getIconWidth(),b3.getIconHeight()));
	}
	else {
	  butt.setIcon(b4);
	  butt.setPressedIcon(b4);
	  butt.setDisabledIcon(b4disabled);
	  butt.setPreferredSize(new Dimension(b4.getIconWidth(),b4.getIconHeight()));
	}
    }
}
