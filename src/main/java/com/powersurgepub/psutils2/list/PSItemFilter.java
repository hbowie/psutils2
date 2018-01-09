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

package com.powersurgepub.psutils2.list;

  import com.powersurgepub.psutils2.basic.*;

  import java.util.*;

/**
   A filter specification for a particular list, 
   made up of zero or more field fieldFilters. 

   @author Herb Bowie
 */

public class PSItemFilter {

  /** Does and logic apply? (If not, then "or" logic.) */
  private    boolean                  andLogic = false;

  /** The collection of field filters that make up this item filter. */
  private    ArrayList<PSFieldFilter> fieldFilters;

  /** 
     Constructs a new item filter specification.     

     @param andLogic True if and logic is to be used.
   */

  public PSItemFilter (boolean andLogic) {

    this.andLogic = andLogic;
    fieldFilters = new ArrayList();
  }

  /**
     Adds another data filter to the specification. The sequence in which
     fields are added determines their evaluation order.

     @param filter Next field filter for this specification.
   */
  public void addFilter (PSFieldFilter filter) {
    fieldFilters.add (filter);
  }

   /**
     Selects the item.

     @return Decision whether to select the record (true or false).

     @param psItem An item to evaluate.

     @throws IllegalArgumentException if the operator is invalid.
   */
  public boolean selects (PSItem psItem) 
      throws IllegalArgumentException {

    int size = fieldFilters.size();
    if (size == 0) {
      return true;
    } else {
      int i = 0;
      boolean selected = selectionAt (psItem, i);
      boolean endCondition;
      if (andLogic) {
        endCondition = false;
      } else {
        endCondition = true;
      }
      i = 1;
      while ((selected != endCondition) && (i < size)) {
        selected = selectionAt (psItem, i);
        i++;
      }
      return selected;
    } // end condition where number of fieldFilters is non-zero
  } // end selects method

  private boolean selectionAt (PSItem psItem, int index) 
      throws IllegalArgumentException {

    PSFieldFilter oneFilter = fieldFilters.get (index);
    return oneFilter.selects (psItem);
  }

  /**
     Sets the And logic flag.

     @param andLogic True if "and" logic is to be used, false if "or".
   */
  public void setAndLogic (boolean andLogic) {
    this.andLogic = andLogic;
  } 

  /**
     Returns this object as some kind of string.

     @return Concatenation of the string representation of all the
             field filters.
   */
  public String toString () {

    StringBuilder recordBuf = new StringBuilder ();
    for (int i = 0; i < fieldFilters.size (); i++) {
      if (i > 0) {
        recordBuf.append (GlobalConstants.LINE_FEED_STRING);
      }
      recordBuf.append (fieldFilters.get(i).toString());
    }
    return recordBuf.toString ();
  } // end toString method

} // end class PSItemFilter