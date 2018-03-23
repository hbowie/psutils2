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

	import com.powersurgepub.psutils2.env.*;
	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.list.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.script.*;
	import com.powersurgepub.psutils2.tabdelim.*;
	import com.powersurgepub.psutils2.txmin.*;
	import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.net.*;
  import java.util.*;
  import java.util.zip.*;

 	import javafx.collections.*;
 	import javafx.event.*;
  import javafx.geometry.*;
 	import javafx.scene.control.*;
 	import javafx.scene.control.Alert.*;
  import javafx.scene.input.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;

/**
 The Text Merge module for reading input files. 

 @author Herb Bowie
 */
public class TextMergeInput
      implements TextMergeResetter {
  
  private   static  final String  MIME_TYPE = "mimetype";
  
  // Maximum value for normalization Type
  private		static	final int			NORMALTYPE_MAX = 1;
  
  private     Window              ownerWindow = null;
  private     DataRecList         dataRecList = null;
  
  private     TextMergeController textMergeController = null;
  private     TextMergeScript     textMergeScript = null;
  
  private     FXUtils             fxUtils;
  
  private     File                appFolder;
  private     URL                 pageURL;
  
  private     TabPane             tabs = null;
  private     MenuBar             menus = null;
  
  private     boolean             quietMode = true;
  private     boolean             tabSet = false;
  private     boolean             menuSet = false;
  
  // Input Modules
  private     ArrayList<TextMergeInputModule> inputModules = new ArrayList();
  
  private     TextMergeInputModule  inputModule;
  
  private     TextMergeInputTDF     inTDF = new TextMergeInputTDF();
  private     TextMergeInputDirEntry inDir = new TextMergeInputDirEntry();
  private     TextMergeInputHTML    inHTML = new TextMergeInputHTML();
  private     TextMergeInputXML     inXML = new TextMergeInputXML();
  private     TextMergeInputExcel   inExcel = new TextMergeInputExcel();
  private     TextMergeInputTunes   inTunes = new TextMergeInputTunes();
  private     TextMergeInputClub    inClub = new TextMergeInputClub();
  // private     TextMergeInputOutline inOutline = new TextMergeInputOutline();
  private     TextMergeInputGrid    inGrid = new TextMergeInputGrid();
  // private     TextMergeInputReturnedMail inMail = new TextMergeInputReturnedMail();
  private     TextMergeInputYojimbo inYojimbo = new TextMergeInputYojimbo();
  // private     TextMergeInputYAML    inYAML = new TextMergeInputYAML();
  private     TextMergeInputMetaMarkdown inMarkdown = new TextMergeInputMetaMarkdown();
  private     TextMergeInputNotenik inNotenik = new TextMergeInputNotenik();
  private     TextMergeInputMacApps inMacApps = new TextMergeInputMacApps();
  private     TextMergeInputVCard   inVCard   = new TextMergeInputVCard();
  
  private     int                   inputModuleIndex = 0;
  
  private     boolean               inputModuleFound = false;
  
  // Menu Objects
  private			MenuItem						  fileOpen;
  private     MenuItem              fileEpub;
  
  // Fields used for the User Interface
   
  
  private     Tab                   inputTab;
  
  // Input Panel objects
  private     GridPane              inputPane;
  
  // Open Input Button
  private     Button                openDataButton;
  
  // Input Type Drop Down List
  private     Label                 inputTypeLabel;
  
  private     ComboBox              inputTypeBox  = new ComboBox ();
  
  // Data Dictionary Check Box
  private    Label                  inputDictionaryLabel;
  private    CheckBox               inputDictionaryCkBox;
  
  // Explode Tags Check Box
  private    Label                  explodeTagsLabel;
  private    CheckBox               explodeTagsCkBox;
  
  // Merge Radio Buttons
  private    Label                  inputMergeLabel;
  private		 ToggleGroup            inputMergeGroup;
  private 	 RadioButton            inputMergeNoButton;
  private    RadioButton            inputMergeButton;
  private		 RadioButton            inputMergeSameColumnsButton;
  
  // Directory Depth
  private    Label                  inputDirMaxDepthLabel;
  private    TextField              inputDirMaxDepthValue;
  private    Button                 inputDirMaxDepthUpButton;
  private    Button                 inputDirMaxDepthDownButton;
  
  // Normalization Type Drop Down List
  private    Label                  inputNormalLabel;
  public    static final String       INPUT_NORMAL0 = 		"None";
  public    static final String       INPUT_NORMAL1 = 		"Boeing Docs";
  
  private     ObservableList<String> inputNormalTypes 
      = FXCollections.<String>observableArrayList(INPUT_NORMAL0, INPUT_NORMAL1);
  private     ComboBox            inputNormalBox  = new ComboBox (inputNormalTypes);
  
  private     String              possibleFileName = "";
  
  // File chosen as input Tab-Delimited File.
  private     File                chosenFile = null;
  
  private     DataSource          dataSource = null;
  
  private     String              fileName = "";
  
  private     String              fileNameToDisplay = "";
  
  private     String              tabName = "";
  
  private     FileName            tabFileName;
  
  private     URL                 tabURL;
  
  private			int									dirMaxDepth = 1;
  
  // Normalization Fields
  private			boolean							normalization = false;
  private static final String     NORMALIZATION_KEY = "normalization";
  private			int									normalType = 0;
  private     String							normalTypeValue = "No Normalization";
  private     DataSource          normalizer;
  
  // Data Dictionary Fields
	private			TabDelimFile				dictFile;
	private     DataDictionary      dataDict;
  private			boolean							usingDictionary = false;
  private     String              usingDictionaryValue = "No";
  public static final String      DICTIONARY_EXT = "dic";
  
  // Tags Explosion Fields
  private			boolean							explodeTags = false;
  
  // Merge Fields
  private			int							 		merge = 0;
  private			String							mergeValue = "No";
  
  private     Logger              log = Logger.getShared();
  
  private			String							inputObject = "";
  
  // Epub files
  private     File                epubFolder;
  private     File                epubFile;
    
  public TextMergeInput (
      Window ownerWindow,
      DataRecList dataRecList, 
      TextMergeController textMergeController, 
      TextMergeScript textMergeScript) {
    
    this.ownerWindow = ownerWindow;
    this.dataRecList = dataRecList;
    
    setListOptions();
    
    this.textMergeController = textMergeController;
    this.textMergeScript = textMergeScript;
    
    appFolder = Home.getShared().getAppFolder();
    try {
      pageURL = appFolder.toURI().toURL(); 
    } catch (MalformedURLException e) {
      Trouble.getShared().report ("Trouble forming pageURL from " + appFolder.toString(), 
          "URL Problem");
    }
    
    // Initialization related to input modules
    int insertAt = 0;
    insertAt = addInputModule(inTDF, insertAt);
    insertAt = addInputModule(inClub, insertAt);
    insertAt = addInputModule(inExcel, insertAt);
    insertAt = addInputModule(inDir, insertAt);
    insertAt = addInputModule(inGrid, insertAt);
    insertAt = addInputModule(inHTML, insertAt);
    insertAt = addInputModule(inTunes, insertAt);
    insertAt = addInputModule(inMacApps, insertAt);
    insertAt = addInputModule(inMarkdown, insertAt);
    insertAt = addInputModule(inNotenik, insertAt);
    // insertAt = addInputModule(inOutline, insertAt);
    // insertAt = addInputModule(inMail, insertAt);
    insertAt = addInputModule(inVCard, insertAt);
    insertAt = addInputModule(inXML, insertAt);
    // insertAt = addInputModule(inYAML, insertAt);
    insertAt = addInputModule(inYojimbo, insertAt);
    
    inputTypeBox.getSelectionModel().select (0);
    
    // Normalization init
    String normalProperty = UserPrefs.getShared().getPref (NORMALIZATION_KEY);
    normalization = Boolean.valueOf(normalProperty).booleanValue();
    if (! normalization) {
      File boeing = new File (Home.getShared().getAppFolder(), "boeing.txt");
      if (boeing.exists()) {
        normalization = true;
      } else {

      }
    }
    
    // Open file if it was passed as a parameter
    possibleFileName = System.getProperty ("tabfile", "");
    if ((possibleFileName != null) && (! possibleFileName.equals (""))) {
      fileName = possibleFileName;
      try {
        tabURL = new URL (pageURL, fileName);
        openURL();
      } catch (MalformedURLException e) {
        // Shouldn't happen
      }
    } else {
      openEmpty();
    }
  }

  /**
   Let's reset as many variables as we can to restore the
   text merge state to its original condition.
   */
  public void textMergeReset() {

  }

  public void setList (DataRecList dataRecList) {
    this.dataRecList = dataRecList;
    setListOptions();
    textMergeController.setListAvailable(dataRecList != null);
  }
  
  private void setListOptions() {
    /*
    if (psList instanceof DataRecList) {
      dataRecList = (DataRecList)psList;
    } else {
      dataRecList = null;
      throw new IllegalArgumentException("List must be a DataRecList");
    }
    */
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
    
    // Equivalent Menu Item for File Open
    if (fileMenuFound) {
      fileOpen = new MenuItem ("Open...");
      KeyCombination openKC
        = new KeyCharacterCombination("O", KeyCombination.SHORTCUT_DOWN);
      fileOpen.setAccelerator(openKC);
      fileMenu.getItems().add (fileOpen);
      fileOpen.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          tabs.getSelectionModel().select (inputTab);
          chooseAndOpen();	
        }
      });
      
      // Menu Item for EPub
      fileEpub = new MenuItem ("Create EPub...");
      fileMenu.getItems().add (fileEpub);
      fileEpub.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          chooseEpubFiles();	
        }
      });
          
    } // end if file menu found
  } // end method setMenus
  
  /**
   Build the input tab and add it to the tabpane that is being passed. 
  
   @param tabs The tabpane to receive the new tab. 
  */
  public void setTabs(TabPane tabs) {
    quietMode = false;
    tabSet = true;
    
    this.tabs = tabs;
    
    fxUtils = FXUtils.getShared();
    
    inputTab = new Tab("Input");
    
		inputPane = new GridPane();
    fxUtils.applyStyle(inputPane);
    
    //
    // First Column
    //
    
    // Button to Specify the Input Source and Open it
    // Let's put this in the top left corner
    openDataButton = new Button ("Open Input");
    Tooltip openDataTip = new Tooltip("Specify the Data Source to be Input");
    Tooltip.install(openDataButton, openDataTip);
    openDataButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        chooseAndOpen();
      }
    });
    openDataButton.setMaxWidth(Double.MAX_VALUE);
    openDataButton.setMaxWidth(Double.MAX_VALUE);
    inputPane.add(openDataButton, 0, 0, 1, 1);
    GridPane.setHgrow(openDataButton, Priority.SOMETIMES);
        
		// Label for Input Type
    inputTypeLabel = new Label ("Type of Data Source");
    fxUtils.addLabelHeading(inputTypeLabel, 0, 1);
    
    // Combo Box for Input Type
		inputTypeBox.setEditable (false);
		inputTypeBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle (ActionEvent event) {
		      String inType = (String)inputTypeBox.getSelectionModel().getSelectedItem();
          initInputModules();
          inputModuleIndex = 0;
          inputModuleFound = false;
          while (inputModuleIndex < inputModules.size() && (! inputModuleFound)) {
            inputModule = inputModules.get(inputModuleIndex);
            inputModuleFound = inputModule.setInputType(inType);
            if (! inputModuleFound) {
              inputModuleIndex++;
            }
          }
		    } // end ActionPerformed method
		  } // end action listener for input type combo box
		); 
    inputTypeBox.setMaxWidth(Double.MAX_VALUE);
    inputPane.add(inputTypeBox, 0, 2);
    GridPane.setHgrow(inputTypeBox, Priority.SOMETIMES);
    
    // create directory depth fields
    inputDirMaxDepthLabel = new Label ("Maximum Directory Depth");
    fxUtils.addLabelHeading(inputDirMaxDepthLabel, 0, 5);
    
    inputDirMaxDepthValue = new TextField (String.valueOf(dirMaxDepth));
    inputDirMaxDepthValue.setEditable (false);
    inputDirMaxDepthValue.setAlignment(Pos.BASELINE_RIGHT);
    inputPane.add(inputDirMaxDepthValue, 0, 6);
    GridPane.setHgrow(inputDirMaxDepthValue, Priority.SOMETIMES);
    
    inputDirMaxDepthUpButton = new Button ("Increment (+)");
    // inputDirMaxDepthUpButton.setBorder (raisedBevel);
    inputDirMaxDepthUpButton.setTooltip
      (new Tooltip("Increase Level of Sub-Directory Explosion"));
    inputDirMaxDepthUpButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        dirMaxDepth++;
        inputDirMaxDepthValue.setText (String.valueOf(dirMaxDepth));
      }
    });
    inputDirMaxDepthUpButton.setMaxWidth(Double.MAX_VALUE);
    inputPane.add(inputDirMaxDepthUpButton, 0, 7, 1, 1);
    GridPane.setHgrow(inputDirMaxDepthUpButton, Priority.SOMETIMES);
    
    inputDirMaxDepthDownButton = new Button ("Decrement (-)");
    // inputDirMaxDepthDownButton.setBorder (raisedBevel);
    inputDirMaxDepthDownButton.setTooltip
      (new Tooltip("Decrease Level of Sub-Directory Explosion"));
    inputDirMaxDepthDownButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (dirMaxDepth > 1) {
            dirMaxDepth--;
        }
        inputDirMaxDepthValue.setText (String.valueOf(dirMaxDepth));
      }
    });
    inputDirMaxDepthDownButton.setMaxWidth(Double.MAX_VALUE);
    inputPane.add(inputDirMaxDepthDownButton, 0, 8, 1, 1);
    GridPane.setHgrow(inputDirMaxDepthDownButton, Priority.SOMETIMES);
    
    //
    // Second Column
    //
    
    // Data Dictionary Label
    inputDictionaryLabel = new Label ("Data Dictionary Input");
    fxUtils.addLabelHeading(inputDictionaryLabel, 1, 1);
    
    // Input Dictionary Checkbox
    inputDictionaryCkBox = new CheckBox ("Open Companion Dictionary?");
    inputDictionaryCkBox.setSelected (false);
    inputDictionaryCkBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        usingDictionary = inputDictionaryCkBox.isSelected();
      }
    });
    inputPane.add(inputDictionaryCkBox, 1, 2, 1, 1);
    GridPane.setHgrow(inputDictionaryCkBox, Priority.SOMETIMES);
    
  		// Combo box for Normalization type
    inputNormalLabel = new Label ("Data Normalization");
    
		normalType = 0;
    if (normalization) {
      inputNormalBox.getSelectionModel().select(0);
      inputNormalBox.setEditable (false);
      inputNormalBox.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          String inType = (String)inputNormalBox.getSelectionModel().getSelectedItem();
            normalType = 0;
            if (inType.equals (INPUT_NORMAL1)) {
              normalType = 1;
            }
            setNormalTypeImplications();
        }
      });
      fxUtils.addLabelHeading(inputNormalLabel, 1, 5);
      inputPane.add(inputNormalBox, 1, 6, 1, 1);
      GridPane.setHgrow(inputNormalBox, Priority.SOMETIMES);
    }
    
    //
    // Third Column
    //
    
    // Create Radio Buttons for File Merge
    inputMergeLabel = new Label ("Merge into Existing Data");
    fxUtils.addLabelHeading(inputMergeLabel, 2, 1);

    inputMergeGroup = new ToggleGroup();
  
    inputMergeNoButton = new RadioButton ("No Merge");
    // inputMergeNoButton.setActionCommand ("NO");
    inputMergeNoButton.setSelected (true);
    merge = 0;
    inputMergeGroup.getToggles().add (inputMergeNoButton);
    inputMergeNoButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (inputMergeNoButton.isSelected()) {
          merge = 0;
          setMergeImplications();
        }
      }
    });
    inputPane.add(inputMergeNoButton, 2, 2, 1, 1);
    GridPane.setHgrow(inputMergeNoButton, Priority.SOMETIMES);
    
    inputMergeButton = new RadioButton ("Merge New Data with Old");
    // inputMergeButton.setActionCommand ("MERGE");
    inputMergeGroup.getToggles().add (inputMergeButton);
    inputMergeButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (inputMergeButton.isSelected()) {
          merge = 1;
          setMergeImplications();
        }
      }
    });
    inputPane.add(inputMergeButton, 2, 3, 1, 1);
    GridPane.setHgrow(inputMergeButton, Priority.SOMETIMES);
		
    inputMergeSameColumnsButton = new RadioButton ("Merge with Same Columns");
    // inputMergeSameColumnsButton.setActionCommand ("SAME");
    inputMergeGroup.getToggles().add (inputMergeSameColumnsButton);
    inputMergeSameColumnsButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (inputMergeSameColumnsButton.isSelected()) {
          merge = 2;
          setMergeImplications();
        }
      }
    });
    inputPane.add(inputMergeSameColumnsButton, 2, 4, 1, 1);
    GridPane.setHgrow(inputMergeSameColumnsButton, Priority.SOMETIMES);
    
    // Create Check Box for Tags Explosion
    explodeTagsLabel = new Label ("Tags Explosion");
    fxUtils.addLabelHeading(explodeTagsLabel, 2, 5);
    
    explodeTagsCkBox = new CheckBox ("One row for each tag?");
    explodeTagsCkBox.setSelected (false);
    explodeTagsCkBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        explodeTags = explodeTagsCkBox.isSelected();
      }
    });
    inputPane.add(explodeTagsCkBox, 2, 6, 1, 1);
    GridPane.setHgrow(explodeTagsCkBox, Priority.SOMETIMES);

    // Finish up the Input Pane
    setMergeImplications();
    setNormalTypeImplications();
    inputTab.setContent(inputPane);
    inputTab.setClosable(false);
    tabs.getTabs().add(inputTab);
  } // end method setTabs
  
  /**
   Select the tab for this panel. 
  */
  public void selectTab() {
    if (tabs != null) {
      tabs.getSelectionModel().select(inputTab);
    }
  }
  
  /**
     Play one recorded action in the Input module.
   */
  public void playScript (
      String  inActionAction,
      String  inActionModifier,
      String  inActionObject,
      String  inActionValue,
      int     inActionValueAsInt,
      boolean inActionValueValidInt) {
    
    if (inActionAction.equals (ScriptConstants.EPUB_IN_ACTION)) {
      epubFolder = new File (inActionValue);
    }
    else
    if (inActionAction.equals (ScriptConstants.EPUB_OUT_ACTION)) {
      epubFile = new File (inActionValue);
      createEpub();
    }
    else
    if (inActionAction.equals (ScriptConstants.SET_ACTION)) {
      if (inActionObject.equals (ScriptConstants.DIR_DEPTH_OBJECT)) {
        if (inActionValueValidInt) {
          if (inActionValueAsInt > 0) {
            dirMaxDepth = inActionValueAsInt;
          } else {
            Logger.getShared().recordEvent (LogEvent.MEDIUM, 
              inActionValue + " is not a valid value for an Open Directory Depth",
              true);
          }
        } else {
          Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            inActionValue + " is not a valid integer for an Open Directory Depth Value",
            true);
        }
      }
      else
      if (inActionObject.equals (ScriptConstants.NORMAL_OBJECT)) {
        if (inActionValueValidInt) {
          if (inActionValueAsInt >= 0
              && inActionValueAsInt <= NORMALTYPE_MAX) {
            normalType = inActionValueAsInt;
            setNormalTypeImplications();
          } else {
            Logger.getShared().recordEvent (LogEvent.MEDIUM, 
              inActionValue + " is not a valid value for a Normalization Type Value",
              true);
          }
        } else {
          Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            inActionValue + " is not a valid integer for a Normalization Type Value",
            true);
        }
      }
      else
      if (inActionObject.equals (ScriptConstants.EXPLODE_TAGS_OBJECT)) {
        char xplTagsChar = inActionValue.toLowerCase().charAt(0);
        explodeTags = (xplTagsChar == 't' || xplTagsChar == 'y');
      } else {
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
          inActionObject + " is not a valid Scripting Object for an Open Set Action",
          true);
      }
    } 
    else
    if (inActionAction.equals (ScriptConstants.OPEN_ACTION)) {
      
      merge = 0;
      if (inActionObject.equals (ScriptConstants.MERGE_OBJECT)) {
        merge = 1;
        setMergeImplications();
      } else
      if (inActionObject.equals (ScriptConstants.MERGE_SAME_OBJECT)) {
        merge = 2;
        setMergeImplications();
      }

      inputModuleIndex = 0;
      inputModuleFound = false;
      while (inputModuleIndex < inputModules.size() && (! inputModuleFound)) {
        inputModule = inputModules.get(inputModuleIndex);
        inputModuleFound = inputModule.setInputTypeByModifier(inActionModifier);
        if (! inputModuleFound) {
          inputModuleIndex++;
        }
      }
          
      if (inActionModifier.equals (ScriptConstants.URL_MODIFIER)) {
        try {
          tabURL = new URL (Home.getShared().getPageURL(), inActionValue);
        } catch (MalformedURLException e) {
          tabURL = null;
        }
        if (tabURL == null) {
          Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            inActionValue + " is not a valid " + inActionModifier + " for an Open Action",
            true);
        }
        else {
          fileName = inActionValue;
          openURL();
          // openDataName.setText (fileNameToDisplay);
        } // end file existence selector
      } // end if URL modifier
      else
      if (inputModuleFound) {
        chosenFile = new File (inActionValue);
        if (chosenFile == null) {
          Logger.getShared().recordEvent (LogEvent.MEDIUM, 
            inActionValue + " is not a valid " + inActionModifier + " for an Open Action",
            true);
        }
        else {
          openFileOrDirectory();
          // openDataName.setText (fileNameToDisplay);
        } // end file existence selector
      } // end file or directory
      else {
        Logger.getShared().recordEvent (LogEvent.MEDIUM, 
          inActionModifier + " is not a valid Scripting Modifier for an Open Action",
          true);
      } // end Action Modifier selector
    } // end valid action
    else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM, 
        inActionAction + " is not a valid Scripting Action for the Open Module",
        true);
    } // end Action selector
  } // end playInputModule method
  
  /**
   Add another input module to the list. 
  
   @param anotherInputModule Another PSTextMerge input module to be made
                             available. 
  */
  private int addInputModule (
      TextMergeInputModule anotherInputModule,
      int insertAt) {
    int k = insertAt;
    inputModules.add(anotherInputModule);
    anotherInputModule.setInputType(0);
    for (int j = 1; j <= anotherInputModule.getInputTypeMax(); j++) {
      inputTypeBox.getItems().add(k, anotherInputModule.getInputTypeLabel(j));
      k++;
    }
    return k;
  }
  
  private void initInputModules() {
    for (int i = 0; i < inputModules.size(); i++) {
      inputModules.get(i).setInputType(0);
    }
  }
  
  /**
     Open the tab-delimited data file as an empty data set.
   */
  private void openEmpty () {
    fileNameToDisplay = "No Input File";
    tabName = "";
    dataDict = new DataDictionary();
    dataDict.setLog (log);
    dataRecList.initialize();
   
    initDataSets();
    textMergeController.setListAvailable(false);
  }
  
  private void chooseAndOpen() {
    if (Home.runningOnMac() 
        && inputModule != null 
        && inputModule instanceof TextMergeInputMacApps) {
      chooseAndOpenDirectory();
    }
    else
    if (inputModule instanceof TextMergeInputNotenik
        || inputModule instanceof TextMergeInputDirectory
        || inputModule instanceof TextMergeInputDirEntry) {
      chooseAndOpenDirectory();
    } else {
      chooseAndOpenFile();
    }
  }
  
  /**
     Open the input file.
   */
  private void chooseAndOpenDirectory() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Choose a Folder to Open");
    if (Home.runningOnMac()
        && inputModule != null
        && inputModule instanceof TextMergeInputMacApps) {
      File top = new File ("/");
      chooser.setInitialDirectory (top);
    }
    else
    if (textMergeScript.hasCurrentDirectory()) {
      File currDir = textMergeScript.getCurrentDirectory();
      if (currDir != null
          && currDir.exists()
          && currDir.isDirectory()) {
        chooser.setInitialDirectory (textMergeScript.getCurrentDirectory());
      }
    } 
    File result = chooser.showDialog (null);
    if (result != null) {
      chosenFile = result;
      openFileOrDirectory();
    }
  }
  
  /**
     Open the input file.
   */
  private void chooseAndOpenFile() {
    FileChooser chooser = new FileChooser();
    if (textMergeScript.hasCurrentDirectory()) {
      chooser.setInitialDirectory (textMergeScript.getCurrentDirectory());
    } 
    File result = chooser.showOpenDialog (null);
    if (result != null) {
      chosenFile = result;
      openFileOrDirectory();
    }
  }
  
  /**
   Open the passed file or directory as an input file. 
  
   @param inFile The file or directory to be opened. 
  */
  public void openFileOrDirectory (File inFile) {
    chosenFile = inFile;
    openFileOrDirectory();
  }
  
  /** 
     Decides whether to open the data source as a file or as a directory.
   */
  private void openFileOrDirectory() {
    
    textMergeScript.recordScriptAction (
        ScriptConstants.INPUT_MODULE, 
        ScriptConstants.SET_ACTION,
        ScriptConstants.NO_MODIFIER, 
        ScriptConstants.NORMAL_OBJECT, 
        String.valueOf (normalType));
    
    textMergeScript.recordScriptAction (
        ScriptConstants.INPUT_MODULE, 
        ScriptConstants.SET_ACTION,
        ScriptConstants.NO_MODIFIER, 
        ScriptConstants.EXPLODE_TAGS_OBJECT, 
        String.valueOf (explodeTags));
    
    log.recordEvent (LogEvent.NORMAL,
        "Rows before open: "
            + String.valueOf(dataRecList.totalSize()),
        false);
    if (merge == 0) {
      dataDict = new DataDictionary();
      dataDict.setLog (log);
    }
    
    FileName chosenFileName = new FileName (chosenFile);
    
    if (chosenFileName.getExt().trim().equals (inXML.getPreferredExtension())
        && inXML.getInputType() < 1 && inTunes.getInputType() < 1) {
      inXML.setInputType(2);
      inputModuleFound = true;
      inputModule = inXML;
    }
    else
    if (chosenFileName.getExt().trim().equals (inExcel.getPreferredExtension())
        && inExcel.getInputType() == 0) {
      inExcel.setInputType(1);
      inputModuleFound = true;
      inputModule = inExcel;
    }
    /*
    else
    if (chosenFileName.getExt().trim().equals (inYAML.getPreferredExtension())
        && inYAML.getInputType() == 0) {
      inYAML.setInputType(1);
      inputModuleFound = true;
      inputModule = inYAML;
    } */
    
    log.recordEvent (LogEvent.NORMAL,
        "Input Module Found? " + String.valueOf(inputModuleFound),
        false);
    
    if (! inputModuleFound) {
      inTDF.setInputType(1);
      inputModuleFound = true;
      inputModule = inTDF;
    }
    
    log.recordEvent (LogEvent.NORMAL,
        "Using Input Module " + inputModule.getClass().getName(), false);
    log.recordEvent (LogEvent.NORMAL,
        "With Input Type of " + String.valueOf(inputModule.getInputType()), false);
    
    if (inputModuleFound) {
      fileNameToDisplay = chosenFile.getName();
      tabName = chosenFile.getAbsolutePath();
      if (chosenFile.isDirectory()
          && (! (inputModule instanceof TextMergeInputNotenik))) {
        setCurrentDirectoryFromDir (chosenFile);
        TextMergeDirectoryReader dirReader 
            = new TextMergeDirectoryReader (chosenFile);
        dirReader.setInputModule(inputModule);
        dirReader.setMaxDepth(dirMaxDepth);
        textMergeScript.recordScriptAction (
            ScriptConstants.INPUT_MODULE, 
            ScriptConstants.SET_ACTION,
            ScriptConstants.NO_MODIFIER, 
            ScriptConstants.DIR_DEPTH_OBJECT, 
            String.valueOf (dirMaxDepth));
        dataSource = dirReader;
      } else {
        setCurrentDirectoryFromFile (chosenFile);
        textMergeScript.setNormalizerPath(textMergeScript.getCurrentDirectory().getPath());
        openDict();
        dataSource = inputModule.getDataSource(chosenFile);
      }
      openData();
      textMergeScript.recordScriptAction (
          ScriptConstants.INPUT_MODULE, 
          ScriptConstants.OPEN_ACTION, 
          inputModule.getInputTypeModifier(), 
          inputObject, 
          chosenFile.getAbsolutePath());
    }
    
    if (dataRecList != null) {
      log.recordEvent (LogEvent.NORMAL,
        "Rows loaded:      "
            + String.valueOf(dataRecList.getRecordsLoaded()),
        false);
    }
    log.recordEvent (LogEvent.NORMAL,
        "Rows after open:  "
            + String.valueOf(dataRecList.totalSize()),
        false);
  } // end openFileOrDirectory method
  
  /**
     Open dictionary file, if requested.
   */
  private void openDict () {
    if (usingDictionary) {
      tabFileName = new FileName (tabName);
      dictFile = 
        new TabDelimFile (textMergeScript.getCurrentDirectory(),
          tabFileName.replaceExt(DICTIONARY_EXT));
      try {
        dataDict.load (dictFile);
      } catch (IOException e) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Problem Reading Input Dictionary",
            false);
      } // end of catch
    } // end if using dictionary
  }
  
  /**
     Open the tab-delimited data file as a URL on the Web.
   */
  private void openURL () {
    textMergeScript.recordScriptAction (
        ScriptConstants.INPUT_MODULE, 
        ScriptConstants.SET_ACTION,
        ScriptConstants.NO_MODIFIER, 
        ScriptConstants.NORMAL_OBJECT, 
        String.valueOf (normalType));
    if (merge == 0) {
      dataDict = new DataDictionary();
      dataDict.setLog (log);
    }
    tabName = tabURL.toString();
    fileNameToDisplay = fileName;
    dataSource = new TabDelimFile (tabURL);
    openData();
    textMergeScript.recordScriptAction (
        ScriptConstants.INPUT_MODULE, 
        ScriptConstants.OPEN_ACTION, 
        ScriptConstants.URL_MODIFIER,
        inputObject, tabURL.toString());
  }
  
  /**
     Opens the input data source (whether a file or a directory).
   */
  private void openData () { 

    dataSource.setLog (log);
    DataSource original = dataSource;
    boolean openOK = false;
    if (normalType > 0) {
      if (normalType == 1) {
        try {
          BoeingDocsNormalizer docs = new BoeingDocsNormalizer (original);
          docs.setDataParent (textMergeScript.getNormalizerPath());
          dataSource = docs;
          log.recordEvent (LogEvent.NORMAL, 
            "BoeingDocsNormalizer successfully constructed",
            false);
        } catch (IOException e) {
          log.recordEvent (LogEvent.MAJOR, 
            "I/O Error in Data Normalization routine",
            false);
        }
      } // end if Boeing docs normalizer
      dataSource.setLog (log);
    } //end if noralization type specified
      
    try {
      if (merge == 1) {
        dataRecList.merge (dataSource);
      }
      else
      if (merge == 2) {
        dataRecList.mergeSame (dataSource);
      }
      else
      if (explodeTags) {
        dataRecList.loadAndExplode(dataDict, dataSource, log);
      }
      else {
        dataRecList.load(dataDict, dataSource, log);
      }
      dataDict.setLog (log);
      dataRecList.setSource(new FileSpec(chosenFile));

      initDataSets();
      /*
      if (openOutputDataButton != null) {
        openOutputDataButton.setEnabled (true);
        fileSave.setEnabled (true);
      }
      */
      log.recordEvent (LogEvent.NORMAL, 
          "Data Source named "
              + fileNameToDisplay
              + " was opened successfully",
          false);
      openOK = true;
    } catch (IOException e) {
      if (quietMode) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Data Source named "
                + fileNameToDisplay
                + " could not be opened successfully" 
                + "\n       Data Source = "
                + original.toString()
                + "\n       I/O Error = "
                + e.toString(),
          true);
      } else {
        log.recordEvent (LogEvent.MEDIUM, 
            "Data Source named "
                + fileNameToDisplay
                + " could not be opened successfully"
                + "\n       Data Source = "
                + original.toString()
                + "\n       I/O Error = "
                + e.toString(),
          true);
        if (! quietMode) {
          Alert alert = new Alert(AlertType.ERROR);
          alert.initOwner(ownerWindow);
          alert.setTitle("Data Source Error");
          alert.setHeaderText(null);
          alert.setContentText("Data Source named "
                  + fileNameToDisplay
                  + " could not be opened successfully");
          alert.showAndWait();
        }
      } // end catch block
      openOK = false;
      dataRecList.newListLoaded();
    } // end catch block
    
    textMergeController.setListAvailable(openOK);

  } // openData method
  
  private void chooseEpubFiles () {

    // Let the user select the folder containing the contents of the book
    DirectoryChooser folderChooser = new DirectoryChooser();
    folderChooser.setTitle("Open Folder containing EPub Contents");
    if (textMergeScript.hasCurrentDirectory()) {
      folderChooser.setInitialDirectory (textMergeScript.getCurrentDirectory());
    }
    epubFolder = folderChooser.showDialog(ownerWindow);
    if (epubFolder != null) {
      // Let the user specify the name and location of the output epub file
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialDirectory(epubFolder.getParentFile());
      String epubFileName;
      if (epubFolder.getName().equalsIgnoreCase("epub")) {
        epubFileName = epubFolder.getParentFile().getName() + ".epub";
      } else {
        epubFileName = epubFolder.getName() + ".epub";
      }
      fileChooser.setInitialFileName(epubFileName);
      fileChooser.setTitle("Specify the output EPub File");
      epubFile = fileChooser.showSaveDialog(ownerWindow);
      if (epubFile != null) {
        // Copy the folder contents into the output zip file
        createEpub();
      } // end if user specified an output file
    } // end if user specified an input folder
  } // end method chooseEpubFiles
  
  /**
     Create the EPub file.
   */
  private void createEpub() {
        
    try {
      FileOutputStream epubStream = new FileOutputStream(epubFile);
      ZipOutputStream epub = new ZipOutputStream(
          new BufferedOutputStream(epubStream));
      // The Mime Type must be the first entry
      addEpubEntry (epub, epubFolder, new File (epubFolder, MIME_TYPE),
          ZipOutputStream.DEFLATED);
      addEpubDirectory (epub, epubFolder, epubFolder);
      epub.close();
      textMergeScript.recordScriptAction (
          ScriptConstants.INPUT_MODULE, 
          ScriptConstants.EPUB_IN_ACTION, 
          ScriptConstants.NO_MODIFIER,
          inputObject, epubFolder.toString());
      textMergeScript.recordScriptAction (
          ScriptConstants.INPUT_MODULE, 
          ScriptConstants.EPUB_OUT_ACTION, 
          inTDF.getInputTypeModifier(1),
          inputObject, epubFile.toString());
      log.recordEvent (LogEvent.NORMAL,
        "Successfully created EPub file " + epubFile.toString(),
        false);
    } catch (IOException e) {
      log.recordEvent (LogEvent.MEDIUM,
        "Unable to create EPub file " + epubFile.toString()
        + " due to I/O Exception " + e.toString(),
        false);
      Trouble.getShared().report
        ("I/O error creating an EPub from " + epubFolder.toString(),
        "EPub Problem");
    }
  } // end method createEpub
  
  /**
   Add the contents of the specified folder, including the contents
   of sub-folders, to the specified zip output stream.

   @param zipOut     The zip output stream to receive the output.
   @param topFolder  The top folder being zipped.
   @param folder     The specific folder to be zipped.
   @throws java.io.IOException
   */
  private void addEpubDirectory(
      ZipOutputStream zipOut, 
      File topFolder, 
      File folder)
        throws java.io.IOException {
    
    String filesAndFolders[] = folder.list();
    for (int i = 0; i < filesAndFolders.length; i++) {
      String nextFileOrFolderName = filesAndFolders[i];
      File nextFileOrFolder = new File (folder, nextFileOrFolderName);
      if (nextFileOrFolderName.equalsIgnoreCase (MIME_TYPE)) {
        // skip this, since we should have already added it as the first entry
      }
      else
      if (nextFileOrFolderName.startsWith(".")) {
        // skip this, since we don't want hidden system files
      }
      else
      if (nextFileOrFolder.isDirectory()) {
        addEpubDirectory (zipOut, topFolder, nextFileOrFolder);
      }
      else
      if (nextFileOrFolder.isFile()) {
        addEpubEntry (zipOut, topFolder, nextFileOrFolder,
            ZipOutputStream.DEFLATED);
      }
    } // end for each file or folder in the directory
  } // end method addEpubDirectory

  /**
   Add another entry to the specified zip output stream.

   @param zipOut     The zip output stream to receive the output.
   @param topFolder  The top folder being zipped.
   @param file       The specific file to be added.
   @throws java.io.IOException
   */
  private void addEpubEntry(
      ZipOutputStream zipOut,
      File topFolder,
      File file,
      int method)
        throws java.io.IOException {
    FileInputStream inStream = new FileInputStream(file);
    String topFolderPath = topFolder.getPath();
    String filePath = file.getPath();
    String zipPath = filePath.substring(topFolderPath.length() + 1);
    ZipEntry entry = new ZipEntry (zipPath);
    entry.setMethod(method);
    zipOut.putNextEntry(entry);
    int bytesRead;
    byte[] buffer = new byte[4096];
    while((bytesRead = inStream.read(buffer)) != -1) {
      zipOut.write(buffer, 0, bytesRead);
    }
    inStream.close();
  }
  
  private void setCurrentDirectoryFromFile (File inFile) {
    textMergeScript.setCurrentDirectory(new File (inFile.getParent()));
  }
  
  private void setCurrentDirectoryFromDir (File inFile) {
    textMergeScript.setCurrentDirectory(inFile);
  }
  
  /**
     Sets other values related to the merge option.
   */
  private void setMergeImplications () {
    if ((merge > 0) && (! textMergeController.isListAvailable())) {
      merge = 0;
    }
    if (merge == 1) {
      mergeValue = "Yes";
      inputObject = ScriptConstants.MERGE_OBJECT;
    }
    else 
    if (merge == 2) {
      mergeValue = "Same";
      inputObject = ScriptConstants.MERGE_SAME_OBJECT;
    } else {
      mergeValue = "No";
      inputObject = ScriptConstants.NO_OBJECT;
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
     Sets appropriate input data normalization values.
   */
  private void setNormalTypeImplications() {
    if (normalType == 0) {
      normalTypeValue = "No Normalization";
    } 
    else 
    if (normalType == 1) {
      normalTypeValue = "Boeing Docs";
    }
  }
  
  private void initDataSets () {
    dataRecList.setComparator(new PSDefaultComparator());
    dataRecList.setInputFilter(null);
    /*
    dataTable = new DataTable (filteredDataSet);
    numberOfFields = recDef.getNumberOfFields();
    if (tabTableBuilt) {
      tabTable.setModel (dataTable);
      tabNameLabel.setText (fileNameToDisplay);
      setColumnWidths();
    }
    if (sortTabBuilt) {
      loadSortFields();
    }
    if (filterTabBuilt) {
      loadFilterFields();
    }
    */
  }
  
  public String getFileNameToDisplay() {
    return fileNameToDisplay;
  }
  
}
