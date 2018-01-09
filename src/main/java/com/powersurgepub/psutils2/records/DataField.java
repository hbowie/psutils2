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

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.values.*;
  import java.util.*;
  
/**
   A single data field, consisting of a definition and the actual data stored
   as a string. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
 */
public class DataField {
  
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
    
  /** Higher-numbered (more recently merged) field takes priority. */
  public static final int			LATER_OVERRIDES = +1;
  
  /** Lower-numbered field (earlier merge) take priority. */
  public static final int			EARLIER_OVERRIDES = -1;
  
  /** No record takes priority -- combine by appending only. */
  public static final int			NO_OVERRIDES = 0;
    
  /** 
     Combine result indicating one of the two fields was null,
     or the fields were equal, so no data was lost.
   */
  public static final int     NO_DATA_LOSS    = 0;
  
  /** 
     Combine result indicating two fields were not equal, and one
     value replaced the other.
   */
  public static final int     DATA_OVERRIDE   = 1;
  
  /** 
     Combine result indicating two fields were concatenated.
   */
  public static final int     DATA_COMBINED   = 2;
    
  /** 
     Combine result indicating two fields did not have same
     field names.
   */
  public static final int 		DIFFERENT_NAMES = 3;
  
  /** Unknown field (returned when index is out of bounds). */
  public  final static  DataField UNKNOWN_FIELD = new DataField
    (DataDictionary.UNKNOWN_FIELD_DEFINITION, "");

    /** The definition of this data field. */
  protected    DataFieldDefinition    def;

  /** The data in the field, stored as a string. */
  protected    DataValue              data = null;
  
  /** Can the string represent an integer? */
  protected    boolean                aNumber = false;
  
  /** The string as an integer value. */
  protected    int                    dataInteger = 0;
  
  /** The string as a long value. */
  protected    long                   dataLong = 0;
  
  /** The collection of fields stored within this record. */
  protected    ArrayList              fields;
  
  /** 
     A prior version of the data in this field, saved in case it needs
     to be restored.
   */
  protected    DataValue              oldData;
  
  /** Has the oldData field been set to something (a prior value)? */
  protected  boolean                oldDataSet;
  
  /**
     Constructs a data field with defaults.
    
   */
  public DataField () {
    this (DataDictionary.UNKNOWN_FIELD_DEFINITION,  "");
  }
  
  /**
     Constructs a data field from a record definition
     a column number and the actual data.
    
     @param recDef  A record definition.
    
     @param column  An index to a particular column in the record definition passed.
    
     @param inData The data in string form.
   */
  public DataField (RecordDefinition recDef, int column, String inData) {
    this (recDef.getDef (column), inData);
  }
  
  /**
     Constructs a data field from a definition and the actual data.
    
     @param def  A data field definition.
    
     @param data The data in string form.
   */
  public DataField (DataFieldDefinition def, String data) {
    this.def = def;
    this.data = def.getEmptyDataValue();
    setData (data);
    oldData = null;
    oldDataSet = false;
    fields = new ArrayList ();
  }
  
  public DataField (DataFieldDefinition def, DataValue data) {
    this.def = def;
    setData(data);
    oldData = null;
    oldDataSet = false;
    fields = new ArrayList();
  }
  
  public void displayFields() {
    System.out.println("DataField.displayFields");
    for (int i = 0; i < fields.size(); i++) {
      DataField nextField = getField(i);
      System.out.println("- " + nextField.getProperName()
          + ": " + nextField.getData());
    }
    System.out.println(" ");
  }
  
  public int getNumberOfFields () {
    return fields.size();
  }
  
