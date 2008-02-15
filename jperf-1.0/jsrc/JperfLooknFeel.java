import javax.swing.*;
import java.io.Serializable;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.*;

public class JperfLooknFeel extends MetalLookAndFeel implements Serializable {

    public String getDescription() {
	return "NLANR Jperf Theme";
    }

    public String getID() {
	return "JperfTheme";
    }

    public String getName() {
	return "NLANR Jperf Look And Feel";
    }

    public boolean isNativeLookAndFeel() {
	return false;
    }
	
    public boolean isSupportedLookAndFeel() {
	return true;
    }

    protected void initClassDefaults(UIDefaults defaultTable) {
	super.initClassDefaults(defaultTable);
	defaultTable.put("ButtonUI","buttonUI");
    }

    protected void initComponentDefaults(UIDefaults table) {
	super.initComponentDefaults(table);
    }

    protected void initSystemColorDefaults(UIDefaults defaultTable) {
	super.initSystemColorDefaults(defaultTable);
	String[] defaultSystemColors = {
	  //borders around boxes
	    "controlShadow", "#41657F"
	};
	loadSystemColors(defaultTable, defaultSystemColors, false);
    }
}
