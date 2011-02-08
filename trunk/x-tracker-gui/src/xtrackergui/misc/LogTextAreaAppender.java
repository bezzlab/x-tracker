
//
//    xTrackerGui
//
//    Package: xtrackergui.misc
//    File: LogTextAreaAppender.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.misc;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Appender Class to append log messages to a JTextArea
 * see http://logging.apache.org/log4j/1.2/ for further
 * information
 *
 * @author andrew bullimore
 */
public class LogTextAreaAppender extends WriterAppender {

    // Text area where log messages appear
    static private JTextArea logInfoTextArea = null;

    /**
     * Sets the text area where logging messages are appended
     *
     * @param textArea The text area for log message output
     * 
     */
    static public void setLogInfoTextArea(JTextArea textArea) {

        LogTextAreaAppender.logInfoTextArea = textArea;
    }

    /**
     *
     *
     * @param loggingEvent See http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/spi/LoggingEvent.html
     *
     */
    @Override
    public void append(LoggingEvent loggingEvent) {

        final String message = this.layout.format(loggingEvent);

	SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                logInfoTextArea.append(message);
            }
       });
    }
}
