/*
 * Copyright 2011 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.publish;

	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.logging.*;

  import java.io.*;
  import java.util.*;

  import javax.xml.transform.*;
  import javax.xml.transform.stream.*;

  import org.xml.sax.*;
  import org.xml.sax.helpers.*;

/**
 *
 * This class will read an XML file containing a series of XSL transformations,
 * and attempt to perform each transformation. 
 */
public class XMLTransformer 
      extends DefaultHandler {
  
  public static final String      TRANSFORMERS       = "transformers";
  public static final String      TRANSFORMER        = "transformer";
  public static final String      XMLFILE            = "xml";
  public static final String      XSLFILE            = "xsl";
  public static final String      OUTPUT             = "out";
  public static final String      XMLEXT             = "xmlext";
  public static final String      OUTEXT             = "outext";
  
  private     boolean             ok = true;
  
  /** XML parser to read file of transformation parameters. */
  private     XMLReader           parser;
  
  /** Level within XML structure. */
  private     int                 elementLevel = -1;
  
  // Array of character strings being built as characters are received from parser
  private     ArrayList           chars = new ArrayList();
  
  public  static final TransformerFactory  factory = TransformerFactory.newInstance();
  
  private     File                root = null;
  
  private     String              xmlFileString = "";     
  private     String              xslFileString = "";
  private     String              outputString  = "";
  private     String              xmlExt        = "xml";
  private     String              outputExt     = "html";
  
  /** 
   Creates a new instance of XMLTransformer.
   */
  public XMLTransformer() {

  }
  
  /**
   Perform a sequence of XSLT transformations.
   */
  public boolean transform (File xmlScriptFile, File root) {
    
    this.root = root;
    ok = true;
    chars = new ArrayList();
    elementLevel = -1;
    
    try {
      parser = XMLReaderFactory.createXMLReader();
    } catch (SAXException e) {
      Logger.sharedRecordEvent (LogEvent.MINOR, 
          "Generic SAX Parser Not Found",
          false);
      try {
        parser = XMLReaderFactory.createXMLReader
            ("org.apache.xerces.parsers.SAXParser");
      } catch (SAXException eex) {
        Logger.sharedRecordEvent (LogEvent.MEDIUM, 
            "Xerces SAX Parser Not Found",
            false);
        ok = false;
      } // end catch specific sax parser not found
    } // end catch generic sax parser exception
    if (ok) {
      parser.setContentHandler (this);
    }
        
    if (ok) {
      try {
        parser.parse (xmlScriptFile.toURI().toString());
      } 
      catch (SAXException saxe) {
          Logger.sharedRecordEvent (LogEvent.MEDIUM, 
              "Encountered SAX error while reading XML file " + xmlScriptFile.toString() 
              + saxe.toString(),
              false);   
          ok = false;
      } 
      catch (java.io.IOException ioe) {
          Logger.sharedRecordEvent (LogEvent.MEDIUM, 
              "Encountered I/O error while reading XML file " + xmlScriptFile.toString() 
              + ioe.toString(),
              false);   
          ok = false;
      }
    }
    return ok;
  }
  
  public void startElement (
      String namespaceURI,
      String localName,
      String qualifiedName,
      Attributes attributes) {
    
    elementLevel++;
    StringBuffer str = new StringBuffer();
    storeField (elementLevel, str);
    
  } // end method
  
  public void characters (char [] ch, int start, int length) {
    StringBuffer xmlchars = new StringBuffer();
    xmlchars.append (ch, start, length);
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
      // str.append (ch, start, length);
    } // end if we are at a valid element level
  } // end method characters
  
  public void ignorableWhitespace (char [] ch, int start, int length) {
    
  }
  
  public void endElement (
      String namespaceURI,
      String localName,
      String qualifiedName) {
    
    if (elementLevel >= 0) {
      StringBuffer str = (StringBuffer)chars.get (elementLevel);
      if (str.length() > 0
          && str.charAt (str.length() - 1) == ' ') {
        str.deleteCharAt (str.length() - 1);
      }
      if (localName.equals (XMLFILE)) {
        xmlFileString = str.toString();
      } 
      else
      if (localName.equals (XMLEXT)) {
        xmlExt = str.toString();
      } 
      else      
      if (localName.equals (XSLFILE)) {
        xslFileString = str.toString();
      } 
      else 
      if (localName.equals (OUTPUT)) {
        outputString = str.toString();
      } 
      else
      if (localName.equals (OUTEXT)) {
        outputExt = str.toString();
      } 
      else
      if (localName.equals (TRANSFORMER)) {
        transform();
      } 
      elementLevel--;
    } // end if level is valid
  } // end method
  
  private void storeField (int level, StringBuffer str) {
    if (chars.size() > level) {
      chars.set (level, str);
    } else {
      chars.add (level, str);
    }
  } // end method
  
  /**
   Perform a transformation, or series of transformations, using the 
   data colleccted from the incoming xml file being read.
   */
  private boolean transform () {
    boolean transformOK = true;
    
    // Build the necessary File objects
    File xslFile = new File (root, xslFileString);
    File xmlFile = new File (root, xmlFileString);
    File outFile = new File (root, outputString);

    // Make sure the input xsl file actually exists
    if (xslFile.exists()) {
      
      // Make sure any referenced output folders are created, if they
      // don't already exist
      String outFileName = outFile.getName();
      int dotPosition = outFileName.indexOf ('.');
      File outFolder = outFile;
      if (dotPosition > 0) {
        outFolder = outFile.getParentFile();
      }
      FileUtils.ensureFolder (outFolder);
      
      // Build the actual xsl transformation engine
      StreamSource style = new StreamSource (xslFile);
      Transformer tf = null;
      try {
        tf = factory.newTransformer (style);
      } catch (TransformerConfigurationException e) {
        transformOK = false;
        Logger.sharedRecordEvent (LogEvent.MEDIUM, 
              "XSLT Transformer could not be configured properly",
              false);
      }
      
      // Make sure we have a good transformer
      if (transformOK) {
        
        // See if input is a single file, or a directory
        if (xmlFile.isDirectory()) {
          
          // look for all xml files in this directory or any sub-directories
          transformOK = transformFolder 
              (tf, xmlFile, outFile, xmlExt, outputExt);
          
        } else {
          
          // process only one xml file
          transformOK = transform (tf, xmlFile, outFile);
          if (transformOK) {
            Logger.sharedRecordEvent (LogEvent.NORMAL, 
                "  XSL Transformation", 
                false);
            Logger.sharedRecordEvent (LogEvent.NORMAL, 
                "    from  " + xmlFile.toString(), 
                false);
            Logger.sharedRecordEvent (LogEvent.NORMAL, 
                "    to    " + outFile.toString(),
                false);
            Logger.sharedRecordEvent (LogEvent.NORMAL, 
                "    using " + xslFile.toString(), 
                false);
          } else {
            Logger.sharedRecordEvent (LogEvent.MINOR, 
              "  Transformation error processing " + xmlFile.toString() +
                " to " + outFile.toString() + 
                " using " + xslFile.toString(), 
                false);         
          }
        }
      } else {
        Logger.sharedRecordEvent (LogEvent.MINOR, 
            "  Could not successfully parse " + xslFile.toString(), false);    
        ok = false;
      }
    }
    
    if (! transformOK) {
      ok = false;
    }
    return transformOK;
  }
  
  /**
   Use an xsl stylesheet to transform a folder full of xml files into 
   corresponding html files, on a 1-for-1 basis.
   */
  public static boolean transformFolder 
      (Transformer tf, File inFolder, File outFolder, 
        String inFileExt, String outFileExt) {
    
    boolean transformOK = true;
    
    //Ensure the output folder exists
    FileUtils.ensureFolder (outFolder);
    
    String[] dirEntry = inFolder.list();
    for (int j = 0; j < dirEntry.length; j++) {
      String entry = dirEntry [j];
      File xmlEntry = new File (inFolder, entry);
      File outEntry = new File (outFolder, entry);
      if (xmlEntry.exists()
          && xmlEntry.canRead()) {
        if (xmlEntry.isDirectory()) {
          transformFolder (tf, xmlEntry, outEntry, inFileExt, outFileExt);
        } else {
          FileName xmlFileName = new FileName (xmlEntry);
          if (xmlFileName.getExt().equals (inFileExt)) {
            FileName outFileName = new FileName (outEntry);
            outEntry = new File (
                outFolder, 
                outFileName.replaceExt(outFileExt));
            transformOK = transform (tf, xmlEntry, outEntry);
          } // end if xml file found
        } // end if not a directory
      } // end if directory entry is readable
    } // end for each directory entry in current directory
      
    return transformOK;
  }
  
  /**
   Use an xsl stylesheet to transform one xml file to an html file.
   */
  public static boolean transform (File xslParam, File xmlParam, File htmlParam) {
    
    boolean transform2OK = true;
    
    // Set up XSL Transformer
    StreamSource style = new StreamSource (xslParam);
    Transformer tf = null;
    try {
      tf = factory.newTransformer (style);
    } catch (TransformerConfigurationException e) {
      transform2OK = false;
      Logger.sharedRecordEvent (LogEvent.MEDIUM, 
            "XSLT Transformer could not be configured properly " + e.toString(),
            false);
    }
    
    if (transform2OK) {
      transform (tf, xmlParam, htmlParam);
    } // end if transformer configured without error
    
    return transform2OK;
  }
  
  public static boolean transform (Transformer tf, File inFile, File outFile) {
    StreamSource source = new StreamSource (inFile);
    StreamResult result = new StreamResult (outFile);
    return transform (tf, source, result);
  }
  
  public static boolean transform (Transformer tf, Source source, Result result) {
    boolean ok = true;
    try {
      tf.transform (source, result);
    } catch (TransformerException e) {
        ok = false;
        Logger.sharedRecordEvent (LogEvent.MEDIUM, 
            source.toString() + " could not be transformed: "
              + e.toString(),
            false);
    }
    return ok;
  }
  
}
