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

/**
   Context for a single character from an HTML file. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
 */

class HTMLContext {

  final static int    TEXT = 0;
  final static int    COMMENT = 1;
  final static int    TAG_NAME = 2;
  final static int    ATTRIBUTE_NAME = 3;
  final static int    ATTRIBUTE_VALUE = 4;
  final static char   SPACE = ' ';
  final static String TEXTILE = "Textile";
  final static String MARKDOWN = "Markdown";

  private           HTMLFile					file;
  String            type = "html";
  boolean           textile = false;
  boolean           markdown = false;
  int 							fieldType = 0;
  boolean 					quoted = false;
  boolean           preformatted = false;
  char              startQuoteChar = ' ';
  char 							lastCharacter1 = ' ';
  char 							lastCharacter2 = ' ';
  StringBuffer      extraCharacters = new StringBuffer();
  StringBuffer 			word = new StringBuffer("");
  StringBuffer			field = new StringBuffer("");
  int								listLevel = 0;
  int								headingLevel = 0;
  String						listItemTag = "";
  
  /** Is there an active dt (definition term) above us? */
  boolean           defTermActive = false;
  
  /** Indicates whether entities should be translated or left alone. */
  boolean           entityTranslation = true;
  
  /** 
     Count of number of entity characters found in current string, with the
     '&' being 1.
   */
  int								entityCharCount = 0;
  
  /**
     Indicates whether this entity is identified by a number (true)
     or a mnemonic (false).
   */
  boolean						entityNumeric = false;
  
  /**
     The characters that define the entity, if a mnemonic.
   */
  StringBuffer  		entityMnemonic = new StringBuffer ("");
  
  /**
     The number identifying the entity, if numeric.
   */
  int								entityInt = 0;

  /**
     The last open block tag.
   */
  String            lastOpenBlock = "";

  /** The next block tag to be used. */
  String            nextBlock = "p";

  /** Was the last line blank? */
  boolean           lastLineBlank = true;

  /** Do we have a block quote in progress? */
  boolean           blockQuoting = false;

  /** last sequence of list characters at beginning of line */
  String            lastListChars = "";

  /**
     Sole constructor.
   */
  HTMLContext () {

  }

  public void setType (String type) {
    this.type = type;
    textile = (type.equals (TEXTILE));
    markdown = (type.equals (MARKDOWN));
  }

  public boolean isTextile () {
    return textile;
  }

  public boolean isMarkdown () {
    return markdown;
  }
  
  public void setExtraCharacter (char extraCharacter) {
    extraCharacters.append (extraCharacter);
  }

  public void setExtraCharacters (String extraString) {
    extraCharacters.append (extraString);
  }
  
  public boolean hasExtraCharacter () {
    return (extraCharacters.length() > 0);
  }
  
  public char getExtraCharacter () {
    char returnChar = SPACE;
    if (hasExtraCharacter()) {
      returnChar = extraCharacters.charAt (0);
      extraCharacters.deleteCharAt (0);
    }
    return returnChar;
  }

  public HTMLFile getFile() {
    return file;
  }

  public void setFile (HTMLFile file) {
    this.file = file;
  }
  
  /**
     Returns HTML context as a String.
    
     @return String representation.
   */
  public String toString() {
    StringBuilder work = new StringBuilder ("");
    work.append ("field type" + String.valueOf (fieldType));
    work.append (", quoted? " + String.valueOf (quoted));
    return work.toString();
  }

}
