
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.parsers
//    File: XsdPrimativeDataType.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.parsers;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author andrew bullimore
 */
public enum XsdPrimativeDataType {

    STRING("string", String.class),
    BYTE("byte", Byte.class),
    DECIMAL("decimal", Double.class),
    INT("int", Integer.class),
    INTEGER("integer", Integer.class),
    POSITIVEINTEGER("positiveInteger", Integer.class),
    NEGATIVEINTEGER("negativeInteger", Integer.class),
    NONPOSITIVEINTEGER("nonPositiveInteger", Integer.class),
    NONNEGATIVEINTEGER("nonNegativeInteger", Integer.class),
    LONG("long", Long.class),
    SHORT("short", Short.class),
    DOUBLE("double", Double.class),
    FLOAT("float", Float.class),
    BOOLEAN("boolean", Boolean.class),
    UNKNOWN("unknown", Object.class);

    private final String primativeAsString;
    private final Class primativeClassType;
    private static final Map<String, XsdPrimativeDataType> primTypeByString = new HashMap<String, XsdPrimativeDataType>();
    private static final Map<Class, XsdPrimativeDataType> primTypeByClass = new HashMap<Class, XsdPrimativeDataType>();

    static {

        for(XsdPrimativeDataType type : EnumSet.allOf(XsdPrimativeDataType.class)) {

            primTypeByString.put(type.getPrimativeAsString(), type);
            primTypeByClass.put(type.getPrimativeAsClass(), type);
        }
    }

    /**
     *
     *
     * 
     * @param primAsString
     * @param primAsClass
     */
    XsdPrimativeDataType(String primAsString, Class primAsClass) {

        primativeAsString = primAsString;
        primativeClassType = primAsClass;
    }

    @Override
    public String toString() {

        return primativeAsString;
    }

    /**
     *
     *
     * @return
     */
    public String getPrimativeAsString() {

        return primativeAsString;
    }

    /**
     *
     *
     * @return
     */
    public Class getPrimativeAsClass() {

        return primativeClassType;
    }

    /**
     *
     * @param typeAsString 
     * @return
     */
    public static XsdPrimativeDataType getPrimDataTypeEnumFromString(String typeAsString) {

        XsdPrimativeDataType primDataType = XsdPrimativeDataType.UNKNOWN;

        if(primTypeByString.containsKey(typeAsString)) {

            primDataType = (XsdPrimativeDataType) primTypeByString.get(typeAsString);
        }

        return primDataType;
    }

    /**
     *
     * @param typeAsClass
     * @return
     */
    public static XsdPrimativeDataType getPrimDataTypeEnumFromClass(Class typeAsClass) {

        XsdPrimativeDataType primDataType = XsdPrimativeDataType.UNKNOWN;

        if(primTypeByClass.containsKey(typeAsClass)) {

            primDataType = (XsdPrimativeDataType) primTypeByClass.get(typeAsClass);
        }

        return primDataType;
    }
}
