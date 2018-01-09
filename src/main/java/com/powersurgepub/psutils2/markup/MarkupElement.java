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

package com.powersurgepub.psutils2.markup;

  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.txbio.*;

  import java.util.*;

  import org.xml.sax.*;
  import org.xml.sax.helpers.*;
  
/*
 * MarkupElement.java
 *
 * Created on March 2, 2007, 11:14 AM
 *
 * This class represents a Markup Language (XML/HTML) element and its contents 
 * (or a string of text forming part of an element, but not an entire element). 
 *
 * MarkupElement objects are intended to be nested within each other, 
 * representing the nesting of tags within an XML/HTML document. 
 */

public class MarkupElement 
  extends DefaultHandler {
  
  public    final static char               DOUBLE_QUOTE             = '\"';
  
  public    static final  Attributes        EMPTY_ATTRIBUTES = new AttributesImpl();
  
  public    static final  int               NO_HTML = 0;
  public    static final  int               MINIMAL_HTML = 2;
  public    static final  int               OLD_HTML = 4;
  public    static final  int               NEW_HTML = 6;
  
  public    static final  String            NAKED_TEXT = "";
  public    static final  String            ANCHOR     = "a";
  public    static final  String            HREF       = "href";
  public    static final  String            PARAGRAPH  = "p";
  public    static final  String            BREAK      = "br";
  private                 String            name = NAKED_TEXT;
  
  private   AttributesImpl    attributes = new AttributesImpl();
  
  private   StringBuffer      text = new StringBuffer();
  
  public    static final  int               EMPTY   = 0;
  public    static final  int               STARTED = 2;
  public    static final  int               NESTING = 4;
  public    static final  int               CLOSED  = 6;
  private   int               stage   = EMPTY;

  public    static final  int               ROOT    = 0;
  private   int               level   = ROOT;
  
  private   MarkupElement     parent = null;
  
  private   ArrayList         content = new ArrayList();
  
  private   MarkupElement     latestElement = null;
  
  private   MarkupLineWriterState state;
  
  /** Creates a new instance of MarkupElement */
  public MarkupElement() {
  }
  
  public void set (String in, boolean useMarkdown) {

    text = new StringBuffer();
    content = new ArrayList();
    append (in, useMarkdown);
    close();
    // displayEverything();
  }
  
  /**
   Add text to the stored markup. The text is not parsed, and may include
   markup.
   */
  public void append (String in, boolean useMarkdown) {

    if (in.length() > 0) {
      MarkupParser parser = new MarkupParser();
      parser.setInput (in, useMarkdown);
      parser.parse (this);
    }

  } // end method append
  
  public boolean isEmpty() {
    return (content.isEmpty() && text.length() == 0);
  }
  
  public String getText() {
    return text.toString();
  }
  
  /**
   This section of the program contains methods that can be used by an XML 
   parser.
   */
  
  public void startElement (
      String namespaceURI,
      String localName,
      String qualifiedName,
      Attributes attributes) {
    
    if (this.stage == EMPTY && level > ROOT) {
      setName (localName);
      setAttributes (attributes);
    } else {
      if (latestElement == null
          || latestElement.isClosed()) {
        newElement();
      }
      latestElement.startElement 
          (namespaceURI, localName, qualifiedName, attributes);
    }
    checkLatestElement();
  } // end method startElement
  
  public void startElement (String localName, boolean isAttribute) {
    
    if (this.stage == EMPTY && level > ROOT) {
      setName (localName);
    } else {
      if (latestElement == null
          || latestElement.isClosed()) {
        newElement();
      }
      latestElement.startElement (localName, isAttribute);
    }
    checkLatestElement();
  } // end method startElement
  
  /**
   Process characters (text) from the markup parser.
   */
  public void characters (String str) {
    data (str);
  }
  
  public void characters (char [] ch, int start, int length) {
    
    StringBuffer xmlchars = new StringBuffer();
    xmlchars.append (ch, start, length);
    data (xmlchars.toString());
  }
  
  public void characters (StringBuffer xmlchars) {
    
    data (xmlchars.toString());

  } // end method characters
  
  public void data (String xmlchars) {
        
    String trimmed = xmlchars.trim();
    
    if (trimmed.length() > 0) {
      if (name.equals (NAKED_TEXT) && level == 1) {
        setName (PARAGRAPH);
      }

      if (latestElement != null
          && latestElement.getStage() < CLOSED) {
        latestElement.characters (xmlchars);
      } 
      else 
      if (name.equals(NAKED_TEXT) && level > 1) {
        text.append (xmlchars);
        setStage (CLOSED);
      } else {
        newElement();
        latestElement.characters (xmlchars);
        /* if (level >= ROOT) {
          latestElement.setStage (CLOSED);
        } */
      }
      checkLatestElement();
    }

  }
  
  public void ensureTrailingWhitespace() {
    if (text.length() > 0
        && (! Character.isWhitespace(text.charAt(text.length() - 1)))) {
      text.append(' ');
    }
  }
  
  public void ignorableWhitespace (char [] ch, int start, int length) {
    
  }
  
  public void endElement (
      String namespaceURI,
      String localName,
      String qualifiedName) {
    
    if (latestElement != null
        && latestElement.getStage() < CLOSED) {
      latestElement.endElement (namespaceURI, localName, qualifiedName);
    } else {
      setStage (CLOSED);
      if ((! localName.equals (name))
          && parent != null) {
        parent.closeParent (namespaceURI, localName, qualifiedName);
      }
    }
    checkLatestElement();
    
  } // end method
  
  public void endElement (String localName) {
    
    if (latestElement != null
        && latestElement.getStage() < CLOSED) {
      latestElement.endElement (localName);
    } else {
      setStage (CLOSED);
      if ((! localName.equals (name))
          && parent != null) {
        parent.closeParent (localName);
      }
    }
    checkLatestElement();
    
  }
  
  public void closeParent (
      String namespaceURI,
      String localName,
      String qualifiedName) {

    if (level > ROOT) {
      setStage (CLOSED);
      if ((! localName.equals (name))
          && parent != null
          && level > 1) {
        parent.closeParent (namespaceURI, localName, qualifiedName);
      }
    }

  } // end method
  
  public void closeParent (String localName) {

    if (level > ROOT) {
      setStage (CLOSED);
      if ((! localName.equals (name))
          && parent != null
          && level > 1) {
        parent.closeParent (localName);
      }
    }

  } // end method
  
  private void newElement() {
    latestElement = new MarkupElement();
    latestElement.setParent (this);
    latestElement.setLevel (level + 1);
  }
  
  /**
   This method should be called after passing one or more elements and character
   strings, to ensure that this the passed content has been appropriately saved
   and linked. 
   */
  public void close () {
    if (latestElement != null) {
      latestElement.close();
      addLatestElement();
    }
    setStage (CLOSED);
  }
  
  /**
   Check latest child element of this element to see if it is
   ready to be filed. 
   */
  private void checkLatestElement() {

    if (latestElement != null
        && latestElement.isClosed()) {
      addLatestElement();
    }
  }
  
  private void addLatestElement () {
    content.add (latestElement);
    latestElement = null;
    if (stage < NESTING) {
      stage = NESTING;
    }
  }
  
  public String getFirstFifty () {
    String firstText = getFirstText();
    int end = firstText.length();
    if (firstText.length() >= 50) {
      end = 50;
      char c = ' ';
      for (int i = 49; i > 24; i--) {
        c = firstText.charAt (i);
        if (c == ' ' && end == 50) {
          end = i;
        }
        else
        if (c == ',' || c == '-' || c == '.' || c == ';') {
          end = i;
        }
      } // end for each character from position 49 to 24
    } // end if firstText length greater than or equal to 50
    return firstText.substring (0, end);
  }
  
  public String getFirstText () {
    StringBuffer firstText = new StringBuffer();
    if (text.length() > 0) {
      firstText.append (text);
    } 
    int i = 0;
    while (firstText.length() == 0
        && i < content.size()) {
      MarkupElement next = (MarkupElement)content.get (i);
      firstText.append (next.getFirstText());
      i++;
    }
    return firstText.toString();
  }
  
  /**
   Build a string that can be used as a short and sweet handle to determine
   if one markup element is anything like another. Current strategy is to 
   concatenate the lower-case representation of the first xx characters 
   of each of the first words until the signature reaches a sufficient length. 
   */
  public String getSignature (
      int leadingLettersPerWord, 
      int sufficientLength
      ) {
    
    // Process this element's text
    String basicText = toString (NO_HTML, false);
    StringBuffer signature = new StringBuffer();
    int i = 0;
    while ((signature.length() < sufficientLength)
        && (i < basicText.length())) {
      // Process next word in text
      boolean wordDone = false;
      int wordChars = 0;
      while (! wordDone) {
        char c = basicText.charAt (i);
        if (Character.isLetter (c)) {
          if (wordChars < leadingLettersPerWord) {
            signature.append (Character.toLowerCase (c));
          }
          wordChars++;
        } else {
          // Character is not a letter
          if (c == '\'') {
            // just skip an apostrophe
          } else {
            if (wordChars > 0) {
              wordDone = true;
            }
          }
        } // end if character is not a letter
        i++;
        if (i >= basicText.length()) {
          wordDone = true;
        }
      } // end of processing next word
    } // end of processing text
    
    return signature.toString();
    
  } // end getSignature method
  
  /**
   Build a string that contains lower-case representations of all words in all
   text, concatenated without separators. Intended to help identify equality. 
   */
  public void getWords (
      StringBuffer words
      ) {
    
    // Process this element's text
    int i = 0;
    while (i < text.length()) {
      char c = text.charAt (i);
      if (Character.isLetter (c)) {
        words.append (Character.toLowerCase (c));
      }
      i++;
    } // end of processing text
    
    // Process child elements
    i = 0; 
    while (i < content.size()) {
      MarkupElement next = (MarkupElement)content.get (i);
      next.getWords (words);
      i++;
    } // end while processing more children
    
  } // end getWords method
  
  public void displayEverything () {
    if (level == ROOT) {
      System.out.println (" ");
      System.out.println ("MarkupElement.displayEverything");
    } 
    StringBuffer indent = new StringBuffer();
    for (int i = 0; i < level; i++) {
      indent.append ("  ");
    }
    System.out.println (indent.toString()
        + "Level = " 
        + String.valueOf(level)
        + " name = " + name
        + " content items = " + String.valueOf (content.size()
        + " attributes = " + String.valueOf (attributes.getLength())));
    for (int i = 0; i < attributes.getLength(); i++) {
      System.out.println (indent.toString()
          + attributes.getLocalName (i)
          + "=\""
          + attributes.getValue (i)
          + "\"");
    }
    System.out.println (indent.toString()
        + " Text = " + text.toString());
    for (int i = 0; i < content.size(); i++) {
      MarkupElement next = (MarkupElement)content.get (i);
      next.displayEverything();
    } 
  }
  
  public void writeMarkupWithTagWriter (MarkupTagWriter writer) {
    
    boolean emptyTag = false;
    if (text.length() == 0
        && content.size() == 0) {
      emptyTag = true;
    }
    
    if (! name.equals (NAKED_TEXT)) {
      writer.writeStartTag (
        "",
        name,
        "",
        attributes,
        emptyTag);
    } // end if need to write starting tag
    
    if (text.length() > 0) {
      writer.writeContent (text.toString());
    } // end if we need to write text as content
    
    for (int i = 0; i < content.size(); i++) {
      MarkupElement next = (MarkupElement)content.get (i);
      next.writeMarkupWithTagWriter (writer);
    }
    
    if ((! name.equals (NAKED_TEXT))
        && (! emptyTag)) {
        writer.writeEndTag (
            "",
            name,
            "");
    } // end if we need to write an ending tag
  } // end method writeMarkup
 
  public void writeMarkup (MarkupWriter markupWriter) {
    
    boolean emptyTag = false;
    if (text.length() == 0
        && content.size() == 0) {
      emptyTag = true;
    }
    
    if (! name.equals (NAKED_TEXT)) {
      if (name.equalsIgnoreCase(ANCHOR)) {
        int i = 0;
        boolean hrefFound = false;
        while (i < attributes.getLength() && (! hrefFound)) {
          String attrName = attributes.getLocalName(i);
          String attrValue = attributes.getValue(i);
          if (attrName.equalsIgnoreCase(HREF)) {
            markupWriter.start(
                name, 
                "", 
                attrValue, 
                false);
            hrefFound = true;
          } else {
            i++;
          }
        }
      } else {
        markupWriter.start (
          name,
          "",
          "",
          false);
      }
    } // end if need to write starting tag
    
    if (text.length() > 0) {
      markupWriter.cleanAndWrite (text.toString());
    } // end if we need to write text as content
    
    for (int i = 0; i < content.size(); i++) {
      MarkupElement next = (MarkupElement)content.get (i);
      next.writeMarkup (markupWriter);
    }
    
    if ((! name.equals (NAKED_TEXT))
        && (! emptyTag)) {
        markupWriter.end (
            name,
            "");
    } // end if we need to write an ending tag
  } // end method writeMarkup 
  
  /**
   Write output markup using the MarkupLineWriter interface.
   */
  public void writeMarkupLines (
      TextLineWriter writer,
      int     htmlLevel,
      boolean htmlEntities,
      int     startingIndent,
      int     indentPerLevel) {
    
    if (parent == null) {
      // displayEverything();
      state = new MarkupLineWriterState
        (writer, startingIndent, indentPerLevel);
    } else {
      state = parent.getState();
    }
    
    StringBuffer pending = new StringBuffer();
    
    boolean breakBefore = true;
    boolean breakAfter = true;
    
    // Determine line breaks around tag
    if (name.equals ("a")
        || name.equals ("i")
        || name.equals ("b")
        || name.equals ("cite")
        || name.equals ("em")
        || name.equals ("strong")) {
      breakBefore = false;
      breakAfter = false;
    } 
    else
    if (name.equals ("br")) {
      breakBefore = false;
    } 
    
    // Write Starting tag
    if (isNakedText()) {
      // no starting tag
    }
    else
    if ((isParagraph() || isBreak()) && htmlLevel <= MINIMAL_HTML) {
      // no starting tag
    }
    else
    if (htmlLevel <= NO_HTML) {
      // no starting tag
    } else {
      if (breakBefore) {
        // start a new line
        state.requestNewLine ();
      }
      state.append ("<");
      state.append (name);
      for (int i = 0; i < attributes.getLength(); i++) {
        writeAttribute (attributes.getLocalName(i), attributes.getValue(i));
      }
      if (isEmptyTag() && htmlLevel >= NEW_HTML && (! state.endsWithSlash())) {
        state.append (" /");
      }
      state.append (">");
      
      state.writeStr();

      if (breakAfter) {
        state.requestNewLine();
      }
      if (! isEmptyTag() && breakBefore) {
        state.moreIndent();
      }
    } // end if need to write starting tag
    
    // write text
    if (text.length() > 0) {
      state.writeText (text, htmlEntities);
    } // end if we need to write text as content
    
    // write content
    int paraCount = 0;
    for (int i = 0; i < content.size(); i++) {
      MarkupElement next = (MarkupElement)content.get (i);
      if (htmlLevel <= MINIMAL_HTML
          && next.isParagraph()
          && paraCount > 0) {
        state.requestNewLine();
        state.newLine();
      }

      next.writeMarkupLines (
          writer,
          htmlLevel,
          htmlEntities,
          state.indent,
          indentPerLevel);
      if (next.isParagraph()) {
        paraCount++;
      }
    }

    if (name.equals("br") && htmlLevel <= MINIMAL_HTML) {
      state.writeText ("  ", htmlEntities);
      state.requestNewLine();
    }
    
    // write ending tag
    if ((! isNakedText())
        && (! isEmptyTag())) {
      if (breakBefore) {
        // start a new line
        state.lessIndent();
        state.requestNewLine ();
      }
      if ((isParagraph() || isBreak()) && htmlLevel <= MINIMAL_HTML) {
        // no ending tag
      }
      else
      if (htmlLevel <= NO_HTML) {
        // no ending tag
      } else {
        state.append ("</" 
          + name 
          + ">");
      }
      state.writeStr();

      if (breakAfter) {
        state.requestNewLine();
      }
      if (isParagraph()) {
        state.writeStr();
        state.requestNewLine();
      }
    } // end if we need to write an ending tag
    
  } // end method writeMarkupLines
  
  private void writeAttribute (String name, String value) {
    if (name.trim().length() > 0) {
      state.append (" " + name);
      String valueRaw = StringUtils.removeQuotes(value);
      if (valueRaw.length() > 0) {
        state.append ("=\"" + valueRaw + "\"");
      } // end if we have a value
    } // end if we have a name
  } // end method
  
  /**
   Add quotation marks surrounding paragraphs of text.
   */
  public void addQuotes (MarkupQuoter quoter) {
    
    boolean moreThanWhiteSpace = (text.toString().trim().length() > 0);
    
    if (this.isParagraph()) {
      quoter.setFirstText (true);
      quoter.setLastText (this);
    }
    
    if (quoter.isFirstText() && moreThanWhiteSpace) {
      removeLeadingWhitespace();
      if (text.charAt (0) != DOUBLE_QUOTE) {
        text.insert (0, DOUBLE_QUOTE);
      }
      quoter.setFirstText (false);
    }
    
    if (moreThanWhiteSpace) {
      quoter.setLastText (this);
    }
    
    int lastEntry = content.size() - 1;
    for (int i = 0; i < content.size(); i++) {
      MarkupElement next = (MarkupElement)content.get (i);
      if (next.isParagraph()) {
        quoter.setLastParagraph (i >= lastEntry);
      }
      next.addQuotes (quoter);
    }
    
    if (this.isParagraph() && quoter.isLastParagraph()) {
      quoter.getLastText().addClosingQuote();
    }
    
  }
  
  public void addClosingQuote () {
    removeTrailingWhitespace();
    int i = text.length() - 1;
    if (i < 0 || text.charAt(i) != DOUBLE_QUOTE) {
      text.append (DOUBLE_QUOTE);
    }
  }
  
  /**
   Remove quotation marks surrounding paragraphs of text.
   */
  public void removeQuotes (MarkupQuoter quoter) {
    
    boolean moreThanWhiteSpace = (text.toString().trim().length() > 0);
    
    if (this.isParagraph()) {
      quoter.setFirstText (true);
      quoter.setLastText (this);
    }
    
    if (quoter.isFirstText() && moreThanWhiteSpace) {
      removeLeadingWhitespace();
      if (text.length () > 0 && text.charAt (0) == DOUBLE_QUOTE) {
        text.deleteCharAt (0);
      }
      quoter.setFirstText (false);
    }
    
    if (moreThanWhiteSpace) {
      quoter.setLastText (this);
    }
    
    for (int i = 0; i < content.size(); i++) {
      MarkupElement next = (MarkupElement)content.get (i);
      next.removeQuotes (quoter);
    }
    
    if (this.isParagraph()) {
      quoter.getLastText().removeClosingQuote();
    }
    
  }
  
  public void removeClosingQuote () {
    removeTrailingWhitespace();
    int i = text.length() - 1;
    if (i < 0 || text.charAt(i) == DOUBLE_QUOTE) {
      text.deleteCharAt (i);
    }
  }
  
  private void removeLeadingWhitespace () {
    while (text.length() > 0 && Character.isWhitespace (text.charAt (0))) {
      text.deleteCharAt (0);
    }
  }
  
  private void removeTrailingWhitespace () {
    while (text.length() > 0 && Character.isWhitespace (text.charAt (text.length() - 1))) {
      text.deleteCharAt(text.length() - 1);
    }
  }
  
  public void setLevel (int level) {
    if (level >= 0) {
      this.level = level;
    }
  }
  
  public int getLevel () {
    return level;
  }
  
  public void setParent (MarkupElement parent) {
    this.parent = parent;
  }
  
  public MarkupElement getParent () {
    return parent;
  }
  
  public void setName (String name) {
    this.name = name;
    setStage (STARTED);
    // if (MarkupUtils.tagEmpty (name)) {
    //   setStage (CLOSED);
    // }
  }
  
  public String getName() {
    return name;
  }
    
  public void setAttributes (Attributes attributes) {

    this.attributes.setAttributes (attributes);

  }
  
  public void setStage (int stage) {
    if (stage >= EMPTY && stage <= CLOSED) {
      this.stage = stage;
    }
  }
  
  public int getStage () {
    return stage;
  }
  
  public String toString () {
    return toString (NEW_HTML, true);
  }
  
  public String toString (int htmlLevel, boolean htmlEntities) {
    
    MarkupStringMaker maker = new MarkupStringMaker ();
    writeMarkupLines (maker, htmlLevel, htmlEntities, 0, 0);
    return maker.toString();
  }
  
  public MarkupLineWriterState getState () {
    return state;
  }
  
  public boolean isRoot () {
    return (level == ROOT);
  }

  public boolean isParagraph () {
    return (name.equals (PARAGRAPH));
  }

  public boolean isBreak () {
    return (name.equals (BREAK));
  }
  
  public boolean isNakedText () {
    return (name.equals (NAKED_TEXT));
  }
  
  public boolean isEmptyTag () {
    return (text.length() == 0 && content.size() == 0);
  }
  
  public boolean isClosed () {
    return (stage >= CLOSED);
  }
  
}
