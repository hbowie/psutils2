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

  import com.powersurgepub.psutils2.textio.*;

/**
 * A utility module available for use by an object implementing the
 * MarkupLineWriter interface.
 */
public class MarkupLineWriterState {
  
  protected char              c                 = ' ';
  protected char              c2                = ' ';
  protected boolean           lastCharWasWhiteSpace = true;
  protected boolean           lastCharWasEmDash = false;
  protected boolean           nextCharIsWhiteSpace = false;
  protected boolean           nextCharIsLetter = false;
  
  protected int               indent            = 0;
  protected int               indentPerLevel    = 0;
  private   boolean           firstNewLine      = true;
  protected boolean           newLineRequested  = false;
  protected boolean           freshLine         = true;
  protected boolean           whiteSpace        = true;
  protected boolean           whiteSpacePending = false;
  protected StringBuffer      str               = new StringBuffer();
  protected TextLineWriter    writer;
  
  /** Creates a new instance of MarkupLineWriterState */
  public MarkupLineWriterState (
      TextLineWriter writer,
      int indent,
      int indentPerLevel) {
    this.writer = writer;
    this.indent = indent;
    this.indentPerLevel = indentPerLevel;
    resetCharContext();
  }
  
  public void moreIndent () {
    indent = indent + indentPerLevel;
  }
  
  public void lessIndent () {
    indent = indent - indentPerLevel;
  }
  
  public void newLine () {
    requestNewLine();
    writer.newLine();
  }
  
  public void requestNewLine () {
    if (str.length() > 0
        && (! freshLine)) {
      writeStr();
    }
    newLineRequested = true;
    resetCharContext();
    /*
    writer.newLine();
    for (int i = 0; i < indent; i++) {
      str.append (" ");
    }
    writeStr();
    freshLine = true;
    whiteSpace = true;
    */
  }
  
  public void resetCharContext () {
    c = ' ';
    c2 = ' ';
    lastCharWasWhiteSpace = true;
    lastCharWasEmDash = false;
  }
  
  public void writeText (StringBuffer s, boolean htmlEntities) {
    writeText (s, htmlEntities, false);
  }
  
  public void writeText (StringBuffer s, boolean htmlEntities, boolean xmlEntities) {
    writeText (s.toString(), htmlEntities, xmlEntities);
  }
  
  public void writeText (String s, boolean htmlEntities) {
    writeText (s, htmlEntities, false);
  } 
  
  /**
   Write out text (non-tag) data
   */
  public void writeText (String s, boolean htmlEntities, boolean xmlEntities) {
    /* System.out.println ("MarkupLineWriterState.writeText length="
        + String.valueOf (s.length())
        + " chars = ("
        + s
        + ")");
    System.out.println ("  whiteSpace? " + String.valueOf (whiteSpace)
        + " freshLine? " + String.valueOf (freshLine)); */
    
    lastCharWasWhiteSpace = (whiteSpace || freshLine || newLineRequested);
    if (lastCharWasWhiteSpace) {
      whiteSpacePending = false;
    } 

    StringBuffer t = new StringBuffer();
    if (whiteSpacePending) {
      t.append (' ');
      lastCharWasWhiteSpace = true;
      whiteSpacePending = false;
    }
    int j = 0;
    int nextSemi = 0;
    for (int i = 0; i < s.length(); i++) {
      c = s.charAt (i);
      
      j = i + 1;
      if (j < s.length()) {
        c2 = s.charAt (j);
        nextCharIsWhiteSpace = Character.isWhitespace (c2);
        nextCharIsLetter = Character.isLetter (c2);
        
      } else {
        c2 = ' ';
        nextCharIsWhiteSpace = true;
        nextCharIsLetter = false;
      }
      if (nextSemi <= i && nextSemi >= 0) {
        nextSemi = s.indexOf (s, i + 1);
      }
      
      // If this is the second char in the -- sequence, then just let
      // it go by, since we already wrote out the em dash
      if (lastCharWasEmDash) {
        lastCharWasEmDash = false;
        lastCharWasWhiteSpace = false;
      }
      else
        
      // If we have white space, write out only one space
      if (c == ' '
          || c == '\t'
          || c == '\r'
          || c == '\n') {
        lastCharWasEmDash = false;
        if (lastCharWasWhiteSpace) {
          // do nothing
        } else {
          t.append (' ');
          lastCharWasWhiteSpace = true;
        }
      } 
      else
        
      // If we have two dashes, replace them with an em dash
      if (c == '-' && c2 == '-' && htmlEntities) {
        t.append ("&#8212;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = true;
      } 
      else
        
      // If we have a double quotation mark, replace it with a smart quote  
      if (c == '"' && (htmlEntities)) {
        if (lastCharWasWhiteSpace) {
          t.append ("&#8220;");
        } else {
          t.append ("&#8221;");
        }
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
      } 
      else 
        
      // If we have a double quotation mark, replace it with a entity  
      if (c == '"' && (xmlEntities)) {
        t.append ("&quot;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
      } 
      else 
        
      // If we have a single quotation mark, replace it with the appropriate entity
      if (c == '\'' && htmlEntities) {
        if (lastCharWasWhiteSpace) {
          t.append ("&#8216;");
        } 
        else
        if (! nextCharIsLetter) {
          t.append ("&#8217;");
        } else {
          t.append (c);
        }
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
      }
      else
        
      // If we have a single quotation mark, replace it with the appropriate entity
      if (c == '\'' && xmlEntities) {
        t.append ("&quot;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
      }
      else
        
      // if an isolated ampersand, replace it with appropriate entity
      if (c == '&' && (nextSemi < 0 || nextSemi > (i + 7))) {
        t.append ("&amp;");
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
      }
      else
      
      // If nothing special, then just move it to the output
      {
        lastCharWasWhiteSpace = false;
        lastCharWasEmDash = false;
        t.append (c);
      }
      /* System.out.println ("    c = "
          + String.valueOf (c)
          + " lastCharWasWhiteSpace? "
          + String.valueOf (lastCharWasWhiteSpace)); */
    } // end for each char in text
    /* if (lastCharWasWhiteSpace && t.length() > 0) {
      t.deleteCharAt (t.length() - 1);
      whiteSpacePending = true;
      lastCharWasWhiteSpace = false;
    } */
    whiteSpace = lastCharWasWhiteSpace;
    write (t);
    // System.out.println ("  Ending whiteSpace? " + String.valueOf (whiteSpace));
  } // end method writeText
  
  public void write (StringBuffer s) {
    write (s.toString());
  }
  
  public void write (String s) {
    append (s);
    writeStr();
  }
  
  public void append (String s) {
    str.append (s);
    /* if (s.length() > 0) {
      freshLine = false;
    } */
  }
  
  public boolean endsWithSlash () {
    return ((str.length() > 0) && (str.charAt (str.length() - 1) == '/'));
  }
  
  public void writeStr () {
    String trimmed = str.toString().trim();
    boolean allWhiteSpace = (trimmed.length() == 0);
    if (newLineRequested && allWhiteSpace) {
      // do nothing
    } else {
      if (newLineRequested) {
        if (! freshLine) {
          writer.newLine();
        }
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < indent; i++) {
          left.append (" ");
        }
        writer.write (left.toString());
        freshLine = true;
        // whiteSpace = true;
        newLineRequested = false;
      }
      writer.write (str.toString());
      freshLine = false;
    }
    str = new StringBuffer();
  }
  
}
