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

package com.powersurgepub.psutils2.records;

  import com.powersurgepub.psutils2.logging.*;
  import java.io.IOException;

/**
   A storage repository for DataRecord objects. <p>
   
   This code is copyright (c) 1999-2000 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
    2000/05/21 - Initial creation.
      </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
   @version 
    2003/07/29 - Added setDataParent method.
 */
public interface DataStore {
    
  /**
     Opens the data store for output.
    
     @param  recDef A record definition to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForOutput (RecordDefinition recDef)
      throws IOException;
  
  /**
     Opens the data store for ouput.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForOutput ()
      throws IOException;
  
  /**
     Writes the next output data record.
    
     @param  Next data record.
    
     @throws IOException If writing to a store that might generate
                         these.
   */
  public void nextRecordOut (DataRecord inRec)
      throws IOException;
    
  /**
     Returns the record definition for the writer.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef ();
  
  /**
     Returns the sequential record number of the last record written.
    
     @return Sequential record number of the last record written via 
             nextRecordOut, where 1 identifies the first record.
   */
  public int getRecordNumber ();
  
  /**
     Returns the store as some kind of string.
    
     @return String identification of the data store.
   */
  public String toString ();
  
  /**
     Closes the writer.
    
     @throws IOException If there is trouble closing the file.
   */
  public void close () 
      throws IOException ;
    
  /**
     Sets a log to be used by the writer to record events.
    
     @param  log A logger object to use.
   */
  public void setLog (Logger log);
  
  /**
     Indicates whether all data records are to be logged.
    
     @param  dataLogging True if all data records are to be logged.
   */
  public void setDataLogging (boolean dataLogging);
  
  /**
     Sets a file ID to be used to identify this writer in the log.
    
     @param  fileId An identifier for this writer.
   */
  public void setFileId (String fileId);
  
  /**
     Sets a path to be used to read any associated files.
    
     @param  dataParent A path to be used to read any associated files.
   */
  public void setDataParent (String dataParent);
}
