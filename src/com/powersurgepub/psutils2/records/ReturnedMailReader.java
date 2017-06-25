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

  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.textio.*;

  import java.io.*;

/**
 *
 * @author hbowie
 */
public class ReturnedMailReader 
    extends File
        implements DataSource {

  private   ReturnedMailParser      parser;

  private   boolean           ok = true;
  
  private   boolean           endOfRecord = false;

  private   int               recordNumber = 0;

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

  /** The logger to use to log events. */
  private    Logger           log;

  /* Let's not log all data. */
  private    boolean          dataLogging = false;

  public ReturnedMailReader (String inPath) {
    super (inPath);
    initialize();
  }

  /**
     Constructs a ClearQuest grid reader given a file object
     defining the file to be read.

     @param  inPathFile grid file to be read.
   */
  public ReturnedMailReader (File inPathFile) {
    super (inPathFile.getAbsolutePath());
    initialize();
  }

  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "cqgrid".
   */
  private void initialize () {
    fileId = "returnedmail";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
    parser = new ReturnedMailParser (this);
  }

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
    recDef.addColumn ("Email");
    ok = true;
    displayLine ("Input file is "  + this.toString());
    parser.open();
  } // end of openForInput method

  /**
   Returns the next directory entry.

   @return Next directory entry as a data record.
  */
  public DataRecord nextRecordIn () {
    if (this.isAtEnd()) {
      return null;
    } else {
      recordNumber++;
      DataRecord nextRec = new DataRecord ();
      nextRec.addField (recDef, parser.readBadEmailAddress());
      return nextRec;
    } // end of logic if more email addresses to return
  } // end of nextRecordIn method

  /**
     Indicates whether there are more records to return.

     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return parser.isAtEnd();
  }

  /**
     Closes the reader.
   */
  public void close() {
    ensureLog();
    parser.close();
    logEvent.setSeverity (LogEvent.NORMAL);
    logEvent.setMessage (this.toString () + " closed successfully");
    log.recordEvent (logEvent);
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
    return recordNumber;
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
