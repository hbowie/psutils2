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

  import com.powersurgepub.psutils2.list.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.urls.*;

  import java.util.*;  

  import javafx.scene.control.*;
  
/**
   A record or row, consisting of one or more
   Data Fields. Since each field contains its own
   definition, a record definition is not contained
   within each data record. 
  
 */

public class DataRecord 
    extends DataField 
    implements
      Comparable,
      ItemWithURL,
      PSItem,
      Taggable {
  
  /** An index pointing to a particular field number within the record. */
  private   int           fieldNumber;
  
  /** A number indicating sequence in which records were created/added. */
  private		int						recordSequence = 0;
  
  private   Comparator    comparator = null;
  
  /**
     Constructs a new data record with no fields. 
   */
  public DataRecord () {
    
  }
  

  
  /**
     Adds a new field to this record, if one with this name does not 
     already exist. If one with this name does exist, then updates the
     data portion of the field to reflect the new data value. Maintains
     field length statistics. 
    
     @return Column number of field added or updated.
    
     @param  recDef Definition to be used for this record.
     @param  name   Name of the field to be added or updated.
     @param  data   Data value to be added or updated.
   */
  public int storeField (RecordDefinition recDef, String name, String data) {
    int columnNumber = getColumnNumber (name);
    DataField targetField = getField (columnNumber);
    DataFieldDefinition workDef = null;
    if (targetField == null
        || targetField == UNKNOWN_FIELD) {
      columnNumber = recDef.getColumnNumber(name);
      if (columnNumber >= 0) {
        workDef = recDef.getDef(columnNumber);
      } else {
        workDef = new DataFieldDefinition (name);
        columnNumber = recDef.addColumn(workDef);
      }
      targetField = new DataField (workDef, data);
      while (getNumberOfFields() < columnNumber) {
        DataFieldDefinition missingDef = recDef.getDef(getNumberOfFields());
        DataField missingField = new DataField(missingDef, "");
        addField(missingField);
      }
      return addField (targetField);
    } 
    else {
      targetField.setData (data);
      recDef.anotherField (data, columnNumber);
      return columnNumber;
    }
  }
  
  /**
     Adds a new field to this record, if one with this name does not 
     already exist. If one with this name does exist, then updates the
     data portion of the field to reflect the new data value. Maintains
     field length statistics. 
    
     @return Column number of field added or updated.
    
     @param  recDef Definition to be used for this record.
     @param  name   Name of the field to be added or updated.
     @param  data   Data value to be added or updated.
   */
  public int storeField (RecordDefinition recDef, DataFieldDefinition fieldDef, String data) {
    int columnNumber = getColumnNumber (fieldDef.getProperName());
    DataField targetField = getField (columnNumber);
    DataFieldDefinition workDef;
    if (targetField == null
        || targetField == UNKNOWN_FIELD) {
      columnNumber = recDef.getColumnNumber(fieldDef.getProperName());
      if (columnNumber >= 0) {
        workDef = recDef.getDef(columnNumber);
      } else {
        workDef = fieldDef;
        columnNumber = recDef.addColumn(workDef);
      }
      targetField = new DataField (workDef, data);
      while (getNumberOfFields() < columnNumber) {
        DataFieldDefinition missingDef = recDef.getDef(getNumberOfFields());
        DataField missingField = new DataField(missingDef, "");
        addField(missingField);
      }
      return addField (targetField);
    } 
    else {
      targetField.setData (data);
      recDef.anotherField (data, columnNumber);
      return columnNumber;
    }
  }
  
  /**
     Adds a new field to this record, if one with this name does not 
     already exist. If one with this name does exist, then updates the
     data portion of the field to reflect the new data value. Maintains
     field length statistics. 
    
     @return Column number of field added or updated.
    
     @param  recDef Definition to be used for this record.
     @param  name   Name of the field to be added or updated.
     @param  data   Data value to be added or updated.
   */
  public int storeField (RecordDefinition recDef, DataField field) {

    int columnNumber = getColumnNumber (field.getProperName());
    DataField targetField = getField (columnNumber);
    DataFieldDefinition workDef;
    if (targetField == null
        || targetField == UNKNOWN_FIELD) {
      columnNumber = recDef.getColumnNumber(field.getProperName());
      
      if (columnNumber >= 0) {
        workDef = recDef.getDef(columnNumber);
        targetField = new DataField (workDef, field.getDataValue());
      } else {
        workDef = field.getDef();
        columnNumber = recDef.addColumn(workDef);
        targetField = field;
      }
      while (getNumberOfFields() < columnNumber) {
        DataFieldDefinition missingDef = recDef.getDef(getNumberOfFields());
        DataField missingField = new DataField(missingDef, "");
        addField(missingField);
      }
      int newColumnNumber = addField (targetField);
      return newColumnNumber;
    } 
    else {
      targetField.setData (field.getDataValue());
      recDef.anotherField (field.getData(), columnNumber);
      return columnNumber;
    }
  }
  
  /**
    For calculated fields, calculate the field values.
   */
  public void calculate () {
    int column = 0;
    DataField field;
    while (column < fields.size()) {
      field = (DataField)fields.get (column);
      if (field.isCalculated()) {
        field.calculate (this);
      }
      column++;
    }
  }
  
  /**
     Compares the keys of two data records to see if they
     are equal.
     
     @return zero if records have identical keys, 
             a positive number if this record is greater than rec2, or
             a negative number if this record is less than rec2.
    
     @param  rec2 Second data record to compare to this one.
    
     @param  seq  A sequence specification that defines the key
                  fields for both records.
   */
  public int compareTo (DataRecord rec2, SequenceSpec seq) {
    SequenceField fieldSeq;
    int columnNumber, compareResult = 0;
    boolean ascending;
    DataField field1, field2;
    seq.startWithFirstField();
    while ((seq.hasMoreFields()) && (compareResult == 0)) {
      fieldSeq = seq.nextField();
      columnNumber = fieldSeq.getColumnNumber();
      field1 = this.getField (columnNumber);
      field2 = rec2.getField (columnNumber);
      compareResult = field1.compareTo (field2);
      if (! fieldSeq.isAscending()) {
        compareResult = compareResult * -1;
      }
    }
      return compareResult;
  }
    
  /**
     Tries to combine the two records.
     
     @return true if records were combined successfully, false if they were not. 
    
     @param  rec2   Second data record to compare to this one.
     @param  recDef The record definition for the two records.
     @param  precedence Indicator of whether earlier or later record
                        take precedence.
     @param  maxAllowed The maximum return value allowed as a result of the 
                        combination of the two records' fields. 
     @param  minNoLoss  If maxAllowed permits data to be overwritten,
                        then this parameter specifies the minimum number
                        of fields that must be without data loss.
   */
  public boolean combine (DataRecord rec2, RecordDefinition recDef, 
      int precedence, int maxAllowed, int minNoLoss) {
    Iterator columns = recDef.iterator();
    int result = 0;
    int maxResult = 0;
    int noLossFields = 0;
    Column column;
    DataField field1, field2;
    boolean combineSuccess;
    int columnIndex = 0;
    while (columns.hasNext()) {
      column = (Column)columns.next();
      field1 = getField (recDef, columnIndex);
      field2 = rec2.getField (recDef, columnIndex);
      result = field1.combine (field2, precedence, 
          getRecordSequence(), rec2.getRecordSequence());
      if (result > maxResult) {
        maxResult = result;
      }
      if (result == DataField.NO_DATA_LOSS) {
        noLossFields++;
      }
      columnIndex++;
    } // end while more columns
    if (maxResult > maxAllowed) {
      combineSuccess = false;
    }
    else
    if (maxResult == maxAllowed
        && maxAllowed == DataField.DATA_OVERRIDE
        && noLossFields < minNoLoss) {
      combineSuccess = false;
    }
    else {
      combineSuccess = true;
    }
    columns = recDef.iterator();
    columnIndex = 0;
    while (columns.hasNext()) {
      column = (Column)columns.next();
      field1 = getField (recDef, columnIndex);
      field1.finalizeCombining (combineSuccess);
      columnIndex++;
    } // end while more columns
    return combineSuccess;
  } // end combine method
  
  /**
     Indicates whether this records has more fields to process,
     using an internal index.
    
     @return True if there are more fields to process.
   */
  public boolean hasMoreFields () {
    return (fieldNumber < fields.size());
  }
  
  /**
     Returns the first field in the record, and positions the internal
     index for subsequent calls to nextField.
    
     @return First data field within this record.
   */
  public DataField firstField () {
    startWithFirstField ();
    return nextField ();
  }
  
  /**
     Positions the intenal index so that nextField will return the 
     first field the next time it is called.
   */
  public void startWithFirstField () {
    fieldNumber = 0;
  }
  
  /**
     Returns the next field in the record.
    
     @return Next field within record, using internal index
             to keep track of position within record.
   */
  public DataField nextField () {
    return getField (fieldNumber++);
  }
  
  /**
   Return the value stored in the indicated column. 
  
   @param columnIndex An index to the desired column, with zero pointing
                      to the first column. 
  
   @return An Object stored at the indicated column. 
  */
  public Object getColumnValue (int columnIndex) {
    return getField(columnIndex).getData();
  }
  
  /**
     Indicates whether this record contains the given field.
    
     @return True if the field is already contained within this
                  record, with a non-empty data value.
    
     @param  inName Name of the field of interest.
   */
  public boolean containsField (String inName) {
    DataField workField = getField (inName);
    if ((workField == null) || (workField == UNKNOWN_FIELD)) {
      return false;
    }
    Object workObject = workField.getData();
    if (workObject == null) {
      return false;
    }
    String workString = (String)workObject;
    if (workString.equals ("")) {
      return false;
    }
    return true;
  }
  



  
  /** 
     Sets the record number.
    
     param recordSequence A sequentially assigned record number.
   */
  public void setRecordSequence (int recordSequence) {
    this.recordSequence = recordSequence;
  }
  
  /** 
     Gets the record number.
    
     @return Sequentially assigned record number.
   */
  public int getRecordSequence () {
    return recordSequence;
  }
  
  public String getData () {
    return toString();
  }
  
  /**
     Returns this record as some kind of string.
    
     @return Concatenation of all the fields within this record.
   */
  public String toString () {
    StringBuffer recordBuf = new StringBuffer ("DataRecord -- ");
    for (int i = 0; i < fields.size (); i++) {
      if (i > 0) {
        recordBuf.append ("; ");
      }
      recordBuf.append (((DataField)fields.get(i)).toString());
    }
    return recordBuf.toString ();
  }
  
  /**
   Get the comparator to be used;
   */
  public Comparator getComparator() {
    return comparator;
  }
  
  /**
   Set the comparator to be used. 
   */
  public void setComparator (Comparator comparator) {
    this.comparator = comparator;
  }
  
  /**
   Determine if this item has a key that is equal to the passed
   item.

   @param  obj2        The second object to be compared to this one.
   @param  comparator  The comparator to be used to make the comparison. 
   @return True if the keys are equal.
   */
  public boolean equals (Object obj2, Comparator comparator) {
    return (this.compareTo (obj2, comparator) == 0);
  }
  
  /**
   Determine if this item has a key that is equal to the passed
   item.

   @param  obj2  The second object to be compared to this one.
   @return True if the keys are equal.
   */
  public boolean equals (Object obj2) {
    return (this.compareTo (obj2) == 0);
  }
  
  /**
   Compare this ClubEvent object to another, using the key field(s) for comparison.
 
   @param The second object to compare to this one.
 
   @return A number less than zero if this object is less than the second,
           a number greater than zero if this object is greater than the second,
           or zero if the two item's keys are equal.
   */
  public int compareTo (Object obj2, Comparator comparator) {
    if (comparator == null) {
      return 0;
    }
    return comparator.compare (this, obj2);
  }
  
  /**
   Compare one item to another, for sequencing purposes.

   @param  The second object to be compared to this one.
   @return A negative number if this item is less than the passed
           item, zero if they are equal, or a positive number if
           this item is greater.
   */
  public int compareTo (Object obj2) {
    if (comparator == null) {
      return 0;
    }
    return comparator.compare (this, obj2);
  }
  
  /**
   Return the tags assigned to this taggable item. 
   
   @return The tags assigned. 
   */
  public Tags getTags () {
    return null;
  }

  /**
   Flatten all the tags for this item, separating each level/word into its own
   first-level tag.
   */
  public void flattenTags() {
    
  }

  /**
   Convert the tags to all lower-case letters.
   */
  public void lowerCaseTags () {
    
  }

  /**
   Set the first TagsNodeValue occurrence for this Taggable item. This is stored
   in a TagsModel occurrence.

   @param tagsNode The tags node to be stored.
   */
  public void setTagsNode (TreeItem<TagsNodeValue> tagsNode) {
    
  }

  /**
   Return the first TagsNodeValue occurrence for this Taggable item. These nodes
   are stored in a TagsModel occurrence.

   @return The tags node stored. 
   */
  public TreeItem<TagsNodeValue> getTagsNode () {
    return null;
  }
  
  /**
   Returns the url as a string.
  */
  public String getURLasString () {
    return "";
  }
  
  public boolean hasTitle() {
    return hasFields();
  }
  
  public boolean hasFields() {
    return (fields.size() > 0);
  }
  
}