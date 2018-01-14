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
 Perform the initial parsing of a bunch of markdown lines, taking a stream
 of simple Strings and returning a stream of MarkdownLines.

 @author Herb Bowie
 */
public class MarkdownInitialParser {
  
  private MarkdownDoc        doc = new MarkdownDoc();
  private MarkdownLineReader reader = null;
  private String             line = null;
  private MarkdownLine       onDeck = null;
  private boolean            atEnd = false;
  
  public MarkdownInitialParser (MarkdownLineReader reader) {
    this.reader = reader;
  }
  
  /**
   Pass back next Markdown Line that has had initial parsing performed. 
  
   @return Next MarkdownLine object, or null if no more to return. 
  */
  public MarkdownLine getNextLine() {
      
    line = null;
    if ((! atEnd)
        && onDeck == null) {
      // Prime the pump
      getLine();
      if (line != null) {
        onDeck = new MarkdownLine (doc, line);
      } // end if first line not null
    } // end of pump priming
    getLine();
    MarkdownLine mdLine = null;
    if (line != null) {
      mdLine = new MarkdownLine (doc, line);
      if (mdLine.isUnderlines()) {
        if (mdLine.getUnderlineChar() == '=') {
          onDeck.setHeadingLevel(1);
        }
        else
        if (mdLine.getUnderlineChar() == '-') {
          onDeck.setHeadingLevel(2);
        }
      } // end if next line is underlines
    } // end if new line not null
    MarkdownLine returnLine = onDeck;
    onDeck = mdLine;
    return returnLine;
  } // end method getNextLine
  
  private void getLine() {
    line = reader.getMarkdownInputLine();
    if (line == null) {
      atEnd = true;
    }
  }

}