  /**
     Determines if two data fields can be combined without losing
     any data. If the fields are combined, then the previous value
     of this data field will be stored in oldData.
    
     @return integer indicating results of combining. 
    
     @param  field2 Another data field to try to combine with 
                    this one.
     @param  precedence Indicates which field, if any should take precedence.
     @param  recSeq1 Record sequence number of first (this) field.
     @param  recSeq1 Record sequence number of second field.
   */
  public int combine (DataField field2, int precedence, int recSeq1, int recSeq2) {
    oldData = null;
    oldDataSet = false;
    CommonName common1 = def.getCommonName();
    CommonName common2 = field2.getDef().getCommonName();
    if (field2 == null) {
      return NO_DATA_LOSS;
    }
    if (field2.equals(DataRecord.UNKNOWN_FIELD)) {
      return NO_DATA_LOSS;
    }
    if (this.equals(DataRecord.UNKNOWN_FIELD)) {
      oldData = data;
      oldDataSet = true;
      setData (field2.getData());
      def = field2.getDef();
      return NO_DATA_LOSS;
    }
    if (! common1.equals (common2)) {
      return DIFFERENT_NAMES;
    }
    String dataStr2 = field2.getData();
    if (dataStr2.equals ("")) {
      return NO_DATA_LOSS;
    }
    String dataStr1 = data.toString();
    if (dataStr1.equalsIgnoreCase (dataStr2)) {
      return NO_DATA_LOSS;
    }
    if (dataStr1.equals ("")) {
      oldData = data;
      oldDataSet = true;
      setData (field2.getData());
      return NO_DATA_LOSS;
    }
    if ((precedence == LATER_OVERRIDES
        && recSeq2 > recSeq1)
        || (precedence == EARLIER_OVERRIDES
          && recSeq2 < recSeq1)) {
      oldData = data;
      oldDataSet = true;
      setData (field2.getData());
      return DATA_OVERRIDE;
    }
    if ((precedence == LATER_OVERRIDES
        && recSeq1 > recSeq2)
        || (precedence == EARLIER_OVERRIDES
          && recSeq1 < recSeq2)) {
      return DATA_OVERRIDE;
    }
    if (! def.isCombineByAppendingOK()) { 
      return DATA_OVERRIDE;
    }
    oldData = data;
    oldDataSet = true;
    setData (dataStr1 + " " + dataStr2);
    return DATA_COMBINED;
  } // end of combine method
  
