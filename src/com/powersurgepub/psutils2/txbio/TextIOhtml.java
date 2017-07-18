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

/**
  A class for providing input services for text trees and nodes
  from HTML. Note that this class acts as a bridge between the TextBlocs
  data model and the HTML parsing services provided by HTMLFile.
 */
public class TextIOhtml 
    extends TextIOModule {
  
  public  static final String       HTML = "HTML";
  public  static final String       MARKDOWN = "Markdown";
  public  static final String       TEXTILE = "Textile";
  
  public  static final String       MARKDOWN_EXT = "markdown";
  public  static final String       HTML_EXT     = "html";
  
  private     HTMLFile              htmlFile;
  
  private boolean                   preformatted = false;
  
  private boolean                   tocFound = false;
  private int                       tocFrom = 1;
  private int                       tocThrough = 6;
  private boolean                   captureHeading = false;
  private TocEntry                  tocEntry = null;
  private TreeItem<TextData>        headingNode = null;
  
  public TextIOhtml () {
    
  }
  
  public void registerTypes (List types) {
    

    types.add (getHTMLtype());
    types.add (getTextileType());
    types.add (getMarkdownType());
  }
  
  public TextIOType getHTMLtype() {
    TextIOType HTMLtype = new TextIOType (HTML,
        this, true, false, "html");
    HTMLtype.addExtension ("htm");
    HTMLtype.addExtension ("xhtml");
    HTMLtype.addExtension ("xhtm");
    return HTMLtype;
  }
  
  public TextIOType getTextileType() {
    TextIOType textileType = new TextIOType (TEXTILE,
        this, true, false, "textile");
    return textileType;
  }
  
  public TextIOType getMarkdownType() {
    TextIOType markdownType = new TextIOType (MARKDOWN,
        this, true, false, MARKDOWN_EXT);
    markdownType.addExtension("markdown");
    markdownType.addExtension("mkdown");
    markdownType.addExtension("mdtext");
    markdownType.addExtension("md");
    markdownType.addExtension("mdown");
    markdownType.addExtension("txt");
    markdownType.addExtension("text");
    return markdownType;
  }
  
  /* -----------------------------------------------------------------
   This section of the class has methods for reading nodes from the
   file. 
   ------------------------------------------------------------------- */
  
  public boolean load (TextTree tree, TextLineReader textLineReader, TextIOType type) {
    htmlFile = new HTMLFile(textLineReader, 
        "TextLineReader", "** unknown **", type.getLabel());
    return load(tree, type);
  }
  
  public boolean load (TextTree tree, URL url, TextIOType type, String parm) {
    htmlFile = new HTMLFile (url, type.getLabel());
    htmlFile.setMetadataAsMarkdown(! parm.equalsIgnoreCase("nometa"));
    return load(tree, type);
  }
  
  private boolean load (TextTree tree, TextIOType type) {
    this.tree = tree;
    preformatted = false;
    boolean ok = true;
    
    try {
      htmlFile.openForInput();
    } catch (IOException e) {
      ok = false;
      Logger.getShared().recordEvent (LogEvent.MEDIUM, 
          "Encountered I/O error while reading HTML file " 
          + htmlFile.toString() + " Exception: " + e.toString(),
          false);   
    }
    
    if (ok) {
      try {
        HTMLTag tag = htmlFile.readTag();
        currentNode = tree.getTextRoot();
        while (tag != null) {
          
          // Process any text that immediately preceded this tag
          // System.out.println ("TAG");
          // System.out.println ("Preceding text: " + tag.getPrecedingText());
          // System.out.println ("Name:           " + tag.getName());
          // System.out.println ("Ending?         " + String.valueOf (tag.isEnding()));
          Enumeration attributes = tag.getAttributes();
          if (attributes.hasMoreElements()) {
            // System.out.println ("Attributes: ");
            while (attributes.hasMoreElements()) {
              HTMLAttribute attr = (HTMLAttribute)attributes.nextElement();
              if (attr != null) {
                // System.out.println
                //     ("  name=" + attr.getName() + " value=" + attr.getValue());
              } // end if 
            } // end while more attributes
          } // end if any attributes
          // System.out.println (" ");
          if (tag.getPrecedingText().length() > 0) {
            characters (tag.getPrecedingText());
          }
          
          if (tag.isEnding()) {
            // close an open tag
            endElement (tag);
          } else {
            // open a new tag
            startElement (tag);
          }
          
          // Done with last tag -- try to get another one
          tag = htmlFile.readTag();
        } // while we have more html tags
      }
      catch (FileNotFoundException e) {
        ok = false;
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            "File Not Found when attempting to read HTML file " 
            + htmlFile.toString() + " Exception: " + e.toString(),
            false);   
      } 
      catch (IOException e) {
        ok = false;  
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
          "Encountered I/O error while reading HTML file " 
          + htmlFile.toString() + " Exception: " + e.toString(),
            false);   
      }
    } // end if ok
    return ok;
  }
  
  
  /**
   Handle the beginning of a new element when parsing XML.
   */
  public void startElement (HTMLTag tag) {
    
    if (tag.hasName()
        && tag.getName().equals(TextType.PRE)) {
      preformatted = true;
    }
    else
    if (tag.hasName()
        && tag.getName().equals(TextType.TOC)) {
      tocFound = true;
    }
    if (tag.hasName()
        && tag.getName().equals ("a")
        && tag.containsAttribute ("alias")) {
      replaceAlias (tag);
    } else {
      makeNodeFromElementStart (tag);
    }
  }

  private void replaceAlias (HTMLTag tag) {
    String alias = tag.getAttribute("alias").getValue();
    String href  = tag.getAttribute("href").getValue();
    replaceAlias (tree.getTextRoot(), alias, href);
  }

  private void replaceAlias (TreeItem<TextData> node, String alias, String href) {

    // Replace matching alias with new value
    if (node.getValue().isAttributeHref()
        && node.getValue().getText().equals (alias)) {
      node.getValue().setText (href);
    } 

    // Process this node's children
    for (int i = 0; i < node.getChildren().size(); i++) {
      replaceAlias (node.getChildren().get(i), alias, href);
    }

  }

  private void makeNodeFromElementStart (HTMLTag tag) {
    // Create a new node for this element
    TreeItem<TextData> nextNode = tree.createNode(tag.getName(), "");
    nextNode.getValue().setType (tag.getName());
    if (currentNode.getValue().isNakedText()) {
      TreeItem<TextData> parentNode = currentNode.getParent();
      currentNode = parentNode;
    }
    currentNode.getChildren().add (nextNode);

    // Harvest any attributes
    Enumeration attributes = tag.getAttributes();
    
    // Harvest TOC entries
    if (tag.isHeadingTag()) {
      tocEntry = new TocEntry();
      tocEntry.setLevel(tag.getHeadingLevel());
      if (tag.containsAttribute("id")) {
        tocEntry.setID(tag.getAttributeValue("id"));
      }
      captureHeading = true;
      headingNode = nextNode;
    }
    while (attributes.hasMoreElements()) {
      HTMLAttribute attr = (HTMLAttribute)attributes.nextElement();
      if (attr != null) {
        nextNode.getValue().addAttribute (attr.getName(), attr.getValue());
        if (tag.getName().equals(TextType.TOC)) {
          if (attr.getName().equalsIgnoreCase("from")) {
            tocFrom = extractHeadingLevel(attr.getValue(), tocFrom);
          }
          else
          if (attr.getName().equalsIgnoreCase("thru")
              || attr.getName().equalsIgnoreCase("through")) {
            this.tocThrough = extractHeadingLevel(attr.getValue(), tocThrough);
          }
        } // end if toc tag
      } // end if 
    }

    // Decide whether current node should be left open
    if (nextNode.getValue().getType().equalsIgnoreCase (TextType.XML)
        || nextNode.getValue().getType().equalsIgnoreCase (TextType.DOCTYPE)
        || nextNode.getValue().getType().equals (TextType.COMMENT)
        || nextNode.getValue().getType().equalsIgnoreCase (TextType.IMAGE)
        || nextNode.getValue().getType().equalsIgnoreCase (TextType.BREAK)
        || nextNode.getValue().getType().equalsIgnoreCase (TextType.HORIZONTAL_RULE)
        || nextNode.getValue().getType().equalsIgnoreCase (TextType.AREA)) {
      // consider this tag closed
    } else {
      // leave this tag open
      currentNode = nextNode;
    }
  } // end method
  
  public void characters (String more) {
    if (currentNode.getValue().hasChildTags()) {
      TreeItem<TextData> nextNode = tree.createNode(TextType.NAKED_TEXT, "");
      currentNode.getChildren().add (nextNode);
      currentNode = nextNode;
    }
    currentNode.getValue().characters (more, preformatted);
    if (captureHeading && tocEntry != null) {
      tocEntry.append(more);
    }
  }
  
  public void endElement (HTMLTag tag) {
    
    if (tag.hasName()
        && tag.getName().equals(TextType.PRE)) {
      preformatted = false;
    }
    
    if (tag.isHeadingTag() 
        && tocEntry != null
        && headingNode != null) {
      if (tocEntry.lacksID()) {
        tocEntry.deriveID();
        headingNode.getValue().addAttribute ("id", tocEntry.getID());
      }
      if (tocFound
          && tag.getHeadingLevel() >= tocFrom
          && tag.getHeadingLevel() <= tocThrough) {
        tree.addTocEntry(tocEntry);
      }
      tocEntry = null;
      headingNode = null;
      captureHeading = false;
    }
    
    TreeItem<TextData> parentNode = null;
    if (currentNode != null) {
      TreeItem<TextData> nodeToClose = currentNode;
      while ((nodeToClose != null) 
          && (! nodeToClose.getValue().getType().equalsIgnoreCase (tag.getName()))) {
        parentNode = nodeToClose.getParent();
        nodeToClose = parentNode;
      }
      if (nodeToClose != null) {
        currentNode = nodeToClose;
      }
      if (currentNode != null
          && currentNode.getValue().getType().equalsIgnoreCase (tag.getName())) {
        parentNode = currentNode.getParent();
        if (parentNode != null) {
          currentNode = parentNode;
        } // end if we have a parent
      } // end if we have a current node to work with
    } // end if we have a current node to work with
  } // end method
  
  /**
   Return the numeric portion of the passed tag. 
  
   @param headingTag The tag to be evaluated (h1, h2, etc.)
   @param defaultLevel If we don't find a digit in the range of 1 - 6,
                       then we'll return this value instead. 
  
   @return The last digit in the passed string, if it is in the range 1 - 6,
           otherwise the default level.
  */
  private int extractHeadingLevel(String headingTag, int defaultLevel) {
    int headingLevel = 0;
    for (int i = 0; i < headingTag.length(); i++) {
      if (Character.isDigit(headingTag.charAt(i))) {
        try {
          headingLevel = Integer.parseInt(headingTag.substring(i, i + 1));
        } catch (NumberFormatException e) {
          // ignore any exceptions
        }
      } // end if character is a digit
    } // end for each character in tag
    if (headingLevel >= 1 && headingLevel <= 6) {
      return headingLevel;
    } else {
      return defaultLevel;
    }
  }

  public boolean store (TextTree tree, TextWriter writer, TextIOType type,
      boolean epub, String epubSite) {
    this.tree = tree;
    boolean ok = false;
    return false;
  }

}
