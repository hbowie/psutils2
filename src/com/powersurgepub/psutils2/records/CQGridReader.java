/*
 * CQGridReader.java
 *
 * Created on April 14, 2006, 5:27 PM
 */

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
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;

/**
 *
 * @author  b286172
 */
public class CQGridReader
    extends     File
        implements  DataSource {
  
  public static final char    PIPE = '|';

  private   boolean           ok = true;
  private   FileReader        cqGridReader;
  private   BufferedReader    cqGridBufReader;
  private   int               row = 0;
  private   int               lastColumn = -1;
  private   int               column = 0;
  private   int               ix = 0;
  private   int               num = 0;
  private   boolean           endOfRecord = false;
  private   boolean           endOfFile = false;
  private   char[]            chars = new char[1000];

  /** The logger to use to log events. */
  private    Logger           log;

  /* Let's not log all data. */
  private    boolean          dataLogging = false;

  /** The data dictionary to be used by this record. */
  private    DataDictionary   dict;

  /** The record definition to be used by this record. */
  private    RecordDefinition recDef;

  /** The identifier for this reader. */
  private    String           fileId;

  /** Data to be sent to the log. */
  private    LogData          logData;

  /** An event to be sent to the log. */
  private    LogEvent         logEvent;

  /**
     Constructs a ClearQuest Grid reader given a path defining the file
     to be read.

     @param  inPath file path to be read.
   */
  public CQGridReader (String inPath) {
    super (inPath);
    initialize();
  }

  /**
     Constructs a ClearQuest grid reader given a file object
     defining the file to be read.

     @param  inPathFile grid file to be read.
   */
  public CQGridReader (File inPathFile) {
    super (inPathFile.getAbsolutePath());
    initialize();
  }

  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "cqgrid".
   */
  private void initialize () {
    fileId = "cqgrid";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }

  /**
     Opens the reader for input using a newly defined
     data dictionary.

     @throws IOException If there are problems reading the directory.
   */
  public void openForInput ()
      throws IOException {
    openForInput (new DataDictionary());
  }

  /**
     Opens for input with the supplied record definition.

     @param  inRecDef Record definition already constructed.

     @throws IOException If there are problems reading the directory.
   */
  public void openForInput (RecordDefinition inRecDef)
      throws IOException {
    openForInput (inRecDef.getDict());
  } // end of openForInput method

  /**
     Opens for input with the supplied data dictionary.

     @param  inDict Data dictionary already constructed.

     @throws IOException If there are problems reading the directory.
   */
  public void openForInput (DataDictionary inDict)
      throws IOException {
    ensureLog();
    dict = inDict;
    recDef = new RecordDefinition (dict);
    ok = true;
    // displayLine ("Input file is "  + this.toString());
    try {
      cqGridReader = new FileReader (this);
      cqGridBufReader = new BufferedReader (cqGridReader);
      endOfFile = false;
      ix = 0;
      num = 0;

      if (ok) {
        row = 0;
        endOfRecord = false;
        lastColumn = -1;
        column = -1;
        while (row == 0 && (! endOfRecord) && (! endOfFile)) {
          String fieldName = getField();
          // displayLine ("Field name is " + fieldName);
          if (fieldName.trim().length() > 0) {
            recDef.addColumn (fieldName);
          }
        }
      }
    } catch (java.io.FileNotFoundException e) {
      Trouble.getShared().report("File Not Found Exception","File Not Found");
      ok = false;
    }

  } // end of openForInput method
  
 /**
     Returns the next directory entry.
    
     @return Next directory entry as a data record.
   */
  public DataRecord nextRecordIn () {
    if (this.isAtEnd()) {
      return null;
    } else {
      row++;
      DataRecord nextRec = new DataRecord ();
      endOfRecord = false;
      column = -1;
      while ((! endOfRecord) && (! endOfFile)) {
        nextRec.addField (recDef, getField());
      }
      if (endOfFile) {
        return null;
      } else {
        return nextRec;
      }
    } // end of logic if more directory entries to return
  } // end of nextRecordIn method
  
  /**
     Closes the reader.
   */
  public void close() {
    ensureLog();
    try {
      cqGridBufReader.close();
      logEvent.setSeverity (LogEvent.NORMAL);
      logEvent.setMessage (this.toString () + " closed successfully");
    } catch (java.io.IOException e) {
      System.out.println ("IO Exception during close");
      ok = false;
      logEvent.setSeverity (LogEvent.MINOR);
      logEvent.setMessage (this.toString () + " closed unsuccessfully");      
    }

    log.recordEvent (logEvent);
  }

  /**
     Ensures that a log is available, by allocating a new one if
     one has not already been supplied.
   */
  protected void ensureLog () {
    if (log == null) {
      setLog (new Logger (new LogOutput()));
    }
  }

  /**
     Sets a log to be used by the reader to record events.

     @param  log A logger object to use.
   */
  public void setLog (Logger log) {
    this.log = log;
  }

  /**
     Indicates whether all data records are to be logged.

     @param  dataLogging True if all data records are to be logged.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }

  private String getField() {
    StringBuffer field = new StringBuffer();
    endOfRecord = false;
    int specialStringPosition = 0;
    char c = getChar();
    // If double pipes, then skip past both of them
    if (c == PIPE) {
      c = getChar();
    }
    if (row == 0) {
      while ((c != PIPE) && (c != GlobalConstants.LINE_FEED) && (! endOfFile)) {
        if (c != GlobalConstants.CARRIAGE_RETURN) {
          field.append (c);
        }
        c = getChar();
      }
      lastColumn = column;
    } else {
      while ((! endOfFile)
          && (c != PIPE)
          && ((c != GlobalConstants.LINE_FEED)
              || (column < lastColumn))) {
        if (c == GlobalConstants.CARRIAGE_RETURN) {
          // do nothing
        }
        else
        if (c == GlobalConstants.LINE_FEED) {
          field.append ("<br/>");
        } 
        else
        if (c == '‚') {
          specialStringPosition = 1;
        }
        else
        if ((specialStringPosition == 1) && (c == 'Ä')) {
          specialStringPosition = 2;
        }
        else
        if ((specialStringPosition == 2) && (c == 'ô')) {
          field.append ("'");
          specialStringPosition = 0;
        }
        else
        if (c == '\u0092') {
          field.append ("'");
        } 
        else
        if (c == 'í') {
          field.append ("'");
        }
        else
        if (c == '\u0095') {
          field.append ("&bull;");
        } 
        else
        if (c == '"') {
          field.append("&quot;");
        }
        else
        if (c == '\u0093') {
          field.append ("&ldquo;");
        }
        else
        if (c == '\u0094') {
          field.append ("&rdquo;");
        } else {
          field.append (c);
        }
        c = getChar();
      } // end while more characters in field
    } // end if row > 0
    column++;
    if (c == GlobalConstants.LINE_FEED) {
      endOfRecord = true;
    }
    /* displayLine ("Field is " + field.toString ());
    displayLine ("  row = " + String.valueOf (row)
        + " column = " + String.valueOf (column)
        + " lastColumn = " + String.valueOf (lastColumn)); */
    return field.toString ();
  }

  private char getChar () {
    if (ix >= num) {
      getBlock();
    }
    if (num < 0) {
      endOfFile = true;
    }
    char c = ' ';
    if (! endOfFile) {
      c = chars [ix];
      ix++;
    }
    String cstr;
    if (c == GlobalConstants.LINE_FEED) {
      cstr = "<LF>";
    }
    else
    if (c == GlobalConstants.CARRIAGE_RETURN) {
      cstr = "<CR>";
    }
    else
    if (Character.isLetterOrDigit (c)) {
      cstr = String.valueOf (c);
    }
    else
    if (c == ',' || c == '.' || c == ';' || c == ':'
        || c == '-' || c == ' ' || c == PIPE
        || c == '(' || c == ')' || c == '"') {
      cstr = String.valueOf (c);
    } else {
      cstr = String.valueOf (c);
      // System.out.println ("Unknown character = " + c);
    }
    // System.out.println ("getChar returning " + cstr
    //     + " at buffer position " + String.valueOf (ix - 1));
    return c;
  }

  private void getBlock() {
    num = 0;
    try {
      num = cqGridBufReader.read(chars, 0, 1000);
      ix = 0;
    } catch (java.io.IOException io) {
      num = -1;
      System.out.println ("IO Exception");
    }
    // System.out.println ("getBlock returned " + String.valueOf (num) + " characters");
  }
  
  /**
     Sets a file ID to be used to identify this reader in the log.
    
     @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }

  private void displayLine (String line) {
    System.out.println (line);
  }

  private void displayString (String string) {
    System.out.print (string);
  }

  private void displayChar (char c) {
    System.out.print (c);
  }
  
  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return endOfFile;
  }
  
  /**
     Returns the record definition for the reader.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef () {
    return recDef;
  }
  
  /**
     Returns the sequential record number of the last record returned.
    
     @return Sequential record number of the last record returned via 
             nextRecordIn, where 1 identifies the first record.
   */
  public int getRecordNumber () {
    return row;
  }
  
  /**
     Retrieves the path to the original source data (if any).
    
     @return Path to the original source data (if any).
   */
  public String getDataParent () {
    return this.getParent ();
  }
   /**
     Does nothing in particular.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {
    
  }

}
