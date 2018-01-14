/*
 * Copyright 2014 - 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.mkdown.*;
  import com.powersurgepub.psutils2.textio.*;

  import java.util.*;

/**
 Adds a Table of Contents to a Markdown document.

 @author Herb Bowie
 */
public class AddToCtoMarkdown 
    implements 
      // HeadOutTransformer,
      MarkdownLineReader {
  
  private             TextLineReader      reader;
  private             TextLineWriter      lineWriter;
  
  private             ArrayList<MarkdownLine> headings;
  
  public AddToCtoMarkdown () {
    
  }
  
 /**
   Generate a Table of Contents from the headings found in the Markdown source. 
  
   @param reader The line reader to be used to access the input.
   @param lineWriter The line writer to be used to create the output. 
   @param startHeadingLevel The heading level at which to start generating 
          table of contents entries. This should be the numerically lowest level.
   @param endHeadingLevel The last (numerically highest) heading level for which
          table of contents entries should be generated. 
   @throws TransformException If an error occurs. 
  */
  public void transformNow(TextLineReader reader, TextLineWriter lineWriter,
      int startHeadingLevel, int endHeadingLevel) 
      // throws TransformException 
      {
    // System.out.println("AddToCtoMarkdown.transformNow");
    this.reader = reader;
    this.lineWriter = lineWriter;
    
    // First pass -- table headings for ToC generation.
    headings = new ArrayList<MarkdownLine>();
    reader.open();
    MarkdownInitialParser mdParser = new MarkdownInitialParser (this);
    
    int firstHeadingLevel = 0;
    int lastHeadingLevel = 1;

    MarkdownLine mdLine = mdParser.getNextLine();
    while (mdLine != null) {    
      if (mdLine.getHeadingLevel() > 0
        && mdLine.getHeadingLevel() >= startHeadingLevel 
        && mdLine.getHeadingLevel() <= endHeadingLevel) {
        if (! mdLine.isTableOfContentsHeading()) {
          headings.add(mdLine);
        } // end if we have a heading string
      } // end if we have a heading identifier
      
      mdLine = mdParser.getNextLine();
    } // end while more markdown lines to process
    
    // System.out.println("- " + String.valueOf(headings.size()) + " headings found");
    
    reader.close();
    
    // Second pass -- write output, inserting Table of Contents. 
    reader.open();
    mdParser = new MarkdownInitialParser (this);
    MarkupWriter writer 
        = new MarkupWriter(lineWriter, MarkupWriter.HTML_FRAGMENT_FORMAT);
    writer.setIndenting(false);
    writer.setIndentPerLevel(0);
    writer.openForOutput();
    
    firstHeadingLevel = 0;
    
    boolean listItemOpen[] = new boolean[7];
    for (int i = 0; i < 7; i++) {
      listItemOpen[i] = false;
    }
    lastHeadingLevel = 1;
    mdLine = mdParser.getNextLine();
    
    while (mdLine != null) {    
      if (mdLine.getHeadingLevel() > 0) {
        // Heading Line
        writer.writeHeadingWithID(
            mdLine.getHeadingLevel(), 
            mdLine.getLineContent(), 
            mdLine.getID());
        
        if (mdLine.isTableOfContentsHeading()) {
          // Generate Table of Contents following table of contents heading
          // System.out.println("- Table of Contents Heading found");
          // writer.writeLine("");
          writer.startDiv("", "toc");
          writer.startUnorderedList("");
          MarkdownLine hdLine;
          for (int i = 0; i < headings.size(); i++) {
            hdLine = headings.get(i);
            if (firstHeadingLevel < 1) {
              firstHeadingLevel = hdLine.getHeadingLevel();
              lastHeadingLevel = hdLine.getHeadingLevel();
            }
            String link = "#" + hdLine.getID();
            String text = hdLine.getLineContent();
            if (hdLine.getHeadingLevel() > lastHeadingLevel) {
              writer.startUnorderedList("");
            } else {
              if (hdLine.getHeadingLevel() < lastHeadingLevel) {
                int l = lastHeadingLevel;
                while (l > hdLine.getHeadingLevel()) {
                  if (listItemOpen[l]) {
                    writer.endListItem();
                    writer.endUnorderedList();
                    listItemOpen[l] = false;
                  }
                  l--;
                } // end while higher (more deeply indented) lists still open
              } else {
                // No change in heading level
                if (listItemOpen[hdLine.getHeadingLevel()]) {
                  writer.endListItem();
                  listItemOpen[hdLine.getHeadingLevel()] = false;
                }
              }
            } // end if new heading level less than or equal to last

            if (listItemOpen[hdLine.getHeadingLevel()]) {
              writer.endListItem();
              listItemOpen[hdLine.getHeadingLevel()] = false;
            }
            writer.startListItem("");
            writer.startLink(link);
            writer.write(text);
            writer.endLink(link);
            listItemOpen[hdLine.getHeadingLevel()] = true;
            lastHeadingLevel = hdLine.getHeadingLevel();
          } // end for each stored heading line
          int l = lastHeadingLevel;
          while (l >= firstHeadingLevel) {
            if (listItemOpen[l]) {
              writer.endListItem();
              writer.endUnorderedList();
              listItemOpen[l] = false;
            }
            l--;
          } // end while higher (more deeply indented) lists still open
          writer.endDiv();
        } // end if table of contents heading
      } else {
        // Not a heading line
        writer.writeLine(mdLine.getLine());
      }
      
      mdLine = mdParser.getNextLine();
    } // end while more markdown lines to process
    
    reader.close();
    writer.close(); 
    
  } // end transformNow method
  
  /**
   Obtains the next line of raw markdown source. 
  
   @return The next markdown input line, or null when no more input is available.
   */
  public String getMarkdownInputLine() {
    if (reader == null
        || reader.isAtEnd()
        || (! reader.isOK())) {
      return null;
    } else {
      return reader.readLine();
    }
  }

}
