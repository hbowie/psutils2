/*
 * Copyright 1999 - 2016 Herb Bowie
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

  import com.powersurgepub.psutils2.strings.*;

  import java.util.*;
  
  
/**
   A single data field. <p>
   
   @author Herb Bowie.
  
 */
public class PSField {

  /** Number of elements in each of the operand arrays. */
  public static final int      NUMBER_OF_LOGICAL_OPERANDS = 12;
  
  /** An array of valid operands consisting of special characters. */
  public static final String[] SYMBOL_LOGICAL_OPERANDS = {
    "=",  ">",  ">=", "<",  "<=", "<>", "()", "!()", "(<)", "!(<)", "(>)", "!(>)"};
    
  /** 
     An array of valid operands consisting of alternate special 
     character combinations.
   */
  public static final String[] ALT_SYMBOL_LOGICAL_OPERANDS = {
    "==", ">",  "!<", "<",  "!>", "!=", "[]", "![]", "[<]", "![<]", "[>]", "![>]"};
    
  /** An array of valid operands consisting of brief mnemonics. */
  public static final String[] MNEMONIC_LOGICAL_OPERANDS = {
    "eq", "gt", "ge", "lt", "le", "ne", "co", "nc", "st", "ns", "fi", "nf"};
    
  /** An array of valid operands consisting of full words. */
  public static final String[] WORD_LOGICAL_OPERANDS = {
    "equals", "greater than", "greater than or equal to",
    "less than", "less than or equal to", "not equal to",
    "contains", "does not contain", "starts with", "does not start with", 
    "ends with", "does not end with"};  

  /** The data in the field, stored as a string. */
  protected    String                 data;
  
  /** Can the string represent an integer? */
  protected    boolean                aNumber = false;
  
  /** The string as an integer value. */
  protected    int                    dataInteger = 0;
  
  /** The string as a long value. */
  protected    long                   dataLong = 0;
  
  /**
     Constructs a data field with defaults.
    
   */
  public PSField () {
 
  }
  
  /**
     Constructs a data field from the actual data.
    
     @param inData The data in string form.
   */
  public PSField (String inData) {
    this.data = StringUtils.purifyInvisibles (inData);
    processData();
  }
  
  /**
     Performs given operation on this data field.
    
     @param operator Either ++ or --
   */
  public void operate (String operator) {
    if (this.isAnInteger()) {
      if (operator.equals ("++")) {
        dataInteger++;
        dataLong++;
        setStringFromInt();
      } else
      if (operator.equals ("--")) {
        dataInteger--;
        dataLong--;
        setStringFromInt();
      }
    } // end if an integer
  } // end method operate
  
  /**
     Performs given operation on this data field, 
     using one operand.
    
     @param operator Either +, - or *
    
     @param operand Second field to be used with the operator.
   */
  public void operate (String operator, PSField operand) {

    if (this.isAnInteger() && operand.isAnInteger()) {
      int o = operand.getDataInteger();
      operate (operator, o);
    } else {
      if (operator.equals ("+")) {
        setData (data + operand.getData());
      } 
      else
      if (operator.equals ("=")) {
        setData (operand.getData());
      } 
    } // end if not both integers
  } // end method operate
  
  /**
     Performs given operation on this data field, 
     using one integer operand.
    
     @param operator Either +, +=,  -, -=, * or *=
    
     @param operand Number to be used with the operator.
   */
  public void operate (String operator, int operand) {

    if (this.isAnInteger()) {
      if (operator.equals ("+") || operator.equals ("+=")) {
        dataInteger = dataInteger + operand;
        setStringFromInt();
      } else
      if (operator.equals ("++")) {
        dataInteger++;
        setStringFromInt();
      } else
      if (operator.equals ("-") || operator.equals ("-=")) {
        dataInteger = dataInteger - operand;
        setStringFromInt();
      } else
      if (operator.equals ("--")) {
        dataInteger--;
        setStringFromInt();
      } else
      if (operator.equals ("*") || operator.equals ("*=")) {
        dataInteger = dataInteger * operand;
        setStringFromInt();
      } else
      if (operator.equals ("=")) {
        dataInteger = operand;
        setStringFromInt();
      }
    } // end if an integer
  } // end method operate
  
