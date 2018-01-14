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

/**
 A data value stored as a string. 

 @author Herb Bowie
 */
public class DataValueString

    implements
        DataValue {
  
  public static final String[] DERIVED_SUFFIX = {
    
  };
  
  private String value = null;
  
  public DataValueString() {
    
  }
  
  /**
   Set the value from a String. 
  
   @param value The value as a string. 
  */
  public void set(String value) {
    this.value = value;
  }
  
  public int length() {
    if (hasData()) {
      return value.length();
    } else {
      return 0;
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
    if (value == null) {
      return "";
    } else {
      return value;
    }
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
