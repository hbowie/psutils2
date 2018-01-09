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

/**
 Determines whether an item should be included in a list or excluded from it. 

 @author Herb Bowie.
 */
public class PSFieldFilter {
  
  /** The list to be filtered. */
  private    PSList   psList;

  /** The column number of this field within the list. */
  private    int                column;

  /** Logical operator to use for comparison. */
  private    String             operator;

  /** Value to be compared to field data. */
  private    PSField            value;
  
  public PSFieldFilter() {
    
  }
  
/**
 Constructs the filter field.

 @param psList        The list to be filtered.
 @param fieldName     The name of a field within the record definition.
 @param operator      The operator to use for comparison.
 @param valueString   The value to compare the field to, passed as a String.
 */
  public PSFieldFilter (PSList psList, String fieldName, 
      String operator, String valueString) {

    this.psList = psList;
    this.column = psList.getColumnNumber (fieldName);
    this.operator = operator;
    this.value = new PSField(valueString);
  }
  
   /**
     Selects the item.

     @param psItem An item to be evaluated.

     @return Decision whether to select the item (true or false).

     @throws IllegalArgumentException if the operator is invalid.
   */
  public boolean selects (PSItem psItem) 
    throws IllegalArgumentException {

    Object obj = psItem.getColumnValue(column);
    String fieldValue;
    if (obj == null) {
      fieldValue = "";
    } else {
      fieldValue = obj.toString();
    }
    PSField field = new PSField (fieldValue);
    return field.operateLogically (operator, value);
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

  public void setOperator (String operator) {
    this.operator = operator;
  }

  /**
     Returns the operator for this field.

     @return Operator for this field.
   */
  public String getOperator () {
    return operator;
  }
  
  public void setValue (String valueString) {
    this.value = new PSField(valueString);
  }
  
  public void setValue (PSField value) {
    this.value = value;
  }

  /**
     Returns the value to which this field is to be compared.

     @return Value to use in comparison.
   */
  public String getValue () {
    return value.getData();
  }

}
