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

package com.powersurgepub.psutils2.values;

/**
 The state or status of an item, such as an action or an event. A status 
 indicates a position within a life cycle. A status is represented as
 a single digit, indicating an integer in the range of 0 - 9, where 
 lower numbers represent earlier states, and higher numbers represent 
 later ones. Each possible status also has a meaningful label associated 
 with it, such as "Open" or "Closed". <p>

 This class is more generalized than the similar class, ActionStatus, 
 in package com.powersurgepub.psdatalib.elements. 
 
 @author Herb Bowie.
 */
public class ItemStatus 
    implements 
        DataValue {

  public final static String  SIMPLE_NAME   = "status";
  public final static String  DISPLAY_NAME  = "Status";
  public final static String  BRIEF_NAME    = "Status";
  public final static int     COLUMN_WIDTH  = 80;
  
  public static final String[] DERIVED_SUFFIX = {
    
  };

  public static final String DONE = "X";
  public static final String NOT_DONE_YET = " ";
  private ItemStatusConfig config;
  
  /** Status of item. */
	private    	int          		status = 0;
  
  /**********************************************************************
  *
  * Constructors
  *
  **********************************************************************/ 
  
  /**
   Constructor with no arguments. 
  */
  public ItemStatus() {
    config = ItemStatusConfig.getShared();
  }
  
  /**
   Constructor with both arguments.
  
   @param config The Item Status Configuration to be used.
   @param status The status value. 
  */
  public ItemStatus(ItemStatusConfig config, int status) {
    this.config = config;
    setValue(status);
  }

  /**
   Constructor with only the configuration.
  
   @param config The Item Status Configuration to be used.
  */
  public ItemStatus(ItemStatusConfig config) {
    this.config = config;
  }

  /**
   Constructor with only the status as an integer. 
  
   @param status 
  */
  public ItemStatus(int status) {
    config = ItemStatusConfig.getShared();
    setValue(status);
  }
  
  /**
   Constructor with a string as input. 
  
   @param status As a string. 
  */
  public ItemStatus(String status) {
    config = ItemStatusConfig.getShared();
    set(status);
  }
  
  /**********************************************************************
  *
  * Set value from a string that may contain digits and/or letters.
  *
  **********************************************************************/ 
  
  /**
   Set the status from a String. 
  
   @param value A String representation of the status. 
  */
  public void set(String value) {
    setValue(value);
  }

  /**
   Set the status from a String. 
  
   @param value A String representation of the status. 
  */
  public void setValue(String value) {
    setStatus(value);
  }

  /**
   Set the status from any object that can be made into a string. 
  
   @param obj An object that can be made into a string. 
  */
  public void setValue(Object obj) {
    setValue(obj.toString());
  }
  
  public void setStatusFromLabel(String str) {
    setStatus(str);
  }
  
  /**
   Set the status from a string that may contain digits and/or letters.
  
   @param str A string representing a status in some way. 
  */
  public void setStatus(String str) {
    StringBuilder digits = new StringBuilder();
    StringBuilder letters   = new StringBuilder();
    int newStatus = -1;
    
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isWhitespace(c) || c == '-') {
        // Ignore it
      }
      else
      if (Character.isLetter(c)) {
        letters.append(c);
      }
      else
      if (Character.isDigit(c)) {
        digits.append(c);
      }
    } // End of input string
    
    int number = -1;
    if (digits.length() > 0) {
      try {
        number = Integer.parseInt(digits.toString());
      } catch (NumberFormatException e) {
        // Something wrong with our number!
      }
    }
    
    if (letters.length() > 0) {
      newStatus = config.lookup(letters.toString());
    }
    
    if ((! config.statusValid(newStatus))
        && config.statusValid(number)) {
      newStatus = number;
    }
    set(newStatus);
  }
  
  /**********************************************************************
  *
  * Set value from a boolean closed indicator.
  *
  **********************************************************************/ 
  
  /**
   Set the status value based on a boolean value indicating whether the item
   should be closed. 
  
   @param closed Should the item be closed?
  */
  public void setValue(boolean closed) {
    setStatus(closed);
  }

  /**
   Set the status value based on a boolean value indicating whether the item
   should be closed. 
  
   @param closed Should the item be closed?
  */
  public void setStatus(boolean closed) {
    if (closed) {
      setStatus(ItemStatusConfig.CLOSED);
    } else {
      setStatus(ItemStatusConfig.OPEN);
    }
  }
  
  /**********************************************************************
  *
  * Set value from an integer.
  *
  **********************************************************************/ 
  
  /**
   Set the status value using an integer. 
  
   @param value A status value expressed as an integer.
  */
  public void setValue(int value) {
    set(value);
  }
  
  /**
   Set the status value using an integer. 
  
   @param status A status value expressed as an integer.
  */
  public void setStatus(int status) {
    set(status);
  }
  
  /**
   Set the status value using an integer. 
  
   @param status A status value expressed as an integer.
  */
  public void set(int status) {
    if (config.statusValid(status)) {
      this.status = status;
    }
  }
  
  /**********************************************************************
  *
  * Get the status in various ways. 
  *
  **********************************************************************/ 
  
  /**
   Return the status as a combination of the digit and the label, separated 
   by a dash. 
  
   @return String containing both digit and label. 
  */
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append(getValueAsChar());
    str.append(" - ");
    str.append(getLabel());
    return str.toString();
  }
  
  /**
   Return the status value as an integer. 
  
   @return The status value as an integer.
  */
  public int getValueAsInt() {
    return status;
  }
  
  /**
   Get the status value as a single digit. 
  
   @return Status value as a single digit. 
  */
  public char getValueAsChar() {
    String chars = String.valueOf(status).trim();
    if (chars.length() > 0) {
      return chars.charAt(0);
    } else {
      return '0';
    }
  }

  /**
   Return the status value as an integer. 
  
   @return The status value as an integer.
  */
  public int getStatus() {
    return status;
  }

  public String getStatusLabel() {
    return getLabel();
  }
  
  /**
   Return the label String for this value. 
  
   @return String.
  */
  public Object getValue() {
    return getLabel();
  }

  /**
   Return the label for this status.
  
   @return A string labeling the status value. 
  */
  public String getLabel() {
    return config.getLabel(status);
  }

  /**
   Get the class definition for the preferred form of the element.

   @return The class definition for the preferred form of the element.
   */
  public Class getElementClass() {
    return String.class;
  }

  public String getLabel(int status) {
    return config.getLabel(status);
  }
  
  /**********************************************************************
  *
  * Get the status as a boolean. 
  *
  **********************************************************************/

  /**
   Return an "X" to mark items that are done. 
   
   @return an X if item is completed or canceled, or a space if not done. 
  */
  public String getDone() {
    if (isDone()) {
      return DONE;
    } else {
      return NOT_DONE_YET;
    }
  }
  /**
     Indicates whether item is still pending.

     @return True if item is canceled or closed.
   */
  public boolean isDone () {
    return (status >= ItemStatusConfig.COMPLETED);
  }

  /**
     Indicates whether item is still pending.

     @return True if item is open or in-work.
   */
  public boolean isNotDone () {
    return (status < ItemStatusConfig.COMPLETED);
  }

  public String getSimpleName() {
    return SIMPLE_NAME;
  }

  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  public String getBriefName() {
    return BRIEF_NAME;
  }

  public int getColumnWidth() {
    return COLUMN_WIDTH;
  }
  
  /**********************************************************************
  *
  * Odds and ends. 
  *
  **********************************************************************/
  
  /**
     Compares this data value to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than value2,
             or positive if this field is greater than value2.
    
     @param  value2 Another data value to be compared to this one.
   */
  public int compareTo(DataValue value2) {
    if (value2 instanceof ItemStatus) {
      ItemStatus status2 = (ItemStatus)value2;
      if (status < status2.getStatus()) {
        return -1;
      }
      else
      if (status > status2.getStatus()) {
        return 1;
      } else {
        return 0;
      }
    } else {
      return toString().compareTo(value2.toString());
    }
  }
  
  public int compareTo(Object obj2) {
    return toString().compareTo(obj2.toString());
  }

  /**
   Does this value have any data stored in it? 
  
   @return True if data, false if empty. 
  */
  public boolean hasData() {
    return (config.statusValid(status));
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
