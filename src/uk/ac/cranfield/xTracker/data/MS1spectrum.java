package uk.ac.cranfield.xTracker.data;

/**
 *
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
    public int compareTo(MS1spectrum spec){
        return (int)(this.rt-spec.rt);
    }
}
