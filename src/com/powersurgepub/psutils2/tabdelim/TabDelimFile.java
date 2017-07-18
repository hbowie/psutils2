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

package com.powersurgepub.psutils2.tabdelim;

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.net.*;


/**
   A disk file of tab-delimited records. Records are passed to and from 
   this class in psdata.DataRecord formats. The first tab-delimited record 
   in the file should contain field names (column headings).  
 */
public class TabDelimFile 
    implements DataSource,
               DataStore   {
                 
  // The following fields are set by any constructor
                 
  /** File Name (without any path info) from which we can extract the extension. */
  private    FileName           fileNameObject;
  
  /** File name extension, used to identify comma-separated value files. */
  private    String             fileNameExt = "";
  
  /** Field delimiter -- either a tab or a comma. */
  private    char               fieldDelimiter = GlobalConstants.TAB;
  
  /** Field delimiter stored as a string. */
  private    String             fieldDelimiterStr = GlobalConstants.TAB_STRING;
  
  /** Path to the original source file (if any). */
  private		 String							dataParent;
  
  private    File               file = null;
  private    boolean            inOK = true;
  private    String             fileEncoding = "UTF-8";
  private    InputStreamReader  inReader = null;
  private    BufferedReader     inBuffered = null;
  private    TextLineWriter     writer = null;
  

  
  // The following fields are set by any open method
  
  /** Data dictionary to be used for the file. */
  private    DataDictionary     dict;
  
  /** Record definition to be used for the file. */
  private    RecordDefinition   recDef;
  
  /** Sequential number identifying last record read or written. */
  private    int                recordNumber;
  
  
  // The following fields are used by append processing
  
  /** Are we opened for an append operation? */
  private    boolean            openedForAppend = false;
  private    boolean            openedForInput  = false;
  private    boolean            atEnd = false;
  private    boolean            openedForOutput = false;
  
  /** Temporary file identifier. */
  private    File               tempFile;
  
  /** Temporary tab-delimited file. */
  private    TabDelimFile       tempTab;

  private    int                in = 0;
  private    char               c = ' ';
  private    StringBuilder      field = new StringBuilder();
  private    char               quoteChar = ' ';
  private    boolean            endOfLine = true;
  
  
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

  private  String       nextLine = "";

  /*========================================================================
   *
   * Constructors
   *
   *========================================================================*/
  
  
  /**
     A constructor that accepts a path and file name.
    
     @param inPath      A path to the directory containing the file.
    
     @param inFileName  The file name itself (without path info).
   */
  public TabDelimFile (String inPath, String inFileName) {
    file = new File (inPath, inFileName);
    commonFileConstruction();
  }
  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name.
   */
  public TabDelimFile (String inFileName) {
    file = new File (inFileName);
    commonFileConstruction();
  }
  
  /**
     A constructor that accepts two parameters: a File object
     representing the path to the file, and a String containing the file
     name itself.
     
     @param inPathFile  A path to the directory containing the file
     
     @param inFileName  The file name itself (without path info).
   */
  public TabDelimFile (File inPathFile, String inFileName) {
    file = new File (inPathFile, inFileName);
    commonFileConstruction();
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.</p>
     
     @param inFile  The text file itself.
   */
  public TabDelimFile (File inFile) {
    file = inFile;
    commonFileConstruction();
  }
  
  /**
     A constructor that accepts a URL
     representing the file.</p>
     
     @param url  A URL pointing to the text file itself.
   */
  public TabDelimFile (URL url) {
    writer = new FileMaker (url);
    if (url.getProtocol().equals ("file")) {
      file = new File (url.getFile());
    } else {
      file = null;
      inOK = false;
    }
    commonConstruction();
  }
  
  /**
   A constructor that accepts a TextLineWriter.
   */
  public TabDelimFile (TextLineWriter writer) {
    this.writer = writer;
    commonConstruction();
  }
  
  private void commonFileConstruction () {
    fileNameObject = new FileName (file.getName());
    fileNameExt = fileNameObject.getExt();
    if (fileNameExt.equals ("csv")) {
      fieldDelimiter = GlobalConstants.COMMA;
    } else {
      fieldDelimiter = GlobalConstants.TAB;
    }
    dataParent = file.getParent();
    if (writer == null) {
      writer = new FileMaker (file);
    }
    commonConstruction();
  }
  
  private void commonConstruction () {
    
  }
  
  public void setEncoding(String encoding) {
    this.fileEncoding = encoding;
  }
  
  public String getEncoding() {
    return fileEncoding;
  }

  /*========================================================================
   *
   * Input methods
   *
   *========================================================================*/
  
  /**
    Opens an existing file and prepares it to have additional data
    added to it at the end of the existing file. This method will 
    create a temporary file (same path, file name and extension as 
    the original file, but with "_temp" at the end of the file name,
    before the extension), and will then copy the existing file to
    the temp file.
   
    @param  recDef Record Definition to be used for this file.
   
    @throws IOException If disk problems are encountered.
   */
  public void openForAppend (RecordDefinition recDef) 
      throws IOException {
        
    // Create a temp file     
    if (file != null 
        && dataParent != null && (! dataParent.equals(""))
        && fileNameObject != null 
        && fileNameExt != null && (! fileNameObject.equals(""))) {
      tempFile 
          = new File (dataParent, fileNameObject.getBase() + "_temp." + fileNameExt);
    } else {
      tempFile = File.createTempFile (Home.getShared().getProgramNameNoSpace()
          + "_tabdelimfileappend", ".tmp");
    }
    if (tempFile.exists()
        && tempFile.isFile()
        && tempFile.canWrite()) {
      // Good to go: temp file exists but we can write over it
    }
    else
    if (! tempFile.exists()) {
      // Good to go: temp file does not yet exist
    } else {
      throw new IOException ("Could not create temp file");
    }

    tempTab = new TabDelimFile (tempFile);
    tempTab.openForOutput (recDef);
    
    // Copy the contents of the current file to the temp file
    if (inBuffered != null && inOK) {
      openedForAppend = false;
      try {
        openForInput ();
        tempTab.setLog (getLog());
        tempTab.setDataLogging (getDataLogging());
        DataRecord inRec;
        do {
          inRec = nextRecordIn ();
          if (inRec != null) {
            tempTab.nextRecordOut (inRec);
          }
        } while (inRec != null);
        close();
      } catch (IOException e) {
        // Do nothing
      }
    }
    
    // Prepare to receive records to be appended via nextRecordOut
    openedForAppend = true;
  }
  
  /**
     Opens the tab delimited file for subsequent input. This method also
     reads the first record from the file and attempts to build the
     record definition from it.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput () 
      throws IOException {
    this.dict = new DataDictionary();
    openForInput (dict);
  }
  
  /**
     Opens the tab delimited file for subsequent input. This method also
     reads the first record from the file and attempts to build the
     record definition from it.
    
     @param  dict Data dictionary to be used by this file.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput (DataDictionary dict) 
      throws IOException {
    this.dict = dict;
    openForInputCommon();
  }
  
  /**
     Opens the tab delimited file for subsequent input. This method also
     accepts a record definition, which will be used in lieu of obtaining
     column definitions from the first row of the input file.
    
     @param  recDef Record Definition to be used for this file.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput (RecordDefinition recDef) 
      throws IOException {
    this.dict = recDef.getDict();
    this.recDef = recDef;
    openForInputCommon();
  }

  private void openForInputCommon() {
    inOK = true;
    openedForInput = true;
    atEnd = false;

    if (file == null) {
      inOK = false;
      Trouble.getShared().report
          ("No file specified",
              "File Open Error");
    } else {
      if (! file.exists() ) {
        inOK = false;
        Trouble.getShared().report
            ("File "+ file.toString() + " could not be found",
                "File Open Error");
      }

      if (! file.isFile () ) {
        inOK = false;
        Trouble.getShared().report
            ("File "+ file.toString() + " is not a file",
                "File Open Error");
      }

      if (! file.canRead () ) {
        inOK = false;
        Trouble.getShared().report
            ("File "+ file.toString() + " cannot be read",
                "File Open Error");
      }
    }

    if (inOK) {
      try {
        FileInputStream fileInputStream = new FileInputStream(file);
        inReader = new InputStreamReader (fileInputStream, fileEncoding);
        inBuffered = new BufferedReader (inReader);
      } catch (IOException e) {
        inOK = false;
        Trouble.getShared().report
            ("File "+ file.toString() + " could not be opened for input",
                "File Open Error");
      }
    }

    if (! inOK) {
      openedForInput = false;
      atEnd = true;
    }

    recDef = new RecordDefinition (dict);
    c = ' ';
    firstRecordIn ();
    recordNumber = 0;
  }
  
  /**
     Builds the record definition from the first record
     read from disk.
    
     @return Record definition built from the passed record.
    
     @param  inStr The first record from the disk file, containing field
                   names separated by tab characters.
   */
  private void firstRecordIn () {

    do {
      readField();
      if (field.length() > 0) {
        int i = recDef.addColumn(field.toString());
      }
    } while ((! atEnd) && (! endOfLine));
  }
  
  /**
     Returns the next record in the input file as a data record,
     using the readLine method within XTextFile to get the next
     line from the text file.
    
     @return Next data record, built from tab-delimited record.
    
     @throws IOException If there is an error reading the file.
   */
  public DataRecord nextRecordIn () 
      throws IOException {

    if (atEnd) {
      return null;
    } else {
      DataRecord nextRec = new DataRecord ();
      recordNumber++;
      do {
        readField();
        int i = nextRec.addField (recDef, field.toString());
      } while ((! atEnd) && (! endOfLine));
      nextRec.calculate();
      return nextRec;
    } 
  }

  /**
     Returns the next field from the input file.

     @return Next sub-string delimited by field separator.
   */
  private void readField () {

    field = new StringBuilder();
    endOfLine = false;
    quoteChar = ' ';

    // Skip any leading spaces
    while ((! atEnd) && (c == GlobalConstants.SPACE)) {
      readCharacter();
    }

    // Preserve integrity of quoted fields
    if ((! atEnd) 
        && ((c == GlobalConstants.SINGLE_QUOTE)
          || (c == GlobalConstants.DOUBLE_QUOTE))) {
      quoteChar = c;
      boolean outOfQuotes = false;
      readCharacter();
      while ((! atEnd) && (! outOfQuotes)) {
        if (c == quoteChar) {
          readCharacter();
          if (c == quoteChar) {
            // Two quote chars in a row: replace with one and keep looking for
            // ending single occurrence of quote char
            field.append(c);
            readCharacter();
          } else {
            outOfQuotes = true;
          }
        } else {
          // Next character is not a quote char
          field.append(c);
          readCharacter();
        }
      } // end while still within quoted field
    } // end if field starts with a single or double quote

    // Consume characters until we find a field delimiter or end of line
    while ((! atEnd)
        && c != fieldDelimiter
        && c != GlobalConstants.CARRIAGE_RETURN
        && c != GlobalConstants.LINE_FEED) {
      field.append(c);
      readCharacter();
    }

    // See if we're at end of line
    if (atEnd) {
      endOfLine = true;
    }
    else
    if (c == GlobalConstants.CARRIAGE_RETURN
         || c == GlobalConstants.LINE_FEED) {
      endOfLine = true;
      char firstEndOfLineChar = c;
      readCharacter();
      if ((! atEnd)
         && (c == GlobalConstants.CARRIAGE_RETURN
           || c == GlobalConstants.LINE_FEED)
         && c != firstEndOfLineChar) {
        readCharacter();
      }
    } else {
      // Read past field delimiter
      readCharacter();
    }
  }

  private void readCharacter() {
    try {
      in = inBuffered.read();
      if (in < 0) {
        atEnd = true;
        c = ' ';
      } else {
        c = (char)in;
      }
    } catch (IOException e) {
      atEnd = true;
      inOK = false;
      c = ' ';
      reportIOTrouble();
    }

  }

  private void reportIOTrouble() {
    Trouble.getShared().report(
        "Error reading input file " + file.toString(),
        "I/O Error on Input");
  }
  
  public boolean isAtEnd () {
    if (inBuffered == null) {
      return true;
    } else {
      return atEnd;
    }
  }

  /*========================================================================
   *
   * Output methods
   *
   *========================================================================*/
  
  public void openForOutput () {
    writer.openForOutput();
    recordNumber = 0;
    openedForOutput = true;
  }
  
  /**
     Opens the tab-delimited file for subsequent output, and
     writes out the first record containing tab-delimited 
     column headings.
    
     @param recDef Record definition to be used for this file.
    
     @throws IOException If there is trouble opening the disk file.
   */
  public void openForOutput (RecordDefinition recDef) 
      throws IOException {
    writer.openForOutput ();
    this.recDef = recDef;
    dict = recDef.getDict();
    String firstLine = this.firstRecordOut();
    writer.write (firstLine);
    writer.newLine();
    recordNumber = 0;
    openedForOutput = true;
  }
  
  /**
     Builds the first record of the output file, containing field
     names (column headings) separated by tab characters.
    
     @return First record to be written to the output file.
   */
  public String firstRecordOut () {
    TabDelimBuilder builder 
      = new TabDelimBuilder ();
    recDef.resetColumnNumber();
    while (recDef.hasMoreDefs()) { 
      DataFieldDefinition fieldDef = recDef.nextDef();
      String properName = fieldDef.getProperName();
      builder.nextToken (properName);
    }
    return builder.toString();
  }
  
  /**
     Writes the next data record to the output disk file.
    
     @param inRec Next data record to be written out.
    
     @throws IOException If there is trouble writing the record.
   */
  public void nextRecordOut (DataRecord inRec) 
      throws IOException {
        
    if (openedForAppend) {
      tempTab.nextRecordOut (inRec);
    } else {
      recordNumber++;
      TabDelimBuilder builder 
        = new TabDelimBuilder();
      inRec.startWithFirstField();
      while (inRec.hasMoreFields()) {
        DataField field = inRec.nextField();
        String fieldData = StringUtils.purifyInvisibles((String)field.getData());
        builder.nextToken (fieldData);
      }
      String nextLine = builder.toString();
      writer.write (nextLine);
      writer.newLine();
    } 

  }

  /*========================================================================
   *
   * Utility methods
   *
   *========================================================================*/
  
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
  
  public void setMaxDepth(int maxDepth) {
  
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
  
  public String getPath () {
    if (file != null) {
      return file.getPath();
    } else {
      return "";
    }
  }

  public String getAbsolutePath () {
    if (file != null) {
      return file.getAbsolutePath();
    } else {
      return dataParent;
    }
  }
  
  public String getFilePathAndName () {
    if (file != null) {
      return file.getAbsolutePath();
    } else {
      return "";
    }
  }
  
  public String getFileName () {
    if (file != null) {
      return file.getName();
    } else {
      return "";
    }
  }
  
  /**
     Sets a path to be used to read any associated files.
    
     @param  dataParent A path to be used to read any associated files.
   */
  public void setDataParent (String dataParent) {
    this.dataParent = dataParent;
  }
  
  /**
    Close this file.
   
    @throws IOException If there is trouble opening the disk file.
   */
  public void close () 
      throws IOException {
        
    if (openedForInput) {
      inBuffered.close();
    }
    if (openedForOutput) {
      writer.close();
    }
    
    if (openedForAppend) {
      tempTab.close();
      File oldFile = new File (dataParent, 
					fileNameObject.getBase() 
					+ "_old." 
					+ fileNameExt);
      if (oldFile.exists()) {
        oldFile.delete();
      }
			if (file.exists()) {
				boolean renameOK = file.renameTo (oldFile);
			}
      boolean renameOK = tempFile.renameTo 
          (new File (dataParent.toString(), fileNameObject.toString()));
      openedForAppend = false;
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
  
  /**
     Returns this object as some sort of string.
    
     @return Name of this disk file.
   */
  public String toString () {
    if (file != null) {
      return file.toString();
    } else {
      return (super.toString ());
    }
  }
}
