package uk.ac.cranfield.xTracker.data;

/**
 * The interface of spectrum
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public interface xSpectrum {
    public double[] getMzData(String filename);
    public double[] getIntensityData(String filenames);
}
