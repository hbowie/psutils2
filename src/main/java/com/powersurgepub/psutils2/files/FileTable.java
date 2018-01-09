/*
 * Copyright 2015 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.files;

  import java.io.*;
  import java.text.*;
  import java.util.*;

 	import javafx.collections.*;
 	import javafx.scene.control.*;
  import javafx.scene.control.cell.*;

/**
 A Table Model containing a list of files. 

 @author Herb Bowie
 */
public class FileTable {
  
  public static final int NUMBER_OF_COLUMNS = 3;
  
  private SimpleDateFormat format 
      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
  
  private ObservableList<FileBean>      files;
  
  private TableView<FileBean>           fileTable = null;
  
  private TableColumn<FileBean, String> nameColumn = null;
  private TableColumn<FileBean, String> dateColumn = null;
  private TableColumn<FileBean, String> existsColumn = null;
  
  public FileTable() {
    files = FXCollections.observableArrayList();
  }
  
  public void createTable() {
    fileTable = new TableView<FileBean>(files);
    fileTable.setItems(files);
    addColumns();
  }
  
  private void addColumns() {
    buildColumns();
    fileTable.getColumns().addAll
      (nameColumn, dateColumn, existsColumn);
  }
  
  /**
   Build all the columns we might ever use to display a table of notes. 
  */
  public void buildColumns() {
    
    // Build the Done Column
    nameColumn = new TableColumn<FileBean, String>(getColumnName(0));
    nameColumn.setPrefWidth(240);
    nameColumn.setMaxWidth(400);
    nameColumn.setCellValueFactory(
        new PropertyValueFactory<FileBean, String>("name")
    );

    // Build the Date Columns
    dateColumn = new TableColumn<FileBean, String>(getColumnName(1));
    dateColumn.setPrefWidth(120);
    dateColumn.setMaxWidth(160);
    dateColumn.setCellValueFactory(
        new PropertyValueFactory<FileBean, String>("date")
    );

    // Build the Exists Column
    existsColumn = new TableColumn<FileBean, String>(getColumnName(2));
    existsColumn.setPrefWidth(60);
    existsColumn.setMaxWidth(120);
    existsColumn.setCellValueFactory(
        new PropertyValueFactory<FileBean, String>("exists")
    );

  }
  
  public TableView getTable() {
    return fileTable;
  }
  
  public void add(File anotherFile) {
    System.out.println("FileTable.add " + anotherFile.toString());
    files.add(new FileBean(anotherFile));
  }
  
  public FileBean get(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < files.size()) {
      return files.get(rowIndex);
    } else {
      return null;
    }
  }
  
  /**
   How many rows are in the table?
  
   @return Number of rows. 
  */
  public int getRowCount() {
    return files.size();
  }
  
  /**
   How many columns are in the table?
  
   @return Number of columns. 
  */
  public int getColumnCount() {
    return NUMBER_OF_COLUMNS;
  }
  
  public String getColumnName(int columnIndex) {
    if (columnIndex >= 0 && columnIndex < NUMBER_OF_COLUMNS) {
      switch (columnIndex) {
        case 0:
          return "File Name";
        case 1:
          return "Last Mod Date";
        case 2:
          return "Exists?";
        default:
          return null;
      } // end switch
    } else {
      return "";
    }
  }
  
  /** 
   Return a value from the table. 
  
   @param rowIndex    Desired row index. 
   @param columnIndex Desired column index.
  
   @return Desired value, or null if either index is out of bounds. 
  */
  public String getValueAt (int rowIndex, int columnIndex) {
    if (rowIndex >= 0 && rowIndex < files.size()
        && columnIndex >= 0 && columnIndex < NUMBER_OF_COLUMNS) {
      File file = files.get(rowIndex).getFile();
      switch (columnIndex) {
        case 0:
          return file.getName();
        case 1:
          if (file != null && file.exists()) {
            Date lastMod = new Date(file.lastModified());
            return format.format(lastMod); 
          } else {
            return " ";
          }
        case 2:
          return String.valueOf(file.exists());
        default:
          return null;
      } // end switch
    } else {
      return null;
    }
  } // end method getValueAt

}
