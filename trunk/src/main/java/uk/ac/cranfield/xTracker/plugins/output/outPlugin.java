package uk.ac.cranfield.xTracker.plugins.output;

import uk.ac.cranfield.xTracker.plugins.pluginInterface;
import uk.ac.cranfield.xTracker.utils.XMLparser;
import uk.ac.cranfield.xTracker.xTracker;

public abstract class outPlugin implements pluginInterface{
    /**
     * Gets the plugin type.
     * @return plugin type
     */
    @Override
    public String getType(){
        return xTracker.OUTPUT_TYPE;
    }
    
    protected String getOutputFileName(String filename){
        return getOutputFileName(filename, "output", "outputFilename");
    }
    
    protected String getOutputFileName(String filename, String baseTag, String contentTag){
        XMLparser parser = new XMLparser(filename);
        parser.validate(baseTag);
        return parser.getElementContent(baseTag, contentTag);
    }
}


