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
  
/**
   A generic lookup table. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing 
           (<a href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
   @version 
    2003/04/21 - Added optional path specifier to constructor.
 */

  import com.powersurgepub.psutils2.logging.*;
  import java.io.*;
  import java.util.*;

public class LookupTable {
  
  protected String          tdfPath;
  protected String          tdfName;
  protected String          tdfKey;
  
	/** DataSet containing the lookup table, read from the data source. */
  protected DataSet					lookupTable;
  
  /** Map to the HashMap entries. */
  protected Map							keyMap;
  
  /** Should we consider case of the key? */
  protected boolean					caseConsiderate;
  
  /** Log used to record events. */
  protected    Logger             log;

	/**
	   Constructor.
    
     @param tdfPath Path to the file, if not in the program's directory.
     
     @param tdfName Name of the file containing the lookup table.
    
     @param tdfKey  Name of the key field to be used in the lookup.
    
     @param caseConsiderate Should lookup respect the case (upper or lower)
                            of letters found in the lookup key?
	 */
	public LookupTable (
      String tdfPath, 
      String tdfName, 
      String tdfKey, 
      boolean caseConsiderate) 
        throws IOException {
    this.tdfPath = tdfPath;
    this.tdfName = tdfName;
    this.tdfKey = tdfKey;
    this.caseConsiderate = caseConsiderate;
    keyMap = new HashMap();
	}
   
  public void load () {
    keyMap = new HashMap(lookupTable.getNumberOfRecords());
    lookupTable.openForInput();
    DataRecord nextRec;
    String key;
    while (lookupTable.hasMoreRecords()) {
      nextRec = lookupTable.nextRecordIn();
      if (caseConsiderate) {
        key = nextRec.getFieldData(tdfKey);
      }
      else {
        key = nextRec.getFieldData(tdfKey).toLowerCase();
      }
      keyMap.put (key, nextRec);
    }
  }
  
  /**
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
	/**
	   Lookup the desired field from the specified record.
    
     @return String value if key found, otherwise null.
    
     @param searchKey The key value that we are looking for.
    
     @param fieldName The name of the field we want returned.
   */

	public String get (String searchKey, String fieldName) {
    String key;
    if (caseConsiderate) {
      key = searchKey;
    }
    else {
      key = searchKey.toLowerCase();
    }
    DataRecord foundRec = (DataRecord)keyMap.get(key);
    if (foundRec == null) {
      return null;
    } else {
      return foundRec.getFieldData (fieldName);
    }
	} 	 
	
	/*
	   Returns the object in string form.
	  
	   @return object formatted as a string
	 */
	public String toString() {
    return ("TabDelimLookup: " + lookupTable.toString());
	}
  
}
