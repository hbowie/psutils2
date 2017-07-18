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
  import com.powersurgepub.psutils2.records.*;

	import java.io.*;
  import java.net.*;

/**
   A source of parsed HTML. The HTML can be read as HTMLTag objects, or as
   DataRecord objects, to obtain all files linked to by this file. <p>
  
   This code is copyright (c) 2003 by Herb Bowie.
   All rights reserved. <p>
  
   Version History: <ul><li>
    2003/05/24 - Originally written.
       </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
   @version 
    2003/05/24 - Originally written.
 */

public class HTMLLinksFile 
  extends HTMLFile 
    implements DataSource {
    
  public  final static String   LINK_FROM 			= "Link From";
  public  final static String		LINK_TYPE 			= "Link Type";
  public  final static String		LINK_TO					= "Link To";
  
  public  final static String   A_HREF_TYPE 		= "ahref";
  public  final static String   LINK_HREF_TYPE 	= "linkhref";
  public  final static String   MAILTO_TYPE 		= "mailto";
  public  final static String   IMG_SRC_TYPE 		= "imgsrc";
  
  /** This file as a URL. */
  private			URL							thisURL;
  
  // Next tag to process
  private			HTMLTag					nextTag;
  
  // Has a link record been built yet?
  private			boolean					linkFound = false;
  
  // File name as extracted from HTML
  private			String					fileName;
  
  /** File name converted to a URL. */
  private			URL							fileURL;
  
  // Type of link
  private			String					linkType;
  
  private			int							count = 0;
	  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name passed as a single String.
   */
  public HTMLLinksFile (String inFileName) {
    super (inFileName);
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.
     
     @param inFile  The text file itself.
   */
  public HTMLLinksFile (File inFile) {
    super (inFile);
  }
  
  /**
     A constructor that accepts a URL pointing to a Web resource.
     
     @param url  The URL of a text file.
   */  
  public HTMLLinksFile (URL url) {
    super (url);
  }
  
  /**
     Performs open operations for this particular HTML interpretation rule. 
     Build record definition. 
   */
  void openWithRule () 
      throws IOException, FileNotFoundException {
    
    thisURL = toURL();
    storeField (LINK_FROM, "");
    storeField (LINK_TYPE, "");
    storeField (LINK_TO, "");
    getNextLine();
		getNextCharacter();
  }

  /**
     Retrieve the next file link.
    
     @return formatted file link or null if end of file.
   */
  public DataRecord nextRecordIn () 
      throws IOException, FileNotFoundException {
    HTMLLink link = nextLinkIn();
    if (link == null) {
      return null;
    } else {
      dataRec = new DataRecord();
      storeField (LINK_FROM, link.getFrom());
      storeField (LINK_TYPE, link.getType());
      storeField (LINK_TO, link.getTo());
      return dataRec;
    } 
  }
  
  /**
     Retrieve the next HTML link.
    
     @return HTMLLink object or null if end of file.
   */
  public HTMLLink nextLinkIn () 
      throws IOException, FileNotFoundException {
    linkFound = false;
    do {
      processTag();
    } while ((! isAtEnd()) 
        && (! linkFound)
        && (nextTag != null));
    if (linkFound) {
      if (linkType.equals (A_HREF_TYPE)
          && fileName.startsWith ("mailto:")) {
        // fileName = fileName.substring(7);
        linkType = MAILTO_TYPE;
      }
      else {
        try {
          fileURL = new URL (thisURL, fileName); 
          fileName = fileURL.toExternalForm();
          int colonPos = fileName.indexOf(':');
          if (colonPos >= 0) {
            // fileName = fileName.substring(colonPos + 1);
          } 
        } catch (MalformedURLException e) {
          System.out.println ("malformed URL with " + thisURL.toString() + ", " + fileName);
        }
      }
      HTMLLink link = new HTMLLink (toString(), linkType, fileName);
      return link;
    } else {
      return null;
    }
  }
  
  /**
     Checks the next HTML tag for a file reference.
   */
  private void processTag () {
    try {
      nextTag = readTag();
    } catch (java.io.IOException e) {
      nextTag = null;
    }
    if (nextTag == null) {
      // don't do anything
    } 
    else {  // tag not null
      if (nextTag.getName().equals("a")
          && (! nextTag.isEnding())
          && (nextTag.containsAttribute ("href"))) {
        fileName = nextTag.getAttribute("href").getValue();
        int numSignPos = fileName.indexOf ('#');
        if (numSignPos == 0) {
          // internal to this file -- don't return it
        } else {
          if (numSignPos > 0) {
            fileName = fileName.substring (0, numSignPos);
          }
          linkType = A_HREF_TYPE;
          linkFound = true;
        }
      } // end beginning a tag 
      else
      if (nextTag.getName().equals("link")
          && (! nextTag.isEnding())
          && (nextTag.containsAttribute ("href"))) {
        fileName = nextTag.getAttribute("href").getValue();
        linkType = LINK_HREF_TYPE;
        linkFound = true;
      } // end beginning link tag 
      else
      if (nextTag.getName().equals("img")
          && (! nextTag.isEnding())
          && (nextTag.containsAttribute ("src"))) {
        fileName = nextTag.getAttribute("src").getValue();
        linkType = IMG_SRC_TYPE;
        linkFound = true;
      } // end beginning link tag 
    }
  }
    
} // end class
