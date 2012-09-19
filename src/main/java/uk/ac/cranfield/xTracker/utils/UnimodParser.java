package uk.ac.cranfield.xTracker.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jun Fan@cranfield
 */
public class UnimodParser {
    private static HashMap<String,String> unimodID = null;
    private static HashMap<String,Double> unimodShift = null;
    private static final String UNIMOD = "unimod.obo";
    /**
     * retrieve the UNIMOD accession according to the given modification name
     * @param name e.g. iTraq4plex
     * @return UNIMOD accession, e.g. UNIMOD:214
     */
    public static String getUnimodID(String name){
        if(unimodID == null){
            initialize();
        }
        
        return unimodID.get(name);
    }
    /**
     * retrieve the mass shift value in UNIMOD according to the given modification name
     * @param name the modification name
     * @return the mass shift
     */
    public static double getUnimodShift(String name){
        if(unimodShift == null){
            initialize();
        }
        return unimodShift.get(name);
    }
    

    private static void initialize() {
        unimodID = new HashMap<String, String>();
        unimodShift = new HashMap<String, Double>();
        try{
            InputStreamReader isr = new InputStreamReader(UnimodParser.class.getClassLoader().getResource(UNIMOD).openStream());
            BufferedReader in = new BufferedReader(isr);
            String line;
            //remove header
            while ((line = in.readLine()) != null) {
                if (line.startsWith("[Term]")) {
                    break;
                }
            }
            String name="";
            String id="";
            while ((line = in.readLine()) != null) {
                if (line.equals("[Term]")){
                    name = "";
                    id = "";
                }
                if(line.startsWith("id:")){
                    Pattern pattern = Pattern.compile("id:\\s+(UNIMOD:\\d+)");
                    Matcher m = pattern.matcher(line);
                    if (m.find()) {
                        id = m.group(1);
                    }
                }else if(line.startsWith("name:")){
                    Pattern pattern = Pattern.compile("name:\\s+(.+)");
                    Matcher m = pattern.matcher(line);
                    if (m.find()) {
                        name = m.group(1);
                    }
                }else if(line.startsWith("xref: delta_mono_mass")){
                    Pattern pattern = Pattern.compile("(\\d*\\.?\\d+)");
                    Matcher m = pattern.matcher(line);
                    if (m.find()) {
                        unimodID.put(name, id);
                        unimodShift.put(name, Double.parseDouble(m.group()));
                    }
                }
            }
            in.close();
        }catch(IOException e){
            System.out.println("Can not find the required file "+UNIMOD);
            System.exit(1);
        }catch(Exception e){
        }
    }
}
