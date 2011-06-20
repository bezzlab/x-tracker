
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.guiutils
//    File: OverlayBorder.java
//    Date: 17/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.guiutils;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 *
 * http://forums.sun.com/thread.jspa?threadID=775954
 * @author andrew bullimore
 */
public class OverlayBorder implements Border {
    
    private static final Insets INSETS = new Insets(0, 0, 0, 0);
    private final Border over;
    private final Border under;

    public static void overlayBorder(JComponent component, Border border) {
        
        component.setBorder(new OverlayBorder(border, component.getBorder()));
    }

    public static void restoreBorder(JComponent component) {
        
        Border border = component.getBorder();
        if(border instanceof OverlayBorder) {
            
            component.setBorder(((OverlayBorder) border).under);
        }
    }

    private OverlayBorder(Border over, Border under) {

            this.over = over;
            this.under = under;
    }

    @Override
    public Insets getBorderInsets(Component component) {

        return (this.under != null) ? this.under.getBorderInsets(component) : INSETS;
    }

    @Override
    public boolean isBorderOpaque() {

        return this.over.isBorderOpaque() && ((this.under != null) ? this.under.isBorderOpaque() : true);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

        this.over.paintBorder(c, g, x, y, w, h);
        this.under.paintBorder(c, g, x, y, w, h);
    }
}
