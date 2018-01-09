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

package com.powersurgepub.psutils2.txmin;

  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.strings.*;

  import java.io.*;
  import java.net.*;
  import java.util.*;

  import org.xml.sax.*;
  import org.xml.sax.helpers.*;

/**
 Class to read an info.plist file and extract key fields. 

 @author Herb Bowie
 */
public class InfoPlistReader 
    extends DefaultHandler {
  
  public static final String INFO_PLIST = "info.plist";
  
  public static final String PLIST = "plist";
  public static final String KEY = "key";
  public static final String STRING = "string";
  public static final String SHORT_VERSION = "CFBundleShortVersionString";
  public static final String CATEGORY = "LSApplicationCategoryType";
  public static final String MIN_SYS_VERSION = "LSMinimumSystemVersion";
  public static final String COPYRIGHT = "NSHumanReadableCopyright";
  
  private     XMLReader           parser;
  private     boolean             parserOK = false;
  
  private     File                infoPlist = null;
  
  // Array of character strings being built as characters are received from parser
  private     ArrayList           chars = new ArrayList();
  
  private     int                 elementLevel = -1;
  
  private     String              key = "";
  
  private     String              version = "";
  private     String              category = "";
  private     String              minSysVersion = "";
  private     String              copyright = "";
  
  public InfoPlistReader () {
    createParser();
  }
  
  public boolean readInfoPlistFile (File inFile) {
    
    version = "";
    category = "";
    minSysVersion = "";
    copyright = "";
    
    if (! parserOK) {
      return false;
    }
    
    if (inFile.getName().endsWith(".app")) {
      File contents = new File (inFile, "Contents");
      infoPlist = new File (contents, INFO_PLIST);
    } else {
      infoPlist = inFile;
    }
    
    if (infoPlist == null
        || (! infoPlist.exists())
        || (! infoPlist.canRead())
        || (! infoPlist.getName().equals(INFO_PLIST))) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM, 
          "info.plist file for " + inFile.toString() + " not accessible", 
          false);
      return false;
    }

    chars = new ArrayList();
    elementLevel = -1;
    key = "";
    
    String infoPlistURI = inFile.toString();
    try {
      String webPage = inFile.toURI().toURL().toString();
      infoPlistURI = StringUtils.tweakAnyLink(webPage, false, false, false, "");
    } catch (MalformedURLException e) {
      // do nothing
    }

    boolean parseOK = false;
    try {
      // System.out.println("Parsing " + infoPlistURI);
      parser.parse (infoPlistURI);
      parseOK = true;
    } 
    catch (SAXException saxe) {
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            "Encountered SAX error while reading XML file " + infoPlistURI 
            + " " + saxe.toString(),
            false);   
    } 
    catch (java.io.IOException ioe) {
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            "Could not verify program version using PAD file at " + infoPlistURI 
            + " due to I/O Exception " + ioe.getMessage(),
            false);   
    } // end catch
    
    return parseOK;

  } // end method readPadFile
  
  /**
   Create XML Parser to read info.plist file.
   */
  private void createParser () {
    
    parserOK = true;
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
        parserOK = false;
      } // end catch specific sax parser not found
    } // end catch generic sax parser exception
    if (parserOK) {
      parser.setContentHandler (this);
    }
  } // end method createParser
  
  public void startElement (
      String namespaceURI,
      String localName,
      String qualifiedName,
      Attributes attributes) {
    
    // System.out.println("  startElement " + localName);
    elementLevel++;
    StringBuffer str = new StringBuffer();
    storeField (elementLevel, str);
    /**
    if (localName.equals (PLIST)) {
      elementLevel = 0;
      StringBuffer str = new StringBuffer();
      storeField (elementLevel, str);
    }
    else
    if (localName.equals (SHORT_VERSION)) {
      elementLevel = 0;
      StringBuffer str = new StringBuffer();
      storeField (elementLevel, str);
    }
    */
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
    StringBuffer xmlchars = new StringBuffer();
    xmlchars.append (ch, start, length);
    // System.out.println ("    Characters: " + xmlchars.toString());
    // System.out.println ("    Element Level: " + String.valueOf(elementLevel));
    if (elementLevel >= 0 
        && elementLevel < chars.size()) {
      StringBuffer str = (StringBuffer)chars.get (elementLevel);
      boolean lastCharWhiteSpace = false;
      if (str.length() < 1
          || Character.isWhitespace (str.charAt (str.length() - 1))) {
        lastCharWhiteSpace = true;
      }
      char c;
      boolean charWhiteSpace;
      for (int i = start; i < start + length; i++) {
        c = ch [i];
        charWhiteSpace = Character.isWhitespace (c);
        if (charWhiteSpace) {
          if (lastCharWhiteSpace) {
            // do nothing
          } else {
            lastCharWhiteSpace = true;
            str.append (" ");
          }
        } else {
          str.append (c);
          lastCharWhiteSpace = false;
        }
      } // end for each passed character
      // System.out.println("    Resulting string: " + str.toString());
      // str.append (ch, start, length);
    } // end if we are at a valid element level
  } // end method characters
  
  public void ignorableWhitespace (char [] ch, int start, int length) {
    
  }
  
  public void endElement (
      String namespaceURI,
      String localName,
      String qualifiedName) {
    
    StringBuffer str;
    if (elementLevel >= 0) {
      if (chars.size() > elementLevel) {
        str = (StringBuffer)chars.get (elementLevel);
      } else {
        str = new StringBuffer();
      }
      if (str.length() > 0
          && str.charAt (str.length() - 1) == ' ') {
        str.deleteCharAt (str.length() - 1);
      }
      // System.out.println("    endElement " + localName);
      // System.out.println("    str: " + str.toString());
      if (localName.equals (KEY)) {
        // StringBuffer str = new StringBuffer();
        // storeField (elementLevel, str);
        key = str.toString();
        // elementLevel--;
      } 
      else
      if (localName.equals(STRING)) {
        if (key.equals(SHORT_VERSION)) {
          version = str.toString();
        }
        else
        if (key.equals(CATEGORY)) {
          category = str.toString();
        }
        else 
        if (key.equals(MIN_SYS_VERSION)) {
          minSysVersion = str.toString();
        }
        else
        if (key.equals(COPYRIGHT)) {
          copyright = str.toString();
        }
      }
      elementLevel--;
    } // end if we are within a wisdom element
  } // end method
  
  private void storeField (int level, StringBuffer str) {
    if (chars.size() > level) {
      chars.set (level, str);
    } else {
      chars.add (level, str);
    }
  } // end method
  
  public String getVersion() {
    return version;
  }
  
  public String getCategory() {
    return category;
  }
  
  public String getMinSysVersion() {
    return minSysVersion;
  }
  
  public String getCopyright() {
    return copyright;
  }

}
