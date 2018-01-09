/*
 * Copyright 1999 - 2013 Herb Bowie
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
   A source of parsed XML passed back as DataRecord objects. Each piece of
   data is passed back as a separate record, identifying all of its containing
   tags. <p>
  
   This code is copyright (c) 2003 by Herb Bowie.
   All rights reserved. <p>
  
   Version History: <ul><li>
    2003/12/15 - Originally written.
  
       </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
   @version 
    2003/12/15 - Originally written.

 */

public class XMLParser 
  extends XTextFile 
    implements DataSource {
    
  public  final static String   NAME   = "Name";
  public  final static String   DATA   = "Data";

	private ArrayList             name = new ArrayList();
  
  private int                   maxDepth = 3;
  
  private int                   nameLevel = 0;
          
  private String								line;
	private int										lineLength;
	private int										lineIndex;
  
  private char                  nextChar;
  private char                  lastChar1;
  private char                  lastChar2;
  
  private StringBuilder          word;
  private int                   wordType          = UNDEFINED_TYPE;
  private static final int        UNDEFINED_TYPE  = -1;
  private static final int        COMMENT_TYPE    = 0;
  private static final int        NAME_TYPE       = 1;
  private static final int        DATA_TYPE       = 2;
  private static final int        END_NAME_TYPE   = 3;
  
  private int                   nullDataTag       = 0;
  private String                saveName;
  
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
  public XMLParser (String inFileName) {
    super (inFileName);
    commonConstruction();
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.
     
     @param inFile  The text file itself.
   */
  public XMLParser (File inFile) {
    super (inFile);
    commonConstruction();
  }
  
  /**
     A constructor that accepts a URL pointing to a Web resource.
     
     @param url  The URL of a text file.
   */  
  public XMLParser (URL url) {
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
     Opens the XML file for subsequent input. 
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput () 
      throws IOException {
    this.dict = new DataDictionary();
    openForInput (dict);
  }

  /**
     Opens the XML file for subsequent input. 
    
     @param  recDef Record Definition with dictionary to be used.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput (RecordDefinition recDef) 
      throws IOException {
    openForInput (recDef.getDict());
  }
    
  /**
     Opens the XML file for subsequent input. 
    
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
    for (int i = 0; i <= maxDepth; i++) {
      storeName (i, "");
      name.add ("");
    }
    storeField (DATA, "");
    nextChar = ' ';
    nameLevel = 0;
    getNextLine();
		getNextCharacter();
    recordNumber = 0;
  }
  
  /**
     Retrieve the next piece of data.
    
     @return formatted datum.
   */
  public DataRecord nextRecordIn () 
      throws IOException, FileNotFoundException {
        
    dataRec = new DataRecord();
    
    do {
      getNextWord();
      if (wordType == NAME_TYPE) {
        if (nameLevel < 0) {
          nameLevel = 0;
        }
        if (nameLevel < name.size()) {
          name.set (nameLevel, word.toString());
        } else {
          name.add (word.toString());
        }
        nameLevel++;
      } 
      else
      if (wordType == END_NAME_TYPE) {
        if (nameLevel > 0) {
          nameLevel--;
        }
      }
    } while ((! isAtEnd())
        && ((wordType != DATA_TYPE)
            || (nameLevel <= 0)));
    
    if (wordType == DATA_TYPE
        && nameLevel > 0) {

      // populate the data record
      for (int i = 0; i <= maxDepth; i++) {
        if (i < nameLevel) {
          storeName (i, (String)name.get (i));
        } else {
          storeName (i, "");
        }
      }
      storeField (DATA, word.toString());
    }

    recordNumber++;
    return dataRec;
  }
  
  /**
     Adds a name field to the record being built. 
    
     @return The column number assigned to the field.
     @param  level     The level of the field.
     @param  value     The data value assigned to the field. 
   */
  int storeName (int level, String value) {
    String fieldName = NAME + String.valueOf (level + 1);
    int column = recDef.getColumnNumber (fieldName);
    if (column < 0) {
      column = recDef.addColumn (fieldName);
    }
    column = dataRec.storeField (recDef, fieldName, value);
    return column;
  }
  
  
  /**
     Adds a field to the record being built. 
    
     @return The column number assigned to the field.
     @param  fieldName The name of the field.
     @param  value     The data value assigned to the field. 
   */
  int storeField (String fieldName, String value) {
    int column = recDef.getColumnNumber (fieldName);
    if (column < 0) {
      column = recDef.addColumn (fieldName);
    }
    column = dataRec.storeField (recDef, fieldName, value);
    return column;
  }
	
	/**
	   Get next word.
	 */
	void getNextWord () 
			throws IOException, FileNotFoundException {
        
    wordType = -1;
    
    if (nullDataTag == 2) {
      wordType = 2;
      word = new StringBuilder();
      nullDataTag = 3;
    }
    else
    if (nullDataTag == 3) {
      wordType = 3;
      word = new StringBuilder (saveName);
      nullDataTag = 0;
    } else {
    
      word = new StringBuilder();

      skipWhiteSpace();

      if (nextChar == '<') {
        getNextCharacter();
        if (nextChar == '?') {
          wordType = COMMENT_TYPE;
        }
        else
        if (nextChar == '/') {
          wordType = END_NAME_TYPE;
          getNextCharacter();
        } else {
          wordType = NAME_TYPE;
        }
      } else {
        wordType = DATA_TYPE;
      }

      skipWhiteSpace();

      while ((! isAtEnd())
          && (! ((wordType == COMMENT_TYPE)
              && (nextChar == '>')
              && (lastChar1 == '?')))
          && (! ((wordType == DATA_TYPE)
              && (nextChar == '<')))
          && (! ((wordType == NAME_TYPE)
              && (nextChar == '>'))) 
          && (! ((wordType == END_NAME_TYPE)
              && (nextChar == '>')))) {
        if (wordType == NAME_TYPE
            && nextChar == '/'
            && word.length() > 0) {
          nullDataTag = 2;
          saveName = word.toString().trim();
        } else {
          word.append (nextChar);
        }
        getNextCharacter();
      }

      if (nextChar == '>') {
        getNextCharacter();
      }
    }
    
    
	} // end getNextWord method
  
  /**
    Skip white space (spaces, tabs, carriage returns and line feeds)
   */
  private void skipWhiteSpace () 
			throws IOException {
    while ((! isAtEnd())
        && ((nextChar == GlobalConstants.SPACE)
            || (nextChar == GlobalConstants.CARRIAGE_RETURN)
            || (nextChar == GlobalConstants.LINE_FEED)
            || (nextChar == GlobalConstants.TAB))) {
      getNextCharacter();
    }
  }
  
  private static String charToString (char c) {
    if (Character.isISOControl (c)) {
      return String.valueOf (Character.getNumericValue (c));
    } else {
      return String.valueOf (c);
    }
  }

	/**
	   Ready next character for processing.
	 */
	void getNextCharacter()
			throws IOException, FileNotFoundException {
		lastChar2 = lastChar1;
		lastChar1 = nextChar;
		if (lineIndex >= lineLength) {
			nextChar = GlobalConstants.LINE_FEED;
			getNextLine();
		}
		else {
			nextChar = line.charAt (lineIndex);
      lineIndex++;
		}
	} // end getNextCharacter method

	/**
	   Ready next input text line for processing.
	 */
	void getNextLine()
			throws IOException, FileNotFoundException {
		line = readLine();
		lineLength = line.length();
		lineIndex = 0;
	}
  
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
    this.maxDepth = maxDepth;
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

