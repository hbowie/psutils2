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

package com.powersurgepub.psutils2.widgets;

  import java.awt.*;
  import javax.swing.*;

/**
 
 @author Herb Bowie
 */
public class LinkWidget
    extends JScrollPane 
      implements
        DataWidget {
  
  private JTextArea textArea = new JTextArea();

  /**
   Default constructor with no 
  */
  public LinkWidget() {
    textArea.setColumns(60);
    textArea.setRows(3);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(false);
    textArea.setTabSize(0);
    setViewportView(textArea);
    Dimension min = this.getMinimumSize();
    int minW = min.width;
    min.setSize(minW, 60);
    this.setMinimumSize(min);
    
    Dimension pref = this.getPreferredSize();
    int prefW = pref.width;
    pref.setSize(prefW, 80);
    this.setPreferredSize(pref);
  }
  
  public void setText(String t) {
    textArea.setText(t);
    textArea.setCaretPosition(0);
  }
  
  public String getText() {
    return textArea.getText();
  }
  
  public JTextArea getTextArea() {
    return textArea;
  }

}
