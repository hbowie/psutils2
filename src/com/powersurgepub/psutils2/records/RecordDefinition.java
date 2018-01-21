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
  import com.powersurgepub.psutils2.strings.*;

  import java.util.*;
  
/**
   A definition of the fields stored in a particular record. Each field is
   associated internally with a particular column number. <p>
   
 */

public class RecordDefinition {

  /** 
     The Data Dictionary that contains the DataFieldDefinition objects for all
     the fields within this record definition.
   */
  private   DataDictionary  dict;
  
  /** 
     A collection of Column objects, each of which points to a
     particular DataFieldDefinition in the DataDictionary. 
   */
  private   ArrayList       columns;
  
  /**
     A collection of Strings containing the names of all the
     fields in the record.
   */
  private   ArrayList       names;
  
  /** An index to an entry in the columns collection. */
  private   int             columnNumber;
  
  /**
     The no-arg constructor will instantiate a new DataDictionary.
   */
  public RecordDefinition () {
    
    this (new DataDictionary ());
  }
  
  /**
     Accepts a DataDictionary that has already been built,
     and uses that.
    
     @param dict A data dictionary that has already been created.
   */
  public RecordDefinition (DataDictionary dict) {
  
    this.dict = dict;
    columns = new ArrayList();
    names = new ArrayList();
  }
  
  /**
     Accepts an existing RecordDefinition, and uses the same 
     DataDictionary already used by that RecordDefinition.
    
     @param inRec An existing record definition with a dictionary
                  that is to be shared with this record definition.
   */
  public RecordDefinition (RecordDefinition inRec) {
    this (inRec.getDict());
  }

  /**
    Copy the Data Field Definitions from another record definition into this one.

    @param inRec The Record Definition containing the desired data field definitions.
   */
  public void copyDefs(RecordDefinition inRec) {
    this.dict = inRec.getDict();
    int inRecIndex = 0;
    while (inRecIndex < inRec.getNumberOfFields()) {
      DataFieldDefinition inDef = inRec.getDef(inRecIndex);
      putColumn(inDef);
      inRecIndex++;
    }
  }
  
  /**
     Merges a second RecordDefinition into this one, combining the 
     fields of both.
    
     @param inRec 2nd RecordDefinition
   */
  public void merge (RecordDefinition inRec) {
    int inColumnIndex = 0;
    Column inColumn;
    int dictIndex;
    DataFieldDefinition inDef;
    int columnIndex;
    Column column;
    resetMergedColumns();
    DataDictionary inDict = inRec.getDict();
    Iterator inColumns = inRec.iterator();
    while (inColumns.hasNext()) {
      inColumn = (Column)inColumns.next();
      dictIndex = inColumn.getDataFieldDefNumber();
      inDef = inDict.getDef(dictIndex);
      columnIndex = putColumn (inDef);
      column = (Column) columns.get(columnIndex);
      column.setMergedColumn (inColumnIndex++);
    }
  }  
  
  /**
     Resets all mergedColumn fields in all columns to -1, to indicate
     no merge.
   */
  private void resetMergedColumns () {
    Column work;
    for (int i = 0; i < columns.size(); i++) {
      work = (Column) columns.get (i); 
      work.resetMergedColumn();
    }
  }
  
  /**
     Returns the data dictionary being used by this record definition,
     typically so that it can be shared.
    
     @return Data dictionary being used by this object.
   */
  public DataDictionary getDict () {
    return dict;
  }
  
  /**
   Does this record definition contain a field with the passed name?
  
   @param inName The name of the field in question. 
  
   @return True if found, false otherwise.
  */
  public boolean contains(String inName) {
    int ix = getColumnNumber(inName);
    return (ix >= 0);
  }
  
  /**
     Returns the column number assigned to a particular field,
     as identified by its name.
    
     @return Column number for the given field, or a -1 if not found.
    
     @param  inName The name of the desired field, which will be converted
                    to its common form.
   */
  public int getColumnNumber (String inName) {
    CommonName common = new CommonName (inName);
    return getColumnNumber (common);
  }
  
