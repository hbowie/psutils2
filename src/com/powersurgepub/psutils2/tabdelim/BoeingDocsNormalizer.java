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
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.strings.*;

  import java.io.*;
  import java.util.*;
  
/**
   An object that will take standard file directory objects from DirectoryReader
   and, assuming that the directory contains folders and files with names 
   following a Boeing Mesa IT standard, will convert them to a standard set
   of columns. 
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>) 
 */
public class BoeingDocsNormalizer 
    implements DataSource {
    
  /** The name of the column headings used in the output record definition. */
  public  static   final  String  COLUMN_NAME_SYSTEM_ID 		= "System ID";
  public  static   final  String  COLUMN_NAME_CHANGE_NUMBER = "Change Number";
  public  static   final  String  COLUMN_NAME_SUB_FOLDER 		= "Sub-Folder";
  public  static   final  String  COLUMN_NAME_STAGE 				= "Stage";
  public  static   final  String  COLUMN_NAME_FILE_NAME 		= "File Name";
  public  static   final  String  COLUMN_NAME_PATH 					= "Path";
  public  static   final  String  COLUMN_NAME_DOC_TYPE_CODE	= "Doc Type";
  public  static   final  String  COLUMN_NAME_DOC_TYPE_DESC = "Doc Type Desc";
  public  static   final  String  COLUMN_NAME_LAST_MOD_DATE = "Last Mod Date";
	public 	static 	 final  String  COLUMN_NAME_ERRORS        = "Errors";
  public  static   final  String  DELIMS                    = "-_.";
    
  /** The source of data to be normalized. */
  private    DataSource					inData;
  
  /** Maximum number of records to be processed. */
  private		 int 								maxRecs = -1;
  
  /** Next available number to be assigned to the next data set instantiated. */
  private    static  int        dataSetNumber = 0;
  
  /** Definition of the records being passed to the normalizer. */
  private    RecordDefinition   inRecDef;
  
  /** Definition of the records being passed back from the normalizer. */
  private    RecordDefinition   recDef;
  
  /** Definition of the next File record read from the input data source. */
  private    DataRecord					nextFileRec;
  
  /** Number of last record returned. */
  private    int                lastRecordNumber;
  
  /** Number of next record to be returned. */
  private    int                recordNumber;
  
  /** Path to be used to find lookup files. */
  private		 String							dataParent = "";
  
  /** Lookup table for governing document types. */
  private    TabDelimLookup			govDocTypes;
  
  /** Lookup table for System document types. */
  private		 TabDelimLookup     sysDocTypes;
  
  /** Data dictionary used by the record definition. */
  private    DataDictionary     dict;
  
  /** Should all data be logged (or only data preceding significant events(? */
  private    boolean            dataLogging = false;
  
  /** Log used to record events. */
  private    Logger             log;
  
  /** Data to be logged. */
  private    LogData            logData;
  
  /** Event to be logged. */
  private    LogEvent           logEvent;
  
  /** Identifier used to identify this reader in the log. */
  private    String             fileId;
  
  /** Flag used to indicate that we have reached the end of the input data. */
  private    boolean            atEnd = false;
  
  /** 
     Constructs a normalizer from another DataSource implementation, 
     using the passed data dictionary.
     The passed data source provides the record definition, as well as
     all of its records.
    
     @param inDict Data Dictionary to be used.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public BoeingDocsNormalizer (DataDictionary inDict, DataSource inData) 
      throws IOException {
    dict = inDict;
    this.inData = inData;
    initialize();
  }
  
  /** 
     Constructs a normalizer from another DataSource implementation, 
     using the passed data dictionary.
     The passed data source provides all of its records.
     A maximum record count is also passed, to limit the size
     of the data set. 
    
     @param inDict Data Dictionary to be used.
    
     @param inData Data source that allows data records to be read.
    
     @param maxRecs Maximum number of records to be loaded.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public BoeingDocsNormalizer 
      (DataDictionary inDict, DataSource inData, int maxRecs) {
    dict = inDict;
    this.inData = inData;
    this.maxRecs = maxRecs;
    initialize();
  }
  
  /** 
     Constructs a normalizer from another DataSource implementation.
     The passed data source provides the record definition, as well as
     all of its records.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public BoeingDocsNormalizer (DataSource inData) 
      throws IOException {
    dict = new DataDictionary();
    this.inData = inData;
    initialize();
  }
  
  /**
     Common initialization code for all constructors.
   */
  private void initialize () {
    dataSetNumber++;
    fileId = "DataSet" + String.valueOf (dataSetNumber);
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }
  
  /**
     Retrieves the path to the original source file (if any).
    
     @return Path to the original source file (if any).
   */
  public String getDataParent () {
    if ((dataParent == null) || (dataParent.equals(""))) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return dataParent;
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
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  /**
     Indicates whether all data should be logged.
    
     @param dataLoging True if all data should be logged,
                       false if only data preceding significant
                       events should be logged.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
  /**
     Overrides the default identifier assigned to this data set.
    
     @param fileId Identifier to be used for this data set.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }
  
  /**
     Does nothing in this class.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {

  }

  /**
     Allocate a new Logger instance if one has not yet been provided.
   */
  private void ensureLog () {
    if (log == null) {
      log = new Logger (new LogOutput());
    }
  }
  
  /** 
     Returns this object as some kind of String.
    
     @return Concatenation of all the records in the set,
             separated by line feeds.
   */
  public String toString () {
    return ("BoeingDocsNormalizer");
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
    atEnd = false;
    ensureLog();
    dict = inDict;
    inData.openForInput (dict);
    inRecDef = inData.getRecDef();
    recDef = new RecordDefinition (dict);
    recDef.addColumn (COLUMN_NAME_SYSTEM_ID);
    recDef.addColumn (COLUMN_NAME_CHANGE_NUMBER);
    recDef.addColumn (COLUMN_NAME_SUB_FOLDER);
    recDef.addColumn (COLUMN_NAME_STAGE);
    recDef.addColumn (COLUMN_NAME_FILE_NAME);
    recDef.addColumn (COLUMN_NAME_PATH);
    recDef.addColumn (COLUMN_NAME_DOC_TYPE_CODE);
    recDef.addColumn (COLUMN_NAME_DOC_TYPE_DESC);
    recDef.addColumn (COLUMN_NAME_LAST_MOD_DATE);
		recDef.addColumn (COLUMN_NAME_ERRORS);
    govDocTypes = new TabDelimLookup (dataParent, 
        "gov_doc_types.txt", "Code", false);
    sysDocTypes = new TabDelimLookup (dataParent,
        "sys_doc_types.txt", "Code", false);
    recordNumber = 0;
    nextFileRec = getNextDocsFile();
  } // end of openForInput method
  
  /**
     Frees up resources associated with open.
   */
  public void close() 
      throws IOException {
    inData.close();
  }
  
  /**
     Indicates whether the last record has already been returned.
    
     @return True if the last record has already been returned.
   */
  public boolean isAtEnd () {
    return atEnd;
  }
  
  /**
     Returns the next data record from the set.
    
     @return Next data record
   */
  public DataRecord nextRecordIn () 
      throws IOException {
    if (nextFileRec == null) {
      return null;
    } else {
      // start building next output record
      DataRecord nextRec = new DataRecord();
   
      // Create variables to hold desired output fields
      String sortKey = nextFileRec.getFieldData (DirectoryReader.DIR_ENTRY_SORT_KEY);
      String folder1 = nextFileRec.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "1");
      String folder2 = nextFileRec.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "2");
      String folder3 = nextFileRec.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "3");
      String folder4 = nextFileRec.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "4");
      
      String sysFolder = "";
      String subFolder = "";
      String subSubFolder = "";
      int pathBeginFolder = 1;
      if (folder2.indexOf("docs") > 3) {
        sysFolder = folder2;
        subFolder = folder3;
        pathBeginFolder = 3;
        subSubFolder = folder4;
      } 
      else
      if (folder1.indexOf("docs") > 3) {
        sysFolder = folder1;
        subFolder = folder2;
        pathBeginFolder = 2;
        subSubFolder = folder3;
      } else {
        int i = sortKey.lastIndexOf ("docs");
        int j = i + 4;
        while (i >= 0 && sortKey.charAt(i) != ' ') {
          i--;
        }
        i++;
        sysFolder = sortKey.substring (i, j);
        
        subFolder = folder1;
        pathBeginFolder = 1;
        subSubFolder = folder2;
      }
      
      StringBuffer errors = new StringBuffer();
      
      String sysID = "";
      if (sysFolder.endsWith ("docs")
          && sysFolder.length() >= 7) {
        sysID = sysFolder.substring (0, sysFolder.length() - 4).toUpperCase();
      } else {
        errors.append (", Sysdocs Folder Not Found");
      }
      
      String fileName = nextFileRec.getFieldData (DirectoryReader.DIR_ENTRY_NAME);
      
      // scan file name for possible change number and for document type
      StringScanner nameScanner = new StringScanner (fileName.toUpperCase());
      String fileNameWord1 = nameScanner.getNextString (DELIMS);
      String invalidDocType = "";
      String docType = "";
      String docTypeDesc = "";
      boolean docTypeFound = false;
      int changeNumberStart = 0;
      int changeNumberEnd = 0;
      String changeNumber = "";
      if (fileNameWord1.equalsIgnoreCase (sysID)
          || (sysID.equals("APPL") && fileNameWord1.equals("AP"))
          || (fileNameWord1.length() > 2
              && fileNameWord1.length() < 6)) {
        changeNumberStart = nameScanner.getIndex() + 1;
        boolean more = true;
        do {
          if (nameScanner.moreChars() 
              && nameScanner.getNextChar() != '.') {
            changeNumberEnd = nameScanner.getIndex();
            docType = nameScanner.getNextString (DELIMS);
            boolean docTypeNumeric = true;
            try {
              int dti = Integer.parseInt (docType);
            } catch (NumberFormatException e) {
              docTypeNumeric = false;
            }
            if (docType.length() == 2) {
              docTypeDesc = sysDocTypes.get (docType, "Meaning");
              if (docTypeDesc == null) {
                if ((invalidDocType.length() < 1) && (! docTypeNumeric)) {
                  invalidDocType = docType;
                }
                // errors.append 
                //    (", Description Not Found For System Doc Type " 
                //        + docType );
              } else {
                docTypeFound = true;
              }
            }
            else
            if (docType.length() == 3) {
              docTypeDesc = govDocTypes.get (docType, "Meaning");
              if (docTypeDesc == null) {
                if ((invalidDocType.length() < 1) && (! docTypeNumeric)) {
                  invalidDocType = docType;
                }
                // errors.append 
                //     (", Description Not Found For Governing Doc Type " 
                //        + docType );
              } else {
                docTypeFound = true;
              }
            }
          } else {
            more = false;
          }
        } while ((! docTypeFound) && more);
        if (docTypeFound
            && ((changeNumberEnd - changeNumberStart) > 1)) {
          changeNumber = nameScanner.substring (changeNumberStart, changeNumberEnd).trim();
        } 
        if (! docTypeFound) {
          docType = invalidDocType;
          docTypeDesc = "";
          errors.append (", File Name Does Not Contain a Valid Doc Type");
        }
      } else {
        errors.append (", File Name does not begin with System ID");
      }
      
      // If there is a sub-folder, see if we can find what looks
      // like a change number in the sub-folder. 
      if (subFolder.length() > 0) {
        StringScanner folderScanner = new StringScanner (subFolder.toUpperCase());
        changeNumberStart = 0;
        changeNumberEnd = 0;
        int wordCount = 0; 
        boolean more = true;
        boolean numbersFound = false;
        String nextWord;
        do {
          changeNumberEnd = folderScanner.getIndex();
          if (folderScanner.moreChars()) {
            nextWord = folderScanner.getNextString (DELIMS);
            wordCount++;
            if (wordCount == 1
                && nextWord.equalsIgnoreCase (sysID)) {
              changeNumberStart = folderScanner.getIndex() + 1;
              wordCount = 0;
            } else {
              if (wordCount <= 3) {
                String intTest = "no";
                boolean validInt = true;
                boolean cPrefix = false;
                if (wordCount == 1
                    && nextWord.length() > 1
                    && nextWord.charAt (0) == 'C') {
                  intTest = nextWord.substring (1);
                  cPrefix = true;
                } 
                else {
                  intTest = nextWord;
                }
                try {
                  int result = Integer.parseInt (intTest);
                } catch (NumberFormatException e) {
                  validInt = false;
                }
                if (wordCount > 1 && (! validInt)) {
                  more = false;
                }
                if (validInt) {
                  numbersFound = true;
                }
              } // end if wordCount <= 3
              else { 
                more = false;
              } // end wordCount > 3
            } // end if not leading SysID
          } // end if folderScanner has more characters
          else {
            more = false;
          }
        } while (more);
        if (numbersFound
            && ((changeNumberEnd - changeNumberStart) > 2)) {
          if (changeNumber.length() == 0) {
            changeNumber = folderScanner.substring(changeNumberStart, changeNumberEnd).trim();
          }
          subFolder = subSubFolder;
        } 
      } // end if checking sub-folder for a change number
      if (changeNumber.length() == 0) {
        changeNumber = "none";
      } else {
        if (changeNumber.charAt(0) != 'C') {
          changeNumber = "C" + changeNumber;
        }
        if (changeNumber.indexOf ('_') >= 0) {
          changeNumber = changeNumber.replace ('_', '-');
        }
      }
      
      String stage = "unknown";
      if (folder1.equalsIgnoreCase ("in_work")) {
        stage = "In Work";
      }
      else
      if (folder1.equalsIgnoreCase ("published")) {
        stage = "Published";
      }
      else
      if (folder1.equalsIgnoreCase ("approved")) {
        stage = "Approved";
      }
      else
      if (sortKey.indexOf ("in work") >= 0) {
        stage = "In Work";
      }
      else
      if (sortKey.indexOf ("published") >= 0) {
        stage = "Published";
      }
      else
      if (sortKey.indexOf ("approved") >= 0) {
        stage = "Approved";
      } else {
        errors.append (", Stage of Document Could Not Be Determined");
      }
      
      int fieldNumber;
      
      // Supply System ID
      fieldNumber = nextRec.addField (recDef, sysID);
      
      // Supply Change Number
      fieldNumber = nextRec.addField (recDef, changeNumber);
      
      // Supply Sub-Folder
      fieldNumber = nextRec.addField (recDef, subFolder);
      
      // Supply Stage
      fieldNumber = nextRec.addField (recDef, stage);
      
      // Supply File Name
      fieldNumber = nextRec.addField (recDef, fileName);
        
      // Supply Path
      StringBuffer path = new StringBuffer();
      if (pathBeginFolder <= 1) {
        path.append (folder1);
      }
      
      if (pathBeginFolder <= 2 && folder2.length() > 0) {
        if (path.length() > 0) {
          path.append ("/");
        }
        path.append (folder2);
      }
      
      if (pathBeginFolder <= 3 && folder3.length() > 0) {
        if (path.length() > 0) {
          path.append ("/");
        }
        path.append (folder3);
      }
      
      if (pathBeginFolder <= 4 && folder4.length() > 0) {
        if (path.length() > 0) {
          path.append ("/");
        }
        path.append (folder4);
      }
      
      fieldNumber = nextRec.addField (recDef, path.toString());
      
      // Supply Doc Type
      fieldNumber = nextRec.addField (recDef, docType);
      
      // Supply Doc Type Desc
      fieldNumber = nextRec.addField (recDef, docTypeDesc);
      
      // Supply Last Modification Date for the file
      fieldNumber = nextRec.addField (recDef, 
          nextFileRec.getFieldData (DirectoryReader.DIR_ENTRY_LAST_MOD_DATE));
      
      // Supply Errors
      if (errors.length() > 2) {
        errors.delete (0, 2);
      }
      fieldNumber = nextRec.addField (recDef, errors.toString());

      if (dataLogging) {
        ensureLog();
        logData.setData (nextRec.toString());
        logData.setSequenceNumber (recordNumber);
        log.nextLine (logData);
      }
      // System.out.println ("nextRecordIn returning " + nextRec.toString());
      lastRecordNumber = recordNumber;
      recordNumber++;
      nextFileRec = getNextDocsFile();
      /* if (nextFileRec == null) {
        // System.out.println ("nextRecordIn next input record is null");
      } else {
        // System.out.println ("nextRecordIn next input record is " + nextFileRec.toString());
      }
      if (isAtEnd()) {
        System.out.println ("nextRecordIn is at end");
      } */
      return nextRec;
    } // end non-null input record
  } // end method nextRecordIn
  
  /**
     Gets next input record that represents a file (rather than a directory).
    
     @return Next DataRecord from the input data source that
             represents a file.
   */
  public DataRecord getNextDocsFile () 
      throws IOException {
    DataRecord nextRecIn;
    String dirType = "";
    String fileName = "";
    String folder1 = "";
    String folder2 = "";
    String folder3 = "";
    String folder4 = "";
    do {
      nextRecIn = inData.nextRecordIn();
      if (nextRecIn == null) {
        atEnd = true;
      }
      
      if (nextRecIn != null) {
        // System.out.println ("getNextDocsFile reading " + nextRecIn.toString());
        dirType  = nextRecIn.getFieldData (DirectoryReader.DIR_ENTRY_TYPE);
        fileName = nextRecIn.getFieldData (DirectoryReader.DIR_ENTRY_NAME);
        folder1  = nextRecIn.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "1");
        folder2  = nextRecIn.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "2");
        folder3  = nextRecIn.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "3");
        folder4  = nextRecIn.getFieldData (DirectoryReader.DIR_ENTRY_FOLDER + "4");
      }
    } while ((! isAtEnd())
        && ((! dirType.equals("File"))
          // || ((! folder1.endsWith("docs"))
          //    && (! folder2.endsWith("docs")))
          || (folder1.startsWith ("index"))
          || (fileName.startsWith("Backup of"))
          || (fileName.startsWith("Copy of"))
          || (fileName.startsWith("Shortcut to"))
          || (fileName.startsWith("~"))
          || (folder3.equals ("images"))
          || (folder3.equals ("image"))
          || (folder3.startsWith ("_"))
          || (folder4.equals ("images"))
          || (folder4.equals ("image"))
          || (folder4.startsWith ("_"))));
    
    /* if (nextRecIn == null) {
      // System.out.println ("getNextDocsFile returning null record");
    } else {
      // System.out.println ("getNextDocsFile returning " + nextRecIn.toString());
    } */

    return nextRecIn;
  }
  
  /**
     Returns the record definition used by this data source.
    
     @return Record definition used by this data source.
   */
  public RecordDefinition getRecDef() {
    return recDef;
  }
  
  /**
     Returns a Vector containing the names of all the fields
     stored in the data set.
    
     @return Proper names of all the fields.
   */
  public ArrayList getNames() {
    return recDef.getNames();
  }
  
  /**
     Returns the record number of the last data record returned.
    
     @return Record number of the last data record returned.
   */
  public int getLastRecordNumber() {
    return lastRecordNumber;
  }
  
  /**
     Returns the record number of the next data record to be returned.
    
     @return Record number of the next data record returned.
   */
  public int getRecordNumber() {
    return recordNumber;
  }
  
  /**
     Return the number of fields in each record.
    
     @return Number of fields stored in each record.
   */
   
  public int getNumberOfFields () {
    return recDef.getNumberOfFields();
  }
  
} // end class DataSet
