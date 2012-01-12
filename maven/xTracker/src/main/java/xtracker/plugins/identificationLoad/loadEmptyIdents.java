package xtracker.plugins.identificationLoad;

import xtracker.data.xLoad;

public class loadEmptyIdents implements identData_loadPlugin {
    /**
     * Plugin that does not load any identification.
     * @param inputData the xLoad data structure
     * @param paramFile an (empty) parameter file.
     * @return ret the xLoad structure as in input.
     */
    public xLoad start(xLoad inputData, String paramFile) {
        return inputData;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
    private final static String name = "Load empty identification file";
    private final static String version = "1.00";
//    private final static String type = "IDENTDATA_LOAD_plugin";
    private final static String description = "This plugin loads no identifications. Useful if you want to quantitate without knowing peptide identifications.";
}