  /**
     If a combining operation was completed successfully, 
     then oldData is erased, otherwise the old data
     is restored.
    
     @param combiningOK True if a combining operation on
                        some collection of fields (such as a
                        record) was successful.
   */
  public void finalizeCombining (boolean combiningOK) {
    if ((!combiningOK) && (oldDataSet)) {
      setData (oldData);
    }
    oldData = null;
    oldDataSet = false;
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
  public void operate (String operator, DataField operand) {

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
    data.set(String.valueOf (dataInteger));
    processData();
  }
  
  /**
     Returns the data stored in the data field.
    
     @return Actual data stored in this field.
   */
  public String getData () { return data.toString(); }
  
  public DataValue getDataValue () { return data; }
  
  public boolean hasData () { return data.hasData(); }
  
  /**
     Returns the field as a boolean value.
    
     @return The data field stored as a boolean.
   */
  public boolean getDataBoolean() { 
    String dataStr = data.toString();
    if (dataStr.length() >= 2) {
      if (dataStr.substring(0, 2).equalsIgnoreCase("on")) {
        return true;
      }
    }
    if (dataStr.length() >= 1) {
      if (dataStr.substring(0, 1).equalsIgnoreCase("y")
          || dataStr.substring(0, 1).equalsIgnoreCase("t")
          || dataStr.substring(0, 1).equals("1")) {
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
     Returns the data field definition stored in this data field.
    
     @return Data field definition for this data field.
   */
  public DataFieldDefinition getDef () { return def; }
  
  /**
   Return the common field name for this field. 
  
   @return The common name for this field. 
  */
  public CommonName getCommonName() {
    return def.getCommonName();
  }
  
  /**
     Returns the name of the field definition stored in this data field, 
     in its common form.
    
     @return Common name for this data field (no caps or punctuation).
   */
  public String getCommonFormOfName () { 
    return def.getCommonName().getCommonForm(); 
  }
  
  /**
     Returns the proper (original) name of the field definition 
     stored in this data field.
    
     @return Proper (original) name for this data field.
   */
  public String getProperName () { 
    return def.getProperName(); 
  }
  
  /**
    Is this a calculated field?
    
    @return True if this field should be calculated.
   */
  public boolean isCalculated () {
    return def.isCalculated();
  }
  
  /**
    For calculated fields, calculate the field value.
    
    @param DataRecord Record containing this field.
   */
  public void calculate (DataRecord dataRec) {
    if (def.isCalculated()) {
      setData (def.calculate (dataRec));
    }
  }
  
  /**
     Returns the length of the data string.
    
     @return Length of data field as a string.
   */
  public int length() { return data.toString().length(); }
  
  /**
   * Sets the data portion of the field to a new value, without removing
   * any line breaks, etc.
   *
   * @param data New data value to be set.
   */
  public void setDataRaw (String data) {
    this.data.set (data);
    processData();
  }
  
  public void setData (DataValue value) {
    this.data = value;
  }
  
  /**
     Sets the data portion of the field to a new value.
    
     @param data New data value to be set.
   */
  public void setData (String data) {
    if (data == null) {
      this.data.set("");
    } else {
      DataFormatRule rule = def.getRule();
      String transformed = rule.transform(data);
      String purified = StringUtils.purifyInvisibles(transformed);
      this.data.set (purified);
      processData();
    }
    
  } // end method setData
  
  /**
     Perform standard processing on a data value.
   */
  private void processData () {
    int i = 0;
    int sign = +1;
    char c = ' ';
    dataInteger = 0;
    dataLong = 0;
    int digitCount = 0;
    String dataStr = data.toString();
    int l = dataStr.length();
    aNumber = true;
    while ((i < l) && (aNumber)) {
      c = dataStr.charAt(i);
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
    return def.toString() + ": " + data.toString() 
    + (aNumber ? " (int " + String.valueOf(dataInteger) + ")" : "");
  }
  
  /**
     Returns this data field as some kind of string.
    
     @return The definition plus the data.
   */
  public String toString () {
    return def.getProperName() + ": " + data;
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
  public boolean operateLogically (String operator, DataField operand) 
      throws IllegalArgumentException {
    boolean trueOrFalse = true;
    int opLength = operator.length();
    char firstChar = ' ';
    String oplc = " ";
    if (opLength > 0) {
      firstChar = operator.charAt (0);
    }
    int opIndex = -1;
    if (Character.isLetter (firstChar)) {
      oplc = operator.toLowerCase();
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
      String datalc = data.toString().toLowerCase();
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
  public boolean equals (DataField field2) {
    return (this.compareTo(field2) == 0);
  }
  
  /**
     Compares this data field to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than field2,
             or positive if this field is greater than field2.
    
     @param  field2 Another data field to be compared to this one.
   */
  public int compareTo (DataField field2) {
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
      return data.compareTo (field2.getDataValue());
    }
  } // end compareTo method
  
  /*
    ==================================================
    Methods brought over from DataRecord
    ==================================================
   */
  
  /**
     Adds a new field to this record, if one with this name does not 
     already exist. If one with this name does exist, then updates the
     data portion of the field to reflect the new data value.
    
     @return Column number of field added or updated.
    
     @param  name   Name of the field to be added or updated.
    
     @param  data   Data value to be added or updated.
   */
  public int storeField (String name, String data) {
    int i = getColumnNumber (name);
    DataField targetField = getField (i);
    if (targetField == null
        || targetField == UNKNOWN_FIELD) {
      DataFieldDefinition workDef = new DataFieldDefinition (name);
      targetField = new DataField (workDef, data);
      return addField (targetField);
    } 
    else {
      targetField.setData (data);
      return i;
    }
  }
  
  /**
     Returns the column number of the desired data field.
    
     @return Column number of the desired field,
             or -1, if the desired field could not be found.
    
     @param  inName The name of the field in which user is interested.
   */
  public int getColumnNumber (String inName) {
    int i = 0;
    CommonName workName = new CommonName ("");
    DataField workField = null;
    DataFieldDefinition workDef;
    CommonName searchName = new CommonName (inName);
    while ((i < fields.size()) 
      && (! searchName.equals (workName))) {
      workField = (DataField)fields.get (i);
      workDef = workField.getDef ();
      workName = workDef.getCommonName ();
      if (! searchName.equals (workName)) {
        i++;
      }
    }
    if (i < fields.size()) {
      return i;
    } else {
      return GlobalConstants.NOT_FOUND;
    }
  }
  
  /**
     Returns the data portion of the desired field as a string.
    
     @return Data portion of the desired field, or an empty
             string if the field does not exist within this record.
    
     @param  inName The name of the field in which user is interested.
   */
  public String getFieldData (String inName) {
    DataField workField = getField (inName);
    if (workField == null) {
      return ""; 
    } else {
      return (String)workField.getData ();
    }
  }
  
  /**
     Returns the data portion of the desired field as an integer.
    
     @return Data portion of the desired field, or zero if the field 
             does not exist within this record, or if the field is
             non-numeric.
    
     @param  inName The name of the field in which user is interested.
   */
  public int getFieldAsInteger (String inName) {
    DataField workField = getField (inName);
    if (workField == null) {
      return 0; 
    } else {
      return workField.getDataInteger ();
    }
  }
  
  /**
   Returns the data portion of the desired field as a long. 
  
   @param inName The name of the field in which the user is interested.
  
   @return Data portion of the desired field, or zero if the field
           does not exist within this record, or if the field is non-numeric. 
  */
  public long getFieldAsLong (String inName) {
    DataField workField = getField (inName);
    if (workField == null) {
      return 0;
    } else {
      return workField.getDataLong();
    }
  }
  
  /**
     Returns the data portion of the desired field as a boolean.
    
     @return Data portion of the desired field, or false if the field 
             does not exist within this record, or if the field is
             not an apparent boolean.
    
     @param  inName The name of the field in which user is interested.
   */
  public boolean getFieldAsBoolean (String inName) {
    DataField workField = getField (inName);
    if (workField == null) {
      return false; 
    } else {
      return workField.getDataBoolean ();
    }
  }
  
  /**
   Given both a column index and a field name, first check the field at this
   index to see if it has the right name; if not, then look up the correct
   field by name.
  
   @param columnNumber The presumptive column number. 
   @param inName The field name. 
  
   @return The data field with a matching field name. 
  */
  public DataField getField(int columnNumber, String inName) {
    CommonName inNameCommon = new CommonName(inName);
    DataField workField = getField(columnNumber);
    if (! workField.getCommonFormOfName().equals(inNameCommon.getCommonForm())) {
      workField = getField(inName);
    }
    return workField;
  }
  
  /**
     Returns the entire data field.
    
     @return Data field, including definition and data,
             or null, if the desired field could not be found.
    
     @param  inName The name of the field in which user is interested.
   */
  public DataField getField (String inName) {
    int i = getColumnNumber (inName);
    if (i != GlobalConstants.NOT_FOUND) {
      return getField (i);
    } else {
      return new DataField (new DataFieldDefinition (inName), "");
    }
  }
  
  /**
     Returns the entire data field.
    
     @return Data field, including definition and data,
             or unknown field, if the desired field could not be found.
    
     @param  columnNumber The column number of the field in which 
                    the user is interested.
   */
  public DataField getField (int columnNumber) {
    if ((columnNumber < fields.size()) && (columnNumber >= 0)) {
      return (DataField)fields.get (columnNumber);
    } else {
      return UNKNOWN_FIELD;
    }
  }
  
  /**
     Returns the entire data field.
    
     @return Data field, including definition and data,
             or null field, if the desired field could not be found.
             If a null field has to be created, and it is at the next
             column number to be added to the record, it will be permanently
             added to the record. 
    
     @param  recDef The Record Definition for this record. 
     @param  columnNumber The column number of the field in which 
                    			the user is interested.
   */
  public DataField getField (RecordDefinition recDef, int columnNumber) {
    if ((columnNumber < fields.size()) && (columnNumber >= 0)) {
      return (DataField)fields.get (columnNumber);
    } else {
      DataFieldDefinition fieldDef = recDef.getDef (columnNumber);
      DataField newField = new DataField (fieldDef, "");
      if (columnNumber == fields.size()) {
        addField (newField);
      }
      return newField;
    }
  }
  
  /**
     Adds a field to this record, given a record layout and a data string.
     Assigns the field definition from the next column of the record 
     definition, so fields must be added in the same sequence as the 
     columns in the record definition.
    
     @return Column number of the field within this record.
    
     @param  recDef Definition to be used for this record.
    
     @param  data   Data to be added, in form of a string.
   */
  public int addField (RecordDefinition recDef, String data) {
    int columnNumber = fields.size();
    String strData = data;
    if (data == null) {
      strData = "";
    }
    DataField field = new DataField (recDef, columnNumber, strData);
    fields.add (field);
    recDef.anotherField (strData, columnNumber);
    return columnNumber;
  }
  
  /**
   Adds a field to this field/record, given a definition and a String 
   representing the data. 
  
   @param inDef - The field definition to be used. 
   @param inData - A string representation of the data value. 
  
   @return The position of the new field within the list of all fields. 
  */
  public int addField (DataFieldDefinition inDef, String inData) {
    DataValue value = inDef.getEmptyDataValue();
    value.set(inData);
    DataField field = new DataField(inDef, value);
    int columnNumber = fields.size();
    fields.add(field);
    return columnNumber;
  }
  
  /**
     Adds a field to this record, given a complete data field
     (consisting of a definition and the data itself).
    
     @return Column number of the field within this record.
    
     @param  inField Data field, consisting of definition plus data.
   */
  public int addField (DataField inField) {
    int columnNumber = fields.size();
    fields.add (inField);
    return columnNumber;
  }
  
} // end DataField class