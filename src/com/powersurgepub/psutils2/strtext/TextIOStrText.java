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

package com.powersurgepub.psutils2.strtext;

	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.textio.*;
	import com.powersurgepub.psutils2.txbio.*;
	import com.powersurgepub.psutils2.txbmodel.*;

  import java.io.*;
  import java.net.*;
  import java.util.*;

/**
 This is an I/O module designed to read structured text. 
 <p>
 This module currently supports the following input interfaces.

 <ul>
   <li>DataSource -- Assume this is a "flat" file that can be formatted
       as a series of records, each containing the same fields. </li>
   <li>ElementParser --  </li>
 </ul>
 <p>
 No output interfaces are currently supported. 

 @author Herb Bowie
 */
public class TextIOStrText 
    extends TextIOModule
    implements  
      DataSource, 
      ElementParser {
  
  public  static final  String              STRUCTURED_TEXT = "Text - Structured";
  public  static final  String              PREFERRED_FILE_EXTENSION = "txt";
  
  /** The identifier for this reader. */
  private               String              fileId = STRUCTURED_TEXT;
  
  /** The logger to use to log events. */    
  private               Logger              log = null;
  
  /** The data dictionary to be used by this record. */
  private               DataDictionary      dict = null;
  
  /** The record definition to be used by this record. */
  private               RecordDefinition    recDef = null;
  
  /** Data Record currently being built. */
  private               DataRecord					dataRec;
  
  /** The reader used to read text lines from the data source. */
  private               TextLineReader      lineReader = null;
  
  private               boolean             atEnd = false;
  
  private               StrTextField           field = null;
  
  /** The last line read. */
  private               StrTextLine            line = null;
  
  private               int                 dataFieldIndention = 0;
  
  /** Pointer to a particular record within the input. */
  private               int                 recordNumber;
  
  private               String              lineSeparator 
      = System.getProperty("line.separator");
  
  private               ElementHandler      handler = null;
  
  private               String              streamTag = "";
  private               int                 streamTagLevel = -1;
  private               String              documentTag = "";
  private               int                 documentTagLevel = -1;
  private               String              markupTag = "";
  private               boolean             useMarkdown = false;
  
  private               boolean             markupOpen = false;
  
  private               List<StrTextField> 
      fields = new ArrayList<StrTextField>();
  
  public TextIOStrText () {
    initialize();
  }
  
  /**
     Constructs a structured text I/O module given a path defining the directory
     to be read.
    
     @param  inPath Directory path to be read.
   */
  public TextIOStrText (String inPath) {
    lineReader = new FileLineReader(inPath);
    initialize();
  }

  /**
     Constructs a structured text I/O module given a file object 
     defining the directory to be read.
    
     @param  inPathFile Directory path to be read.
   */
  public TextIOStrText (File inPathFile) {
    lineReader = new FileLineReader(inPathFile);
    initialize();
  }
  
  public TextIOStrText (URL inURL) {
    lineReader = new FileLineReader(inURL);
    initialize();
  }
  
  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "directory".
   */
  private void initialize () {
    fileId = STRUCTURED_TEXT;
  }
  
  public void setSource(String inPath) {
    lineReader = new FileLineReader(inPath);
  }
  
  public void setSource(File inPathFile) {
    lineReader = new FileLineReader(inPathFile);
  }
  
  public void setSource(URL inURL) {
    lineReader = new FileLineReader(inURL);
  }
  
  public void setLineSeparator(String lineSeparator) {
    this.lineSeparator = lineSeparator;
  }
  
  public void registerTypes (List types) {
    
    TextIOType type = new TextIOType (
        STRUCTURED_TEXT,       // Label
        this,       // Text I/O Module
        true,       // Good for input?
        false,      // Good for output?
        PREFERRED_FILE_EXTENSION);     // File extension
    types.add (type);
    
  }
  
  public boolean load  (TextTree tree, URL url, TextIOType type, String parm) {
    this.tree = tree;
    boolean ok = true;
    ensureLog();
    lineReader = new FileLineReader(url);
    ok = lineReader.open();
    /* ok = createParser();
    if (ok) {
      String urlString = "";
      try {
        urlString = url.toString();
        currentNode = tree.getTextRoot();
        parser.parse (urlString);
      }
      catch (SAXException saxe) {
        ok = false;
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            "Encountered SAX error while reading XML file " + urlString 
            + saxe.toString(),
            false);   
      } 
      catch (MalformedURLException e) {
        ok = false;
        Logger.getShared().recordEvent (LogEvent.MAJOR,
            "WisdomXMLIO parseXMLFile malformed URL derived from " 
            + file.toString(),
            false);
      }
      catch (java.io.IOException ioe) {
        ok = false;  
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            "Encountered I/O error while reading XML file " + urlString 
            + ioe.toString(),
            false);   
      }
    } // end if ok */
    ok = lineReader.close();
    return ok;
  }
  
  /**
   Set the tag that defines the beginning of an entire stream. When writing
   structured text, this tag will be skipped altogether. 
  
   @param streamTag The tag to be interpreted as representing an entire stream.
  */
  public void setStreamTag(String streamTag) {
    this.streamTag = streamTag;
  }
  
  /**
   Set the tag that defines the beginning of a document. When writing
   structured text, this tag will be skipped, and three dashes ('---') will
   be written instead. 
  
   @param documentTag The tag to be interpreted as representing an entire
                      document.
  */
  public void setDocumentTag(String documentTag) {
    this.documentTag = documentTag;
  }
  
  public void setMarkupTag(String markupTag) {
    this.markupTag = markupTag;
  }
  
  public void setUseMarkdown(boolean useMarkdown) {
    this.useMarkdown = useMarkdown;
  }
  
  /**
   Parse the previously designated structured text source and pass the 
   parsed elements to the supplied element handler. 
  
   @param handler The element handler that will consume the elements. 
  */
  public void parse (ElementHandler handler) 
      throws IOException {
    
    this.handler = handler;
    
    atEnd = true;
    
    if (lineReader == null) {
      throw new IOException();
    }

    boolean ok = lineReader.open();
    if (! ok) {
      throw new IOException();
    }
    
    atEnd = false;
    ensureLog();
    
    streamTagLevel = -1;
    documentTagLevel = -1;
    
    if (streamTagPresent()) {
      streamTagLevel++;
      documentTagLevel++;
    }
    if (documentTagPresent()) {
      documentTagLevel++;
    }
    
    markupOpen = false;

    handler.startDocument();
    field = new StrTextField();
    if (streamTagPresent()) {
      field = new StrTextField();
      field.setName(streamTag);
      startElement(field);
    }
    
    nextLineIn();
    while (! atEnd) {
      parseLine();
    }
    
    harvestData();
          
    if (documentTagPresent()) {
      endElementsAbove (streamTagLevel);
    }
    if (streamTagPresent()) {
      endElement(streamTagLevel);
    }
    handler.endDocument();
    lineReader.close();
  }
  
  /**
   Process the last line read from the data source, and then read another.
  */
  private void parseLine() {
    
    if (atEnd) {
      // no need to do anything
    }
    else
          
    if (line.isNewDocument()) {
      harvestData();
      endElementsAbove (streamTagLevel);
      if (documentTagPresent()) {
        field = new StrTextField();
        field.setName(documentTag);
        startElement(field);
      }
    }
    else
      
    if ((field.isBlockValue()
          && line.getIndention() > dataFieldIndention) 
      || (field.isBlockValue()
          && line.isBlank())
      || (line.hasNoName() && line.hasValue())) {
      field.anotherLine(line, lineSeparator);
    }
    else
      
    if (line.isBlank()) {
      // Ignore if not part of block value
    }
    else
      
    if (line.hasName()) {
      harvestData();
      while (getHighestIndention() >= line.getIndention()
          && getHighestFieldIndex() > streamTagLevel
          && getHighestFieldIndex() > documentTagLevel) {
        endElement(getHighestFieldIndex());
      }
      field.firstLine(line);
      startElement(field);
    }
    
    nextLineIn();
  }
  
  private boolean streamTagPresent() {
    return (streamTag != null && streamTag.length() > 0);
  }
  
  private boolean documentTagPresent() {
    return (documentTag != null && documentTag.length() > 0);
  }
  
  private void startElementByName(String name) {
    StrTextField namedField = new StrTextField();
    namedField.setName(name);
    startElement(namedField);
  }
  
  private void startElement(StrTextField fieldToStart) {
    startElement(fieldToStart.getName());
    fields.add(fieldToStart);
  }
  
  private void startElement(String name) {
    handler.startElement(name, false);
    if (name.equals(markupTag)) {
      markupOpen = true;
    }
  }
  
  /**
   If we have a data value saved in the current field, then pass it along
   to the handler before we start building the next field. 
   */
  private void harvestData() {
    if (field != null
        && field.hasName()
        && field.hasValue()) {
      if (markupOpen && useMarkdown) {
        handler.data(field.getValue(), useMarkdown);
      } else {
        handler.data(field.getValue());
      }
    }
    field = new StrTextField();
  }
  
  /**
   End the element at this level and, by implication, any open elements 
   at higher levels.
  
   @param level The level to be ended, along with higher levels.
  */
  private void endElement(int level) {
    endElementsTo(level);
  }
  
  private void endElementByName (String name) {
    boolean found = false;
    int i = getHighestFieldIndex();
    while (i >= 0 && (! found)) {
      StrTextField checkField = fields.get(i);
      if (name.equals(checkField.getName())) {
        found = true;
      } else {
        i--;
      }
    } // end looking for name
    if (found) {
      endElementsTo(i);
    }
  }
  
  /**
   End elements down to and including the specified level.
  
   @param level The level to be ended, after ending higher elements as well. 
  */
  private void endElementsTo(int level) {
    while (fields.size() > (level)) {
      endElementAt(getHighestFieldIndex());
    }
  }
  
  /**
   End elements at a higher level than the one specified. 
  
   @param level The level above which elements should be ended.  
  */
  private void endElementsAbove (int level) {
    while (getHighestFieldIndex() > level) {
      endElementAt(getHighestFieldIndex());
    }
  }
  
  private void endElementAt (int level) {
    StrTextField endField = fields.get(level);
    endElement(endField.getName());
    fields.remove(level);
  }
  
  private void endElement(String name) {
    handler.endElement(name);
    if (name.equals(markupTag)) {
      markupOpen = false;
    }
  }
  
  private int getHighestIndention() {
    if (fields.size() > 0) {
      return (getHighestField().getIndention());
    } else {
      return -1;
    }
  }
  
  private String getHighestName() {
    if (fields.size() > 0) {
      return (getHighestField().getName());
    } else {
      return "";
    }
  }
  
  private StrTextField getHighestField() {
    if (fields.size() > 0) {
      return (fields.get(getHighestFieldIndex()));
    } else {
      return null;
    }
  }
  
  private int getHighestFieldIndex() {
    return (fields.size() - 1);
  }
  
  /**
     Opens the reader for input.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput ()
      throws IOException {
    openForInput (new DataDictionary());
  }
  
  /**
     Opens the reader for input.
    
     @param inRecDef A record definition to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (RecordDefinition inRecDef)
      throws IOException {
    openForInput (inRecDef.getDict());
  }
  
  /**
     Opens the reader for input.
    
     @param inDict A data dictionary to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (DataDictionary inDict)
      throws IOException {
    
    atEnd = false;
    boolean ok = lineReader.open();
    if (! ok) {
      throw new IOException();
    }
    ensureLog();
    dict = inDict;
    recDef = new RecordDefinition (dict);
    dataRec = new DataRecord();

    recordNumber = 0;
    
    nextLineIn();
    if (line.isNewDocument()) {
      nextLineIn();
    }
  }
  
  /**
     Returns the next input data record.
    
     @return Next data record.
    
     @throws IOException If reading from a source that might generate
                         these.
   */
  public DataRecord nextRecordIn ()
      throws IOException {
    
    dataRec = new DataRecord();
    
    nextFieldIn();
    int fieldsStored = 0;
    if (! isAtEnd()) {
      do {
        if (field.hasName() || field.hasValue()) {
          storeField (field.getName(), field.getValue());
          fieldsStored++;
        }
        nextFieldIn();
      } while ((! isAtEnd())
          && (! line.isNewDocument())
          && (line.getIndention() >= dataFieldIndention));
    }

    if (line.isNewDocument()) {
      nextLineIn();
    }
    
    if (fieldsStored == 0) {
      return null;
    } else {
      recordNumber++;
      return dataRec;
    }
  }
  
  /**
   Populate the next Structured Text Field instance. 
  
  */
  private void nextFieldIn() {
    
    field = new StrTextField();
    
    // Make sure we have a line with a field name to start with
    while ((! isAtEnd()
        && (! line.isNewDocument()))
        && (line.hasNoName() 
        // || line.hasNoValue()
        )) {
      nextLineIn();
    }
    
    // Now build the field, if we're not at the end of the file/document
    if (! isAtEnd()  && (! line.isNewDocument())) {
      field.firstLine(line);
      dataFieldIndention = line.getIndention();
      boolean goodLine = true;
      do {
        nextLineIn();
        goodLine = true;
        if (isAtEnd()
            || line.isNewDocument()
            || (! field.isBlockValue())
            || line.getIndention() <= dataFieldIndention) {
          goodLine = false;
        } else {
          field.anotherLine(line, lineSeparator);
        }
      } while (goodLine);
    } // end if we found the first line for the field

  }
  
  /**
   Read the next line from the input source, and perform some preliminary 
   evaluation of the line, including counting its indention. Note that a leading
   tab character is counted as equivalent to two spaces. 
  */
  private void nextLineIn() {
    
    String textLine = lineReader.readLine();
    line = new StrTextLine (textLine);
    if ((line.toString().equals("") && lineReader.isAtEnd()) 
        || line.isEndOfFile()) {
      atEnd = true;
    }
    
  } // end nextLineIn method

  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return atEnd;
  }
  
  /**
     Adds a field to the record being built. 
    
     @return The column number assigned to the field.
     @param  fieldName The name of the field.
     @param  value     The data value assigned to the field. 
   */
  private int storeField (String fieldName, String value) {
    
    int column = recDef.getColumnNumber (fieldName);
    
    if (column < 0) {
      column = recDef.addColumn (fieldName);
    }
    column = dataRec.storeField (recDef, fieldName, value);
    return column;
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
    return "";
  }
  
  /**
     Closes the reader.
    
     @throws IOException If there is trouble closing the file.
   */
  public void close () 
      throws IOException {
    boolean ok = lineReader.close();
    if (! ok) {
      throw new IOException();
    }
  }
  
  /**
     Ensures that a log is available, by allocating a new one if
     one has not already been supplied.
   */
  protected void ensureLog () {
    if (log == null) {
      setLog (Logger.getShared());
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
    
  }
  
  /**
     Sets a file ID to be used to identify this reader in the log.
    
     @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId) {
    
  }
  
  /**
     Sets the maximum directory explosion depth.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {
    
  }
  
  /**
     Retrieves the path to the original source file (if any).
    
     @return Path to the original source file (if any).
   */
  public String getDataParent () {
    return "";
  }
  
  public boolean store 
    (TextTree tree, TextWriter writer, TextIOType type, 
       boolean epub, String epubSite) {
    return false;
  }
  
}
