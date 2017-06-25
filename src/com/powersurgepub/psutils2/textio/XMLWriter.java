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

package com.powersurgepub.psutils2.textio;

  import com.powersurgepub.psutils2.strings.*;

  import java.io.*;
  import java.net.*;

/**

@author hbowie
 */
public class XMLWriter {
  
  private               String                xmlVersion = "1.0";
  private               String                xmlEncoding = "UTF-8";
  
  private               String                xmlNameSpacePrefixWithColon = "";
  
  private               TextWriter            writer = null;
  
  private               boolean               openForOutput = false;
  private               boolean               nodeStartPending = false;
  
  private               boolean               xmlEntityEncoding = false;
  private               StringConverter       xmlEntityEncoder = null;
  
  private               boolean               displayData = false;
  
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
  public XMLWriter (String inPath, String inFileName) {
    writer = new TextWriter (new File (inPath, inFileName));
  }
  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name.
   */
  public XMLWriter (String inFileName) {
    writer = new TextWriter (new File (inFileName));
  }
  
  /**
     A constructor that accepts two parameters: a File object
     representing the path to the file, and a String containing the file
     name itself.
     
     @param inPathFile  A path to the directory containing the file
     
     @param inFileName  The file name itself (without path info).
   */
  public XMLWriter (File inPathFile, String inFileName) {
    writer = new TextWriter (new File (inPathFile, inFileName));
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.</p>
     
     @param inFile  The text file itself.
   */
  public XMLWriter (File inFile) {
    writer = new TextWriter (inFile);
  }
  
  /**
     A constructor that accepts a URL
     representing the file.</p>
     
     @param url  A URL pointing to the text file itself.
   */
  public XMLWriter (URL url) {
    writer = new TextWriter (url);
  }
  
  /**
   A constructor that accepts a TextLineWriter.
   */
  public XMLWriter (TextLineWriter writer) {
    this.writer = new TextWriter (writer);
  }
  
  /**
     Opens the XML file for output.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForOutput () 
      throws IOException {
    
    writer.openForOutput ();
    openForOutput = true;
    writer.setIndenting(true);
    writer.setIndentPerLevel(2);
    boolean ok = writer.writeLine (
      "<?xml version=\"" 
      + xmlVersion
      + "\" encoding=\""
      + xmlEncoding
      + "\"?>");

    if (! ok) {
      throw new IOException("Unsuccessful write");
    }
  }
  
  public TextLineWriter getTextLineWriter() {
    
    return writer;
  }
  
  public void writeComment (String comment) 
      throws IOException{
    
    boolean ok = writer.writeLine ("<!-- " + comment + " -->");
    if (! ok) {
      throw new IOException("Unsuccessful write");
    }
  }
  
  public void setXmlEntityEncoding (boolean xmlEntityEncoding) {
    this.xmlEntityEncoding = xmlEntityEncoding;
    if (xmlEntityEncoding) {
      xmlEntityEncoder = new StringConverter();
      xmlEntityEncoder.addXML();
      // System.out.println("XMLWriter.setXMLEntityEncoding StringConverter  before setForward = "
      //     + xmlEntityEncoder.toString());
      xmlEntityEncoder.setForward(true);
      // System.out.println("XMLWriter.setXMLEntityEncoding StringConverter  after setForward = "
      //     + xmlEntityEncoder.toString());
      xmlEntityEncoder.prepare();
      // System.out.println("XMLWriter.setXMLEntityEncoding StringConverter  after prepare = "
      //     + xmlEntityEncoder.toString());
    }
  }
  
  public StringConverter getXMLEntityEncoder() {
    return xmlEntityEncoder;
  }
  
  public boolean getXMLEntityEncoding () {
    return xmlEntityEncoding;
  }
  
  public void writeNode(String name, String data) 
      throws IOException {
    boolean ok = false;
    startNodeOut(name, false);
    /* if (name.equalsIgnoreCase("title")) {
      displayData = true;
    } */
    writeData(data, false);
    // displayData = false;
    endNodeOut(name, false);
  }
  
  public void writeAttribute(String name, String data) 
      throws IOException {
    boolean ok = false;
    startNodeOut(name, true);
    writeData(data, true);
    endNodeOut(name, true);
  }
  
  public void startNodeOut(String name) 
      throws IOException {
    startNodeOut (name, false);
  }
  
  public void startNodeOut(String name, boolean isAttribute) 
      throws IOException {
    boolean ok = false;
    if (isAttribute) {
      ok = writer.write (" " + name);
    } else {
      finishNodeStartIfPending();
      ok = writer.ensureNewLine();
      ok = writer.write (
        "<" +
        xmlNameSpacePrefixWithColon +
        name);
      writer.moreIndent();
      nodeStartPending = true;
    }

    if (! ok) {
      throw new IOException("Unsuccessful write");
    }
  }
  
  public void writeData(String data) 
      throws IOException {
    writeData (data, false);
  }
  
  public void writeData(String data, boolean isAttribute) 
      throws IOException {
    boolean ok = true;
    if (isAttribute) {
      ok = writer.write ("=\"" + data + "\"");
    } else {
      finishNodeStartIfPending();
      /* if (displayData) {
        System.out.println("XMLWriter.writeData  in = " + data);
      } */
      if (xmlEntityEncoding) {
        String converted = xmlEntityEncoder.convert (data);
        /* if (displayData) {
          System.out.println("XMLWriter.writeData out = " + converted);
        } */
        // System.out.println("XMLWriter.writeData with entity encoding: "
        //     + converted);
        ok = writer.write(converted);
      } else {
        // System.out.println("XMLWriter.writeData without entity encoding: "
        //     + data);
        ok = writer.write(data);
      }
    }
    if (! ok) {
      throw new IOException("Unsuccessful write");
    }
  }
  
  public void endNodeOut(String name) 
      throws IOException {
    endNodeOut(name, false);
  }
  
  public void endNodeOut(String name, boolean isAttribute) 
      throws IOException {
    boolean ok = true;
    if (isAttribute) {
      
    } else {
      if (nodeStartPending) {
        ok = writer.write(" /");
        nodeStartPending = false;
        writer.lessIndent();
      } else {
        ok = writer.ensureNewLine();
        writer.lessIndent();
        ok = writer.write("</" + xmlNameSpacePrefixWithColon + name);
      }
      writer.write(">");
      ok = writer.newLine();
    }
    if (! ok) {
      throw new IOException("Unsuccessful write");
    }
  }
  
  private void finishNodeStartIfPending() 
      throws IOException {
    boolean ok = true;
    if (nodeStartPending) {
      ok = writer.write(">");
      ok = writer.newLine();
      nodeStartPending = false;
    }
    if (! ok) {
      throw new IOException("Unsuccessful close");
    }
  }
  
  public void close() 
      throws IOException {
    boolean ok = false;
    if (openForOutput) {
      ok = writer.close();
      openForOutput = false;
    }
    if (! ok) {
      throw new IOException("Unsuccessful close");
    }
  }
  
  public boolean isOpenForOutput() {
    return openForOutput;
  }
  
  public String getDestination() {
    if (writer == null) {
      return "null";
    } else {
      return writer.getDestination();
    }
  }
  
}
