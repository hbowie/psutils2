/*
 * Copyright 2012 - 2015 Herb Bowie
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

package com.powersurgepub.psutils2.clubplanner;

	import com.powersurgepub.psutils2.basic.*;
	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.textio.*;

  import java.io.*;
  import java.util.*;
  

/**
   A reader that reads Club Planner event records. 
  
   @author Herb Bowie 
   
 */
 public class ClubEventReader 
     extends File
         implements  
             DataSource {
   
   public static final String STATUS = "Status";
   public static final String FLAGS  = "Flags";
   public static final String NULL   = "null";
  
  /** 
     The number of levels of directories and sub-directories to be read. 
     A value of 1 (the default) indicates that only the top level directory
     should be read. A value of 2 indicates one level of sub-directories, and
     so forth.
   */
  
  private		 int							currDirDepth;
  
  private		 File							currDirAsFile;
  


  /** The logger to use to log events. */    
  private    Logger           log;
  
  /* Let's not log all data. */
  private    boolean          dataLogging = false;
  
  /** The data dictionary to be used by this record. */
  private    DataDictionary   dict = new DataDictionary();
  
  /** The record definition to be used by this record. */
  private    RecordDefinition recDef = null;
  
  /** Pointer to a particular record within the array. */
  private    int              recordNumber;
  
  /** Data to be sent to the log. */
  private    LogData          logData;
  
  /** An event to be sent to the log. */
  private    LogEvent         logEvent;
  
  /** The identifier for this reader. */
  private    String           fileId;
  
  /** The directory to be read. */
  private    String           inPath;
  
  /** The type of data set to generate: planner or minutes. */
  private    int              inType = 1;
  public static final int     PLANNER_TYPE = 1;
  public static final int     NOTES_TYPE = 2;
  
  /** Is this a minutes file? */
  private    boolean          minutesFile = false;
  
  
  /** The number of directories in the top directory to be read. */
  private		 int							directoryNumberOfFolders;
  
  private    ClubEventCalc    clubEventCalc = null;
  
  private    boolean          endOfNotesBlock = false;
  
  private    StringBuilder    eventFieldValue = new StringBuilder();
  
  private    ClubEvent        clubEvent;
  private    EventNote        eventNote;
  private    int              noteIndex = 0;
  private    boolean          endOfEvent = false;
  private    boolean          atEnd = false;
  
  private    int              fieldAsHTMLNumber = -1;
  private    TextLineReader   reader = null;
  private    FileName         inPathFileName;
  
  private    boolean          blockComment = false;
  
  // The next line to be processed, and info about that line. 
  private    String           line = "";
  private    int              lineStart = 0;
  private    int              lineEnd = line.length();
  private    int              blockCommentStart = -1;
  private    int              blockCommentEnd = -1;
  private    int              lineCommentStart = -1;
  
  private    int              fieldNumber = -1;
  private    int              valueStart = 0;
  private    String           appendValue = "";
  
  
  private    boolean          notesHeaderLine = false;
  private    int              notesHeaderDashStart = 0;

  /**
     Constructs a club event reader given a path defining the directory
     to be read.
    
     @param  inPath Directory path to be read.
   */
  public ClubEventReader (String inPath, int inType) {
    super (inPath);
    this.inPath = inPath;
    this.inType = inType;
    initialize();
  }

  /**
     Constructs a club event reader given a file object 
     defining the directory to be read.
    
     @param  inPathFile Directory path to be read.
   */
  public ClubEventReader (File inPathFile, int inType) {
    super (inPathFile.getAbsolutePath());
    this.inPath = this.getAbsolutePath();
    this.inType = inType;
    initialize();
  }
  
  public ClubEventReader (File inFile, int inType, boolean minutesFile) {
    super(inFile.getAbsolutePath());
    this.inPath = this.getAbsolutePath();
    this.inType = inType;
    this.minutesFile = minutesFile;
    initialize();
  }
  
  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "directory".
   */
  private void initialize () {
    if (this.isDirectory()) {
      FileName dirName = new FileName (inPath, FileName.DIR_TYPE);
      directoryNumberOfFolders = dirName.getNumberOfFolders();
    } else {
      FileName dirName = new FileName (this.getParentFile());
      directoryNumberOfFolders = dirName.getNumberOfFolders();
    }
    
    recDef = new RecordDefinition(dict);
    for (int i = 0; i < ClubEvent.COLUMN_COUNT; i++) {
      recDef.addColumn (ClubEvent.getColumnName(i));
    }
    
    if (inType == NOTES_TYPE) {
      for (int i = 0; i < EventNote.COLUMN_COUNT; i++) {
        recDef.addColumn (EventNote.getColumnName(i));
      }
    }
    
    fileId = "ClubPlannerDataSource";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }
  
  /**
   Set the club event calculator to use. 
  
   @param clubEventCalc The club event calculator to use. 
  */
  public void setClubEventCalc (ClubEventCalc clubEventCalc) {
    this.clubEventCalc = clubEventCalc;
  }
  
  /**
   Ensure that we have a club event calculator. If one hasn't been passed, 
   then let's create a new one. 
  */
  private void ensureClubEventCalc() {
    if (clubEventCalc == null) {
      this.clubEventCalc = new ClubEventCalc();
    }
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
  
  public void openForInput (ClubEvent clubEvent) 
      throws IOException {
    dict = new DataDictionary();
    this.clubEvent = clubEvent;
    openForInputCommon();
  }
  
  /**
     Opens for input with the supplied data dictionary.
    
     @param  inDict Data dictionary already constructed.
    
     @throws IOException If there are problems opening the file.
   */
  public void openForInput (DataDictionary inDict) 
      throws IOException {
    
    dict = inDict;
    clubEvent = new ClubEvent();
    openForInputCommon();
    
  } // end of openForInput method
  
  private void openForInputCommon() {
    
    ensureLog();
    
    recordNumber = 0;
    fieldNumber = -1;

    reader = new FileLineReader (this);
    ensureClubEventCalc();
    if (! minutesFile) {
      clubEventCalc.setFileName(this);
    }
    atEnd = false;
    reader.open();
    readNextEvent();
  }
  
  /**
   Return next ClubEvent object. 
  
   @return Next ClubEvent object, or null if no more to return.
  */
  public ClubEvent nextClubEvent() {
    if (clubEvent != null
        && clubEvent.isModified() 
        && clubEvent.getWhat() != null
        && clubEvent.getWhat().length() > 0) {
      recordNumber++;
      ClubEvent nextEvent = clubEvent;
      readNextEvent();
      return nextEvent;
    } else {
      atEnd = true;
      return null;
    }
  }
  
  public ClubEvent getClubEvent() {
    return clubEvent;
  }
  
  public DataRecord nextRecordIn () {
    if (isAtEnd()) {
      return null;
    }
    else
    if (inType == PLANNER_TYPE) {
      return nextEventRecordIn();
    }
    else
    if (inType == NOTES_TYPE) {
      return nextNotesRecordIn();
    } else {
      return null;
    }
  }

  /**
     Returns the next directory entry.
    
     @return Next directory entry as a data record.
   */
  private DataRecord nextEventRecordIn () {
    if (clubEvent.isModified() 
        && clubEvent.getWhat().length() > 0) {
      recordNumber++;
      ClubEvent nextEvent = clubEvent;
      readNextEvent();
      return nextEvent.getDataRec();
    } else {
      atEnd = true;
      return null;
    }
  } // end of nextRecordIn method
  
  private void readNextEvent() {
    
    clubEvent = new ClubEvent();
    endOfEvent = false;
    // Now gather field values from the input file
    blockComment = false;
    
    if (clubEventCalc.ifOpYearFromFolder()) {
      clubEvent.setYear(clubEventCalc.getOpYearFromFolder());
    }
    // clubEvent.setFileName(inPathFileName.getBase());
    if (clubEventCalc.ifStatusFromFolder()) {
      clubEvent.setFlags(clubEventCalc.getStatusFromFolder());
    }
    if (clubEventCalc.ifCategoryFromFolder()) {
      clubEvent.setCategory(clubEventCalc.getCategoryFromFolder());
    }
    clubEvent.resetModified();
    if (! minutesFile) {
      clubEvent.setDiskLocation(this);
    }

    while (reader != null
        && reader.isOK()
        && (! reader.isAtEnd())
        && (! endOfEvent)) {
      readAndEvaluateNextLine();
      processNextLine();
    } // end while reader has more lines
    setLastEventFieldValue();
    if (clubEvent.hasWhat()
        && clubEvent.getWhat().length() > 0) {
      clubEventCalc.calcAll(clubEvent);
    }
    noteIndex = 0;
  }
  
  /**
   Returns the next minutes entry as a record. 
  
   @return Next minutes entry.
  */
  private DataRecord nextNotesRecordIn () {
    
    if (atEnd) {
      return null;
    }
    else
    if ((! clubEvent.isModified())
        || clubEvent.getWhat().length() == 0) {
      atEnd = true;
      return null;
    }
    else
    if (noteIndex >=  clubEvent.sizeEventNoteList()) {
      atEnd = true;
      return null;
    } else {
      DataRecord dataRec = clubEvent.getDataRec();
      eventNote = clubEvent.getEventNote(noteIndex);
      for (int i = 0; i < EventNote.COLUMN_COUNT; i++) {
        Object noteField = eventNote.getColumnValue(i);
        if (noteField == null) {
          int dataRecFieldNumber = dataRec.addField
              (recDef, "");
        } else {
          int dataRecFieldNumber = dataRec.addField
              (recDef, noteField.toString());
        }
      }
      noteIndex++;
      return dataRec;
    }
  }
  
  /**
    Read the next line and see what we've got. 
   */
  private void readAndEvaluateNextLine () {
    
    line = reader.readLine();
    lineStart = 0;
    lineEnd = line.length();

    // Check for comments
    blockCommentStart = -1;
    blockCommentEnd = -1;
    lineCommentStart = -1;
    
    notesHeaderLine = false;

    if (blockComment) {
      lineEnd = 0;
    }

    int slashScanStart;
    int slashIndex = line.indexOf('/');
    while (slashIndex >= 0 && slashIndex < line.length()) {
      slashScanStart = slashIndex + 1;

      // Get characters before and after slash
      char beforeSlash = ' ';
      if (slashIndex > 0) {
        beforeSlash = line.charAt(slashIndex - 1);
      }
      char afterSlash = ' ';
      if (slashIndex < (line.length() - 1)) {
        afterSlash = line.charAt(slashIndex + 1);
      }

      if (blockComment) {
        if (beforeSlash == '*') {
          lineStart = slashIndex + 1;
          lineEnd = line.length();
          blockComment = false;
        }
      } else {
        if (afterSlash == '*') {
          lineEnd = slashIndex;
          blockComment = true;
        }
        else
        if (beforeSlash == ' ' && afterSlash == '/') {
          lineEnd = slashIndex;
          slashScanStart++;
        }
      }
      slashIndex = line.indexOf('/', slashScanStart);
    }
    
    // Check for markdown heading
    int headingLevel = 0;
    int colonPos = -1;
    while ((lineStart + headingLevel) < lineEnd
        && line.charAt(lineStart + headingLevel) == '#') {
      headingLevel++;
    }
    if (headingLevel > 0
        && (lineStart + headingLevel) < lineEnd
        && line.charAt(lineStart + headingLevel) == ' ') {
      // Looks like a markdown heading
      lineEnd = 0;
    } else {
      headingLevel = 0;
      colonPos = line.indexOf(':', lineStart);
    }
    
    // Check for end of event
    if ((lineEnd - lineStart) >= 3
        && line.substring(lineStart, lineStart + 3).equals("...")) {
      lineEnd = 0;
      endOfEvent = true;
    }
    
    valueStart = lineStart;

    int commonNameIndex = -1;
    if (colonPos > 0 && colonPos < lineEnd) {
      String possibleFieldName 
          = line.substring(lineStart, colonPos).trim();
      int dataStartFollowingColon = colonPos + 1;
      while (dataStartFollowingColon < lineEnd 
          && Character.isWhitespace(line.charAt(dataStartFollowingColon))) {
        dataStartFollowingColon++;
      }
      
      if (possibleFieldName.equalsIgnoreCase(STATUS)) {
        possibleFieldName = FLAGS;
      }
      
      commonNameIndex = ClubEvent.commonNameStartsWith(possibleFieldName);

      if (commonNameIndex >= 0) {
        valueStart = dataStartFollowingColon;
      }
    } // end if colon found on the line
    
    appendValue = line.substring(valueStart, lineEnd).trim();
    
    if (commonNameIndex >= 0) {
      setLastEventFieldValue();
      fieldNumber = commonNameIndex;
    }
    
  } // end method readAndEvaluateNextLine
  
  /**
   At the end of a file, or when starting a new field, take the accumulated
   String value found and apply it to the last field. 
   */
  private void setLastEventFieldValue() {
    if (fieldNumber >= 0
        && eventFieldValue.length() > 0
        && (! eventFieldValue.toString().trim().equalsIgnoreCase(NULL))) {
      clubEvent.setColumnValue(fieldNumber, eventFieldValue.toString().trim());
    }
    eventFieldValue = new StringBuilder();
  }
  
  /**
    Process the contents of the next line. 
   */
  private void processNextLine() {
    
    if (fieldNumber >= 0) {
      // A valid field has been identified on this or a prior line
      if (appendValue.length() == 0) {
        if (ClubEvent.isMarkdownFormat(fieldNumber)) {
          eventFieldValue.append(GlobalConstants.LINE_FEED);
        }
      } else {
        // This line is not blank
        if (eventFieldValue.length() > 0
            && eventFieldValue.charAt(eventFieldValue.length() - 1) 
              != GlobalConstants.LINE_FEED) {
          eventFieldValue.append(" ");
        }
        eventFieldValue.append(appendValue);
        if (ClubEvent.isMarkdownFormat(fieldNumber)) {
          eventFieldValue.append(GlobalConstants.LINE_FEED);
        }
      } // end if processing a line with a non-blank value
    } // end if we have found a valid field identifier for this line    
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
     Returns the reader as some kind of string.
    
     @return Name of the directory.
   */
  public String toString () {
    return ("Directory Name is "
      + super.toString ());
  }

  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return atEnd;
  }
  
  /**
     Closes the reader.
   */
  public void close() {
    reader.close();
    ensureLog();
    // logEvent.setSeverity (LogEvent.NORMAL);
    // logEvent.setMessage (inPath + " closed successfully.");
    // log.recordEvent (logEvent);
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
     Sets the maximum directory explosion depth. The default is 1, meaning
     that only one level is returned (no explosion). If this is changed, it
     should be done after the reader is constructed, but before it is opened
     for input.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {

  }
  
  /**
     Retrieves the path to the original source data (if any).
    
     @return Path to the original source data (if any).
   */
  public String getDataParent () {
    if (inPath == null) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return inPath;
    }
  }
  
  /**
     Sets a file ID to be used to identify this reader in the log.
    
     @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }
  
} 