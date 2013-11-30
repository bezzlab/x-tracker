package uk.ac.cranfield.xTracker;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
//import javax.swing.filechooser.FileView;

//import cern.colt.function.DoubleDoubleFunction;
//import cern.colt.list.DoubleArrayList;
//import cern.colt.matrix.DoubleMatrix1D;
//import cern.colt.matrix.DoubleMatrix2D;
//import cern.colt.matrix.impl.DenseDoubleMatrix2D;
//import cern.colt.matrix.linalg.EigenvalueDecomposition;
//import cern.jet.stat.Descriptive;

import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The collection of useful tools which can be used in many situations, not only in this project
 * @author Jun Fan
 * @version 0.1
 */
public class Utils {

    /**
     * refer to http://www.velocityreviews.com/forums/t150785-java-swing-graphics-to-image.html
     *     and http://tips4java.wordpress.com/2008/10/13/screen-image/
     * two methods all work
     * currently have a bug, still to use ScreenImage.java
     */
    public static void saveJFrameToImage(JFrame frame, String suffix, String filename) {
//		System.out.println(java.util.Arrays.toString(ImageIO.getWriterFormatNames()));
        try {
            layoutComponent(frame);
            Rectangle rectangle = new Rectangle(frame.getBounds());
//			Rectangle rectangle = new Rectangle(frame.getX(),frame.getY(),frame.getWidth(),frame.getHeight());
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(rectangle);

//        int width = frame.getWidth();
//        int height = frame.getHeight();
//
//        BufferedImage image = new BufferedImage(width, height,
//                BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = image.createGraphics();
//        frame.paint(graphics);
//        graphics.dispose();

            ImageIO.write(image, suffix, new java.io.File(filename));
        } catch (Exception e) {
        }
    }

    static private void layoutComponent(Component component) {
        synchronized (component.getTreeLock()) {
            component.doLayout();
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    layoutComponent(child);
                }
            }
        }
    }

    /**
     * @return the euclidean distance between 1D matrices @param first and @param second
     */
//	public static double euclideanDistance(DoubleMatrix1D first,DoubleMatrix1D second){
//		if(first.size()!=second.size()) return Double.NaN;
//		double value = first.aggregate(second, new DoubleDoubleFunction(){
//			public double apply(double arg0, double arg1) {
//				return arg0+arg1;
//			}},
//			new DoubleDoubleFunction(){
//				public double apply(double arg0, double arg1) {
//					return (arg0-arg1)*(arg0-arg1);
//		}});
//		return Math.sqrt(value);
//	}
//    /**
//     * @return the mean of a double array @param arr
//     */
//    public static double mean(double[] arr) {
//        double sum = 0;
//        for (double d : arr) {
//            sum += d;
//        }
//        return sum / arr.length;
//    }
//
    public static ArrayList<Double> filter(List<Double> list){
        ArrayList<Double> filtered = new ArrayList<Double>();
        for(Double val:list){
            if (val == null || val.isNaN() || val.isInfinite()) continue;
            filtered.add(val);
        }
        return filtered;
    }

    public static Double mean(List<Double> list){
        ArrayList<Double> filtered = new ArrayList<Double>();
        if(filtered.isEmpty()) return null;
        return sum(filtered)/filtered.size();
    }

    public static Double median(List<Double> list) {
        ArrayList<Double> filtered = filter(list);
        if (filtered.isEmpty()) return null;
//            return Double.NaN;
        Collections.sort(filtered);
        int len = filtered.size();
        int middle = len / 2;
        if (len % 2 == 1) {
            return filtered.get(middle);
        } else {
            return (filtered.get(middle - 1) + filtered.get(middle)) / 2.0;
        }
    }

    public static Double sum(List<Double> list) {
        ArrayList<Double> filtered = filter(list);
        if(filtered.isEmpty()) return null;
        double ret = 0;
        for(double d:filtered){
            ret += d;
        }
        return ret;
    }

    public static double log2(double x) {
        return Math.log(x)/Math.log(2);
    }

    /**
     * a static method to return @return a file chooser starting from @param path which would require
     * a confirmation when overwriting an existing file
     */
    public static FileChooserWithGenericFileFilter createOverwriteFileChooser(String path) {
        return new FileChooserWithGenericFileFilter(path) {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                            "The selected file already exists. "
                            + "Do you want to overwrite it?",
                            "The file already exists",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                    }
                }
                super.approveSelection();
            }
        };
    }

    /**
     * do a PCA analysis on @param matrix which is centred and
     * @return the object which contains eigen value and vectors
     */
