package uk.ac.cranfield.xTracker.data;

/**
 * The class for the MS1 spectra
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class MS1spectrum implements Comparable<MS1spectrum> {
    private xSpectrum spectrum;
    private float rt;
    
    public MS1spectrum(xSpectrum specIn){
        this(specIn,0);
    }
    
    public MS1spectrum(xSpectrum specIn,float rtIn){
        spectrum = specIn;
        rt = rtIn;
    }

    public float getRt() {
        return rt;
    }

    public xSpectrum getSpectrum() {
        return spectrum;
    }
    
    @Override
    /**
     * Enable to order MS1 spectra according to the retention time
     */
    public int compareTo(MS1spectrum spec){
        return (int)(this.rt-spec.rt);
    }
}
