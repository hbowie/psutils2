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

  import java.util.*;

/**
   A comparator for a field within a list (used for sorting, etc.). <p>

   @author Herb Bowie 
 */

public class PSFieldComparator
    implements Comparator {

  /** A value that can be used to specify an ascending sequence. */
  public final static String  ASCENDING   = "Ascending";

  /** A value that can be used to specify a descending sequence. */
  public final static String  DESCENDING  = "Descending";

  /** The list containing the field to be compared. */
  private    PSList             psList;

  /** The column number of this field within the list. */
  private    int                column;

  /** Is this field to be sorted in ascending sequence? */
  private    boolean            ascending = true;

/**
   Constructs the comparator, assuming an ascending sequence.

   @param psList        The list containing the fields to be compared.
   @param fieldName     The name of a field within the record definition.
 */

  public PSFieldComparator (PSList psList, String fieldName) {

    this.psList = psList;
    this.column = psList.getColumnNumber(fieldName);
    this.ascending = true;
  }

/**
   Constructs the comparator, assuming an ascending sequence.

   @param psList        The list containing the fields to be compared.
   @param column  Column of a field within the record definition.
 */

  public PSFieldComparator (PSList psList, int column) {

    this.psList = psList;
    this.column = column;
    this.ascending = true;
  }

/**
   Constructs the sequence field.

   @param psList        The list containing the fields to be compared.
   @param fieldName     Name of a field within the record definition.
   @param ascending     Ascending sequence (or descending)?
 */

  public PSFieldComparator (PSList psList, String fieldName, boolean ascending) {

    this.psList = psList;
    this.column = psList.getColumnNumber(fieldName);
    this.ascending = ascending;
  }

/**
   Constructs the comparator.

   @param psList        The list containing the fields to be compared.
   @param fieldName     Name of a field within the record definition.
   @param ascendingStr  Something starting with 'D', 'd', 'F' or 'f' for 
                        descending, anything else for ascending
 */

  public PSFieldComparator (PSList psList, String fieldName, String ascendingStr) {

    this.psList = psList;
    this.column = psList.getColumnNumber(fieldName);
    setAscending (ascendingStr);
  }

/**
   Constructs the field comparator.
  
   @param psList        The list containing the fields to be compared.
   @param fieldName     Name of a field within the record definition.
   @param ascendingChar 'D', 'd', 'F' or 'f' for 
                        descending, anything else for ascending
 */
  public PSFieldComparator (PSList psList, String fieldName, char ascendingChar) {

    this.psList = psList;
    this.column = psList.getColumnNumber(fieldName);
    setAscending (ascendingChar);
  }

/**
   Constructs the field comparator.

   @param psList        The list containing the fields to be compared.
   @param column  Column of a field within the record definition.
   @param ascending     Ascending sequence (or descending)?
 */
  public PSFieldComparator (PSList psList, int column, boolean ascending) {

    this.psList = psList;
    this.column = column;
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

  public void setList (PSList psList) {
    this.psList = psList;
  }

  public PSList getList() {
    return psList;
  } 
  
  public void setColumn (int column) {
    this.column = column;
  }
  
  public void setColumn (String fieldName) {
    this.column = psList.getColumnNumber(fieldName);
  }

  /**
     Returns the column number of this field within its record definition.

     @return Column number of this field.
   */
  public int getColumn () {
    return column;
  }

  /**
     Indicates whether data is to be sorted by this field in ascending sequence.

     @return True if ascending, false if descending.
   */
  public boolean isAscending () {
    return ascending;
  }
  
   /**
    Compares the given field within the two passed items.

    @param item1 The first item.
    @param item2 The second item. 

    @return -1 if the first field is lower than the second, 
            +1 of the first field is higher than the second, or
            zero if the two fields are equal.
   */
  public int compare (Object obj1, Object obj2) {

    int result = 0;
    if (obj1 instanceof PSItem && obj2 instanceof PSItem) {
      PSItem item1 = (PSItem)obj1;
      PSItem item2 = (PSItem)obj2;
      Object fieldObj1 = item1.getColumnValue(column);
      Object fieldObj2 = item2.getColumnValue(column);
      if (fieldObj1 == null) {
        result = -1;
      }
      else
      if (fieldObj2 == null) {
        result = 1;
      }
      else
      if (fieldObj1 instanceof String
          && fieldObj2 instanceof String) {
        String str1 = (String)fieldObj1;
        String str2 = (String)fieldObj2;
        try {
          int int1 = Integer.parseInt(str1);
          int int2 = Integer.parseInt(str2);
          if (int1 < int2) {
            result = -1;
          }
          else 
          if (int1 > int2) {
            result = 1;
          } else {
            result = 0;
          }
        } catch (NumberFormatException e) {
          result = str1.compareTo(str2);
        }
      }
      else
      if (fieldObj1 instanceof Comparable
          && fieldObj2 instanceof Comparable) {
        Comparable field1 = (Comparable)fieldObj1;
        Comparable field2 = (Comparable)fieldObj2;
        result = field1.compareTo(field2);
      } 
    } 
    if (ascending) {
      return result;
    }
    else
    if (result == 0) {
      return result;
    } else {
      return (result * -1);
    }
  }

  /**
     Returns this object as some kind of string.

     @return Column number plus ascending or descending.
   */
  public String toString () {

    return ("Sequence by column "
      + column + " " + (ascending ? "ascending" : "descending"));
  }

} // end PSFIeldComparator Class

