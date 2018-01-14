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

package com.powersurgepub.psutils2.txbio;

	import com.powersurgepub.psutils2.textio.*;
	import com.powersurgepub.psutils2.txbmodel.*;

  import java.net.*;
  import java.util.*;

  import javafx.scene.control.*;

/**
 A class providing output services for Textile.
 
 @author Herb Bowie
 */
public class TextIOtextile 
    extends TextIOModule {
  
  public  static final String       TEXTILE = "Textile";
  
  private TextWriter writer;
  
  private boolean comment = false;
  private boolean blockQuote = false;
  private boolean needExplicitParagraph = false;
  private String  href = "";
  
  public TextIOtextile () {
    
  }
  
  
  public void registerTypes (List types) {
    
    TextIOType type = new TextIOType (TEXTILE,
        this, false, true, "textile");
    type.addExtension ("txt");
    types.add (type);
    
  }
  
  public boolean load (TextTree tree, URL url, TextIOType type, String parm) {
    this.tree = tree;
    boolean ok = false;
    return false;
  }
  
  public boolean store (TextTree tree, TextWriter writer, TextIOType type,
      boolean epub, String epubSite) {
    boolean ok = true;
    this.tree = tree;
    this.writer = writer;
    
    // Traverse the tree
    ok = store (tree.getTextRoot());
    
    return ok;
  }
  
  private boolean store (TreeItem<TextData> item) {
    
    TextData node = item.getValue();
    boolean ok = true;
    System.out.println ("Writing " + node.getType()
        + " " + node.getText());
    
    // Open the node
    if (node.isBreak()) {
      writer.ensureNewLine();
    }
    
    if (node.isParagraph() || node.isHeading()) {
      writer.ensureBlankLine();
    }
    
    if (node.isParagraph() 
        && (needExplicitParagraph || node.hasStyle())) {
      writeBlockTag ("p", node.getStyle(), false);
      needExplicitParagraph = false;
    }
    
    if (node.isHeading()) {
      writeBlockTag (node.getType(), node.getStyle(), false);
    }
    
    if (node.isBlockQuote()) {
      writeBlockTag ("bq", node.getStyle(), true);
      writer.assumeLastLineBlank();
      blockQuote = true;
    }
    
    if (node.isAnchor()) {
      href = node.getAttributeHref();
      if (href.length() > 0) {
        writer.write ("\"");
      }
    }
    
    if (node.isItalics() && (! node.isTextTitleCase())) {
      writer.write ("__");
    }
    
    if (node.isCite()
        || (node.isItalics() && (node.isTextTitleCase()))) {
      writeInlineStart ("??", node.getStyle());
    }
    
    if (node.isComment()) {
      comment = true;
    }
    
    // Write out any text
    if (node.getTextType().isLocation()
        || node.getType().equalsIgnoreCase (TextType.XML)
        || node.getType().equalsIgnoreCase (TextType.DOCTYPE)
        || node.getType().equalsIgnoreCase (TextType.COMMENT)
        || comment
        || (node.isAttributeHref() && href.length() > 0)
        || node.isAttributeTarget()) {
      // ignore text
    } else {
      writer.write (node.getText());
    }
    
    // Process this node's children
    for (int i = 0; i < item.getChildren().size(); i++) {
      store (item.getChildren().get(i));
    }
    
    // Close the node
    if (node.isParagraph()
        || node.isHeading()
        || node.isBlockQuote()) {
      needExplicitParagraph = false;
      writer.ensureNewLine();
    }
    
    if (node.isComment()) {
      comment = false;
    }
    
    if (node.isBlockQuote()) {
      blockQuote = false;
      needExplicitParagraph = true;
    }
    
    if (node.isAnchor() && href.length() > 0) {
      writer.write ("\":" + href);
    }
    
    if (node.isItalics() && (! node.isTextTitleCase())) {
      writer.write ("__");
    }
    
    if (node.isCite()
        || (node.isItalics() && (node.isTextTitleCase()))) {
      writer.write ("??");
    }
    
    return ok;
  }
  
  private void writeBlockTag (String type, String style, boolean multiple) {
    writer.ensureBlankLine();
    StringBuffer work = new StringBuffer();
    work.append (type);
    if (style.length() > 0) {
      work.append ("(" + style + ")");
    }
    if (multiple) {
      work.append (".");
    }
    work.append (". ");
    writer.write (work);
  }
  
  private void writeInlineStart (String type, String style) {
    StringBuffer work = new StringBuffer();
    work.append (type);
    if (style.length() > 0) {
      work.append ("(" + style + ")");
    }
    writer.write (work);
  }

}