  /**
     Sets string to represent current integer value.
   */
  private void setStringFromInt() {
    data = String.valueOf (dataInteger);
    processData();
  }
  
  /**
     Returns the data stored in the data field.
    
     @return Actual data stored in this field.
   */
  public String getData () { return data; }
  
  /**
     Returns the field as a boolean value.
    
     @return The data field stored as a boolean.
   */
  public boolean getDataBoolean() { 
    if (data.length() >= 2) {
      if (data.substring(0, 2).equalsIgnoreCase("on")) {
        return true;
      }
    }
    if (data.length() >= 1) {
      if (data.substring(0, 1).equalsIgnoreCase("y")
          || data.substring(0, 1).equalsIgnoreCase("t")
          || data.substring(0, 1).equals("1")) {
        return true;
      }
    }
    return false; 
  }
  
  /**
     Is this field an integer? 
    
     @return True if the field contains an integer number.
   */
  public boolean isAnInteger() { return aNumber && (dataInteger == dataLong); }
  
  public boolean isANumber()   { return aNumber; }
  
  /**
     Returns the field as an integer value.
    
     @return The data field stored as an integer.
   */
  public int getDataInteger() { return dataInteger; }
  
  /**
   Returns the field as a long value. 
  
   @return The data field stored as a long value. 
  */
  public long getDataLong() { return dataLong; }
  
  /**
     Returns the length of the data string.
    
     @return Length of data field as a string.
   */
  public int length() { return data.length(); }
  
  /**
   * Sets the data portion of the field to a new value, without removing
   * any line breaks, etc.
   *
   * @param data New data value to be set.
   */
  public void setDataRaw (String data) {
    this.data = data;
    processData();
  }
  
  /**
     Sets the data portion of the field to a new value.
    
     @param data New data value to be set.
   */
  public void setData (String data) {
    this.data = StringUtils.purifyInvisibles (data);
    processData();
    
  } // end method setData
  
  /**
     Perform standard processing on a data value.
   */
  private void processData () {
    int i = 0;
    int sign = +1;
    char c;
    dataInteger = 0;
    dataLong = 0;
    int digitCount = 0;
    int l = data.length();
    aNumber = true;
    while ((i < l) && (aNumber)) {
      c = data.charAt(i);
      if (Character.isDigit(c)) {
        dataInteger = (dataInteger * 10) + Character.getNumericValue(c);
        dataLong    = (dataLong    * 10) + Character.getNumericValue(c);
        digitCount++;
      } 
      else 
      if (Character.isLetter(c)) {
        aNumber = false;
      }
      else
      if ((c == '-') && ((i == 0) || (i == l))){
        sign = -1;
      }
      else
      if (c == ',') {
      }
      else
      if (c == ' ') {
      }
      else {
        aNumber = false;
      }
      i++;
    } // end while
    if (aNumber && digitCount == 0) {
      aNumber = false;
    }
    if (aNumber) {
      dataInteger = dataInteger * sign;
      dataLong    = dataLong    * sign;
    }
    
  } // end method processData
  
  /**
     Returns this data field as some kind of string.
    
     @return The definition plus the data.
   */
  public String toStringLong () {
    return data.toString() 
    + (aNumber ? " (int " + String.valueOf(dataInteger) + ")" : "");
  }
  
  /**
     Returns this data field as some kind of string.
    
     @return The definition plus the data.
   */
  public String toString () {
    return data;
  }
  
