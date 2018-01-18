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

package com.powersurgepub.psutils2.widgets;

  import com.powersurgepub.psutils2.ui.*;

  import javafx.beans.value.*;
  import javafx.geometry.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.input.*;
  import javafx.scene.layout.*;

/**
 A class that can be used to present the user with a popup list from which
 to choose a value. 
 
 TextSelector can be used to present the user with a
 popup list from which he or she can select a value.

 PopUpList provides the list that is displayed.

 TextHandler defines an interface for the class that is to
 be notified when text selection is complete.

 ValueList is the class that provides the list
 from which the user will choose a value.
 */
public class TextSelector
    extends GridPane
    implements 
      DataWidget {

  private boolean         handlesMultipleValues = false;
  
  private PopUpList       popUpList;
  
  private TextHandler     handler       = null;
  
  private ValueList       listModel     = null;
  
  private TextField       textField;
  private Button          popUpButton;
  
  private StringBuilder   text;
  private int             semicolon = 0;
  private int             comma = 0;
  private int             start = 0;
  
  private boolean         poppingUp = false;
  
  public TextSelector () {
    buildUI();
  }
  
  /**
   Build the User Interface. 
  */
  private void buildUI() {
    
    popUpList     = new PopUpList();

    this.setHgap(2);
    this.setVgap(2);
    
    popUpButton = new Button("+");
    popUpButton.setOnAction(e -> showPopUp());
    this.add(popUpButton, 0, 0, 1, 1);
    
    textField = new TextField();
    textField.setPrefColumnCount(50);
    textField.setContextMenu(popUpList);
    textField.setOnKeyTyped(e -> textKeyTyped(e));
    this.add(textField, 1, 0, 1, 1);
    GridPane.setHgrow(textField, Priority.ALWAYS);

  }

  /**
   Can this field handle multiple values, or only one?

   @param handlesMultipleValues True if we should allow multiple values, false otherwise.
   */
  public void setHandlesMultipleValues(boolean handlesMultipleValues) {
    this.handlesMultipleValues = handlesMultipleValues;
  }

  public boolean handlesMultipleValues() {
    return handlesMultipleValues;
  }

  public boolean oneValueOnly() {
    return (! handlesMultipleValues);
  }
  
  private void textKeyTyped(KeyEvent e) {
    
    String typed = e.getCharacter();
    if (! typed.equals(KeyEvent.CHAR_UNDEFINED)) {
      if (typed.equals("'")) {
        showPopUp();
      }
    }
  }
  
  /**
   Show the pop up list.
  */
  private void showPopUp() {
    setPrefix();
    popUpList.show(textField, Side.BOTTOM, 0, 0);
  }
  
  /**
   Tailor the contents of the pop up list based on user input so far.  
  */
  private void setPrefix () {
    checkText();
    String cat;
    if (start < text.length()) {
      cat = text.substring (start);
    } else {
      cat = "";
    }
    popUpList.setPrefix (cat);
  }
  
  public void setListSelection () {
    String value = popUpList.getSelectedValue();
    setListSelection (value);
  }
  
  public void setListSelection (String value) {
    checkText();
    if (start < text.length() || oneValueOnly()) {
      text.replace (start, text.length(), value);
    } else {
      text.append (value);
    }
    setText (text.toString());
  }
  
  private void checkText () {
    text = new StringBuilder(getText());
    if (oneValueOnly()) {
      start = 0;
    } else {
      semicolon = text.lastIndexOf(";");
      comma = text.lastIndexOf(",");
      start = comma;
      if (semicolon > comma) {
        start = semicolon;
      }
      if (start < 0) {
        start = 0;
      }
      while (start < text.length()
          && (!Character.isLetter(text.charAt(start)))) {
        start++;
      }
    }
  }
  
  public void setValueList (ValueList listModel) {
    this.listModel = listModel;
    popUpList.setTextSelector (this);
    popUpList.setModel (listModel);
  }
  
  public void addTextHandler (TextHandler handler) {
    this.handler = handler;
  }
  
  /**
   Get the data entered by the user, represented as a String. 
  
   @return A string representing data entered by the user. 
  */
  public String getText() {
    return textField.getText();
  }
  
  /**
   Display a string using this widget.
  
   @param t The string to be displayed. 
  */
  public void setText(String t) {
    textField.setText(t);
  }

}
