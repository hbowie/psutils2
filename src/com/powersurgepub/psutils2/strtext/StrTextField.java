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

package com.powersurgepub.psutils2.strtext;

/**
 One field definition for YAML, that may consist of a field name and/or a value.

 @author hbowie
 */
public class StrTextField {
  
  private               String              name  = "";
  private               StringBuilder       value = new StringBuilder();
  
  private               int                 indention = 0;
  private               int                 blockIndention = 0;
  private               boolean             withinQuotes = false;
  private               char                quoteChar = ' ';
  private               boolean             blockValue = false;
  private               boolean             blockFolding = true;
  
  /**
   Construct a new YAML Field occurence.
  */
  public StrTextField() {
    
  }
  
  /**
   Process the first YAML line for the field. Normally, this would contain 
   a field name, and possibly a value as well. 
  
   @param firstLine The first YAML line for this field. 
  */
  public void firstLine (StrTextLine firstLine) {
    
    indention = firstLine.getIndention();
    
    withinQuotes = firstLine.isOpenQuote();
    if (withinQuotes) {
      quoteChar = firstLine.getQuoteChar();
    }
    
    blockValue = firstLine.hasBlockValue();
    if (blockValue) {
      blockFolding = firstLine.isBlockFolding();
    }
    
    name = firstLine.getName();
    value.append(firstLine.getValue());
  }
  
  /**
   Append data from a subsequent line containing data for a block value. 
  
   @param line The subsequent line.
   @param newLineString The string to be used to denote a new line. 
  */
  public void anotherLine (StrTextLine line, String newLineString) {
    
    blockValue = true;
    if (blockIndention <= indention) {
      blockIndention = line.getIndention();
    }
    
    line.setBlockValue (indention, blockFolding);
    
    if (value.length() > 0) {
      if (blockFolding) {
        if (! Character.isWhitespace(value.charAt(value.length() - 1))) {
          value.append(' ');
        }
      } else {
        value.append(newLineString);
      }
    } // end if field already has some content
    
    value.append(line.getValue());
    
  } // end method anotherLine
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean hasName() {
    return (name != null && name.length() > 0);
  }
  
  public String getName() {
    return name;
  }
  
  public boolean hasValue() {
    return (value != null && value.length() > 0);
  }
  
  public String getValue() {
    return value.toString();
  }
  
  public boolean isBlockValue() {
    return blockValue;
  }
  
  public void setIndention(int indention) {
    this.indention = indention;
  }
  
  public int getIndention() {
    return indention;
  }
  
}
