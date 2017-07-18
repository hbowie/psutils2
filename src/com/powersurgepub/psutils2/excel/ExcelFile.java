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

package com.powersurgepub.psutils2.excel;

  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.strings.*;

  import java.io.*; 
  import java.net.*; 

  import jxl.*; 

/**
  Returns the first sheet of the workbook as a series of rows and columns, with
  the first row of the sheet being used as column headings. Conforms to the
  PowerSurgePub DataSource interface. <p>

  The first or only worksheet (tab) will be accessed. The first row will be
  expected to contain column headings, with data in following rows. The first
  blank row will terminate the list. Each row in the spreadsheet (after the
  first, containing headings) will be treated as a data record, and each column
  will be treated as a separate field. Columns containing hyperlinks will also
  generate fields containing the hyperlinks, and named by appending "link" to
  the column heading. For example, a column named "ISBN" could have its content
  accessed with the variable "isbn" and its link accessed with the variable
  "isbnlink".
 */
public class ExcelFile
    extends File 
        implements DataSource {
  
  /** Excel work book */
  private    Workbook           workbook;
  
  /** Index number for sheet (0 = first) */
  private    int                sheetNumber = 0;
  
  /** Last column of the spreadsheet containing data */
  private    int                lastColumn = 0;
  
  /** Sheet in the workbook */
  private    Sheet              sheet;
  
  /** Identify which columns have hyperlinks. */
  private    boolean[]          hasHyperlink;
  
  /** Hyperlinks. */
  private    Hyperlink[]        links;
  
  /** Data dictionary to be used for the file. */
  private    DataDictionary     dict;
  
  /** Record definition to be used for the file. */
  private    RecordDefinition   recDef;
  
  /** Number of non-blank columns in spreadsheet. */
  private    int                columns = 0;
  
  /** Sequential number identifying last record read or written. */
  private    int                recordNumber;
  
  /** Row in spreadsheet */
  private    int                row = 0;
  
  /** Is the file at end? */
  private    boolean            atEnd = false;
  
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
     A constructor that accepts a single File object
     representing the file.</p>
     
     @param inFile  The text file itself.
   */
  public ExcelFile (String inFile) {
    super (inFile);
    // System.out.println ("ExcelFile constructor " + inFile);
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
    recDef = new RecordDefinition(dict);
    try {
      workbook = Workbook.getWorkbook (this);
    } catch (jxl.read.biff.BiffException e) {
      throw (new java.io.IOException());
    }
    sheet = workbook.getSheet (sheetNumber);
    
    // Load column definitions from headings found in first row
    int column = 0;
    row = 0;
    String field = " ";
    while (field.length() > 0 && column < sheet.getColumns()) {
      field = getCell (column, row);
      if (field.length() > 0) {
        recDef.addColumn (field);
        column++;
      } // end if we have another field name
    } // end while more field names
    columns = recDef.getNumberOfFields();
    
    // Load any hyperlink columns
    loadHyperlinks();    
    recordNumber = 0;
    row++;
    if (recDef.getNumberOfFields() > 0) {
      atEnd = false;
    } else {
      atEnd = true;
    }
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
    try {
      workbook = Workbook.getWorkbook (this);
    } catch (jxl.read.biff.BiffException e) {
      throw (new java.io.IOException());
    }
    sheet = workbook.getSheet (sheetNumber);
    columns = recDef.getNumberOfFields();
    loadHyperlinks();
    recordNumber = 0;
    row = 0;
    atEnd = false;
  }
  
  private void loadHyperlinks () {
    // System.out.println ("Attempting to load hyperlinks...");
    links = sheet.getHyperlinks();
    // System.out.println (String.valueOf (links.length) + " hyperlinks loaded");
    /* for (int i = 0; i < links.length; i++) {
      System.out.println ("  " + String.valueOf (i) + " " 
          + String.valueOf (links[i].getColumn()) + " "
          + String.valueOf (links[i].getRow()) + " "
          + links[i].getURL().toString()
          );
    } */
    
    // Now add fields for any hyperlinks
    int column = 0;
    hasHyperlink = new boolean [sheet.getColumns()];
    while (column < columns) {
      boolean found = false;
      int i = 0;
      while ((! found) && (i < links.length)) {
        Hyperlink link = (Hyperlink)links[i];
        if (column == link.getColumn()) {
          found = true;
          // System.out.println ("Column " 
          //     + String.valueOf (column) + " has hyperlinks");
        }
        i++;
      }
      if (found) {
        DataFieldDefinition def = recDef.getDef (column);
        String linkColumnName = def.getCommonName() + "link";
        // System.out.println ("Adding column " + linkColumnName);
        recDef.addColumn (linkColumnName);
        hasHyperlink[column] = true;
      } else {
        hasHyperlink[column] = false;
      }
      column++;
    }
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
    
    if (row < sheet.getRows()) {
      DataRecord nextRec = new DataRecord ();
      int column = 0;
      int nonBlankFields = 0;
      String field = " ";
      
      // Load all the data columns
      while (column < sheet.getColumns()) {
        field = getCell (column, row);
        if (field.length() > 0) {
          nonBlankFields++;
        }
        int i = nextRec.addField (recDef, field);
        column++;
      }
      
      // Now add any hyperlinks
      for (int i = 0; i < hasHyperlink.length; i++) {
        if (column < recDef.getNumberOfFields()) {
          if (hasHyperlink [i]) {
            String linkString = "";
            boolean found = false;
            int j = 0;
            while ((! found) && (j < links.length)) {
              Hyperlink link = (Hyperlink)links[j];
              if (i == link.getColumn()
                  && row == link.getRow()) {
                found = true;
                if (link.isURL()) {
                  URL url = link.getURL();
                  linkString = url.toString();
                }
                else
                if (link.isFile()) {
                  File file = link.getFile();
                  linkString = file.toString();
                }
              } // end if this hyperlink is the one for this row and column
              j++;
            } // end while looking for hyperlinks
            if (linkString.length() > 0) {
              nonBlankFields++;
            }
            int c = nextRec.addField (recDef, linkString);
            column++;
          } // end if this column has any hyperlinks
        } // end if we have more fields defined for the record
      } // end for each column in spreadsheet
      
      // Now do record processing
      if (nonBlankFields > 0) {
        nextRec.calculate();
        recordNumber++;
        row++;
        return nextRec;
      } else {
        atEnd = true;
        return null;
      }
    } else {
      atEnd = true;
      return null;
    }
  } // end method nextRecordIn
  
  /**
    Returns the contents of the given cell as a String, with leading and
    trailing quotation marks and spaces removed.
   */
  public String getCell (int column, int row) {
    return StringUtils.removeQuotes
        (sheet.getCell (column, row).getContents()).trim();
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
     Indicates whether the file has reached its end.</p>
    
     @return    True if file is at end, false if there are more records to read.
   */
  public boolean isAtEnd() {
    return atEnd;
  }
  
  /**
    Close this file.
   
    @throws IOException If there is trouble opening the disk file.
   */
  public void close () 
      throws IOException {
    workbook.close();
    /*
    if (openedForAppend) {
      tempTab.close();
      File oldFile = new File (dataParent, 
					fileNameObject.getBase() 
					+ "_old." 
					+ fileNameExt);
      if (oldFile.exists()) {
        oldFile.delete();
      }
			if (exists()) {
				boolean renameOK = renameTo (oldFile);
			}
      boolean renameOK = tempFile.renameTo 
          (new File (dataParent.toString(), fileNameObject.toString()));
      openedForAppend = false;
    } */
  }
  
  /**
     Retrieves the path to the original source file (if any).
    
     @return Path to the original source file (if any).
   */
  public String getDataParent () {
    return getParent();
  }
  
  /**
     Sets the file ID to be passed to the Logger.
    
     @param fileId Used to identify the source of the data being logged.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    // logData.setSourceId (fileId);
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
  
  public void setMaxDepth(int maxDepth) {
  
  }
  
}
