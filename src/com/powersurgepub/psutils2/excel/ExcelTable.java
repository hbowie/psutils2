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

package com.powersurgepub.psutils2.excel;

  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;

  import java.io.*; 
  import jxl.*; 
  import jxl.format.*;


/**
  Returns the first sheet of the workbook as formatted HTML table data cells.
  Conforms to the PowerSurgePub DataSource interface. <p>

  The first or only worksheet (tab) will be accessed. The first blank row will
  terminate the list. With this option, each row in the spreadsheet will be
  returned as a single data field, identified by a variable name of "Table Row".
  The data returned will include beginning and ending td tags for each column,
  with appropriate formatting and cell dimensions and hyperlinks, mimicking the
  format of the Excel spreadsheet as closely as possible. The data returned does
  not include beginning or ending tr tags.
 
  @author Herb Bowie
 */
public class ExcelTable
    extends File 
        implements DataSource {
  
  /** A representation of an Excel worksheet as a table. */
  private    Table              table;
  
  /** Data dictionary to be used for the file. */
  private    DataDictionary     dict;
  
  /** Record definition to be used for the file. */
  private    RecordDefinition   recDef;
  
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
  
  private  String       eol;
  
  /**
     A constructor that accepts a single File object
     representing the file.</p>
     
     @param inFile  The text file itself.
   */
  public ExcelTable (String inFile) {
    super (inFile);
    eol = System.getProperty ("line.separator");
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
    loadTable();
    
    // Load one column definition for formatted HTML table rows
    recDef.addColumn ("Table Row");
       
    recordNumber = 0;
    row = 0;
    if (table.getColumns() > 0
        && table.getRows() > 0) {
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
    loadTable();
    recordNumber = 0;
    row = 0;
    if (table.getColumns() > 0
        && table.getRows() > 0) {
      atEnd = false;
    } else {
      atEnd = true;
    }
  }
  
  private void loadTable () 
      throws IOException {
    Workbook workbook;
    try {
      workbook = Workbook.getWorkbook (this);
    } catch (jxl.read.biff.BiffException e) {
      throw (new java.io.IOException());
    }
    Sheet sheet = workbook.getSheet (0);
    table = new Table (sheet);
    workbook.close();
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
    
    if (row < table.getRows()) {
      DataRecord nextRec = new DataRecord ();
      int column = 0;
      StringBuffer field = new StringBuffer();
      
      // Load all the data columns
      while (column < table.getColumns()) {
        TableCell pscell = table.get (column, row);
        Cell cell = pscell.getCell();
        Hyperlink link = pscell.getHyperlink();
        String contents = cell.getContents();
        jxl.format.CellFormat format = cell.getCellFormat();

        if ((! pscell.isMerged()) || (pscell.isMergedAnchor())) {
          
          // Build the table data tag
          field.append ("    <td class=pspubxcell");
          
          if (format != null) {
            Alignment align = format.getAlignment();
            VerticalAlignment valign = format.getVerticalAlignment();

            // Set the align attribute if necessary
            if (align == Alignment.RIGHT) {
              field.append (" align=right");
            } else 
            if (align == Alignment.CENTRE) {
              field.append (" align=center");
            }

            // Set the vertical alignment attribute
            if (valign == VerticalAlignment.TOP) {
              field.append (" valign=top");
            } 
            else
            if (valign == VerticalAlignment.CENTRE) {
              field.append (" valign=middle");
            } 
            else
            if (valign == VerticalAlignment.BOTTOM) {
              field.append (" valign=bottom");
            } 
          }
          
          // Set cell dimensions if not 1 x 1  
          if (pscell.isMerged() && pscell.getColspan() > 1) {
            field.append (" colspan=" + String.valueOf (pscell.getColspan()));
          }
          if (pscell.isMerged() && pscell.getRowspan() > 1) {
            field.append (" rowspan=" + String.valueOf (pscell.getRowspan()));
          }
          field.append (">" + eol);
          if (link != null) {
            field.append ("      <a href=\"");
            if (link.isURL()) {
              field.append (link.getURL());
            }
            else
            if (link.isFile()) {
              field.append (link.getFile());
            }
            field.append ("\">" + eol);
          }
          if (contents.length() > 0) {
            field.append ("        " + contents + eol);
          } else {
            field.append ("        " + "&nbsp;" + eol);
          }
          if (link != null) {
            field.append ("      </a>" + eol);
          }
          field.append ("    </td>" + eol);
        }
        column++;
      }
      
      DataField data = new DataField (recDef, 0, "");
      data.setDataRaw (field.toString());
      int i = nextRec.addField (data);
      
      // Now add any hyperlinks
      /*
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
       */
      
      // Now do record processing
      nextRec.calculate();
      recordNumber++;
      row++;
      return nextRec;
    } else {
      atEnd = true;
      return null;
    }
  } // end method nextRecordIn
  
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

