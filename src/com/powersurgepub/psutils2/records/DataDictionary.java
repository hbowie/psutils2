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
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.strings.*;

  import java.io.IOException;
  import java.util.Vector;
  
  
/**
   A dictionary of DataFieldDefinition and DataFieldAlias objects. <p>
  
 */
public class DataDictionary 
    implements  DataSource, 
                DataStore  {
    
  /** The proper name of a field. */
  public   final    static    String  PROPER_NAME_ID          = "Proper Name";
  
  /** The common name of a field. */
  public   final    static    String  COMMON_NAME_ID          = "Common Name";
  
  /** The alias for a field. */
  public   final    static    String  ALIAS_FOR_ID            = "Alias For";
  
  /** The data formatting rule of a field. */
  public   final    static    String  DATA_FORMAT_RULE_ID     = "Data Format Rule";
  
  /** Combine by Appending Flag for a field. */
  public   final    static    String  COMBINE_BY_APPENDING_ID = "Combine by Appending?";
  
  /** Function Name field. */
  public   final    static    String  FUNCTION_NAME = "Function Name";
  
  /** Function Parm fields. */
  public   final    static    String  FUNCTION_PARM1 = "Parm1";
  public   final    static    String  FUNCTION_PARM2 = "Parm2";
  public   final    static    String  FUNCTION_PARM3 = "Parm3";
  public   final    static    String  FUNCTION_PARM4 = "Parm4";
  public   final    static    String  FUNCTION_PARM5 = "Parm5";
  
  /** Unknown field definition (to be returned when index is out of bounds) */
  public   final    static    DataFieldDefinition UNKNOWN_FIELD_DEFINITION 
    = new DataFieldDefinition ("** unknown **");
    
  /** A data dictionary for dictionary entries themselves. */
  private static DataDictionary   metaDictionary;
  
  /** A record definition for data dictionary fields. */
  private static RecordDefinition metaRecDef;

  /** A collection of DataFieldDefinition objects. */
  private   Vector       defs;
  
  /** A collection of DataFieldAlias objects. */
  private   Vector       aliases;
  
  /** An index for cycling through the definitions sequentially. */
  private   int          defNumber;
  
  /** An index for cycling through the aliases sequentially. */
  private   int          aliasNumber;
  
  /** A sequential record number assigned to each dictionary entry returned as a record. */
  private   int          recordNumber = 0;
  
  /** The logger to use to log events. */    
  private    Logger           log;
  
  /* Let's not log all data. */
  private    boolean          dataLogging = false;
  
  /** Data to be sent to the log. */
  private    LogData          logData;
  
  /** An event to be sent to the log. */
  private    LogEvent         logEvent;
  
  /** The identifier for this reader. */
  private    String           fileId;
  
  /** Path to the original source file (if any). */
  private		 String							dataParent;
  
  /**
     The constructor requires no arguments.
   */
  public DataDictionary () {
    defs = new Vector ();
    aliases = new Vector ();
    resetDefNumber();
  }
  
  /**
     Opens the dictionary for input, to return the 
     dictionary entries as data records.
    
     @param inDict A data dictionary to use.
   */
  public void openForInput (DataDictionary inDict) {
    openForInput();
  }
  
  /**
     Opens the dictionary for input, to return the 
     dictionary entries as data records.
    
     @param inRecDef A record definition to use.
   */
  public void openForInput (RecordDefinition inRecDef) {
    openForInput();
  }
  
  /**
     Opens the reader for input, to return the 
     dictionary entries as data records.
   */
  public void openForInput () {
    if (metaRecDef == null) {
      initMeta();
    }
    this.resetDefNumber();
    this.resetAliasNumber();
    fileId = "data dictionary";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
    recordNumber = 0;
    ensureLog();
    logEvent.setSeverity (LogEvent.NORMAL);
    logEvent.setMessage ("Data dictionary opened successfully for input");
    logEvent.setDataRelated(false);
    log.recordEvent (logEvent);
  }
  
  /**
     Loads the data dictionary from any DataSource (such as TabDelimFile).
    
     @param inData Source of data dictionary entries. DataSource object
                   must have already been constructed, but not yet
                   opened for input.
    
     @throws IOException If trouble reading inData.
   */
  public void load (DataSource inData) 
      throws IOException {
    this.openForOutput();
    inData.setLog (log);
    inData.setDataLogging (false);
    inData.openForInput (metaDictionary);
    dataParent = inData.getDataParent();
    DataRecord entry = inData.nextRecordIn();
    while (! inData.isAtEnd()) {
      if (entry != null) {
        this.nextRecordOut (entry);
      }
      entry = inData.nextRecordIn();
    }
    inData.close();
    this.close();
  }
  
  /**
     Stores the data dictionary to any DataStore (such as TabDelimFile).
    
     @param outData Data store for data dictionary entries. DataStore
                    object must have already been created, but
                    not yet opened for output.
    
     @throws IOException If trouble writing outData.
   */
  public void store (DataStore outData )
      throws IOException {
    this.openForInput();
    outData.openForOutput(metaRecDef);
    DataRecord entry = nextRecordIn();
    while (entry != null) {
      outData.nextRecordOut (entry);
      entry = nextRecordIn();
    }
    outData.close();
    this.close();
  }
  
  /**
     Prepares the dictionary to receive dictionary entries
     as data records, adding them to this dictionary.
    
     @param recDef Record Definition accepted for the sake of consistency
                   with DataStore.
   */
  public void openForOutput (RecordDefinition recDef) {
    this.openForOutput();
  }
  
  /**
     Prepares the dictionary to receive dictionary entries
     as data records, adding them to this dictionary.
   */
  public void openForOutput () {
    if (metaRecDef == null) {
      initMeta();
    }
    this.resetDefNumber();
    this.resetAliasNumber();
    fileId = "data dictionary";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
    recordNumber = 0;
    ensureLog();
    logEvent.setSeverity (LogEvent.NORMAL);
    logEvent.setMessage ("Data dictionary opened successfully for output");
    logEvent.setDataRelated(false);
    log.recordEvent (logEvent);
  }
  
  /**
     Initialize the metaDictionary and metaRecDef fields
     with standard data about the data dictionary.
   */
  public void initMeta () {
    metaDictionary = new DataDictionary();
    metaRecDef = new RecordDefinition (metaDictionary);
    metaRecDef.addColumn (PROPER_NAME_ID);
    metaRecDef.addColumn (COMMON_NAME_ID);
    metaRecDef.addColumn (ALIAS_FOR_ID);
    metaRecDef.addColumn (DATA_FORMAT_RULE_ID);
    metaRecDef.addColumn (COMBINE_BY_APPENDING_ID);
    metaRecDef.addColumn (FUNCTION_NAME);
    metaRecDef.addColumn (FUNCTION_PARM1);
    metaRecDef.addColumn (FUNCTION_PARM2);
    metaRecDef.addColumn (FUNCTION_PARM3);
    metaRecDef.addColumn (FUNCTION_PARM4);
    metaRecDef.addColumn (FUNCTION_PARM5);
  }
  
  /**
     Returns the dictionary entry as a data record.
    
     @return Next data record.
   */
  public DataRecord nextRecordIn () {
    if (this.hasMoreDefs()) {
      DataFieldDefinition workDef = this.nextDef();
      DataRecord workRec = new DataRecord();
      workRec.addField(metaRecDef, workDef.getProperName());
      workRec.addField(metaRecDef, workDef.getCommonName().toString());
      workRec.addField(metaRecDef, "");
      workRec.addField(metaRecDef, workDef.getRule().toString());
      workRec.addField(metaRecDef, String.valueOf (workDef.isCombineByAppendingOK()));
      workRec.addField(metaRecDef, workDef.getFunctionName());
      workRec.addField(metaRecDef, workDef.getFunctionParm1());
      workRec.addField(metaRecDef, workDef.getFunctionParm2());
      workRec.addField(metaRecDef, workDef.getFunctionParm3());
      workRec.addField(metaRecDef, workDef.getFunctionParm4());
      workRec.addField(metaRecDef, workDef.getFunctionParm5());
      recordNumber++;
      return workRec;
    } 
    else 
    if (this.hasMoreAliases()) {
      DataFieldAlias workAlias = this.nextAlias();
      DataRecord workRec = new DataRecord();
      workRec.addField(metaRecDef, workAlias.getAlias().toString());
      workRec.addField(metaRecDef, workAlias.getAlias().toString());
      workRec.addField(metaRecDef, workAlias.getOriginal().toString());
      workRec.addField(metaRecDef, "");
      workRec.addField(metaRecDef, "");
      workRec.addField(metaRecDef, "");
      workRec.addField(metaRecDef, "");
      workRec.addField(metaRecDef, "");
      workRec.addField(metaRecDef, "");
      recordNumber++;
      return workRec;
    } 
    else {
      return null;
    }
  } // end method nextRecordIn
  
  /**
     Adds the next dictionary entry to the dictionary, when
     received as a data record.
    
     @param entry Next data record to be added to the dictionary.
   */
  public void nextRecordOut (DataRecord entry) {
  
    // Set proper name
    String properName = entry.getFieldData (PROPER_NAME_ID);
    if (! properName.equals ("")) {
    
      // Set common name
      String commonName = entry.getFieldData (COMMON_NAME_ID);
      
      // Set alias for
      String aliasFor = entry.getFieldData (ALIAS_FOR_ID);
      
      // Set data format rule
      String dataFormatRuleStr = entry.getFieldData (DATA_FORMAT_RULE_ID);
      DataFormatRule dataFormatRule = DataFormatRule.constructRule (dataFormatRuleStr);
      
      // Set Combine by Appending ID
      String combineByAppendingOKStr = entry.getFieldData (COMBINE_BY_APPENDING_ID);
      boolean combineByAppendingOK = Boolean.valueOf(combineByAppendingOKStr).booleanValue();
      
      if (aliasFor.equals ("")) {
        DataFieldDefinition workDef = new DataFieldDefinition (properName, 
          dataFormatRule, combineByAppendingOK);
        ensureLog();
        workDef.setLog(log);
        workDef.setDataParent (dataParent);
        // Set Function fields
        workDef.setFunctionName (entry.getFieldData (FUNCTION_NAME));
        workDef.setFunctionParm1 (entry.getFieldData (FUNCTION_PARM1));
        workDef.setFunctionParm2 (entry.getFieldData (FUNCTION_PARM2));
        workDef.setFunctionParm3 (entry.getFieldData (FUNCTION_PARM3));
        workDef.setFunctionParm4 (entry.getFieldData (FUNCTION_PARM4));
        workDef.setFunctionParm5 (entry.getFieldData (FUNCTION_PARM5));
        workDef.completeDefinition();
        int defNum = this.putDef (workDef);
        recordNumber++;
      } else {
        DataFieldAlias workAlias = new DataFieldAlias (properName, aliasFor);
        int aliasNum = this.putAlias (workAlias);
        recordNumber++;
      } 
    }
  } // end method nextRecordOut
  
  /**
     Returns the sequential record number of the last record returned.
    
     @return Sequential record number of the last record returned via 
             nextRecordIn, where 1 identifies the first record.
   */
  public int getRecordNumber () {
    return recordNumber;
  }
  
  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return ((! this.hasMoreDefs()) && (! this.hasMoreAliases()));
  }
  
  /**
     Closes the reader/writer.
   */
  public void close() {
    ensureLog();
    logEvent.setSeverity (LogEvent.NORMAL);
    logEvent.setMessage ("Data dictionary closed successfully");
    logEvent.setDataRelated (false);
    log.recordEvent (logEvent);
  }
  
  /**
     Ensures that a log is available, by allocating a new one if
     one has not already been supplied.
   */
  protected void ensureLog () {
    if (log == null) {
      setLog (new Logger (new LogOutput()));
    }
  }
    
  /**
     Sets a log to be used by the reader to record events.
    
     @param  log A logger object to use.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  /**
     Indicates whether all data records are to be logged.
    
     @param  dataLogging True if all data records are to be logged.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
  /**
     Sets a file ID to be used to identify this reader in the log.
    
     @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }
  
  /**
     Does nothing in this class.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {

  }
  
  /**
     Retrieves the path to the original source file (if any).
    
     @return Path to the original source file (if any).
   */
  public String getDataParent () {
    if (dataParent == null) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return dataParent;
    }
  }
  
  /**
     Sets a path to be used to read any associated files.
    
     @param  dataParent A path to be used to read any associated files.
   */
  public void setDataParent (String dataParent) {
    this.dataParent = dataParent;
  }
  
  /**
     Returns the record definition for the reader.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef () {
    return metaRecDef;
  }
  
  /**
     Returns a definition at a given index.
    
     @return The field definition.
    
     @param  The index pointing to the desired definition.
   */
  public DataFieldDefinition getDef (int inIndex) {
    if ((inIndex < 0) || (inIndex > defs.size())) {
      return UNKNOWN_FIELD_DEFINITION;
    } else {
      return (DataFieldDefinition)defs.elementAt(inIndex);
    }
  }
  
  /**
     Sets the data format rule for a given DataFieldDefinition, identified by
     its name.
    
     @param inName Name of the DataFieldDefinition object whose rule
                   is to be set.
    
     @param inRule DataFormatRule (or one of its subclasses) to be
                   used for placing these fields into a standard
                   format.
   */
  public void setRule (String inName, DataFormatRule inRule) {
    DataFieldDefinition workDef = getDef (inName);
    workDef.setRule (inRule);
  }
  
  /** 
     Gets the DataFieldDefinition associated with a particular name, 
     either directly or through an alias.
    
     @return A data field definition from the data dictionary, or null
             if no matching definition found.
    
     @param inName The name of a data field definition. This will
                   be converted to a common name internally.
   */
  public DataFieldDefinition getDef (String name) {
    CommonName common = new CommonName (name);
    return getDef (common);
  }
  
  /** 
     Gets the DataFieldDefinition stored in this dictionary, matching
     the name of the DataFieldDefinition being passed, or its alias.
    
     @return A data field definition from the data dictionary.
    
     @param inDef Another DataFieldDefinition.
   */
  public DataFieldDefinition getDef (DataFieldDefinition inDef) {
    return getDef (inDef.getCommonName());
  }
  
  /** 
     Gets the DataFieldDefinition associated with a particular common name,
     either directly or through an alias.
    
     @return A data field definition from the data dictionary, or null
             if no definition found.
    
     @param inCommon The name of a data field definition, assumed to be
                     already converted to its common form.
   */
  public DataFieldDefinition getDef (CommonName inCommon) {
    int i = getDefNum (inCommon);
    if (i == GlobalConstants.NOT_FOUND) {
      return null;
    } else {
      return getDef(i);
    }
  }
  
  /** 
     Gets the index of the DataFieldDefinition associated with 
     a particular name, either directly or through an alias.
    
     @return The index associated with a data field definition 
             from the data dictionary, or a -1 if not found.
    
     @param inName The name of a data field definition, 
                   which will be converted to its common form.
   */
  public int getDefNum (String name) {
    CommonName common = new CommonName (name);
    return getDefNum (common);
  }
  
  /** 
     Gets the index of the DataFieldDefinition associated with 
     a particular common name (as taken from another definition), 
     either directly or through an alias.
    
     @return The index associated with a data field definition 
             from the data dictionary, or a -1 if not found.
    
     @param inDef Another data field definition, whose name
                  will be used to match a definition already
                  in this dictionary.
   */
  public int getDefNum (DataFieldDefinition inDef) {
    return getDefNum (inDef.getCommonName());
  }
  
  /** 
     Gets the index of the DataFieldDefinition associated with 
     a particular common name, either directly or through an alias.
    
     @return The index associated with a data field definition 
             from the data dictionary, or a -1 if not found.
    
     @param inCommon The name of a data field definition, 
                     which is assumed to already be in its 
                     common form.
   */
  public int getDefNum (CommonName inCommon) {
    CommonName common = getAliasOriginal (inCommon);
    int i = 0;
    while ((i < defs.size()) 
      && (!(common.equals(getDef(i).getCommonName())))) {
      i++;
    }
    if (i < defs.size()) {
      return i;
    } else {
      return GlobalConstants.NOT_FOUND;
    }
  }
  
  /**
     Stores a new Data Field Definition, if it does not already exist
     in the dictionary, either directly or through an alias.
    
     @return The index of the new entry in the dictionary, or
             an existing entry that has the same common name
             as inDef.
    
     @param  inDef The Data Field Definition to be added to
                   the dictionary.
   */
  public int putDef (DataFieldDefinition inDef) {
    int seq;
    seq = getDefNum (inDef);
    if (seq == GlobalConstants.NOT_FOUND) {
      seq = defs.size();
      ensureLog();
      inDef.setLog(log);
      CommonName alias = inDef.getCommonName();
      CommonName original = getAliasOriginal (alias);
      if (original.equals (alias)) {
        defs.addElement (inDef);
      } else {
        defs.addElement (new DataFieldDefinition (original.toString()));
      }
    } 
    return seq;
  }
  
  /**
     Stores a new Data Field Definition, if it does not already exist
     in the dictionary, either directly or through an alias.
    
     @return The index of the new entry in the dictionary, or
             an existing entry that has the same common name
             as inName.
    
     @param  inName A field name from which a new Data Field
                    Definition will be constructed.
   */
  public int putDef (String inName) {
    DataFieldDefinition workDef;
    workDef = new DataFieldDefinition (inName);
    return putDef (workDef);
  }
  
  /**
     Stores a new Data Field Alias, if it does not already exist
     in the dictionary.
    
     @return The index of the new entry in the dictionary, or
             an existing entry that has the same common name
             as inAlias.
    
     @param  inAlias    The alternate name for the entry.
    
     @param  inOriginal The original name for the entry.
   */
  public int putAlias (String inAlias, String inOriginal) {
    DataFieldAlias workAlias = new DataFieldAlias (inAlias, inOriginal);
    return putAlias (workAlias);
  }
  
  /**
     Stores a new Data Field Alias, if it does not already exist
     in the dictionary.
    
     @return The index of the new entry in the dictionary, or
             an existing entry that has the same common name
             as inAliasObject.
    
     @param  inAliasObject The Data Field Alias to be added to
                           the dictionary.
   */
  public int putAlias (DataFieldAlias inAliasObject) {
    int i = 0;
    boolean found = false;
    CommonName inAlias = inAliasObject.getAlias();
    while ((i < aliases.size()) && (! found)) {
      DataFieldAlias currAlias = (DataFieldAlias)aliases.elementAt(i);
      if (inAlias.equals(currAlias.getAlias())) {
        found = true; 
      } else {
        i++;
      }
    }
    if (found) {
      aliases.setElementAt (inAliasObject, i);
    } else {
      i = aliases.size();
      aliases.addElement (inAliasObject);
    }
    return i;
  }
  
  /**
     Returns the original name for the given alias, if one exists
     (otherwise the input alias name is returned).
    
     @return The original name for an alias, or the input name,
             if no DataFieldAlias exists for the input alias.
    
     @param  inAlias The alias for which the original name
                     is desired.
   */
  public CommonName getAliasOriginal (String inAlias) {
    CommonName commonAlias = new CommonName (inAlias);
    return getAliasOriginal (commonAlias);
  }

  /**
     Returns the original name for the given alias, if one exists
     (otherwise the input alias name is returned).
    
     @return The original name for an alias, or the input name,
             if no DataFieldAlias exists for the input alias.
    
     @param  commonAlias The alias for which the original name
                         is desired, in CommonName format.
   */
  public CommonName getAliasOriginal (CommonName commonAlias) {
    int i = 0;
    boolean found = false;
    DataFieldAlias currAlias;
    while ((i < aliases.size()) && (! found)) {
      currAlias = (DataFieldAlias)aliases.elementAt(i);
      if (commonAlias.equals(currAlias.getAlias())) {
        found = true; 
      } else {
        i++;
      }
    }
    if (found) {
      currAlias = (DataFieldAlias)aliases.elementAt(i);
      return currAlias.getOriginal();
    } else {
      return commonAlias;
    }
  }
  
  /**
     Returns a string identifying the dictionary.
    
     @return The toString value for all the definitions in the dictionary.
   */
  public String toString () {
    StringBuffer recordBuf = new StringBuffer ();
    for (int i = 0; i < defs.size (); i++) {
      if (i > 0) {
        recordBuf.append ("; ");
      }
      recordBuf.append (defs.elementAt(i).toString());
    }
    return recordBuf.toString ();
  }
  
  /**
     Indicates whether there are more definitions to be
     processed sequentially.
    
     @return True if defNumber is less than the size of the DataFieldDefinition
             collection.
   */
  public boolean hasMoreDefs () {
    return (defNumber < defs.size());
  }
  
  /**
     Returns the first Definition in the dictionary.
    
     @return The first DataFieldDefinition in the dictionary.
   */
  public DataFieldDefinition firstDef () {
    resetDefNumber();
    return nextDef ();
  }
  
  /**
     Returns the next Definition in the dictionary.
    
     @return Next DataFieldDefinition in the dictionary.
   */
  public DataFieldDefinition nextDef () {
    return (DataFieldDefinition)defs.elementAt (defNumber++);
  }
  
  /**
     Resets the defNumber index to its starting value.
   */
  public void resetDefNumber () {
    defNumber = 0;
  }
  
  /**
     Indicates whether there are more aliases to be
     processed sequentially.
    
     @return True if aliasNumber is less than the size of the 
             DataFieldAlias collection.
   */
  public boolean hasMoreAliases () {
    return (aliasNumber < aliases.size());
  }
  
  /**
     Returns the first Alias in the dictionary.
    
     @return The first DataFieldAlias in the dictionary.
   */
  public DataFieldAlias firstAlias () {
    resetAliasNumber();
    return nextAlias ();
  }
  
  /**
     Returns the next Alias in the dictionary.
    
     @return Next DataFieldAlias in the dictionary.
   */
  public DataFieldAlias nextAlias () {
    return (DataFieldAlias)aliases.elementAt (aliasNumber++);
  }
  
  /**
     Resets the aliasNumber index to its starting value.
   */
  public void resetAliasNumber () {
    aliasNumber = 0;
  }

}
