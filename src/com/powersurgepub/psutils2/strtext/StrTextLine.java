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

	import com.powersurgepub.psutils2.basic.*;

  import java.util.*;

/**
 One line of structured text.

 @author Herb Bowie
 */
public class StrTextLine {
  
  // Working fields
  private               String              line = "";
  private               int                 lineIndex = 0;
  
  // Result Fields
  private               int                 indention = 0;
  private               boolean             newListMember = false;
  private               boolean             newDocument = false;
  private               boolean             blank = false;
  private               boolean             endOfFile = false;
  private               int                 colonPosition = -1;
  private               int                 nameStart = -1;
  private               int                 nameEnd = -1;
  private               int                 valueStart = -1;
  private               int                 valueEnd = -1;
  
  private               boolean             withinQuotes = false;
  private               char                quoteChar = ' ';
  private               boolean             blockValue = false;
  private               boolean             blockFolding = true;
  
  public StrTextLine(String line) {
    
    this.line = line;

    // Look for special line types
    if (line.length() >= 3 
        && line.substring(0, 3).equals("---")) {
      newDocument = true;
      lineIndex = 3;
      while (lineIndex < line.length()
          && Character.isWhitespace(line.charAt(lineIndex))) {
        lineIndex++;
      }
    }
    else
    if (line.length() >= 3 
        && line.substring(0, 3).equals("...")) {
      endOfFile = true;
      lineIndex = 3;
    } 
    
    // Count indention and scoot past it
    if ((! newDocument) && (! endOfFile)) { 
      boolean endOfIndention = false;
      while (! endOfIndention) {
        if (lineIndex >= line.length()) {
          endOfIndention = true;
        } else {
          char c = line.charAt(lineIndex);
          if (c == GlobalConstants.SPACE) {
            indention++;
            lineIndex++;
          }
          else
          if (c == GlobalConstants.TAB) {
            indention = indention + 2;
            lineIndex++;
          }
          else
          if (c == '-') {
            indention++;
            newListMember = true;
            lineIndex++;
          } else {
            endOfIndention = true;
          }
        } // end if another char to inspect
      } // end while counting indention
    } // end if not --- or ...
    
    skipWhiteSpaceAndComments();
    
    if (lineIndex >= line.length()) {
      blank = true;
    }
    
    // Look for a colon separating the field name from its value
    if ((! endOfFile) 
        && (! withinQuotes) 
        && (lineIndex + 2) < line.length()) {
      colonPosition = line.indexOf(": ", lineIndex);
    }
    
    // Determine where the field name starts and ends
    if (colonPosition > 0) {
    
      // Skip past a question mark at the beginning of a field name
      // It indicates that spaces can be embedded within the field name,
      // but we allow those anyway. 
      if (lineIndex < line.length()
          && line.charAt(lineIndex) == '?') {
        lineIndex++;
        skipWhiteSpaceAndComments();
      }

      char c = nextChar();
      if (c == '|' || c == '>' || c == ':' || c == ' ') {
        // no field name :-(
      } else {
        nameStart = lineIndex;
        nameEnd = colonPosition;
        while (Character.isWhitespace(line.charAt(nameEnd - 1))) {
          nameEnd--;
        }
      } // End if valid first character of a field name
      
      lineIndex = colonPosition + 1;
      skipWhiteSpaceAndComments();
      
    } // end if we found a colon on the line
    
    if (nextChar() == '|') {
        blockValue = true;
        blockFolding = false;
        lineIndex++;
    }
    else
    if (nextChar() == '>') {
      blockValue = true;
      blockFolding = true;
      lineIndex++;
    }
    else
    if (nextChar() == '"') {
      withinQuotes = true;
      quoteChar = nextChar();
      lineIndex++;
    }
    else
    if (nextChar() == '\'') {
      withinQuotes = true;
      quoteChar = nextChar();
      lineIndex++;
    }

    if (! withinQuotes) {
      skipWhiteSpaceAndComments();
    }
    
    if (lineIndex < line.length()) {
      valueStart = lineIndex;
      valueEnd = line.length();
      while (valueEnd > valueStart 
          && (Character.isWhitespace(line.charAt(valueEnd - 1)))) {
        valueEnd--;
      }
      if (withinQuotes) {
        if (line.charAt(valueEnd - 1) == quoteChar) {
          valueEnd--;
          withinQuotes = false;
        } else {
          valueEnd = line.length();
        }
      } // End if within quotes
    } // End if we have some nonblank characters left on the line
    
  } // End constructor
  
  private void skipWhiteSpaceAndComments() {
    
    // Skip any leading white space
    while (lineIndex < line.length()
        && Character.isWhitespace(line.charAt(lineIndex))) {
      lineIndex++;
    }
    
    // Skip remainder of line if it is just a comment
    if (nextChar() == '#') {
      lineIndex = line.length();
    }
  }
  
  private char nextChar() {
    if (lineIndex < line.length()) {
      return line.charAt(lineIndex);
    } else {
      return ' ';
    }
  }
  
  /**
   Indicates whether line starts with three or more hyphens, used to indicate
   the beginning of a new document. 
  
   @return True if line starts with three or more hyphens, indicating the 
           beginning of a new document. 
  */
  public boolean isNewDocument() {
    return newDocument;
  }
  
  public boolean isEndOfFile() {
    return endOfFile;
  }
  
  /**
   Indicates whether line has a name and/or any data. 
  
   @return True if line is blank, or contains only a comment. 
  */
  public boolean isBlank() {
    return blank;
  }
  
  public boolean isOpenQuote() {
    return withinQuotes;
  }
  
  public char getQuoteChar() {
    return quoteChar;
  }
  
  public int getIndention() {
    return indention;
  }
  
  /**
   Treat this line as one line within a block value. 
  
   @param indention    The indention of the field name. 
   @param blockFolding True if block lines are to be folded, false if 
                       new lines are to be preserved. 
  */
  public void setBlockValue(int nameIndention, boolean blockFolding) {
    if (blockFolding) {
      valueStart = indention;
    } else {
      valueStart = nameIndention;
      valueEnd = line.length();
    }
  }
  
  public String getName() {
    if (hasName()) {
      return line.substring(nameStart, nameEnd);
    } else {
      return "";
    }
  }
  
  public boolean hasNoName() {
    return (! hasName());
  }
  
  public boolean hasName() {
    return (nameStart >= 0 && nameEnd > nameStart);
  }
  
  public String getValue() {
    if (valueStart >= 0 && valueEnd > valueStart) {
      return line.substring(valueStart, valueEnd);
    } else {
      return "";
    }
  }
  
  public boolean hasValue() {
    return (valueStart >= 0 && valueEnd > valueStart);
  }
  
  public boolean hasBlockValue() {
    return blockValue;
  }
  
  /**
   See if the line has any sort of value (block or otherwise) associated
   with it. 
  
   @return True if no data and no indicator that a block value follows.
  */
  public boolean hasNoValue() {
    return ((! hasValue()) && (! hasBlockValue()));
  }
  
  public boolean isBlockFolding() {
    return blockFolding;
  }
  
  public String toString() {
    return line;
  }
  
}
