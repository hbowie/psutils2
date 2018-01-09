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
  
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.txbmodel.*;

  import java.io.*;
  import java.net.*;
  import java.util.*;

  import javafx.scene.control.*;

/**
  A class to write out data (trees and nodes) in a variety of
  different markup languages.
 */
public class MarkupWriter 
    extends TextIOModule
    implements TextLineWriter {
  
  private              int                 markupFormat = 1;
  public  final static int                 UNDEFINED_FORMAT = 0;
  public  final static int                 MARKUP_FORMAT_LOW = 1;
  public  final static int                 MARKDOWN_FORMAT = 1;
  public  final static int                 TEXTILE_SYNTAX_1_FORMAT = 2;
  public  final static int                 TEXTILE_SYNTAX_2_FORMAT = 3;
  public  final static int                 HTML_FORMAT = 4;
  public  final static int                 HTML_FRAGMENT_FORMAT = 5;
  public  final static int                 NETSCAPE_BOOKMARKS_FORMAT = 6;
  public  final static int                 XML_FORMAT = 7;
  public  final static int                 OPML_FORMAT = 8;
  public  final static int                 STRUCTURED_TEXT_FORMAT = 9;
  public  final static int                 MARKUP_FORMAT_HIGH = 9;
  
  public  final static String              OPEN_TAG = "<";
  public  final static String              END_TAG = "/";
  public  final static String              CLOSE_TAG = ">";
  public  final static String              EXCLAMATION_POINT = "!";
  public  final static String              DASH = "-";
  
  public  final static String              TOC_STYLE = "toc";
  
  private              boolean             htmlEntities = false;
  private              boolean             xmlEntities = false;
  private              boolean             markdownLinksInline = false;
  
  public  final static String[]            LABELS = 
      { "Unknown", "Markdown", "Textile Format 1", "Textile Format 2", 
        "HTML", "HTML Fragment", "Netscape Bookmarks", "XML", "OPML", 
        "Structured Text" };
  
  public  final static String[]            EXTENSIONS =
      { "unknown", "mdtext",   "textile", "textile",         
        "html", "html", "html", "xml", "opml", "txt" };
 
  private              String              xmlVersion = "1.0";
  private              String              xmlEncoding = "UTF-8";
  
  private              boolean             indenting = false;
  private              int                 indentPerLevel = 2;
  
  private              TextLineWriter      lineWriter = null;
  private              TextWriter          writer = null;

  private              boolean             epub = false;
  private              String              epubSite = "";
  
  private              String              tag = "";
  private              boolean             heading = false;
  private              int                 headingLevel = 1;
  
  private              boolean             comment = false;
  private              boolean             preformatted = false;
  
  private              int                 nextLink = 0;
  private              ArrayList           links = new ArrayList();
  private              boolean             blockQuote = false;
  private              boolean             quoteMacroOpen = false;
  private              boolean             needExplicitParagraph = false;
  private              boolean             skipWhitespace = true;
  
  private              char                lastChar = ' ';
  private              char                c = ' ';
  private              char                c2 = ' ';
  private              char                c3 = ' ';
  private              boolean             lastCharWasWhiteSpace = true;
  private              boolean             lastCharWasEmDash = false;
  private              boolean             whiteSpace        = true;
  private              boolean             whiteSpacePending = false;
  private              boolean             startingQuote = true;
  private              int                 copyrightPending = 0;
  
  private              boolean             storingText = false;
  private              StringBuffer        text = new StringBuffer();

  private              StringBuffer        listChar = new StringBuffer();
  
  // The following fields are used for Structured Text output
  private              String              streamTag = "";
  private              String              documentTag = "";
  private              boolean             writeBlankValues = true;
  private              int                 minimumCharsToColon = 0;
  private              int                 paragraphsWithinTag = 0;
  
  /**
  Construct a new MarkupWriter, passing in the line writer to be used
  and identifying the markup format to be used. 
  
  @param lineWriter   A writer that satisfies the TextLineWriter interface.
  @param markupFormat An integer identifying the markup format to be used. 
  */
  public MarkupWriter (TextLineWriter lineWriter, int markupFormat) {
    setLineWriter (lineWriter);
    setMarkupFormat (markupFormat);
  }

  /**
  Construct a new MarkupWriter, passing in a file to be written to, and 
  identifying the markup format to be used. 
  
  @param file         The file to be output. 
  @param markupFormat An integer identifying the markup format to be used. 
  */
  public MarkupWriter (File file, int markupFormat) {
    setFile (file);
    setMarkupFormat (markupFormat);
  }
  
  /**
  Construct a new MarkupWriter, deferring the output and format definition until
  later. 
  */
  public MarkupWriter () {

  }
  
  /**
  Pass the file to be used as output. 
  
  @param file         The file to be output.
  */
  public void setFile(File file) {
    FileMaker fileMaker = new FileMaker (file);
    setLineWriter (fileMaker);
  }
  
  /**
  Pass the line writer to be used. 
  @param lineWriter 
  */
  public void setLineWriter (TextLineWriter lineWriter) {
    this.lineWriter = lineWriter;
    writer = new TextWriter (lineWriter);
    writer.setIndentPerLevel(indentPerLevel);
    writer.setIndenting(indenting);
  }
  
  /**
  Turn indenting on or off. 
  
  @param indenting True if we want to use the indenting feature.
  */
  public void setIndenting (boolean indenting) {
    
    this.indenting = indenting;
    if (writer != null) {
      writer.setIndenting(indenting);
    }
  }

  /**
  Set the number of spaces to indent per level. Turn indenting on if the
  specified value is greater than zero.
  
  @param indentPerLevel Number of spaces to indent per level.
  */
  public void setIndentPerLevel (int indentPerLevel) {
   
    this.indentPerLevel = indentPerLevel;
    if (indentPerLevel > 0) {
      indenting = true;
    }
    if (writer != null) {
      writer.setIndentPerLevel(indentPerLevel);
    }
  }
  
  public String getDestination() {
    if (lineWriter == null) {
      return "";
    } else {
      return lineWriter.getDestination();
    }
  }
  
  public boolean isOK() {
    return lineWriter.isOK();
  }
  
  public void registerTypes (List types) {
    
    TextIOType type1 = new TextIOType (LABELS [MARKDOWN_FORMAT],
        this, false, true, EXTENSIONS [MARKDOWN_FORMAT]);
    type1.addExtension ("markdown");
    type1.addExtension ("md");
    types.add (type1);
    
    TextIOType type2 = new TextIOType (LABELS [TEXTILE_SYNTAX_1_FORMAT],
        this, false, true, EXTENSIONS [TEXTILE_SYNTAX_1_FORMAT]);
    types.add (type2);

    TextIOType type3 = new TextIOType (LABELS [TEXTILE_SYNTAX_2_FORMAT],
        this, false, true, EXTENSIONS [TEXTILE_SYNTAX_2_FORMAT]);
    types.add (type3);
    
    TextIOType type4 = new TextIOType (LABELS [HTML_FORMAT],
        this, false, true, EXTENSIONS [HTML_FORMAT]);
    types.add (type4);

    TextIOType type5 = new TextIOType (LABELS [HTML_FRAGMENT_FORMAT],
        this, false, true, EXTENSIONS [HTML_FRAGMENT_FORMAT]);
    types.add (type5);

    TextIOType type6 = new TextIOType (LABELS [NETSCAPE_BOOKMARKS_FORMAT],
        this, false, true, EXTENSIONS [NETSCAPE_BOOKMARKS_FORMAT]);
    types.add (type6);
    
    TextIOType type7 = new TextIOType (LABELS [OPML_FORMAT],
        this, false, true, EXTENSIONS [OPML_FORMAT]);
    types.add (type7);

    TextIOType type8 = new TextIOType (LABELS [XML_FORMAT],
        this, false, true, EXTENSIONS [XML_FORMAT]);
    types.add (type8);
    
    TextIOType type9 = new TextIOType (LABELS [STRUCTURED_TEXT_FORMAT],
        this, false, true, EXTENSIONS [STRUCTURED_TEXT_FORMAT]);
    types.add (type8);
  }
  
  public boolean load (TextTree tree, URL url, TextIOType type, String parm) {
    return false;
  } // end load method

  /**
   Store a tree using the designated type of markup.

   @param tree
   @param writer
   @param type
   @param epub
   @param epubSite
   @return
   */
  public boolean store (TextTree tree, TextWriter writer, TextIOType type,
      boolean epub, String epubSite) {
    boolean ok = true;
    this.tree = tree;
    this.writer = writer;
    this.epub = epub;
    this.epubSite = epubSite;
    ok = setMarkupFormatFromType (type);
 
    if (ok) {
      ok = openForOutput();
    }
    if (ok) {
      ok = store (tree.getTextRoot());
    }
    if (ok) {
      ok = close();
    } 
    return ok;
  }
  
  private boolean setMarkupFormatFromType (TextIOType type) {
    boolean typeFound = false;
    int formatIndex = MARKUP_FORMAT_LOW;
    while (! typeFound && formatIndex <= MARKUP_FORMAT_HIGH) {
      typeFound = (type.getLabel().equalsIgnoreCase (LABELS [formatIndex]));
      if (! typeFound) {
        formatIndex++;
      }
    }
    if (typeFound) {
      setMarkupFormat (formatIndex);
    }
    return typeFound;
  }

  public void setMarkupFormat (int markupFormat) {
    this.markupFormat = markupFormat;
    htmlEntities = (markupFormat == HTML_FORMAT
        || markupFormat == HTML_FRAGMENT_FORMAT
        || markupFormat == NETSCAPE_BOOKMARKS_FORMAT);
    xmlEntities = (markupFormat == XML_FORMAT
        || markupFormat == OPML_FORMAT);
  }
  
  public int getMarkupFormat() {
    return markupFormat;
  }

  public void setMarkdownLinksInline (boolean markdownLinksInline) {
    this.markdownLinksInline = markdownLinksInline;
  }
  
  private boolean store (TreeItem<TextData> node) {
    
    boolean ok = true;
    
    extractTagInfo (node.getValue().getType());
    // Open the node
    if (ok) {
      if (node.getValue().isAttribute()
          || node.getValue().getTextType().isLocation()
          || node.getValue().isNakedText()) {
        // do nothing
      }
      else
      if (tag.equals (TextType.TOC)
          && tree != null
          && tree.hasTocEntries()) {
        writeTOC();
      }
      else
      if (markupFormat == HTML_FORMAT
          || markupFormat == HTML_FRAGMENT_FORMAT) {
        startXML (node);
      }
      else
      if (tag.equals (TextType.HTML)
          || tag.equals (TextType.HEAD)
          || tag.equals (TextType.BODY)
          || tag.equals (TextType.BREAK)
          || tag.equals (TextType.HORIZONTAL_RULE)
          || tag.equals (TextType.PARAGRAPH)
          || heading
          || tag.equals (TextType.BLOCK_QUOTE)
          || tag.equals (TextType.ANCHOR)
          || tag.equals (TextType.ITALICS)
          || tag.equals (TextType.EMPHASIS)
          || tag.equals (TextType.CITATION)
          || tag.equals (TextType.ORDERED_LIST)
          || tag.equals (TextType.UNORDERED_LIST)
          || tag.equals (TextType.LIST_ITEM)
          || tag.equals (TextType.DEFINITION_LIST)
          || tag.equals (TextType.DEFINITION_TERM)
          || tag.equals (TextType.DEFINITION_DEF)) {
      ok = start (node.getValue().getType(), node.getValue().getStyle(), node.getValue().getAttributeHref(), false);
      } else {
        // write out tag and all attributes
        startXML (node);
      }
    }
    
    if (node.getValue().isComment()) {
      comment = true;
    }
    
    if (tag.equals(TextType.PRE)) {
      preformatted = true;
    }
    
    // Write out any text
    if (node.getValue().getTextType().isLocation()
        || node.getValue().getType().equalsIgnoreCase (TextType.XML)
        || node.getValue().getType().equalsIgnoreCase (TextType.DOCTYPE)
        || node.getValue().getType().equalsIgnoreCase (TextType.COMMENT)
        || comment
        || node.getValue().isAttributeHref()
        || node.getValue().isAttributeTarget()
        || node.getValue().isAttribute()) {
      // ignore text
    } 
    else
    if (preformatted) {
      if (ok) {
        writeText(node.getValue().getText());
      }
    } else {
      if (ok) {
        writeTextForMarkup (node.getValue().getText());
      }
    }
    
    // Process this node's children
    for (int i = 0; i < node.getChildren().size(); i++) {
      store (node.getChildren().get(i));
    }
    
    // Close the node
    if (ok) {
      if (node.getValue().isAttribute()
          || node.getValue().getTextType().isLocation()
          || node.getValue().isNakedText()) {
        // do nothing
      }
      else
      if (node.getValue().isSelfClosing()) {
        // already closed
      } else {
        ok = end (node.getValue().getType(), node.getValue().getAttributeHref());
      }
    }
    
    if (node.getValue().isComment()) {
      comment = false;
    }
    
    if (node.getValue().getType().equals(TextType.PRE)) {
      preformatted = false;
    }
    
    return ok;
  }
  
  /**
   Store the Table of Contents. 
  */
  private void writeTOC() {
    int currentLevel = 0;
    int startingLevel = 0;
    boolean itemOpen[] = {false, false, false, false, false, false, false};
    for (int index = 0; index < tree.getTocSize(); index++) {
      
      // Process each table of contents entry
      TocEntry tocEntry = tree.getTocEntry(index);
      int level = tocEntry.getLevel();
      
      // Record starting heading level for TOC
      if (startingLevel == 0) {
        startingLevel = level;
        currentLevel = level - 1;
      }

      // If this is a higher level, then open unordered list(s)
      while (currentLevel < level) {
        currentLevel++;
        startUnorderedList(TOC_STYLE);
      }

      // If this is a lower level, then close open item(s) and list(s)
      while (currentLevel > level) {
        if (itemOpen [currentLevel]) {
          endListItem();
          itemOpen [currentLevel] = false;
        }
        endUnorderedList();
        currentLevel--;
      }
      
      // If we've got an item still open at this level, then close it
      if (itemOpen [currentLevel]) {
        endListItem();
        itemOpen [currentLevel] = false;
      }

      // Now write the next table of contents entry
      startListItem(TOC_STYLE);
      startLink(tocEntry.getLink());
      writeText(tocEntry.getHeading());
      endLink(tocEntry.getLink());
      itemOpen [level] = true;
      
    } // end of TOC entries
    
    // If we've still got any items or lists open, then close them
    while (currentLevel >= startingLevel) {
      if (itemOpen [currentLevel]) {
        endListItem();
      }
      endUnorderedList();
      currentLevel--;
    }
  } // end method writeTOC

  /**
   Open the output file and write out any standard headers.

   @return True if everything went ok.
   */
  public boolean openForOutput () {
    boolean ok = true;
    needExplicitParagraph = false;
    nextLink = 0;
    links = new ArrayList ();
    ok = writer.openForOutput();
    if (ok) {
      switch (markupFormat) {
        case MARKDOWN_FORMAT:
          break;
        case TEXTILE_SYNTAX_1_FORMAT:   
          break;
        case TEXTILE_SYNTAX_2_FORMAT: 
          break;
        case HTML_FORMAT:
          // writeXMLDeclaration();
          writeHTMLDoctype();
          writer.setIndenting (true);
          startHTML();
          break;
        case HTML_FRAGMENT_FORMAT:
          break;
        case NETSCAPE_BOOKMARKS_FORMAT:
          ok = writer.writeLine
              ("<!DOCTYPE NETSCAPE-Bookmark-file-1>");
          writer.setIndenting (true);
          startHTML();
          ok = writer.writeLine
              ("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">");
          break;
        case XML_FORMAT:
          writeXMLDeclaration();
          writer.setIndenting (true);
          break;
        case OPML_FORMAT:
          writeXMLDeclaration();
          writer.setIndenting (true);
          startOPML();
          break;
        case STRUCTURED_TEXT_FORMAT:
          paragraphsWithinTag = 0;
          break;
        default:
          break;
      }
    }
    return ok;
  }

  public boolean writeXMLDeclaration () {
    return writer.writeLine ("<?xml version=\""
        + xmlVersion
        + "\" encoding=\""
        + xmlEncoding
        + "\"?>");
  }
  
  public boolean writeHTMLDoctype () {
    return writer.writeLine 
        ("<!DOCTYPE html>");
  }
  
  public boolean flush () {
    writeLinks();
    return writer.flush();
  }
  
  public boolean close () {
    boolean ok = true;
    writeLinks();
    if (ok) {
      switch (markupFormat) {
        case MARKDOWN_FORMAT:
          break;
        case TEXTILE_SYNTAX_1_FORMAT:   
          break;
        case TEXTILE_SYNTAX_2_FORMAT: 
          break;
        case HTML_FORMAT:
          endHTML();
          break;
        case HTML_FRAGMENT_FORMAT:
          break;
        case NETSCAPE_BOOKMARKS_FORMAT:
          endHTML();
          break;
        case XML_FORMAT:
          break;
        case OPML_FORMAT:
          endOPML();
          break;
        case STRUCTURED_TEXT_FORMAT:
          ok = writer.writeLine("...");
          break;
        default:
          break;
      }
    }
    
    if (ok) {
      ok = writer.close();
    }
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
  
  /**
   Should blank values be written to the output, or just ignored?
  
   @param writeBlankValues True to write blank values, false otherwise. 
                           If never specified, defaults to true. 
  */
  public void setWriteBlankValues (boolean writeBlankValues) {
    this.writeBlankValues = writeBlankValues;
  }
  
  /**
   Set the minimum number of characters to space to the right before placing
   a colon, when writing structured text. 
  
   @param minimumCharsToColon The minimum number of characters to space to the
                              right, starting from the beginning of the tag. 
   */
  public void setMinimumCharsToColon (int minimumCharsToColon) {
    this.minimumCharsToColon = minimumCharsToColon;
  }
  
  /**
  Write out a comment to the output file. 
  
  @param text
  @return 
  */
  public boolean writeComment(String text) {
    
    boolean ok = true;
    
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case STRUCTURED_TEXT_FORMAT:
        if (! writer.isNewLineStarted()) {
          ok = writer.write(' ');
        }
        ok = writer.write("# ");
        ok = writer.write(text);
        ok = writer.newLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
      case OPML_FORMAT:
      default:
        ok = writer.ensureNewLine();
        ok = openTag();
        ok = writer.write ("!-- ");
        ok = writer.write (text);
        ok = writer.write (" --");
        ok = closeTag();
        ok = writer.newLine();
        break;
    }
    return ok;
    
  }
  
  public boolean write (String tagAnyCase, String text) {
    return write (tagAnyCase, text, "", "", false);
  }
  
  /**
   Write out a tag and enclosed text.
   
   @param tagAnyCase
   @param text
   @param style
   @param link
   @param multiple
   @return
   */
  public boolean write (String tagAnyCase,
      String text, String style, String link, boolean multiple) {
    
    boolean ok = true;
    if (text.length() > 0 || writeBlankValues) {
      ok = start (tagAnyCase, style, link, multiple);
      if (ok) {
        writeTextForMarkup (text);
      }
      if (ok) {
        ok = end (tagAnyCase, link);
      }
    }
    return ok;
  }
  
  /**
   Write a new outline node for an OPML document. The outline element will 
   be closed, so no children are expected. 
  
   @param text The text of the outline node. 
  */
  public void writeOutline (String text) {
    String t;
    if (text == null || text.length() == 0) {
      t = " ";
    } else {
      t = formatTextForMarkup (text);
    }
    startXML (TextType.OUTLINE,
        TextType.TEXT, t,
        true, true, true);
  }
  
  /**
   Start a new outline node for an OPML document. 
  
   @param text The text of the outline node. 
  */
  public void startOutline (String text) {
    startXML (TextType.OUTLINE,
        TextType.TEXT, formatTextForMarkup (text),
        true, true, false);
  }
  
  /**
   Start a new outline node for an OPML document. 
  
   @param type Type of node
   @param text The text that makes up this outline entry. 
   @param link An optional link. 
   @param emptyTag Is this an empty tag? True means close it, false means
                   leave it open so that it may include children. 
   @return True if no I/O errors. 
  */
  public boolean startOutline 
      (String type, String text, String link, boolean emptyTag) {
    boolean ok = true;
    if (text.length() > 0) {
      if (link.length() > 0) {
        startXML (TextType.OUTLINE,
            TextType.TYPE, type,
            TextType.TEXT, formatTextForMarkup (text),
            TextType.URL, link,
            true, true, emptyTag);
      } else {
        startXML (TextType.OUTLINE,
            TextType.TYPE, type,
            TextType.TEXT, formatTextForMarkup (text),
            true, true, emptyTag);
      }
    } else {
      startXML (TextType.OUTLINE,
          TextType.TYPE, type,
          true, true, emptyTag);
    }
    return ok;
  }
  
  public boolean startOutlineOpen() {
    boolean ok;
    ok = openTag();
    if (ok) {
      ok = writer.write (TextType.OUTLINE);
    }
    return ok;
  }
  
  /**
   Add another attribute-value pair to the tag being built.
  
   @param attribute The attribute.
   @param value     The value.
  */
  public void writeOutlineAttribute (String attribute, String value) {
    if (attribute.length() > 0) {
      writer.write (" " + attribute);
      String v;
      if (value == null || value.length() == 0) {
        v = " ";
      } else {
        v = formatTextForMarkup (value);
      }
      writer.write ("=\"" + v + "\"");
    }
  }
  
  public boolean startOutlineClose() {
    boolean ok;
    ok = closeTag();
    if (ok) {
      ok = writer.newLine();
    }
    writer.moreIndent();
    return ok;
  }
  
  public boolean endOutline () {
    boolean ok = true;
    endXML (TextType.OUTLINE, true, true, true);
    return ok;
  }
  
  public boolean cleanAndWrite (String text) {
    
    boolean ok = true;
    
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        if (isNewLineStarted()) {
          skipWhitespace = true;
        }
        for (int i = 0; i < text.length(); i++) {
          if (ok) {
            if (Character.isWhitespace (text.charAt (i))) {
              if (! skipWhitespace) {
                ok = writeText (' ');
                skipWhitespace = true;
              }
            } else {
              ok = writeText (text.charAt (i));
              skipWhitespace = false;
            }
          }
        }
        break;
      case HTML_FORMAT:
        ok = writeTextForMarkup (text);
        break;
      case HTML_FRAGMENT_FORMAT:
        ok = writeTextForMarkup (text);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        ok = writeTextForMarkup (text);
        break;
      case XML_FORMAT:
        ok = writeTextForMarkup (text);
        break;
      case OPML_FORMAT:
        if (isNewLineStarted()) {
          skipWhitespace = true;
        }
        for (int i = 0; i < text.length(); i++) {
          if (ok) {
            char c = text.charAt (i);
            if (Character.isWhitespace (c)) {
              if (! skipWhitespace) {
                ok = writeText (' ');
                skipWhitespace = true;
              }
            } 
            else
            if (c == '"') {
              ok = writeText ("&quot;");
              skipWhitespace = false;
            }
            else {
              ok = writeText (c);
              skipWhitespace = false;
            }
          }
        }
        break;
      case STRUCTURED_TEXT_FORMAT:
        ok = writeTextForMarkup (text);
        break;
      default:
        ok = writeText (text);
        break;
    }
    
    return ok;
  }

  /**
   Insert the specified file into the output stream.
   */
  public boolean insertFile (File inFile) {
    boolean ok = true;

    if (inFile.exists()
        && inFile.isFile()
        && inFile.canRead()) {
      ok = true;
    } else {
      ok = false;
    }

    // If the file was found, then read and copy it into the output
    if (ok) {
      try {
        BufferedReader inReader = new BufferedReader(new FileReader (inFile));
        String inLine = inReader.readLine();
        while (inLine != null) {
          ok = writeLine (inLine);
          inLine = inReader.readLine();
        }
      } catch (java.io.IOException e) {
        ok = false;
      }
    }

    return ok;
  }
  
  public boolean write (String text) {
    return writer.write (text);
  }
  
  public boolean writeLine (String text) {
    return writer.writeLine (text);
  }
  
  public boolean newLine () {
    return writer.newLine();
  }
  
  public boolean writeTextForMarkup (String s) {
    return writer.write (formatTextForMarkup (s));
  }
  
  /**
   Write out text (non-tag) data
   */
  public String formatTextForMarkup (String s) {

    lastCharWasWhiteSpace = (whiteSpace || writer.isNewLineStarted());
    lastChar = ' ';
    if (lastCharWasWhiteSpace) {
      whiteSpacePending = false;
    }
    startingQuote = lastCharWasWhiteSpace;

    StringBuilder t = new StringBuilder();
    if (whiteSpacePending) {
      t.append (' ');
      lastCharWasWhiteSpace = true;
      whiteSpacePending = false;
      startingQuote = true;
    }
    int j = 0;
    int k = 0;
    int nextSemi = 0;
    for (int i = 0; i < s.length(); i++) {
      c = s.charAt (i);
      j = i + 1;
      if (j < s.length()) {
        c2 = s.charAt (j);
      } else {
        c2 = ' ';
      }
      k = j + 1;
      if (k < s.length()) {
        c3 = s.charAt (k);
      } else {
        c3 = ' ';
      }
      if (nextSemi <= i && nextSemi >= 0) {
        nextSemi = s.indexOf (';', i + 1);
      }
      
      // If this is the second char in the -- sequence, then just let
      // it go by, since we already wrote out the em dash
      if (lastCharWasEmDash) {
        lastCharWasEmDash = false;
        lastCharWasWhiteSpace = false;
        startingQuote = false;
      }
      else
      if (copyrightPending > 0) {
        copyrightPending--;
      }
      else
      // If we have white space, write out only one space
      if (c == ' '
          || c == '\t'
          || c == '\r'
          || c == '\n') {
        lastCharWasEmDash = false;
        startingQuote = true;
        if (lastCharWasWhiteSpace) {
          // do nothing
        } else {
          t.append (' ');
          lastCharWasWhiteSpace = true;
        }
      } 
      else
        
      // If we have an en dash, replace it with the appropriate entity
      if (c == '–' && htmlEntities) {
        t.append ("&#8211;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      }
      else
        
      // If we have two dashes, replace them with an em dash
      if (c == '-' && c2 == '-' && htmlEntities) {
        t.append ("&#8212;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = true;
        startingQuote = false;
      } 
      else

      // If we have a c in parentheses, replace it with the copyright symbol
      if (c == '(' && c2 == 'c' && c3 == ')' && htmlEntities) {
        t.append ("&copy;");
        lastCharWasWhiteSpace = false;
        copyrightPending = 2;
      }
      else
        
      // If we have a double quotation mark, replace it with a smart quote  
      if (c == '"' && (htmlEntities)) {
        if (startingQuote) {
          t.append ("&#8220;");
        } else {
          t.append ("&#8221;");
        }
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      } 
      else 
        
      // If we have a double quotation mark, replace it with a entity  
      if (c == '"' && (xmlEntities)) {
        t.append ("&quot;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      } 
      else 
        
      // If we have a single quotation mark, replace it with the appropriate entity
      if (c == '\'' && htmlEntities) {
        if (startingQuote) {
          t.append ("&#8216;");
        } else {
          t.append ("&#8217;");
        }
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      }
      else
        
      // If we have a single quotation mark, replace it with the appropriate entity
      if (c == '\'' && xmlEntities) {
        t.append ("&apos;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      }
      else
        
      // if an isolated ampersand, replace it with appropriate entity
      if (c == '&' && (nextSemi < 0 || nextSemi > (i + 7) || c2 == ' ')) {
        t.append ("&amp;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      }
      else

      // if an isolated less than sign, replace it with appropriate entity
      if (c == '<' && (c2 == ' ' || c2 == '>')) {
        t.append ("&lt;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      }
      else

      // if an isolated greater than sign, replace it with appropriate entity
      if (c == '>' && (lastChar == '<')) {
        t.append ("&gt;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
      }
      else

      // Check for certain forms of punctuation
      if (c == '(' || c == '[' || c == '{' || c == '/') {
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = true;
        t.append (c);
      }
      else
      
      // If nothing special, then just move it to the output
      {
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        startingQuote = false;
        t.append (c);
      }
      lastChar = c;
    } // end for each char in text
    whiteSpace = lastCharWasWhiteSpace;
    return (t.toString());
  } // end method writeText
  
  public boolean start (String tagAnyCase) {
    return start (tagAnyCase, "", "", false);
  }
  
  /** 
   Start the tag. 
   @param tagAnyCase
   @param style
   @param link
   @param multiple
   @return
   */
  public boolean start (String tagAnyCase, 
      String style, String link, boolean multiple) {
    
    boolean ok = true;
    extractTagInfo (tagAnyCase);
    if (tag.equals (TextType.HTML)) {
      startHTML ();
    }
    else
    if (tag.equals (TextType.HEAD)) {
      startHead();
    }
    else
    if (tag.equals (TextType.BODY)) {
      startBody();
    }
    else
    if (tag.equals (TextType.BREAK)) {
      writeBreak();
    }
    else
    if (tag.equals (TextType.HORIZONTAL_RULE)) {
      writeHorizontalRule();
    }
    else
    if (tag.equals (TextType.PARAGRAPH)) {
      startParagraph (style, multiple);
    }
    else
    if (heading) {
      startHeading (headingLevel, style);
    }
    else
    if (tag.equals (TextType.BLOCK_QUOTE)) {
      startBlockQuote (style, multiple);
    }
    else
    if (tag.equals (TextType.ANCHOR)) {
      if (link.length() > 0) {
        startLink (link);
      }
    }
    else
    if (tag.equals (TextType.ITALICS)) {
      startItalics();
    }
    else
    if (tag.equals (TextType.EMPHASIS)) {
      startEmphasis();
    }
    else
    if (tag.equals (TextType.STRONG)) {
      startStrong();
    }
    else
    if (tag.equals (TextType.CITATION)) {
      startCitation (style);
    }
    else
    if (tag.equals (TextType.ORDERED_LIST)) {
      startOrderedList (style);
    }
    else
    if (tag.equals (TextType.UNORDERED_LIST)) {
      startUnorderedList (style);
    }
    else
    if (tag.equals (TextType.LIST_ITEM)) {
      startListItem (style);
    }
    else
    if (tag.equals (TextType.DEFINITION_LIST)) {
      startDefinitionList (style);
    }
    else
    if (tag.equals (TextType.DEFINITION_TERM)) {
      startDefinitionTerm (style);
    }
    else
    if (tag.equals (TextType.DEFINITION_DEF)) {
      startDefinitionDef (style);
    } else {
      switch (markupFormat) {
        case MARKDOWN_FORMAT:
        case TEXTILE_SYNTAX_1_FORMAT:       
        case TEXTILE_SYNTAX_2_FORMAT: 
          break;
        case HTML_FORMAT:
        case HTML_FRAGMENT_FORMAT:
        case NETSCAPE_BOOKMARKS_FORMAT:
        case XML_FORMAT:
          startXML (tag, 
            style, 
            true,   // Break before
            true,   // Break after
            false); // Empty tag
          break;
        case OPML_FORMAT:
          break;
        case STRUCTURED_TEXT_FORMAT:
          ok = startStructuredText(tag);
          break;
        default:
          break;
      }
    }
    
    return ok;
  }
  
  public boolean end (String tagAnyCase) {
    return end (tagAnyCase, "");
  }
  
  /**
   End the tag. 
   
   @param tagAnyCase
   @param link
   @return
   */
  public boolean end (String tagAnyCase, String link) {
    
    boolean ok = true;
    extractTagInfo (tagAnyCase);
    
    if (tag.equals (TextType.HTML)) {
      endHTML();
    }
    else
    if (tag.equals (TextType.HEAD)) {
      endHead();
    }
    else
    if (tag.equals (TextType.BODY)) {
      endBody();
    }
    else
    if (tag.equals (TextType.BREAK)) {
      // Ignore it -- we did whatever we needed to do when we started the break
    }
    else
    if (tag.equals (TextType.HORIZONTAL_RULE)) {
      // Ignore it -- we did whatever we needed to do when we started the HR
    }
    else
    if (tag.equals (TextType.PARAGRAPH)) {
      endParagraph ();
    }
    else
    if (heading) {
      endHeading (headingLevel);
    }
    else
    if (tag.equals (TextType.BLOCK_QUOTE)) {
      endBlockQuote ();
    }
    else
    if (tag.equals (TextType.ANCHOR)) {
      endLink (link);
    }
    else
    if (tag.equals (TextType.ITALICS)) {
      endItalics();
    }
    else
    if (tag.equals (TextType.EMPHASIS)) {
      endEmphasis();
    }
    else
    if (tag.equals (TextType.STRONG)) {
      endStrong();
    }
    else
    if (tag.equals (TextType.CITATION)) {
      endCitation ();
    }
    else
    if (tag.equals (TextType.ORDERED_LIST)) {
      endOrderedList ();
    }
    else
    if (tag.equals (TextType.UNORDERED_LIST)) {
      endUnorderedList ();
    }
    else
    if (tag.equals (TextType.LIST_ITEM)) {
      endListItem ();
    }
    else
    if (tag.equals (TextType.DEFINITION_LIST)) {
      endDefinitionList ();
    }
    else
    if (tag.equals (TextType.DEFINITION_TERM)) {
      endDefinitionTerm ();
    }
    else
    if (tag.equals (TextType.DEFINITION_DEF)) {
      endDefinitionDef ();
    } else {
      switch (markupFormat) {
        case MARKDOWN_FORMAT:
        case TEXTILE_SYNTAX_1_FORMAT:       
        case TEXTILE_SYNTAX_2_FORMAT: 
          break;
        case HTML_FORMAT:
        case HTML_FRAGMENT_FORMAT:
        case NETSCAPE_BOOKMARKS_FORMAT:
        case XML_FORMAT:
          endXMLforKnownTags (tag);
          break;
        case OPML_FORMAT:
          break;
        case STRUCTURED_TEXT_FORMAT:
          ok = endStructuredText(tag);
          break;
        default:
          break;
      }

    }
    
    return ok;
  }
  
  private void extractTagInfo (String tagAnyCase) {
    
    tag = tagAnyCase.toLowerCase();
    
    heading = false;
    headingLevel = 1;
    if (tag.length() == 2
        && tag.substring(0, 1).equals (TextType.HEADING_PREFIX)) {
      try {
        headingLevel = Integer.parseInt (tag.substring(1));
        heading = true;
      } catch (NumberFormatException e) {
        heading = false;
        headingLevel = 0;
      }
      if (heading && 
          (headingLevel < 1 || headingLevel > 6)) {
        heading = false;
      }
    } // end if heading

  }
  
  public void startHTML () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
        startXML (TextType.HTML, "", true, true, false);
        break;
      case HTML_FRAGMENT_FORMAT:
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        startXML (TextType.HTML.toUpperCase(), "", true, true, false);
        break;
      case STRUCTURED_TEXT_FORMAT:
        break;
      default:
        break;
    }
  }
  
  public void endHTML () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
        endXML (TextType.HTML, true, true, false);
        break;
      case HTML_FRAGMENT_FORMAT:
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        endXML (TextType.HTML.toUpperCase(), true, true, false);
        break;
      case STRUCTURED_TEXT_FORMAT:
        break;
      default:
        break;
    }
  }
  
  public void startOPML () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
        break;
      case HTML_FRAGMENT_FORMAT:
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
      case XML_FORMAT:
        startXML (TextType.OPML, TextType.VERSION, TextType.VERSION_NUMBER,
            true, true, false);
        break;
      case STRUCTURED_TEXT_FORMAT:
        break;
      default:
        break;
    }
  }
  
  public void endOPML () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
        break;
      case HTML_FRAGMENT_FORMAT:
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
      case XML_FORMAT:
        endXML (TextType.OPML, true, true, false);
        break;
      case STRUCTURED_TEXT_FORMAT:
        break;
      default:
        break;
    }
  }
  
  public void startHead () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.HEAD, "", true, true, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        startXML (TextType.HEAD, "", true, true, false);
        break;
      case STRUCTURED_TEXT_FORMAT:
        break;
      default:
        break;
    }
  }
  
  public void endHead () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXML (TextType.HEAD, true, true, true);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        endXML (TextType.HEAD, true, true, true);
        break;
      case STRUCTURED_TEXT_FORMAT:
        break;
      default:
        break;
    }
  }
  
  public void startBody () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
      case OPML_FORMAT:
        startXML (TextType.BODY, "", true, true, false);
        break;
      case STRUCTURED_TEXT_FORMAT:
        startStructuredText(TextType.BODY);
        break;
      default:
        break;
    }
  }
  
  public void endBody () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
      case OPML_FORMAT:
        endXML (TextType.BODY, true, true, true);
        break;
      case STRUCTURED_TEXT_FORMAT:
        endStructuredText(TextType.BODY);
        break;
      default:
        break;
    }
  }
  
  public void writeHeadingWithID (int level, String heading, String id) {
    startHeadingWithID (level, id);
    write (heading);
    endHeading (level);
  }
  
  public void writeHeading (int level, String heading, String style) {
    startHeading (level, style);
    write (heading);
    endHeading (level);
  }
  
  public void startHeading (int level, String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        // if (level > 2) {
          for (int i = 0; i < level; i++) {
            writer.write ('#');
          }
          writer.write(' ');
        // }
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        startTextileBlockTag (TextType.HEADING_PREFIX + String.valueOf (level),
            style, false);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
        startXML (TextType.HEADING_PREFIX + String.valueOf (level), style,
            true, true, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        if (level == 1) {
          startXML (TextType.HEADING_PREFIX.toUpperCase()
              + String.valueOf (level), "",
              false, false, false);
        } else {
          startXML (TextType.HEADING_PREFIX.toUpperCase()
              + String.valueOf (level), TextType.FOLDED.toUpperCase(), "",
              false, false, false);
        }
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        startStructuredText(TextType.HEADING_PREFIX.toUpperCase()
            + String.valueOf (level));
      default:
        break;
    }
  }
  
  public void startHeadingWithID (int level, String id) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        // if (level > 2) {
          for (int i = 0; i < level; i++) {
            writer.write ('#');
          }
          writer.write(' ');
        // }
        break;
      case TEXTILE_SYNTAX_1_FORMAT:       
      case TEXTILE_SYNTAX_2_FORMAT: 
        startTextileBlockTag (TextType.HEADING_PREFIX + String.valueOf (level),
            "", false);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
        startXML (TextType.HEADING_PREFIX + String.valueOf (level), 
            TextType.ID, id,
            true, false, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        if (level == 1) {
          startXML (TextType.HEADING_PREFIX.toUpperCase()
              + String.valueOf (level), 
              TextType.ID, id,
              false, false, false);
        } else {
          startXML (TextType.HEADING_PREFIX.toUpperCase()
              + String.valueOf (level), TextType.FOLDED.toUpperCase(), "",
              false, false, false);
        }
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        startStructuredText(TextType.HEADING_PREFIX.toUpperCase()
            + String.valueOf (level));
      default:
        break;
    }
  }
  
  public void endHeading (int level) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        /* if (level > 2) {
          writer.write(' ');
          for (int i = 0; i < level; i++) {
            writer.write ('#');
          }
        } else {
          writer.newLine();
          char underchar = '-';
          if (level == 1) {
            underchar = '=';
          }
          while (writer.getLineLength() < writer.getLastLineLength()) {
            writer.write (underchar);
          }
        } */
        writer.newLine();
        writer.ensureBlankLine ();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        needExplicitParagraph = false;
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
        endXML (TextType.HEADING_PREFIX + String.valueOf (level),
            false, true, true);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.HEADING_PREFIX.toUpperCase() + String.valueOf (level));
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        endStructuredText(TextType.HEADING_PREFIX.toUpperCase()
            + String.valueOf (level));
      default:
        break;
    } 
  }
  
  public void startBlockQuote (String style, boolean multiple) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureNewLine();
        writer.setLinePrefix ("> ");
        blockQuote = true;
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
        startTextileBlockTag (TextType.TEXTILE_BLOCK_QUOTE, style, multiple);
        writer.assumeLastLineBlank();
        blockQuote = true;
        break;
      case TEXTILE_SYNTAX_2_FORMAT:
        if (multiple) {
          writer.ensureBlankLine();
          writer.writeLine (TextType.QUOTE_MACRO);
          writer.assumeLastLineBlank();
          quoteMacroOpen = true;
        } else {
          startTextileBlockTag (TextType.TEXTILE_BLOCK_QUOTE, style, multiple);
          writer.assumeLastLineBlank();
          blockQuote = true;
        }
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.BLOCK_QUOTE, style, true, true, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        startStructuredText(TextType.BLOCK_QUOTE);
      default:
        break;
    } 
  }
  
  public void endBlockQuote () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureNewLine();
        writer.setLinePrefix ("");
        blockQuote = false;
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
        needExplicitParagraph = false;
        blockQuote = false;
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_2_FORMAT:
        if (quoteMacroOpen) {
          // writer.ensureNewLine();
          writer.writeLine (TextType.QUOTE_MACRO);
          blockQuote = false;
          quoteMacroOpen = false;
        } else {
          needExplicitParagraph = false;
          blockQuote = false;
          writer.ensureBlankLine();
        }
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.BLOCK_QUOTE);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        endStructuredText(TextType.BLOCK_QUOTE);
      default:
        break;
    } 
  }
  
  public void startDiv (String style, String id) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        boolean hasStyle = (style != null && style.length() > 0);
        boolean hasID    = (id    != null && id.length() > 0);
        if (hasStyle && hasID) {
          startXML (TextType.DIV, 
              TextType.CLASS, style, 
              TextType.ID, id, 
              true, true, false);
        }
        else
        if (hasStyle) {
          startXML (TextType.DIV, 
              style,
              true, true, false);
        }
        else
        if (hasID) {
          startXML (TextType.DIV,
              TextType.ID, id,
              true, true, false);
        } else {
          startXML (TextType.DIV,
              "",
              true, true, false);
        }
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    } 
  }
  
  public void endDiv () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        needExplicitParagraph = false;
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.DIV);
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureNewLine();
        break;
      default:
        break;
    } 
  }
  
  public void writeParagraph (String text, String style, boolean multiple) {
    startParagraph (style, multiple);
    write (text);
    endParagraph();
  }
  
  public void startParagraph (String style, boolean multiple) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.ensureBlankLine();
        if (needExplicitParagraph 
            || multiple
            || style.length() > 0) {
          startTextileBlockTag (TextType.PARAGRAPH, style, multiple);
          needExplicitParagraph = false;
        }
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        startXML (TextType.PARAGRAPH, style, true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        if (paragraphsWithinTag == 0) {
          writer.ensureNewLine();
        } else {
          writer.ensureBlankLine();
        }
        paragraphsWithinTag++;
        break;
      default:
        break;
    } 
  }
  
  public void endParagraph () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        needExplicitParagraph = false;
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.PARAGRAPH);
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureNewLine();
        break;
      default:
        break;
    } 
  }

  public void startOrderedList (String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        listChar.append ('#');
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        startXML (TextType.ORDERED_LIST, style, true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void endOrderedList () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        if (listChar.length() > 0) {
          listChar.deleteCharAt (listChar.length() - 1);
        }
        needExplicitParagraph = false;
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.ORDERED_LIST);
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void startUnorderedList (String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        listChar.append ('*');
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        startXML (TextType.UNORDERED_LIST, style, true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void startUnorderedList (String attribute1, String value1) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        listChar.append ('*');
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        startXML (TextType.UNORDERED_LIST, attribute1, value1,
            true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void endUnorderedList () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        if (listChar.length() > 0) {
          listChar.deleteCharAt (listChar.length() - 1);
        }
        needExplicitParagraph = false;
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.UNORDERED_LIST);
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void startListItem (String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        writer.ensureNewLine();
        writeText (listChar.toString() + " ");
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        startXML (TextType.LIST_ITEM, style, true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void endListItem () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        // writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        needExplicitParagraph = false;
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case NETSCAPE_BOOKMARKS_FORMAT:
      case XML_FORMAT:
        endXML (TextType.LIST_ITEM, true, true, false);
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void startDefinitionList (String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        listChar.append (';');
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.DEFINITION_LIST, style, true, true, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        startXML (TextType.DEFINITION_LIST.toUpperCase(),
            style, true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void endDefinitionList () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        needExplicitParagraph = false;
        if (listChar.length() > 0) {
          listChar.deleteCharAt (listChar.length() - 1);
        }
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.DEFINITION_LIST);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        endXMLforKnownTags (TextType.DEFINITION_LIST.toUpperCase());
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void startDefinitionTerm (String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        writer.ensureNewLine();
        writeText (listChar.toString() + " ");
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.DEFINITION_TERM, style, true, true, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        startXML (TextType.DEFINITION_TERM.toUpperCase(),
            style, true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void endDefinitionTerm () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        writeText (" ");
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.DEFINITION_TERM);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        endXMLforKnownTags (TextType.DEFINITION_TERM.toUpperCase());
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void startDefinitionDef (String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        writeText (": ");
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.DEFINITION_DEF, style, true, true, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        startXML (TextType.DEFINITION_DEF.toUpperCase(),
            style, true, true, false);
        break;
      case OPML_FORMAT:
        startStoringText();
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }

  public void endDefinitionDef () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        writeText (" ");
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.DEFINITION_DEF);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        endXMLforKnownTags (TextType.DEFINITION_DEF.toUpperCase());
        break;
      case OPML_FORMAT:
        startOutline (TextType.PARAGRAPH, text.toString(), "", true);
        stopStoringText();
      case STRUCTURED_TEXT_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      default:
        break;
    }
  }
  
  private void startStoringText () {
    text = new StringBuffer();
    storingText = true;
  }
  
  private void stopStoringText () {
    storingText = false;
  }
  
  private boolean isNewLineStarted () {
    if (storingText) {
      return false;
    } else {
      return writer.isNewLineStarted();
    }
  }
  
  private boolean writeText (char c) {
    boolean ok = true;
    if (storingText) {
      text.append (c);
    } else {
      ok = writer.write (c);
    }
    return ok;
  }
  
  private boolean writeText (String str) {
    boolean ok = true;
    if (storingText) {
      text.append (str);
    } else {
      ok = writer.write (str);
    }
    return ok;
  }
  
  public void writeTitle(String title) {
    startTitle();
    writeText(title);
    endTitle();
  }
  
  public void startTitle () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.TITLE, "", true, true, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        startXML (TextType.TITLE.toUpperCase(), "", true, true, false);
        break;
      case OPML_FORMAT:
        startXML (TextType.TITLE, "", true, true, false);
        break;
      case STRUCTURED_TEXT_FORMAT:
        startStructuredText (TextType.TITLE);
        break;
      default:
        break;
    } 
  }
  
  public void endTitle () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        needExplicitParagraph = false;
        writer.newLine();
        writer.ensureBlankLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.TITLE);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        endXMLforKnownTags (TextType.TITLE.toUpperCase());
        break;
      case OPML_FORMAT:
        endXMLforKnownTags (TextType.TITLE);
        break;
      case STRUCTURED_TEXT_FORMAT:
        endStructuredText (TextType.TITLE);
        break;
      default:
        break;
    } 
  }
  
  public void writeBreak () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.write ("  ");
        writer.newLine();
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.newLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.BREAK, "", false, true, true);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        startXML (TextType.BREAK.toUpperCase(), "", false, true, true);
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.write ("  ");
        writer.newLine();
        break;
      default:
        break;
    } 
  }

  public void writeHorizontalRule () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT:
        writer.write ("----");
        writer.newLine();
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.HORIZONTAL_RULE, "", false, true, true);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        break;
      default:
        break;
    }
  }
  
  public void startTextileBlockTag (String type, String style, boolean multiple) {
    writer.ensureBlankLine();
    writer.write (type);
    if (style.length() > 0) {
      writer.write ("(" + style + ")");
    }
    if (multiple) {
      writer.write (".");
      if (! type.equals (TextType.PARAGRAPH)) {
        needExplicitParagraph = true;
      }
    }
    writer.write (". ");
  }
  
  /**
   Write out one of a series of items, where each item may have a link, 
   and where the entire list will take the form item1, item2 and item3.
   
   @param The text that makes up this item.
   @param The link that goes with this item, where blank means no link.
   @param The position of this item in the series, where 0 indicates the first.
   @param The number of items in the series.
   */
  public void writeItemInFlatList (
      String text, 
      String link,
      int listPosition,
      int listLength) {
    
    writeLink (text, link);
    String listSuffix = "";
    if (listLength > 1) {
      int listEndProximity = listLength - listPosition - 1;
      if (listEndProximity == 1) {
        listSuffix = " and ";
      }
      else
      if (listEndProximity > 1) {
        listSuffix = ", ";
      }
    }
    writer.write (listSuffix);
  }
  
  public void emphasis (String text) {
    startEmphasis ();
    write (text);
    endEmphasis ();
  }
  
  public void startEmphasis () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.write ('*');
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.write (TextType.TEXTILE_EMPHASIS);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.EMPHASIS, "", false, false, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ('*');
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.write ('*');
        break;
      default:
        break;
    }
  }
  
  public void endEmphasis () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.write ('*');
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.write (TextType.TEXTILE_EMPHASIS);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.EMPHASIS);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ('*');
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.write ('*');
        break;
      default:
        break;
    } 
  }
  
  public void startStrong () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.write ("**");
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.write (TextType.TEXTILE_EMPHASIS);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.STRONG, "", false, false, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ("**");
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.write ("**");
        break;
      default:
        break;
    }
  }
  
  public void endStrong () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.write ("**");
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.write (TextType.TEXTILE_EMPHASIS);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.STRONG);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ("**");
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.write ("**");
        break;
      default:
        break;
    } 
  }
  
  public void startItalics () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.write ('*');
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.write (TextType.TEXTILE_ITALICS);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.ITALICS, "", false, false, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ('*');
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.write ('*');
        break;
      default:
        break;
    }
  }
  
  public void endItalics () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.write ('*');
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.write (TextType.TEXTILE_ITALICS);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.ITALICS);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ("*");
        break;
      case STRUCTURED_TEXT_FORMAT:
        writer.write ('*');
        break;
      default:
        break;
    } 
  }
  
  public void startCitation (String style) {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        startXML (TextType.CITATION, style, false, false, false);
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        startTextileInlineTag (TextType.TEXTILE_CITATION, style);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        startXML (TextType.CITATION, style, false, false, false);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ('*');
        break;
      case STRUCTURED_TEXT_FORMAT:
        startXML (TextType.CITATION, style, false, false, false);
        break;
      default:
        break;
    }
  }
  
  public void endCitation () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        endXMLforKnownTags (TextType.CITATION);
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        writer.write (TextType.TEXTILE_CITATION);
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        endXMLforKnownTags (TextType.CITATION);
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        break;
      case OPML_FORMAT:
        writeText ('*');
        break;
      case STRUCTURED_TEXT_FORMAT:
        endXMLforKnownTags (TextType.CITATION);
        break;
      default:
        break;
    }
  }
  
  public void startTextileInlineTag (String type, String style) {
    writer.write (type);
    if (style.length() > 0) {
      writer.write ("(" + style + ")");
    }
  }
  
  /**
   Write out a string with a hyperlink to something else. 
  
   @param text The text to be displayed. 
   @param link The destination for the link. 
  */
  public void writeLink (String text, String link) {
    startLink (link);
    write (text);
    endLink (link);
  }
  
  public void startLink (String inLink) {
    
    String link = epubLink(inLink);

    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        if (link.length() > 0) {
          writer.write ("[");
        }
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
        if (link.length() > 0) {
          writer.write ("\"");
        }
        break;
      case TEXTILE_SYNTAX_2_FORMAT:
        if (link.length() > 0) {
          writer.write ("[");
        }
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        if (link.length() > 0) {
          openTag();
          writer.write (TextType.ANCHOR);
          writer.write (" ");
          writer.write (TextType.HREF);
          writer.write ("=\"");
          writer.write (StringUtils.legitimizeURL(link));
          writer.write ("\"");
          closeTag();
        }
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        if (link.length() > 0) {
          openTag();
          writer.write (TextType.ANCHOR.toUpperCase());
          writer.write (" ");
          writer.write (TextType.HREF.toUpperCase());
          writer.write ("=\"");
          writer.write (link);
          writer.write ("\"");
          closeTag();
        }
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        if (link.length() > 0) {
          openTag();
          writer.write (TextType.ANCHOR);
          writer.write (" ");
          writer.write (TextType.HREF);
          writer.write ("=\"");
          writer.write (link);
          writer.write ("\"");
          closeTag();
        }
        break;
      default:
        break;
    }
  }
  
  public void endLink (String link) {

    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        if (link.length() > 0) {
          writer.write ("]");
          if (markdownLinksInline) {
            writer.write("(");
            writer.write(link);
            writer.write(")");
          } else {
            writer.write ("[");
            writer.write (String.valueOf(links.size()));
            writer.write ("]");
            links.add (link);
          }
        }
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
        if (link.length() > 0) {
          writer.write ("\":" + link);
        }
        break;
      case TEXTILE_SYNTAX_2_FORMAT:
        if (link.length() > 0) {
          writer.write ("|");
          writer.write (link);
          writer.write ("]");
        }
        break;
      case HTML_FORMAT:
      case HTML_FRAGMENT_FORMAT:
      case XML_FORMAT:
        if (link.length() > 0) {
          endXMLforKnownTags (TextType.ANCHOR);
        }
        break;
      case NETSCAPE_BOOKMARKS_FORMAT:
        if (link.length() > 0) {
          endXMLforKnownTags (TextType.ANCHOR.toUpperCase());
        }
        break;
      case OPML_FORMAT:
        break;
      case STRUCTURED_TEXT_FORMAT:
        endXMLforKnownTags (TextType.ANCHOR);
        break;
      default:
        break;
    }

  }
  
  /**
   Write out all the links referenced above at some convenient spot
   in the text. Each time this method is called, it will write out 
   all the links created since the last time this method was called. 
   */
  public void writeLinks () {
    switch (markupFormat) {
      case MARKDOWN_FORMAT:
        writer.ensureBlankLine();
        int lines = 0;
        while (nextLink < links.size()) {
          writer.write ("[");
          writer.write (String.valueOf (nextLink));
          writer.write ("]: ");
          writer.write ((String)links.get(nextLink));
          nextLink++;
          writer.newLine();
          lines++;
        }
        if (lines > 0) {
          writer.newLine();
        }
        break;
      case TEXTILE_SYNTAX_1_FORMAT:
      case TEXTILE_SYNTAX_2_FORMAT: 
        break;
      default:
        break;
    }
  }
  
  public boolean startStructuredText(String tag) {
    boolean ok = true;
    if (streamTag.length() > 0 && tag.equalsIgnoreCase(streamTag)) {
      // Do nothing, since structured text 
      // doesn't need an identifier for the stream.
    }
    else
    if (documentTag.length() > 0 && tag.equalsIgnoreCase(documentTag)) {
      ok = writer.writeLine("---");
    } else {
      ok = writer.ensureNewLine();
      ok = writer.write (tag);
      int chars = tag.length();
      while (chars < minimumCharsToColon) {
        ok = writer.write(' ');
        chars++;
      }
      ok = writer.write (": ");
      writer.moreIndent();
      paragraphsWithinTag = 0;
      /* if (blockValue) {
        writer.newLine();
      } */
    }
    return ok;
  }
  
  public boolean blockValueFollows(boolean preserveNewLines) {
    boolean ok = true;
    switch (markupFormat) {
      case STRUCTURED_TEXT_FORMAT:
        if (preserveNewLines) {
          ok = writer.write("| ");
        } else {
          ok = writer.write("> ");
        }
        ok = writer.ensureNewLine();
        break;
      default:
        break;
    }
    return ok;
  }
  
  public boolean endStructuredText(String tag) {
    boolean ok = true;
    if (streamTag.length() > 0 && tag.equalsIgnoreCase(streamTag)) {
      // Do nothing, since structured text 
      // doesn't need an identifier for the stream.
    }
    else
    if (documentTag.length() > 0 && tag.equalsIgnoreCase(documentTag)) {
      // Do nothing to end the document
    } else {
      writer.lessIndent();
      ok = writer.ensureNewLine();
    }
    return ok;
  }
  
  /* ====================================================================
  
   Utility methods for writing XML can be found in the following section.
  
     ==================================================================== */
  
  public void writeXMLElement(String tag, String text) {
    startXML (tag, "", true, false, false);
    writeTextForMarkup(text);
    endXML   (tag, false, true, false);
  }
  
  public void writeXMLElementLong(String tag, String text) {
    startXML (tag, "", true, true, false);
    writeTextForMarkup(text);
    endXML   (tag, true, true, false);
  }
  
  public void startXML (String tag) {
    startXML (tag, "", true, true, false);
  }
  
  /**
   Start a new XML tag. 
  
   @param tag
   @param style
   @param breakBefore
   @param breakAfter
   @param emptyTag 
  */
  public void startXML (String tag, String style, boolean breakBefore,
      boolean breakAfter, boolean emptyTag) {
    if (breakBefore) {
      writer.ensureNewLine();
    }
    openTag();
    writer.write (tag);
    if (style.length() > 0) {
      writer.write (" class=\"" + style + "\"");
    }
    if (emptyTag) {
      writer.write (" ");
      endingTag();
    }
    closeTag();
    if (breakAfter) {
      writer.newLine();
      if (! emptyTag) {
        writer.moreIndent();
      }
    }
  }
  
  public void startXML (String tag, String attribute1, String value1,
      boolean breakBefore, boolean breakAfter, boolean emptyTag) {
    if (breakBefore) {
      writer.ensureNewLine();
    }
    openTag();
    writer.write (tag);
    writeAttribute (attribute1, value1);
    if (emptyTag) {
      writer.write (" ");
      endingTag();
    }
    closeTag();
    if (breakAfter) {
      writer.newLine();
      if (! emptyTag) {
        writer.moreIndent();
      }
    }
  }
  
  public void startXML (String tag, String attribute1, String value1,
      String attribute2, String value2,
      boolean breakBefore, boolean breakAfter, boolean emptyTag) {
    if (breakBefore) {
      writer.ensureNewLine();
    }
    openTag();
    writer.write (tag);
    writeAttribute (attribute1, value1);
    writeAttribute (attribute2, value2);
    if (emptyTag) {
      writer.write (" ");
      endingTag();
    }
    closeTag();
    if (breakAfter) {
      writer.newLine();
      if (! emptyTag) {
        writer.moreIndent();
      }
    }
  }
  
  public void startXML (String tag, String attribute1, String value1,
      String attribute2, String value2,
      String attribute3, String value3,
      boolean breakBefore, boolean breakAfter, boolean emptyTag) {
    if (breakBefore) {
      writer.ensureNewLine();
    }
    openTag();
    writer.write (tag);
    writeAttribute (attribute1, value1);
    writeAttribute (attribute2, value2);
    writeAttribute (attribute3, value3);
    if (emptyTag) {
      writer.write (" ");
      endingTag();
    }
    closeTag();
    if (breakAfter) {
      writer.newLine();
      if (! emptyTag) {
        writer.moreIndent();
      }
    }
  }

  public void startXML (TreeItem<TextData> node) {
    
    if (node.getValue().breakBeforeOpeningTag()) {
      writer.ensureNewLine();
    }

    openTag();
    writer.write (node.getValue().getType());
    boolean emptyTag = node.getValue().getTextType().isSelfClosingTag();
    if (node.getValue().hasStyle()) {
      writeAttribute (TextType.CLASS, node.getValue().getStyle());
    }
    for (int i = 0; i < node.getChildren().size(); i++) {
      TreeItem<TextData> childNode = node.getChildren().get(i);
      TextData childData = childNode.getValue();
      if (childData.isAttribute()) {
        if (childData.getType().equals(TextType.CLOSING)) {
          emptyTag = true;
        }
        else
        if (epub && childData.getTextType().isAttributeTarget()) {
          // Target attributes neither allowed nor needed for epubs
        }
        else
        if (node.getValue().isComment()) {
          writer.write (childData.getText());
        }
        else
        if (node.getValue().isAnchor()
            && childData.isAttributeHref()) {
          writeAttribute (childData.getType(), epubLink(childData.getText()));
        } 
        else
        if (childData.getType().equalsIgnoreCase(TextType.CLASS)) {
          // Skip it, since we already wrote it out
        } else {
          writeAttribute (childData.getType(), childData.getText());
        }
      }
    }
    if (node.getValue().isComment()) {
      writer.write (" --");
    }
    else
    if (emptyTag) {
      writer.write (" ");
      endingTag();
    }
    closeTag();

    if (node.getValue().breakAfterOpeningTag()) {
      writer.newLine();
      if (! emptyTag) {
        writer.moreIndent();
      }
    }
    
  }

  private String epubLink (String inLink) {
    if (epub
        && inLink.toLowerCase().endsWith(".mp3")
        && inLink.startsWith("../")) {
      return epubSite + inLink.substring(2);
    } else {
      return inLink;
    }
  }
  
  /**
   Add another attribute-value pair to the tag being built.
  
   @param attribute The attribute.
   @param value     The value.
  */
  public void writeAttribute (String attribute, String value) {
    if (attribute.length() > 0) {
      writer.write (" " + attribute);
      if (value.length() > 0) {
        writer.write ("=\"" + value + "\"");
      }
    }
  }
  
  public void endXML (String tag) {
    endXML (tag, true, true, false);
  }
  
  /* 
   End an XHTML element with standard breaks depending on the type of tag. 
  */
  public void endXMLforKnownTags (String tag) {
    endXML (tag, 
        TextType.breakBeforeClosingTag(tag), 
        TextType.breakAfterClosingTag(tag),
        TextType.blankLineAfterClosingTag(tag));
  }
  
  /**
   End an XML element. 
  
   @param tag The tag being ended. 
  
   @param breakBefore Line break before?
   @param breakAfter  Line break after?
   @param blankLineAfter Blank line after?
  */
  public void endXML (String tag, 
      boolean breakBefore, 
      boolean breakAfter,
      boolean blankLineAfter) {
    if (breakBefore) {
      writer.ensureNewLine();
      writer.lessIndent();
    }
    openTag();
    endingTag();
    writer.write (tag);
    closeTag();
    if (breakAfter) {
      writer.newLine();
      if (blankLineAfter) {
        writer.newLine();
      }
    }
  }

  /**
   Increase indentation for following lines. 
  */
  public void moreIndent () {
    writer.moreIndent();
  }

  /**
   Decrease indentation for following lines. 
  */
  public void lessIndent () {
    writer.lessIndent();
  }
  
  /**
   Write an opening tag (aka '<').
  
   @return True unless an I/O error. 
  */
  public boolean openTag () {
    return writer.write (OPEN_TAG);
  }
  
  /**
   Write an ending tag (aka '/').
  
   @return True unless an I/O error. 
  */
  public boolean endingTag () {
    return writer.write (END_TAG);
  }
  
  /**
   Write a closing tag (aka '>'). 
  
   @return True unless an I/O error. 
  */
  public boolean closeTag () {
    return writer.write (CLOSE_TAG);
  }

}
