package uk.ac.cranfield.xTracker.plugins.rawdataLoad;

import uk.ac.cranfield.xTracker.plugins.pluginInterface;
import uk.ac.cranfield.xTracker.xTracker;

public abstract class rawData_loadPlugin implements pluginInterface{
    /**
     * Gets the plugin type.
     * @return plugin type
     */
    @Override
    public String getType(){
        return xTracker.RAWDATA_LOAD_TYPE;
    }
}