  /**
     Returns the column number assigned to a particular field,
     as identified by the name passed within a data field definition.
    
     @return Column number for the given field, or a -1 if not found.
    
     @param  inDef  A data field definition for the desired field.
   */
  public int getColumnNumber (DataFieldDefinition inDef) {
    return getColumnNumber (inDef.getCommonName());
  }
  
  /**
     Returns the column number assigned to a particular field,
     as identified by its common name.
    
     @return Column number for the given field, or a -1 if not found.
    
     @param  inCommon The name of the desired field, assumed to
                      already be converted to its common form.
   */
  public int getColumnNumber (CommonName inCommon) {

    CommonName common = dict.getAliasOriginal (inCommon);

    int i = 0;
    while ((i < columns.size()) 
      && (!(common.equals(dict.getDef(getDefNum(i)).getCommonName())))) {
      i++;
    }
    if (i < columns.size()) {
      return i;
    } else {
      return GlobalConstants.NOT_FOUND;
    }
  }
  
  /** 
     Returns the index to a particular DataFieldDefinition in
     the DataDictionary, stored in a particular column number.
    
     @return Index to a data dictionary entry, or -1 if the column number
             passed is out of bounds.
    
     @param  columnNumber Index to a particular column in the record definition.
   */
  public int getDefNum (int columnNumber) {
    if ((columnNumber < 0) || (columnNumber >= columns.size())) {
      return -1;
    } else {
      Column column = (Column)columns.get(columnNumber);
      return column.getDataFieldDefNumber();
    }
  }
  
  /**
     Adds the given data field definition to this record definition,
     if it does not already exist within the record definition.
     either under its original name or a known alias.
    
     @return Column number of the existing data field definition, if
             it was already part of this record, or the column
             number of the new entry, if it was added.
    
     @param  inDef The data field definition to be added.
   */
  public int putColumn (DataFieldDefinition inDef) {
    int i;
    i = getColumnNumber (inDef);
    if (i == GlobalConstants.NOT_FOUND) {
      i = addColumn (inDef);
    } 
    return i;
  }
  
  /**
     Adds the given data field definition to this record definition,
     whether it already exists within the record definition or not.
     Also adds the data field definition to the data dictionary, if it
     is not already there. 
    
     @return Column number of the new entry.
    
     @param  inName The name of a data field to be added.
   */
  public int addColumn (String inName) {
    DataFieldDefinition def = new DataFieldDefinition (inName);
    return addColumn (def);
  }
  
  /**
     Adds the given data field definition to this record definition,
     whether it already exists within the record definition or not.
     Also adds the data field definition to the data dictionary, if it
     is not already there. 
    
     @return Column number of the new entry.
    
     @param  inName The data field definition to be added.
   */
  public int addColumn (DataFieldDefinition inDef) {
    int j = dict.putDef (inDef);
    Column column = new Column (j);
    int i = columns.size();
    columns.add (column);
    names.add (inDef.getProperName());
    return i;
  }
  
  /** 
     Uses actual field values to maintain field length statistics. If field
     length statistics (average length, maximum length) are required for a
     particular application, then this method should be called once for each 
     actual field value that is part of a particular record collection. 
    
     @param  data One String value for this field.
    
     @param  columnNumber Index to a particular column.
   */
  public void anotherField (String data, int columnNumber) {
    if ((columnNumber >= 0) && (columnNumber < columns.size())) {
      Column column = (Column)columns.get (columnNumber);
      column.anotherField (data);
    }
  }

  
  /**
     Returns a Vector containing the names of all the fields
     in the record definition.
    
     @return Proper names of all the fields.
   */
  public ArrayList getNames () {
    return names;
  }
  
  public Column getColumn (int c) {
    if (c >= 0 && c < columns.size()) {
      return (Column)columns.get (c);
    } else {
      return null;
    }
  }
  
