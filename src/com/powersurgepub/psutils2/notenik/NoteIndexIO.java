/*
 * Copyright 2016 - 2016 Herb Bowie
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

package com.powersurgepub.psutils2.notenik;

	import com.powersurgepub.psutils2.index.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;

  import java.io.*;
  import java.util.*;

/**
 
 @author Herb Bowie
 */
public class NoteIndexIO 
    implements DataSource {
  
  public static final String INITIAL_LETTER = "Initial Letter";
  public static final String TERM = "Term";
  public static final String LOWER_CASE_TERM = "Lower Case Term";
  public static final String TERM_LINK = "Term Link";
  public static final String PAGE = "Page";
  public static final String ANCHOR = "Anchor";
  
  private             NoteIO                noteIO = null;
  private             IndexCollection       index = new IndexCollection();
  private             Iterator              iterator = null;
  private             IndexTerm             term = null;
  private             int                   refIndex = 0;
  private             RecordDefinition      recDef = new RecordDefinition();
  private             int                   recordNumber = 0;
  private             String                fileId = "Note Index";
  private             Logger                log;
  private             boolean               dataLogging = false;
  
  public NoteIndexIO(File fileOrFolder, int inType) {
    noteIO = new NoteIO(fileOrFolder, inType);
    
    recDef.addColumn(INITIAL_LETTER);
    recDef.addColumn(LOWER_CASE_TERM);
    recDef.addColumn(TERM);
    recDef.addColumn(TERM_LINK);
    recDef.addColumn(PAGE);
    recDef.addColumn(ANCHOR);
    
  }
  
  /**
     Opens the reader for input.
    
     @param inDict A data dictionary to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (DataDictionary inDict)
      throws IOException {
    openForInput();
  }
      
  /**
     Opens the reader for input.
    
     @param inRecDef A record definition to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (RecordDefinition inRecDef)
      throws IOException {
    openForInput();
  }
  
  /**
     Opens the reader for input.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput ()
      throws IOException {
    
    noteIO.openForInput();
    NoteParms noteParms = noteIO.getNoteParms();
    Note note = null;
    while (! noteIO.isAtEnd()) {
      note = noteIO.readNextNote();
      if (note != null
          && note.hasTitle()
          && note.hasIndex()) {
        index.add(note.getTitle(), note.getIndex());
      }
    }
    noteIO.close();
    iterator = index.iterator();
    term = null;
    recordNumber = 0;
  }
  
  /**
     Returns the next input data record.
    
     @return Next data record.
    
     @throws IOException If reading from a source that might generate
                         these.
   */
  public DataRecord nextRecordIn ()
      throws IOException {
    int maxRefIndex = -1;
    if (term == null) {
      Map.Entry mapEntry = (Map.Entry)iterator.next();
      term = (IndexTerm)mapEntry.getValue();
      refIndex = 0;
    } else {
      refIndex++;
    }
    if (term != null) {
      maxRefIndex = term.getRefSize() - 1;
    }
    
    DataRecord rec = new DataRecord();
    if (term == null) {
      return null;
    } else {
      StringBuilder initialChar = new StringBuilder();
      initialChar.append(term.getTerm().toUpperCase().charAt(0));
      rec.storeField(recDef, INITIAL_LETTER, initialChar.toString());
      rec.storeField(recDef, LOWER_CASE_TERM, term.getTermLower());
      rec.storeField(recDef, TERM, term.getTerm());
      rec.storeField(recDef, TERM_LINK, term.getLink());
      if (refIndex < term.getRefSize()) {
        IndexPageRef ref = term.getRef(refIndex);
        rec.storeField(recDef, PAGE, ref.getPage());
        rec.storeField(recDef, ANCHOR, ref.getAnchor());
      } else {
        rec.storeField(recDef, PAGE, "");
        rec.storeField(recDef, ANCHOR, "");
      }
      
      if (refIndex >= maxRefIndex) {
        term = null;
      }
      
      recordNumber++;
      return rec;
    } 
  }
    
  /**
     Returns the record definition for the reader.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef () {
    return recDef;
  }
  
  /**
     Returns the sequential record number of the last record returned.
    
     @return Sequential record number of the last record returned via 
             nextRecordIn, where 1 identifies the first record.
   */
  public int getRecordNumber () {
    return recordNumber;
  }
  
  /**
     Returns the reader as some kind of string.
    
     @return String identification of the reader.
   */
  public String toString () {
    return noteIO.toString();
  }

  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return term == null && (! iterator.hasNext());
  }
  
  /**
     Closes the reader.
    
     @throws IOException If there is trouble closing the file.
   */
  public void close () 
      throws IOException {
    
  }
    
  /**
     Sets a log to be used by the reader to record events.
    
     @param  log A logger object to use.
   */
  public void setLog (Logger log) {
    this.log = log;
    noteIO.setLog(log);
  }
  
  /**
     Indicates whether all data records are to be logged.
    
     @param  dataLogging True if all data records are to be logged.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
    noteIO.setDataLogging(dataLogging);
  }
  
  /**
     Sets a file ID to be used to identify this reader in the log.
    
     @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    noteIO.setFileId(fileId);
  }
  
  /**
     Sets the maximum directory explosion depth.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {
    noteIO.setMaxDepth(maxDepth);
  }
  
  /**
     Retrieves the path to the parent folder of the original source file (if any).
    
     @return Path to the parent folder of the original source file (if any).
   */
  public String getDataParent () {
    return noteIO.getDataParent();
  }
  
}

