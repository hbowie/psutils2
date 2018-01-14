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

package com.powersurgepub.psutils2.txbio;

  import com.powersurgepub.psutils2.excel.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.tabdelim.*;
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.txbmodel.*;


  import java.io.*;
  import java.net.*;
  import java.util.*;

  import javafx.scene.control.*;

/**
  Loads TextBlocs data from tab-delimited (or similar) source files. 
 */
public class TextIOTabs 
  extends TextIOModule {
  
  public static final String  HTML_BOOKMARKS_HEADINGS 
      = "HTML Bookmarks using Headings";
  public static final String  HTML_BOOKMARKS_LISTS   
      = "HTML Bookmarks using Lists";
  public static final String  HTML_LINKS 
      = "HTML Links";;
  public static final String  HTML_TABLE 
      = "HTML Table";
  public static final String  TABDELIM 
      = "Tab-Delimited";
  public static final String  EXCEL_SPREADSHEET
      = "Excel Spreadsheet";
  public static final String  EXCEL_TABLE
      = "Excel Table";
  
  private           DataSource      source;
  
  public TextIOTabs () {
    
  }
  
  public void registerTypes (List types) {
    
    TextIOType type = new TextIOType (HTML_BOOKMARKS_HEADINGS,
        this, true, false, "");
    types.add (type);
    
    type = new TextIOType (HTML_BOOKMARKS_LISTS,
        this, true, false, "");
    types.add (type);
    
    type = new TextIOType (HTML_LINKS,
        this, true, false, "");
    types.add (type);
    
    type = new TextIOType (HTML_TABLE,
        this, true, false, "");
    types.add (type);
    
    type = new TextIOType (TABDELIM,
        this, true, true, "tab");
    type.addExtension ("tdu");
    types.add (type);
    
    type = new TextIOType (EXCEL_SPREADSHEET,
        this, true, false, "xls");
    types.add (type);
    
    type = new TextIOType (EXCEL_TABLE,
        this, true, false, "xls");
    types.add (type);
    
  }
  
  public boolean load (TextTree tree, URL url, TextIOType type, String parm) {
    boolean ok = true;
    File file = null;
    if (url.getProtocol().equals ("file")) {
      file = new File (url.getFile());
    }
    if (type.toString().equals (HTML_BOOKMARKS_HEADINGS)) {
      HTMLFile htmlSource = new HTMLFile (file);
      htmlSource.useHeadings();
      source = htmlSource;
    }
    else
    if (type.toString().equals (HTML_BOOKMARKS_LISTS)) {
      source = new HTMLFile (file);
    }
    else
    if (type.toString().equals (HTML_LINKS)) {
      source = new HTMLLinksFile (file);
    } 
    else
    if (type.toString().equals (HTML_TABLE)) {
      source = new HTMLTableFile (file);
    }
    else
    if (type.toString().equals (TABDELIM)) {
      source = new TabDelimFile (url);
    }
    else
    if (type.toString().equals (EXCEL_SPREADSHEET)
        && file != null) {
      source = new ExcelFile (file.getPath());
    }
    else
    if (type.toString().equals (EXCEL_TABLE)
        && file != null) {
      source = new ExcelTable (file.getPath());
    } else {
      ok = false;
    }
    
    if (ok) {
      try {
        source.openForInput();
        int recordNumber = 0;
        while (! source.isAtEnd()) {
          DataRecord record = source.nextRecordIn();
          if (record != null) {
            recordNumber++;
            TreeItem<TextData> recordNode 
                = tree.createNode(TextType.RECORD, String.valueOf (recordNumber));
            tree.getTextRoot().getChildren().add(recordNode);
            int fieldNumber = 0;
            while (fieldNumber < record.getNumberOfFields()) {
              DataField field = record.getField (fieldNumber);
              if (field != null) {
                TreeItem<TextData> fieldNode 
                  = tree.createNode(field.getProperName(), field.getData());
                recordNode.getChildren().add (fieldNode);
              } // end if we have a good field
              fieldNumber++;
            } // end while more fields in record
          } // end if we have a good record
        } // end while we have more records from file
        source.close();
      } catch (IOException e) {
        ok = false;
      } // end if IO error
    } // end if we have a valid source to load from

    return ok;
  } // end load method
  
  public boolean store (TextTree tree, TextWriter writer, TextIOType type,
      boolean epub, String epubSite) {
    this.tree = tree;
    boolean ok = false;
    return false;
  }

}


