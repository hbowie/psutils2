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

  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;

  import java.io.*;
  import java.util.*;
  import org.xml.sax.*;
  import org.xml.sax.helpers.*;

/**
 *
 * @author hbowie
 */
public class XMLParser2
    extends DefaultHandler {
  
  private     String              xmlSourceAsString;
  private     File                xmlSourceAsFile;
  private     int                 maxFolderDepth = 99;
  private     String              fileExtension = "xml";
  private     DirectoryReader     directoryReader;
  private     String              recordName;

  private     XMLReader           parser;
  
  private     DataDictionary      dict;
  private     RecordDefinition    recDef;
  private     DataSet             data;
  private     DataRecord          record;
  private     int                 elementLevel = -1;
  
  private     ArrayList           dataObjects;
  private     ArrayList           chars;
  
  /** Log used to record events. */
  private     Logger              log = Logger.getShared();
  
  /** Creates a new instance of XMLParser2 */
  public XMLParser2() {
    dict = new DataDictionary();
    recDef = new RecordDefinition(dict);
    data = new DataSet(recDef);
    dataObjects = new ArrayList();
    chars = new ArrayList();
  }
  
  /**
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
    data.setLog (log);
  }
  
  public DataSet parse (
      String xmlSourceAsString, 
      int maxFolderDepth,
      String fileExtension,
      String recordName) {
    this.xmlSourceAsString = xmlSourceAsString;
    this.maxFolderDepth = maxFolderDepth;
    this.fileExtension = fileExtension;
    this.recordName = recordName;
    boolean ok = true;

    try {
      parser = XMLReaderFactory.createXMLReader();
    } catch (SAXException e) {
      log.recordEvent (LogEvent.MINOR, 
          "Generic SAX Parser Not Found",
          false);
      try {
        parser = XMLReaderFactory.createXMLReader
            ("org.apache.xerces.parsers.SAXParser");
      } catch (SAXException eex) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Xerces SAX Parser Not Found",
            false);
        ok = false;
      }
    }
    if (ok) {
      parser.setContentHandler (this);
      xmlSourceAsFile = new File (xmlSourceAsString);
      if (! xmlSourceAsFile.exists()) {
        ok = false;
        log.recordEvent (LogEvent.MEDIUM, 
            "XML File or Directory " + xmlSourceAsString + " cannot be found",
            false);
      }
    }
    if (ok) {
      if (! xmlSourceAsFile.canRead()) {
        ok = false;
        log.recordEvent (LogEvent.MEDIUM, 
            "XML File or Directory " + xmlSourceAsString + " cannot be read",
            false);       
      }
    }
    if (ok) {
      if (xmlSourceAsFile.isDirectory()) {
        directoryReader = new DirectoryReader (xmlSourceAsString);
        directoryReader.setLog (log);
        directoryReader.setMaxDepth (maxFolderDepth);
        try {
          directoryReader.openForInput();
          while (! directoryReader.isAtEnd()) {
            File nextFile = directoryReader.nextFileIn();
            if ((nextFile != null) 
                && (nextFile.exists())
                && (nextFile.canRead()) 
                && (nextFile.isFile())) {
              FileName fileName = new FileName (nextFile);
              if ((fileExtension.trim().length() < 1)
                  || (fileName.getExt().equalsIgnoreCase (fileExtension))) {
                parseXMLFile (nextFile);
              } // end if file extension is ok
            } // end if file exists, can be read, etc.
          } // end while more files in specified folder
        } catch (IOException ioe) {
          ok = false;
          log.recordEvent (LogEvent.MEDIUM, 
              "Encountered I/O error while reading XML directory " + xmlSourceAsString,
              false);     
        } // end if caught I/O Error
        directoryReader.close();
      } // end if passed String identified a directory
      else 
      if (xmlSourceAsFile.isFile()) {
        parseXMLFile (xmlSourceAsFile);
      }
    } // end if everything still OK
    return data;
  }  
  
  private void parseXMLFile (File xmlFile) {
    try {
      parser.parse (xmlFile.toURI().toString());
    } 
    catch (SAXException saxe) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Encountered SAX error while reading XML file " + xmlFile.toString() 
            + saxe.toString(),
            false);   
    } 
    catch (java.io.IOException ioe) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Encountered I/O error while reading XML file " + xmlFile.toString() 
            + ioe.toString(),
            false);   
    }
  }
  
  public void startElement (
      String namespaceURI,
      String localName,
      String qualifiedName,
      Attributes attributes) {
    // System.out.println ("startElement " + localName);
    StringBuffer str = new StringBuffer();
    if (recordName.length() < 1) {
      recordName = localName;
    }
    if (recordName.equals (localName)) {
      elementLevel = 0;
      DataRecord record = new DataRecord();
      storeField (elementLevel, record, str);
      harvestAttributes (attributes, record);
    }
    else
    if (elementLevel >= 0) {
      elementLevel++;
      DataField field = new DataField();
      storeField (elementLevel, field, str);
      harvestAttributes (attributes, field);
    }
  } // end method
  
  private void harvestAttributes (Attributes attributes, DataField field) {
    for (int i = 0; i < attributes.getLength(); i++) {
      String name = attributes.getLocalName (i);
      DataFieldDefinition def = dict.getDef (name);
      if (def == null) {
        def = new DataFieldDefinition (name);
        dict.putDef (def);
      }
      // System.out.println ("  Attribute " + name + " = " + attributes.getValue (i));
      DataField attr = new DataField (def, attributes.getValue (i));
      field.addField (attr);
    }
  }
  
  public void characters (char [] ch, int start, int length) {
    StringBuffer str = (StringBuffer)chars.get (elementLevel);
    str.append (ch, start, length);
  }
  
  public void ignorableWhitespace (char [] ch, int start, int length) {
    
  }
  
  public void endElement (
      String namespaceURI,
      String localName,
      String qualifiedName) {
    if (elementLevel >= 0) {
      if (elementLevel == 0) {
        DataRecord record = (DataRecord)dataObjects.get (elementLevel);
        data.addRecord (record);
        elementLevel--;
      } else {
        DataField subField = (DataField)dataObjects.get (elementLevel);
        StringBuffer str = (StringBuffer)chars.get (elementLevel);
        subField.setDataRaw (str.toString());
        elementLevel--;
        DataField superField = (DataField)dataObjects.get (elementLevel);
        superField.addField (subField);
      }
    } // end if we are within the desired record type
  } // end method
  
  private void storeField (int level, DataField field, StringBuffer str) {
    if (dataObjects.size() > level) {
      dataObjects.set (level, field);
      chars.set (level, str);
    } else {
      dataObjects.add (field);
      chars.add (level, str);
    }
  } // end method
  
}
