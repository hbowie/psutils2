/*
 * Copyright 1999 - 2016 Herb Bowie
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

package com.powersurgepub.psutils2.txbmodel;

/**
 Represents a particular type of data stored in a TextData node.
 A type may also be referred to as a tag (especially when used as part of a
 markup language.

 @author Herb Bowie
 */
public class TextType {

  public  static final String LOCATION_PREFIX = "location-";
  public  static final String LOCATION_FILE   = "location-file";
  public  static final String LOCATION_URL    = "location-url";
  public  static final String LOCATION_NEW    = "location-new";

  public  static final String RECORD      = "record";
  public  static final String NAKED_TEXT  = "";
  public  static final String XML         = "?xml";
  public  static final String DOCTYPE     = "!doctype";
  public  static final String COMMENT     = "!--";
  public  static final String BLOCKQUOTE  = "blockquote";
  public  static final String TARGET      = "target";
  public  static final String CITE        = "cite";
  public  static final String AREA        = "area";
  public  static final String CLOSING     = "/";
  public  static final String META        = "meta";

  public static final String ANCHOR         = "a";
  public static final String BODY           = "body";
  public static final String BOLD           = "b";
  public static final String BLOCK_QUOTE    = "blockquote";
  public static final String BREAK          = "br";
  public static final String CITATION       = "cite";
  public static final String CLASS          = "class";
  public static final String CODE           = "code";
  public static final String DEFINITION_LIST = "dl";
  public static final String DEFINITION_TERM = "dt";
  public static final String DEFINITION_DEF = "dd";
  public static final String DIV            = "div";
  public static final String EMPHASIS       = "em";
  public static final String FOLDED         = "folded";
  public static final String FOOTER         = "footer";
  public static final String HEADING_PREFIX = "h";
  public static final String HEAD           = "head";
  public static final String HREF           = "href";
  public static final String HEADING_1      = "h1";
  public static final String HEADING_2      = "h2";
  public static final String HEADING_3      = "h3";
  public static final String HEADING_4      = "h4";
  public static final String HEADING_5      = "h5";
  public static final String HEADING_6      = "h6";
  public static final String HORIZONTAL_RULE = "hr";
  public static final String HTML           = "html";
  public static final String ID             = "id";
  public static final String ITALICS        = "i";
  public static final String IMAGE          = "img";
  public static final String PARAGRAPH      = "p";
  public static final String PRE            = "pre";
  public static final String SCRIPT         = "script";
  public static final String SPAN           = "span";
  public static final String STRONG         = "strong";
  public static final String STYLE          = "style";
  public static final String TITLE          = "title";
  public static final String ORDERED_LIST   = "ol";
  public static final String TOC            = "toc";
  public static final String UNORDERED_LIST = "ul";
  public static final String LIST_ITEM      = "li";

  private       String                  type      = NAKED_TEXT;

  // Constants for use with OPML
  public static final String OPML           = "opml";
  public static final String OUTLINE        = "outline";
  public static final String TEXT           = "text";
  public static final String TYPE           = "type";
  public static final String URL            = "url";
  public static final String VERSION        = "version";
  public static final String VERSION_NUMBER = "1.0";

  // Constants for use with Textile
  public static final String TEXTILE_BLOCK_QUOTE    = "bq";
  public static final String TEXTILE_CITATION       = "??";
  public static final String TEXTILE_EMPHASIS       = "_";
  public static final String TEXTILE_ITALICS        = "__";
  public static final String QUOTE_MACRO    = "{quote}";

  public static final String      SRC         = "src";
  public static final String      LINK        = "link";
  public static final String      REL         = "rel";
  public static final String      STYLESHEET  = "stylesheet";
  public static final String      TEXT_CSS    = "text/css";
  public static final String      COMPACT     = "compact";
  public static final String      NAME        = "name";
  public static final String      CONTENT     = "content";
  public static final String      HTML_CLASS  = "class";
  public static final String      HTML_ID     = "id";
  public static final String      HTML_LINK   = "a";

  public static final String      NO_STYLE    = "";

  public static final String      TABLE       = "table";
  public static final String      CAPTION     = "caption";
  public static final String      TABLE_ROW   = "tr";
  public static final String      TABLE_HEADER = "th";
  public static final String      TABLE_DATA  = "td";
  public static final String      COLUMN_SPAN = "colspan";
  public static final String      WIDTH       = "width";
  
  public static final String      DATA_TOGGLE = "data-toggle";
  public static final String      DATA_TARGET = "data-target";
  public static final String      CONTAINER   = "container";
  public static final String      ROW         = "row";
  public static final String      SPAN1       = "span1";
  public static final String      SPAN2       = "span2";
  public static final String      SPAN3       = "span3";
  public static final String      SPAN4       = "span4";
  public static final String      SPAN5       = "span5";
  public static final String      SPAN6       = "span6";
  public static final String      SPAN7       = "span7";
  public static final String      SPAN8       = "span8";
  public static final String      SPAN9       = "span9";
  public static final String      SPAN10      = "span10";
  public static final String      SPAN11      = "span11";
  public static final String      SPAN12      = "span12";
  

