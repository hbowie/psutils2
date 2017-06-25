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
    extends Stage {
  
  private ValueList valueList = null;
  
  private Scene scene;
  private BorderPane pane;
  private ListView<String> list;
  
  private TextSelector textSelector;
  
  public PopUpList() {
    super(StageStyle.UTILITY);
    buildUI();
  }
  
  
  /**
   Build the User Interface. 
  */
  private void buildUI() {

    list = new ListView<String>();

    list.setMinSize(120, 240);
    list.setPrefSize(480, 240);

    // listScrollPane.setPreferredSize(new java.awt.Dimension(200, 140));

    // list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    list.setMaxSize(400, 800);
    list.getSelectionModel().selectedItemProperty().addListener (
        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      listSelectionMade();
    });

    pane.setCenter(list);
    scene = new Scene(pane);
    this.setScene(scene);

  }
  
  
  private void listSelectionMade() {
    if (! list.getSelectionModel().isEmpty()) {
      announceSelection();
    }
  }
  /**
   Set the model to be used for the JList. 
   
   @param listModel Model to be used for the JList. 
   */
  public void setModel (ValueList valueList) {
    this.valueList = valueList;
    list.getItems().clear();
    for (int i = 0; i < valueList.size(); i++) {
      list.getItems().add(valueList.get(i));
    }
    // list.setModel (listModel);
    // list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
  }
  
  /**
   Set the prefix to be used to find the first matching list occurrence.
   
   @param prefix Prefix used to search through the list for the first partial
                 match.
   */
  public void setPrefix (String prefix) {
    if (prefix != null && list.getItems().size() > 0) {
      int match = 0;
      boolean matched = false;
      while (match < list.getItems().size() && (! matched)) {
        String item = list.getItems().get(match);
        if (item.startsWith(prefix)) {
          matched = true;
        } else {
          match++;
        }
      } // end while searching for a match
      if (match >= list.getItems().size()) {
        match = 0;
      }
          
      // list.getNextMatch (prefix, 0, Position.Bias.Forward);
      // list.clearSelection();
      list.getSelectionModel().select(match);
      list.scrollTo(match);
      // list.ensureIndexIsVisible (match);
    }
  }
  
  /**
   Sets the TextSelector to be coordinated with this list.
   
   @param textSelector TextSelector to be coordinated with this list.
   */
  public void setTextSelector (TextSelector textSelector) {
    this.textSelector = textSelector;
  }
  
  public void announceSelection () {
    if (textSelector != null) {
      textSelector.setListSelection (getSelectedValue());
    }
  }
  
  public String getSelectedValue () {
    if (isSelectionEmpty()) {
      return "";
    } else {
      return list.getSelectionModel().getSelectedItem();
    }
  }
  
  public boolean isSelectionEmpty() {
    return list.getSelectionModel().isEmpty();
  }

  public void nextItemOnList () {
    int index = list.getSelectionModel().getSelectedIndex();
    index++;
    if (index < list.getItems().size()) {
      list.getSelectionModel().select(index);
      list.scrollTo(index);
    }
  }

  public void priorItemOnList () {
    int index = list.getSelectionModel().getSelectedIndex();
    if (index > 0) {
      index --;
      list.getSelectionModel().select(index);
      list.scrollTo(index);
    }
  }

}
