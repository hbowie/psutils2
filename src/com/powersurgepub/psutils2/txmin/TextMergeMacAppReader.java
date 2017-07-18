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

package com.powersurgepub.psutils2.txmin;

	import com.powersurgepub.psutils2.basic.*;
	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.strings.*;

  import java.io.*;
  import java.net.*;
  import java.util.*;
  import java.text.*;

/**
   A directory entry reader that creates a data record from the passed file. <p>
 */
 public class TextMergeMacAppReader 
    extends     File
    implements  DataSource {

  /** The name of the application. */
  public  static   final  String  MAC_APP_NAME = "Name";
  
  /** A link to the application.  */
  public  static   final  String  MAC_APP_FILE = "Link";
  
  /** The path from the starting folder to the file. */
  public  static   final  String  MAC_APP_PATH = "Path";
    
  /** The name of the last modification date field within the directory record. */
  public  static   final  String  MAC_APP_LAST_MOD_DATE = "Date";
  
  /** The version number of the app. */
  public  static   final  String  MAC_APP_VERSION = "Version";
  
  public  static   final  String  MAC_APP_TAGS = "Tags";
  
  public  static   final  String  PUBLIC_APP_CATEGORY = "public.app-category.";
  
  public  static   final  String  MAC_APP_BODY = "Body";
  
  /** 
     The number of levels of directories and sub-directories to be read. 
     A value of 1 (the default) indicates that only the top level directory
     should be read. A value of 2 indicates one level of sub-directories, and
     so forth.
   */
  private		 int							maxDepth = 0;
  
  private		 int							currDirDepth;
  
  /** The number of directories in the top directory to be read. */
  private		 int							directoryNumberOfFolders;

  /** The logger to use to log events. */    
  private    Logger           log;
  
  /* Let's not log all data. */
  private    boolean          dataLogging = false;
  
  /** The data dictionary to be used by this record. */
  private    DataDictionary   dict = null;
  
  /** The record definition to be used by this record. */
  private    RecordDefinition recDef = null;
  
  /** Pointer to a particular record within the array. */
  private    int              recordNumber;
  
  private    boolean          atEnd = false;
  
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
  
  private    InfoPlistReader  plistReader = null;
  private    boolean          plistOK = false;
  
  private    String name = "";
  private    String appName = "";
  private    String tags = "";
  private    String fileLink = "";
  private    String dirEntryType = "?";
  private    String size = " ";
  private    String lastModDate = " ";
  private    String lastModTime = " ";
  private    String version = "";
  private    String minSysVersion = "";
  private    String copyright = "";
  private    String body = "";

  /**
     Constructs a directory reader given a path defining the directory
     to be read.
    
     @param  inPath Directory path to be read.
   */
  public TextMergeMacAppReader (String inPath) {
    super (inPath);
    initialize();
  }

  /**
     Constructs a directory reader given a file object 
     defining the directory to be read.
    
     @param  inPathFile Directory path to be read.
   */
  public TextMergeMacAppReader (File inPathFile) {
    super (inPathFile.getAbsolutePath());
    initialize();
  }
  
  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "directory".
   */
  private void initialize () {
    fileId = "mac-apps";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
    plistReader = new InfoPlistReader();
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
  
  public void setCurrDirDepth(int currDirDepth) {
    this.currDirDepth = currDirDepth;
  }
  
  public void setDirectoryNumberOfFolders (int directoryNumberOfFolders) {
    this.directoryNumberOfFolders = directoryNumberOfFolders;
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
    if (recDef == null) {
      buildRecDef();
    }
    recordNumber = 0;
    atEnd = false;
  } // end of openForInput method

  /**
     Returns the next directory entry.
    
     @return Next directory entry as a data record.
   */
  public DataRecord nextRecordIn () {
    if (recordNumber > 0) {
      atEnd = true;
      return null;
    } else {
      recordNumber++;
      DataRecord nextRec = new DataRecord ();
      int fieldNumber;
      FileName fileName = new FileName (this);
      
      retrieveMacAppInfo();
      
      // App name
      fieldNumber = nextRec.addField (recDef, appName);
      
      // App Category
      fieldNumber = nextRec.addField(recDef, tags);
      
      // Link to file
      fieldNumber = nextRec.addField (recDef, fileLink);
      
      // Date last modified
      fieldNumber = nextRec.addField (recDef, lastModDate);
      
      // Version
      fieldNumber = nextRec.addField (recDef, version);
      
      // Body
      fieldNumber = nextRec.addField (recDef, body);
      
      return nextRec;
    } // end of logic if more directory entries to return
  } // end of nextRecordIn method
  
  public void retrieveMacAppInfo() {
    ensureLog();
    File contents = new File (this, "Contents");
    File infoPlist = new File (contents, "info.plist");
    plistOK = plistReader.readInfoPlistFile(infoPlist);

    name = this.getName();
    appName = name.substring(0, name.length() - 4);

    tags = "";
    if (plistOK) {
      tags = plistReader.getCategory();
      if (tags.startsWith(PUBLIC_APP_CATEGORY)) {
        tags = tags.substring(PUBLIC_APP_CATEGORY.length());
      }
    }

    fileLink = this.toString();
    try {
      String webPage = this.toURI().toURL().toString();
      fileLink = StringUtils.tweakAnyLink(webPage, false, false, false, "");
    } catch (MalformedURLException e) {
      // do nothing
    }

    dirEntryType = "?";
    size = " ";
    lastModDate = " ";
    lastModTime = " ";
    if (this.isFile()) {
      dirEntryType = "File";
      size = String.valueOf (this.length());
      Date lastMod = new Date (this.lastModified());
      lastModDate = dateFormatter.format (lastMod);
      lastModTime = timeFormatter.format (lastMod);
    } else
    if (this.isDirectory()) {
      dirEntryType = "Directory";
      size = String.valueOf (this.length());
      Date lastMod = new Date (this.lastModified());
      lastModDate = dateFormatter.format (lastMod);
      lastModTime = timeFormatter.format (lastMod);
    }

    version = "";
    body = "";
    minSysVersion = "";
    copyright = "";
    if (plistOK) {
      version = plistReader.getVersion();
      minSysVersion = plistReader.getMinSysVersion();
      copyright = plistReader.getCopyright();
      body = "Minimum System Version: " + minSysVersion
          + GlobalConstants.LINE_FEED_STRING
          + "Copyright: " + copyright;
    }
  }
  
  public String getAppName() {
    return appName;
  }
  
  public String getTags() {
    return tags;
  }
  
  public String getFileLink() {
    return fileLink;
  }
  
  public String getLastModDate() {
    return lastModDate;
  }
  
  public String getVersion() {
    return version;
  }
  
  public String getMinSysVersion() {
    return minSysVersion;
  }
  
  public String getCopyright() {
    return copyright;
  }
  
  public String getBody() {
    return body;
  }

  /**
     Returns the record definition for the reader.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef () {
    if (recDef == null) {
      buildRecDef();
    }
    return recDef;
  }
  
  private void buildRecDef() {
    if (dict == null) {
      dict = new DataDictionary();
    }
    recDef = new RecordDefinition (dict);
    recDef.addColumn (MAC_APP_NAME);
    recDef.addColumn (MAC_APP_TAGS);
    recDef.addColumn (MAC_APP_FILE);
    recDef.addColumn (MAC_APP_LAST_MOD_DATE);
    recDef.addColumn (MAC_APP_VERSION);
    recDef.addColumn (MAC_APP_BODY);
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
    return (atEnd);
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
     Retrieves the path to the original source data (if any).
    
     @return Path to the original source data (if any).
   */
  public String getDataParent () {

    return this.getParent();
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