  /**
     Returns a data field definition stored in a particular
     column of this record.
    
     @return Data field definition for the given column.
    
     @param  column Column number for which the definition
                    is desired.
   */
  public DataFieldDefinition getDef (int columnNumber) {
    int i;
    i = getDefNum (columnNumber);
    return dict.getDef (i);
  }
  
  /**
   Remove all columns from the record definition. 
   */
  public void clear() {
    while (columns.size() > 0) {
      remove(columns.size() - 1);
    }
  }
  
  /**
   Remove the column at the given index. 
  
   @param i An index to the column to be removed. 
  
   @return The column being removed, or null if the index is out of bounds. 
  */
  public Object remove(int i) {
    if (i < 0 || i >= columns.size()) {
      return null;
    } else {
      names.remove(i);
      return columns.remove(i);
    }
  }
  
  /**
     Return the number of fields in each record.
    
     @return Number of fields stored in each record.
   */
  public int getNumberOfFields () {
    return columns.size();
  }
  
  /**
     Gets the maximum length of any data String for the indicated column number.
    
     @return Maximum length of field.
    
     @param columnNumber Index to one field in the record definition.
   */
  public int getMaximumLength (int columnNumber) { 
    Column column = (Column)columns.get (columnNumber);
    return column.getMaximumLength();
  }
  
  /**
     Gets the average length of all data Strings for the indicated column number.
    
     @return Average length (in characters) of all data Strings for this column.
    
     @param columnNumber Index to one field in the record definition.
   */
  public int getAverageLength (int columnNumber) {
    Column column = (Column)columns.get (columnNumber);
    return column.getAverageLength();
  }

  
  /**
     Return this object as some kind of string.
    
     @return Concatenation of all the data field definitions
             in this record definition.
   */
  public String toString () {
    StringBuilder recordBuf = new StringBuilder ();
    for (int i = 0; i < columns.size (); i++) {
      if (i > 0) {
        recordBuf.append ("; ");
      }
      recordBuf.append (getDef(i).toString());
    }
    return recordBuf.toString ();
  }
  
  /**
     Indicates whether more definitions are left to return
     from the record definition.
    
     @return True if there are more definitions to return.
   */
  public boolean hasMoreDefs () {
    return (columnNumber < columns.size());
  }
  
  /**
     Returns the first definition in the table, and 
     preps the internal index to return subsequent entries
     with nextDef.
    
     @return First data field definition in the record definition.
   */
  public DataFieldDefinition firstDef () {
    resetColumnNumber ();
    return nextDef ();
  }
  
  /**
     Sets the internal index to return the first column
     with the next call to nextDef.
   */
  public void resetColumnNumber () {
    columnNumber = 0;
  }
  
  /**
     Returns the next Column in the record
     definition, using an internal pointer to keep track
     of a position within the collection.
    
     @return Next Column in the record definition.
   */
  public DataFieldDefinition nextDef () {
    return getDef (columnNumber++);
  }
  
  public void display() {
    System.out.println(" ");
    System.out.println("RecordDefinition.display");
    for (int i = 0; i < getNumberOfFields(); i++) {
      DataFieldDefinition fieldDef = getDef(i);
      System.out.println(String.valueOf(i) + ". " 
          + fieldDef.getProperName() + " ("
          + fieldDef.getCommonName().toString() + ") type = "
          + String.valueOf(fieldDef.getType()));
    }
  }
  
  /**
     Returns an Iterator that can be used to retrieve all
     DataFieldDefinition objects in the collection, one at
     a time. 
    
     @return Iterator.
   */
  public Iterator iterator () {
    return new ColumnIterator();
  }
  
  class ColumnIterator 
      implements Iterator {
  
    private int index;
    
    ColumnIterator () {
      index = 0;
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public boolean hasNext() {
      return (index < getNumberOfFields());
    }
    
    public Object next() {
      if (hasNext()) {
        return columns.get (index++);
      }
      else {
        throw new NoSuchElementException();
      }
    }
  }
  
}