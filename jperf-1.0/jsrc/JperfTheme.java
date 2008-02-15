import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import java.awt.event.*;

public class JperfTheme extends DefaultMetalTheme {

    public String getName() {
	return "JperfTheme";
    }

    //color1: for the titles and highlights
    private final ColorUIResource primary1 = new ColorUIResource(69,132,66);
    
    //color2: depressed menus
    private final ColorUIResource primary2 = new ColorUIResource(129,183,126);

    //color3: scrollbar highlights
    private final ColorUIResource primary3 = new ColorUIResource(69,132,66);
    
    //color4: base outline
    private final ColorUIResource secondary1 = new ColorUIResource(129,183,126);

    //color5: inactive tabs
    private final ColorUIResource secondary2 = new ColorUIResource(129,183,126);
	//(135,146,171);
    
    //color6: background color
  private final ColorUIResource secondary3 = new ColorUIResource(220,239,206);
  //private final ColorUIResource secondary3 = new ColorUIResource(189,216,164);
  
  
  
  

    private FontUIResource controlFont;
    private FontUIResource systemFont;
    private FontUIResource userFont;
    private FontUIResource smallFont;

    private int baseFontSize = 11;

    //subject to change
    private String fontName = "dialog";

    
    JperfTheme() {
    ; //do nothing.. not sure what this is for yet
    }

    protected ColorUIResource getPrimary1() { return primary1;}
    protected ColorUIResource getPrimary2() { return primary2;}
    protected ColorUIResource getPrimary3() { return primary3;}
    protected ColorUIResource getSecondary1() { return secondary1;}
    protected ColorUIResource getSecondary2() { return secondary2;}
    protected ColorUIResource getSecondary3() { return secondary3;}
}
