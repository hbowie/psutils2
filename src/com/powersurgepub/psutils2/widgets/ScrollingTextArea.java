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

  import javafx.scene.control.*;

/**
 
 @author Herb Bowie
 */
public class ScrollingTextArea
    extends TextArea 
      implements
        DataWidget {
  
  /**
   Constructor with all possible parameters.
  
   @param columns   Number of columns for the text area. 
   @param rows      Number of rows for the text area. 
   @param lineWrap  Should the text area wrap?
   @param wrapStyleWord Should the text area wrap at words?
  */
  public ScrollingTextArea (
      int columns,
      int rows,
      boolean lineWrap,
      boolean wrapStyleWord) {
    
    super();
    this.setPrefColumnCount(columns);
    this.setPrefRowCount(rows);
    this.setWrapText(lineWrap);
    this.setWrapStyleWord(wrapStyleWord);
    // this.setTabSize(2);
    // this.setVerticalScrollBarPolicy
    //     (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    // setViewportView(textArea);
  }
  
  /**
   Constructor with all possible parameters.
  
   @param columns   Number of columns for the text area. 
   @param rows      Number of rows for the text area. 
  */
  public ScrollingTextArea (
      int columns,
      int rows) {
    
    super();
    this.setPrefColumnCount(columns);
    this.setPrefRowCount(rows);
    // setViewportView(textArea);
  }

  /**
   Default constructor with no 
  */
  public ScrollingTextArea() {
    super();
    this.setPrefColumnCount(60);
    this.setPrefRowCount(5);
    this.setWrapText(true);
    this.setWrapStyleWord(true);
    // setViewportView(textArea);
  }
  
  public void setColumns(int columns) {
    this.setPrefColumnCount(columns);
  }
  
  public void setRows(int rows) {
    this.setPrefRowCount(rows);
  }
  
  public void setLineWrap(boolean lineWrap) {
    this.setWrapText(lineWrap);
  }
  
  public void setWrapStyleWord(boolean wrapStyleWord) {
    this.setWrapStyleWord(wrapStyleWord);
  }
  
  public void setTextWithCaretAtZero(String t) {
    this.setText(t);
    this.positionCaret(0);
  }
  
  public TextArea getTextArea() {
    return this;
  }

}
