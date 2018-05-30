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

package com.powersurgepub.psutils2.widgets;

  import com.powersurgepub.psutils2.ui.*;

  import javafx.application.*;
  import javafx.beans.value.*;
  import javafx.collections.*;
  import javafx.event.*;
  import javafx.geometry.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 A class that provides a popup list for TextSelector.
 
 TextSelector can be used to present the user with a
 popup list from which he or she can select a value.

 PopUpList provides the list that is displayed.

 TextHandler defines an interface for the class that is to
 be notified when text selection is complete.

 ValueList is the class that provides the list
 from which the user will choose a value.
 @author Herb Bowie
 */
public class PopUpList 
    extends ContextMenu {
  
  private String                    prefix = "";
  private ObservableList<MenuItem>  items;
  private String                    selectedValue = "";
  
  private ValueList                 list = null;
  
  private TextSelector              textSelector = null;
  
  public PopUpList() {
    super();
    items = this.getItems();
  }
  
  /**
   Set the model to be used for the JList. 
   
   @param valueList List of selectable values.
   */
  public void setModel (ValueList valueList) {
    this.list = valueList;
    rebuildMenuItems();
  }
  
  /**
   Set the prefix to be used to find the first matching list occurrence.
   
   @param prefix Prefix used to search through the list for the first partial
                 match.
   */
  public void setPrefix (String prefix) {
    this.prefix = prefix;
    rebuildMenuItems();
  }
  
  /**
   Rebuild the context menu value list based on current values of entire
   list plus the current prefix, if any. 
  */
  public void rebuildMenuItems() {
    items.clear();
    for (int i = 0; i < list.size(); i++) {
      String value = list.get(i);
      boolean matched = true;
      int j = 0;
      while (matched 
          && j < prefix.length()
          && j < value.length()) {
        matched = (value.substring(j, j + 1).equalsIgnoreCase(prefix.substring(j, j + 1)));
        if (matched) {
          j++;
        }
      } // end of matching scan
      if (matched) {
        MenuItem valueItem = new MenuItem();
        valueItem.setText(value);
        valueItem.setOnAction(e -> itemSelected(e));
        items.add(valueItem);
      }
    } // end of value list
  } // end method rebuildMenuItems
  
  /**
   Let everyone know that a context menu item was selected.
  
   @param e The ActionEvent that occurred. 
  */
  private void itemSelected(ActionEvent e) {
    Object source = e.getSource();
    if (source instanceof MenuItem) {
      MenuItem sourceMenuItem = (MenuItem)source;
      selectedValue = sourceMenuItem.getText();
      announceSelection();
    }
  }
  
  /**
   Sets the TextSelector to be coordinated with this list.
   
   @param textSelector TextSelector to be coordinated with this list.
   */
  public void setTextSelector (TextSelector textSelector) {
    this.textSelector = textSelector;
  }
  
  /**
   Let the text selector know that the user has selected something. 
  */
  public void announceSelection () {
    if (textSelector != null) {
      textSelector.setListSelection (getSelectedValue());
    }
  }
  
  /**
   Get the last value selected (if any).
  @return 
  */
  public String getSelectedValue () {
    return selectedValue;
  }
  
  /**
   See if there is a selection. 
  
   @return True if we've got something; false if we're shooting blanks. 
  */
  public boolean isSelectionEmpty() {
    return (selectedValue == null || selectedValue.length() == 0);
  }

}
