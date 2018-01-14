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

  import com.powersurgepub.psutils2.basic.*;

/**
 A single character from an XML/HTML file. Used with input processing.

 @author Herb Bowie
 */

public class MarkupCharacter {

  char 						character;
  char						originalCharacter;
  MarkupContext	  context;
  boolean 				whiteSpace = false;
  boolean 				endsWord = false;
  boolean 				endsField = false;
  boolean 				endsTag = false;
  boolean					translatedEntity = false;
  boolean         lonelyAmpersand = false;
  boolean         docType = false;

  /** Creates a new instance of MarkupCharacter */

  public MarkupCharacter (MarkupContext context) {

    this.context = context;
    character = ' ';
    originalCharacter = ' ';
    evaluateCharacter();
  }

  /**
     Sets new field type.

     @param field type.
   */

  void setFieldType (int fieldType) {
    context.fieldType = fieldType;
    evaluateCharacter();
  }

  

  /**
     Sets new character value.

     @param character to be evaluated.
   */

  void setCharacter (char character) {
    this.character = character;
    originalCharacter = character;
    evaluateCharacter();
  }

  /**
     Evaluate character.
   */

  void evaluateCharacter () {

    character = originalCharacter;

    // reset result flags
    whiteSpace = false;
    endsWord = false;
    endsField = false;
    endsTag = false;
    translatedEntity = false;
    lonelyAmpersand = false;

    // treat tabs, carriage returns and line feeds like spaces (unless a comment)
    if (character == GlobalConstants.CARRIAGE_RETURN
        || character == GlobalConstants.LINE_FEED
        || character == GlobalConstants.TAB) {
      if (context.fieldType != MarkupContext.COMMENT) {
        character = ' ';
      }
    }

    // end of file condition
    if (context.inputIsAtEnd) {
      whiteSpace = true;
      endsWord = true;
      endsField = true;
      endsTag = true;
      context.entityCharCount = 0;
      this.character = ' ';
    } 
    else

    // possible start of comment
    if (context.fieldType == MarkupContext.TAG_NAME
        && context.word.equals ("!--")) {
      endsWord = true;
      endsField = true;
      if (character == ' ') {
        whiteSpace = true;
      }
    } 
    else

    // possible start of HTML tag
    if (character == '<') {
      if (context.fieldType == MarkupContext.TEXT) {
        endsWord = true;
        endsField = true;
        context.entityCharCount = 0;
      }
    }
    else

    // possible end of HTML tag
    if (character == '>') {
      if (context.fieldType == MarkupContext.TAG_NAME
          || context.fieldType == MarkupContext.ATTRIBUTE_NAME
          || (context.fieldType == MarkupContext.ATTRIBUTE_VALUE
            && (! context.quoted))
          || (context.fieldType == MarkupContext.DOCTYPE
            && (! context.quoted))
          || (context.fieldType == MarkupContext.COMMENT
            && context.lastCharacter1 == '-'
            && context.lastCharacter2 == '-')) {
        endsWord = true;
        endsField = true;
        endsTag = true;
      }
    }

    else
    // possible attribute value indicator
    if (character == '=') {
      if (context.fieldType == MarkupContext.ATTRIBUTE_NAME) {
        endsWord = true;
        endsField = true;
      }
    }

    else
    // possible end of quoted attribute value
		if (character == GlobalConstants.DOUBLE_QUOTE
        && context.quoted
        && (context.fieldType == MarkupContext.ATTRIBUTE_VALUE
          || context.fieldType == MarkupContext.DOCTYPE)) {
      endsWord = true;
      endsField = true;
    }
    else

    // possible white space
    if (character == ' ') {
      if (context.fieldType == MarkupContext.COMMENT
          || (context.fieldType == MarkupContext.ATTRIBUTE_VALUE
            && context.quoted)
          || (context.fieldType == MarkupContext.DOCTYPE
            && context.quoted)) {
        whiteSpace = false;
      }
      else {
        whiteSpace = true;
      }
    }

    // Optionally translate character entities
    if ((! context.inputIsAtEnd)
        && context.entityTranslation
        && context.fieldType == MarkupContext.TEXT) {
      if (context.entityCharCount > 0) {
        if (character == ' ') {
          context.entityCharCount = 0;
          whiteSpace = true;
          endsWord = true;
          lonelyAmpersand = true;
        }
        else
        if (character == ';') {
          translateEntity();
          context.entityCharCount = 0;
        } else {
          context.entityCharCount++;
          if (context.entityCharCount == 2
              && character == '#') {
            context.entityNumeric = true;
          } else { // character is part of entity identifying string
            context.entityMnemonic.append (character);
            int number = Character.getNumericValue (character);
            if (context.entityNumeric
                && number >= 0) {
              context.entityInt = (context.entityInt * 10) + number;
            }
          }  // end if character is part of entity identifying string        
        } // end if translation still going
      } // end if already in process of translation
      else

      if (character == '&') {
        context.entityCharCount = 1;
        context.entityNumeric = false;
        context.entityInt = 0;
        context.entityMnemonic.setLength(0);
      } // end if start of character entity
    } // end if possibly translating character entities

    // check to see if white space should end a word or a field
    if (character == ' ') {
      if (context.fieldType == MarkupContext.TEXT) {
        endsWord = true;
      }
      else
      if (context.fieldType >= MarkupContext.TAG_NAME
          && (! context.quoted)) {
        endsWord = true;
        endsField = true;
      }
    }

    // If we've ended a field, make sure we ended the word too
    if (endsField) {
      endsWord = true;
    }
  } // end evaluateCharacter method

  private void translateEntity () {
    String mnem = context.entityMnemonic.toString();
    int number = context.entityInt;
    int charCount = context.entityCharCount;
    if (mnem.equals ("amp") || number == 38) {
      character = '&';
    } else
    if (mnem.equals ("gt") || number == 62) {
      character = '>';
    } else
    if (mnem.equals ("lt") || number == 60) {
      character = '<';
    } else
    if (mnem.equals ("quot") || number == 34) {
      character = '"';
    } else
    if (mnem.equals ("nbsp") || number == 160) {
      character = ' ';
      whiteSpace = true;
    } else { // if all else fails, make it a space
     character = ' ';
     whiteSpace = true;
    }
    translatedEntity = true;
  } // end translateEntity method

  /**
     Returns HTML character as a String.

     @return String representation.
   */
  public String toString() {
    StringBuffer work = new StringBuffer ("");
    work.append (character);
    work.append (" - field type " + String.valueOf (context.fieldType));
    work.append (", quoted? " + String.valueOf (context.quoted));
    work.append (", white space? " + String.valueOf (whiteSpace));
    work.append (", ends word? " + String.valueOf (endsWord));
    work.append (", ends field? " + String.valueOf (endsField));
    work.append (", ends tag? " + String.valueOf (endsTag));
    work.append (", translated entity? " + String.valueOf (translatedEntity));
    return work.toString();
  }

}

