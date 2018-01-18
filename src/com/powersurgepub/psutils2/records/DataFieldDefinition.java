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

package com.powersurgepub.psutils2.records;

  import com.powersurgepub.psutils2.index.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.values.*;
  
  
/**
   A definition of a generic data field. <p>
   
 */
public class DataFieldDefinition {

  /** The proper name (as originally input) for the field. */
  private    	String          properName;
  
  /** 
     The common name used internally as the key, with all characters
     changed to lower-case, and all spaces and punctuation removed.
   */
  private    	CommonName      commonName;
  
  /** A rule for standardizing the format of this type of data. */
  private    	DataFormatRule  rule;
  
  /** An indicator of the type of data field. */
  private     int             dataFieldType = DEFAULT_TYPE;
  
  public static final int     MIN_TYPE            = 0;
  
  public static final int     DEFAULT_TYPE        = 0;
  public static final int     STRING_TYPE         = 1;
  public static final int     TITLE_TYPE          = 2;
  public static final int     STRING_BUILDER_TYPE = 3;
  public static final int     TAGS_TYPE           = 4;
  public static final int     LINK_TYPE           = 5;
  public static final int     LABEL_TYPE          = 6;
  public static final int     AUTHOR_TYPE         = 7;
  public static final int     DATE_TYPE           = 8;
  public static final int     RATING_TYPE         = 9;
  public static final int     STATUS_TYPE         = 10;
  public static final int     SEQ_TYPE            = 11;
  public static final int     INDEX_TYPE          = 12;
  public static final int     RECURS_TYPE         = 13;
  public static final int     CODE_TYPE           = 14;
  public static final int     DATE_ADDED_TYPE     = 15;
  
  public static final int     MAX_TYPE            = 15;
  
  /** 
     If two fields of this type are to be combined, is it OK to do 
     so by appending one after the other (as in a note field)?
   */
  private    	boolean         combineByAppendingOK;
  
  /** The path to be used to find any associated files needed by functions. */
  private    	String          dataParent = "";
  
  /** Lookup table. */
  private    LookupTable	    lookupTable;
  
  /** Is this a calculated field? */
  private			boolean					calculated = false;
  
  /** The Name of the function to be performed to calculate this field */
	private    	String          functionName = "";
  
  /** Lookup Function literal. */
  private 	static final String	LOOKUP = "lookup";

	/** The first parameter to be passed to this function */
	private    	String          functionParm1 = "";

	/** The second parameter to be passed to this function */
	private    	String          functionParm2 = "";

	/** The third parameter to be passed to this function */
	private    	String          functionParm3 = "";

	/** The fourth parameter to be passed to this function */
	private    	String          functionParm4 = "";

	/** The fifth parameter to be passed to this function */
	private    	String          functionParm5 = "";
  
  /** Log used to record events. */
  private     Logger          log;
  
  /**
     Constructor with input name. Common name is created, default
     rule (with no transformation) is assumed, and no combination by
     appending is allowed.
    
     @param properName Name of field.
   */  
  public DataFieldDefinition (String properName) {
    this (properName, new DataFormatRule(), false);
  }
  
  /**
     Constructor with input name and data formatting rule. Common name is created,
     and no combination by appending is allowed.
    
     @param properName Name of field.
    
     @param rule Instance of DataFormatRule or one of its subclasses.
   */ 
  public DataFieldDefinition (String properName, DataFormatRule rule) {
    this (properName, rule, false);
  }
  
  /**
     Constructor with input name, data formatting rule and combine by apending
     flag. Common name is created.
    
     @param properName Name of field.
    
     @param rule Instance of DataFormatRule or one of its subclasses.
    
     @param combineByAppendingOK Can two of these fields be combined by appending?
   */
  public DataFieldDefinition 
      (String properName, DataFormatRule rule, boolean combineByAppendingOK) {

    this.properName = properName;
    commonName =  new CommonName (properName);
    this.rule  = rule;
    this.combineByAppendingOK = combineByAppendingOK;
    setTypeFromName();
  }
      
