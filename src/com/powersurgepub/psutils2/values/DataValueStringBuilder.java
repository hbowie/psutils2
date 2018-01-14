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

package com.powersurgepub.psutils2.values;

  import com.powersurgepub.psutils2.basic.*;

/**
 A data value stored as a string, and that supports multiple append operations. 

 @author Herb Bowie
 */
public class DataValueStringBuilder

    implements
        DataValue {
  
  public static final String[] DERIVED_SUFFIX = {
    
  };
  
  private StringBuilder value = null;
  
  private int pendingBlankLines = 0;
  
  /**
   Construct an object with an empty string as the initial value. 
  */
  public DataValueStringBuilder() {
    this.value = new StringBuilder();
  }
  
  /**
   Construct an object with the passed string as the initial value. 
  
   @param value The initial value for the object. 
  */
  public DataValueStringBuilder(String value) {
    this.value = new StringBuilder(value);
  }
  
  /**
   Set the initial value from a String. 
  
   @param value The value as a string. 
  */
  public void set(String value) {
    this.value = new StringBuilder(value);
  }
  
  /**
   Append a string to whatever is already in the object. 
  
   @param value The string to be added to the existing string. 
  */
  public void append(String value) {
    addPendingBlankLines();
    this.value.append(value);
  }
  
  /**
   Append the passed string, and add a line feed. Note that we will try to
   avoid putting completely blank lines at either the beginning or the end
   of the field. 
  
   @param line The string to be added to the existing string, to be followed by
               a line feed. 
  */
  public void appendLine(String line) {
    if (line.length() > 0) {
      addPendingBlankLines();
      value.append(line);
      value.append(GlobalConstants.LINE_FEED);
    } else {
      // Blank line
      if (value.length() == 0) {
        // Skip it -- let's not put blank lines at the beginning of a field.
      } else {
        // Let's hold off on adding blank lines, to make sure we don't
        // end up with any at the end of the field either.
        pendingBlankLines++;
      }
    }
  }
  
  public void addPendingBlankLines() {
    while (pendingBlankLines > 0) {
      value.append(GlobalConstants.LINE_FEED);
      pendingBlankLines--;
    }
  }
  
  /**
   Return the length of the string. 
  
   @return Length of the string. 
  */
  public int length() {
    if (value == null) {
      return 0;
    } else {
      return value.length();
    }
  }
  
  /**
   Does this value have any data stored in it? 
  
   @return True if data, false if empty. 
  */
  public boolean hasData() {
    return (value != null && value.length() > 0);
  }
  
  /** 
   Converts the value to a String.
  
   @return the value as a string. 
  */
  public String toString() {
    return value.toString();
  }
  
  /**
     Compares this data value to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than value2,
             or positive if this field is greater than value2.
    
     @param  value2 Another data value to be compared to this one.
   */
  public int compareTo(DataValue value2) {
    return toString().compareTo(value2.toString());
  }
  
  /**
   Identify how many other fields can be derived from this one. 
  
   @return The possible number of derived fields. 
  */
  public int getNumberOfDerivedFields() {
    return DERIVED_SUFFIX.length;
  }
  
  /**
   Return a suffix that will uniquely identify this derivation. The suffix 
   need not, and should not, begin with a hyphen or any other punctuation. 
  
   @param d An index value indicating which of the possible derived fields
            is desired. 
  
   @return The suffix identifying the requested derived field, or null if 
           the index is out of range of the possible fields. 
  */
  public String getDerivedSuffix(int d) {
    if (d < 0 || d >= getNumberOfDerivedFields()) {
      return null;
    } else {
      return DERIVED_SUFFIX [d];
    }
  }
  
  /**
   Return the derived field, in String form. 
  
   @param d An index value indicating which of the possible derived fields
            is desired. 
  
   @return The derived field requested, or null if the index is out of range
           of the possible fields. 
  */
  public String getDerivedValue(int d) {
    switch (d) {
      case 0:
      default:
        return null;
    }
  }

}
