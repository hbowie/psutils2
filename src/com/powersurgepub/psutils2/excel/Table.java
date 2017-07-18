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

  import jxl.*; 

/**
  Defines a table, based on a sheet.

  @author Herb Bowie
 */
public class Table {
  
  private int columns = 0;
      
  private int rows = 0;
  
  private boolean[] columnHasLinks;
  
  private TableCell[][] cells;
  
  /** Creates a new instance of Table */
  public Table(Sheet sheet) {
    
    columns = sheet.getColumns();
    rows = sheet.getRows();
    
    // Create two-dimensional array of cells
    cells = new TableCell [sheet.getColumns()] [sheet.getRows()];
    
    // Load basic information about each cell
    for (int column = 0; column < columns; column++) {
      for (int row = 0; row < rows; row++) {
        cells [column][row] = new TableCell (sheet.getCell (column, row));
      }
    }
    
    // Now add fields for any hyperlinks
    Hyperlink[] links = sheet.getHyperlinks();
    columnHasLinks = new boolean[columns];
    for (int column = 0; column < columns; column++) {
      columnHasLinks [column] = false;
    }
    
    for (int i = 0; i < links.length; i++) {
      Hyperlink link = links[i];
      cells[link.getColumn()][link.getRow()].setHyperlink (link);
      columnHasLinks[link.getColumn()] = true;
    }
    
    // Now add data about merged cells
    Range[] merged = sheet.getMergedCells();
    for (int i = 0; i < merged.length; i++) {
      Range block = merged[i];
      int leftColumn = block.getTopLeft().getColumn();
      int rightColumn = block.getBottomRight().getColumn();
      int topRow = block.getTopLeft().getRow();
      int bottomRow = block.getBottomRight().getRow();
      for (int column = leftColumn; column <= rightColumn; column++) {
        for (int row = topRow; row <= bottomRow; row++) {
          cells[column][row].setMerged (true);
          if (column == leftColumn && row == topRow) {
            cells[column][row].setMergedAnchor (true);
            cells[column][row].setColspan (rightColumn - leftColumn + 1);
            cells[column][row].setRowspan (bottomRow - topRow + 1);
          } else {
            cells[column][row].setMergedAnchor (false);
          } // end if not upper-left cell of range
        } // end for each row in range of merged cells
      } // end for each column in range of merged cells
    } // end for each block of merged cells
    
  } // end constructor
  
  public int getColumns () {
    return columns;
  }
  
  public int getRows () {
    return rows;
  }
  
  public TableCell get (int column, int row) {
    if (column < 0 || column >= columns
        || row < 0 || row >= rows) {
      return null;
    } else {
      return cells[column][row];
    }
  }
  
  public boolean hasLinks (int column) {
    return columnHasLinks [column];
  }
  
}
