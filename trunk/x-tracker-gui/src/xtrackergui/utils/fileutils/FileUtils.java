
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.fileutils
//    File: FileUtils.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.fileutils;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.log4j.Logger;

/**
 * File utility class
 *
 * @author andrew bullimore
 */
public class FileUtils {

    // log4j logger
    private static Logger logger = Logger.getLogger(xtrackergui.gui.XTrackerGuiUIFrame.class.getName());

    /**
     * Private Default Constructor - utility class with static methods,
     * there is no requirement to create an instance of this class
     *
     */
    private FileUtils() {
    }

    @Override
    public String toString() {

        return "FileUtils class";
    }

    /**
     * Gets all the files in a given directory
     *
     * @param directoryPath Directory path
     * @return An array list of Files found
     */
    public static File[] getDirectoryListing(String directoryPath) throws FileNotFoundException {

        // Assume we are given a directory path
        File directory = new File(directoryPath);

        if(!directory.exists() && !directory.isDirectory()) {

            throw new FileNotFoundException("directory " + directoryPath + " not found");
        }
            
        return directory.listFiles();
    }

    /**
     * Returns a File, if found, from the given path
     *
     * @throws FileNotFoundException
     * @param path The path to the file
     * @return The file created
     */
    public static File getFile(String path) throws FileNotFoundException {

        File file = new File(path);

        if(!file.exists() && !file.isFile()) {

            throw new FileNotFoundException("file " + path + " not found");
        }

        return file;
    }

    /**
     * Removes the extension from the given file name
     *
     * @param fileName The file to remove the extension from
     * @return The file name without extension
     */
    public static String getFileNameWithoutExtension(String fileName) {

        String name = "";

        int index = fileName.lastIndexOf('.');

        if(index > 0 && index < fileName.length() - 1) {

            name = fileName.substring(0, index);
        }

        return name; // fileName.substring(0, fileName.lastIndexOf('.'));
    }

    /**
     * Return the file extension for the file in parameter file
     *
     * @param file The file to extract the extension from
     */
    public static String getFileNameExtension(File file) {

        String extension = "";

        String fileName = file.getName();

        int index = fileName.lastIndexOf('.');

        if(index > 0 &&  index < fileName.length() - 1) {

            extension = fileName.substring(index + 1).toLowerCase();
        }
        return extension;
    }

    /**
     * Removes the extension from the given file name and appends .xsd extension instead
     *
     * @param fileName The file name to change the extension of
     * @return The file name with a .xsd extension
     */
    public static String convertFileExtensionToXsd(String fileName) {

        String name = FileUtils.getFileNameWithoutExtension(fileName);

        return name + ".xsd";
    }

    /**
     * Find a xsd schema file matching the name of a plugin jar file
     *
     *
     * @throws FileNotFoundException
     * @param directoryPath The path to the xsd schema directory
     * @param jarFileName The jar file name
     * @return A xsd file
     */
    public static File getXsdSchemaFileFromJarFileName(String directoryPath, String jarFileName) throws FileNotFoundException {

        File xsdSchemaFile = null;

        // Swap the .jar extension for a .xsd extension
        String xsdSchemaFileName = FileUtils.convertFileExtensionToXsd(jarFileName);

        // make sure the directory path is set - can be an empty string
        if(directoryPath != null) {

            // Get the file - throws FileNotFoundException if can't find
            xsdSchemaFile = FileUtils.getFile(directoryPath + xsdSchemaFileName);
            
            if(logger.isDebugEnabled()) {

                    logger.debug("FileUtils::getXsdSchemaFile: xsd schema file - " + xsdSchemaFile.getAbsolutePath());
            }
        }

        return xsdSchemaFile;
    }
}
