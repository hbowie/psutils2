/*
 * Copyright 2013 - 2013 Herb Bowie
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
package com.powersurgepub.psutils2.mkdown;

/**
 This class represents a single symbol (aka punctuation) found at the beginning
 of a line of markdown. Note that a single line may have zero to many of these.

 @author Herb Bowie
 */
public class LeadingSymbol {
  
  private               char          symbolType = BLANK_LINE;
  public  static final  char            NO_SPECIAL    = 'n';
  public  static final  char            BLANK_LINE    = ' ';
  public  static final  char            H1_UNDERLINES = '=';
  public  static final  char            H2_UNDERLINES = '-';
  public  static final  char            BYLINE        = 'b';
  public  static final  char            METADATA      = 'm';
  public  static final  char            NEW_PARAGRAPH = 'p';
  public  static final  char            H1            = '1';
  public  static final  char            H2            = '2';
  public  static final  char            H3            = '3';
  public  static final  char            H4            = '4';
  public  static final  char            H5            = '5';
  public  static final  char            H6            = '6';
  public  static final  char            BLOCK_QUOTE   = 'q';
  public  static final  char            UNORDERED_LIST_ITEM = 'u';
  public  static final  char            ORDERED_LIST_ITEM = 'o';
  
  private               int           indentLevels = 0;
  
  public LeadingSymbol() {
    
  }
  
  public char getType() {
    return symbolType;
  }
  
  public void setType (char c) {
    switch (c) {
      case '>': 
        symbolType = BLOCK_QUOTE;
        break;
      case '-':
        symbolType = H2_UNDERLINES;
        break;
      case '=': 
        symbolType = H1_UNDERLINES;
        break;
      case ' ':
        symbolType = BLANK_LINE;
        break;
      default:
        symbolType = c;
    }
  }
  
  public void setTypeToByline() {
    symbolType = BYLINE;
  }
  
  public void setTypeToMetadata() {
    symbolType = METADATA;
  }
  
  public boolean isH1() {
    return (symbolType == H1_UNDERLINES);
  }
  
  public boolean isH2() {
    return (symbolType == H2_UNDERLINES);
  }
  
  public void setIndentLevels(int indentLevels) {
    this.indentLevels = indentLevels;
  }
  
  public void incrementIndentLevels() {
    indentLevels++;
  }
  
  public int getIndentLevels() {
    return indentLevels;
  }
  
  public boolean isBlankLine() {
    return (symbolType == BLANK_LINE);
  }
  
  public boolean isUnderlines() {
    return (symbolType == H1_UNDERLINES
        || symbolType == H2_UNDERLINES);
  }
  
  public boolean isByLine() {
    return (symbolType == BYLINE);
  }
  
  public char getUnderlineChar() {
    if (isUnderlines()) {
      return symbolType;
    } else {
      return ' ';
    }
  }
  
  public boolean isMetadata() {
    return symbolType == METADATA;
  }
  
  public void setHeadingLevel(int headingLevel) {
    if (headingLevel >= 1
        && headingLevel <= 6) {
      
      switch (headingLevel) {
        case 1: symbolType = H1;
                break;
        case 2: symbolType = H2;
                break;
        case 3: symbolType = H3;
                break;
        case 4: symbolType = H4;
                break;
        case 5: symbolType = H5;
                break;
        case 6: symbolType = H6;
                break;
      }
    } // end if we have a good heading level
  }
  
  public boolean isHeading() {
    return (symbolType >= H1
        && symbolType <= H6);
  }
  
  public int getHeadingLevel() {
    switch (symbolType) {
      case H1: 
        return 1;
      case H2:
        return 2;
      case H3:
        return 3;
      case H4:
        return 4;
      case H5:
        return 5;
      case H6:
        return 6;
      default:
        return 0;
    }
  }
  
  public boolean isListItem() {
    return (isUnorderedListItem() || isOrderedListItem());
  }
  
  public void setUnorderedListItem() {
    symbolType = UNORDERED_LIST_ITEM;
  }
  
  public void setOrderedListItem() {
    symbolType = ORDERED_LIST_ITEM;
  }
  
  public boolean isUnorderedListItem() {
    return (symbolType == UNORDERED_LIST_ITEM);
  }
  
  public boolean isOrderedListItem() {
    return (symbolType == ORDERED_LIST_ITEM);
  }
  
  /**
   Display internal data for testing purposes. 
  */
  public void display() {
    System.out.println("  LeadingSymbol.display");
    System.out.println("    symbol type = " + symbolType);
    if (indentLevels > 0) {
      System.out.println("    indent levels: " + String.valueOf(indentLevels));
    }
  }

}
