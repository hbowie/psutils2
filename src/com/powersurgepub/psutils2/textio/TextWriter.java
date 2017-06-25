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

package com.powersurgepub.psutils2.textio;

  import com.powersurgepub.psutils2.strings.*;

  import java.io.*;
  import java.net.*;

/**
  Writes characters and lines to an output destination with optional line 
  indenting and line prefixes. 
 */
public class TextWriter
    implements TextLineWriter {
  
  private     TextLineWriter        writer;  
  private     boolean               outOK = false;
  private     int                   lineLength = 0;
  private     int                   lastLineLength = 0;
  private     boolean               lastLineBlank = true;
  
  /** 
  Indicates that a new line sequence was the last thing written to the
  output text file. 
  */
  private     boolean               newLineStarted = true;
  private     String                linePrefix = "";
  private     boolean               indenting = false;
  private     int                   indent            = 0;
  private     int                   indentPerLevel    = 2;
  
  /**
  Construct a new text writer from a text line writer. 
  
  @param writer A text line writer.
  */
  public TextWriter (TextLineWriter writer) {
    this.writer = writer;
  }
  
  /**
  Construct a new text writer from a file.
  
  @param file The file to which the text is to be written. 
  */
  public TextWriter (File file) {
    this.writer = new FileMaker (file);
  }
  
  /**
  Construct a new text writer from a URL.
  
  @param url The url to which the text is to be written.
  */
  public TextWriter (URL url) {
    writer = new FileMaker (url);
  }
  
  /**
  Construct a new text writer, assuming that we will be building a string to be
  retrieved later, since no output destination is being provided. 
  */
  public TextWriter () {
    this.writer = new StringMaker();
  }
  
  public String getDestination() {
    if (writer == null) {
      return "null";
    } else {
      return writer.getDestination();
    }
  }
  
  /**
  Open the writer for output.
  
  @return OK if everything was successful.
  */
  public boolean openForOutput () {
    outOK = writer.isOK();
    lineLength = 0;
    lastLineLength = 0;
    lastLineBlank = true;
    newLineStarted = true;
    linePrefix = "";
    if (outOK) {
      outOK = writer.openForOutput();
    }
    return outOK;
  }
  
  /**
  Turn indenting on or off. 
  
  @param indenting True if we want to use the indenting feature.
  */
  public void setIndenting (boolean indenting) {
    this.indenting = indenting;
    // indent = 0;
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
  }
  
  /**
  Increase the indent for following lines. 
  */
  public void moreIndent () {
    indent = indent + indentPerLevel;
  }
  
  /**
  Decrease the indent for following lines.
  */
  public void lessIndent () {
    indent = indent - indentPerLevel;
    if (indent < 0) {
      indent = 0;
    }
  }
  
  /**
  Set an optional prefix to be written out at the beginning of every following
  line. 
  
  @param linePrefix A prefix to be written out at the beginning of every
  following line. 
  */
  public void setLinePrefix (String linePrefix) {
    this.linePrefix = linePrefix;
  }
  
  /**
  Let's pretend that the last line we wrote was blank, whether it was or not.
  */
  public void assumeLastLineBlank () {
    lastLineBlank = true;
  }
  
  /**
  If the last line was blank, then no need to do anything. If the last line
  wasn't blank, then write one out now;
  */
  public void ensureBlankLine() { // ???
    while (! lastLineBlank) {
      newLine();
    }
  }
  
  /**
  Write a string buffer to the output, with prefix and indentation as previously 
  specified, and then end the current line and start a new one. 
  
  @param  s The string buffer to be written.
  @return True if everything was written successfully. 
  */
  public boolean writeLine (StringBuffer s) {
    return writeLine (s.toString());
  }
  
  /**
  Write a string to the output, with prefix and indentation as previously 
  specified, and then end the current line and start a new one. 
  
  @param  s The string to be written.
  @return True if everything was written successfully. 
  */
  public boolean writeLine (String s) {
    boolean ok = write (s);
    if (ok) {
      ok = newLine ();
    }
    return ok;
  }
  
  /**
  Write a character to the output, with prefix and indentation as previously 
  specified. 
  
  @param  c The character to be written.
  @return True if everything was written successfully. 
  */
  public boolean write (char c) {
    return write (String.valueOf (c));
  }
  
  /**
  Write a string buffer to the output, with prefix and indentation as previously 
  specified. 
  
  @param s The string buffer to be written.
  @return True if everything was written successfully. 
  */
  public boolean write (StringBuffer s) {
    return write (s.toString());
  }
  
  /**
  Write a string to the output, with prefix and indentation as previously 
  specified. 
  
  @param s The string to be written.
  @return True if everything was written successfully. 
  */
  public boolean write (String s) {
    lineLength 
        = lineLength + StringUtils.trimRight(s).length();
    if (outOK) {
      if (newLineStarted) {
        if (linePrefix.length() > 0) {
          outOK = writer.write (linePrefix);
        }
        else
        if (indenting && indent > 0) {
          for (int i = 0; i < indent; i++) {
            if (outOK) {
              outOK = writer.write (" ");
            } // end if everything still ok
          } // end when we've indented far enough
        } // end if indenting
      } // end if first stuff written on a new line
      outOK = writer.write (s);
    } // end if ok so far
    newLineStarted = false;
    return outOK;
  } // end method write
  
  /**
   If the last thing written was a new line sequence, then do nothing. 
   Otherwise write a new line sequence.
   */
  public boolean ensureNewLine () {
    boolean ok = true;
    if (! newLineStarted) {
      ok = newLine();
    }
    return ok;
  }
  
  /**
  End the current line and start a new one by writing a new line sequence to 
  the output text file. Keep track of whether the last line was blank, and
  indicate that a new line has been started.
  
  @return  OK if everything was successful.
  */
  public boolean newLine () {
    lastLineLength = lineLength;
    if (lastLineLength == 0) {
      lastLineBlank = true;
    } else {
      lastLineBlank = false;
    }
    if (outOK) {
      outOK = writer.newLine();
    } // end if ok so far
    lineLength = 0;
    newLineStarted = true;
    return outOK;
  } // end method newLine
  
  /**
  How much data has been written so far since the last new line sequence?
  
  @return Number of characters written since the last new line sequence.
  */
  public int getLineLength () {
    return lineLength;
  }
  
  /**
  How much data was written to the last line written, prior to the last new
  line sequence?
  
  @return Number of characters written to the last line, prior to the last
          new line sequence.
  */
  public int getLastLineLength () {
    return lastLineLength;
  }
  
  /**
  Has a new line been started without yet writing any characters to the line?
  
  @return True if a new line has been started without yet writing any text
          to the new line. 
  */
  public boolean isNewLineStarted () {
    return newLineStarted;
  }
  
  /**
  Flush the data written so far to the output file. 
  
  @return True if no input/output errors. 
  */
  public boolean flush () {
    if (outOK) {
      outOK = writer.flush();
    }
    return outOK;
  } // end method flush
  
  /**
  Close the output file. 
  
  @return True if no input/output errors. 
  */
  public boolean close () {
    if (outOK) {
      outOK = writer.close();
    } // end if ok so far
    return outOK;
  }
  
  public boolean isOK() {
    return outOK;
  }

}
