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
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.strings.*;

  import java.io.*;
  import java.util.*;
  import java.text.*;

/**
   A directory reader that returns Directory Entries as DataRecord
   objects or as File objects. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
   @version 
    2003/07/30 - Added getDataParent method for consistency with DataSource. 
 */
 public class DirectoryReader 
    extends     File
    implements  DataSource {
    
  /** The name of the sort key field within the directory record. */
  public  static   final  String  DIR_ENTRY_SORT_KEY = "Sort Key";
    
  /** The name of the name field within the directory record. */
  public  static   final  String  DIR_ENTRY_FOLDER = "Folder";
  
  /** The path from the starting folder to the file. */
  public  static   final  String  DIR_ENTRY_PATH = "Path";

  /** The name of the name field within the directory record. */
  public  static   final  String  DIR_ENTRY_NAME = "File Name";
    
  /** The name of the type field within the directory record. */
  public  static   final  String  DIR_ENTRY_TYPE = "Type";
    
  /** The name of the English-like name within the directory record. */
  public  static   final  String  DIR_ENTRY_ENGLISH_NAME = "English Name";

  /** The file name, without path and without file extension. */
  public  static   final  String  DIR_ENTRY_FILE_NAME_NO_EXT = "File Name w/o Ext";
    
  /** The name of the file extension field within the directory record. */
  public  static   final  String  DIR_ENTRY_FILE_EXT = "File Ext";
    
  /** The name of the file size field within the directory record. */
  public  static   final  String  DIR_ENTRY_FILE_SIZE = "File Size";
    
  /** The name of the last modification date field within the directory record. */
  public  static   final  String  DIR_ENTRY_LAST_MOD_DATE = "Last Mod Date";
  
  /** The name of the last modification time field within the directory record. */
  public  static   final  String  DIR_ENTRY_LAST_MOD_TIME = "Last Mod Time";
  
  /** The next apparent word in the file name. */
  public  static   final  String  DIR_ENTRY_WORD = "Word";
  
  /** The maximum number of words to extract from the file name. */
  public  static   final  int     MAX_WORDS = 5;
  
  /** 
     The number of levels of directories and sub-directories to be read. 
     A value of 1 (the default) indicates that only the top level directory
     should be read. A value of 2 indicates one level of sub-directories, and
     so forth.
   */
  private		 int							maxDepth = 1;
  
  private		 DirToExplode			newDirToExplode;
  
  private		 int							currDirDepth;
  
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
  
  /** Something to use to format dates. */
  private    DateFormat       dateFormatter
    = new SimpleDateFormat ("yyyy-MM-dd");
    
  /** Something to use to format times. */
  private    DateFormat       timeFormatter
    = new SimpleDateFormat ("HH:mm:ss zzz");
  
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
  public DirectoryReader (String inPath) {
    super (inPath);
    directoryPath = inPath;
    initialize();
  }

  /**
     Constructs a directory reader given a file object 
     defining the directory to be read.
    
     @param  inPathFile Directory path to be read.
   */
  public DirectoryReader (File inPathFile) {
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
    fileId = "directory";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }
  
  /** 
     Returns the contents of the directory as a data set,
     sorted by the standard sort key for the directory.
    
     @return Directory contents as a data set.
    
     @throws IOException Shouldn't ever happen, with a DataSet as the DataSource.
   */
  public DataSet sorted () 
      throws IOException {
    DataSet ds = new DataSet(this);
    SequenceSpec seq = new SequenceSpec (recDef, DIR_ENTRY_SORT_KEY);
    ds.setSequence (seq);
    return ds;
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
    recDef.addColumn (DIR_ENTRY_SORT_KEY);
    for (int i = 1; i < maxDepth; i++) {
      recDef.addColumn (DIR_ENTRY_FOLDER + String.valueOf(i));
    }
    recDef.addColumn (DIR_ENTRY_PATH);
    recDef.addColumn (DIR_ENTRY_NAME);
    recDef.addColumn (DIR_ENTRY_TYPE);
    recDef.addColumn (DIR_ENTRY_ENGLISH_NAME);
    recDef.addColumn (DIR_ENTRY_FILE_NAME_NO_EXT);
    recDef.addColumn (DIR_ENTRY_FILE_EXT);
    recDef.addColumn (DIR_ENTRY_FILE_SIZE);
    recDef.addColumn (DIR_ENTRY_LAST_MOD_DATE);
    recDef.addColumn (DIR_ENTRY_LAST_MOD_TIME);
    for (int i = 1; i <= MAX_WORDS; i++) {
      recDef.addColumn (DIR_ENTRY_WORD + String.valueOf(i));
    }
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
      File dirEntryFile = nextFileIn();
      FileName dirEntryFileName = new FileName (dirEntryFile);
      
      // Sort key
      fieldNumber = nextRec.addField 
        (recDef, StringUtils.wordSpace (dirEntryFile.getAbsolutePath(), true));
        
      // Individual folder names
      StringBuilder path = new StringBuilder();
      for (int i = 1; i < maxDepth; i++) {
        if (i < currDirDepth) {
          String folder = dirEntryFileName.getFolder (directoryNumberOfFolders + i);
          fieldNumber = nextRec.addField (recDef, folder);
          if (path.length() > 0) {
            path.append ('/');
          }
          path.append (folder);
        } else {
          fieldNumber = nextRec.addField (recDef, "");
        }
      }
      
      // Path
      fieldNumber = nextRec.addField (recDef, path.toString());
      
      // File name
      fieldNumber = nextRec.addField (recDef, dirEntryFile.getName());
      
      String dirEntryType = "?";
      String size = " ";
      String lastModDate = " ";
      String lastModTime = " ";
      if (dirEntryFile.isFile()) {
        dirEntryType = "File";
        size = String.valueOf (dirEntryFile.length());
        Date lastMod = new Date (dirEntryFile.lastModified());
        lastModDate = dateFormatter.format (lastMod);
        lastModTime = timeFormatter.format (lastMod);
      } else
      if (dirEntryFile.isDirectory()) {
        dirEntryType = "Directory";
      }
      
      // Type of entry: File or Directory
      fieldNumber = nextRec.addField (recDef, dirEntryType);
      
      // File name looking like a regular English name
      fieldNumber = nextRec.addField (recDef, dirEntryFileName.getFileNameEnglish());
      String ext = dirEntryFileName.getExt();

      // File name without path or extension
      fieldNumber = nextRec.addField (recDef, dirEntryFileName.getBase());
      
      // File extension
      fieldNumber = nextRec.addField (recDef, ext);
      
      // File size
      fieldNumber = nextRec.addField (recDef, size);
      
      // Date last modified
      fieldNumber = nextRec.addField (recDef, lastModDate);
      
      // Time last modified
      fieldNumber = nextRec.addField (recDef, lastModTime);
      StringScanner fileNameScanner 
          = new StringScanner (dirEntryFileName.getBase());
          
      // Individual words in file name
      for (int i = 1; i <= MAX_WORDS; i++) {
        nextRec.addField (recDef, fileNameScanner.getNextWord());
      }
      
      return nextRec;
    } // end of logic if more directory entries to return
  } // end of nextRecordIn method

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
        if (currDirDepth < maxDepth) {
          newDirToExplode = new DirToExplode (currDirDepth + 1, dirEntryFile.getAbsolutePath());
          dirList.add (newDirToExplode);
        }
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
    /** 
    ensureLog();
    logEvent.setSeverity (LogEvent.NORMAL);
    logEvent.setMessage ("DirectoryReader: " + directoryPath + " closed successfully");
    log.recordEvent (logEvent);
    */
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
