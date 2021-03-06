/*
 * Copyright 1999 - 2017 Herb Bowie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powersurgepub.psutils2.logging;

  import javafx.scene.control.*;

/**

   A complete set of all the possible logging destinations.  

   Allows a client program to easily switch between the various
   logging destinations. 
 */

public class LogJuggler {

  /** Requests null output when passed to setLogOutput. */
  public    static final String LOG_NONE_STRING     = "None";

  /** Requests System.out output when passed to setLogOutput. */
  public    static final String LOG_WINDOW_STRING   = "Window";

  /** Requests disk file output when passed to setLogOutput. */
  public    static final String LOG_DISK_STRING     = "Disk"; 

  /** Requests JTextArea output when passed to setLogOutput. */
  public    static final String LOG_TEXT_STRING   	= "Text";
  
  /** Requests System.out output when passed to setLogOutput. */
  public    static final String LOG_SYSOUT_STRING   	= "SysOut";

  /** Default program ID to pass to the logging objects. */
  private    String    programID = "LogJuggler";

  /** The logger being used. */
  private Logger         logger;

  /** A logging destination of a window. */
  private LogOutput      windowLog;

  /** A null logging destination. */
  private LogOutputNone  noLog;
  
  /** A logging destination of a disk file. */
  private LogOutputDisk  diskLog;

  /** A logging destination of a text area. */
  private LogOutputText  textLog;
  
  /** A logging destination of System.out. */
  private LogOutput      sysLog;

  /**
     The "noarg" constructor sets default values for all
     the necessary logging objects. The default logging destination is
     the standard System.out destination.
   */

  public LogJuggler () {
    setLogger (Logger.getShared());
  } // end LogJuggler constructor

  /**
     This constructor accepts a program ID, to be printed
     in the log.

     @param programID the name of the client program.
   */

  public LogJuggler (String programID) {
    setLogger (Logger.getShared());
    this.programID = programID;
  } 

  /** 
     Returns the current logging destination. 

     @return Current logging destination.
   */
  public LogOutput getLogOutput ()     { 
    return logger.getLogOutput(); 
  }

  /** 
     Returns the current logging object. 

     @return Current logging object.
   */
  public Logger   getLogger ()  { 
    return logger; 
  }

  /**
     Sets the logging destination based on the String
     literal passed.

     @param logString One of the following values: <ul><li>
                      LOG_NONE_STRING for null output <li>
                      LOG_DISK_STRING for disk output <li>
                      LOG_WINDOW_STRING for output to System.out <li>
	                    LOG_TEXT_STRING for output to a text area. <li>

                      All other values will also send output to System.out </ul>
   */
  public void switchLogOutput (String logString) {
    if (logString.equals (LOG_NONE_STRING)) {
      if (noLog == null) {
        noLog = new LogOutputNone();
      }
      logger.setLogOutput(noLog);
    } else
    if (logString.equals (LOG_DISK_STRING)) {
      if (diskLog == null) {
        diskLog = new LogOutputDisk (programID);
      }
      logger.setLogOutput(diskLog);
    } else 
		if (logString.equals (LOG_TEXT_STRING)
        && textLog != null) {
      logger.setLogOutput(textLog);
    } else
    if (logString.equals (LOG_SYSOUT_STRING)) {
      if (sysLog == null) {
        sysLog = new LogOutput ();
      }
      logger.setLogOutput(sysLog);
    } else {
      if (windowLog == null) {
        windowLog = new LogOutput ();
      }
      logger.setLogOutput(windowLog);
		} 
  }
  
  public void setLogger(Logger logger) {
    if (logger != null) {
      this.logger = logger;
    }
  }

  /**
     Sets the logging destination to the passed value.

     @param log A valid logging output destination.
   */
  public void setLogOutput (LogOutput logOutput) {
    logger.setLogOutput(logOutput);
  }
  
  public void setTextArea(TextArea textArea) {
    textLog = new LogOutputText(textArea);
    logger.setLogOutput(textLog);
  }

  /**
     Closes up logging operations, including all logs used.
   */

  public void close () {
    if (noLog != null) {
      noLog.close();
    }

    if (diskLog != null) {
      diskLog.close();
    }

    if (windowLog != null) {
      windowLog.close();
    }

		if (textLog != null) {
			textLog.close();
		}

  }
  
  /**
     Creates a LogEvent object and then records it.
    
     @param severity      the severity of the event
    
     @param message       the message to be written to the log
    
     @param dataRelated   indicates whether this event is related
                          to preceding data.
   */
  public void recordEvent (int severity, String message, boolean dataRelated) {
    logger.recordEvent(severity, message, dataRelated);
  }

  /**
     Returns printable representation of the object.

     @return A literal of "LogJuggler" plus the object ID.
   */
  public String toString () {
    return "LogJuggler " + super.toString();
  } // end toString method

} // end Logger class

