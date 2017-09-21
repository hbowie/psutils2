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
  import javafx.scene.control.*;

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
    extends TextField
    implements 
      DataWidget {
  
  private PopUpList       popUpList     = new PopUpList();
  
  private TextHandler     handler       = null;
  
  private ValueList       listModel     = null;
  
  private StringBuilder   text;
  private int             semicolon = 0;
  private int             comma = 0;
  private int             start = 0;
  
  private boolean         poppingUp = false;
  
  public TextSelector () {
    super();
    
    this.focusedProperty().addListener(new ChangeListener<Boolean>() {
      public void changed(ObservableValue<? extends Boolean> ov,
          Boolean old_val, Boolean focusGained) {
        focusChanged(focusGained);
      }
    });
    
    this.textProperty().addListener(new ChangeListener<String>() {
      public void changed(ObservableValue<? extends String> ov,
      String old_val, String new_val) {
        setPrefix();
      }
    });

    /*
    addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        if (popup != null) {
          if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            popUpList.nextItemOnList();
          }
          else
          if (evt.getKeyCode() == KeyEvent.VK_UP) {
            popUpList.priorItemOnList();
          }
        }
      }
    });
    
    addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(java.awt.event.KeyEvent evt) {
          if ((listModel != null)
              && (listModel.size() > 0)
              && popup == null) {
          focusGained();
        }
        char c = evt.getKeyChar();
        if (c == '\r' || c == '\n') {
          if (! popUpList.isSelectionEmpty()) {
            setListSelection();
          }
        }
      }
    });
    */
    
    /*
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent documentEvent) {
        setPrefix();
      }
      public void insertUpdate(DocumentEvent documentEvent) {
        setPrefix();
      }
      public void removeUpdate(DocumentEvent documentEvent) {
        setPrefix();
      }
    };
    this.getDocument().addDocumentListener(documentListener);
    */
  }
  
  private void focusChanged(boolean focusGained) {
    System.out.println("TextSelector.focusChanged");
    if (focusGained) {
      System.out.println("  - focus gained");
      if (listModel != null
          && listModel.size() > 0
          && this.getText().length() > 0) {
        focusGained();
      }
    } else {
      System.out.println("  - focus lost");
      focusLost();
      if (handler != null) {
        handler.textSelectionComplete();
      }
    }
  }
  
  public void addTextHandler (TextHandler handler) {
    this.handler = handler;
  }
  
  public void focusGained() {

    // Point location = this.getLocationOnScreen();
    // Dimension dimension = getSize();
    // popUpList.setLocation (location.x, location.y + dimension.height);
    // popUpList.setBounds (
    //     location.x, 
    //     location.y + dimension.height,
    //     dimension.width / 2,
    //     dimension.height * 12);
    // popUpList.setMinimumSize (new Dimension (dimension.width / 2, dimension.height * 12));
    // popUpList.setSize (dimension.width / 2, dimension.height * 12);
    // popUpList.doLayout();
    // Dimension popUpListSize = new Dimension(dimension.width, 240);
    // popUpList.setPreferredSize (popUpListSize);
    // popup = popupFactory.getPopup (this, popUpList,
    //     location.x, location.y + dimension.height);
    // popup.show();
    // popUpList.showList();
    // popUpList.setAlwaysOnTop (true);
    // popUpList.setFocusable (false);
    // this.requestFocusInWindow();
    
    // displayingList = false;
    poppingUp = true;
    popUpList.show();
    this.requestFocus();
    poppingUp = false;
  }
  
  public void focusLost() {
    if (poppingUp) {
      // do nothing
    }
    else
    if (popUpList.isShowing()) {
      popUpList.hide();
    }
    // popup.hide();
    // popup = null;
  }
  
  public void setPrefix () {
    System.out.println("TextSelector.setPrefix");
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
    if (start < text.length()) {
      text.replace (start, text.length(), value);
    } else {
      text.append (value);
    }
    setText (text.toString());
  }
  
  public void checkText () {
    text = new StringBuilder (getText());
    semicolon = text.lastIndexOf (";");
    comma = text.lastIndexOf (",");
    start = comma;
    if (semicolon > comma) {
      start = semicolon;
    }
    if (start < 0) {
      start = 0;
    } 
    while (start < text.length()
        && (! Character.isLetter (text.charAt (start)))) {
      start++;
    }
  }
  
  public void setValueList (ValueList listModel) {
    System.out.println("TextSelector.setValueList");
    this.listModel = listModel;
    popUpList.setTextSelector (this);
    popUpList.setModel (listModel);
  }

}
