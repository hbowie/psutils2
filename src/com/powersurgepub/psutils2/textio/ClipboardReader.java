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

package com.powersurgepub.psutils2.textio;

  import java.io.*;

  import javafx.scene.input.*;

/**
 Implements the TextLineReader interface with input from the system clipboard.

 @author Herb Bowie
 */
public class ClipboardReader 
    implements 
      TextLineReader {
  
  private             StringLineReader    reader = null;
  
  private             boolean             ok = true;
  
  private             Clipboard           clipboard = null;
  
  /**
   Constructor.
  
   @param s The string to be used as input. 
  */
  public ClipboardReader() {
    reader = new StringLineReader("");
    clipboard = Clipboard.getSystemClipboard();
    boolean hasTransferableText = ((clipboard != null) 
        && clipboard.hasString());
    if (hasTransferableText) {
        reader = new StringLineReader
            ((String)clipboard.getContent(DataFormat.PLAIN_TEXT));
    }
  }
 
  /**
   Ready the input source to be read. 
   
   @return 
  */
  public boolean open () {
    return reader.open();
  }
  
  /**
   Read the next line from the input String. End of line characters or Strings
   are not returned. Line endings are denoted by a Line Feed or Carriage Return
   (each optionally followed by the other), or an HTML comment enclosing an
   EOL marker: <!--EOL-->.
   
   @return The next line, or null if end of file.
  */
  public String readLine () {
    return reader.readLine();
  }
  
  public boolean close() {
    return reader.close();
  }
  
  public boolean isOK () {
    return (ok && reader.isOK());
  }
  
  public boolean isAtEnd() {
    return reader.isAtEnd();
  }
  

}
