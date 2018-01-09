/*
 * Copyright 1999 - 2014 Herb Bowie
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
  
/**
   A definition of one column within a particular RecordDefinition.<p>
   
 */

public class Column {

  public static final int DEFAULT_AVERAGE_LENGTH = 10;
  public static final int DEFAULT_MAXIMUM_LENGTH = 20;
  
  /** 
     An integer that points to a particular DataFieldDefinition 
     in the DataDictionary defined by the RecordDefinition. 
   */
  private   int         dataFieldDefNumber;
  
  /**
     An integer that points to a corresponding column in the
     record definition that has been most recently merged into this one. 
     A value of -1 means that no merge has occurred, or that no 
     corresponding field existed in the merged record definition. 
   */
  private   int         mergedColumn = -1;
  
  /** The length of the longest field within a particular dataSet. */
  private   int         maximumLength = 0;
  
  /** The total number of characters used by all fields within a particular DataSet. */
  private   int         totalLength = 0;
  
  /** The total number of fields (aka records) within this DataSet. */
  private   int         totalFields = 0;
  
  /**
     The constructor just initializes the fields.
    
     @param dataFieldDefNumber An integer pointing to a DataFieldDefinition within
            a DataDictionary.
   */
  public Column (int dataFieldDefNumber) {
    this.dataFieldDefNumber = dataFieldDefNumber;
  }
    
  /**
     Returns the pointer to a data dictionary entry.
    
     @return Index to an entry in a data dictionary.
   */
  public int getDataFieldDefNumber () {
    return dataFieldDefNumber;
  }
  
  /**
     Gets the mergedColumn variable.
    
     @return The column number of the merged RecordDefinition.
   */
  public int getMergedColumn () {
    return mergedColumn;
  }

  /**
     Resets the mergedColumn variable to -1, to indicate no merged column.
   */
  public void resetMergedColumn () {
    setMergedColumn (-1);
  }
  
  /**
     Sets the mergedColumn variable.
    
     @param mergedColumn The column number of the merged RecordDefinition.
   */
  public void setMergedColumn (int mergedColumn) {
    this.mergedColumn = mergedColumn;
  }
  
  /** 
     Uses actual field values to maintain field length statistics. If field
     length statistics (average length, maximum length) are required for a
     particular application, then this method should be called once for each 
     actual field value that is part of a particular record collection. 
    
     @param  data One String value for this field.
   */
  public void anotherField (String data) {
    int l = data.length();
    totalFields++;
    totalLength += l;
    if (l > maximumLength) {
      maximumLength = l;
    }
  }
  
  /**
     Gets the maximum length of any field value passed to the anotherField method.
    
     @return Maximum length (in characters) of any field.
   */
  public int getMaximumLength () { 
    if (totalFields > 0) {
      return maximumLength;
    }
    else {
      return DEFAULT_MAXIMUM_LENGTH;
    }
  }
  
  /**
     Gets the average length of all fields passed to the anotherField method.
    
     @return Average length (in characters) of all data Strings for this column.
   */
  public int getAverageLength () {
    if (totalFields > 0) {
      return totalLength / totalFields;
    }
    else {
      return DEFAULT_AVERAGE_LENGTH;
    }
  }
} // end class Column