  public void setTypeFromName() {
    if (commonName.equals("link")) {
      setType (LINK_TYPE);
    }
    else
    if (commonName.equals("title")) {
      setType (TITLE_TYPE);
    }
    else
    if (commonName.equals("tags")) {
      setType (TAGS_TYPE);
    }
    else
    if (commonName.equals("teaser")) {
      setType (STRING_BUILDER_TYPE);
    }
    else
    if (commonName.equals("body")) {
      setType (STRING_BUILDER_TYPE);
    }
    else
    if (commonName.equals("seq") 
        || commonName.equals("sequence")
        || commonName.contains("version")) {
      setType (SEQ_TYPE);
    }
    else
    if (commonName.equals("index")) {
      setType (INDEX_TYPE);
    }
    else
    if (commonName.equals("author")
        || commonName.equals("by")) {
      setType(AUTHOR_TYPE);
    } 
    else
      if (commonName.equals("dateadded")) {
        setType(DATE_ADDED_TYPE);
      }
      else
    if (commonName.equals("date")
        || commonName.contains("date")) {
      setType(DATE_TYPE);
    }
    else
    if (commonName.equals("rating")) {
      setType(RATING_TYPE);
    }
    else
    if (commonName.equals("status")) {
      setType(STATUS_TYPE);
    }
    else
    if (commonName.equals("recurs") 
        || commonName.equals("every")) {
      setType(RECURS_TYPE);
    }
    else
    if (commonName.equals("code")) {
      setType(CODE_TYPE);
    }else {
      setType(DEFAULT_TYPE);
    }
  }
  
  public void setType(int type) {
    if (type >= MIN_TYPE && type <= MAX_TYPE) {
      this.dataFieldType = type;
    }
  }
  
  public int getType() {
    return dataFieldType;
  }
  
  public DataValue getEmptyDataValue() {
    switch (dataFieldType) {
      case DEFAULT_TYPE:
        return new DataValueString();
      case STRING_TYPE:
        return new DataValueString();
      case TITLE_TYPE:
        return new Title();
      case STRING_BUILDER_TYPE:
        return new DataValueStringBuilder();
      case TAGS_TYPE:
        return new Tags();
      case LINK_TYPE:
        return new Link();
      case SEQ_TYPE:
        return new DataValueSeq();
      case INDEX_TYPE:
        return new IndexPageValue();
      case RECURS_TYPE:
        return new RecursValue();
      case CODE_TYPE:
        return new DataValueStringBuilder();
      default:
        return new DataValueString();
    }
  }
  
  /**
     Sets the path to be used to find other files.
    
     @param dataParent The path to any associated files needed by this field's function. 
   */
  public void setDataParent (String dataParent) {
    this.dataParent = dataParent;
  }

  /**
     Sets the Name of the function to be performed to calculate this field.
 
     @param  functionName The Name of the function to be performed.
   */
  public void setFunctionName (String functionName) {
    this.functionName = functionName.toLowerCase();
  }

  /**
     Returns the Name of the function to be performed to calculate this field.
 
     @return The Name of the function to be performed.
   */
  public String getFunctionName () {
    return functionName;
  }

  /**
     Sets the first parameter to be passed to this function.
 
     @param  functionParm1 The first parameter to be passed to this function.
   */
  public void setFunctionParm1 (String functionParm1) {
    this.functionParm1 = functionParm1;
  }

  /**
     Returns the first parameter to be passed to this function.
 
     @return The first parameter to be passed to this function.
   */
  public String getFunctionParm1 () {
    return functionParm1;
  }

  /**
     Sets the second parameter to be passed to this function.
 
     @param  functionParm2 The second parameter to be passed to this function.
   */
  public void setFunctionParm2 (String functionParm2) {
    this.functionParm2 = functionParm2;
  }

  /**
     Returns the second parameter to be passed to this function.
 
     @return The second parameter to be passed to this function.
   */
  public String getFunctionParm2 () {
    return functionParm2;
  }

  /**
     Sets the third parameter to be passed to this function.
 
     @param  functionParm3 The third parameter to be passed to this function.
   */
  public void setFunctionParm3 (String functionParm3) {
    this.functionParm3 = functionParm3;
  }

  /**
     Returns the third parameter to be passed to this function.
 
     @return The third parameter to be passed to this function.
   */
  public String getFunctionParm3 () {
    return functionParm3;
  }

  /**
     Sets the fourth parameter to be passed to this function.
 
     @param  functionParm4 The fourth parameter to be passed to this function.
   */
  public void setFunctionParm4 (String functionParm4) {
    this.functionParm4 = functionParm4;
  }

  /**
     Returns the fourth parameter to be passed to this function.
 
     @return The fourth parameter to be passed to this function.
   */
  public String getFunctionParm4 () {
    return functionParm4;
  }

  /**
     Sets the fifth parameter to be passed to this function.
 
     @param  functionParm5 The fifth parameter to be passed to this function.
   */
  public void setFunctionParm5 (String functionParm5) {
    this.functionParm5 = functionParm5;
  }

  /**
     Returns the fifth parameter to be passed to this function.
 
     @return The fifth parameter to be passed to this function.
   */
  public String getFunctionParm5 () {
    return functionParm5;
  }
  
