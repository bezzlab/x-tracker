

package xtracker.plugins.rawdataLoad.misc;

/*
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

Contact: Alexandros Georgiou <a.georgiou (at) uu (dot) nl>
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**Part of an MzML reader that handles zlib decompression, and unpacking of
 * various data types. E.g. to get an array of doubles from a ZLIB stream of
 * m/z values:
 * 
 * <pre>
 *      double[] mz = unpackFloat64(inflate(data));
 * </pre>
 * 
 * Note that unpacking of 16-bit floats has not been tested.
 *
 * @author a.georgiou@uu.nl
 */
public class ToolmzML {


    static private Inflater inflater = new Inflater();

    static protected byte[] inflate(byte[] data) throws DataFormatException {
            //inflate data into chunks of size equal to compressed data length
            List<byte[]> byteArrays = new ArrayList<byte[]>();

            inflater.reset();
            inflater.setInput(data);
            byte[] buffer = new byte[data.length];
            int byteCount = 0;
            while (!inflater.finished()) {
                byteCount += inflater.inflate(buffer);
                byteArrays.add(buffer);
                buffer = new byte[data.length];
            }
            //logger.info("ZLIB: "+data.length+" bytes --> "+byteCount+" bytes");
            return byteConcat(byteArrays,byteCount);
    }

    /**Concatenates a list of byte arrays into one byte array
     * 
     * @param byteArrays The list of byte arrays to concatenate
     * @param byteCount The sum of the lengths of all the arrays
     * @return The concatenated bytes
     */
    protected static byte[] byteConcat(List<byte[]> byteArrays, int byteCount) {
            byte[] resultBytes = new byte[byteCount];

            //concatenate all the chunks into the result array
            int offset =0;
            for (byte[] b:byteArrays) {
                int amount = Math.min(b.length, byteCount-offset);
                System.arraycopy(b, 0, resultBytes, offset, amount);
                offset += amount;
            }
            return resultBytes;
    }

    /* Converters */
    protected static short[] toShortArray(int[] data) {
        short[] result = new short[data.length];
        for (int i=0; i<data.length; i++)
            result[i] = (short) data[i];
        return result;
    }

    protected static short[] toShortArray(long[] data) {
        short[] result = new short[data.length];
        for (int i=0; i<data.length; i++)
            result[i] = (short) data[i];
        return result;
    }

    protected static short[] toShortArray(double[] data) {
        short[] result = new short[data.length];
        for (int i=0; i<data.length; i++)
            result[i] = (short) data[i];
        return result;
    }

    protected static double[] toFloatArray(int[] data) {
        double[] result = new double[data.length];
        for (int i=0; i<data.length; i++)
            result[i] = data[i];
        return result;
    }

    protected static double[] toFloatArray(long[] data) {
        double[] result = new double[data.length];
        for (int i=0; i<data.length; i++)
            result[i] = data[i];
        return result;
    }

    protected static double[] unpackFloat64(byte[] bytes) {
        double[] values = new double[bytes.length/8];
        int count=0;
        for (int i=0; i<(bytes.length-1); i+=8) {
            long bits = 0;
            for (int j=7; j>=0; j--)
                bits = (bits << 8) | (bytes[i+j] & 0xff);
            values[count] = Double.longBitsToDouble(bits);
            count++;
        }
        return values;
    }

    protected static double[] unpackFloat32(byte[] bytes) {
        double[] values = new double[bytes.length/4];
        int count = 0;
        for (int i=0; i<(bytes.length-1); i+=4) {

            int bits =0;
            for (int j=3; j>=0; j--)
                bits = (bits << 8) | (bytes[i+j] & 0xff);
            values[count]= (double) Float.intBitsToFloat(bits);
            count++;
        }
        return values;
    }

    /**Decode a 16-bit float. In IEEE 754r, 16-bit floats are made of:
     *      1 bit sign
     *      4 bit exponent
     *      11 bit mantissa
     * (from http://www.validlab.com/754R/drafts/archive/2006-10-04.pdf)
     * 
     * @param bytes The bytes in intel ordering (MSB in higher address)
     * @return The equivalent array of doubles
     */
    protected static double[] unpackFloat16(byte[] bytes) {
        //TODO test this with actual values
        double[] values = new double[bytes.length/2];

        int count=0;
        for (int i=0; i<(bytes.length-1); i+=2) {
            
            int exponent = bytes[i+1] & 0x78;                   //select bits 14 through 11
            int mantissa = ((bytes[i+1] & 0x7)<<8) | bytes[i];  //select bits 10 through 0
            double value = mantissa * Math.pow(2, exponent);
            
            if ( (bytes[i+1]&0x80)!=0 ) //test bit 15 (MSB)
                value = - value;
            values[count] = value;
            count++;
        }
        return values;
    }

    protected static int[] unpackInt32(byte[] bytes) {
        //TODO test for correct endianness 
        int[] values = new int[bytes.length/4];
        int count=0;
        for (int i=0; i<(bytes.length-1); i+=4) {
            //pack 4 bytes into an int
            int bits =0;
            for (int j=3; j>=0; j--)
                bits = (bits << 8) | (bytes[i+j] & 0xff);
            //store
            values[count] = bits;
        }
        return values;
    }

    protected static long[] unpackInt64(byte[] bytes) {
        //TODO test for correct endianness 
        long[] values = new long[bytes.length/8];
        int count=0;
        for (int i=0; i<(bytes.length-1); i+=8) {
            //pack 4 bytes into an int
            int bits =0;
            for (int j=3; j>=0; j--)
                bits = (bits << 8) | (bytes[i+j] & 0xff);
            //store
            values[count] = bits;
        }
        return values;
    }
    
    
    // =================================================================
    // methods added by Laurie Tonon for X-Tracker
    //===================================================================
    
    /**
     * method to check if an mzML file in indexed.
     * The method open the file and check the second line, where the tag "indexedmzML" should be
     * @param filename the complete path to the file to read
     * @return true if the file is indexed, false either
     */
    protected static boolean isIndexed(String filename){
        boolean indexed=false;
        try{
                     //open the file 
			InputStream ips=new FileInputStream(filename); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
                        
                        //read the first line
			ligne=br.readLine();
                        // read the second line
                        ligne=br.readLine();
                        // if the tag "indexedmzML" is present, the file is indexed
                        if(ligne.contains("indexedmzML")){ 
                            indexed=true;
                        }
                        
			br.close(); 
		}		
		catch (Exception e){
			System.err.println("ERROR: an error occured reading the file "+ filename + ": "+e.toString());
                        System.exit(1);
		}
        return indexed;
    }
    
    /**
     * method the choose the right parser to read a file.
     * Choose the dom parser if the file is small enough. If not, test if it is indexed:
     * If indexed, choose the SAX parser, if not, impossible to parse
     * @param filename the complete path to the file to read
     * @return 0 for dom parser, 1 for sax parser
     */
    public static int choiceParser(String filename){
        int parseNB=-1;
        
        File file= new File(filename);
        
        if(file.length()<=10000000){
           parseNB=0;
        }
        else{
            boolean indexed=isIndexed(filename);
            
            if(indexed){
                parseNB=1;
            }
            else{
                System.err.println("ERROR: The file " + filename + " does not have an index of offsets and is too big to be read.\n Please modify your file to add an index.");
                System.exit(1);
            }
        }
        return parseNB;
    }
}
