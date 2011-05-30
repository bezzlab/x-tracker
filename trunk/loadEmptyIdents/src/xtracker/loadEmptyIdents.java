package xtracker;




import java.util.*;

public class loadEmptyIdents implements identData_loadPlugin
{


    /**
     * Plugin that does not load any identification.
     * @param inputData the xLoad data structure
     * @param paramFile an (empty) parameter file.
     * @return ret the xLoad structure as in input.
     */
    public xLoad start(xLoad inputData, String paramFile)
    {


     xLoad ret = inputData;
    



        return ret;
        }




    

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    public String getType()
    {

        return type;
    }

        public String getDescription()
    {

        return description;
    }


    Vector<String> identificationFiles = new Vector<String>();

    private final static String name = "Load empty identification file";
    private final static String version = "1.00";
    private final static String type = "IDENTDATA_LOAD_plugin";
    private final static String description = "This plugin loads no identifications. Useful if you want to quantitate without knowing peptide identifications.";


    //public static xLoad ret;

}