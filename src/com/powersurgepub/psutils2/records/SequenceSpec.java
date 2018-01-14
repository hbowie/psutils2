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

  import com.powersurgepub.psutils2.basic.*;

  import java.util.*;

/**

   A sequence specification for a particular record definition, 
   made up of zero or more sequence fields. <p>
  

   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">

           herb@powersurgepub.com</a>)<br>

           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">

           www.powersurgepub.com/software</a>)


 */

public class SequenceSpec {

  

  /** The record definition to which this sequence spec applies. */

  private    RecordDefinition  recDef;

  

  /** The collection of sequence fields that make up this sequence specification. */

  private    ArrayList            seqFields;

  

  /** An internal index used to cycle through all the sequence fields sequentially. */

  private    int               fieldNumber = -1;

  

  /** The data dictionary for the record definition. */

  private    DataDictionary    dict;

  

  /** 

     Constructs a new sequence specification from the given record

     definition. Sequence field(s) must be added separately.

    

     @param recDef Record definition for which a sequence specification is desired.

   */

  public SequenceSpec (RecordDefinition recDef) {

    this.recDef = recDef;
    initialize();
  }

  

  /**

     Constructs a new sequence specification from the given record
     definition, and adds the first (or only) sequence field.

     @param recDef Record definition for which a sequence specification is desired.

     @param seqField First (or only) sequence field for this specification.

   */

  public SequenceSpec (RecordDefinition recDef,
      SequenceField seqField) {

    this.recDef = recDef;
    initialize();
    addField (seqField);

  }

  

  /**

     Constructs a new sequence specification from the given record
     definition, and adds the first (or only) sequence field.

     @param recDef Record definition for which a sequence specification is desired.

     @param fieldName Name of first (or only) sequence field for this specification.
                      The desired sequence is assumed to be ascending.
   */

  public SequenceSpec (RecordDefinition recDef,
      String fieldName) {

    this.recDef = recDef;
    initialize();
    addField (fieldName);

  }

  

  /**

     Constructs a new sequence specification from the given record
     definition, adds the first (or only) sequence field by name, 
     and specifies whether the sequence for that field is ascending
     or descending.

     @param recDef Record definition for which a sequence specification is desired.

     @param fieldName Name of first (or only) sequence field for this specification.

     @param ascendingStr Something starting with 'D', 'd', 'F' or 'f' for 
                         descending, anything else for ascending.
   */

  public SequenceSpec (RecordDefinition recDef,

      String fieldName, String ascendingStr) {

    this.recDef = recDef;
    initialize();
    addField (fieldName, ascendingStr);

  }

  

  /**

     Constructs a new sequence specification from the given record
     definition, adds the first (or only) sequence field by name, 
     and specifies whether the sequence for that field is ascending
     or descending.

     @param recDef Record definition for which a sequence specification is desired.

     @param fieldName Name of first (or only) sequence field for this specification.

     @param ascendingChar Character of 'D', 'd', 'F' or 'f' for 
                          descending, anything else for ascending.
   */

  public SequenceSpec (RecordDefinition recDef,

      String fieldName, char ascendingChar) {

    this.recDef = recDef;
    initialize();
    addField (fieldName, ascendingChar);

  }

    

  /**

     Initializes common fields as part of all constructors.

   */

  private void initialize () {

    dict = recDef.getDict();
    seqFields = new ArrayList ();
    startWithFirstField();

  }

  

  /**

     Adds another sequence field to the specification. The sequence in which
     fields are added determines their sort significance, going from major
     to minor.

     @param seqField Next sequence field for this specification.
   */

  public void addField (SequenceField seqField) {

    if (seqField.getRecDef() == recDef) {
      seqFields.add (seqField);

    } 

  }

  

  /**

     Adds another sequence field to the specification. The sequence in which
     fields are added determines their sort significance, going from major
     to minor.

     @param fieldName Name of next sequence field for this specification.
                      The desired sequence is assumed to be ascending.
   */

  public void addField (String fieldName) {

    seqFields.add (new SequenceField (recDef, fieldName));

  }

  

  /**

     Adds another sequence field to the specification. The sequence in which
     fields are added determines their sort significance, going from major
     to minor.

     @param fieldName Name of next sequence field for this specification.

     @param ascendingStr Something starting with 'D', 'd', 'F' or 'f' for 
                         descending, anything else for ascending.
   */

  public void addField (String fieldName, String ascendingStr) {

    seqFields.add 
      (new SequenceField (recDef, fieldName, ascendingStr));
  }

  

  /**

     Adds another sequence field to the specification. The sequence in which
     fields are added determines their sort significance, going from major
     to minor.

     @param fieldName Name of next sequence field for this specification.

     @param ascendingChar Character of 'D', 'd', 'F' or 'f' for 
                          descending, anything else for ascending.
   */

  public void addField (String fieldName, char ascendingChar) {

    seqFields.add 
      (new SequenceField (recDef, fieldName, ascendingChar));

  }

  

  /**

     Returns this object as some kind of string.

     @return Concatenation of the string representation of all the
             specification's sequence fields.
   */

  public String toString () {

    StringBuilder recordBuf = new StringBuilder ();
    for (int i = 0; i < seqFields.size (); i++) {
      if (i > 0) {
        recordBuf.append (GlobalConstants.LINE_FEED_STRING);
      }
      recordBuf.append (seqFields.get(i).toString());
    }
    return recordBuf.toString ();
  }



  /**

     Has the last sequence field for this specification already been returned?

    

     @return True if there are no more fields to return.

   */

  public boolean isAtEnd () {
    return (! hasMoreFields());
  }

    

  /**
     Are there more fields to return?

     @return True if there are more sequence fields in this specification to return.
   */

  public boolean hasMoreFields () {

    return ((fieldNumber + 1) < seqFields.size());

  }

  /**
     Returns the first sequence field within this specification, or null
     if the specification does not yet contain any fields.

     @return First sequence field for this specification.
   */

  public SequenceField firstField () {

    startWithFirstField ();
    return nextField ();

  }

  

  /**

     Resets internal index of fieldNumber so that next call to nextField

     will return first sequence field for this specification.

   */

  public void startWithFirstField () {

    fieldNumber = -1;

  }

  

  /**
     Returns the next sequence field within this specification.

     @return Next field, using fieldNumber as an internal index.
   */

  public SequenceField nextField () {

    return (SequenceField)seqFields.get (++fieldNumber);

  }

  

  /**
     Returns the record definition used by this specification.

     @return Record definition for this specification.
   */

  public RecordDefinition getRecDef() {

    return recDef;

  }

  

  /**
     Returns the field number of the last field passed back by nextField.

     @return Sequence number of last field returned, with zero representing
             the first field.
   */

  public int getFieldNumber() {

    return fieldNumber;

  }

  

} // end class SequenceSpec