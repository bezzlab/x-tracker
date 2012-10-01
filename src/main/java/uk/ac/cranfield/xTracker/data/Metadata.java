/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import uk.ac.ebi.pride.jmztab.model.Param;

/**
 *
 * @author Jun Fan@cranfield
 */
public class Metadata {
    private ArrayList<Param> software = new ArrayList<Param>();

    public ArrayList<Param> getSoftware() {
        return software;
    }

    public void addSoftware(Param oneSoftware) {
        software.add(oneSoftware);
    }
}
