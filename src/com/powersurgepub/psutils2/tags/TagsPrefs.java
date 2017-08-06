/*
 * Copyright 2014-2017 Herb Bowie
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

package com.powersurgepub.psutils2.tags;

  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.prefs.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.widgets.*;

  import javafx.beans.value.*;
	import javafx.event.*;
  import javafx.geometry.*;
	import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
  A Preferences pane used to identify tags to select, and tags to suppress. 

  @author Herb Bowie
 */
public class TagsPrefs 
    implements
      PrefSet {
  
  private static final String SELECT_TAGS    = "select-tags";
  private static final String SUPPRESS_TAGS  = "suppress-tags";
  
  private GridPane    grid     = new GridPane();
  
  private Label tagsSelectLabel = new Label("Tags to Select:");
  private Label tagsSuppressLabel = new Label("Tags to Suppress:");
  private TextSelector  selectTextSelector;
  private TextSelector  suppressTextSelector;
  
  public TagsPrefs() {
    buildUI();
  }
  
  private void buildUI() {
        // Set spacing between components
    grid.setPadding(new Insets(10));
    grid.setHgap(10);
    grid.setVgap(10);
   
    // First row
    grid.add(tagsSelectLabel, 0, 0, 1, 1);
    selectTextSelector = new TextSelector();
    selectTextSelector.setEditable(true);
    selectTextSelector.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
            (SELECT_TAGS, selectTextSelector.getText());
      }
    });
    grid.add(selectTextSelector, 1, 0, 1, 1);
    
    // Second row
    grid.add(tagsSuppressLabel, 0, 1, 1, 1);
    suppressTextSelector.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
            (SUPPRESS_TAGS, suppressTextSelector.getText());
      }
    });
    grid.add(suppressTextSelector, 1, 1, 1, 1);
    
    selectTextSelector.setText
        (UserPrefs.getShared().getPref (SELECT_TAGS, ""));
    suppressTextSelector.setText
        (UserPrefs.getShared().getPref (SUPPRESS_TAGS, ""));
    
  }
  
  public void setTagsValueList (ValueList valueList) {
    selectTextSelector.setValueList (valueList);
    suppressTextSelector.setValueList(valueList);
  }
  
  public String getSelectTagsAsString() {
    return selectTextSelector.getText();
  }
  
  public String getSuppressTagsAsString() {
    return suppressTextSelector.getText();
  }
  
  public void savePrefs() {
    UserPrefs.getShared().setPref
        (SELECT_TAGS, selectTextSelector.getText());
    UserPrefs.getShared().setPref
        (SUPPRESS_TAGS, suppressTextSelector.getText());
  }
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "Tags Export";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return grid;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    savePrefs();
  }

}
