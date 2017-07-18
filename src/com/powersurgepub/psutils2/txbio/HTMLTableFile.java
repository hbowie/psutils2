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

package com.powersurgepub.psutils2.txbio;

  import com.powersurgepub.psutils2.records.*;

	import java.io.*;
  import java.net.*;

/**
   A source of parsed HTML. The HTML can be read as HTMLTag objects, or as
   DataRecord objects, if the input file consists of tabular data arranged 
   within an HTML Table. 
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
 */

public class HTMLTableFile 
  extends HTMLFile 
    implements DataSource {
    
  private final static 	int						NO_TABLE_STATUS		= 0;
  private final static 	int						START_OF_CELL			= 1;
  private final static 	int						END_OF_CELL				= 2;
  private final static 	int   				END_OF_ROW 				= 3;
  private final static 	int   				END_OF_TABLE 			= 4;
  private final static 	int   				END_OF_FILE 			= 5;
  private final static 	String   			END_OF_ROW_STR 		= "</tr>";
  private final static 	String   			END_OF_TABLE_STR 	= "</table>";
  private final static 	String   			END_OF_FILE_STR 	= "<EOF>";
  
  private 						 	StringBuffer 	text = new StringBuffer("");
  private 							int 					tableStatus 			= 0;
  private								int						rowSpan           = 1;
  private								int						colSpan						= 1;
  private								String				cellString        = "";

  private               int           tablesToSkip      = 0;
	  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name passed as a single String.
   */
  public HTMLTableFile (String inFileName) {
    super (inFileName);
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.
     
     @param inFile  The text file itself.
   */
  public HTMLTableFile (File inFile) {
    super (inFile);
  }
  
  /**
     A constructor that accepts a URL pointing to a Web resource.
     
     @param url  The URL of a text file.
   */  
  public HTMLTableFile (URL url) {
    super (url);
  }

  public void setTablesToSkip (int tablesToSkip) {
    this.tablesToSkip = tablesToSkip;
  }
  
  /**
     Performs open operations for this particular HTML interpretation rule. 
     Build record definition. 
   */
  void openWithRule () 
      throws IOException, FileNotFoundException {

    // If requested, skip the specified number of tables
    for (int i = 0; i < tablesToSkip; i++) {
      if (tableStatus >= END_OF_FILE) {
        break;
      }
      do {
        cellString = getNextTableCell();
      } while (tableStatus < END_OF_TABLE);
    }
    
    // Find first qualified table cell
    do {
      cellString = getNextTableCell();
    } while ((cellString.length() < 1
        || cellString.length() > 40
        || colSpan != 1
        || rowSpan != 1)
        && (! cellString.equals (END_OF_FILE_STR)));
        
    // Now process all table cells (column headings) in this row
    while ((! cellString.equals (END_OF_FILE_STR))
        && (! cellString.equals (END_OF_TABLE_STR))
        && (! cellString.equals (END_OF_ROW_STR))) {
      storeField (cellString, "");
      cellString = getNextTableCell();
    }
  }
  
  /**
     Retrieve the next table row.
    
     @return formatted table row or null if end of file.
   */
  public DataRecord nextRecordIn () 
      throws IOException, FileNotFoundException {
    
    // Find next table cell
    if ((! cellString.equals (END_OF_FILE_STR))
        && (! cellString.equals (END_OF_TABLE_STR))) {
      cellString = getNextTableCell();
    }
        
    // Now process all table cells (columns) in this row
    if ((cellString.equals (END_OF_FILE_STR))
        || (cellString.equals (END_OF_TABLE_STR))) {
      setAtEnd (true);
      return null;
    }
    else {
      dataRec = new DataRecord();
      while ((! cellString.equals (END_OF_FILE_STR))
          && (! cellString.equals (END_OF_TABLE_STR))
          && (! cellString.equals (END_OF_ROW_STR))) {
        dataRec.addField (recDef, cellString);
        cellString = getNextTableCell();
      }
      recordNumber++;
      if (cellString.equals (END_OF_FILE_STR)
          || cellString.equals (END_OF_TABLE_STR)) {
        setAtEnd (true);
      }
      return dataRec;
    }
  }
  
  /**
     Returns the text contained within the next table cell (td/th tag). 
    
     @return The text within the next cell.
   */
  private String getNextTableCell () 
      throws IOException, FileNotFoundException {
    do {
      readTagForTable();
    } while (tableStatus != START_OF_CELL
        && tableStatus < END_OF_ROW);
        
    while (tableStatus < END_OF_CELL) {
      readTagForTable();
      text.append (tag.getPrecedingText());   
    }
    if (tableStatus == END_OF_FILE) {
      setAtEnd (true);
      return END_OF_FILE_STR;
    }
    else
    if (tableStatus == END_OF_TABLE) {
      return END_OF_TABLE_STR;
    }
    else
    if (tableStatus == END_OF_ROW) {
      return END_OF_ROW_STR;
    }
    else {
      return text.toString();
    }    
  }
  
  /**
     Gets the next tag and evaluates its table status.
   */
  private void readTagForTable () 
      throws IOException, FileNotFoundException {
    tableStatus = NO_TABLE_STATUS;
    tag = readTag();
    if (tag == null) {
      tableStatus = END_OF_FILE;
    } 
    else
    if (tag.getName().equals ("td")
        || tag.getName().equals ("th")) {
      if (tag.isEnding()) {
        tableStatus = END_OF_CELL;
      }
      else {
        colSpan = 1;
        rowSpan = 1;
        HTMLAttribute csAttribute = tag.getAttribute ("colspan");
        if (csAttribute != null) {
          colSpan = csAttribute.getValueAsInt ();
          if (colSpan < 1) {
            colSpan = 1;
          }
        }
        HTMLAttribute rsAttribute = tag.getAttribute ("rowspan");
        if (rsAttribute != null) {
          rowSpan = rsAttribute.getValueAsInt ();
          if (rowSpan < 1) {
            rowSpan = 1;
          }
        }
        tableStatus = START_OF_CELL;
        text = new StringBuffer("");
      }
    }
    else
    if (tag.isEnding()) {
      if (tag.getName().equals ("tr")) {
        tableStatus = END_OF_ROW;
      }
      else
      if (tag.getName().equals ("table")) {
        tableStatus = END_OF_TABLE;
      }
    }
  }
}