  private       boolean                 attribute = false;

  public TextType () {
  }

  public TextType(String type) {
    this.type = type;
  }

  /**
   Set the type of the text node. This is a label identifying the type of node.
   In the case of HTML, this would be the tag or attribute.

   @param type The type of the node.
   */
  public void setType (String type) {
    this.type = type;
  }

  /**
   Return the type of the node.

   @return The type of the node.
   */
  public String getType () {
    return type;
  }

  /**
   Does this type represent a location for the data contained in the tree?

   @return True if the type starts with "location-", otherwise false. 
   */
  public boolean isLocation() {
    return type.startsWith(LOCATION_PREFIX);
  }

  /**
   Is this a break tag?

   @return True if the type is a break.
   */
  public boolean isBreak () {
    return (type.equalsIgnoreCase (BREAK));
  }

  /**
   Is this a block quote?

   @return True if the type is a block quote.
   */
  public boolean isBlockQuote () {
    return (type.equalsIgnoreCase (BLOCKQUOTE));
  }

  /**
   Is this a paragraph?

   @return True if the type is a paragraph.
   */
  public boolean isParagraph () {
    return (type.equalsIgnoreCase (PARAGRAPH));
  }

  /**
   Is this a heading type?

   @return True if the tag/type starts with 'h' and the second (and only other)
           character is a digit.
   */
  public boolean isHeading () {
    return isHeading (type);
  }

  /**
   Is the passed type a heading type?

   @param tag The tag/type to be evaluated.
   @return True if the tag/type starts with 'h' and the second (and only other)
           character is a digit.
   */
  public static boolean isHeading (String tag) {
    return (tag.length() == 2
        && (tag.charAt(0) == 'h' || tag.charAt(0) == 'H')
        && Character.isDigit(tag.charAt(1)));
  }

  /**
   Return the heading level for a heading tag.

   @return Second character of the heading tag, if it is one, otherwise zero.
   */
  public int getHeadingLevel () {
    return getHeadingLevel (type);
  }

  /**
   Return the heading level for a heading tag.

   @return Second character of the heading tag, if it is one, otherwise zero.
   */
  public static int getHeadingLevel (String tag) {
    if (isHeading(tag)) {
      return Character.getNumericValue(tag.charAt(1));
    } else {
      return 0;
    }
  }

  /**
   Is this a comment type?

   @return True if the tag/type is a comment.
   */
  public boolean isComment() {
    return (type.equals (COMMENT));
  }

  /**
   Is this text without any identifying type?

   @return True if the type is null.
   */
  public boolean isNakedText () {
    return (type.equals (NAKED_TEXT));
  }

  /**
   Is this an anchor/hyperlink type?

   @return True if the tag/type represents an anchor.
   */
  public boolean isAnchor () {
    return (type.equalsIgnoreCase (ANCHOR));
  }

  /**
   Is this an italics type?

   @return True if the tag/type is italics.
   */
  public boolean isItalics () {
    return (type.equalsIgnoreCase (ITALICS));
  }

  /**
   Is this a citation type?

   @return True if the tag/type represents a citation.
   */
  public boolean isCite () {
    return (type.equalsIgnoreCase (CITE));
  }

  /**
   Should we start a new line before the opening tag?

   @return True if we should start a new line.
   */
  public boolean breakBeforeOpeningTag () {
    return breakBeforeOpeningTag (type);
  }

  /**
   Should we start a new line before the opening tag?

   @return True if we should start a new line.
   */
  public static boolean breakBeforeOpeningTag (String tag) {
    if (tag.equalsIgnoreCase (CITATION)
        || tag.equalsIgnoreCase (ITALICS)
        || tag.equalsIgnoreCase (STRONG)
        || tag.equalsIgnoreCase (EMPHASIS)
        || tag.equalsIgnoreCase (BREAK)
        || tag.equalsIgnoreCase(ANCHOR)) {
      return false;
    } else {
      return true;
    }
  }

  /**
   Should we start a new line after the opening tag?

   @return True if we should start a new line.
   */
  public boolean breakAfterOpeningTag () {
    return breakBeforeClosingTag(type);
  }

  /**
   Should we start a new line after the opening tag?

   @return True if we should start a new line.
   */
  public static boolean breakAfterOpeningTag (String tag) {
    if (tag.equalsIgnoreCase (CITATION)
        || tag.equalsIgnoreCase (CODE)
        || tag.equalsIgnoreCase (ITALICS)
        || tag.equalsIgnoreCase (STRONG)
        || tag.equalsIgnoreCase (EMPHASIS)
        || tag.equalsIgnoreCase (LIST_ITEM)
        || tag.equalsIgnoreCase (DEFINITION_TERM)
        || tag.equalsIgnoreCase (DEFINITION_DEF)
        || tag.equalsIgnoreCase (PARAGRAPH)
        || isHeading(tag)
        || tag.equalsIgnoreCase(ANCHOR)
        || tag.equalsIgnoreCase(PRE)) {
      return false;
    } else {
      return true;
    }
  }

