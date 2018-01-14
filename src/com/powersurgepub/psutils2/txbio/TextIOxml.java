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

package com.powersurgepub.psutils2.txbio;

	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.textio.*;
	import com.powersurgepub.psutils2.txbmodel.*;

  import java.io.*;
  import java.net.*;
  import java.util.*;

  import javafx.scene.control.*;

  import org.xml.sax.*;
  import org.xml.sax.helpers.*;

/**
  A class for providing input services for text trees and nodes
  from XML. 
 */
public class TextIOxml 
    extends TextIOModule
    implements NodeWriter {
  
  public  static final  String                XML = "XML";
  public  static final  String                FILE_EXT = "xml";
  
  private               String                xmlVersion = "1.0";
  private               String                xmlEncoding = "UTF-8";
  private               String                xmlNameSpacePrefixWithColon = "";   
  
  private               File                  file;
  private               XMLReader             parser;
  private               TextWriter            writer;
  private               boolean               openForOutput = false;
  private               boolean               nodeStartPending = false;
  
  public TextIOxml () {
    
  }
  
  public void registerTypes (List types) {
    
    TextIOType type = new TextIOType (XML,
        this, true, false, "xml");
    type.addExtension("opml");
    types.add (type);
    
  }
  
  public String getFileExt() {
    return FILE_EXT;
  }
  
  public void setFile(File file) {
    this.file = file;
  }
  
  /* -----------------------------------------------------------------
   This section of the class has methods for reading nodes from the
   file. 
   ------------------------------------------------------------------- */
  
  public boolean load (TextTree tree, URL url, TextIOType type, String parm) {
    this.tree = tree;
    boolean ok = true;
    ok = createParser();
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
    } // end if ok
    return ok;
  }

  /**
   Create XML Parser.
   */
  private boolean createParser () {
    
    boolean ok = true;
    try {
      parser = XMLReaderFactory.createXMLReader();
    } catch (SAXException e) {
      Logger.getShared().recordEvent (LogEvent.MINOR, 
          "Generic SAX Parser Not Found",
          false);
      try {
        parser = XMLReaderFactory.createXMLReader
            ("org.apache.xerces.parsers.SAXParser");
      } catch (SAXException eex) {
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            "Xerces SAX Parser Not Found",
            false);
        ok = false;
      } // end catch specific sax parser not found
    } // end catch generic sax parser exception
    if (ok) {
      parser.setContentHandler (this);
    }
    return ok;
  } // end method createParser
  
  
  /**
   Handle the beginning of a new element when parsing XML.
   */
  public void startElement (
      String namespaceURI,
      String localName,
      String qualifiedName,
      Attributes attributes) {
    
      TreeItem<TextData> nextNode = tree.createNode(localName, "");
      currentNode.getChildren().add(nextNode);
      currentNode = nextNode;
      for (int i = 0; i < attributes.getLength(); i++) {
        TreeItem<TextData> attrNode = tree.createNode(
            attributes.getLocalName (i), 
            attributes.getValue(i));
        attrNode.getValue().setAttribute(true);
        currentNode.getChildren().add(attrNode);
      } 
      /* while (fieldNumber < record.getNumberOfFields()) {
        DataField field = record.getField (fieldNumber);
        if (field != null) {
          TextNode fieldNode = new TextNode(tree);
          fieldNode.setType (field.getProperName());
          fieldNode.setText (field.getData());
          recordNode.add (fieldNode);
        } // end if we have a good field
        fieldNumber++;
      } // end while more fields in record */
    
    /* debugging
    if (attributes.getLength() > 0) {
      System.out.println ("WisdomXMLIO.startElement"
          + " namespaceURI = " + namespaceURI
          + " localName = " + localName
          + " qualifiedName = " + qualifiedName); 
      for (int i = 0; i < attributes.getLength(); i++) {
        System.out.println ("  attribute"
            + " localName = " + attributes.getLocalName (i)
            + " type = " + attributes.getType (i)
            + " value =" + attributes.getValue(i));
      } 
    } */
    
  } // end method
  
  /*
  private void harvestAttributes (Attributes attributes, DataField field) {
    for (int i = 0; i < attributes.getLength(); i++) {
      String name = attributes.getLocalName (i);
      DataFieldDefinition def = dict.getDef (name);
      if (def == null) {
        def = new DataFieldDefinition (name);
        dict.putDef (def);
      }
      System.out.println ("  Attribute " + name + " = " + attributes.getValue (i));
      DataField attr = new DataField (def, attributes.getValue (i));
      field.addField (attr);
    }
  }
   */
  
  public void characters (char [] ch, int start, int length) {
    StringBuffer chars = new StringBuffer();
    chars.append(ch, start, length);
    boolean allWhitespace = true;
    int i = 0;
    while(allWhitespace && i < chars.length()) {
      allWhitespace = Character.isWhitespace(chars.charAt(i));
      i++;
    }
    // System.out.println ("TextIOxml characters length = " + String.valueOf(length));
    // System.out.println ("  chars = [" + chars.toString() + "]");
    // System.out.println ("  all white space? " + String.valueOf(allWhitespace));

    if (allWhitespace) {
      // skip it
    } else {
      if (currentNode != null) {
        if (currentNode.getValue().hasChildTags()) {
          // System.out.println ("  current node of type " + currentNode.getType()
          //     + " has child tags");
          TreeItem<TextData> nextNode = tree.createNode(TextType.NAKED_TEXT, "");
          currentNode.getChildren().add (nextNode);
          // currentNode = nextNode;
          nextNode.getValue().characters (ch, start, length);
        } else {
          currentNode.getValue().characters (ch, start, length);
        }
      } // end if current node exists
    } // end if not all white space
  } // end method characters
  
  public void endElement (
      String namespaceURI,
      String localName,
      String qualifiedName) {

    // System.out.println ("TextIOxml endElement localName = " + localName);
    if (currentNode != null) {
      // System.out.println ("  Current Node initially of type: " + currentNode.getType());
      if (currentNode.getValue().getType().equalsIgnoreCase(localName)) {
        TreeItem<TextData> parentNode = currentNode.getParent();
        if (parentNode != null) {
          currentNode = parentNode;
          // System.out.println ("  Current Node now of type " + currentNode.getType());
        }
      }
    }
  } // end method
  
  /* -----------------------------------------------------------------
   This section of the class has methods for writing nodes to the
   file. 
   ------------------------------------------------------------------- */
  
  public boolean store (TextTree tree, TextWriter writer, TextIOType type,
      boolean epub, String epubSite) {
    this.tree = tree;
    boolean ok = false;
    return false;
  }
  
  public boolean openForOutput() {
    boolean ok = true;
    writer = new TextWriter(file);
    ok = writer.openForOutput();
    if (ok) {
      openForOutput = true;
      writer.setIndenting(true);
      writer.setIndentPerLevel(2);
      ok = writer.writeLine ("<?xml version=\""
        + xmlVersion
        + "\" encoding=\""
        + xmlEncoding
        + "\"?>");
    }
    return ok;
  }
  
  public TextLineWriter getTextLineWriter() {
    if (writer == null) {
      return null;
    } else {
      return writer;
    }
  }
  
  public boolean writeComment (String comment) {
    boolean ok = false;
    ok = writer.writeLine ("<!-- " + comment + " -->");
    return ok;
  }
  
  public boolean writeNode(String name, String data) {
    boolean ok = false;
    ok = startNodeOut(name, false);
    ok = writeData(name, false);
    ok = endNodeOut(name, false);
    return ok;
  }
  
  public boolean writeAttribute(String name, String data) {
    boolean ok = false;
    ok = startNodeOut(name, false);
    ok = writeData(name, false);
    ok = endNodeOut(name, false);
    return ok;
  }
  
  public boolean startNodeOut(String name) {
    return startNodeOut (name, false);
  }
  
  public boolean startNodeOut(String name, boolean isAttribute) {
    boolean ok = false;
    if (isAttribute) {
      writer.write (" " + name);
    } else {
      finishNodeStartIfPending();
      writer.ensureNewLine();
      ok = writer.write (
        "<" +
        xmlNameSpacePrefixWithColon +
        name);
      writer.moreIndent();
      nodeStartPending = true;
    }

    return ok;
  }
  
  public boolean writeData(String data) {
    return writeData (data, false);
  }
  
  public boolean writeData(String data, boolean isAttribute) {
    boolean ok = true;
    if (isAttribute) {
      ok = writer.write ("=\"" + data + "\"");
    } else {
      ok = finishNodeStartIfPending();
      ok = writer.write(data);
    }
    return ok;
  }
  
  public boolean endNodeOut(String name) {
    return endNodeOut(name, false);
  }
  
  public boolean endNodeOut(String name, boolean isAttribute) {
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
    return ok;
  }
  
  public boolean close() {
    boolean ok = false;
    if (openForOutput) {
      ok = writer.close();
      openForOutput = false;
    }
    return ok;
  }
  
  private boolean finishNodeStartIfPending() {
    boolean ok = true;
    ok = finishNodeStartIfPending();
    if (nodeStartPending) {
      writer.write(">");
      writer.newLine();
      nodeStartPending = false;
    }
    return ok;
  }

}
