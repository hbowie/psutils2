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

package com.powersurgepub.psutils2.txbio;

	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.strings.*;
	import com.powersurgepub.psutils2.textio.*;

  import java.io.*;
  import java.net.*;

/**
 Writes psdata DataRecords to an XML file. 

 @author Herb Bowie
 */
public class XMLRecordWriter
    implements DataStore {
  
  public  static final  String                XML = "XML";
  public  static final  String                FILE_EXT = "xml";
  
  private               String                xmlVersion = "1.0";
  private               String                xmlEncoding = "UTF-8";
  
  private               String                docTag = "file";
  
  private               String                recTag = "record1";
  
  private               int                   recDefNumber = 0;
  
  private               boolean               recTagOpen = false;
  
  private               XMLWriter             writer = null;
  
  /** Path to the parent of the output file (if any). */
  private               String                dataParent = "";
  
  /** Data dictionary to be used for the file. */
  private               DataDictionary        dict;
  
  /** Record definition to be used for subsequent output operations. */
  private               RecordDefinition      recDef = null;
  
  /** Sequential number identifying last record written. */
  private               int                   recordNumber = 0;
  
  /** Log to record events. */
  private               Logger                log;
  
  /** Do we want to log all data, or only data preceding significant events? */
  private               boolean               dataLogging = false;
  
  /** Identifier for this file (to be printed in the log as a source ID). */
  private               String                fileId = "XMLRecordWriter";
  
  /*========================================================================
   *
   * Constructors
   *
   *========================================================================*/
  
  
  /**
     A constructor that accepts a path and file name.
    
     @param inPath      A path to the directory containing the file.
    
     @param inFileName  The file name itself (without path info).
   */
  public XMLRecordWriter (String inPath, String inFileName) {
    writer = new XMLWriter (new File (inPath, inFileName));
  }
  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name.
   */
  public XMLRecordWriter (String inFileName) {
    writer = new XMLWriter (new File (inFileName));
  }
  
  /**
     A constructor that accepts two parameters: a File object
     representing the path to the file, and a String containing the file
     name itself.
     
     @param inPathFile  A path to the directory containing the file
     
     @param inFileName  The file name itself (without path info).
   */
  public XMLRecordWriter (File inPathFile, String inFileName) {
    writer = new XMLWriter (new File (inPathFile, inFileName));
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.</p>
     
     @param inFile  The text file itself.
   */
  public XMLRecordWriter (File inFile) {
    writer = new XMLWriter (inFile);
  }
  
  /**
     A constructor that accepts a URL
     representing the file.</p>
     
     @param url  A URL pointing to the text file itself.
   */
  public XMLRecordWriter (URL url) {
    writer = new XMLWriter (url);
  }
  
  /**
   A constructor that accepts a TextLineWriter.
   */
  public XMLRecordWriter (TextLineWriter writer) {
    this.writer = new XMLWriter (writer);
  }
  
  /**
   Sets the XML tag to be used to identify and encapsulate the entire document.
   Defaults to 'file'.
  
   @param docTag The XML tag to be used to identify and encapsulate the
                 entire document. 
  */
  public void setDocTag(String docTag) {
    this.docTag = docTag;
  }
  
  /**
     Opens the data store for output.
    
     @param  recDef A record definition to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForOutput (RecordDefinition recDef) 
      throws IOException {
    
    if (! isOpenForOutput()) {
      writer.openForOutput ();
      writer.startNodeOut(docTag);
      recDefNumber = 0;
      recordNumber = 0;
    }
    this.recDef = recDef;
    recDefNumber++;
    recTag = "record" + String.valueOf(recDefNumber);
    dict = recDef.getDict();
  }
  
  /**
     Opens the data store for output.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForOutput () 
      throws IOException {
    
    if (! isOpenForOutput()) {
      writer.openForOutput ();
      writer.startNodeOut(docTag);
      recDefNumber = 0;
      recordNumber = 0;
    }
  }
  
  public void setRecTag(String recTag) {
    this.recTag = recTag;
  }
  
  public void setXmlEntityEncoding (boolean xmlEntityEncoding) {

    if (writer != null) {
      writer.setXmlEntityEncoding(xmlEntityEncoding);
    } 
  }
  
  public boolean getXMLEntityEncoding () {
    if (writer != null) {
      return writer.getXMLEntityEncoding();
    } else {
      return false;
    }
  }
  
  public StringConverter getXMLEntityEncoder() {
    return writer.getXMLEntityEncoder();
  }
  
  /**
     Writes the next output data record.
    
     @param  Next data record.
    
     @throws IOException If writing to a store that might generate
                         these.
   */
  public void nextRecordOut (DataRecord inRec)
      throws IOException {
    writer.startNodeOut(recTag);
    inRec.startWithFirstField();
    DataField nextField = null;
    while (inRec.hasMoreFields()) {
      nextField = inRec.nextField();
      if (nextField != null) {
        writer.writeNode(nextField.getProperName(), nextField.getData());
      }
    }
    writer.endNodeOut(recTag);
  }
    
  /**
     Returns the record definition for the writer.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef () {
    return recDef;
  }
  
  /**
     Returns the sequential record number of the last record written.
    
     @return Sequential record number of the last record written via 
             nextRecordOut, where 1 identifies the first record.
   */
  public int getRecordNumber () {
    return recordNumber;
  }
  
  /**
     Returns the store as some kind of string.
    
     @return String identification of the data store.
   */
  public String toString () {
    if (writer == null) {
      return "";
    } else {
      return writer.getDestination();
    }
  }
  
  /**
     Closes the writer.
    
     @throws IOException If there is trouble closing the file.
   */
  public void close () 
      throws IOException {
    if (isOpenForOutput()) {
      writer.endNodeOut(docTag);
      writer.close();
    }
  }
    
  /**
     Sets a log to be used by the writer to record events.
    
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
     Sets a file ID to be used to identify this writer in the log.
    
     @param  fileId An identifier for this writer.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
  }
  
  /**
     Sets a path to be used to read any associated files.
    
     @param  dataParent A path to be used to read any associated files.
   */
  public void setDataParent (String dataParent) {
    this.dataParent = dataParent;
  }
  
  public boolean isOpenForOutput() {
    return (writer != null
        && writer.isOpenForOutput());
  }
}

