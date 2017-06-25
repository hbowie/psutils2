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

package com.powersurgepub.psutils2.tags;

  import javafx.event.*;
  import javafx.geometry.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 
 @author Herb Bowie
 */
public class TagsChangeScreen 
    extends Stage {
  
  private   TagsList              findCategories     = new TagsList();
  private   TagsList              replaceCategories  = new TagsList();
  private   TaggableList          taggableList;
  private   TagsChangeAgent       changeAgent;
  
  private   Stage     parent;
  
  private   Scene     scene;
  private   GridPane  grid;
  private   Label     findLabel       = new Label("Existing Tag:");
  private   ComboBox  findComboBox    = new ComboBox();
  private   Label     replaceLabel    = new Label("New Tag:");
  private   ComboBox  replaceComboBox = new ComboBox();
  private   Button    cancelButton    = new Button("Cancel");
  private   Button    replaceButton   = new Button("Add/Replace All");
  
  public TagsChangeScreen(Stage parent, 
      boolean modal,
      TagsList tagsList,
      TagsChangeAgent changeAgent) {
    
    super(StageStyle.UTILITY);
    if (modal) {
      this.initModality(Modality.APPLICATION_MODAL);
    }
    if (parent != null) {
      this.initOwner(parent);
    }
    // super (parent, modal);
    this.parent = parent;
    findCategories.addAll (tagsList);
    // findCategories.setSelectedItem (td.getCategories().getSelectedItem());
    replaceCategories.addAll (tagsList);
    // replaceCategories.setSelectedItem (td.getCategories().getSelectedItem());
    this.taggableList = taggableList;
    this.changeAgent = changeAgent;
    buildUI();
  }
  
  private void buildUI() {
    
    this.setTitle("Add/Replace Tags");
    grid = new GridPane();
    
    // Set spacing between components
    grid.setPadding(new Insets(10));
    grid.setHgap(10);
    grid.setVgap(10);

    // First row
    grid.add(findLabel,       0, 0, 1, 1);
    grid.add(findComboBox,    1, 0, 2, 1);
    GridPane.setHgrow(findComboBox, Priority.ALWAYS);
    
    // Second row
    grid.add(replaceLabel,    0, 1, 1, 1);
    grid.add(replaceComboBox, 1, 1, 2, 1);
    GridPane.setHgrow(replaceComboBox, Priority.ALWAYS);
    
    // Third row
    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        cancelButtonActionPerformed();
      }
    });
    grid.add(cancelButton,    1, 2, 1, 1);
    
    replaceButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        replaceButtonActionPerformed();
      }
    });
    grid.add(replaceButton,   2, 2, 1, 1);
    
    
    scene = new Scene(grid);
    this.setScene(scene);
  }
  
  private void replaceButtonActionPerformed() {                                              
    String find = (String)(findComboBox.getSelectionModel().getSelectedItem());
    String replace = (String)(replaceComboBox.getSelectionModel().getSelectedItem());
    changeAgent.changeAllTags(find, replace);
    this.close();
    // dispose();
  }                                             

  private void cancelButtonActionPerformed() {                                             
    this.close();
    // dispose();
  }                                            
  
  /** Closes the dialog */
  private void closeDialog(java.awt.event.WindowEvent evt) {                             
    this.close();
    // dispose();
  }  

}
