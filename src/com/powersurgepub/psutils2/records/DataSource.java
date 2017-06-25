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

  import com.powersurgepub.psutils2.logging.*;
  import java.io.*;

/**
   A source of DataRecord objects. <p>
   
 */
public interface DataSource {
    
  /**
     Opens the reader for input.
    
     @param inDict A data dictionary to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (DataDictionary inDict)
      throws IOException;
      
  /**
     Opens the reader for input.
    
     @param inRecDef A record definition to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (RecordDefinition inRecDef)
      throws IOException;
  
  /**
     Opens the reader for input.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput ()
      throws IOException;
  
  /**
     Returns the next input data record.
    
     @return Next data record.
    
     @throws IOException If reading from a source that might generate
                         these.
   */
  public DataRecord nextRecordIn ()
      throws IOException;
    
  /**
     Returns the record definition for the reader.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef ();
  
  /**
     Returns the sequential record number of the last record returned.
    
     @return Sequential record number of the last record returned via 
             nextRecordIn, where 1 identifies the first record.
   */
  public int getRecordNumber ();
  
  /**
     Returns the reader as some kind of string.
    
     @return String identification of the reader.
   */
  public String toString ();

  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd();
  
  /**
     Closes the reader.
    
     @throws IOException If there is trouble closing the file.
   */
  public void close () 
      throws IOException ;
    
  /**
     Sets a log to be used by the reader to record events.
    
     @param  log A logger object to use.
   */
  public void setLog (Logger log);
  
  /**
     Indicates whether all data records are to be logged.
    
     @param  dataLogging True if all data records are to be logged.
   */
  public void setDataLogging (boolean dataLogging);
  
  /**
     Sets a file ID to be used to identify this reader in the log.
    
     @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId);
  
  /**
     Sets the maximum directory explosion depth.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth);
  
  /**
     Retrieves the path to the parent folder of the original source file (if any).
    
     @return Path to the parent folder of the original source file (if any).
   */
  public String getDataParent ();
}
