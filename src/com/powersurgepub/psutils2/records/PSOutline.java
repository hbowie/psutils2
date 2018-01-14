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

package com.powersurgepub.psutils2.records;

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.textio.*;

	import java.io.*;
  import java.net.*;
  import java.util.*;

/**
   A source of outline data passed back as
   DataRecord objects. <p>
  
   This code is copyright (c) 2005 by Herb Bowie.
   All rights reserved. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
   @version .

 */
public class PSOutline 
  extends XTextFile 
    implements DataSource {
  
  public  final static char     SPACE            = ' ';
  public  final static char     TAB              = '\t';
  public  final static String   SECTION_NUMBER   = "sectionnumber";
  public  final static String   HEADING_FLAG     = "headingflag";
  public  final static String   LEVEL_NUMBER     = "level";
  public  final static String   TEXT             = "text";
  public  final static String   LINK             = "link";
  public  final static int      MAX_LEVELS       = 10;
  
  private char                  bullet           = ' ';
  private int                   spacesPerLevel   = 0;
  private int                   level = 0;
  private int                   lastLevel = 0;
  private int                   sectionNumber[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  
  private boolean               headingflag = false;
          
  private String								line;
  private String                lineText;
  
	private int										lineLength;
	private int										lineIndex;
  private int                   lineType = 0;
  private final static int      NORMAL_LINE           = 0;
  private final static int      BLANK_LINE            = 1;
  private final static int      HEADING_LINE          = 2;
  private final static int      LINK_LINE             = 3;
  
  /** Data Record currently being built. */
  private DataRecord						dataRec;
  
  /** Data dictionary to be used for the file. */
  private DataDictionary    		dict;
  
  /** Record definition to be used for the file. */
  private RecordDefinition   		recDef;
          
  /** Path to the original source file (if any). */
  private	String                dataParent;
  
  /** Sequential number identifying last record read or written. */
  private int                		recordNumber = 0;
  
  // The following fields are used for logging
  
  /** Log to record events. */
  private  Logger       log;
  
  /** Do we want to log all data, or only data preceding significant events? */
  private  boolean      dataLogging = false;
  
  /** Data to be sent to the log. */
  private  LogData      logData;
  
  /** Events to be logged. */
  private  LogEvent     logEvent;
  
  /** Identifier for this file (to be printed in the log as a source ID). */
  private  String       fileId;
	  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name passed as a single String.
   */
  public PSOutline (String inFileName) {
    super (inFileName);
    commonConstruction();
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.
     
     @param inFile  The text file itself.
   */
  public PSOutline (File inFile) {
    super (inFile);
    commonConstruction();
  }
  
  /**
     A constructor that accepts a URL pointing to a Web resource.
     
     @param url  The URL of a text file.
   */  
  public PSOutline (URL url) {
    super (url);
    commonConstruction();
  }
  
  /**
     Common initialization for all constructors.
   */
  private void commonConstruction () {
    dataParent = this.getParent();
  }
  
  /**
     Opens the PSOutline text file for subsequent input. 
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput () 
      throws IOException {
    this.dict = new DataDictionary();
    openForInput (dict);
  }

  /**
     Opens the PSOutline text file for subsequent input. 
    
     @param  recDef Record Definition with dictionary to be used.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput (RecordDefinition recDef) 
      throws IOException {
    openForInput (recDef.getDict());
  }
    
  /**
     Opens the PSOutline text file for subsequent input. 
    
     @param  dict Data dictionary to be used by this file.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput (DataDictionary dict) 
      throws IOException {
    super.openForInput ();
    this.dict = dict;
    dataRec = new DataRecord();
    recDef = new RecordDefinition(this.dict);
    for (int i = 0; i < MAX_LEVELS; i++) {
      recDef.addColumn (SECTION_NUMBER + String.valueOf (i + 1));
    }
    recDef.addColumn (HEADING_FLAG);
    recDef.addColumn (LEVEL_NUMBER);
    recDef.addColumn (TEXT);
    recDef.addColumn (LINK);
    getNextLine();
    recordNumber = 0;
  }
  
  /**
     Retrieve the next piece of data.
    
     @return formatted datum.
   */
  public DataRecord nextRecordIn () 
      throws IOException, FileNotFoundException {
        
    // Get next non-blank line
    while (lineType == BLANK_LINE
        && (! isAtEnd())) {
      getNextLine();
    }
    
    dataRec = new DataRecord();    
    StringBuffer text = new StringBuffer();
    boolean headingFlag = false;
    StringBuffer link = new StringBuffer();
    
    while (level > lastLevel) {
      lastLevel++;
      sectionNumber[lastLevel] = 0;
    }
    while (level < lastLevel) {
      sectionNumber[lastLevel] = 0;
      lastLevel--;
    }
    
    boolean done = isAtEnd();
    while (! done) {
      switch (lineType) {
        case (NORMAL_LINE):
          if (headingFlag) {
            done = true;
          } else {
            if (text.length() > 0
                && text.charAt(text.length() - 1) != SPACE
                && text.charAt(text.length() - 1) != TAB) {
              text.append (SPACE);
            }
            text.append (lineText); 
          }
          break;
        case (BLANK_LINE):
          if (text.length() > 0) {
            done = true;
          }
          break;
        case (HEADING_LINE):
          if (text.length() == 0) {
            sectionNumber[level]++;
            text.append (lineText);
            headingFlag = true;
            getNextLine();
            if (lineType == LINK_LINE) {
              link.append (lineText);
              getNextLine();
            }
          } 
          done = true;
          break;
        case (LINK_LINE):
          link.append (lineText);
          break;
      }
      if (! done) {
        getNextLine();
      }
    } // end while processing lines for this record
    
    // populate the data record
    for (int i = 0; i < MAX_LEVELS; i++) {
      if (sectionNumber[i] == 0) {
        dataRec.addField (recDef, "");
      } else {
        dataRec.addField (recDef, String.valueOf (sectionNumber[i]));
      }
    }
    dataRec.addField (recDef, String.valueOf(headingFlag));
    dataRec.addField (recDef, String.valueOf(lastLevel + 1));
    dataRec.addField (recDef, text.toString());
    dataRec.addField (recDef, link.toString());

    recordNumber++;
    return dataRec;
  }
	
	/**
	   Ready next input text line for processing.
	 */
	void getNextLine()
			throws IOException, FileNotFoundException {
    lineType = NORMAL_LINE;
		line = readLine();
    // System.out.println ("next line = " + line);
		lineLength = line.length();
		int i = 0;
    level = 0;
    int spaces = 0;
    while (i < lineLength 
        && (line.charAt(i) == SPACE
            || line.charAt(i) == TAB)) {
      if (line.charAt(i) == TAB) {
        level++;
      } else {
        spaces++;
        if (spacesPerLevel > 0) {
          if (spaces >= spacesPerLevel) {
            level++;
            spaces = spaces - spacesPerLevel;
          } // end if we have enough spaces for a level
        } // end if we already know how many spaces are per level
      } // end if leading white space character is a space
      i++;
    } // end while more leading white space
    
    // Use number of leading spaces in first indented line
    // to establish number of spaces per level
    if (spacesPerLevel == 0
        && spaces > 0) {
      level++;
      spacesPerLevel = spaces;
    }
    
    // System.out.println ("line level = " + String.valueOf(level));
    
    if (i >= lineLength) {
      lineType = BLANK_LINE;
    } else {
      if (bullet == SPACE) {
        if (Character.isLetterOrDigit (line.charAt(i))) {
          // do nothing
        } else {
          bullet = line.charAt(i);
        }
      } // end if no bullet character yet
      if (line.charAt(i) == bullet) {
        i++;
        lineType = HEADING_LINE;
        while (i < lineLength
            && (line.charAt(i) == SPACE
                || line.charAt(i) == TAB)) {
          i++;
        } // end while more white space following bullet
      } // end if first non-blank character is a bullet character
      if (((i + 2) < lineLength)
          && (line.charAt(i) == 'a')
          && (line.charAt(i + 1) == ':')) {
        lineType = LINK_LINE;
        i = i + 2;
      } 
      else
      if (((i + 5) < lineLength)
          && (line.substring(i, i+5).equals("http:"))) {
        lineType = LINK_LINE;
      }
      lineText = line.substring(i);
    } // end if non-blank line
    // System.out.println ("line type = " + String.valueOf(lineType));
	} // end getNextLine method
  
  /**
     Returns the record definition for the file.
    
     @return Record definition for this tab-delimited file.
   */
  public RecordDefinition getRecDef () {
    return recDef;
  }
  
  /**
     Returns the record number of the last record
     read or written.
    
     @return Number of last record read or written.
   */
  public int getRecordNumber () {
    return recordNumber;
  }
  
  /**
     Sets maximum number of data levels to expect, and to return.
     Defaults to 3, if not explicitly set via this method.
   
     @param maxDepth Maximum number of data levels to expec in the XML,
                     and to return in the output. 
   */
  public void setMaxDepth (int maxDepth) {
    //
  }
  
  /**
     Retrieves the path to the original source file (if any).
    
     @return Path to the original source file (if any).
   */
  public String getDataParent () {
    if (dataParent == null) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return dataParent;
    }
  }
  
  /**
     Sets the Logger object to be used for logging. 
    
     @param log The Logger object being used for logging significant events.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  /**
     Gets the Logger object to be used for logging. 
    
     @return The Logger object being used for logging significant events.
   */
  public Logger getLog () {
    return log;
  } 
  
  /**
     Sets the option to log all data off or on. 
    
     @param dataLogging True to send all data read or written to the
                        log file.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
  /**
     Gets the option to log all data. 
    
     @return True to send all data read or written to the
             log file.
   */
  public boolean getDataLogging () {
    return dataLogging;
  }
  
  /**
     Sets the file ID to be passed to the Logger.
    
     @param fileId Used to identify the source of the data being logged.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }
  
}


