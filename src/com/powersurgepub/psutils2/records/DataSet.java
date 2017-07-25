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

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;

  import java.io.*;
  import java.util.*;

  import javafx.collections.*;
  
/**
   A set, or collection, of DataRecord objects, capable of being sorted. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
 */
public class DataSet 
    implements DataSource,
               DataStore  {
  
  public     static final int   LOW_MEMORY_THRESHOLD = 100000;
  public     static final String TAG = "Tag";
  
  /** Next available number to be assigned to the next data set instantiated. */
  private    static  int        dataSetNumber = 0;
  
  /** Definition of the records being stored in the data set. */
  private    RecordDefinition   recDef;
  
  /** Collection of all records in the data set. */
  private    ObservableList<DataRecord> records;
  
  /** Index used to cycle through the records in the data set. */
  private    int                recordNumber;
  
  /** Index to last record returned. */
  private    int                lastRecordNumber;
  
  /** Number indicating original load sequence. */
  private		 int								nextRecordSequence = 0;

  /** Number of records loaded with last input action. */
  private    int                recordsLoaded = 0;
  
  /** Data dictionary used by the record definition. */
  private    DataDictionary     dict;
  
  /** Specification of the sequence in which the records should be sorted. */ 
  private    SequenceSpec       seqSpec;
  
  /** Specification of the output filter to be used to select desired records. */
  private    DataFilter         inputFilter;
  
  /** Should all data be logged (or only data preceding significant events(? */
  private    boolean            dataLogging = false;
  
  /** Have we already warned user that memory is low? */
  private    boolean            userWarnedOnMemory = false;
  
  /** Log used to record events. */
  private    Logger             log;
  
  /** Data to be logged. */
  private    LogData            logData;
  
  /** Event to be logged. */
  private    LogEvent           logEvent;
  
  /** Identifier used to identify this reader in the log. */
  private    String             fileId;
  
  /** Path to the original source file (if any). */
  private		 String							dataParent;
  
  /** 
     Constructs an empty DataSet from a given record definition.
    
     @param recDef Definition of records to be stored.
   */
  public DataSet (RecordDefinition recDef) {
    this.recDef = recDef;
    initialize();
  }
  
  /** 
     Constructs a DataSet from another DataSource implementation, 
     using the passed data dictionary.
     The passed data source provides the record definition, as well as
     all of its records.
    
     @param inDict Data Dictionary to be used.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public DataSet (DataDictionary inDict, DataSource inData) 
      throws IOException {
    dict = inDict;
    inData.openForInput (dict);
    load(inData);
  }
  
  /** 
     Constructs a DataSet from another DataSource implementation, 
     using the passed data dictionary.
     The passed data source provides the record definition, as well as
     all of its records.
    
     @param inDict Data Dictionary to be used.
    
     @param inData Data source that allows data records to be read.
   
     @param log Logger to be used to report significant program events.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public DataSet (DataDictionary inDict, DataSource inData, Logger log) 
      throws IOException {
    dict = inDict;
    inData.openForInput (dict);
    setLog (log);
    load(inData);
  }
  
  /** 
     Constructs a DataSet from another DataSource implementation, 
     using the passed data dictionary.
     The passed data source provides the record definition, as well as
     all of its records.
     A maximum record count is also passed, to limit the size
     of the data set. 
    
     @param inDict Data Dictionary to be used.
    
     @param inData Data source that allows data records to be read.
    
     @param maxRecs Maximum number of records to be loaded.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public DataSet (DataDictionary inDict, DataSource inData, int maxRecs) 
      throws IOException {
    dict = inDict;
    inData.openForInput (dict);
    dataParent = inData.getDataParent();
    recDef = inData.getRecDef();
    initialize();
    while ((! inData.isAtEnd())
        && (records.size() < maxRecs)) {
      DataRecord nextRec = inData.nextRecordIn();
      if (nextRec != null) {
        this.addRecord (nextRec);
      }
    }
    inData.close();
  }
  
  /** 
     Constructs a DataSet from another DataSource implementation.
     The passed data source provides the record definition, as well as
     all of its records.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public DataSet (DataSource inData) 
      throws IOException {
    inData.openForInput ();
    load (inData);
  }
  
  /** 
     Constructs a DataSet without loading from another data source.
    
     @param inDict Data Dictionary to be used.
   
     @param log Logger to be used to report significant program events.

   */
  public DataSet (DataDictionary inDict, Logger log) {
    dict = inDict;
    setLog (log);
  }
  
  /** 
     Loads this DataSet from another DataSource implementation.
     The passed data source provides the record definition, as well as
     all of its records.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public void loadAndExplode (DataSource inData) 
      throws IOException {
    
    inData.openForInput ();
    dataParent = inData.getDataParent();
    recDef = inData.getRecDef();
    
    // Look for Tags Field
    int tagsColumnNumber = -1;
    boolean hasTag = false;
    int columnNumber = 0;
    while (columnNumber < recDef.getNumberOfFields()) {
      DataFieldDefinition fd = recDef.getDef(columnNumber);
      if (fd.getCommonName().getCommonForm().equalsIgnoreCase(Tags.SIMPLE_NAME)
          || fd.getCommonName().getCommonForm().equalsIgnoreCase
                (Tags.ALTERNATE_SIMPLE_NAME)) {
        tagsColumnNumber = columnNumber; 
      } 
      else 
      if (fd.getCommonName().getCommonForm().equalsIgnoreCase(TAG)) {
        hasTag = true;
      }
      columnNumber++;
    }
    if (tagsColumnNumber >= 0 && (! hasTag)) {
      recDef.addColumn("Tag");
    }
    
    initialize();
    recordsLoaded = 0;
    
    while (! inData.isAtEnd()) {
      DataRecord nextRec = inData.nextRecordIn();
      if (nextRec != null) {
        Tags tags = new Tags(nextRec.getField(Tags.SIMPLE_NAME).getData());
        int tagsIndex = 0;
        boolean endOfTags = false;
        while (! endOfTags) {
          String tag = tags.getTag(tagsIndex);
          if (tagsIndex == 0 || tag.length() > 0) {
            DataRecord outRec = new DataRecord();
            nextRec.startWithFirstField();
            while (nextRec.hasMoreFields()) {
              DataField nextField = nextRec.nextField();
              outRec.storeField(recDef, nextField);
            } // End of fields in Next Rec
            outRec.storeField(recDef, TAG, tag);
            addRecord (outRec);
            recordsLoaded++;
          }
          endOfTags = (tag.length() == 0);
          tagsIndex++;
        } // End while more tags to explode
      } // End if next rec not null
    } // End while more input data
    inData.close();
  }
  
  /** 
     Loads this DataSet from another DataSource implementation.
     The passed data source provides the record definition, as well as
     all of its records.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  private void load (DataSource inData) 
      throws IOException {
    dataParent = inData.getDataParent();
    recDef = inData.getRecDef();
    initialize();
    recordsLoaded = 0;
    while (! inData.isAtEnd()) {
      DataRecord nextRec = inData.nextRecordIn();
      if (nextRec != null) {
        addRecord (nextRec);
        recordsLoaded++;
      }
    }
    inData.close();
  }

  public int getRecordsLoaded() {
    return recordsLoaded;
  }
  
  /**
     Common initialization code for all constructors.
   */
  private void initialize () {
    dict = recDef.getDict();
    records = FXCollections.observableArrayList();
    startWithFirstRecord();
    dataSetNumber++;
    fileId = "DataSet" + String.valueOf (dataSetNumber);
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }
  
  /**
     Adds a subsequent DataSource to an existing DataSet, merging the two
     RecordDefinitions into a new combined one, with an unlimited
     number of records.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public void merge(DataSource inData) 
      throws IOException {
    merge (inData, -1);
  }

  /**
     Adds a subsequent DataSource to an existing DataSet, merging the two
     RecordDefinitions into a new combined one, and specifying a maximum
     number of records to be loaded.
    
     @param inData Data source that allows data records to be read. 
     
     @param maxRecs Maximum number of records to be loaded.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public void merge (DataSource inData, int maxRecs) 
      throws IOException {
      
    Column nextColumn;
    DataField nextField;
    int mergedColumnIndex;
    int columnIndex;
    int recordsIn = 0;
    recordsLoaded = 0;
    
    inData.openForInput();
    RecordDefinition inRecDef = inData.getRecDef();
    recDef.merge (inRecDef);
    
    // add new records to this data set
    while ((! inData.isAtEnd())
        && ((maxRecs < 0) || (recordsIn < maxRecs))) {
      DataRecord inRec = inData.nextRecordIn();
      recordsIn++;
      if (inRec != null) {
        DataRecord nextRec = new DataRecord();
        // Iterator columns = recDef.iterator();
        columnIndex = 0;
        
        // build record
        while (columnIndex < recDef.getNumberOfFields()) {
          nextColumn = recDef.getColumn(columnIndex);
          mergedColumnIndex = nextColumn.getMergedColumn();
          if (mergedColumnIndex < 0) {
            // nextField = new DataField (recDef, columnIndex, "");
            nextRec.addField (recDef, "");
          } 
          else {
            // nextField = inRec.getField(mergedColumnIndex);
            nextRec.addField (recDef, 
                inRec.getField(mergedColumnIndex).getData());
          }
          // nextRec.addField (recDef, nextField.getData());
          columnIndex++;
        } // end while more columns 
        
        nextRec.calculate();
        this.addRecord (nextRec);
        recordsLoaded++;
      } // end if good inRec
    } // end while more new data records
    
    inData.close();
  } // end merge method
  
  /**
     Adds a subsequent DataSource to an existing DataSet, using the
     record definition for the existing DataSet as the definition
     for the new one as well.
    
     @param inData Data source that allows data records to be read.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public void mergeSame (DataSource inData) 
      throws IOException {
    mergeSame (inData, -1);
  }
  
  /**
     Adds a subsequent DataSource to an existing DataSet, using the
     record definition for the existing DataSet as the definition
     for the new one as well.
    
     @param inData Data source that allows data records to be read. 
     
     @param maxRecs Maximum number of records to be loaded.
    
     @throws IOException If the passed data source experiences an i/o error.
   */
  public void mergeSame (DataSource inData, int maxRecs) 
      throws IOException {
    
    inData.openForInput(recDef);
    int recordsIn = 0;
    recordsLoaded = 0;
    
    // add new records to this data set
    while (! inData.isAtEnd()) {
      DataRecord nextRec = inData.nextRecordIn();
      if (nextRec != null) {
        this.addRecord (nextRec);
        recordsIn++;
        recordsLoaded++;
      } // end if good record
    } // end while more data
    inData.close();
  } // end mergeSame method
  
  /**
     Sets a sequence specification and sorts the data set's records
     into the specified sequence.
    
     @param seqSpec Desired sort sequence.
   */
  public void setSequence (SequenceSpec seqSpec) {
    this.seqSpec = seqSpec;
    boolean sorted = false;
    int passes = 0;
    DataRecord reci, recj;
    while (! sorted) {
      sorted = true;
      for (int i = 0, j = 1; j < records.size(); i++, j++) {
        reci = (DataRecord)records.get (i);
        recj = (DataRecord)records.get (j);
        if (reci.compareTo(recj, seqSpec) > 0) {
          sorted = false;
          records.set(i, recj);
          records.set(j, reci);
        } // end of comparison
      } // end of one loop through Vector
      passes++;
    } // end of sorting operation
  } // end of setSequence method
  
  /**
     Looks for records with equal keys and combines them if they meet
     the specified criteria.
    
     @return count of number of records successfully combined.
     @param  precedence Indicator of whether records merged earlier or later 
                        take precedence. <ul> <li>
                        +1 = Later records override earlier <li>
                        -1 = Earlier records override later <li>
                         0 = No overrides. </ul>
     @param  maxAllowed The maximum return value allowed as a result of the 
                        combination of the two records' fields. <ul> <li>
                        0 = No data loss (both fields equal or one field empty) <li>
                        1 = One record may override the other, accoroding
                            to precedence rules <li>
                        2 = Fields may be combined, where that option is allowed
                            by data dictionary. <li>
                        3 = Fields do not have the same name (probably a 
                            program logic error) </ul>
     @param  minNoLoss  If maxAllowed permits data to be overwritten,
                        then this parameter specifies the minimum number
                        of fields that must be without data loss.
   */
  public int combine (int precedence, int maxAllowed, int minNoLoss) {
    int combineCount = 0;
    if (seqSpec != null) {
      DataRecord reci, recj;
      boolean combined;
      int i = 0;
      int j = 1;
      while (j < records.size()) {
        reci = (DataRecord)records.get (i);
        recj = (DataRecord)records.get (j);
        combined = false;
        if (reci.compareTo (recj, seqSpec) == 0) {
          combined = reci.combine (recj, recDef, precedence, maxAllowed, minNoLoss);
          if (combined) {
            recj = (DataRecord)records.remove (j);
            combineCount++;
          } // end if combined
        } // end if equal keys
        if (! combined) {
          i++; 
          j++;
        }
      } // end of while loop through Vector
    } // end of if seqSpec exists
    return combineCount;
  } // end of combine method
  
  /**
     Sets a data filter to be used to select desired output records.
    
     @param inputFilter Desired output filter. 
   */
  public void setInputFilter (DataFilter inputFilter) {
    this.inputFilter = inputFilter;
  }
  
  /**
     Opens the data store for output.
    
     @param  recDef A record definition to use.
   */
  public void openForOutput (RecordDefinition recDef) {
    this.recDef = recDef;
    this.openForOutput();
  }
  
  /**
     Opens the data store for ouput.
   */
  public void openForOutput () {
  }
  
  /**
     Writes the next output data record.
    
     @param  Next data record.
   */
  public void nextRecordOut (DataRecord inRec) {
    addRecord (inRec);
  }
  
  /**
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  /**
     Indicates whether all data should be logged.
    
     @param dataLoging True if all data should be logged,
                       false if only data preceding significant
                       events should be logged.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
  /**
     Overrides the default identifier assigned to this data set.
    
     @param fileId Identifier to be used for this data set.
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
     Allocate a new Logger instance if one has not yet been provided.
   */
  private void ensureLog () {
    if (log == null) {
      log = new Logger (new LogOutput());
    }
  }
  
  /**
     Adds a new record to the set. If a sequence spec has been
     assigned, then the record will be added in the proper
     sequence. If no sequence spec has been provided, then the
     new record will be added at the end of the set.
    
     @param inRec New data record to be added to the data set.
   */
  public void addRecord (DataRecord inRec) {
    inRec.setRecordSequence (nextRecordSequence++);
    if (seqSpec == null) {
      records.add (inRec);
    } else {
      int i = 0;
      while ((i < records.size()) 
        && (inRec.compareTo((DataRecord)records.get(i), seqSpec) >= 0)) {
        i++;
      }
      if (i < records.size()) {
        records.add(i, inRec);
      } else {
        records.add (inRec);
      }
      lastRecordNumber = i;
    } // end of seqSpec processing
    checkMemory();
  } // end method addRecord
  
  public void add (DataRecord inRec) {
    records.add(inRec);
    checkMemory();
  }
  
  public void add (int atIndex, DataRecord inRec) {
    records.add(atIndex, inRec);
    checkMemory();
  }
  
  private void checkMemory() {
    if (! userWarnedOnMemory) {
      Runtime rt = Runtime.getRuntime();
      if (rt.freeMemory() < LOW_MEMORY_THRESHOLD) {
        rt.gc();
        if (rt.freeMemory() < LOW_MEMORY_THRESHOLD) {
          log.recordEvent (LogEvent.MEDIUM, 
            "Available memory is dangerously low",
            true);
          userWarnedOnMemory = true;
        } // end if final check of memory still indicates it is low
      } // end if initial check of memory indicates it is low
    } // end if user not yet warned on low memory
  }
    
  
  public void removeRecord (int recordNumber) 
  		throws ArrayIndexOutOfBoundsException {
  	if ((recordNumber < 0)
  	  || (recordNumber >= records.size())) {
  	  throw new ArrayIndexOutOfBoundsException ();
  	}
    records.remove (recordNumber);
  }
  
  /** 
     Returns this object as some kind of String.
    
     @return Concatenation of all the records in the set,
             separated by line feeds.
   */
  public String toString () {
    StringBuffer recordBuf = new StringBuffer ();
    for (int i = 0; i < records.size (); i++) {
      if (i > 0) {
        recordBuf.append (GlobalConstants.LINE_FEED_STRING);
      }
      recordBuf.append (records.get (i));
    }
    return recordBuf.toString ();
  }
  
  /**
     Prepares the data set to return records
     one at a time.
    
     @param inDict Provided for consistency with DataSource,
                   but not really used or needed.
   */
  public void openForInput (DataDictionary inDict) {
    startWithFirstRecord();
  }
  
  /**
     Prepares the data set to return records
     one at a time.
    
     @param inRecDef Provided for consistency with DataSource,
                     but not really used or needed.
   */
  public void openForInput (RecordDefinition inRecDef) {
    startWithFirstRecord();
  }
  
  /**
     Prepares the data set to return records
     one at a time.
   */
  public void openForInput () {
    startWithFirstRecord();
  }
  
  /**
     Frees up resources associated with open.
   */
  public void close() {
    startWithFirstRecord();
  }
  
  /**
     Indicates whether the last record has already been returned.
    
     @return True if the last record has already been returned.
   */
  public boolean isAtEnd () {
    return (! hasMoreRecords());
  }
    
  /**
     Indicates whether there are more records to return.
    
     @return True if there are more records yet to return.
   */  
  public boolean hasMoreRecords () {
    return ((recordNumber) < records.size());
  }
  
  /** 
     Return the first record in the set, and set up to
     return subsequent records with nextRecordIn.
    
     @return First record in the set.
   */
  public DataRecord firstRecord () {
    startWithFirstRecord ();
    return nextRecordIn ();
  }
  
  /**
     Sets up the internal index recordNumber so that the
     next call to nextRecordIn will return the first
     record in the set.
   */
  public void startWithFirstRecord () {
    recordNumber = 0;
    findNextEligibleRecord ();
    lastRecordNumber = -1;
  }
  
  /**
     Returns the next data record from the set.
    
     @return Next data record
   */
  public DataRecord nextRecordIn () {
    DataRecord nextRec;
    if (hasMoreRecords()) {
      nextRec = (DataRecord)records.get (recordNumber);
    } 
    else {
      nextRec = null;
    }
    if (dataLogging) {
      ensureLog();
      logData.setData (nextRec.toString());
      logData.setSequenceNumber (recordNumber);
      log.nextLine (logData);
    }
    lastRecordNumber = recordNumber;
    recordNumber++;
    findNextEligibleRecord ();
    return nextRec;
  }
  
  /**
     Finds next eligible record, using input data filter.
   */
  private void findNextEligibleRecord () {
    if ((inputFilter != null) && (hasMoreRecords())) {
      boolean eligible = false;
      DataRecord possibleRec;
      do {
        possibleRec = (DataRecord)records.get (recordNumber);
        eligible = inputFilter.selects(possibleRec);
        if (! eligible) {
          recordNumber++;
        }
      } while ((hasMoreRecords()) && (! eligible));
    } // end if inputFilter
  } // end findNextEligibleRecord method
  
  /**
     Returns a data record at a particular location in the set.
    
     @return Data record at specified location.
    
     @param recordNumber The location of the record to be retrieved.
    
     @throws ArrayIndexOutOfBoundsException If the passed record
             number does not refer to a valid record.
   */
  public DataRecord getRecord (int recordNumber)
  		throws ArrayIndexOutOfBoundsException {
  	if ((recordNumber < 0)
  	  || (recordNumber >= records.size())) {
  	  throw new ArrayIndexOutOfBoundsException ();
  	}
  	this.lastRecordNumber = recordNumber;
  	this.recordNumber = recordNumber++;
  	return records.get (lastRecordNumber);
  }
  
  public DataRecord get (int recordNumber)
      throws ArrayIndexOutOfBoundsException {
    return records.get(recordNumber);
  }
  
  /**
     Returns the record definition used by this data source.
    
     @return Record definition used by this data source.
   */
  public RecordDefinition getRecDef() {
    return recDef;
  }
  
  /**
     Returns a Vector containing the names of all the fields
     stored in the data set.
    
     @return Proper names of all the fields.
   */
  public ArrayList getNames() {
    return recDef.getNames();
  }
  
  /**
     Returns the record number of the last data record returned.
    
     @return Record number of the last data record returned.
   */
  public int getLastRecordNumber() {
    return lastRecordNumber;
  }
  
  /**
     Returns the record number of the next data record to be returned.
    
     @return Record number of the next data record returned.
   */
  public int getRecordNumber() {
    return recordNumber;
  }
  
  /**
     Return the number of fields in each record.
    
     @return Number of fields stored in each record.
   */
   
  public int getNumberOfFields () {
    return recDef.getNumberOfFields();
  }
  
  /**
     Returns number of records stored in data set.
    
     @return Number of records stored internally.
   */
  public int getNumberOfRecords () {
    return records.size();
  }
  
  public int size() {
    return records.size();
  }
  
  /**
   Returns the list of records, as an Observable Array List. 
  
   @return The complete list of Data Records, stored as a JavaFX
           Observable Array List. 
  */
  public ObservableList<DataRecord> getList() {
    return records;
  }
  
} // end class DataSet
