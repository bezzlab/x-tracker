/*  * To change this template, choose Tools | Templates  * and open the template in the editor.  */
package xtracker;
import java.util.*;
/**  *  * @author luca  */
public class series {
    series(float rtVal, float icVal){
        RT=rtVal;
        IC=icVal;
    }
    public float getRT(){
        return RT;
    }
    public float getIC(){
        return IC;
    }
    public float RT;
    public float IC;
}