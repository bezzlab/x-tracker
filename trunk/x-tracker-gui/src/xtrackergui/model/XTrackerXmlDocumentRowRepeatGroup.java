
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerXmlDocumentRowRepeatGroup.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author andrew bullimore
 */
public class XTrackerXmlDocumentRowRepeatGroup {

    /**
     *
     *
     */
    public enum RepeatGroupType{
        
        SINGLE("single"),
        MULTIPLESETRANGE("multiplesetrange"),
        MULTIPLESETNUMBER("multiplesetnumber"),
        MULTIPLEUNBOUNDED("multipleunbounded"),
        UNKNOWN("unkown");

        private String repeatGroupTypeString;

        RepeatGroupType(String type) {

            repeatGroupTypeString = type;
        }

        @Override
        public String toString() {

            return repeatGroupTypeString;
        }

        public String getRepeatGroupTypeString() {

            return repeatGroupTypeString;
        }
    };

    private RepeatGroupType groupType = RepeatGroupType.UNKNOWN;
    private String repeatGroupParent = null;
    private List<String> groupMembers = null;
    private int minOccurs;
    private int maxOccurs;
    
    /**
     * Create an xml document repeat group (a nested group of elements or a
     * single element which occurs once or multiple times) ))
     *
     * @param parent The name of the xsd element/ node - all nodes in the schema node map are assigned a repeat group
     * @param min The minimum number of occurrances
     * @param max The maximum number of occurrances
     */
    public XTrackerXmlDocumentRowRepeatGroup(String parent, int min, int max) {

        repeatGroupParent = parent;
        minOccurs = min;
        maxOccurs = max;

        if(max == -1) {

            // unbounded number of occurances
            groupType = RepeatGroupType.MULTIPLEUNBOUNDED;

        } else {

            if(maxOccurs > minOccurs) {

                // range of occurances from minOccurs to maxOccurs
                groupType = RepeatGroupType.MULTIPLESETRANGE;

            } else if((minOccurs != 1 && maxOccurs != 1 && minOccurs == maxOccurs) ||
                      minOccurs > maxOccurs) {

                // set number of occurances - always specify with minOccurs value
                groupType = RepeatGroupType.MULTIPLESETNUMBER;

            } else if(minOccurs == 1 && maxOccurs == 1) {

                groupType = RepeatGroupType.SINGLE;
            }
        }

        groupMembers = new ArrayList<String>();
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();

        if(repeatGroupParent != null) {

            outputString.append("\nRepeat Group - Parent: " + repeatGroupParent);
        }


        outputString.append(": type: " + groupType +
                            ": min occurs: " + minOccurs +
                            ": max occurs: " + maxOccurs);

        if(groupMembers != null) {

            for(String member : groupMembers) {

                outputString.append(": Group member: " + member);
            }

            outputString.append('\n');
        }

        outputString.append('\n');

        return outputString.toString();
    }

    /**
     * Get the repeat groups parent xsd element/ node name
     *
     * @return The xsd schema element/ node
     */
    public String getRepeatGroupParent() {

        return repeatGroupParent;
    }

    /**
     * Get the repeat groups parent xsd element/ node name
     *
     * @return The xsd schema element/ node
     */
    public RepeatGroupType getRepeatGroupType() {

        return groupType;
    }

    /**
     *
     *
     */
    List<String> getGroupMembers() {

        return groupMembers;
    }

    /**
     *
     *
     */
    int getGroupMemberCount() {

        return groupMembers.size();
    }

    /**
     *
     *
     */
    public boolean checkNodeNameIsInRepeatGroup(String name) {

        return groupMembers.contains(name);
    }

    /**
     *
     *
     */
    public void addNodeNameToRepeatGroup(String name) {

        groupMembers.add(name);
    }

    /**
     *
     *
     */
    public int getMinOccurs() {

        return minOccurs;
    }

    /**
     *
     *
     */
    public int getMaxOccurs() {

        return maxOccurs;
    }
}
