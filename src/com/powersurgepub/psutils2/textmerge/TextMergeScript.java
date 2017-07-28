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
  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.list.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.script.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.net.*;

 	import javafx.collections.*;
 	import javafx.event.*;
 	import javafx.scene.control.*;
 	import javafx.scene.control.Alert.*;
  import javafx.scene.input.*;
 	import javafx.scene.layout.*;
  import javafx.scene.text.*;
 	import javafx.stage.*;

/**
  Module to record script actions.

  @author Herb Bowie
 */
public class TextMergeScript
    implements FileSpecOpener {

  /** Default file extension for script files. */
  public		static	final String  SCRIPT_EXT       = "tcz";

  public    static  final String  AUTOPLAY         = "autoplay";
  
  private     Window              ownerWindow;

  private     boolean             quietMode = true;
  private     boolean             tabSet = false;
  private     boolean             menuSet = false;

  private     ScriptExecutor      scriptExecutor = null;

  private     PSList              psList = null;

  private     File                templateLibrary = null;

  private     TextMergeInput      inputModule = null;
  private     TextMergeFilter     filterModule = null;
  private     TextMergeSort       sortModule = null;
  // private     TextMergeTemplate   templateModule = null;
  private     TextMergeOutput     outputModule = null;

  private     TabPane             tabs = null;
  private     Tab                 scriptTab = null;
  private     MenuBar             menus = null;

  private     File                currentDirectory = null;
  private			String							normalizerPath = "";

  // Fields used for the User Interface
  private     Border              raisedBevel;
  private     Border              etched;
  private     GridBagger          gb = new GridBagger();

  // Script panel objects
  private     GridPane            scriptPanel;
  private     Button              scriptRecordButton  = new Button();
  private     Button              scriptStopButton    = new Button();
  private     Button              scriptPlayButton    = new Button();
  private     Button              scriptReplayButton  = new Button();
  private     Button              scriptAutoPlayButton = new Button();
  private     Button              scriptEasyPlayButton = new Button();
  private     Label							  scriptPlaceHolder;
  private     StringBuilder       scriptTextBuilder = new StringBuilder();
  private     TextArea            scriptText = new TextArea("");

	private     boolean             scriptRecording = false;

	private     ScriptFile          outScript;
  private     File                outScriptFile;

	private     ScriptAction        outAction;

  private			Menu								scriptMenu = null;
  private			MenuItem						scriptRecord = null;
  private			MenuItem						scriptEndRecording = null;
  private			MenuItem						scriptPlay = null;
  private			MenuItem						scriptReplay = null;
  private     MenuItem            scriptClear = null;
  private     MenuItem            scriptAutoPlay = null;
  private     MenuItem            scriptEasyPlay = null;
  private     Menu                recentScriptsMenu = null;

  // Easy Play panel objects
  private     GridPane              easyPlayGrid = null;

	/*
	   Scripting stuff
	 */
  private     File                scriptDirectory = null;
	private     URL                 scriptURL;
	private     ScriptFile          inScript = null;
	private     File                inScriptFile = null;
	private     ScriptAction        inAction;
	private     String              inActionModule;
	private     String              inActionAction;
	private     String              inActionModifier;
	private     String              inActionObject;
	private     String              inActionValue;
  private			int									inActionValueAsInt;
  private			boolean							inActionValueValidInt;
  private			String							inputObject = "";
	private     boolean             scriptPlaying = false;
  private     RecentFiles         recentScripts = null;
  private     boolean             autoplayAllowed = true;
  private     String              autoPlay = "";
  private     String              easyPlay = "";
  private     File                easyPlayFolder = null;

  public TextMergeScript (Window ownerWindow, PSList psList) {
    this.ownerWindow = ownerWindow;
    this.psList = psList;
    setListOptions();
  }

  public void allowAutoplay (boolean autoplayAllowed) {
    this.autoplayAllowed = autoplayAllowed;
  }

  /**
   If the user has specified a script file to play at startup, then play it
   when requested.

   @return True if a script was automatically played, false otherwise.
  */
  public boolean checkAutoPlay() {
    boolean played = false;
    if (autoplayAllowed) {
      autoPlay = UserPrefs.getShared().getPref(AUTOPLAY, "");
      if (autoPlay.length() > 0) {
        File autoPlayFile = new File (autoPlay);
        if (autoPlayFile.exists() && autoPlayFile.canRead()) {
          inScriptFile = autoPlayFile;
          inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
          playScript();
          played = true;
        } // end if input script file is available
      } // end if autoplay was specified
    }
    return played;
  }

  public void setPSList (PSList psList) {
    this.psList = psList;
    setListOptions();
  }

  public void setInputModule (TextMergeInput inputModule) {
    this.inputModule = inputModule;
  }

  public void setFilterModule (TextMergeFilter filterModule) {
    this.filterModule = filterModule;
  }

  public void setSortModule (TextMergeSort sortModule) {
    this.sortModule = sortModule;
  }
  
  /*
  public void setTemplateModule (TextMergeTemplate templateModule) {
    this.templateModule = templateModule;
  }
  

  private File getTemplateLibrary() {
    if (templateModule == null) {
      return templateLibrary;
    } else {
      return templateModule.getTemplateLibrary();
    }
  }
  */
  public void setOutputModule (TextMergeOutput outputModule) {
    this.outputModule = outputModule;
  }
  
  private File getTemplateLibrary() {
    return null;
  }

  /**
   Set a class to be used for callbacks.

   @param scriptExecutor The class to be used for callbacks.
  */
  public void setScriptExecutor(ScriptExecutor scriptExecutor) {
    this.scriptExecutor = scriptExecutor;
  }

  public void setCurrentDirectory (File currentDirectory) {
    this.currentDirectory = currentDirectory;
  }

  public void setCurrentDirectoryFromFile (File currentFile) {
    this.currentDirectory = currentFile.getParentFile();
  }

  public boolean hasCurrentDirectory() {
    return (currentDirectory != null);
  }

  public File getCurrentDirectory () {
    return currentDirectory;
  }

  public void setNormalizerPath (String normalizerPath) {
    this.normalizerPath = normalizerPath;
  }

  public String getNormalizerPath() {
    return normalizerPath;
  }

  public void setMenus(MenuBar menus, String menuText) {

    quietMode = false;
    menuSet = true;

    this.menus = menus;
    scriptMenu = new Menu(menuText);
    menus.getMenus().add (scriptMenu);

    // Equivalent Menu Item to Record a Script
    scriptRecord = new MenuItem ("Record...");
    KeyCombination kc
        = new KeyCharacterCombination("R", KeyCombination.SHORTCUT_DOWN);
    scriptRecord.setAccelerator(kc);
    scriptMenu.getItems().add (scriptRecord);
    scriptRecord.setOnAction(new EventHandler<ActionEvent>() 
      {
        @Override
        public void handle(ActionEvent evt) {
          selectTab();
          startScriptRecording();
        } // end ActionPerformed method
      } // end action listener
    );

    // Equivalent Menu Item to Stop Recording of a Script
    scriptEndRecording = new MenuItem ("End Recording");
    KeyCombination serkc
        = new KeyCharacterCombination("E", KeyCombination.SHORTCUT_DOWN);
    scriptEndRecording.setAccelerator(serkc);

    scriptMenu.getItems().add (scriptEndRecording);
    scriptEndRecording.setOnAction(new EventHandler<ActionEvent>() 
      {
        @Override
        public void handle(ActionEvent evt) {
          selectTab();
          stopScriptRecordingUI();
        } // end ActionPerformed method
      } // end action listener
    );
    scriptEndRecording.setDisable (true);

    // Equivalent Menu Item to Play a Script
    scriptPlay = new MenuItem ("Play");
    KeyCombination playkc
        = new KeyCharacterCombination("P", KeyCombination.SHORTCUT_DOWN);
    scriptPlay.setAccelerator (playkc);
    scriptMenu.getItems().add (scriptPlay);
    scriptPlay.setOnAction(new EventHandler<ActionEvent>() 
      {
        @Override      public void handle(ActionEvent evt) {
          selectTab();
          startScriptPlaying();
        } // end ActionPerformed method
      } // end action listener
    );

    // Equivalent Menu Item to Replay a Script
    scriptReplay = new MenuItem ("Play Again");
    scriptReplay.setAccelerator (new KeyCharacterCombination("A"));
    scriptMenu.getItems().add (scriptReplay);
    scriptReplay.setOnAction(new EventHandler<ActionEvent>() 
      {
        @Override      public void handle(ActionEvent evt) {
          selectTab();
          startScriptPlayingAgain();
        } // end ActionPerformed method
      } // end action listener
    );

    // Menu Item to Clear Sort and Filter settings
    scriptClear = new MenuItem ("Clear");
    // scriptClear.setTooltip(new Tooltip("Clear sort and filter settings"));
    scriptMenu.getItems().add (scriptClear);
    scriptClear.setOnAction(new EventHandler<ActionEvent>() 
      {
        @Override
        public void handle(ActionEvent evt) {
          clearSortAndFilterSettings();
        } // end ActionPerformed method
      } // end action listener
    );

    // Equivalent Menu Item to AutoPlay a Script
    if (autoplayAllowed) {
      if (autoPlay.length() == 0) {
        scriptAutoPlay = new MenuItem ("Turn Autoplay On");
      } else {
        scriptAutoPlay = new MenuItem("Turn Autoplay Off");
      }
      scriptMenu.getItems().add (scriptAutoPlay);
      scriptAutoPlay.setOnAction(new EventHandler<ActionEvent>() 
        {
          @Override      public void handle(ActionEvent evt) {
            toggleAutoPlay();
          } // end ActionPerformed method
        } // end action listener
      );
    }

    // Equivalent Menu Item to EasyPlay a Script
    if (easyPlay.length() == 0) {
      scriptEasyPlay = new MenuItem ("Turn Easy Play On");
    } else {
      scriptEasyPlay = new MenuItem("Turn Easy Play Off");
    }
    scriptMenu.getItems().add (scriptEasyPlay);
    scriptEasyPlay.setOnAction(new EventHandler<ActionEvent>() 
      {
        @Override      public void handle(ActionEvent evt) {
          toggleEasyPlay();
        } // end ActionPerformed method
      } // end action listener
    );

    // Initialize Recent Scripts
    recentScriptsMenu = new Menu ("Play Recent Script");
    scriptMenu.getItems().add(recentScriptsMenu);
    recentScripts = new RecentFiles("recentscript");
    recentScripts.loadFromPrefs();
    recentScripts.setRecentFilesMax(10);
    recentScripts.registerMenu(recentScriptsMenu, this);

  } // end method setMenus

  public void setTabs(TabPane tabs) {
    quietMode = false;
    tabSet = true;

    this.tabs = tabs;

		scriptPanel = new GridPane();

		// Button to record a script
		scriptRecordButton.setText("Record");
		scriptRecordButton.setVisible(true);
		scriptRecordButton.setTooltip(new Tooltip("Start recording your actions"));
		scriptRecordButton.setOnAction(new EventHandler<ActionEvent>() 
		  {
		    @Override
        public void handle(ActionEvent evt) {
          startScriptRecording();
		    } // end ActionPerformed method
		  } // end action listener for script record button
		);

    // Button to stop recording
		scriptStopButton.setText("Stop");
		scriptStopButton.setVisible(true);
		scriptStopButton.setTooltip(new Tooltip("Stop recording"));
		scriptStopButton.setOnAction(new EventHandler<ActionEvent>() 
		  {
		    @Override      public void handle(ActionEvent evt) {
		      stopScriptRecordingUI();
		    } // end ActionPerformed method
		  } // end action listener for script stop button
		);

    // Set Off Initially
    scriptStopButton.setDisable (true);

    // Button to playback a script that has already been recorded
		scriptPlayButton.setText("Play");
		scriptPlayButton.setVisible(true);
		scriptPlayButton.setTooltip
        (new Tooltip("Play back a previously recorded script"));
		scriptPlayButton.setOnAction(new EventHandler<ActionEvent>() 
		  {
		    @Override
        public void handle(ActionEvent evt) {
          startScriptPlaying();
		    } // end ActionPerformed method
		  } // end action listener for script play button
		);

    // Button to replay the script just played/recorded
		scriptReplayButton.setText("Play Again");
		scriptReplayButton.setVisible(true);
		scriptReplayButton.setTooltip(new Tooltip("Replay the last script"));
		scriptReplayButton.setOnAction(new EventHandler<ActionEvent>() 
		  {
		    @Override
        public void handle(ActionEvent evt) {
          startScriptPlayingAgain();
		    } // end ActionPerformed method
		  } // end action listener for script play button
		);

    // Set Off Initially
    setScriptReplayControls();

    // Button to set an auto play script
    if (autoplayAllowed) {
      scriptAutoPlayButton.setVisible(true);
      scriptAutoPlayButton.setTooltip
        (new Tooltip("Select a script to automatically play at startup"));
      scriptAutoPlayButton.setOnAction(new EventHandler<ActionEvent>() 
        {
          @Override      public void handle(ActionEvent evt) {
            toggleAutoPlay();
          } // end ActionPerformed method
        } // end action listener for script play button
      );
    }

    // Button to set an easy play folder
		scriptEasyPlayButton.setVisible(true);
		scriptEasyPlayButton.setTooltip(new Tooltip
        ("Select a folder of scripts to be invoked via buttons"));
		scriptEasyPlayButton.setOnAction(new EventHandler<ActionEvent>() 
		  {
		    @Override      public void handle(ActionEvent evt) {
          toggleEasyPlay();
		    } // end ActionPerformed method
		  } // end action listener for script play button
		);

    // Create place holder for third column
    scriptPlaceHolder
        = new Label ("                            ");
    scriptPlaceHolder.setTextAlignment(TextAlignment.CENTER);

		scriptText.setWrapText (true);
		scriptText.setEditable (false);
		scriptText.setVisible (true);

		gb.startLayout (scriptPanel, 3, 3);
		gb.setDefaultRowWeight (0.0);

		gb.setAllInsets (1);

		gb.add (scriptRecordButton);
		gb.add (scriptPlayButton);
    if (autoplayAllowed) {
      gb.add (scriptAutoPlayButton);
    }

    gb.add (scriptStopButton);
		gb.add (scriptReplayButton);
    gb.add (scriptEasyPlayButton);
    // gb.add (scriptPlaceHolder);

		gb.nextRow ();

		gb.setWidth (3);
		gb.setRowWeight (1.0);
		gb.add (scriptText);

    scriptTab = new Tab("Script");
    scriptTab.setContent(scriptPanel);
    tabs.getTabs().add(scriptTab);

    addEasyPlayTabIfRequested();
  }

  private void setListOptions() {

    currentDirectory = null;
    scriptDirectory = null;
    templateLibrary = null;
    easyPlay = "";
    if (tabs != null && easyPlayGrid != null) {
      removeEasyPlayTab();
    }

    if (psList == null) {
      // System.out.println ("  psList is null");
    } else {

      // Set current directory
      FileSpec source = psList.getSource();
      if (source != null) {
        currentDirectory = source.getFolder();
      } else {
        // System.out.println ("  source is null");
      }

      // Set script directory
      String scriptDirectoryPath = null;
      if (source != null) {
        scriptDirectoryPath = source.getScriptsFolder();
      }
      if (scriptDirectoryPath != null
          && scriptDirectoryPath.length() > 0) {
        scriptDirectory = new File (scriptDirectoryPath);
        if (! scriptDirectory.exists()) {
          scriptDirectory = null;
        }
      }

      // Set template library
      String templateLibraryPath = null;
      if (source != null) {
        templateLibraryPath = source.getTemplatesFolder();
      }
      if (templateLibraryPath != null
          && templateLibraryPath.length() > 0) {
        templateLibrary = new File (templateLibraryPath);
        if (! templateLibrary.exists()) {
          templateLibrary = null;
        }
      }
      if (templateLibrary == null) {
        templateLibrary = new File (
            Home.getShared().getAppFolder().getPath(),
            "templates");
      }

      // Set easyplay options
      if (source != null) {
        easyPlay = source.getEasyPlay();
      }
      if (tabSet) {
        if (easyPlay.length() == 0) {
          scriptEasyPlayButton.setText("Turn Easy Play On");
        } else {
          scriptEasyPlayButton.setText("Turn Easy Play Off");
          addEasyPlayTab(easyPlay);
        } // end if easy play folder
      } // end if tabset
    }
  }

  private void addEasyPlayTabIfRequested() {
    if (tabSet) {
      if (easyPlay.length() > 0) {
        addEasyPlayTab(easyPlay);
      } // end if easy play folder
    } // end if tabset
  }

  /**
     Start recording of a script.
   */
  private void  startScriptRecording() {
    if (! scriptRecording) {
      FileChooser fileChooser = new FileChooser();
      if (scriptDirectory != null) {
        fileChooser.setInitialDirectory(scriptDirectory);
      }
      else
      if (currentDirectory != null) {
        fileChooser.setInitialDirectory (currentDirectory);
      }
      fileChooser.setTitle ("Create Output File to Store Script");
      outScriptFile
        = fileChooser.showSaveDialog (ownerWindow);
      if (outScriptFile != null) {
        FileName outScriptFileName
            = new FileName (outScriptFile, FileName.FILE_TYPE);
        if (outScriptFileName.getExt().trim().equals ("")) {
          outScriptFile
              = new File (outScriptFileName.getPath(),
                  outScriptFileName.replaceExt (SCRIPT_EXT));
        }
        outScript = new ScriptFile (outScriptFile, getTemplateLibrary().toString());
        // setCurrentDirectoryFromFile (outScriptFile);
        setScriptDirectoryFromFile (outScriptFile);
        normalizerPath = scriptDirectory.getPath();
        outScript.setLog (Logger.getShared());
        outScript.openForOutput();
        scriptRecording = true;
        appendScriptText ("Recording new script "
          + outScript.getFileName() + GlobalConstants.LINE_FEED_STRING);

        if (tabSet) {
          scriptRecordButton.setDisable (true);
          scriptStopButton.setDisable (false);
          scriptStopButton.requestFocus();
          scriptPlayButton.setDisable (true);
          scriptReplayButton.setDisable (true);
        }
        if (menuSet) {
          scriptRecord.setDisable (true);
          scriptEndRecording.setDisable (false);
          scriptPlay.setDisable (true);
          scriptReplay.setDisable (true);
        }
      } // end if file approved
    } // end if not already recording
  }

  /**
     Stop recording of a script.
   */
  private void stopScriptRecordingUI () {

    stopScriptRecording();

    if (tabSet) {
      scriptRecordButton.setDisable (false);
      scriptStopButton.setDisable (true);
      scriptPlayButton.setDisable (false);
    }
    if (menuSet) {
      scriptRecord.setDisable (false);
      scriptEndRecording.setDisable (true);
      scriptPlay.setDisable (false);
    }
    setScriptReplayControls();
  }

  /**
     Start the playback of a script.
   */
  private void startScriptPlaying() {
    FileChooser fileChooser = new FileChooser();
    if (scriptDirectory != null) {
      fileChooser.setInitialDirectory(scriptDirectory);
    }
    else
    if (currentDirectory != null) {
      fileChooser.setInitialDirectory (currentDirectory);
    }
    fileChooser.setTitle ("Select Pre-Recorded Script to Play Back");
    inScriptFile
      = fileChooser.showOpenDialog (ownerWindow);
    if (inScriptFile != null) {
      // setCurrentDirectoryFromFile (inScriptFile);
      setScriptDirectoryFromFile (inScriptFile);
      inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
      playScript();
    } // end if file approved
  }

  /**
     Start the replay of the last script.
   */
  private void startScriptPlayingAgain() {
    if (inScript != null) {
      playScript();
    } // end if script file defined
  }

  /**
     Toggle the script autoplay.
   */
  private void toggleAutoPlay() {
    if (autoPlay.length() > 0) {
      autoPlay = "";
      saveAutoPlay();
      if (menuSet) {
        scriptAutoPlay.setText("Turn Autoplay On");
      }
      if (tabSet) {
        scriptAutoPlayButton.setText("Turn Autoplay On");
      }
      // removeEasyPlayTab();
    } else {
      FileChooser fileChooser = new FileChooser();
      if (scriptDirectory != null) {
        fileChooser.setInitialDirectory (scriptDirectory);
      }
      else
      if (currentDirectory != null) {
        fileChooser.setInitialDirectory (currentDirectory);
      }
      fileChooser.setTitle ("Select Autoplay Script");
      File autoPlayFile
        = fileChooser.showOpenDialog (ownerWindow);
      if (autoPlayFile != null) {
          setScriptDirectoryFromFile(autoPlayFile);
          autoPlay = autoPlayFile.getPath();
          saveAutoPlay();
          if (menuSet) {
            scriptAutoPlay.setText("Turn Autoplay Off");
          }
          if (tabSet) {
            scriptAutoPlayButton.setText("Turn Autoplay Off");
          }

      } // end if file approved
    } // End if turning autoplay on
  }

  private void saveAutoPlay() {
    UserPrefs.getShared().setPref(AUTOPLAY, autoPlay);
  }

  /**
     Toggle the Easy play folder.
   */
  private void toggleEasyPlay() {
    if (easyPlay.length() > 0) {
      easyPlay = "";
      saveEasyPlay();
      if (menuSet) {
        scriptEasyPlay.setText("Turn Easy Play On");
      }
      if (tabSet) {
        scriptEasyPlayButton.setText("Turn Easy Play On");
      }
      removeEasyPlayTab();
    } else {
      DirectoryChooser folderChooser = new DirectoryChooser();
      if (scriptDirectory != null) {
        folderChooser.setInitialDirectory(scriptDirectory);
      }
      else
      if (currentDirectory != null) {
        folderChooser.setInitialDirectory (currentDirectory);
      }
      folderChooser.setTitle ("Select Easy Play Folder");
      easyPlayFolder
        = folderChooser.showDialog (ownerWindow);
      if (easyPlayFolder != null) {
        setScriptDirectoryFromDir(easyPlayFolder);
        easyPlay = easyPlayFolder.getPath();
        saveEasyPlay();
        if (menuSet) {
          scriptEasyPlay.setText("Turn Easy Play Off");
        }
        if (tabSet) {
          scriptEasyPlayButton.setText("Turn Easy Play Off");
        }
        addEasyPlayTab(easyPlay);
        selectEasyTab();
      } // end if file approved
    } // End if turning easy play on
  }

  private void saveEasyPlay() {
    if (psList != null) {
      FileSpec fileSpec = psList.getSource();
      if (fileSpec != null) {
        fileSpec.setEasyPlay(easyPlay);
      }
    }
  }

	/**
	   Add the Easy Play tab to the interface.
	 */
	private void addEasyPlayTab (String easyPlay) {

    easyPlayGrid = new GridPane();

    gb.startLayout (easyPlayGrid, 3, 6);
		gb.setDefaultRowWeight (0.0);
		gb.setAllInsets (1);

    easyPlayFolder = new File (easyPlay);
    String[] scripts = easyPlayFolder.list();
    for (int i = 0; i < scripts.length; i++) {
      File scriptFile = new File (easyPlayFolder, scripts[i]);
      FileName scriptName = new FileName(scriptFile);
      if (scriptName.getExt().equalsIgnoreCase(SCRIPT_EXT)) {
        Button easyPlayButton = new Button(scriptName.getBase());
        easyPlayButton.setTooltip(new Tooltip
            ("Play " + scriptName.getBase() + "." + SCRIPT_EXT
            + " from " + easyPlay));
        easyPlayButton.setMinSize(200, 28);
        easyPlayButton.setVisible(true);
        easyPlayButton.setOnAction(new EventHandler<ActionEvent>()
        {
          @Override      
          public void handle(ActionEvent evt) {
            inScriptFile = new File
                (easyPlayFolder, scriptName.getBase() + "." + SCRIPT_EXT);
            inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
            playScript();
          }
        });
        gb.add(easyPlayButton);
      } // end if directory entry ends with script extension
    } // end for each directory entry
    
    Tab easyPlayTab = new Tab("Easy");
    easyPlayTab.setContent(easyPlayGrid);
    tabs.getTabs().add(0, easyPlayTab);

	} // end method addEasyPlayTab

  /**
   Set the script replay controls to either be disabled if no script is
   available, or to be enabled if a script is available.
  */
  private void setScriptReplayControls() {
    if (inScript == null
        || inScriptFile == null) {
      if (scriptReplayButton != null) {
        scriptReplayButton.setDisable (true);
        scriptReplayButton.setTooltip(new Tooltip("Replay the last script"));
      }
      if (scriptReplay != null) {
        scriptReplay.setDisable (true);
      }

    } else {
      if (scriptReplayButton != null) {
        scriptReplayButton.setDisable (false);
        scriptReplayButton.setTooltip
            (new Tooltip("Replay script " + inScriptFile.getPath()));
      }
      if (scriptReplay != null) {
        scriptReplay.setDisable (false);
      }
    }
  }

	public void recordScriptAction (String module, String action, String modifier,
	    String object, String value) {
	  if (scriptRecording) {
  	  outAction = new ScriptAction (module, action, modifier, object, value);
  	  outScript.nextRecordOut (outAction);
  	  appendScriptText (outAction.toString() + GlobalConstants.LINE_FEED_STRING);
	  }
	} // end recordScriptAction method

  /**
   Select the easy play tab, or the first tab, if the easy play tab is not
   active.
   */
  public void selectEasyTab() {
    if (tabs != null) {
      tabs.getSelectionModel().select(0);
    }
  }

  /**
   Select the tab for this panel.
  */
  public void selectTab() {
    if (tabs != null) {
      tabs.getSelectionModel().select(scriptTab);
    }
  }

  private void setScriptDirectoryFromFile (File inFile) {
    scriptDirectory = new File(inFile.getParent());
    saveScriptDirectory();
  }

  private void setScriptDirectoryFromDir (File inFile) {
    scriptDirectory = inFile;
    saveScriptDirectory();
  }

  private void saveScriptDirectory() {

    if (psList != null) {
      FileSpec source = psList.getSource();
      if (source != null) {
        source.setScriptsFolder(scriptDirectory);
      } else {
        // System.out.println ("TextMergeScript.saveScriptDirectory source is null");
      }
    } else {
      // System.out.println ("TextMergeScript.saveScriptDirectory psList is null");
    }
  }

  /**
   If we are currently recording a script, then let's close it.

  */
	public void stopScriptRecording() {
    if (scriptRecording) {
      outScript.close();
      scriptRecording = false;
      appendScriptText ("Recording stopped" + GlobalConstants.LINE_FEED_STRING);
      inScript = outScript;
      inScriptFile = outScriptFile;
      setScriptReplayControls();
    } // end if script recording
  } // end stopScriptRecording method

  /**
   Save user preferences before shutting down.
  */
  public void savePrefs () {
    if (recentScripts != null) {
      recentScripts.savePrefs();
    }
  }

  public void clearSortAndFilterSettings() {

    if (sortModule != null) {
      sortModule.playSortModule(
          ScriptConstants.CLEAR_ACTION,
          ScriptConstants.NO_MODIFIER,
          ScriptConstants.NO_OBJECT);
      sortModule.playSortModule(
          ScriptConstants.SET_ACTION,
          ScriptConstants.NO_MODIFIER,
          ScriptConstants.PARAMS_OBJECT);
    }

    if (filterModule != null) {
      filterModule.playScript(
          ScriptConstants.CLEAR_ACTION,
          ScriptConstants.NO_MODIFIER,
          ScriptConstants.NO_OBJECT,
          ScriptConstants.NO_VALUE);
      filterModule.playScript(
          ScriptConstants.SET_ACTION,
          ScriptConstants.NO_MODIFIER,
          ScriptConstants.PARAMS_OBJECT,
          ScriptConstants.NO_VALUE);
    }

  }

  /**
    Standard way to respond to a request to open a file.

    @param file File to be opened by this application.
   */
  public void handleOpenFile (FileSpec fileSpec) {
    playScript (fileSpec.getFile());
  }

  public void playScript (File sFile) {
    inScriptFile = sFile;
    // setCurrentDirectoryFromFile (inScriptFile);
    setScriptDirectoryFromFile (inScriptFile);
    inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
    playScript();
  }

  /**
     Plays back a script file that has already been recorded.
   */
  private void playScript() {
    Logger.getShared().recordEvent (LogEvent.NORMAL,
        "Playing script " + inScript.toString(),
        false);
    if (hasCurrentDirectory()) {
      normalizerPath = currentDirectory.getPath();
    }
    inScript.setLog (Logger.getShared());
    try {
      inScript.openForInput();
    } catch (IOException e) {
      if (quietMode) {
        Logger.getShared().recordEvent (LogEvent.MEDIUM,
          "MSG003 " + inScript.toString() + " could not be opened as a valid Script File",
          true);
      } else {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(ownerWindow);
        alert.setTitle("Script File Error");
        alert.setHeaderText(null);
        alert.setContentText("Script File could not be opened successfully");
        alert.showAndWait();
      }
    }
    scriptPlaying = true;
    appendScriptText ("Playing script "
      + inScript.getFileName() + GlobalConstants.LINE_FEED_STRING);
    if (! quietMode && recentScripts != null) {
      recentScripts.addRecentFile (inScriptFile);
    }
    // Fix this later!!!
    filterModule.initItemFilter();
    while (! inScript.isAtEnd()) {
      try {
        inAction = inScript.nextRecordIn();
      } catch (IOException e) {
        inAction = null;
      }
      if (inAction != null) {
        appendScriptText ("Playing action " +
          inAction.toString() +
          GlobalConstants.LINE_FEED_STRING);
        inActionModule = inAction.getModule();
        inActionAction = inAction.getAction();
        inActionModifier = inAction.getModifier();
        inActionObject = inAction.getObject();
        inActionValue = inAction.getValue();
        try {
          inActionValueAsInt = Integer.parseInt (inActionValue);
          inActionValueValidInt = true;
        } catch (NumberFormatException e) {
          inActionValueAsInt = 0;
          inActionValueValidInt = false;
        }
        if (inActionModule.length() == 0) {
          // Skip blank lines
        }
        else
        if (inActionModule.startsWith("<!--")) {
          Logger.getShared().recordEvent(LogEvent.NORMAL, inActionModule, false);
        }
        else
        if (inActionModule.equals (ScriptConstants.INPUT_MODULE)) {
          playInputModule();
        }
        else
        if (inActionModule.equals (ScriptConstants.SORT_MODULE)) {
          playSortModule();
        }
        else
        if (inActionModule.equals (ScriptConstants.COMBINE_MODULE)) {
          playCombineModule();
        }
        else
        if (inActionModule.equals (ScriptConstants.FILTER_MODULE)) {
          playFilterModule();
        }
        else
        if (inActionModule.equals (ScriptConstants.OUTPUT_MODULE)) {
          playOutputModule();
        }
        else
        if (inActionModule.equals (ScriptConstants.TEMPLATE_MODULE)) {
          playTemplateModule();
        }
        else
        if (inActionModule.equals (ScriptConstants.CALLBACK_MODULE)) {
          playCallbackModule();
        }
        else {
          Logger.getShared().recordEvent (LogEvent.MEDIUM,
            inActionModule + " is not a valid Scripting Module",
            true);
        } // end else unrecognized module
      } // end if inAction not null
    } // end while more script commands
    inScript.close();
    scriptPlaying = false;
    appendScriptText ("Playback stopped" + GlobalConstants.LINE_FEED_STRING);
    resetOptions();
    setScriptReplayControls();
    selectTab();
  } // end method playScript
  
  private void appendScriptText(String str) {
    scriptTextBuilder.append(str);
    scriptText.setText(scriptTextBuilder.toString());
  }

  private void removeEasyPlayTab() {
    tabs.getTabs().remove(easyPlayGrid);
  }

  /**
     Play one recorded action in the Input module.
   */
  private void playInputModule () {
   if (inputModule == null) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM,
          "Input module not available to play scripted input action", false);
    } else {
      inputModule.playScript(
          inActionAction,
          inActionModifier,
          inActionObject,
          inActionValue,
          inActionValueAsInt,
          inActionValueValidInt);
    }
  } // end playInputModule method

  /**
     Play one recorded action in the Sort module.
   */
  private void playSortModule () {
    
    if (sortModule == null) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM,
          "Sort module not available to play scripted sort action", false);
    } else {
      sortModule.playSortModule(
          inActionAction,
          inActionModifier,
          inActionObject);
    }
    
  } // end playSortModule method

  /**
     Play one recorded action in the Combine module.
   */
  private void playCombineModule () {
    
    if (sortModule == null
        || (! sortModule.isCombineAllowed())) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM,
          "Sort module not available to play scripted combine action", false);
    } else {
      sortModule.playCombineModule(
          inActionAction,
          inActionModifier,
          inActionObject,
          inActionValue,
          inActionValueAsInt,
          inActionValueValidInt);
    }
    
  } // end playCombineModule method

  /**
     Play one recorded action in the Filter module.
   */
  private void playFilterModule () {
    
    if (filterModule == null) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM,
          "Filter module not available to play scripted filter action", false);
    } else {
      filterModule.playScript(
          inActionAction,
          inActionModifier,
          inActionObject,
          inActionValue);
    }
  }

  /**
     Play one recorded action in the Output module.
   */
  private void playOutputModule () {
    
    if (outputModule == null) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM,
          "Output module not available to play scripted filter action", false);
    } else {
      outputModule.playScript(
          inActionAction,
          inActionModifier,
          inActionObject,
          inActionValue);
    }
    
    /*
    if (inActionAction.equals (ScriptConstants.SET_ACTION)
      && inActionObject.equals (ScriptConstants.USING_DICTIONARY_OBJECT)) {
      usingDictionary = Boolean.valueOf(inActionValue).booleanValue();
      setDictionaryImplications();
    }
    else
    if (inActionAction.equals (ScriptConstants.OPEN_ACTION)) {
      chosenOutputFile = new File (inActionValue);
      if (chosenOutputFile == null) {
        Logger.getShared().recordEvent (LogEvent.MEDIUM,
          inActionValue + " is not a valid file name for an Output Open Action",
          true);
      }
      else {
        createOutput();
      } // end file existence selector
    } // end valid action
    else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        inActionAction + " is not a valid Scripting Action for the Output Module",
        true);
    } // end Action selector
   */
  } // end playOutputModule method

  /**
     Play one recorded action in the Template module.
   */
  private void playTemplateModule () {
    /*
    if (templateModule == null) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM,
          "Template module not available to play scripted template action", false);
    } else {
      templateModule.playTemplateModule(
          inActionAction,
          inActionModifier,
          inActionObject,
          inActionValue);
    }
    */
  } // end playTemplateModule method

  private void playCallbackModule() {

    Logger.getShared().recordEvent(LogEvent.NORMAL,
        "Playing callback for " + inActionAction + " action", false);
    if (scriptExecutor == null) {
      Logger.getShared().recordEvent(LogEvent.MEDIUM,
          "Script callback executor not available", false);
    }
    if (scriptExecutor != null) {
      Logger.getShared().recordEvent(LogEvent.NORMAL,
          "Callback executor is " + scriptExecutor.getClass().getName(), false);
      scriptExecutor.scriptCallback(inActionAction);
    }

  }

  /**
    Reset all the input options that might have been modified by a script
    that was played.
   */
  private void resetOptions() {

    // initInputModules();

    // usingDictionary = false;
    // setDictionaryImplications();

    // merge = 0;
    // setMergeImplications();

    // dirMaxDepth = 0;

    // normalType = 0;
    // setNormalTypeImplications();

    if (! quietMode) {
      // inputTypeBox.setSelectedIndex (0);
      // inputDictionaryCkBox.setSelected (false);
      // inputMergeNoButton.setSelected (true);
      // inputDirMaxDepthValue.setText (String.valueOf(dirMaxDepth));
      // if (normalization) {
      //   inputNormalBox.setSelectedIndex (0);
      // }
    }
  }

}
