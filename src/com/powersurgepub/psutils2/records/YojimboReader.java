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
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.textio.*;

  import java.io.*;
  import java.util.*;
  import java.text.*;

/**
   A directory reader that returns Directory Entries as DataRecord
   objects or as File objects. <p>
   
   This code is copyright (c) 1999-2008 by Herb Bowie of PowerSurge Publishing.
   All rights reserved. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
   @version 
    2003/07/30 - Added getDataParent method for consistency with DataSource. 
 */
 public class YojimboReader 
    extends     File
    implements  DataSource {
    
  public  static   final  String  YOJIMBO_NAME = "Name";
  public  static   final  String  YOJIMBO_LOCATION = "Location";
  public  static   final  String  YOJIMBO_ACCOUNT = "Account";
  public  static   final  String  YOJIMBO_PASSWORD = "Password";
  public  static   final  String  YOJIMBO_PRODUCT_NAME = "Product Name";
  public  static   final  String  YOJIMBO_OWNER_NAME = "Owner Name";
  public  static   final  String  YOJIMBO_EMAIL_ADDRESS = "Email Address";
  public  static   final  String  YOJIMBO_ORGANIZATION = "Organization";
  public  static   final  String  YOJIMBO_SERIAL_NUMBER = "Serial Number";
  public  static   final  String  YOJIMBO_COMMENTS = "Comments";
  
  /** 
     The number of levels of directories and sub-directories to be read. 
     A value of 1 (the default) indicates that only the top level directory
     should be read. A value of 2 indicates one level of sub-directories, and
     so forth.
   */
  
  private		 DirToExplode			newDirToExplode;
  
  private		 int							currDirDepth;

  private    int              maxDepth = 99;
  
  private		 File							currDirAsFile;
  
  private    ArrayList        dirList;
  
  private		 int							dirNumber;		
  
  private		 ArrayList				dirEntries;
  
  private		 int							entryNumber;
  
  private    String						nextDirEntry;

  /** The logger to use to log events. */    
  private    Logger           log;
  
  /* Let's not log all data. */
  private    boolean          dataLogging = false;
  
  /** The data dictionary to be used by this record. */
  private    DataDictionary   dict;
  
  /** The record definition to be used by this record. */
  private    RecordDefinition recDef;
  
  /** Pointer to a particular record within the array. */
  private    int              recordNumber;
  
  /** Data to be sent to the log. */
  private    LogData          logData;
  
  /** An event to be sent to the log. */
  private    LogEvent         logEvent;
  
  /** The identifier for this reader. */
  private    String           fileId;
  
  /** The directory to be read. */
  private    String           directoryPath;
  
  /** The number of directories in the top directory to be read. */
  private		 int							directoryNumberOfFolders;

  /**
     Constructs a directory reader given a path defining the directory
     to be read.
    
     @param  inPath Directory path to be read.
   */
  public YojimboReader (String inPath) {
    super (inPath);
    directoryPath = inPath;
    initialize();
  }

  /**
     Constructs a directory reader given a file object 
     defining the directory to be read.
    
     @param  inPathFile Directory path to be read.
   */
  public YojimboReader (File inPathFile) {
    super (inPathFile.getAbsolutePath());
    directoryPath = this.getAbsolutePath();
    initialize();
  }
  
  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "directory".
   */
  private void initialize () {
    FileName dirName = new FileName (directoryPath, FileName.DIR_TYPE);
    directoryNumberOfFolders = dirName.getNumberOfFolders();
    fileId = "yojimbo";
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
    recDef.addColumn (YOJIMBO_NAME);
    recDef.addColumn (YOJIMBO_LOCATION);
    recDef.addColumn (YOJIMBO_ACCOUNT);
    recDef.addColumn (YOJIMBO_PASSWORD);
    recDef.addColumn (YOJIMBO_PRODUCT_NAME);
    recDef.addColumn (YOJIMBO_OWNER_NAME);
    recDef.addColumn (YOJIMBO_EMAIL_ADDRESS);
    recDef.addColumn (YOJIMBO_ORGANIZATION);
    recDef.addColumn (YOJIMBO_SERIAL_NUMBER);
    recDef.addColumn (YOJIMBO_COMMENTS);
    dirList = new ArrayList();
    newDirToExplode = new DirToExplode (1, directoryPath);
    dirList.add (newDirToExplode);
    dirNumber = -1;
    dirEntries = new ArrayList();
    entryNumber = -1;
    recordNumber = 0;
    prepareNextRecord();
  } // end of openForInput method

  /**
     Returns the next directory entry.
    
     @return Next directory entry as a data record.
   */
  public DataRecord nextRecordIn () {
    if (this.isAtEnd()) {
      return null;
    } else {
      DataRecord nextRec = new DataRecord ();
      int fieldNumber;
      File dirEntryFile = nextTxtFileIn();
      TextLineReader reader = new FileLineReader (dirEntryFile);
      FileName dirEntryFileName = new FileName (dirEntryFile);
      reader.open();
      String name = "";
      String productName = "";
      String ownerName = "";
      String emailAddress = "";
      String organization = "";
      String serialNumber = "";
      String location = "";
      String account = "";
      String password = "";
      StringBuilder comments = new StringBuilder ();
      while (reader.isOK() && (! reader.isAtEnd())) {
        String line = reader.readLine();
        if (line.startsWith ("Name: ")) {
          name = line.substring (6);
        }
        else
        if (line.startsWith ("Location: ")) {
          location = line.substring (10);
        }
        else
        if (line.startsWith ("Account: ")) {
          account = line.substring (9);
        }
        else
        if (line.startsWith ("Password: ")) {
          password = line.substring (10);
        }
        else
        if (line.startsWith ("Product Name: ")) {
          productName = line.substring (14);
        }
        else
        if (line.startsWith ("Owner Name: ")) {
          ownerName = line.substring (12);
        }
        else
        if (line.startsWith ("Email Address: ")) {
          emailAddress = line.substring (15);
        }
        else
        if (line.startsWith ("Organization: ")) {
          organization = line.substring (14);
        }
        else
        if (line.startsWith ("Serial Number: ")) {
          serialNumber = line.substring (15);
        }
        else
        if (line.startsWith ("Comments: ")) {
          comments.append (line.substring (10));
          comments.append (" ");
        } else {
          comments.append (line);
          comments.append (" ");
        }
      }
      fieldNumber = nextRec.addField (recDef, name);
      fieldNumber = nextRec.addField (recDef, location);
      fieldNumber = nextRec.addField (recDef, account);
      fieldNumber = nextRec.addField (recDef, password);
      fieldNumber = nextRec.addField (recDef, productName);
      fieldNumber = nextRec.addField (recDef, ownerName);
      fieldNumber = nextRec.addField (recDef, emailAddress);
      fieldNumber = nextRec.addField (recDef, organization);
      fieldNumber = nextRec.addField (recDef, serialNumber);
      fieldNumber = nextRec.addField (recDef, comments.toString());
      
      return nextRec;
    } // end of logic if more directory entries to return
  } // end of nextRecordIn method

  /**
   * Returns the next Yojimbo export text file.
   *
   * @return Next Yojimbo text file.
   */
  public File nextTxtFileIn () {
    File txtFile = nextFileIn ();
    while (txtFile != null
        && ((! txtFile.isFile())
          || (! txtFile.canRead())
          || (! txtFile.getName().endsWith (".txt")))) {
      txtFile = nextFileIn();
    }
    return txtFile;
  }

  /**
     Returns the next directory entry as a File object.
    
     @return Next directory entry as a File object.
   */
  public File nextFileIn () {
    if (this.isAtEnd()) {
      return null;
    } else {
      File dirEntryFile = new File (currDirAsFile, nextDirEntry);
      if (dirEntryFile.isDirectory()) {
        newDirToExplode = new DirToExplode (currDirDepth + 1, dirEntryFile.getAbsolutePath());
        dirList.add (newDirToExplode);
      }
      recordNumber++;
      prepareNextRecord();
      return dirEntryFile;
    } // end of logic if more directory entries to return
  } // end of method
  
  /**
     Gets the next directory entry to be returned, if there is one,
     and prepares it for processing.
   */
  private void prepareNextRecord () {
    entryNumber++;
    while (dirNumber < dirList.size()
        && entryNumber >= dirEntries.size()) {
      dirNumber++;
      if (dirNumber < dirList.size()) {
        explodeDir ((DirToExplode)dirList.get (dirNumber));
        entryNumber = 0;
      } // end if more directories to explode
    } // end while still trying to find next record
    if (entryNumber < dirEntries.size()) {
      nextDirEntry = (String)dirEntries.get (entryNumber);
    }
  } // end record preparation
  
  /**
     Explode the next directory.
   */
  private void explodeDir (DirToExplode dir) {
    currDirAsFile = new File (dir.path);
    currDirDepth = dir.depth;
    String[] dirEntry = currDirAsFile.list();
    if (dirEntry == null) {
      dirEntries = new ArrayList();
      logEvent.setSeverity (LogEvent.MINOR);
      logEvent.setMessage (dir.path + " not listed successfully");
      logEvent.setDataRelated(false);
      log.recordEvent (logEvent);
    } else {
      dirEntries = new ArrayList (Arrays.asList(dirEntry));
      // logEvent.setSeverity (LogEvent.NORMAL);
      // logEvent.setMessage (dir.path + " listed successfully");
      // logEvent.setDataRelated(false);
      // log.recordEvent (logEvent);
    }
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
    return (dirNumber >= dirList.size());
  }
  
  /**
     Closes the reader.
   */
  public void close() {
    ensureLog();
    logEvent.setSeverity (LogEvent.NORMAL);
    logEvent.setMessage (directoryPath + " closed successfully");
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
  
  /**
     Sets the maximum directory explosion depth. The default is 1, meaning
     that only one level is returned (no explosion). If this is changed, it
     should be done after the reader is constructed, but before it is opened
     for input.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {
    this.maxDepth = maxDepth;
  }
  
  /**
     Retrieves the path to the original source data (if any).
    
     @return Path to the original source data (if any).
   */
  public String getDataParent () {
    if (directoryPath == null) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return directoryPath;
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
  
  /**
     Inner class to define a directory to be processed.
   */
  class DirToExplode {
    int 		depth = 0;
    String	path  = "";
    
    DirToExplode (int depth, String path) {
      this.depth = depth;
      this.path = path;
    } // DirToExplode constructor
  } // end DirToExplode inner class
  
} // end DirectoryReader class
