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


/**
 *
 * Used to hold context information while parsing markup.
 */

public class MarkupContext {

  final static int TEXT = 0;
  final static int COMMENT = 1;
  final static int DOCTYPE = 2;
  final static int TAG_NAME = 3;
  final static int ATTRIBUTE_NAME = 4;
  final static int ATTRIBUTE_VALUE = 5;

  boolean           markdown = false;

  boolean           inputIsAtEnd = false;
  int 							fieldType = 0;
  boolean 					quoted = false;
  char 							lastCharacter1 = ' ';
  char 							lastCharacter2 = ' ';
  StringBuffer 			word = new StringBuffer("");
  StringBuffer			field = new StringBuffer("");
  int								listLevel = 0;
  int								headingLevel = 0;
  String						listItemTag = "";
  boolean           lastLineBlank = true;
  boolean           lastLineHTML = false;
  boolean           blockQuoting = false;
  /** last sequence of list characters at beginning of line */
  String lastListChars = "";

  /**
     The last open block tag.
   */
  String            lastOpenBlock = "";

  /** The next block tag to be used. */
  String            nextBlock = "p";

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
     The number identifying the entity, if numeric
   */
  int								entityInt = 0;

  /**
     Sole constructor.
   */

  /** Creates a new instance of MarkupContext */

  public MarkupContext() {

  }

  public void setMarkdown(boolean markdown) {
    this.markdown = markdown;
  }

  public boolean useMarkdown() {
    return markdown;
  }

  

  /**
     Returns HTML context as a String.

     @return String representation.
   */

  public String toString() {
    StringBuffer work = new StringBuffer ("");
    work.append ("field type" + String.valueOf (fieldType));
    work.append (", quoted? " + String.valueOf (quoted));
    return work.toString();
  }

}



