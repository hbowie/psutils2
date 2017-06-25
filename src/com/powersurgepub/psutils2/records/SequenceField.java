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

package com.powersurgepub.psutils2.records;

/**

   One field used in a sequence specification
   (used for sorting, etc.). <p>

   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">

           herb@powersurgepub.com</a>)<br>

           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">

           www.powersurgepub.com/software</a>)

 */

public class SequenceField {



  /** A value that can be used to specify an ascending sequence. */

  public final static String  ASCENDING   = "Ascending";

  

  /** A value that can be used to specify a descending sequence. */

  public final static String  DESCENDING  = "Descending";

        

  /** The record definition containing the field to be used in a sequence spec. */

  private    RecordDefinition   recDef;

  

  /** The column number of this field within the record definition. */

  private    int                columnNumber;

  

  /** Is this field to be sorted in ascending sequence? */

  private    boolean            ascending = true;



/**

   Constructs the sequence field, assuming an ascending sequence.

  

   @param recDef        the Record Definition of the record to be sequenced.

  

   @param fieldName     the name of a field within the record definition.

 */

  public SequenceField (RecordDefinition recDef, String fieldName) {

    this (recDef, recDef.getColumnNumber (fieldName), true);

  }

  

/**

   Constructs the sequence field, assuming an ascending sequence.

  

   @param recDef        Record Definition of the record to be sequenced.

  

   @param columnNumber  Column of a field within the record definition.

 */

  public SequenceField (RecordDefinition recDef, int columnNumber) {

    this (recDef, columnNumber, true);

  }

  

/**

   Constructs the sequence field.

  

   @param recDef        Record Definition of the record to be sequenced.

  

   @param fieldName     Name of a field within the record definition.

  

   @param ascending     Ascending sequence (or descending)?

 */

  public SequenceField (RecordDefinition recDef, String fieldName,

       boolean ascending) {

    this (recDef, recDef.getColumnNumber (fieldName), ascending);

  }

  

/**

   Constructs the sequence field.

  

   @param recDef        Record Definition of the record to be sequenced.

  

   @param fieldName     Name of a field within the record definition.

  

   @param ascendingStr  Something starting with 'D', 'd', 'F' or 'f' for 

                        descending, anything else for ascending

 */

  public SequenceField (RecordDefinition recDef, String fieldName,

       String ascendingStr) {

    this (recDef, recDef.getColumnNumber (fieldName), true);

    setAscending (ascendingStr);

  }

  

/**

   Constructs the sequence field.

  

   @param recDef        Record Definition of the record to be sequenced.

  

   @param fieldName     Name of a field within the record definition.

  

   @param ascendingChar 'D', 'd', 'F' or 'f' for 

                        descending, anything else for ascending

 */

  public SequenceField (RecordDefinition recDef, String fieldName,

       char ascendingChar) {

    this (recDef, recDef.getColumnNumber (fieldName), true);

    setAscending (ascendingChar);

  }

  

/**

   Constructs the sequence field.

  

   @param recDef        Record Definition of the record to be sequenced.

  

   @param columnNumber  Column of a field within the record definition.

  

   @param ascending     Ascending sequence (or descending)?

 */

  public SequenceField (RecordDefinition recDef, int columnNumber,

      boolean ascending) {

    this.recDef = recDef;

    this.columnNumber = columnNumber;

    this.ascending = ascending;

  }

  

  /**

     Sets the ascending field.

    

     @param ascendingStr Something starting with 'D', 'd', 'F' or 'f' for 

                         descending, anything else for ascending

   */

  public void setAscending (String ascendingStr) {

    if (ascendingStr.length() > 0) {

      setAscending (ascendingStr.charAt (0));

    } 

    else {

      setAscending ('A');

    }

  }

  

  /**

     Sets the ascending field.

    

     @param ascendingChar 'D', 'd', 'F' or 'f' for 

                          descending, anything else for ascending

   */

  public void setAscending (char ascendingChar) {

    if ((ascendingChar == 'D') || (ascendingChar == 'd')

      || (ascendingChar == 'F') || (ascendingChar == 'f')) {

      this.ascending = false;

    }

    else {

      this.ascending = true;

    }

  }

  

  /**

     Returns the record definition for the reader.

    

     @return Record definition.

   */

  public RecordDefinition getRecDef() {

    return recDef;

  }

  

  /**

     Returns the column number of this field within its record definition.

    

     @return Column number of this field.

   */

  public int getColumnNumber () {

    return columnNumber;

  }

  

  /**

     Indicates whether data is to be sorted by this field in ascending sequence.

    

     @return True if ascending, false if descending.

   */

  public boolean isAscending () {

    return ascending;

  }

  

  /**

     Returns this object as some kind of string.

    

     @return Column number plus ascending or descending.

   */

  public String toString () {

    return ("Sequence by column "

      + columnNumber + " " + (ascending ? "ascending" : "descending"));

  }



} // end SequenceField Class