  /**
     Performs given logical operation on this data field, 
     using one operand.
    
     @return True if expression evaluates to True.
    
     @param operator Either =, ==, >, >= or <=
    
     @param operand Second field to be used with the operator.
    
     @throws IllegalArgumentException if operator not in any of the 
               array constants.
   */
  public boolean operateLogically (String operator, PSField operand) 
      throws IllegalArgumentException {
    boolean trueOrFalse = true;
    int opLength = operator.length();
    char firstChar = ' ';
    String oplc = " ";
    if (opLength > 0) {
      firstChar = operator.charAt (0);
    }
    int opIndex;
    if (Character.isLetter (firstChar)) {
      oplc = operator.toLowerCase();
    } 
    if (oplc.startsWith("equal")) {
      oplc = "equals";
    }
    boolean opFound = false;
    for (opIndex = 0; 
        ((opIndex < NUMBER_OF_LOGICAL_OPERANDS) && (! opFound));
        opIndex++) {
      if ((oplc.equals (WORD_LOGICAL_OPERANDS [opIndex]))
          || (oplc.equals (MNEMONIC_LOGICAL_OPERANDS [opIndex]))
          || (operator.equals (SYMBOL_LOGICAL_OPERANDS [opIndex]))
          || (operator.equals (ALT_SYMBOL_LOGICAL_OPERANDS [opIndex]))) {
        opFound = true;
      } // end if operator found
    } // end 

    if (! opFound) {
      throw new IllegalArgumentException 
        ("Invalid logical operator (" + operator + ")");
    }
    opIndex--;
    if (opIndex < 6) {
      int result = this.compareTo (operand);
      if (opIndex == 0) {       // equals
        trueOrFalse = (result == 0);
      } else
      if (opIndex == 1) {       // greater than
        trueOrFalse = (result > 0);
      } else
      if (opIndex == 2) {       // greater than or equal to
        trueOrFalse = (result >= 0);
      } else
      if (opIndex == 3) {       // less than
        trueOrFalse = (result < 0);
      } else
      if (opIndex == 4) {       // less than or equal to
        trueOrFalse = (result <= 0);
      } else
      if (opIndex == 5) {       // not equal to
        trueOrFalse = (result != 0);
      }
    } // end opIndex < 6
    else {                      // text comparison
      String datalc = data.toLowerCase();
      String operandlc = operand.getData().toLowerCase();
      if (opIndex == 6) {
        int ix = datalc.indexOf (operandlc);
        trueOrFalse = (ix >= 0);
      }
      else
      if (opIndex == 7) {
        int ix = datalc.indexOf (operandlc);
        trueOrFalse = (ix < 0);
      }
      else
      if (opIndex == 8) {
        trueOrFalse = (datalc.startsWith (operandlc));
      }
      else
      if (opIndex == 9) {
        trueOrFalse = (! datalc.startsWith (operandlc));
      }
      else 
      if (opIndex == 10) {
        trueOrFalse = (datalc.endsWith (operandlc));
      }
      else 
      if (opIndex == 11) {
        trueOrFalse = (! datalc.endsWith (operandlc));
      }
    }
    return trueOrFalse;
  } // end method operateLogically
  
  /**
     Checks this field to see if it is equal to another one.
    
     @return True if the two fields are equal.
    
     @param  field2 Another data field to be checked for equality with this one.
   */
  public boolean equals (PSField field2) {
    return (this.compareTo(field2) == 0);
  }
  
  /**
     Compares this data field to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than field2,
             or positive if this field is greater than field2.
    
     @param  field2 Another data field to be compared to this one.
   */
  public int compareTo (PSField field2) {
    if (this.isANumber() && field2.isANumber()) {
      if (this.getDataLong() < field2.getDataLong()) {
        return -1;
      } else
      if (this.getDataLong() > field2.getDataLong()) {
        return +1;
      } else {
        return 0;
      }
    } else { 
      return data.compareTo (field2.getData());
    }
  } // end compareTo method
  
} // end DataField class