  /**
    Complete the definition for this data field, processing any parms
    that were passed.
    */
  public void completeDefinition () {
    if (functionName.equals (LOOKUP)) {
      boolean lookupCaseSensitive = false;
      if ((functionParm3 != null)
          && (functionParm3.length() > 0)) {
        char c = Character.toLowerCase (functionParm3.charAt(0));
        if (c == 'y' || c == 't') {
          lookupCaseSensitive = true;
        }
      } // if functionParm3 present
      try {
        lookupTable = new LookupTable (dataParent, 
            functionParm1, functionParm2, lookupCaseSensitive);
        lookupTable.setLog (log);
        calculated = true;
      } catch (java.io.IOException e) {
        logEvent (LogEvent.MAJOR, 
            "I/O Exception on TabDelimLookup with "
            + "\n  parent path = "+ dataParent 
            + "\n  file name = " + functionParm1 
            + "\n  key field = " + functionParm2 
            + "\n  message = " + e.toString(),
            false);
        calculated = false;
      }
    } // end of lookup function
  } // end of method
  
  /**
    Is this a calculated field?
    
    @return True if this field should be calculated.
   */
  public boolean isCalculated () {
    return calculated;
  }
  
  /**
    For calculated fields, calculate the field value.
    
    @return calculated field value, or original value if not a calculated field.
    
    @param dataRec Record containing this field.
   */
  public String calculate (DataRecord dataRec) {
    if (lookupTable != null) {
      return lookup (dataRec);
    } else {
      return dataRec.getFieldData (commonName.toString());
    }
  }
  
  /**
    For lookup fields, lookup the field value in a separate table.
    
    @return value from lookup table, or original value if not a lookup field.
    
    @param dataRec DataRecord containing this field.
   */
  public String lookup (DataRecord dataRec) {
    if (lookupTable == null) {
      return dataRec.getFieldData (commonName.toString());
    } else {
      return lookupTable.get (dataRec.getFieldData (functionParm4), functionParm5);
    }
  }
  
  /**
     Compare this field definition to another one, by comparing 
     their commonName strings.
    
     @return One of the following values. <ul> <li>
               positive if anotherDef is greater than this one <li>
               zero     if anotherDef is equal to this one <li>
               negative if anotherDef is less than this one </ul>
    
     @param anotherDef Another instance of DataFieldDefinition to compare
                       to this one.
   */
  public int compareTo (DataFieldDefinition anotherDef) {
    return this.commonName.compareTo (anotherDef.getCommonName());
  }
  
  /**
     Compare this field definition to another one, by comparing 
     their commonName strings.
    
     @return One of the following values. <ul> <li>
               true  if anotherDef is equal to this one <li>
               false if they are not equal </ul>
    
     @param anotherDef Another instance of DataFieldDefinition to compare
                       to this one.
   */
  public boolean equals (DataFieldDefinition anotherDef) {
    return this.commonName.equals (anotherDef.getCommonName());
  }
  
  /** 
     Return all field values.
    
     @return All field values.
   */
  public String toString () {
    return properName 
      + " " + commonName 
      + " " + rule.toString()
      + " " + String.valueOf (combineByAppendingOK);
  }
  
  /**
     Gets the common name of the field (without caps or puncutation).
    
     @return The common name of the field.
   */
  public CommonName getCommonName () {
    return commonName;
  }
  
  /**
     Gets the proper name of the field (as originally input).
    
     @return The proper name of the field.
   */
  public String getProperName () {
    return properName;
  }
  
  /**
     Gets the data formatting rule.
    
     @return The data formatting rule.
   */
  public DataFormatRule getRule () {
    return rule;
  }
  
  /**
     Is it OK to combine fields?
    
     @return The flag that indicates whether it is OK to combine fields
             by appending one to the other.
   */
  public boolean isCombineByAppendingOK () {
    return combineByAppendingOK;
  }
  
  /**
     Sets the data formatting rule to use for fields of this type.
    
     @parm rule The data formatting rule.
   */
  public void setRule (DataFormatRule rule) {
    this.rule = rule;
  }
  
  /**
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  /**
     Creates a LogEvent object and then records it.
    
     @param severity      the severity of the event
    
     @param message       the message to be written to the log
    
     @param dataRelated   indicates whether this event is related
                          to preceding data.
   */
  public void logEvent (int severity, String message, boolean dataRelated) {
  	ensureLog();
		log.recordEvent (severity, message, dataRelated);
  }
  
  /**
     Allocate a new Logger instance if one has not yet been provided.
   */
  private void ensureLog () {
    if (log == null) {
      log = new Logger (new LogOutput());
    }
  }
  
} // end of class