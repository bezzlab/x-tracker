package uk.ac.cranfield.xTracker.plugins.identificationLoad;

import uk.ac.cranfield.xTracker.plugins.pluginInterface;
import uk.ac.cranfield.xTracker.xTracker;

public abstract class identData_loadPlugin implements pluginInterface{
    /**
     * Gets the plugin type.
     * @return plugin type
     */
    @Override
    public String getType(){
        return xTracker.IDENTIFICATION_LOAD_TYPE;
    }
    /**
     * create a new metadata object, populate it and assign to study
     */
    abstract void populateMetadata();
}