//	public static EigenvalueDecomposition PCA(DoubleMatrix2D matrix){
//		final int col = matrix.columns();
//		final int row = matrix.rows();
//		//1. calculate the mean
//		double [] columnMean = new double [col];
//		for (int i=0; i< col; i++){
//			columnMean[i] = matrix.viewColumn(i).zSum()/row;
//		}
//
//		//2. center the matrix
//		for (int i=0;i<row; i++)
//			for (int j=0; j< col; j++)
//				matrix.set(i, j, matrix.get(i,j) - columnMean[j]);
////		System.out.println("centered matrix: " + matrix + "\n");
//
//		//3. compute the covariance matrix
//		DoubleMatrix2D cov = new DenseDoubleMatrix2D(col,col);
//		for(int i=0;i<col;i++){
//			DoubleArrayList first = new DoubleArrayList(matrix.viewColumn(i).toArray());
//			for(int j=0;j<col;j++){
//				DoubleArrayList second = new DoubleArrayList(matrix.viewColumn(j).toArray());
//				cov.set(i, j, Descriptive.covariance(first, second));
//			}
//		}
////		System.out.println("Covariance matrix: " + cov + "\n");
////
//		//4. Compute eigenvalues and eigenvectors of the covariance matrix.
//		EigenvalueDecomposition evd = new EigenvalueDecomposition(cov);
//		return evd;
//	}
    /**
     * @return a {@link Set} containing the combination of the two arguments @param a and @param b of type @param <T>
     */
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.addAll(b);
        return result;
    }

    /**
     * @return a {@link Set} containing the common elements between the two arguments @param a and @param b of type @param <T>
     */
    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.retainAll(b);
        return result;
    }

    /**
     * @return a {@link Set} performing a subtract of the subset @param b elements from the superset @param a of type @param <T>
     */
    public static <T> Set<T> difference(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.removeAll(b);
        return result;
    }

    /**
     * @return a {@link Set} containing all the elements that are not in the intersection
     */
    public static <T> Set<T> complement(Set<T> a, Set<T> b) {
        return difference(union(a, b), intersection(a, b));
    }

    /**
     * Get the extension of a file.
     * @param f The file to get the extension from
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * fix @param component to the size of @param dimension
     */
    public static void fixSize(JComponent component, Dimension dimension) {
        component.setMinimumSize(dimension);
        component.setMaximumSize(dimension);
        component.setPreferredSize(dimension);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
//    	The getResource method causes the class loader to look through
//    	the directories and JAR files in the program's class path,
//    	returning a URL as soon as it finds the desired file
        java.net.URL imgURL = Utils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * @return an ImageIcon with given width @param width and height @param height from the location @param path
     */
    protected static ImageIcon createImageIcon(String path, int width, int height) {
        ImageIcon icon = createImageIcon(path);
        if (path == null) {
            return null;
        }
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /**
     * @return an ImageIcon with @param ratio proportional size pf the original ImageIcon from the location @param path
     */
    protected static ImageIcon createImageIcon(String path, double ratio) {
        ImageIcon icon = createImageIcon(path);
        if (path == null) {
            return null;
        }
        return new ImageIcon(icon.getImage().getScaledInstance((int) (icon.getIconWidth() * ratio), (int) (icon.getIconHeight() * ratio), Image.SCALE_SMOOTH));
    }

    /**
     * A utility method for drawing rotated text.
     * <P>
     * A common rotation is -Math.PI/2 which draws text 'vertically' (with the top of the
     * characters on the left).
     *
     * @param text The text.
     * @param g2 The graphics device.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param rotation The clockwise rotation (in radians).
     */
    public static void drawRotatedString(String text, Graphics2D g2,
            float x, float y, double rotation) {

        AffineTransform saved = g2.getTransform();
        AffineTransform rotate = AffineTransform.getRotateInstance(rotation, x, y);
        g2.transform(rotate);
        g2.drawString(text, x, y);
        g2.setTransform(saved);

    }

    /**
     * @param c
     * @param str
     * @return The length of string @param str needed in the component @param c
     */
    public static int getStringLength(JComponent c, String str) {
        return c.getFontMetrics(c.getFont()).stringWidth(str);
    }

    /**
     * @param g2
     * @param str
     * @return The length of String @param str drawn in the JAVA2D
     */
    public static int getStringLength(Graphics2D g2, String str) {
        return g2.getFontMetrics().stringWidth(str);
    }

    /**
     * In Excel or some other application, the Date is represented in the format of an integer
     * @param args the integer format of the date
     * @return the string format of the date
     * @throws Exception
     */
    public static String getDateFromNumber(String args) throws Exception {
        int number = new Integer(args).intValue(); // ;38353;

        final int day_years1 = 366; //leap year
        final int day_years2 = 365; //non leap year
        final int day_month31 = 31;
        final int day_month28 = 28;
        final int day_month29 = 29;
        final int day_month30 = 30;
        int feb = 0;

        boolean check = true;

        int years = 0;
        int months = 1;
        int days = 1;
        int local = 0;

        String Fmt1Month[] = {
            "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
//    	String Fmt2Month [] ={
//    			"0","1","2","3","4","5","6","7","8","9","10","11"};

        /* CHECK HOW MANY YEARS ARE
         * THERE AFTER JAN 1 1900, IS DAY  0
         */

        while (check) {
            if (years % 4 == 0) {
                if (number - day_years1 >= 0) {
                    number = number - day_years1;
                    years++;
                }
                local = day_years1;
            } else {
                if (number - day_years2 >= 0) {
                    number = number - day_years2;
                    years++;
                }
                local = day_years2;
            }

            //check the days left in a year
            if (number <= local) {
                days = number;
                check = false;
                break;
            }
        }

        /** Check the days left after
         *  the years have been extracted
         */
        if (years % 4 == 0) {
            feb = day_month29;
        } else {
            feb = day_month28;
        }
        for (int k = 1; k < 13; k++) {
            switch (k) {
                case 1:
                    ;  //jan
                case 3:
                    ;  //mar
                case 5:
                    ;  //may
                case 7:
                    ;  //jul
                case 8:
                    ;  //aug
                case 10:
                    ;  //oct
                case 12:
                    if (days - day_month31 > 0) {
                        days = days - day_month31;
                        months++;
                    }
                    break;
                case 2:
                    if (days - feb > 0) {
                        days = days - feb;
                        months++;
                    }
                    break;
                case 4:
                    ;
                case 6:
                    ;
                case 9:
                    ;
                case 11:
                    if (days - day_month30 > 0) {
                        days = days - day_month30;
                        months++;
                    }
                    break;
            } //END SWITCH
        }

        years = 1900 + years; //very important

        System.out.println("YEAR :" + years);
        System.out.println("MONTH :" + months);
        System.out.println("DAY :" + days);
        System.out.println(days + "-" + Fmt1Month[months - 1] + "-" + years);

        //Calendar rightNow = Calendar.getInstance();
        //rightNow.set(years,months,days);

        // SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        // String dt  = formatter.format(new Date(days+"-"+Fmt1Month[months-1]+"-"+years));
        // DateFormat df = DateFormat.getDateInstance();
        // Date date = df.parse("07/10/96 4:5 PM, PDT") ;
        // Date date = df.parse("\""+days+"/"+months+"/"+years+"\"");
        //System.out.println(date);
        return days + "-" + Fmt1Month[months - 1] + "-" + years;
    }

    /**
     * Save the string @param saveStr into file @param filename
     * @return the status whether the file is saved successfully
     * can be combined with other method, like @see {@link #convertToXML(Object)}
     * @see #saveObjectToXMLFile(String, Object)
     */
    public static boolean saveStringToFile(String filename, String saveStr) {
        boolean saved = false;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filename));
            try {
                bw.write(saveStr);
                saved = true;
            } finally {
                bw.close();
            }
        } catch (IOException ex) {
        }
        return saved;
    }

    /**
     * @return the string saved in the file @param filename
     */
    public static String getStringFromFile(String filename) {
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(filename));
            try {
                String s;
                while ((s = br.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                br.close();
            }
        } catch (IOException ex) {
        }
        return sb.toString();
    }

    /**
     * @return the XML string of the converted @param object using the given @param alias setting instead of class name
     */
    public static String convertToXML(Object obj, HashMap<String, Class<?>> alias) {
//		XStream xstream = new XStream(new DomDriver());
        XStream xstream = new XStream();
        xstream.setMode(XStream.ID_REFERENCES);
        if (alias != null) {
            for (Iterator<String> it = alias.keySet().iterator(); it.hasNext();) {
                String str = it.next().toString();
                xstream.alias(str, alias.get(str));
            }
        }
//		xstream.autodetectAnnotations(true);
//		xstream.omitField(WorksetTableModel.class, "sheet");
//		xstream.omitField(Project.class, "worksets");
//		xstream.processAnnotations(Project.class); //in the corresponding class, use @XStreamAlias("Name") even can apply for the class name as alias
        return xstream.toXML(obj);
    }

    /**
     * @return the XML string of the converted @param object
     */
    public static String convertToXML(Object obj) {
        return convertToXML(obj, null);
    }

    /**
     * @return the corresponding object converted from @param XMLStr
     */
    public static Object convertFromXML(String XMLStr) {
        return convertFromXML(XMLStr, null);
    }

    /**
     * @return the corresponding object converted from @param XMLStr which used alias when saved.
     * @see {@link #convertFromXML(String, HashMap)}
     */
    public static Object convertFromXML(String XMLStr, HashMap<String, Class> alias) {
//		MyLibrary ml = null;
//		XStream xstream = new XStream(new DomDriver());
        XStream xstream = new XStream();
        xstream.setMode(XStream.ID_REFERENCES);
        if (alias != null) {
            for (Iterator it = alias.keySet().iterator(); it.hasNext();) {
                String str = it.next().toString();
                xstream.alias(str, alias.get(str));
            }
        }
//		xstream.autodetectAnnotations(true);
        Object obj = xstream.fromXML(XMLStr);
        return obj;
    }

    public static boolean saveObjectToXMLFile(String filename,
            Object obj, HashMap<String, Class<?>> alias) {
        return saveStringToFile(filename, convertToXML(obj, alias));
    }

    public static boolean saveObjectToXMLFile(String filename,
            Object obj) {
        return saveStringToFile(filename, convertToXML(obj));
    }

    public static Object getObjectFromXMLFile(String filename) {
        return convertFromXML(getStringFromFile(filename));
    }

    public static Object getObjectFromXMLFile(String filename, HashMap<String, Class> alias) {
        return convertFromXML(getStringFromFile(filename), alias);
    }

    public static String getPath(String fullname){
        int location  = fullname.lastIndexOf("/");
        if (location >=0) return fullname.substring(0,location);
        location =  fullname.lastIndexOf("\\");
        if (location == -1) return "";
        return fullname.substring(0,location);
    }

    public static String getFilename(String fullname){
        int location  = fullname.lastIndexOf("/");
        if (location >=0) return fullname.substring(location+1);
        location =  fullname.lastIndexOf("\\");
        if (location == -1) return fullname;
        return fullname.substring(location+1);
    }

    public static String locateFile(String basename,ArrayList<String> folders){
        if (basename.length()==0) return "";
        File file = new File(basename);
        if(file.exists()) return basename;
        for(String folder:folders){
            String filename = folder+"/"+basename;
            file = new File(filename);
            if(file.exists()) return filename;
        }
        //not found so far, give the final attempt that use the file name of basename plus the working folder, which is always the first element in the folders
        //maybe needed, but will be only available after the extensive test and it is really needed
//        String filename = folders.get(0)+"/"+getFilename(basename);
//        file = new File(filename);
//        if(file.exists()) return filename;
        System.out.println("No such file "+basename+" exists in the following folder(s):");
        System.out.println(folders.toString());
        System.exit(1);
        return null;
    }
}

class FileChooserWithGenericFileFilter extends JFileChooser {

    FileChooserWithGenericFileFilter() {
        super();
    }

    FileChooserWithGenericFileFilter(File file) {
        super(file);
    }

    FileChooserWithGenericFileFilter(String dir) {
        super(dir);
    }

    File getFullSaveSelectedFile() {
        String filename = getSelectedFile().toString();
        String[] fileExts = ((GenericFileFilter) getFileFilter()).getFileExts();
        String ext = fileExts[0];
        if (!filename.endsWith(ext)) {
            filename += ".";
            filename += ext;
        }
        return new File(filename);
    }
}