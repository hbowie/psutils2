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
  import com.powersurgepub.psutils2.template.*;
	import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.util.*;

 	import javafx.event.*;
 	import javafx.scene.control.*;
 	import javafx.scene.control.Alert.*;
	import javafx.scene.input.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;

/**
 The template module used as part of PSTextMerge.

 @author Herb Bowie
 */
public class TextMergeTemplate {

  private     Window              ownerWindow = null;
  private     DataRecList         list = null;
  private     TextMergeController textMergeController = null;
  private     TextMergeScript     textMergeScript = null;
  
  private     TabPane         tabs = null;
  private     MenuBar            menus = null;
  private     boolean             tabSet = false;
  private     boolean             menuSet = false;
  
  private     FXUtils             fxUtils;
  private     Tab templateTab;
  private     GridPane templatePane;
  private     Button setWebRootButton;
  private     Button openTemplateButton;
  private     Button generateOutputButton;
  private     Button setTemplateLibraryButton;
  private     Button openTemplateFromLibraryButton;
  private     Label setTemplateLibraryLabel;
  private     Label openTemplateLabel;
  private     Label generateOutputLabel;
  private     Label templateLibraryName;
  private     Label openTemplateName;
  private     Label generateOutputName;
	private			StringBuilder templateText;
  private     TextArea templateTextArea;


  // Template menu items
  private			Menu								templateMenu;
  private     MenuItem            setWebRoot;
  private			MenuItem						templateOpen;
  private     MenuItem            templateOpenFromLibrary;
  private			MenuItem						templateGenerate;

  // Fields used for Template processing
  private     Template            template;
  private     File                templateFile;
  private     File                lastTemplateFile = null;

  private     boolean             templateCreated = false;
  private     boolean             templateFileReady = false;
  private     boolean             templateFileOK = false;
  private     boolean             generateOutputOK = false;

  private     String              templateFileName = "                         ";
  private     String              outputFileName   = "                         ";

  private static final String     TEMPLATE_LIBRARY_KEY = "templatelib";
  private     File                templateLibraryUserPref = null;
  private     File                templateLibraryAppFolder = null;
  private     File                templateLibraryFileSpec = null;
  private     File                templateLibraryButton = null;

  private     File                webRootFile = null;

  private     File                templateFolder = null;



  public TextMergeTemplate (
      Window ownerWindow,
      DataRecList list, 
      TextMergeController textMergeController, 
      TextMergeScript textMergeScript) {

    this.ownerWindow = ownerWindow;
    this.list = list;
    this.textMergeController = textMergeController;
    this.textMergeScript = textMergeScript;

    // Initialize template library based on app preferences
    // These may be overridden later by list preferences
    templateLibraryUserPref = new File (UserPrefs.getShared().getPref (TEMPLATE_LIBRARY_KEY));
    templateLibraryAppFolder = new File
          (Home.getShared().getAppFolder().getPath(),  "templates");
    templateFolder = Home.getShared().getUserHome();

    setListOptions();
  }

  public void setList (DataRecList list) {
    this.list = list;
    setListOptions();
  }

  private void setListOptions() {

    templateLibraryFileSpec = null;

    if (list != null) {
      FileSpec source = list.getSource();

      // Set template library
      String templateLibraryPath = null;
      if (source != null) {
        templateLibraryPath = source.getTemplatesFolder();
      }
      if (templateLibraryPath != null
          && templateLibraryPath.length() > 0) {
        templateLibraryFileSpec = new File (templateLibraryPath);
        if (! validLibrary (templateLibraryFileSpec)) {
          templateLibraryFileSpec = null;
        }
      }
    }
  }

  public void setTemplateLibrary (File templateLibrary) {
    templateLibraryFileSpec = templateLibrary;
  }

  /**
   Do we have a valid template library?

   @return True if current template library variable points to a valid folder.
  */
  public boolean validTemplateLibrary() {
    return (validLibrary (getTemplateLibrary()));
  }

  /**
   Return the first valid template library available, checking in this order
   of precedence:
   <ol>
     <li>the template library previously identified for the current source file;
     <li>the template library previously stored as a preference for this user;
     <li>the template library supplied with the application package.
   </ol>
  @return A valid template library if one exists, otherwise null.
  */
  public File getTemplateLibrary() {
    if (validLibrary (templateLibraryFileSpec)) {
      return templateLibraryFileSpec;
    }
    else
    if (validLibrary (templateLibraryUserPref)) {
      return templateLibraryUserPref;
    } else {
      return templateLibraryAppFolder;
    }
  }

