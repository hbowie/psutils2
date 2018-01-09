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

  import java.io.*;
  import java.util.*;
  import org.xml.sax.*;
  import org.xml.sax.helpers.*;

/**
 * ItunesParser reads an iTunes Library xml file and returns album information.

 * @author hbowie
 */
public class ItunesParser
    extends DefaultHandler
      implements DataSource {
  
  private     String              xmlSourceAsString;
  private     File                xmlSourceAsFile;

  /** Path to the original source file (if any). */
  private	String                  dataParent;

  /** Identifier for this file (to be printed in the log as a source ID). */
  private  String                 fileId;

  /** Data to be sent to the log. */
  private  LogData                logData;

  /** Do we want to log all data, or only data preceding significant events? */
  private  boolean                dataLogging = false;

  /** Sequential number identifying last record read or written. */
  private int                		  recordNumber = 0;

  private     XMLReader           parser;
  
  private     DataDictionary      dict;
  private     RecordDefinition    recDef;
  private     DataRecord          record;

  /** Data Record currently being built. */
  private     DataRecord				  dataRec;
  private     int                 elementLevel = -1;
  
  private     ArrayList           chars;
  
  /** Log used to record events. */
  private     Logger              log = Logger.getShared();

  private     String              name = "";
  private     String              key = "";
  private     String              saveArtist = "";
  private     boolean             unratedTrack = true;
  private     boolean             podcast = false;
  private     ItunesAlbum         album = new ItunesAlbum();
  private     ArrayList           albums = new ArrayList();
  private     int                 albumIndex = 0;

  /**
   Creates a new instance of ItunesParser.

   @param xmlSourceAsString - File to be read.
   */
  public ItunesParser(String xmlSourceAsString) {
    this.xmlSourceAsString = xmlSourceAsString;
    commonConstruction();
  }
  
  /**
   Creates a new instance of ItunesParser.
   
   @param xmlSourceAsFile - File to be read. 
   */
  public ItunesParser(File xmlSourceAsFile) {
    this.xmlSourceAsString = xmlSourceAsFile.toString();
    this.xmlSourceAsFile = xmlSourceAsFile;
    commonConstruction();
  }

  private void commonConstruction() {
    dict = new DataDictionary();
    recDef = new RecordDefinition(dict);
    buildRecDef();
  }
  
  /**
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
  }

  /**
     Opens the XML file for subsequent input.

     @throws IOException If the input file is not found, or if there
                         is trouble reading it.
   */
  public void openForInput ()
      throws IOException {
    this.dict = new DataDictionary();
    openForInput (dict);
  }

  /**
     Opens the XML file for subsequent input.

     @param  recDef Record Definition with dictionary to be used.

     @throws IOException If the input file is not found, or if there
                         is trouble reading it.
   */
  public void openForInput (RecordDefinition recDef)
      throws IOException {
    openForInput (recDef.getDict());
  }

  /**
     Opens the XML file for subsequent input.

     @param  dict Data dictionary to be used by this file.

     @throws IOException If the input file is not found, or if there
                         is trouble reading it.
   */
  public void openForInput (DataDictionary dict)
      throws IOException {
    this.dict = dict;
    dataRec = new DataRecord();
    recDef = new RecordDefinition(this.dict);
    buildRecDef();
    parse(xmlSourceAsString);
    recordNumber = 0;
    albumIndex = 0;
  }

  private void buildRecDef() {
    recDef.addColumn("Album Title");
    recDef.addColumn("Artist");
    recDef.addColumn("Artist Sort Key");
    recDef.addColumn("Genre");
    recDef.addColumn("Year");
    recDef.addColumn("Tracks");
    recDef.addColumn("Unrated?");
    recDef.addColumn("Hi-Fi");
    recDef.addColumn("Lo-Fi Type");
    recDef.addColumn("Podcast");
  }

  /**
     Returns the record definition for the file.

     @return Record definition for this tab-delimited file.
   */
  public RecordDefinition getRecDef () {
    return recDef;
  }

  /**
     Retrieve the next record.

     @return formatted data record.
   */
  public DataRecord nextRecordIn ()
      throws IOException, FileNotFoundException {

    dataRec = new DataRecord();
    if (albumIndex < albums.size()) {
      album = (ItunesAlbum)albums.get(albumIndex);
      dataRec.addField(recDef, album.getTitle());
      dataRec.addField(recDef, album.getArtist());
      dataRec.addField(recDef, album.getSortArtist());
      dataRec.addField(recDef, album.getGenre());
      dataRec.addField(recDef, album.getYear());
      dataRec.addField(recDef, String.valueOf(album.getTracks()));
      dataRec.addField(recDef,
          (album.getUnratedTracks() > 0 ? "Unrated" : ""));
      dataRec.addField(recDef,
          (album.getHiFiTracks() > 0 ? "Hi-Fi" : ""));
      dataRec.addField(recDef, album.getLoFiType());
      dataRec.addField(recDef, 
          album.getNonPodcastTracks() > 0 ? " " : "Podcast");
      albumIndex++;
      recordNumber++;
      return dataRec;
    } else {
      return null;
    }
  }

  /**
     Returns the record number of the last record
     read or written.

     @return Number of last record read or written.
   */
  public int getRecordNumber () {
    return recordNumber;
  }

  public boolean isAtEnd() {
    return (albumIndex >= albums.size());
  }

  public void close() {
    
  }
  
  public void parse (String xmlSourceAsString) {
    this.xmlSourceAsString = xmlSourceAsString;
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
      if (xmlSourceAsFile.isFile()) {
        parseXMLFile (xmlSourceAsFile);
      }
    } // end if everything still OK
  }  
  
  private void parseXMLFile (File xmlFile) {
    // System.out.println ("ItunesParser.parseXMLFile " + xmlSourceAsString);
    dataParent = xmlFile.getParent();
    albums = new ArrayList();
    album = new ItunesAlbum();
    elementLevel = -1;
    chars = new ArrayList();
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
    elementLevel++;
    if (elementLevel >= 0) {
      storeField (elementLevel, str);
    }
  } // end method
  
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
    // System.out.println ("ItunesParser.endElement " + localName);
    if (elementLevel >= 0) {
      StringBuffer str = (StringBuffer)chars.get (elementLevel);
      // System.out.println ("  " + str.toString());
      if (localName.equalsIgnoreCase("key")) {
        key = str.toString();
      }
      else
      if (localName.equalsIgnoreCase("string")) {
        if (key.equalsIgnoreCase("Album")) {
          String title = str.toString();
          if (! title.equals(album.getTitle())) {
            // Search for existing album
            int i = 0;
            boolean found = false;
            boolean lower = false;
            while (i < albums.size() && (! found) && (! lower)) {
              album = (ItunesAlbum)albums.get(i);
              int comp = title.compareTo(album.getTitle());
              if (comp < 0) {
                lower = true;
              }
              else
              if (comp == 0) {
                found = true;
              } else {
                i++;
              }
            } // end while looking for a match or insertion point
            if (i >= albums.size()) {
              album = new ItunesAlbum(title);
              albums.add(album);
            }
            else
            if (lower) {
              album = new ItunesAlbum(title);
              albums.add(i, album);
            }
          }
          album.setArtist(saveArtist);
        }
        else
        if (key.equalsIgnoreCase("Artist")) {
          saveArtist = str.toString();
          unratedTrack = true;
          podcast = false;
        }
        else
        if (key.equalsIgnoreCase("Sort Artist")) {
          album.setSortArtist(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Genre")) {
          album.setGenre(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Kind")) {
          album.countFidelityTracks(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Location")) {
          if (unratedTrack) {
            album.countUnratedTracks();
          }
          if (! podcast) {
            album.countNonPodcastTracks();
          }
        }
      }
      else
      if (localName.equalsIgnoreCase("integer")) {
        if (key.equalsIgnoreCase("Year")) {
          album.setYear(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Rating")) {
          unratedTrack = false;
        }
      }
      else
      if (localName.equalsIgnoreCase("true")) {
        if (key.equalsIgnoreCase("Podcast")) {
          podcast = true;
        }
      }
    } // end if we are within the desired record type
    elementLevel--;
  } // end method
  
  private void storeField (int level, StringBuffer str) {
    if (chars.size() > level) {
      chars.set (level, str);
    } else {
      chars.add (level, str);
    }
  } // end method

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
     Sets maximum number of data levels to expect, and to return.
     Defaults to 3, if not explicitly set via this method.

     @param maxDepth Maximum number of data levels to expec in the XML,
                     and to return in the output.
   */
  public void setMaxDepth (int maxDepth) {
    // this.maxDepth = maxDepth;
  }

  /**
     Sets the file ID to be passed to the Logger.

     @param fileId Used to identify the source of the data being logged.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }

  /**
     Sets the option to log all data off or on.

     @param dataLogging True to send all data read or written to the
                        log file.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
}