  /**
   Should we start a new line before the closing tag?

   @return True if we should start a new line.
   */
  public boolean breakBeforeClosingTag () {
     return breakBeforeClosingTag (type);
  }

  /**
   Should we start a new line before the closing tag?

   @return True if we should start a new line.
   */
  public static boolean breakBeforeClosingTag (String tag) {
     if (tag.equalsIgnoreCase (CITATION)
        || tag.equalsIgnoreCase (ITALICS)
        || tag.equalsIgnoreCase (STRONG)
        || tag.equalsIgnoreCase (EMPHASIS)
        || tag.equalsIgnoreCase (LIST_ITEM)
        || tag.equalsIgnoreCase (DEFINITION_TERM)
        || tag.equalsIgnoreCase (DEFINITION_DEF)
        || tag.equalsIgnoreCase (PARAGRAPH)
        || isHeading(tag)
        || tag.equalsIgnoreCase(ANCHOR)
        || tag.equalsIgnoreCase(PRE)
        || tag.equalsIgnoreCase (CODE)) {
      return false;
    } else {
      return true;
    }
  }

  /**
   Should we start a new line after the closing tag?

   @return True if we should start a new line.
   */
  public boolean breakAfterClosingTag () {
    return breakAfterClosingTag (type);
  }

  /**
   Should we start a new line after the closing tag?

   @return True if we should start a new line.
   */
  public static boolean breakAfterClosingTag (String tag) {
    if (tag.equalsIgnoreCase (CITATION)
        || tag.equalsIgnoreCase (ITALICS)
        || tag.equalsIgnoreCase (STRONG)
        || tag.equalsIgnoreCase (EMPHASIS)
        || tag.equalsIgnoreCase(ANCHOR)
        || tag.equalsIgnoreCase (CODE)) {
      return false;
    } else {
      return true;
    }
  }
  
  /**
   Should we write a blank line after the closing tag?

   @return True if we should write a blank line.
   */
  public boolean blankLineAfterClosingTag () {
    return blankLineAfterClosingTag (type);
  }

  /**
   Should we start a new line after the closing tag?

   @return True if we should start a new line.
   */
  public static boolean blankLineAfterClosingTag (String tag) {
    if (tag.equalsIgnoreCase (PARAGRAPH)
        || isHeading(tag)
        || tag.equalsIgnoreCase (UNORDERED_LIST)
        || tag.equalsIgnoreCase (ORDERED_LIST)
        || tag.equalsIgnoreCase (DEFINITION_LIST)
        || tag.equalsIgnoreCase (BLOCK_QUOTE)
        || tag.equalsIgnoreCase (DIV)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   Should we assume that the opening tag is also the closing tag?

   @return True if the opening tag is also the closing tag.
   */
  public boolean isSelfClosingTag () {
    return isSelfClosingTag(type);
  }

  /**
   Should we assume that the opening tag is also the closing tag?

   @return True if the opening tag is also the closing tag.
   */
  public static boolean isSelfClosingTag (String tag) {
    return (tag.equalsIgnoreCase(IMAGE)
        || tag.equalsIgnoreCase(BREAK)
        || tag.equalsIgnoreCase(HORIZONTAL_RULE)
        || tag.equalsIgnoreCase(AREA)
        || tag.equalsIgnoreCase(COMMENT)
        || tag.equalsIgnoreCase(META)
        || tag.equalsIgnoreCase(TOC));
  }

  /**
   Is this an href attribute?

   @return True if this is an href attribute.
   */
  public boolean isAttributeHref () {
    return (attribute && type.equalsIgnoreCase (HREF));
  }

  /**
   Is this the target attribute for an anchor/hyperlink?

   @return True if this is a target attribute.
   */
  public boolean isAttributeTarget () {
    return (attribute && type.equalsIgnoreCase (TARGET));
  }

  /**
   Set the flag indicating whether this data should be treated as an
   attribute.

   @param attribute True if an attribute.
   */
  public void setAttribute (boolean attribute) {
    this.attribute = attribute;
  }

  /**
   Is this an attribute?

   @return True if this is an attribute.
   */
  public boolean isAttribute () {
    return attribute;
  }

  /**
   If this is not an attribute, then treat it as a tag.

   @return True if not an attribute.
   */
  public boolean isTag () {
    return (! attribute);
  }

  /**
   Format the data as a single string.

   @return Type, followed by an "a" in parentheses if it is an attribute.
   */
  public String toString() {
    StringBuffer work = new StringBuffer();
    if (type.length() > 0) {
      work.append (type);
      if (attribute) {
        work.append ("(a)");
      }
    }
    return work.toString();
  }


}
