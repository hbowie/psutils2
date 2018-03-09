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

	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.list.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.script.*;
	import com.powersurgepub.psutils2.tabdelim.*;
	import com.powersurgepub.psutils2.ui.*;

  import java.io.*;

 	import javafx.event.*;
 	import javafx.scene.control.*;
	import javafx.scene.input.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;

/**
 The Text Merge module for writing output.

 @author Herb Bowie
 */
public class TextMergeOutput
      implements TextMergeResetter {

  private     Window              ownerWindow = null;
  private     DataRecList         list = null;
  private     TextMergeController textMergeController = null;
  private     TextMergeScript     textMergeScript = null;


  private     TabPane             tabs = null;
  private     MenuBar             menus = null;

  private     boolean             quietMode = true;
  private     boolean             tabSet = false;
  private     boolean             menuSet = false;
  
  private     FXUtils             fxUtils;
  private     Tab outputTab;
  private     GridPane outputPane;
  private     Button openOutputDataButton;
  private     Label openOutputDataLabel;
  private     Label outputDictionaryLabel;
  private     Label openOutputDataName;
  private     CheckBox outputDictionaryCkBox;
  private     Label outputPlaceHolder;

  private			MenuItem						fileSave;

  // Fields used for Output processing
  private     File                chosenOutputFile;
  private     String              tabNameOutput = "";
  private     FileName            tabFileName;
  private			TabDelimFile				tabFileOutput;

  // Data Dictionary Fields
	private			TabDelimFile				dictFile;
	private     DataDictionary      dataDict;
  private			boolean							usingDictionary = false;
  private     String              usingDictionaryValue = "No";
  public static final String      DICTIONARY_EXT = "dic";

  // Fields used for logging

  /** Log used to record events. */
  private    Logger             log = Logger.getShared();

  /** Should all data be logged (or only data preceding significant events(? */
  private    boolean            dataLogging = false;

  public TextMergeOutput (
      Window ownerWindow,
      DataRecList list, 
      TextMergeController textMergeController, 
      TextMergeScript textMergeScript) {

    this.ownerWindow = ownerWindow;
    this.list = list;
    this.textMergeController = textMergeController;
    this.textMergeScript = textMergeScript;
  }


  /**
   Let's reset as many variables as we can to restore the
   text merge state to its original condition.
   */
  public void textMergeReset() {

  }

  public void setList (DataRecList list) {
    this.list = list;
    setListAvailable(true);
  }

  public void setListAvailable (boolean listAvailable) {
    if (listAvailable) {

    } else {

    }
    if (openOutputDataButton != null) {
      openOutputDataButton.setDisable (! listAvailable);
    }
    if (fileSave != null) {
      fileSave.setDisable (! listAvailable);
    }
  }

  public void setMenus(MenuBar menus) {
    quietMode = false;
    menuSet = true;

    this.menus = menus;

    Menu fileMenu = null;
    boolean fileMenuFound = false;
    int i = 0;
    while (i < menus.getMenus().size() && (! fileMenuFound)) {
      MenuItem menuElement = menus.getMenus().get(i);
      if (menuElement instanceof Menu) {
        fileMenu = (Menu)menuElement;
        if (fileMenu.getText().equals("File")) {
          fileMenuFound = true;
        }
      }
    }

    // Equivalent Menu Item
    if (fileMenuFound) {
      fileSave = new MenuItem ("Save...");
      KeyCombination kc
        = new KeyCharacterCombination("S", KeyCombination.SHORTCUT_DOWN);
      fileSave.setAccelerator(kc);
      fileMenu.getItems().add (fileSave);
      fileSave.setOnAction(new EventHandler<ActionEvent>()
        {
          @Override
          public void handle(ActionEvent evt) {
            tabs.getSelectionModel().select(outputTab);
            saveOutputFile();
          } // end ActionPerformed method
        } // end action listener
      );
      fileSave.setDisable (true);
    }
  }

  public void setTabs(TabPane tabs) {
    quietMode = false;
    tabSet = true;

    this.tabs = tabs;
    
    buildUI();

    tabs.getTabs().add(outputTab);
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		outputTab = new Tab("Output");

		outputPane = new GridPane();
		fxUtils.applyStyle(outputPane);

		openOutputDataButton = new Button("Save Output");
		Tooltip openOutputDataButtonTip 
        = new Tooltip("Specify the Output File Name and Location");
    Tooltip.install(openOutputDataButton, openOutputDataButtonTip);
    openOutputDataButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        saveOutputFile();
		  } // end handle method
		}); // end event handler
		outputPane.add(openOutputDataButton, 0, rowCount, 1, 1);
		openOutputDataButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openOutputDataButton, Priority.SOMETIMES);
    openOutputDataButton.setDisable (true);

		rowCount++;

		openOutputDataLabel = new Label("Output Data Destination");
		fxUtils.applyHeadingStyle(openOutputDataLabel);
		outputPane.add(openOutputDataLabel, 0, rowCount, 1, 1);
		openOutputDataLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openOutputDataLabel, Priority.SOMETIMES);

		outputDictionaryLabel = new Label("Data Dictionary Output");
		fxUtils.applyHeadingStyle(outputDictionaryLabel);
		outputPane.add(outputDictionaryLabel, 1, rowCount, 1, 1);
		outputDictionaryLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(outputDictionaryLabel, Priority.SOMETIMES);

		rowCount++;

		openOutputDataName = new Label();
		fxUtils.applyHeadingStyle(openOutputDataName);
		outputPane.add(openOutputDataName, 0, rowCount, 1, 1);
		openOutputDataName.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openOutputDataName, Priority.SOMETIMES);

		outputDictionaryCkBox = new CheckBox("Save Companion Dictionary?");
    outputDictionaryCkBox.setSelected (false);
    usingDictionary = false;
    outputDictionaryCkBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        usingDictionary = outputDictionaryCkBox.isSelected();
        setDictionaryImplications();
		  } // end handle method
		}); // end event handler
		outputPane.add(outputDictionaryCkBox, 1, rowCount, 1, 1);
		outputDictionaryCkBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(outputDictionaryCkBox, Priority.SOMETIMES);

		outputPlaceHolder = new Label("                            ");
		outputPane.add(outputPlaceHolder, 2, rowCount, 1, 1);
		outputPlaceHolder.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(outputPlaceHolder, Priority.SOMETIMES);

		rowCount++;

		outputTab.setContent(outputPane);
		outputTab.setClosable(false);
  } // end method buildUI

  /**
     Open and save output file.
   */
  private void saveOutputFile() {
    FileChooser fileChooser = new FileChooser();
    if (textMergeScript.hasCurrentDirectory()) {
      fileChooser.setInitialDirectory (textMergeScript.getCurrentDirectory());
    }
    chosenOutputFile = fileChooser.showSaveDialog (ownerWindow);
    if (fileChooser != null) {
      writeOutput();
      openOutputDataName.setText (tabNameOutput);
    }
  }

  /**
     Depending on we are using a data dictionary, sets other
     appropriate values.
   */
  private void setDictionaryImplications() {
    if (usingDictionary) {
      usingDictionaryValue = "Yes";
    } else {
      usingDictionaryValue = "No";
    }
    textMergeScript.recordScriptAction
     (ScriptConstants.OUTPUT_MODULE,
      ScriptConstants.SET_ACTION,
      ScriptConstants.NO_MODIFIER,
      ScriptConstants.USING_DICTIONARY_OBJECT,
      String.valueOf(usingDictionary));
  }

  /**
     Play one recorded action in the Output module.
   */
  public void playScript (
      String  inActionAction,
      String  inActionModifier,
      String  inActionObject,
      String  inActionValue) {

    if (inActionAction.equals (ScriptConstants.OPEN_ACTION)) {
      chosenOutputFile = new File (inActionValue);
      writeOutput();
    } // end if action is output
    else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        inActionAction + " is not a valid Scripting Action for the Output Module",
        true);
    } // end Action selector

  } // end playInputModule method

  /**
   Write the output file, now matter how the request came in.

   The variable chosenOutputFile must be set to the appropriate value
   before calling this method.
  */
  private void writeOutput() {

    textMergeScript.setCurrentDirectoryFromFile (chosenOutputFile);
    tabFileOutput = new TabDelimFile (chosenOutputFile);
    tabFileOutput.setLog (log);
    tabFileOutput.setDataLogging (false);
    boolean outputOK = true;
    try {
      tabFileOutput.openForOutput (list.getRecDef());
    } catch (IOException e) {
      outputOK = false;
      log.recordEvent (LogEvent.MEDIUM,
        "Problem opening Output File",
        false);
    }
    if (outputOK) {
      list.openForInput();
      DataRecord inRec;
      int count = 0;
      do {
        inRec = list.nextRecordIn ();
        if (inRec != null) {
          try {
            tabFileOutput.nextRecordOut (inRec);
            count++;
          } catch (IOException e) {
            log.recordEvent (LogEvent.MEDIUM,
              "Problem writing to Output File",
              true);
          }
        } // end if in rec not null
      } while (list.hasMoreRecords());

      list.close();

      try {
        tabFileOutput.close();
      } catch (IOException e) {
      }

      log.recordEvent(LogEvent.NORMAL,
          String.valueOf(count) + " records output",
          false);

      tabNameOutput = chosenOutputFile.getName();
      openOutputDataName.setText (tabNameOutput);
      if (usingDictionary) {
        tabFileName =
          new FileName (chosenOutputFile.getAbsolutePath());
        dictFile =
          new TabDelimFile (textMergeScript.getCurrentDirectory(),
            tabFileName.replaceExt(DICTIONARY_EXT));
        dictFile.setLog (log);
        try {
          dataDict.store (dictFile);
        } catch (IOException e) {
          log.recordEvent (LogEvent.MEDIUM,
              "Problem writing Output Dictionary",
              true);
        }
      } // end if using dictionary

      textMergeScript.recordScriptAction (
          ScriptConstants.OUTPUT_MODULE,
          ScriptConstants.OPEN_ACTION,
          ScriptConstants.NO_MODIFIER,
          ScriptConstants.NO_OBJECT,
          chosenOutputFile.getAbsolutePath());

    } // end if output ok

  }

}
