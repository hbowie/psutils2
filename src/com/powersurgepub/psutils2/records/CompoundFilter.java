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

   A filter specification for a particular record definition, 
   made up of zero or more filter fields. <p>

   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)

 */

public class CompoundFilter 

    implements DataFilter {

  

  /** The record definition to which this filter spec applies. */

  private    RecordDefinition  recDef;

  

  /** Does and logic apply? (If not, then "or" logic.) */

  private    boolean           andLogic = false;

  

  /** The collection of record filters that make up this sequence specification. */

  private    Vector            filters;

  

  /** 

     Constructs a new data filter specification. 

    

     @param recDef Record definition for which a filter specification is desired.

    

     @param andLogic True if and logic is to be used.

   */

  public CompoundFilter (RecordDefinition recDef, boolean andLogic) {

    this.recDef = recDef;

    this.andLogic = andLogic;

    filters = new Vector ();

  }

  

  /**

     Adds another data filter to the specification. The sequence in which

     fields are added determines their evaluation order.

    

     @param filter Next record filter for this specification.

   */

  public void addFilter (DataFilter filter) {

    if (filter.getRecDef() == recDef) {

      filters.addElement (filter);

    } 

  }

  

   /**

     Selects the record.

    

     @return Decision whether to select the record (true or false).

    

     @param dataRec A data record to evaluate.

    

     @throws IllegalArgumentException if the operator is invalid.

   */

  public boolean selects (DataRecord dataRec) 

    throws IllegalArgumentException {

    int size = filters.size();

    if (size == 0) {

      return true;

    } else {

      int i = 0;

      boolean selected = selectionAt (dataRec, i);

      boolean endCondition;

      if (andLogic) {

        endCondition = false;

      } else {

        endCondition = true;

      }

      i = 1;

      while ((selected != endCondition) && (i < size)) {

        selected = selectionAt (dataRec, i);

        i++;

      }

      return selected;

    } // end condition where number of filters is non-zero

  } // end selects method

  

  private boolean selectionAt (DataRecord dataRec, int index) 

    throws IllegalArgumentException {

    DataFilter oneFilter = (DataFilter)filters.elementAt (index);

    return oneFilter.selects (dataRec);

  }

  

  /**

     Sets the And logic flag.

    

     @param andLogic True if "and" logic is to be used, false if "or".

   */

  public void setAndLogic (boolean andLogic) {

    this.andLogic = andLogic;

  }

    

  /**

     Returns the record definition for the filter.

    

     @return Record definition.

   */

  public RecordDefinition getRecDef() {

    return recDef;

  }

    

  /**

     Returns this object as some kind of string.

    

     @return Concatenation of the string representation of all the

             specification's filters.

   */

  public String toString () {

    StringBuffer recordBuf = new StringBuffer ();

    for (int i = 0; i < filters.size (); i++) {

      if (i > 0) {

        recordBuf.append (GlobalConstants.LINE_FEED_STRING);

      }

      recordBuf.append (filters.elementAt(i).toString());

    }

    return recordBuf.toString ();

  } // end toString method

  

} // end class CompoundFilter