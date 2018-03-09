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

package com.powersurgepub.psutils2.textmerge;

	import com.powersurgepub.psutils2.basic.*;
	import com.powersurgepub.psutils2.list.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.script.*;
	import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.widgets.*;

 	import javafx.event.*;
 	import javafx.scene.control.*;
 	import javafx.scene.layout.*;
  import javafx.stage.*;


/**
 The filtering module used as part of PSTextMerge. 

 @author Herb Bowie
 */
public class TextMergeFilter
      implements TextMergeResetter{
  
  private     Window              ownerWindow = null;
  private     DataRecList         list = null;
  private     TextMergeController textMergeController = null;
  private     TextMergeScript     scriptRecorder = null;
  
  private     TabPane             tabs = null;
  private     MenuBar             menus = null;
  
  // Filter panel objects
  private     FXUtils             fxUtils;
  private     Tab filterTab;
  private     GridPane filterPane;
  private     Label filterFieldsLabel;
  private     ComboBoxWidget filterFieldsBox;
  private     ComboBoxWidget filterOperandBox;
  private     ComboBoxWidget filterValueBox = null;
  private     Button filterAddButton;
  private     Button filterClearButton;
  private     Button filterSetButton;
  private     ToggleGroup filterAndOrGroup;
  private     RadioButton filterAndButton;
  private     RadioButton filterOrButton;
  private     Label filterTextLabel;
  private     StringBuilder filterText;
  private     TextArea filterTextArea = null;
  
  private     String[]            defaultFilterValues = {" "};
  
  // Fields using for filtering
  private     boolean             filterTabBuilt      = false;
  private     TextMergeFilter     textMergeFilter;
	private     PSFieldFilter       fieldFilter;
	private     PSItemFilter        itemFilter;
	private     String              currentFilterField;
	private     int                 currentFilterColumn;
	private     String              currentFilterOperand;
	private     String              currentFilterValue = " ";
	private     boolean             currentAndLogic = true;
  
  public TextMergeFilter(
      Window ownerWindow,
      DataRecList list, 
      TextMergeController textMergeController, 
      TextMergeScript scriptRecorder) {
    this.ownerWindow = ownerWindow;
    this.list = list;
    this.textMergeController = textMergeController;
    this.scriptRecorder = scriptRecorder;
    initItemFilter();
  }


  /**
   Let's reset as many variables as we can to restore the
   text merge state to its original condition.
   */
  public void textMergeReset() {

  }
  
  public void setList (DataRecList list) {
    this.list = list;
    loadFilterFields();
  }
  
  public void setTabs(TabPane tabs) {

    this.tabs = tabs;
    
    buildUI();
    
    tabs.getTabs().add(filterTab);
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();

		filterTab = new Tab("Filter");

		filterPane = new GridPane();
		fxUtils.applyStyle(filterPane);

		filterFieldsLabel = new Label("Add desired filter fields then Set the result:");
		filterPane.add(filterFieldsLabel, 0, 0, 3, 1);

		filterFieldsBox = new ComboBoxWidget();
    loadFilterFields();
    filterFieldsBox.setEditable(true);
    filterFieldsBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        currentFilterField 
            = (String)filterFieldsBox.getSelectionModel().getSelectedItem();
        loadFilterValues();
		  } // end handle method
		}); // end event handler
		filterPane.add(filterFieldsBox, 0, 1, 1, 1);
		filterFieldsBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filterFieldsBox, Priority.SOMETIMES);

		filterOperandBox = new ComboBoxWidget();
    for (int i = 0; i < PSField.WORD_LOGICAL_OPERANDS.length; i++) {
      filterOperandBox.addItem(PSField.WORD_LOGICAL_OPERANDS[i]);
    }
    filterOperandBox.getSelectionModel().select(0);
    currentFilterOperand = PSField.WORD_LOGICAL_OPERANDS[0];
    filterOperandBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        currentFilterOperand 
            = (String)filterOperandBox.getSelectionModel().getSelectedItem();
		  } // end handle method
		}); // end event handler
		filterPane.add(filterOperandBox, 1, 1, 1, 1);
		filterOperandBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filterOperandBox, Priority.SOMETIMES);

		filterValueBox = new ComboBoxWidget();
    filterValueBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        currentFilterValue 
            = (String)filterValueBox.getSelectionModel().getSelectedItem();
		  } // end handle method
		}); // end event handler

		filterPane.add(filterValueBox, 2, 1, 1, 1);
		filterValueBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filterValueBox, Priority.SOMETIMES);

		filterAddButton = new Button("Add");
		Tooltip filterAddButtonTip = new Tooltip("Add Field Filter to Parameter List");
    Tooltip.install(filterAddButton, filterAddButtonTip);
    filterAddButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        filterAdd();
		  } // end handle method
		}); // end event handler

		filterPane.add(filterAddButton, 0, 2, 1, 1);
		filterAddButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filterAddButton, Priority.SOMETIMES);

		filterClearButton = new Button("Clear");
		Tooltip filterClearButtonTip = new Tooltip("Clear all Filter Parameters");
    Tooltip.install(filterClearButton, filterClearButtonTip);
    filterClearButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        filterClear();
		  } // end handle method
		}); // end event handler

		filterPane.add(filterClearButton, 1, 2, 1, 1);
		filterClearButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filterClearButton, Priority.SOMETIMES);

		filterSetButton = new Button("Set");
		Tooltip filterSetButtonTip = new Tooltip("Set Table Filter Parameters as Specified Below");
    Tooltip.install(filterSetButton, filterSetButtonTip);
    filterSetButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        filterSetParams();
		  } // end handle method
		}); // end event handler
		filterPane.add(filterSetButton, 2, 2, 1, 1);
		filterSetButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filterSetButton, Priority.SOMETIMES);

		filterAndOrGroup = new ToggleGroup();

		filterAndButton = new RadioButton("And");
    filterAndButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (filterAndButton.isSelected()) {
          filterAndOr (true);
        }
		  } // end handle method
		}); // end event handler
		filterPane.add(filterAndButton, 0, 3, 1, 1);
		filterAndOrGroup.getToggles().add(filterAndButton);

		filterOrButton = new RadioButton("Or");
    filterOrButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (filterOrButton.isSelected()) {
          filterAndOr (false);
        }
		  } // end handle method
		}); // end event handler
		filterPane.add(filterOrButton, 1, 3, 2, 1);
		filterAndOrGroup.getToggles().add(filterOrButton);
    
    filterAndButton.setSelected(true);
    currentAndLogic = true;

		filterTextLabel = new Label("Resulting filter criteria will appear below:");
		filterPane.add(filterTextLabel, 0, 4, 3, 1);

		filterTextArea = new TextArea();
    filterText = new StringBuilder();
		filterPane.add(filterTextArea, 0, 5, 3, 1);
		filterTextArea.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filterTextArea, Priority.ALWAYS);
		filterTextArea.setMaxHeight(Double.MAX_VALUE);
		filterTextArea.setPrefRowCount(100);
		GridPane.setVgrow(filterTextArea, Priority.ALWAYS);
		filterTextArea.setPrefRowCount(100);
		filterTextArea.setWrapText(true);

		filterTab.setContent(filterPane);
		filterTab.setClosable(false);
  } // end method buildUI
  
  /**
   Select the tab for this panel. 
  */
  public void selectTab() {
    if (tabs != null) {
      tabs.getSelectionModel().select(filterTab);
    }
  }
  
  /**
     Play one recorded action in the Filter module.
   */
  public void playScript (
      String inActionAction,
      String inActionModifier,
      String inActionObject,
      String inActionValue) {
    
    if ((inActionAction.equals (ScriptConstants.SET_ACTION)) 
      && (inActionObject.equals (ScriptConstants.AND_OR_OBJECT))) {
      filterAndOr (Boolean.valueOf(inActionValue).booleanValue());
    }
    else
    if (inActionAction.equals (ScriptConstants.ADD_ACTION)) {
      currentFilterOperand = inActionModifier;
      currentFilterField = inActionObject;
      currentFilterValue = inActionValue;
      filterAdd();
    }
    else
    if (inActionAction.equals (ScriptConstants.CLEAR_ACTION)) {
      filterClear();
    }
    else
    if ((inActionAction.equals (ScriptConstants.SET_ACTION))
      && (inActionObject.equals (ScriptConstants.PARAMS_OBJECT))) {
      filterSetParams ();
    } // end valid actions
    else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM, 
        inActionAction + " " + inActionObject +
        " is not a valid Scripting Action for the Filter Module",
        true);
    } // end Action selector
  }
  
  /** 
     Load potential filter fields into the ComboBoxWidget.
   */
  private void loadFilterFields() {
		filterFieldsBox.load (list.getNames(), true);
		if (list.totalSize() > 0
        && filterFieldsBox.getItemCount() > 0) {
		  filterFieldsBox.getSelectionModel().select(0);
		  currentFilterField 
          = (String)filterFieldsBox.getSelectionModel().getSelectedItem();
		  loadFilterValues();
		} else {
		  currentFilterField = "";
		  if (filterValueBox != null && filterValueBox.getItemCount() > 0) {
        filterValueBox.removeAllItems();
      }
		}
  }
  
  /**
   Load values for the current filter field. 
  */
  private void loadFilterValues() {
    if (currentFilterField != null) {
      currentFilterColumn = list.getColumnNumber (currentFilterField);
      if (filterValueBox.getItemCount() > 0) {
        filterValueBox.removeAllItems();
      }
      for (int row = 0; row < list.getRowCount(); row++) {
        filterValueBox.addAlphabetical
            (list.getValueAt(row, currentFilterColumn).toString());
      }
      if (filterValueBox.getItemCount() > 0) {
        filterValueBox.setSelectedIndex(0);
        currentFilterValue = filterValueBox.getSelectedString();
      } else {
        currentFilterValue = "";
      }
    } // end if currentFilterField not null
  } // end method loadFilterValues
  
  private void filterAndOr (boolean currentAndLogic) {
    this.currentAndLogic = currentAndLogic;
    scriptRecorder.recordScriptAction (
        ScriptConstants.FILTER_MODULE, 
        ScriptConstants.SET_ACTION, 
        ScriptConstants.NO_MODIFIER, 
        ScriptConstants.AND_OR_OBJECT, 
        String.valueOf(currentAndLogic));
  }
  
  private void filterAdd () {
    fieldFilter = new PSFieldFilter (list, currentFilterField,
      currentFilterOperand, currentFilterValue);
    itemFilter.addFilter (fieldFilter);
    filterText.append (currentFilterField + " " + currentFilterOperand
      + " " + currentFilterValue + GlobalConstants.LINE_FEED_STRING);
    filterTextArea.setText(filterText.toString());
    scriptRecorder.recordScriptAction (
        ScriptConstants.FILTER_MODULE, 
        ScriptConstants.ADD_ACTION, 
        currentFilterOperand,
        currentFilterField, 
        currentFilterValue);
  }
  
  private void filterClear() {
    initItemFilter();
    scriptRecorder.recordScriptAction (
        ScriptConstants.FILTER_MODULE, 
        ScriptConstants.CLEAR_ACTION, 
        ScriptConstants.NO_MODIFIER, 
        ScriptConstants.NO_OBJECT, 
        ScriptConstants.NO_VALUE);
  }
  
  private void filterSetParams() {
    list.setInputFilter (itemFilter);
    textMergeController.setListAvailable(true);
    // filterDataSet();
    // dataTable.setDataSet (filteredDataSet);
    // dataTable.fireTableDataChanged();
    filterText.append ("The filter parameters listed above have been set." 
      + GlobalConstants.LINE_FEED_STRING);
    filterTextArea.setText(filterText.toString());
    scriptRecorder.recordScriptAction (
        ScriptConstants.FILTER_MODULE, 
        ScriptConstants.SET_ACTION, 
        ScriptConstants.NO_MODIFIER, 
        ScriptConstants.PARAMS_OBJECT, 
        ScriptConstants.NO_VALUE);
	}
  
  /**
   Initialize the item filter. 
  */
  public void initItemFilter () {
    itemFilter = new PSItemFilter (currentAndLogic);
    list.setInputFilter (itemFilter);
    filterText = new StringBuilder();
    if (filterTextArea != null) {
      filterTextArea.setText(filterText.toString());
    }
  }

}
