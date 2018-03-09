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
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.script.*;
	import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.util.*;

 	import javafx.event.*;
 	import javafx.geometry.*;
 	import javafx.scene.control.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;


/**
 The sorting module used as part of PSTextMerge.

 @author Herb Bowie.
 */
public class TextMergeSort
      implements TextMergeResetter {
  
  private     Window              ownerWindow = null;
  private     DataRecList         list = null;
  private     TextMergeController textMergeController = null;
  private     TextMergeScript     scriptRecorder = null;

  private     boolean             combineAllowed = true;
  private     TabPane             tabs = null;
  private     MenuBar             menus = null;

  private     ArrayList           sortDirections = new ArrayList();
  
  private     FXUtils             fxUtils;
  private     Tab                 sortTab;
  private     GridPane            sortPane;
  private     Label               sortFieldsLabel;
  private     ComboBoxWidget      sortFieldsBox;
  private     ComboBoxWidget      sortDirectionBox;
  private     Button              sortAddButton;
  private     Button              sortClearButton;
  private     Button              sortSetButton;
  private     Label               combineFieldsLabel;
  private     Button              combineButton;
  private     Label               combineToleranceLabel;
  private     Label               combineMinNoLossLabel;
  private     ToggleGroup         combineToleranceGroup;
  private     RadioButton         combineToleranceNoLossButton;
  private     TextField           combineMinNoLossValue;
  private     RadioButton         combineToleranceLaterButton;
  private     Button              combineMinNoLossUpButton;
  private     RadioButton         combineToleranceEarlierButton;
  private     Button              combineMinNoLossDownButton;
  private     RadioButton         combineToleranceAppendButton;
  private     Label               sortTextLabel;
  private     StringBuilder       sortText;
  private     TextArea            sortTextArea;
  
  // Sort Panel Objects

  private    static final String  NO_DATA_LOSS_STRING
                = "No Data Loss";
  private		 static final String  LATER_OVERRIDES_STRING
                = "Later Records Override Earlier";
  private    static final String  EARLIER_OVERRIDES_STRING
                = "Earlier Records Override Later";
  private    static final String  COMBINED_STRING
                = "Combine Fields Where Allowed";

  // Fields used for sorting
  private			boolean							sorted = false;
	private     PSItemComparator    itemComparator;
	private     String              currentSortField;
	private     String              currentSortDirection;

  // Fields used for combining
  private     int                 dataLossTolerance = 0;
  private			int                 precedence = +1;
  private			int									minNoLoss = 0;
  private			int									totalCombinations = -1;

  private     boolean             clearBeforeAdd = false;

  public TextMergeSort (
      Window ownerWindow,
      DataRecList list, 
      TextMergeController textMergeController, 
      TextMergeScript scriptRecorder) {
    
    this.ownerWindow = ownerWindow;
    this.list = list;
    this.textMergeController = textMergeController;
    this.scriptRecorder = scriptRecorder;
  }


  /**
   Let's reset as many variables as we can to restore the
   text merge state to its original condition.
   */
  public void textMergeReset() {

  }

  public void setList (DataRecList list) {
    this.list = list;
    loadSortFields();
  }

  public void setTabs (TabPane tabs, boolean combineAllowed) {

    this.tabs = tabs;
    this.combineAllowed = combineAllowed;
    buildUI();
    initSortSpec();
    tabs.getTabs().add(sortTab);
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();

		sortTab = new Tab("Sort");

		sortPane = new GridPane();
		fxUtils.applyStyle(sortPane);

		sortFieldsLabel = new Label("Add desired sort fields then Set the result:");
		sortPane.add(sortFieldsLabel, 0, 0, 3, 1);

		sortFieldsBox = new ComboBoxWidget();
    loadSortFields();
    sortFieldsBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        currentSortField = sortFieldsBox.getSelectedString();
		  } // end handle method
		}); // end event handler
		sortPane.add(sortFieldsBox, 0, 1, 2, 1);
		sortFieldsBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(sortFieldsBox, Priority.SOMETIMES);

		sortDirectionBox = new ComboBoxWidget();
    sortDirections.add(PSFieldComparator.ASCENDING);
    sortDirections.add(PSFieldComparator.DESCENDING);
    sortDirectionBox.load(sortDirections, true);
		sortDirectionBox.setSelectedIndex (0);
		currentSortDirection = sortDirectionBox.getSelectedString();
    sortDirectionBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
		    currentSortDirection = sortDirectionBox.getSelectedString();
		  } // end handle method
		}); // end event handler
		sortPane.add(sortDirectionBox, 2, 1, 1, 1);
		sortDirectionBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(sortDirectionBox, Priority.SOMETIMES);

		sortAddButton = new Button("Add");
		Tooltip sortAddButtonTip = new Tooltip("Add Field and Direction to Sort Parameters");
    Tooltip.install(sortAddButton, sortAddButtonTip);
    sortAddButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
		    sortAdd();
		  } // end handle method
		}); // end event handler
		sortPane.add(sortAddButton, 0, 2, 1, 1);
		sortAddButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(sortAddButton, Priority.SOMETIMES);

		sortClearButton = new Button("Clear");
		Tooltip sortClearButtonTip = new Tooltip("Clear all Sort Parameters");
    Tooltip.install(sortClearButton, sortClearButtonTip);
    sortClearButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
		    sortClear();
		  } // end handle method
		}); // end event handler
		sortPane.add(sortClearButton, 1, 2, 1, 1);
		sortClearButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(sortClearButton, Priority.SOMETIMES);

		sortSetButton = new Button("Set");
		Tooltip sortSetButtonTip = new Tooltip("Set Table Sort Parameters as Specified Below");
    Tooltip.install(sortSetButton, sortSetButtonTip);
    sortSetButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        sortSetParams();
		  } // end handle method
		}); // end event handler
		sortPane.add(sortSetButton, 2, 2, 1, 1);
		sortSetButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(sortSetButton, Priority.SOMETIMES);

    if (combineAllowed) {
      combineFieldsLabel = new Label
        ("After setting a sort sequence, optionally combine records with duplicate keys");
      sortPane.add(combineFieldsLabel, 0, 3, 3, 1);

      combineButton = new Button("Combine");
      Tooltip combineButtonTip = new Tooltip("Combine Records Using Parameters Shown");
      Tooltip.install(combineButton, combineButtonTip);
      combineButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          if (sorted) {
            combineSet();
          }
          else {
            Trouble.getShared().report(
              "Data must be sorted before it can be combined", 
              "Sort/Combine Error");
          } // end if not sorted
        } // end handle method
      }); // end event handler
      sortPane.add(combineButton, 0, 4, 1, 1);
      combineButton.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(combineButton, Priority.SOMETIMES);

      combineToleranceLabel = new Label("Tolerance for Data Loss");
      fxUtils.applyHeadingStyle(combineToleranceLabel);
      sortPane.add(combineToleranceLabel, 1, 4, 1, 1);
      combineToleranceLabel.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(combineToleranceLabel, Priority.SOMETIMES);

      combineMinNoLossLabel = new Label("Minimum Number of Lossless Fields");
      fxUtils.applyHeadingStyle(combineMinNoLossLabel);
      sortPane.add(combineMinNoLossLabel, 2, 4, 1, 1);
      combineMinNoLossLabel.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(combineMinNoLossLabel, Priority.SOMETIMES);

      combineToleranceGroup = new ToggleGroup();

      combineToleranceNoLossButton = new RadioButton("No Data Loss");
      combineToleranceNoLossButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          dataLossTolerance = DataField.NO_DATA_LOSS;
        } // end handle method
      }); // end event handler
      sortPane.add(combineToleranceNoLossButton, 1, 5, 1, 1);
      combineToleranceGroup.getToggles().add(combineToleranceNoLossButton);

      combineMinNoLossValue = new TextField("0");
      combineMinNoLossValue.setAlignment(Pos.BASELINE_RIGHT);
      combineMinNoLossValue.setEditable(false);
      combineMinNoLossValue.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {

        } // end handle method
      }); // end event handler
      sortPane.add(combineMinNoLossValue, 2, 5, 1, 1);
      combineMinNoLossValue.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(combineMinNoLossValue, Priority.SOMETIMES);

      combineToleranceLaterButton = new RadioButton("Later Records Override Earlier");
      combineToleranceLaterButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          dataLossTolerance = DataField.DATA_OVERRIDE;
          precedence = DataField.LATER_OVERRIDES;
        } // end handle method
      }); // end event handler
      sortPane.add(combineToleranceLaterButton, 1, 6, 1, 1);
      combineToleranceGroup.getToggles().add(combineToleranceLaterButton);

      combineMinNoLossUpButton = new Button("Increment (+)");
      Tooltip combineMinNoLossUpButtonTip = new Tooltip("Increase Minimum Number of Lossless Fields");
      Tooltip.install(combineMinNoLossUpButton, combineMinNoLossUpButtonTip);
      combineMinNoLossUpButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          minNoLoss++;
          combineMinNoLossValue.setText (String.valueOf(minNoLoss));
        } // end handle method
      }); // end event handler
      sortPane.add(combineMinNoLossUpButton, 2, 6, 1, 1);
      combineMinNoLossUpButton.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(combineMinNoLossUpButton, Priority.SOMETIMES);

      combineToleranceEarlierButton = new RadioButton("Earlier Records Override Later");
      combineToleranceEarlierButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          dataLossTolerance = DataField.DATA_OVERRIDE;
          precedence = DataField.EARLIER_OVERRIDES;
        } // end handle method
      }); // end event handler
      sortPane.add(combineToleranceEarlierButton, 1, 7, 1, 1);
      combineToleranceGroup.getToggles().add(combineToleranceEarlierButton);

      combineMinNoLossDownButton = new Button("Decrement (-)");
      Tooltip combineMinNoLossDownButtonTip = new Tooltip("Decrease Minimum Number of Lossless Fields");
      Tooltip.install(combineMinNoLossDownButton, combineMinNoLossDownButtonTip);
      combineMinNoLossDownButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          if (minNoLoss > 0) {
            minNoLoss--;
          }
          combineMinNoLossValue.setText (String.valueOf(minNoLoss));
        } // end handle method
      }); // end event handler
      sortPane.add(combineMinNoLossDownButton, 2, 7, 1, 1);
      combineMinNoLossDownButton.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(combineMinNoLossDownButton, Priority.SOMETIMES);

      combineToleranceAppendButton = new RadioButton("Combine Fields Where Allowed");
      combineToleranceAppendButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          dataLossTolerance = DataField.DATA_COMBINED;
        } // end handle method
      }); // end event handler
      sortPane.add(combineToleranceAppendButton, 1, 8, 1, 1);
      combineToleranceGroup.getToggles().add(combineToleranceAppendButton);
      
      combineToleranceNoLossButton.setSelected (true);
      dataLossTolerance = DataField.NO_DATA_LOSS;
    }

		sortTextLabel = new Label("Resulting sort criteria will appear below:");
		sortPane.add(sortTextLabel, 0, 9, 3, 1);

		sortTextArea = new TextArea();
		sortPane.add(sortTextArea, 0, 10, 3, 1);
		sortTextArea.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(sortTextArea, Priority.ALWAYS);
		sortTextArea.setMaxHeight(Double.MAX_VALUE);
		sortTextArea.setPrefRowCount(100);
		GridPane.setVgrow(sortTextArea, Priority.ALWAYS);
		sortTextArea.setPrefRowCount(100);
		sortTextArea.setWrapText(true);

		sortTab.setContent(sortPane);
		sortTab.setClosable(false);
  } // end method buildUI


  /**
   Select the tab for this panel.
  */
  public void selectTab() {
    if (tabs != null) {
      tabs.getSelectionModel().select(sortTab);
    }
  }

  public boolean isCombineAllowed() {
    return combineAllowed;
  }

  /**
     Play one recorded action in the Sort module.
   */
  public void playSortModule (
      String inActionAction,
      String inActionModifier,
      String inActionObject) {

    if (inActionAction.equals (ScriptConstants.ADD_ACTION)) {
      currentSortDirection = inActionModifier;
      currentSortField = inActionObject;
      sortAdd();
    }
    else
    if (inActionAction.equals (ScriptConstants.CLEAR_ACTION)) {
      sortClear();
    }
    else
    if ((inActionAction.equals (ScriptConstants.SET_ACTION))
      && (inActionObject.equals (ScriptConstants.PARAMS_OBJECT))) {
      sortSetParams ();
    } // end valid actions
    else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        inActionAction + " " + inActionObject +
        " is not a valid Scripting Action for the Sort Module",
        true);
    } // end Action selector
  } // end playSortModule method

  /**
     Play one recorded action in the Combine module.
   */
  public void playCombineModule (
      String inActionAction,
      String inActionModifier,
      String inActionObject,
      String inActionValue,
      int    inActionValueAsInt,
      boolean inActionValueValidInt) {
    if (inActionAction.equals (ScriptConstants.ADD_ACTION)) {
      if (inActionObject.equals (ScriptConstants.DATA_LOSS_OBJECT)) {
        dataLossTolerance = inActionValueAsInt;
        if (! inActionValueValidInt) {
          Logger.getShared().recordEvent (LogEvent.MEDIUM,
            inActionValue +
            " is not a valid Combine Data Loss Tolerance Value",
            true);
        } // end if invalid int
      } // end if data loss value
      else
      if (inActionObject.equals (ScriptConstants.PRECEDENCE_OBJECT)) {
        precedence = inActionValueAsInt;
        if (! inActionValueValidInt) {
          Logger.getShared().recordEvent (LogEvent.MEDIUM,
            inActionValue +
            " is not a valid Combine Precedence Value",
            true);
        } // end if invalid int
      } // end if precedence value
      else
      if (inActionObject.equals (ScriptConstants.MIN_NO_LOSS_OBJECT)) {
        minNoLoss = inActionValueAsInt;
        if (! inActionValueValidInt) {
          Logger.getShared().recordEvent (LogEvent.MEDIUM,
            inActionValue +
            " is not a valid Combine Minimum Records with No Loss Value",
            true);
        } // end if invalid int
      } // end if Minimum No Loss value
      else {
        Logger.getShared().recordEvent (LogEvent.MEDIUM,
          inActionObject +
          " is not a valid Combine Parameter",
          true);
      } // end no known combine object
    } // end if add action
    else
    if ((inActionAction.equals (ScriptConstants.SET_ACTION))
      && (inActionObject.equals (ScriptConstants.PARAMS_OBJECT))) {
      combineSet ();
    } // end valid actions
    else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        inActionAction + " " + inActionObject +
        " is not a valid Scripting Action for the Combine Module",
        true);
    } // end Action selector
  } // end playCombineModule method

  /**
     Load potential sort fields into the JComboBox.
   */
  private void loadSortFields() {
		sortFieldsBox.load (list.getNames(), true);
		if (list.totalSize() > 0
        && sortFieldsBox.getItemCount() > 0) {
		  sortFieldsBox.setSelectedIndex (0);
		  currentSortField = sortFieldsBox.getSelectedString();
		} else {
		  currentSortField = "";
		}
  }

  private void sortAdd() {

    if (clearBeforeAdd) {
      itemComparator = new PSItemComparator (list);
    }

    itemComparator.addField (currentSortField, currentSortDirection);

    clearBeforeAdd = false;

    sortText.append (currentSortField + " " + currentSortDirection
      + GlobalConstants.LINE_FEED_STRING);
    sortTextArea.setText(sortText.toString());
    scriptRecorder.recordScriptAction (
      ScriptConstants.SORT_MODULE,
      ScriptConstants.ADD_ACTION,
      currentSortDirection,
      currentSortField,
      ScriptConstants.NO_VALUE);
  }

  private void sortClear() {
    initSortSpec();
    scriptRecorder.recordScriptAction (
      ScriptConstants.SORT_MODULE,
      ScriptConstants.CLEAR_ACTION,
      ScriptConstants.NO_MODIFIER,
      ScriptConstants.NO_OBJECT,
      ScriptConstants.NO_VALUE);
  }

  private void initSortSpec () {
    itemComparator = new PSItemComparator (list);
    clearBeforeAdd = false;
    sortText = new StringBuilder();
    sortTextArea.setText(sortText.toString());
    sorted = false;
  }

  private void sortSetParams() {
    list.setComparator(itemComparator);
    textMergeController.setListAvailable(true);
    sorted = true;
    sortText.append ("The sort parameters listed above have been set."
      + GlobalConstants.LINE_FEED_STRING);
    sortTextArea.setText(sortText.toString());
    scriptRecorder.recordScriptAction (
      ScriptConstants.SORT_MODULE,
      ScriptConstants.SET_ACTION,
      ScriptConstants.NO_MODIFIER,
      ScriptConstants.PARAMS_OBJECT,
      ScriptConstants.NO_VALUE);

    clearBeforeAdd = true;
  }

  private void combineSet() {
    String msg = "";

    if (sorted) {
      if (dataLossTolerance == 0) {
        msg = "No data loss tolerated";
      }
      else
      if (dataLossTolerance == 1) {
        msg = "One field may override another";
      }
      else {
        msg = "Some fields may be combined";
      }
      sortText.append (msg + GlobalConstants.LINE_FEED_STRING);
      scriptRecorder.recordScriptAction (
        ScriptConstants.COMBINE_MODULE,
        ScriptConstants.ADD_ACTION,
        ScriptConstants.NO_MODIFIER,
        ScriptConstants.DATA_LOSS_OBJECT,
        String.valueOf (dataLossTolerance));

      if (dataLossTolerance > 0) {
        if (precedence > 0) {
          msg = "Later fields override earlier ones";
        }
        else
        if (precedence < 0) {
          msg = "Earlier fields override later ones";
        }
        else {
          msg = "No precedence established";
        }
        sortText.append (msg + GlobalConstants.LINE_FEED_STRING);
      }
      scriptRecorder.recordScriptAction (
        ScriptConstants.COMBINE_MODULE,
        ScriptConstants.ADD_ACTION,
        ScriptConstants.NO_MODIFIER,
        ScriptConstants.PRECEDENCE_OBJECT,
        String.valueOf (precedence));

      if (dataLossTolerance > 0) {
        sortText.append ("At least "
            + String.valueOf (minNoLoss)
            + " fields must suffer no data loss"
            + GlobalConstants.LINE_FEED_STRING);
      }
      scriptRecorder.recordScriptAction (
        ScriptConstants.COMBINE_MODULE,
        ScriptConstants.ADD_ACTION,
        ScriptConstants.NO_MODIFIER,
        ScriptConstants.MIN_NO_LOSS_OBJECT,
        String.valueOf (minNoLoss));

      // combineDataSet ();
      // dataTable.fireTableDataChanged();

      sortText.append (String.valueOf (totalCombinations)
        + " records combined."
        + GlobalConstants.LINE_FEED_STRING);
      scriptRecorder.recordScriptAction (
        ScriptConstants.COMBINE_MODULE,
        ScriptConstants.SET_ACTION,
        ScriptConstants.NO_MODIFIER,
        ScriptConstants.PARAMS_OBJECT,
        ScriptConstants.NO_VALUE);
      
      sortTextArea.setText(sortText.toString());
    } // end if sorted
  }

}