  private boolean validLibrary (File library) {
    return (library != null
        && library.exists()
        && library.isDirectory()
        && library.canRead());
  }

  private boolean fileAvailable() {
    return list != null;
  }

  public void setMenus (MenuBar menus) {

    menuSet = true;

    this.menus = menus;
    templateMenu = new Menu("Template");

    // Equivalent Menu Item for Set Web Root
    setWebRoot = new MenuItem ("Set Web Root...");
    templateMenu.getItems().add (setWebRoot);
    setWebRoot.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        setWebRoot();
      }
    });

    // Equivalent Menu Item for Template Open
    templateOpen = new MenuItem ("Open...");
    KeyCombination kc
        = new KeyCharacterCombination("T", KeyCombination.SHORTCUT_DOWN);
    templateOpen.setAccelerator(kc);

    templateMenu.getItems().add (templateOpen);
    templateOpen.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
          // tabs.setSelectedComponent (templatePane);
          openTemplateFile();
        } // end ActionPerformed method
      } // end action listener
    );

    // Equivalent Menu Item for Template Open
    templateOpenFromLibrary = new MenuItem ("Open from Library...");
    templateMenu.getItems().add (templateOpenFromLibrary);
    templateOpenFromLibrary.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
          // tabs.setSelectedComponent (templatePane);
          openTemplateFromLibrary();
        } // end ActionPerformed method
      } // end action listener
    );

    // Equivalent Menu Item for Template Generate
    templateGenerate = new MenuItem ("Generate");
    KeyCombination tgkc
        = new KeyCharacterCombination("G", KeyCombination.SHORTCUT_DOWN);
    templateGenerate.setAccelerator(tgkc);
    templateMenu.getItems().add (templateGenerate);
    templateGenerate.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
          // tabs.setSelectedComponent (templatePane);
          generateTemplate();
        } // end ActionPerformed method
      } // end action listener
    );

    templateGenerate.setDisable (true);

    menus.getMenus().add(templateMenu);
  }

  public void setTabs(TabPane tabs) {

    tabSet = true;
    this.tabs = tabs;
    buildUI();
    
    if (validTemplateLibrary()) {
      Tooltip setTemplateLibraryButtonTip = new Tooltip
        (getTemplateLibrary().toString());
      Tooltip.install(setTemplateLibraryButton, setTemplateLibraryButtonTip);
    }

    generateOutputButton.setDisable (true);

    if (validTemplateLibrary()) {
      templateLibraryName.setText
        (getTemplateLibrary().toString());
    }

    tabs.getTabs().add(templateTab);

  }

  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		templateTab = new Tab("Template");

		templatePane = new GridPane();
		fxUtils.applyStyle(templatePane);

		setWebRootButton = new Button("Set Web Root");
		Tooltip setWebRootButtonTip = new Tooltip("Not yet set");
    Tooltip.install(setWebRootButton, setWebRootButtonTip);
    setWebRootButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        setWebRoot();
		  } // end handle method
		}); // end event handler
		templatePane.add(setWebRootButton, 0, rowCount, 1, 1);
		setWebRootButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(setWebRootButton, Priority.SOMETIMES);

		openTemplateButton = new Button("Open Template");
		Tooltip openTemplateButtonTip = new Tooltip("Specify the Template File to be used for the Merge");
    Tooltip.install(openTemplateButton, openTemplateButtonTip);
    openTemplateButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        openTemplateFile();
		  } // end handle method
		}); // end event handler
		templatePane.add(openTemplateButton, 1, rowCount, 1, 1);
		openTemplateButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openTemplateButton, Priority.SOMETIMES);

		generateOutputButton = new Button("Generate Output");
		Tooltip generateOutputButtonTip = new Tooltip
        ("Merge the data file with the template and generate the output file(s) specified by the Template");
    Tooltip.install(generateOutputButton, generateOutputButtonTip);
    generateOutputButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        generateTemplate();
		  } // end handle method
		}); // end event handler
		templatePane.add(generateOutputButton, 2, rowCount, 1, 1);
		generateOutputButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(generateOutputButton, Priority.SOMETIMES);

		rowCount++;

		setTemplateLibraryButton = new Button("Set Template Library");
    setTemplateLibraryButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        setTemplateLibrary();
		  } // end handle method
		}); // end event handler
		templatePane.add(setTemplateLibraryButton, 0, rowCount, 1, 1);
		setTemplateLibraryButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(setTemplateLibraryButton, Priority.SOMETIMES);

		openTemplateFromLibraryButton = new Button("Open from Library");
		Tooltip openTemplateFromLibraryButtonTip = new Tooltip("Specify the Template File to be used for the Merge");
    Tooltip.install(openTemplateFromLibraryButton, openTemplateFromLibraryButtonTip);
    openTemplateFromLibraryButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        openTemplateFromLibrary();
		  } // end handle method
		}); // end event handler
		templatePane.add(openTemplateFromLibraryButton, 1, rowCount, 1, 1);
		openTemplateFromLibraryButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openTemplateFromLibraryButton, Priority.SOMETIMES);

		rowCount++;

		setTemplateLibraryLabel = new Label("Template Library");
		fxUtils.applyHeadingStyle(setTemplateLibraryLabel);
		templatePane.add(setTemplateLibraryLabel, 0, rowCount, 1, 1);
		setTemplateLibraryLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(setTemplateLibraryLabel, Priority.SOMETIMES);

		openTemplateLabel = new Label("Input Template File");
		fxUtils.applyHeadingStyle(openTemplateLabel);
		templatePane.add(openTemplateLabel, 1, rowCount, 1, 1);
		openTemplateLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openTemplateLabel, Priority.SOMETIMES);

		generateOutputLabel = new Label("Merged Output File(s)");
		fxUtils.applyHeadingStyle(generateOutputLabel);
		templatePane.add(generateOutputLabel, 2, rowCount, 1, 1);
		generateOutputLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(generateOutputLabel, Priority.SOMETIMES);

		rowCount++;

		templateLibraryName = new Label();
		templatePane.add(templateLibraryName, 0, rowCount, 1, 1);
		templateLibraryName.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(templateLibraryName, Priority.SOMETIMES);

		openTemplateName = new Label();
		templatePane.add(openTemplateName, 1, rowCount, 1, 1);
		openTemplateName.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openTemplateName, Priority.SOMETIMES);

		generateOutputName = new Label();
		templatePane.add(generateOutputName, 2, rowCount, 1, 1);
		generateOutputName.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(generateOutputName, Priority.SOMETIMES);

		rowCount++;

		templateText = new StringBuilder();
		templateTextArea = new TextArea();
		templatePane.add(templateTextArea, 0, rowCount, 3, 1);
		templateTextArea.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(templateTextArea, Priority.ALWAYS);
		templateTextArea.setMaxHeight(Double.MAX_VALUE);
		templateTextArea.setPrefRowCount(100);
		GridPane.setVgrow(templateTextArea, Priority.ALWAYS);
		templateTextArea.setPrefRowCount(100);
		templateTextArea.setWrapText(true);

		rowCount++;

		templateTab.setContent(templatePane);
		templateTab.setClosable(false);
  } // end method buildUI

	private void appendText(String text) {
		templateText.append(text);
		templateTextArea.setText(templateText.toString());
	}


  /**
   Select the tab for this panel.
  */
  public void selectTab() {
    if (tabs != null) {
      tabs.getSelectionModel().select(templateTab);
    }
  }

  /**
     Play one recorded action in the Template module.
   */
  public void playTemplateModule (
      String inActionAction,
      String inActionModifier,
      String inActionObject,
      String inActionValue) {

    if (inActionAction.equals (ScriptConstants.OPEN_ACTION)) {
      templateFile = new File (inActionValue);
      if (templateFile == null) {
        Logger.getShared().recordEvent (LogEvent.MEDIUM,
          inActionValue + " is not a valid " + inActionModifier
          + " for a Template Open Action", true);
      }
      else {
        templateFolder = templateFile.getParentFile();
        templateOpen();
      } // end file existence selector
    }
    else
    if (inActionAction.equals (ScriptConstants.WEB_ROOT_ACTION)) {
      webRootFile = new File (inActionValue);
      if (webRootFile == null) {
        Logger.getShared().recordEvent (LogEvent.MEDIUM,
            inActionValue + " is not a valid local directory", true);
      } else {
        setWebRootWithFile();
      }
    }
    else
    if (inActionAction.equals (ScriptConstants.GENERATE_ACTION)) {
      checkTemplateRepeat();
      templateGenerate();
    } // end valid action
    else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        inActionAction + " is not a valid Scripting Action for the Template Module",
        true);
    } // end Action selector
  } // end playTemplateModule method

  /**
     Open a Template File
   */
  private void openTemplateFile () {
    templateFileOK = false;
    FileChooser fileChooser = new FileChooser();
    if (templateFolder != null
        && templateFolder.exists()
        && templateFolder.isDirectory()
        && templateFolder.canRead()) {
      fileChooser.setInitialDirectory(templateFolder);
    }
    else
    if (validTemplateLibrary()) {
      fileChooser.setInitialDirectory(getTemplateLibrary());
    }
    templateFile
      = fileChooser.showOpenDialog (ownerWindow);
    if (templateFile != null) {
      templateFolder = templateFile.getParentFile();
      templateOpen();
      if (! templateFileOK) {
        Trouble.getShared().report(
          "Error occurred while opening template file",
          "Template File Error");
      } // end if error opening template file
    } // end if user performed a valid file selection
  }

  /**
     Opens the template file for input.
   */
  private void templateOpen() {
    // setCurrentDirectoryFromFile (templateFile);
    if (! templateCreated) {
      createNewTemplate();
    }
    templateFileReady = true;
    templateFileName = templateFile.getName();
    if (openTemplateName != null) {
      openTemplateName.setText (templateFileName);
    }
    templateFileOK = template.openTemplate (templateFile);
    if (templateFileOK) {
      textMergeScript.recordScriptAction (
          ScriptConstants.TEMPLATE_MODULE,
          ScriptConstants.OPEN_ACTION,
          ScriptConstants.TEXT_MODIFIER,
          ScriptConstants.NO_OBJECT,
          templateFile.getAbsolutePath());
      // setTemplateDirectoryFromFile (templateFile);
    } else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        templateFileName + " could not be opened as a valid Template File",
        true);
    }

    // Set appropriate value for Generate Button
    if (generateOutputButton != null) {
      if (templateFileOK && fileAvailable() && templateCreated) {
        if (tabSet) {
          generateOutputButton.setDisable (false);
        }
        if (menuSet) {
          templateGenerate.setDisable (false);
        }
      } else {
        if (tabSet) {
          generateOutputButton.setDisable (true);
        }
        if (menuSet) {
          templateGenerate.setDisable (true);
        }
      }
    }
  } // end method templateOpen()

  /**
     Creates a new template object, to perform a new merge operation.
   */
  private void createNewTemplate () {
    template = new Template (Logger.getShared());
    templateCreated = true;
    outputFileName = "";
    if (generateOutputName != null) {
      generateOutputName.setText (outputFileName);
    }
  }

  /**
     Set the location of the web root.
   */
  private void setWebRoot () {
    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setTitle("Set the location of the Web Root");
    File selectedFolder
      = dirChooser.showDialog (ownerWindow);
    if (selectedFolder != null) {
      webRootFile = selectedFolder;
      Tooltip setWebRootButtonTip = new Tooltip
        (webRootFile.getAbsolutePath());
      Tooltip.install(setWebRootButton, setWebRootButtonTip);
      setWebRootWithFile();
    }
  }

  /**
     Sets the web root directory.
   */
  private void setWebRootWithFile () {

    if (webRootFile != null) {
      textMergeScript.recordScriptAction (
          ScriptConstants.TEMPLATE_MODULE,
          ScriptConstants.WEB_ROOT_ACTION,
          ScriptConstants.TEXT_MODIFIER,
          ScriptConstants.NO_OBJECT,
          webRootFile.getAbsolutePath());
        // setTemplateDirectoryFromFile (templateFile);
    } else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        webRootFile.getAbsolutePath()
          + " could not be opened as a valid Web Root Directory",
        true);
    }
  } // end method setWebRootWithFile()

  /**
     Set the location of the template library.
   */
  private void setTemplateLibrary () {
    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setTitle("Set the location of the Template Library");
    File selectedFile
      = dirChooser.showDialog (ownerWindow);
    if (selectedFile != null) {
      templateLibraryFileSpec = selectedFile;
      templateLibraryName.setText
        (templateLibraryFileSpec.getName());
      Tooltip setTemplateLibraryButtonTip = new Tooltip
        (templateLibraryFileSpec.toString());
      Tooltip.install(setTemplateLibraryButton, setTemplateLibraryButtonTip);
      saveTemplateDirectory();
      if (list != null) {
        FileSpec source = list.getSource();
        if (source != null) {
          source.setTemplatesFolder(templateLibraryFileSpec);
        }
      }
    }
  }

  /**
     Open a Template File from the Template Library
   */
  private void openTemplateFromLibrary () {
    templateFileOK = false;
    FileChooser fileChooser = new FileChooser();
    if (validTemplateLibrary()) {
      fileChooser.setInitialDirectory (getTemplateLibrary());
    }
    File selectedFile
      = fileChooser.showOpenDialog (ownerWindow);
    if (selectedFile != null) {
      templateFile = selectedFile;
      templateOpen();
      if (! templateFileOK) {
        Trouble.getShared().report(
          "Error occurred while opening template file",
          "Template File Error");
      } // end if error opening template file
    } // end if user performed a valid file selection
  }

  /**
     Generate the template results
   */
  private void generateTemplate () {
    checkTemplateRepeat();
    boolean repeatOK = false;
    boolean ok = true;
    if (templateFileOK && fileAvailable() && templateCreated) {
      if (
          // (! lastTabNameOutput.equals (""))
          (lastTemplateFile != null)
          // && (lastTabNameOutput.equals (tabName))
            && (lastTemplateFile.equals (templateFile))) {
        Alert repeatAlert = new Alert(AlertType.CONFIRMATION);
        repeatAlert.setTitle("Repeat Confirmation");
        repeatAlert.setContentText
          ("Are you sure you want to repeat\n the last merge operation?");
        Optional<ButtonType> result = repeatAlert.showAndWait();
        ok = (result.get() == ButtonType.OK);
      } // end if repeating a merge operation
      if (ok) {
        templateGenerate();
        if (! generateOutputOK) {
          Trouble.getShared().report(
            "Error occurred while generating output file",
            "Output File Error");
        } // end if output not OK
      } // end if all necessary files ready
    } else {
      Trouble.getShared().report(
        "One or Both Input files Not Ready",
        "Input File Error");
    } // end input files not ready
  }

  /**
     Checks to see if a template file is to be reused.
   */
  private void checkTemplateRepeat () {
    if ((lastTemplateFile != null)
      && (! templateFileReady)) {
      templateFile = lastTemplateFile;
      templateOpen ();
    }
  } // end method checkTemplateRepeat

  /**
     Generates the output file specified in the template.
   */
  private void templateGenerate() {

    if (templateFileOK
        && fileAvailable()
        && templateCreated
        && list instanceof DataSource) {
      DataSource source = (DataSource)list;
      template.setWebRoot (webRootFile);
      if (list.getSource() != null) {
        template.openData (source, list.getSource().toString());
      }
      try {
        generateOutputOK = template.generateOutput();
      } catch (IOException e) {
        generateOutputOK = false;
      }
      lastTemplateFile = templateFile;
      // lastTabNameOutput = tabName;
      templateCreated = false;
      templateFileReady = false;
      if (generateOutputOK) {
        FileName textFileOutName = template.getTextFileOutName();
        if (textFileOutName == null) {
          outputFileName = null;
        } else {
          outputFileName = textFileOutName.toString();
        }
        if (outputFileName != null) {
          FileName outputFN = new FileName (outputFileName);
          if (generateOutputName != null) {
            generateOutputName.setText (outputFN.getFileName());
            Tooltip generateOutputNameTip = new Tooltip
              (generateOutputName.toString());
            Tooltip.install(generateOutputName, generateOutputNameTip);
          }
        }
        textMergeScript.recordScriptAction (
          ScriptConstants.TEMPLATE_MODULE,
          ScriptConstants.GENERATE_ACTION,
          ScriptConstants.NO_MODIFIER,
          ScriptConstants.NO_OBJECT,
          ScriptConstants.NO_VALUE);
      } else {
        Logger.getShared().recordEvent (LogEvent.MEDIUM,
          "Error occurred while generating output file from template "
          + templateFileName,
          true);
      }
    } else {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        "One or Both Input files (Template and/or Data) Not Ready",
        true);
    }
  } // end method templateGenerate

  private void setTemplateDirectoryFromFile (File inFile) {
    templateLibraryFileSpec = new File(inFile.getParent());
    saveTemplateDirectory();
  }

  private void setTemplateDirectoryFromDir (File inFile) {
    templateLibraryFileSpec = inFile;
    saveTemplateDirectory();
  }

  private void saveTemplateDirectory() {
    if (list != null) {
      FileSpec source = list.getSource();
      if (source != null) {
        source.setTemplatesFolder(templateLibraryFileSpec);
      }
    }
  }

  public void resetOutputFileName() {
    outputFileName = "";
  }

  public String getOutputFileName() {
    return outputFileName;
  }

  public void savePrefs() {
    // UserPrefs.getShared().setPref
    //     (TEMPLATE_LIBRARY_KEY, templateLibrary.toString());
  }

}
