/*
 * Copyright 2011 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.publish;

	import com.powersurgepub.psutils2.env.*;
	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.script.*;
	import com.powersurgepub.psutils2.textmerge.*;
	import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;
  import java.net.*;
  import java.util.*;

 	import javafx.event.*;
 	import javafx.scene.*;
 	import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
  import javafx.scene.control.ButtonBar.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;

/**
  A window that can be used to publish some source data in a different form,
  typically for browsing on the Web.
  <p>
  Initial use is for iWisdom. Also used for URL Union.
  <p>
  This class stores data about publications in multiple properties files named
  "pspub_parms.xml". One file is stored in the source folder for the data
  collection, and contains a list of the publish to locations associated with
  that data collection. Another file is tucked away in each publish to location,
  with each of these files containing the various properties specified by the
  user for that particular publication. 

 @author Herb Bowie
 */
public class PublishWindow 
      implements 
        ScriptExecutor,
        WindowToManage {
  
  public final static String PUBLISH_TO         = "publish-to";
  public final static String PUBLISH_URL        = "publish-url";
  public final static String PUBLISH_SCRIPT     = "publish-script";
  public final static String PUBLISH_WHEN       = "publish-when";
  public final static String   PUBLISH_ON_SAVE    = "On Save";
  public final static String   PUBLISH_ON_CLOSE   = "On Close";
  public final static String   PUBLISH_ON_DEMAND  = "On Demand";
  public final static String PUBLISH_TEMPLATES  = "publish-templates";
  public final static String PUBLISH_TEMPLATE   = "publish-template";

  public final static String INDEX_FILE_NAME
      = "index.html";
  public final static String PROPERTIES_OLD_FILE_NAME
      = "pspub_parms.xml";
  public final static String PROPERTIES_SOURCE_FILE_NAME
      = "pspub_source_parms.xml";
  public final static String PROPERTIES_PUBLICATION_FILE_NAME
      = "pspub_publication_parms.xml";

  public final static String[] publishToOptions = {
                      "Add New",
                      "Replace Existing",
                      "Cancel"};
  public final static Object[] publishRemoveOptions = {
                      "Remove Existing",
                      "Cancel"};
  public final static int     UNDEFINED          = -1;
  public final static int     ADD                = 0;
  public final static int     REPLACE            = 1;
  public final static int     CANCEL             = 2;
  public final static int     REMOVE             = 3;

  public final static int     SAVE               = 1;
  public final static int     CLOSE              = 2;
  public final static int     DEMAND             = 3;
  
  private             int     option             = UNDEFINED;

  private             PublishAssistant assistant;

  private             File    defaultTemplatesFolder;
  private             String  defaultTemplatesFolderText;

  private             File             source      = null;
  private             ArrayList        pubs = new ArrayList();
  private             int              currentSelectionIndex = -1;

  private             int              inputSource = SYSTEM_INPUT;
  public final static int                SYSTEM_INPUT = 0;
  public final static int                USER_INPUT = 1; 

  private             File             publishTo = null;
  
  private             StatusBar        statusBar = null;
  
  private             Home             home = Home.getShared();
  
  private             TextMergeHarness textMerge = null;
  
  private     FXUtils             fxUtils;
  private     Stage publishStage;
  private     Scene publishScene;
  private     GridPane publishPane;
  
  private     WindowToManage windowToManage;
  
  
  // Publish To Panel
  private     GridPane publishToPanel;
  private     Label publishToLabel;
  private     Label filler2;
  private     Button publishToBrowseButton;
  private     ComboBoxWidget publishToComboBox;
  private     Label equivalentURLLabel;
  private     TextField equivalentURLText;

  // Templates Panel
  private     GridPane templatePanel;
  private     Label templatesLabel;
  private     Label filler1;
  private     Button templatesBrowseButton;
  private     TextField templatesText;
  private     Label templateSelectLabel;
  private     ComboBoxWidget templateSelectComboBox;
  private     Button templateApplyButton;
  
  // Publish Script Panel
  private     GridPane publishScriptPanel;
  private     Label publishScriptLabel;
  private     Label filler3;
  private     Button publishScriptBrowseButton;
  private     TextField publishScriptText;
  private     Label publishWhenLabel;
  private     ComboBoxWidget publishWhenComboBox;
  private     Button publishNowButton;
  
  // View Panel
  private     GridPane viewPanel;
  private     Label viewLabel;
  private     ComboBoxWidget viewWhereComboBox;
  private     Button viewNowButton;
  
  /** 
   Creates new form PublishWindow. Sets up the window,
   but doesn't load any data.
   */
  public PublishWindow(PublishAssistant assistant) {

    this.assistant = assistant;
    windowToManage = this;
    buildUI();
    publishStage.setTitle (Home.getShared().getProgramName() + " Publish");
    defaultTemplatesFolder
            = new File (Home.getShared().getAppFolder(), "templates");
    try {
      defaultTemplatesFolderText = defaultTemplatesFolder.getCanonicalPath();
    }  catch (java.io.IOException ioex) {
      defaultTemplatesFolderText = "";
    }
    textMerge = TextMergeHarness.getShared();
    textMerge.setExecutor(this);
    textMerge.initTextMergeModules();
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    // Start building the top level panel
    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		publishStage = new Stage(StageStyle.DECORATED);
    publishStage.setTitle("Publish");
    
    publishStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    }); 
    
    publishStage.setOnHidden(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    });
    
    publishPane = new GridPane();
    fxUtils.applyStyle(publishPane);

    // Build the first interior panel -- the Publish To Panel
    
		publishToPanel = new GridPane();
		fxUtils.applyStyle(publishToPanel);

		publishToLabel = new Label("Publish to:");
    fxUtils.standardize(publishToLabel);
		publishToPanel.add(publishToLabel, 0, rowCount, 1, 1);

		filler2 = new Label();
		publishToPanel.add(filler2, 1, rowCount, 1, 1);
		filler2.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filler2, Priority.ALWAYS);

		publishToBrowseButton = new Button("Browse…");
    fxUtils.standardize(publishToBrowseButton);
    publishToBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        inputSource = SYSTEM_INPUT;
        if (publishToComboBox.getSelectedString() != null
            && publishToComboBox.getSelectedString().length() > 0) {
          askAddOrReplace();
        } else {
          option = ADD;
        }

        if (option == ADD || option == REPLACE) {
          DirectoryChooser chooser = new DirectoryChooser();
          chooser.setTitle("Specify Target Folder for Publication");
          File chosen = chooser.showDialog(publishStage);
          String publishToString = "";
          if (chosen != null) {
            try {
              publishToString = chosen.getCanonicalPath();
            } catch (java.io.IOException e) {
              System.out.println("I/O Exception looking for canonical path to " + chosen.toString());
              publishToString = chosen.getAbsolutePath();
            }
            addOrReplaceOrRemove(publishToString);
          } // end if user chose a folder
        } // end if user specified whether to add or replace
        inputSource = USER_INPUT;
		  } // end handle method
		}); // end event handler
		publishToPanel.add(publishToBrowseButton, 2, rowCount, 1, 1);
		publishToBrowseButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(publishToBrowseButton, Priority.SOMETIMES);

		rowCount++;

		publishToComboBox = new ComboBoxWidget();
    publishToComboBox.setEditable(true);
    publishToComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (inputSource == USER_INPUT) {
          inputSource = SYSTEM_INPUT;
          if (publishToComboBox.getSelectedIndex() >= 0) {
            // User has selected an existing value from the drop-down menu
            switchPublishTo(
                currentSelectionIndex, 
                publishToComboBox.getSelectedIndex());
          } else {
            // User has typed in a new value
            String publishToString = publishToComboBox.getSelectedString();
            if (option == UNDEFINED || option == CANCEL) {
              if (publishToString.length() == 0) {
                askRemove();
              } else {
                askAddOrReplace();
              }
            }
            addOrReplaceOrRemove(publishToString);
          }
          inputSource = USER_INPUT;
        }
		  } // end handle method
		}); // end event handler
		publishToPanel.add(publishToComboBox, 0, rowCount, 3, 1);
		publishToComboBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(publishToComboBox, Priority.ALWAYS);

		rowCount++;

		equivalentURLLabel = new Label("Equivalent URL:");
    fxUtils.standardize(equivalentURLLabel);
		publishToPanel.add(equivalentURLLabel, 0, rowCount, 1, 1);

		rowCount++;

		equivalentURLText = new TextField();
    equivalentURLText.setPrefColumnCount(50);
    Tooltip equivalentURLTip = new Tooltip
        ("Folder to which data will be published");
      Tooltip.install(equivalentURLText, equivalentURLTip);
    equivalentURLText.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		publishToPanel.add(equivalentURLText, 0, rowCount, 3, 1);
		equivalentURLText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(equivalentURLText, Priority.ALWAYS);

		rowCount++;
    
    publishPane.add(publishToPanel, 0, 0, 1, 1);
		GridPane.setHgrow(publishToPanel, Priority.ALWAYS);
    
    // Now build the second interior panel -- the templates panel
    
    rowCount = 0;

		templatePanel = new GridPane();
		fxUtils.applyStyle(templatePanel);

		templatesLabel = new Label("Templates:");
    fxUtils.standardize(templatesLabel);
		templatePanel.add(templatesLabel, 0, rowCount, 1, 1);

		filler1 = new Label();
		templatePanel.add(filler1, 1, rowCount, 1, 1);
		filler1.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filler1, Priority.ALWAYS);

		templatesBrowseButton = new Button("Browse…");
    fxUtils.standardize(templatesBrowseButton);
    templatesBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Specify Templates Folder");
        File chosen = chooser.showDialog(publishStage);
        if (chosen != null) {
          setTemplatesFolder(chosen);
        }
		  } // end handle method
		}); // end event handler
		templatePanel.add(templatesBrowseButton, 2, rowCount, 1, 1);
		templatesBrowseButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(templatesBrowseButton, Priority.SOMETIMES);

		rowCount++;

		templatesText = new TextField();
    templatesText.setPrefColumnCount(50);
    templatesText.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		templatePanel.add(templatesText, 0, rowCount, 3, 1);
		templatesText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(templatesText, Priority.ALWAYS);

		rowCount++;

		templateSelectLabel = new Label("Select:");
		templatePanel.add(templateSelectLabel, 0, rowCount, 1, 1);
		templateSelectLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(templateSelectLabel, Priority.SOMETIMES);

		templateSelectComboBox = new ComboBoxWidget();
    templateSelectComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		templatePanel.add(templateSelectComboBox, 1, rowCount, 1, 1);
		templateSelectComboBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(templateSelectComboBox, Priority.SOMETIMES);

		templateApplyButton = new Button("Apply");
    fxUtils.standardize(templateApplyButton);
    templateApplyButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        applySelectedTemplate();
		  } // end handle method
		}); // end event handler
		templatePanel.add(templateApplyButton, 2, rowCount, 1, 1);
		templateApplyButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(templateApplyButton, Priority.SOMETIMES);

		rowCount++;
    
    publishPane.add(templatePanel, 0, 1, 1, 1);
		GridPane.setHgrow(publishToPanel, Priority.ALWAYS);
    
    // Now build the third interior panel -- the Publish Script Panel
    
    rowCount = 0;

		publishScriptPanel = new GridPane();
		fxUtils.applyStyle(publishScriptPanel);

		publishScriptLabel = new Label("Publish Script:");
    fxUtils.standardize(publishScriptLabel);
		publishScriptPanel.add(publishScriptLabel, 0, rowCount, 1, 1);

		filler3 = new Label();
		publishScriptPanel.add(filler3, 1, rowCount, 1, 1);
		filler3.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filler3, Priority.ALWAYS);

		publishScriptBrowseButton = new Button("Browse…");
    fxUtils.standardize(publishScriptBrowseButton);
    publishScriptBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Locate Publication Script");
        File chosen = chooser.showDialog(publishStage);
        if (chosen != null) {
          setPublishScript(chosen);
        }
		  } // end handle method
		}); // end event handler
		publishScriptPanel.add(publishScriptBrowseButton, 2, rowCount, 1, 1);
		publishScriptBrowseButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(publishScriptBrowseButton, Priority.SOMETIMES);

		rowCount++;

		publishScriptText = new TextField();
    publishScriptText.setPrefColumnCount(50);
		Tooltip publishScriptTextTip = new Tooltip("Folder to which data will be published");
    Tooltip.install(publishScriptText, publishScriptTextTip);
    publishScriptText.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		publishScriptPanel.add(publishScriptText, 0, rowCount, 3, 1);
		publishScriptText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(publishScriptText, Priority.ALWAYS);

		rowCount++;

		publishWhenLabel = new Label("Publish when:");
    fxUtils.standardize(publishWhenLabel);
		publishScriptPanel.add(publishWhenLabel, 0, rowCount, 1, 1);
		publishWhenLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(publishWhenLabel, Priority.SOMETIMES);

		publishWhenComboBox = new ComboBoxWidget();
    publishWhenComboBox.addItem("On Demand");
    publishWhenComboBox.addItem("On Close");
    publishWhenComboBox.addItem("On Save");
    publishWhenComboBox.addItem(" ");
    publishWhenComboBox.addItem(" ");
    fxUtils.standardize(publishWhenComboBox);
    publishWhenComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		publishScriptPanel.add(publishWhenComboBox, 1, rowCount, 1, 1);
		publishWhenComboBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(publishWhenComboBox, Priority.SOMETIMES);

		publishNowButton = new Button("Publish Now");
    fxUtils.standardize(publishNowButton);
    publishNowButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        savePublicationProperties(currentSelectionIndex);
        publish();
		  } // end handle method
		}); // end event handler
		publishScriptPanel.add(publishNowButton, 2, rowCount, 1, 1);
		publishNowButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(publishNowButton, Priority.SOMETIMES);

		rowCount++;
    
    publishPane.add(publishScriptPanel, 0, 2, 1, 1);
		GridPane.setHgrow(publishToPanel, Priority.ALWAYS);
    
    // Now Build the fourth interior panel -- the View Panel
    
    rowCount = 0;

		viewPanel = new GridPane();
		fxUtils.applyStyle(viewPanel);

		viewLabel = new Label("View:");
    fxUtils.standardize(viewLabel);
		viewPanel.add(viewLabel, 0, rowCount, 1, 1);

		viewWhereComboBox = new ComboBoxWidget();
    viewWhereComboBox.addItem("Local File");
    viewWhereComboBox.addItem("On Web");
    fxUtils.standardize(viewWhereComboBox);
    viewWhereComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		viewPanel.add(viewWhereComboBox, 1, rowCount, 1, 1);
		viewWhereComboBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(viewWhereComboBox, Priority.SOMETIMES);

		viewNowButton = new Button("View Now:");
    fxUtils.standardize(viewNowButton);
    viewNowButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (getPublishTo() != null) {
          File indexFile = new File (getPublishTo(), INDEX_FILE_NAME);
          if (indexFile.exists()
              && indexFile.canRead()
              && indexFile.isFile()) {
            if (viewWhereComboBox.getSelectedIndex() == 0) {
              openURL (indexFile);
            } else {
              String indexFileName = indexFile.getPath();
              openURL (getEquivalentURLText()
                  + indexFileName.substring(getPublishToText().length()));
            }
          } // end if index file is available
        }
		  } // end handle method
		}); // end event handler
		viewPanel.add(viewNowButton, 2, rowCount, 1, 1);
		viewNowButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(viewNowButton, Priority.SOMETIMES);

		rowCount++;
    
    publishPane.add(viewPanel, 0, 3, 1, 1);
		GridPane.setHgrow(viewPanel, Priority.ALWAYS);

    // Finish up

		publishScene = new Scene(publishPane);
    publishStage.setScene(publishScene);
    
  }
    
  /**
   Indicate whether publish on save is a valid option.

   @param onSaveOption If true, then user will have the option to publish
                       on saveSource; if false, then they will not see that option.
   */
  public void setOnSaveOption (boolean onSaveOption) {
    String publishWhen = publishWhenComboBox.getSelectedString();
    publishWhenComboBox.removeAllItems();
    publishWhenComboBox.addItem(PUBLISH_ON_DEMAND);
    publishWhenComboBox.addItem(PUBLISH_ON_CLOSE);
    if (onSaveOption) {
      publishWhenComboBox.addItem(PUBLISH_ON_SAVE);
    }
    setPublishWhen (publishWhen);
  }

  /**
   Indicate that a new data source is being opened, and pass the folder
   containing the source data. This method will try to obtain a list of
   publications from this folder.

   @param source The folder containing the source data.
   */
  public void openSource(File source) {
    
    this.source = source;

    inputSource = SYSTEM_INPUT;

    // Load the list of all known publications for this data source
    pubs = new ArrayList();
    currentSelectionIndex = -1;
    // publishToComboBox.setSelectedItem("");
    initPublicationProperties();
    String publishToText;
    if (source != null
        && source.exists()
        && source.canRead()) {
      Properties sourceProps = new Properties();
      File oldFile = new File (source, PROPERTIES_OLD_FILE_NAME);
      File inFile = new File (source, PROPERTIES_SOURCE_FILE_NAME);
      if ((inFile == null
          || (! inFile.exists()))
          && (oldFile != null
            && oldFile.exists())) {
        inFile = oldFile;
      }
      if (inFile != null
          && inFile.exists()
          && inFile.canRead()) {
        try {
          FileInputStream in = new FileInputStream (inFile);
          sourceProps.loadFromXML(in);
          int i = 0;
          publishToText = sourceProps.getProperty(
              PUBLISH_TO + "-" + String.valueOf(i));
          while (publishToText != null) {
            addPublication(publishToText);
            i++;
            publishToText = sourceProps.getProperty(
              PUBLISH_TO + "-" + String.valueOf(i));
          }
        } catch (java.io.FileNotFoundException exfnf) {
          System.out.println ("File not found");
        } catch (java.io.IOException exio) {
          System.out.println ("I/O Exception");
        }
      } // end if inFile is available
    } // end if source folder is available
    
    // Populate the publish to combo box
    populatePublishToComboBox();

    // Load the data for the first publish to location
    if (pubs.size() > 0) {
      setPublishTo(0);
    }

    inputSource = USER_INPUT;
  }

  /**
   Indicate that the current data source is being saved. 
   */
  public void saveSource() {
    
    // If user has requested that we publish on save, then publish now.
    checkForPublication(SAVE);
    
    // Save the list of publications for this data source
    savePubsForSource();
  }

  /**
   Indicate that the current data source is being closed. This method will try
   to save the current list of publications for this data source, within the
   source folder passed at open. 
   */
  public void closeSource() {
    
    // If user has requested that we publish on close, then publish now
    checkForPublication(CLOSE);

    // Save the list of publications for this data source
    savePubsForSource();
  }
  
  /**
   Save the list of publications for this data source.
   */
  private void savePubsForSource() {
    
    if (source != null
        && source.exists()
        && source.canWrite()
        && pubs.size() > 0) {
      Properties sourceProps = new Properties();
      for (int i = 0; i < pubs.size(); i++) {
        sourceProps.setProperty(
            PUBLISH_TO + "-" + String.valueOf(i),
            (String)pubs.get(i));
      }
      File outFile = new File (source, PROPERTIES_SOURCE_FILE_NAME);
      try {
        FileOutputStream out = new FileOutputStream (outFile);
        sourceProps.storeToXML(out,
            "com.powersurgepub.pspub.PublishWindow properties");
        File oldFile = new File (source, PROPERTIES_OLD_FILE_NAME);
        if (oldFile != null
            && oldFile.exists()) {
          oldFile.delete();
        }
      } catch (java.io.FileNotFoundException exfnf) {
        System.out.println ("File not found");
      } catch (java.io.IOException exio) {
        System.out.println ("I/O Exception");
      }
    }
  }

  /**
   Check each publication for this data source to see if this is an
   appropriate occasion for publication.

   @param occasion 1 = Save,
                   2 = Close,
                   3 = Demand.
   */
  private void checkForPublication(int occasion) {

    savePublicationProperties(currentSelectionIndex);
    int saveIndex = currentSelectionIndex;
    for (int i = 0; i < pubs.size(); i++) {
      setPublishTo(i);
      switch (occasion) {
        case SAVE:
          if (getPublishWhen().equalsIgnoreCase(PUBLISH_ON_SAVE)) {
            publish();
          }
          break;
        case CLOSE:
          if (getPublishWhen().equalsIgnoreCase(PUBLISH_ON_CLOSE)) {
            publish();
          }
          break;
        case DEMAND:
          if (i == saveIndex) {
            publish();
          }
          break;
        default:
          break;
      }
    }
    setPublishTo(saveIndex);
  }
  
  private void switchPublishTo(int fromIndex, int toIndex) {
    savePublicationProperties(fromIndex);
    setPublishTo(toIndex);
  }

  /**
   Set the publish to location, using the given index to indicate which
   of the known locations is to become the current one.

   @param selectedIndex An index to the pubs list.
   */
  private void setPublishTo(int toIndex) {
    if (toIndex >= 0
        && toIndex < pubs.size()) {
      if (toIndex != publishToComboBox.getSelectedIndex()) {
        publishToComboBox.setSelectedIndex(toIndex);
      }
      String publishToText = (String)pubs.get(toIndex);
      loadPublication(publishToText);
    }
    currentSelectionIndex = toIndex;
  }

  /**
   Load the properties for the given publishTo location.

   @param publishTo The folder to which the data is to be published.
   */
  private void loadPublication(String publishToText) {

    initPublicationProperties();
    if (publishToText != null
        && publishToText.length() > 0) {
      // setPublishTo (publishTo);
      File publishToFolder = new File(publishToText);
      if (publishToFolder.exists()
          && publishToFolder.canRead()) {
        Properties publishProps = new Properties();
        File inFile = new File 
            (publishToFolder, PROPERTIES_PUBLICATION_FILE_NAME);
        File oldFile = new File (publishToFolder, PROPERTIES_OLD_FILE_NAME);
        if ((inFile == null
              || (! inFile.exists()))
            && oldFile != null
            && oldFile.exists()) {
          inFile = oldFile;
        }
        try {
          FileInputStream in = new FileInputStream (inFile);
          publishProps.loadFromXML(in);
          setEquivalentURL(publishProps.getProperty(PUBLISH_URL, ""));
          setPublishScript(publishProps.getProperty(PUBLISH_SCRIPT, ""));
          setPublishWhen(publishProps.getProperty(PUBLISH_WHEN, ""));         
          setTemplatesFolder(publishProps.getProperty(PUBLISH_TEMPLATES,
              defaultTemplatesFolderText));
          setTemplate(publishProps.getProperty(PUBLISH_TEMPLATE, ""));
        } catch (java.io.FileNotFoundException exfnf) {
          // let's hope this doesn't happen
        } catch (java.io.IOException exio) {
          // and that this doesn't happen
        }
      } // end if publish to folder is ready to be used
    } // end if publish to folder has a value

  }

  /**
   Add a publication to the internal list of all publications for this data
   source. Ensure that no duplicates are added.

   @param publishToText The folder path to which the data source will be published.

   @return The index at which the publish to location was added, or was found
           to already exist.
   */
  private int addPublication (String publishToText) {
    int i = -1;
    if (publishToText != null
        && publishToText.length() > 0) {
      File publishToFolder = new File (publishToText);
      if (publishToFolder.exists()) {
        boolean found = false;
        i = 0;
        while (i < pubs.size() && (! found)) {
          found = publishToText.equals((String)pubs.get(i));
          if (! found) {
            i++;
          }
        }
        if (! found) {
          pubs.add(publishToText);
        }
      } // end if publish to folder exists
    } // end if publish to string isn't empty
    return i;
  }

  /**
   Populate the publish to Combo Box from the contents of the publications list.
   */
  private void populatePublishToComboBox() {
    publishToComboBox.removeAllItems();
    for (int i = 0; i < pubs.size(); i++) {
      publishToComboBox.addItem(pubs.get(i));
    }
  }

  /**
   Save the publication properties specified by the user. 
   */
  private void savePublicationProperties(int pubIndex) {

    if (pubIndex >= 0 && pubIndex < pubs.size()) {
      String publishToText = (String)publishToComboBox.getItemAt(pubIndex);
      if (publishToText != null
          && publishToText.length() > 0) {
        File publishToFolder = new File(publishToText);
        if (publishToFolder.exists()
            && publishToFolder.canWrite()) {
          Properties publishProps = new Properties();
          publishProps.setProperty(PUBLISH_TO,        getPublishToText());
          publishProps.setProperty(PUBLISH_URL,       getEquivalentURLText());
          publishProps.setProperty(PUBLISH_SCRIPT,    getPublishScriptText());
          publishProps.setProperty(PUBLISH_WHEN,      getPublishWhen());
          publishProps.setProperty(PUBLISH_TEMPLATES, getTemplatesFolderText());
          publishProps.setProperty(PUBLISH_TEMPLATE,   getTemplate());
          File outFile = new File 
              (publishToFolder, PROPERTIES_PUBLICATION_FILE_NAME);
          try {
            FileOutputStream out = new FileOutputStream (outFile);
            publishProps.storeToXML(out,
                "com.powersurgepub.pspub.PublishWindow properties");
            File oldFile = new File (publishToFolder, PROPERTIES_OLD_FILE_NAME);
            if (oldFile != null
                && oldFile.exists()) {
              oldFile.delete();
            }
          } catch (java.io.FileNotFoundException exfnf) {
            // let's hope this doesn't happen
          } catch (java.io.IOException exio) {
            // and that this doesn't happen
          }
        } // end if publish to folder is ready to be used
      } // end if publish to folder has a value
    } // end if we have a value publication index
  }

  /**
   Initialize the window's fields to reflect null or other default values.
   */
  private void initPublicationProperties() {
    setEquivalentURL("");
    setTemplatesFolder(defaultTemplatesFolderText);
    if (templateSelectComboBox.getItemCount() > 0) {
      templateSelectComboBox.setSelectedIndex(0);
    }
    setPublishScript("");
    setPublishWhen(PUBLISH_ON_DEMAND);
    viewWhereComboBox.setSelectedIndex(0);
  }
  
  // Getters and Setters for publish to location

  private File getPublishTo() {
    return new File(getPublishToText());
  }

  private String getPublishToText() {
    return publishToComboBox.getSelectedString();
  }

  // Getters and Setters for equivalent URL

  private void setEquivalentURL(URL equivalentURL) {
    equivalentURLText.setText(equivalentURL.toString());
  }

  private void setEquivalentURL(String equivalentURLText) {
    this.equivalentURLText.setText(equivalentURLText);
  }

  private String getEquivalentURLText() {
    return equivalentURLText.getText();
  }

  private URL getEquivalentURL() {
    if (equivalentURLText.getText().length() == 0) {
      return null;
    } else {
      try { 
        return new URL(equivalentURLText.getText());
      } catch (MalformedURLException e) {
        return null;
      }
    }
  }

  // Getters and setters for Templates folder

  private void setTemplatesFolder(File templatesFolder) {
    try {
      setTemplatesFolder(templatesFolder.getCanonicalPath());
    } catch (java.io.IOException e) {
      // ignore errors
    }
  }

  private void setTemplatesFolder(String templatesFolderText) {
    templatesText.setText(templatesFolderText);
    populateTemplates();
  }

  /**
   Populate the list of available template collections, based on the location
   of the templates folder.
   */
  private void populateTemplates () {
    File templatesFolder = new File (templatesText.getText());
    if (templatesFolder != null
        && templatesFolder.exists()
        && templatesFolder.isDirectory()
        && templatesFolder.canRead()) {
      File[] templates = templatesFolder.listFiles();
      if (templates != null) {
        // Bubble sort the array to get it into alphabetical order, ignoring case
        boolean swapped = true;
        while (swapped) {
          swapped = false;
          int i = 0;
          int j = 1;
          while (j < templates.length) {
            String lower = templates[i].getName();
            String higher = templates[j].getName();
            if (lower.compareToIgnoreCase(higher) > 0) {
              File hold = templates[i];
              templates[i] = templates[j];
              templates[j] = hold;
              swapped = true;
            } // end if we need to swap the two entries
            i++;
            j++;
          } // end one pass through the array of templates
        } // end while still swapping entries

        // Now load templates into drop-down menu
        templateSelectComboBox.removeAllItems();
        int i = 0;
        while (i < templates.length) {
          if (templates[i].getName().length() > 0
              && templates[i].getName().charAt(0) != '.'
              && templates[i].exists()
              && templates[i].canRead()
              // && templates[i].isFile()
              ) {
            templateSelectComboBox.addItem (templates[i].getName());
          } // end if folder entry looks like a usable template
          i++;
        } // end while loading combo box with templates
      } // end if the templates folder had any contentes
    } // end if we found a valid templates folder
  }

  private String getTemplatesFolderText() {
    return templatesText.getText();
  }

  private File getTemplatesFolder() {
    return new File(templatesText.getText());
  }

  // Getters and setters for template

  private void setTemplate(String template) {
    selectTemplate(template);
  }

  private void selectTemplate (String selectedTemplate) {
    int i = 0;
    boolean found = false;
    while ((! found) && (i < templateSelectComboBox.getItemCount())) {
      if (selectedTemplate.equalsIgnoreCase
          ((String)templateSelectComboBox.getItemAt(i))) {
        templateSelectComboBox.setSelectedIndex(i);
        found = true;
      } else {
        i++;
      }
    } // end while looking for match
  } // end method setPublishWhen

  private String getTemplate() {
    return templateSelectComboBox.getSelectedString();
  }

  // Getters and setters for publish script file

  private void setPublishScript(String publishScriptText) {
    this.publishScriptText.setText(publishScriptText);
  }

  private void setPublishScript(File publishScript) {
    try {
      publishScriptText.setText(publishScript.getCanonicalPath());
    } catch (java.io.IOException e) {
      // ignore errors
    }
  }

  private String getPublishScriptText() {
    return publishScriptText.getText();
  }

  private File getPublishScript() {
    return new File(publishScriptText.getText());
  }

  /**
   Apply the selected template collection, copying the contents of its
   folder to the publish to location.
   */
  private void applySelectedTemplate() {
    String selectedTemplateFolderName = getTemplate();
    if (selectedTemplateFolderName == null) {
      Trouble.getShared().report(
        publishStage,
        "No template has been selected",
        "Template Error");
    } else {
      File templateFolder = new File (
          getTemplatesFolderText(),
          selectedTemplateFolderName);
      if ((! templateFolder.exists())
          || (! templateFolder.canRead())) {
        Trouble.getShared().report(
          publishStage,
          "Template folder cannot be accessed",
          "Template Error");
      } else {
        publishTo = getPublishTo();
        if (publishTo == null
            || (! publishTo.exists()
            || (! publishTo.canWrite()))) {
          Trouble.getShared().report(
            publishStage,
            "Publish To folder is not valid",
            "Template Error");
        } else {
          FileUtils.copyFolder(templateFolder, publishTo);
          String[] dirEntry = publishTo.list();
          for (int i = 0; i < dirEntry.length; i++) {
            String entry = dirEntry [i];
            if (entry.endsWith(".tcz")) {
              File scriptFile = new File (publishTo, entry);
              this.setPublishScript(scriptFile);
            } // end if we have a likely script file
          } // end for each file in the publish to folder
        } // end if we have a good folder to publish to
      } // end if we have a template folder we can read
    } // end if we have a selected template folder name
  } // end method applySelectedTemplate

  // Getters and setters for publish when

  private void setPublishWhen (String publishWhen) {
    int i = 0;
    boolean found = false;
    while ((! found) && (i < publishWhenComboBox.getItemCount())) {
      if (publishWhen.equalsIgnoreCase
          ((String)publishWhenComboBox.getItemAt(i))) {
        publishWhenComboBox.setSelectedIndex(i);
        found = true;
      } else {
        i++;
      }
    } // end while looking for match
  } // end method setPublishWhen

  private String getPublishWhen() {
    return publishWhenComboBox.getSelectedString();
  }
  
  public void publishNow() {
    publish();
  }

  /**
   Perform the publication process.
   */
  private void publish() {
    
    // See if we have a good publishTo location
    if (currentSelectionIndex >= 0) {
      publishTo = new File (publishToComboBox.getSelectedString());
      Logger.getShared().recordEvent (LogEvent.NORMAL,
        "Publishing to " + publishTo.toString(), false);
      File publishScript = getPublishScript();
      Logger.getShared().recordEvent (LogEvent.NORMAL,
        "Using Script  " + publishScript.toString(), false);
      boolean ok = true;
      if (! publishTo.exists()) {
        ok = publishTo.mkdirs();
      }
      if (ok) {
        ok = publishTo.exists()
          && publishTo.canRead()
          && publishTo.canWrite();
      }

      if (ok) {
        ok = publishScript.exists()
          && publishScript.canRead();
      }

      if (ok) {
        assistant.prePub(publishTo);
        FileName publishScriptName = new FileName(publishScript);

        // See if script is an xslt transformer
        if (publishScriptName.getExt().equals("xml")) {
          XMLTransformer tf = new XMLTransformer();
          ok = tf.transform (publishScript, publishTo);
          if (ok) {
            Logger.getShared().recordEvent (LogEvent.NORMAL,
                "  xslScript processed " + publishScript.toString(), false);
            announceStatus("Published");
          } else {
            Logger.getShared().recordEvent (LogEvent.MINOR,
                "  xslScript error processing " + publishScript.toString(), false);
          }
        }
        else
        if (publishScriptName.getExt().equalsIgnoreCase(TextMergeScript.SCRIPT_EXT)) {
          textMerge.playScript (publishScript.getAbsolutePath());
          announceStatus("Published");
        } else {
          Trouble.getShared().report(
              publishStage,
              "File extension for publish script file not recognized",
              "Unknown Script Type");
        }

        assistant.postPub(publishTo);

      } else {
        Trouble.getShared().report(
            publishStage,
            "Trouble with the publication location and/or script",
            "Publish Error");
      }
    }
  } // end of publish method

  /**
   A method provided to PSTextMerge 
   @param operand
   */
  public void scriptCallback(String operand) {
    if (assistant != null
        && publishTo != null) {
      assistant.pubOperation(publishTo, operand);
    }
  }

  /**
   Open the designated file in the user's Web browser.

   @param file The file to be browsed.
   */
  private boolean openURL (File file) {
    return home.openURL(file);
  }

  /**
   Open the designated URL in the user's Web browser.

   @param url The URL to be browsed.
   */
  private boolean openURL (String url) {
    return home.openURL(url);
  }
  
  /**
   The user has indicated a desire to blank out the publish to folder:
   see if they are trying to remove an existing publication.
   */
  private void askRemove() {
    if (option == UNDEFINED) {
      Alert removeAlert = new Alert(AlertType.CONFIRMATION);
      removeAlert.setTitle("Remove");
      removeAlert.setContentText("Remove existing location?");
      Optional<ButtonType> result = removeAlert.showAndWait();
      if (result.get() == ButtonType.OK) {
        option = REMOVE;
      } else {
        option = CANCEL;
      }
    }
  }

  /**
   The user has indicated a desire to modify the publish to folder:
   see if they are trying to add a new publication, or modify an
   existing one.
   */
  private void askAddOrReplace() {
    if (option == UNDEFINED) {
      Alert addReplaceAlert = new Alert(AlertType.CONFIRMATION);
      addReplaceAlert.setTitle("Add or Replace");
      addReplaceAlert.setContentText("Add a new location or replace existing?");
      ButtonType addButtonType = new ButtonType(publishToOptions[0]);
      ButtonType replaceButtonType = new ButtonType(publishToOptions[1]);
      ButtonType cancelButtonType = new ButtonType(publishToOptions[2], ButtonData.CANCEL_CLOSE);
      addReplaceAlert.getButtonTypes().setAll
        (addButtonType, replaceButtonType, cancelButtonType);
      Optional<ButtonType> result = addReplaceAlert.showAndWait();
      if (result.get() == addButtonType) {
        option = ADD;
      }
      else
      if (result.get() == replaceButtonType) {
        option = REPLACE;
      } else {
        option = CANCEL;
      }
    }
  }

  /**
   The user has specified a publish to location. Either add it as a new one,
   or update the existing one, based on the user's previously expressed
   intentions.

   @param publishTo The path to the folder that the user has indicated. 
   */
  private void addOrReplaceOrRemove(String publishTo) {

    if (option == ADD) {
      savePublicationProperties(currentSelectionIndex);
      int pubsNumber = pubs.size();
      int addedIndex = addPublication(publishTo);
      if (addedIndex >= pubsNumber) {
        publishToComboBox.addItem(publishTo);
      }
      publishToComboBox.setSelectedIndex(addedIndex);
      setPublishTo(addedIndex);
      initPublicationProperties();
      lookForPublishScript(publishTo);
    }
    else
    if (option == REPLACE) {
      int selected = publishToComboBox.getSelectedIndex();
      if (selected >= 0) {
        pubs.set(publishToComboBox.getSelectedIndex(), publishTo);
        publishToComboBox.removeItemAt(selected);
        publishToComboBox.insertItemAt(publishTo, selected);
        publishToComboBox.setSelectedIndex(selected);
        setPublishScript("");
        lookForPublishScript(publishTo);
      }
    } 
    else
    if (option == REMOVE) {
      if (currentSelectionIndex >= 0) {
        publishToComboBox.removeItemAt(currentSelectionIndex);
        pubs.remove(currentSelectionIndex);
        if (publishToComboBox.getItemCount() > 0) {
          setPublishTo(0);
        }
      }
    }
    else
    if (option == CANCEL) {
      publishToComboBox.setSelectedIndex(currentSelectionIndex);
    }
    option = UNDEFINED;
  } // end method addOrReplaceOrRemove
  
  /**
   Let's check to see if we can find a publication script in the selected 
   folder. 
  
   @param publishTo The folder selected by the user for publication. 
  */
  private void lookForPublishScript (String publishTo) {
    File publishToFolder = new File(publishTo);
    if (publishToFolder.exists()
        && publishToFolder.canRead()
        && publishToFolder.isDirectory()) {
      String[] candidates = publishToFolder.list();
      boolean found = false;
      int i = 0;
      while (i < candidates.length && (! found)) {
        String candidate = candidates[i];
        if (candidate.endsWith(".tcz")) {
          found = true;
          File scriptFile = new File (publishToFolder, candidate);
          String scriptText = "";
          try {
            scriptText = scriptFile.getCanonicalPath();
          } catch (IOException ioe) {
            scriptText = scriptFile.getAbsolutePath();
          }
          publishScriptText.setText((scriptText));
        } // end if we found a tdf czar script file
        else {
          i++;
        }
      } // end looking for a script file
    } // end if we have a good folder
  } // end method lookForPublishScript
  
  /**
   Sets the statusBar to be used for identifying status. 
  
   @param statusBar A JLabel to be used for brief status messages. 
  */
  public void setStatusBar (StatusBar statusBar) {
    this.statusBar = statusBar;
  }
  
  private void announceStatus(String msg) {
    if (statusBar != null) {
      statusBar.setStatus(msg);
    }
  }
  
  public String getTitle() {
    return publishStage.getTitle();
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      publishStage.show();
    } else {
      publishStage.hide();
    }
  }
  
  public void toFront() {
    publishStage.toFront();
  }
  
  public double getWidth() {
    return publishStage.getWidth();
  }
  
  public double getHeight() {
    return publishStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    publishStage.setX(x);
    publishStage.setY(y);
  }

}
