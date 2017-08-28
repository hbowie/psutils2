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

package com.powersurgepub.psutils2.files;

	import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.env.*;
	import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.prefs.*;
	import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.text.*;
  import java.util.*;

  import javafx.beans.value.*;
	import javafx.event.*;
  import javafx.geometry.*;
	import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 
 @author Herb Bowie
 */
public class FilePrefs 
    implements
      PrefSet {
  
  public static final String BACKUP_FREQUENCY             = "backup-frequency";
  public static final String OCCASIONAL_BACKUPS           = "occasional-backups";
  public static final String MANUAL_BACKUPS               = "manual-backups";
  public static final String AUTOMATIC_BACKUPS            = "automatic-backups";
  public static final String LAST_BACKUP_DATE             = "last-backup-date";
  public static final String NO_DATE                      = "no-date";
  
  public static final String BACKUPS_TO_KEEP              = "backups-to-keep";
  
  public static final String RECENT_FILES_MAX             = "recent-files-max";
  
  public static final String LAUNCH_AT_STARTUP            = "launch-at-startup";
  public static final String NO_FILE                      = "no-file";
  public static final int    NO_FILE_INDEX                = 0;
  public static final String LAST_FILE_OPENED             = "last-file-opened";
  public static final int    LAST_FILE_OPENED_INDEX       = 1;
  public static final int    STARTUP_COMBO_BOX_LITERALS   = 2;
  
  public static final int    RECENT_FILES_MAX_DEFAULT     = 5;
  
  public static final String PURGE_INACCESSIBLE_FILES     = "purge-inaccessible-files";
  public static final String NEVER                        = "never";
  public static final int    NEVER_INDEX                  = 0;
  public static final String AT_STARTUP                   = "at-startup";
  public static final int    AT_STARTUP_INDEX             = 2;
  public static final String NOW                          = "now";
  public static final int    NOW_INDEX                    = 1;
  
  public static final DateFormat  BACKUP_DATE_FORMATTER 
      = new SimpleDateFormat ("yyyy-MM-dd-HH-mm");
  
  private             int    purgeInaccessiblePref        = NEVER_INDEX;
  
  private             long   daysBetweenBackups           = 7;
  
  private             boolean recentFilesMaxUpdateInProgress = false;
  private             boolean backupsToKeepUpdateInProgress  = false;
  
  private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
  
  /** Single shared occurrence of FilePrefs. */
  private static  FilePrefs        filePrefs = null;
  
  private         AppToBackup       appToBackup;
  
  private         RecentFiles       recentFiles = null;
  
  private         String            specificFileAtStartup = "";
  
  public static final String        ESSENTIAL_PATH        = "essential-path";
  public static final int           ESSENTIAL_COMBO_BOX_LITERALS = 1;
  private             String        essentialPath         = "";
  private             boolean       essentialUserSelection = true;
  
  private Tab         tab               = new Tab("Files");
  
  private GridPane    grid     = new GridPane();
  
  private Label       backupFrequencyLabel  = new Label("Backup Frequency:");
  private ToggleGroup backupFrequencyGroup = new ToggleGroup();
  private RadioButton manualBackupsButton = new RadioButton("Manual Only");
  private RadioButton occasionalBackupsButton = new RadioButton("Occasional Suggestions");
  private RadioButton automaticBackupsButton = new RadioButton("Automatic Backups");
  private Label       backupsToKeepLabel    = new Label("Backups to Keep:");
  private TextField   backupsToKeepTextField  = new TextField();
  private Slider      backupsToKeepSlider     = new Slider(0.0, 25.0, 5.0);
  private Label       startupLabel      = new Label("At startup, open:");
  private ComboBox<String> startupComboBox   = new ComboBox<String>();
  private Label       essentialLabel    = new Label("Assign Essential Shortcut to:");
  private ComboBox    essentialComboBox = new ComboBox();
  private Label       purgeWhenLabel = new Label("Purge inaccessible files:");
  private ComboBox    purgeWhenComboBox = new ComboBox();
  private Label       recentLabel       = new Label("Number of Recent Files:");
  private TextField   recentFilesMaxTextField   = new TextField();
  private Slider      recentFilesMaxSlider      = new Slider(0.0, 50.0, 5.0);
  private Label       msgToUser         = new Label();
  
   /**
   Returns a single instance of FilePrefs that can be shared by many classes.
   This is the only way to obtain an instance of FilePrefs, since the
   constructor is private.

  @return A single, shared instance of FilePrefs.
 */
  public static FilePrefs getShared(AppToBackup appToBackup) {
    if (filePrefs == null) {
      filePrefs = new FilePrefs(appToBackup);
    }
    return filePrefs;
  }
  
  /**
   Returns a single instance of FilePrefs that can be shared by many classes.
   This is the only way to obtain an instance of FilePrefs, since the
   constructor is private.

  @return A single, shared instance of FilePrefs.
 */
  public static FilePrefs getShared() {

    return filePrefs;
  }
  
  /** Creates new form FilePrefs */
  public FilePrefs(AppToBackup appToBackup) {
    
    this.appToBackup = appToBackup;
    
    // Set spacing between components
    grid.setPadding(new Insets(10));
    grid.setHgap(10);
    grid.setVgap(10);
   
    // First row
    grid.add(backupFrequencyLabel, 0, 0, 1, 1);
    
    manualBackupsButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        manualBackupsButtonActionPerformed();
      }
    });
    
    occasionalBackupsButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        occasionalBackupsButtonActionPerformed(evt);
      }
    });
    
    Tooltip autoTip = new Tooltip
        ("Backups will be performed automatically whenever you close the program, and at major program events. ");
    automaticBackupsButton.setTooltip(autoTip);
    automaticBackupsButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        automaticBackupsButtonActionPerformed(evt);
      }
    });
    
    backupFrequencyGroup.getToggles().addAll
      (manualBackupsButton, occasionalBackupsButton, automaticBackupsButton);
    grid.add(manualBackupsButton, 1, 0, 2, 1);
    
    // Second Row
    grid.add(occasionalBackupsButton, 1, 1, 2, 1);
    
    // Third Row
    grid.add(automaticBackupsButton, 1, 2, 2, 1);
    
    // Fourth Row
    grid.add(backupsToKeepLabel, 0, 3, 1, 1);
    backupsToKeepTextField.setAlignment(Pos.CENTER_RIGHT);
    backupsToKeepTextField.setPrefColumnCount(5);
    backupsToKeepTextField.setText("5");
    Tooltip backupsToKeepTip = new Tooltip("The number of backups to be retained");
    backupsToKeepTextField.setTooltip(backupsToKeepTip);
    backupsToKeepTextField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        backupsToKeepTextFieldActionPerformed(evt);
      }
    });
    
    backupsToKeepTextField.focusedProperty().addListener (
        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
          backupsToKeepTextFieldFocusChanged(newValue);
    });

    grid.add(backupsToKeepTextField, 1, 3, 1, 1);
    
    backupsToKeepSlider.setShowTickLabels(true);
    backupsToKeepSlider.setShowTickMarks(true);
    backupsToKeepSlider.setMajorTickUnit(5);
    backupsToKeepSlider.setMinorTickCount(4);
    backupsToKeepSlider.setBlockIncrement(1);
    backupsToKeepSlider.setSnapToTicks(true);
    backupsToKeepSlider.setValue(5);
    backupsToKeepSlider.valueProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue arg0, Object arg1, Object arg2) {
          backupsToKeepSliderStateChanged();
      }
    });

    grid.add(backupsToKeepSlider, 2, 3, 1, 1);
    GridPane.setHgrow(backupsToKeepSlider, Priority.ALWAYS);
    
    // Fifth Row
    grid.add(startupLabel, 0, 4, 1, 1);
    startupComboBox.getItems().addAll("Nothing", "Last File Opened");
    startupComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        startupComboBoxActionPerformed(evt);
      }
    });
    grid.add(startupComboBox, 1, 4, 2, 1);
    
    // Sixth Row
    grid.add(essentialLabel, 0, 5, 1, 1);
    essentialComboBox.getItems().add("Nothing");
    essentialComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        essentialComboBoxActionPerformed(evt);
      }
    });
    grid.add(essentialComboBox, 1, 5, 2, 1);
    
    // Seventh Row
    grid.add(purgeWhenLabel, 0, 6, 1, 1);
    purgeWhenComboBox.getItems().addAll("Never", "Now", "At startup");
    purgeWhenComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        purgeWhenComboBoxActionPerformed(evt);
      }
    });
    grid.add(purgeWhenComboBox, 1, 6, 2, 1);
    
    // Eighth Row
    grid.add(recentLabel, 0, 7, 1, 1);
    recentFilesMaxTextField.setAlignment(Pos.CENTER_RIGHT);
    recentFilesMaxTextField.setPrefColumnCount(5);
    recentFilesMaxTextField.setText("5");
    Tooltip recentFilesMaxTip = new Tooltip
        ("The maximum number of recent files to be retained");
    recentFilesMaxTextField.setTooltip(recentFilesMaxTip);
    recentFilesMaxTextField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        recentFilesMaxTextFieldActionPerformed(evt);
      }
    });

    recentFilesMaxTextField.focusedProperty().addListener (
        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
          recentFilesMaxTextFieldFocusChanged(newValue);
    });
    grid.add(recentFilesMaxTextField, 1, 7, 1, 1);
    
    recentFilesMaxSlider.setShowTickLabels(true);
    recentFilesMaxSlider.setShowTickMarks(true);
    recentFilesMaxSlider.setMajorTickUnit(5);
    recentFilesMaxSlider.setMinorTickCount(4);
    recentFilesMaxSlider.setBlockIncrement(1);
    recentFilesMaxSlider.setSnapToTicks(true);
    recentFilesMaxSlider.setValue(5);
    recentFilesMaxSlider.valueProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue arg0, Object arg1, Object arg2) {
          recentFilesMaxSliderStateChanged();
      }
    });
    
    grid.add(recentFilesMaxSlider, 2, 7, 1, 1);
    GridPane.setHgrow(recentFilesMaxSlider, Priority.ALWAYS);
    
    // Ninth Row
    grid.add(msgToUser, 0, 8, 3, 1);
    
    tab.setContent(grid);
    tab.setClosable(false);
    
  } // end constructor
  
  public Tab getTab() {
    return tab;
  }
  
  public void addTab(TabPane tabs) {
    tabs.getTabs().add(tab);
  }
  
  /**
   Load preference fields from stored user preferences. 
   */
  public void loadFromPrefs () {
    
    // Load frequency of backups
    String freq = UserPrefs.getShared().getPref 
        (BACKUP_FREQUENCY, OCCASIONAL_BACKUPS);
    if (freq.equalsIgnoreCase(MANUAL_BACKUPS)) {
      manualBackupsButton.setSelected(true);
    }
    else
    if (freq.equalsIgnoreCase(AUTOMATIC_BACKUPS)) {
      automaticBackupsButton.setSelected(true);
    } else {
      occasionalBackupsButton.setSelected(true);
    }
    
    // Load number of backups to keep
    int backupsToKeep = UserPrefs.getShared().getPrefAsInt 
        (BACKUPS_TO_KEEP, 0);
    backupsToKeepTextField.setText(String.valueOf(backupsToKeep));
    backupsToKeepSlider.setValue(backupsToKeep);
    
    // Load number of recent files
    int recentMax = UserPrefs.getShared().getPrefAsInt
        (RECENT_FILES_MAX, RECENT_FILES_MAX_DEFAULT);
    recentFilesMaxTextField.setText
        (String.valueOf(recentMax));
    recentFilesMaxSlider.setValue(recentMax);
    if (recentFiles != null) {
      recentFiles.setRecentFilesMax(recentMax);
    }
    
    // Load launch at startup preferences
    String launchAtStartup = UserPrefs.getShared().getPref 
        (LAUNCH_AT_STARTUP, LAST_FILE_OPENED);
    if (launchAtStartup.equalsIgnoreCase(NO_FILE)) {
      startupComboBox.getSelectionModel().select(NO_FILE_INDEX);
    }
    else
    if (launchAtStartup.equalsIgnoreCase(LAST_FILE_OPENED)) {
      startupComboBox.getSelectionModel().select(LAST_FILE_OPENED_INDEX);
    } else {
      int i = STARTUP_COMBO_BOX_LITERALS;
      boolean found = false;
      while (i < startupComboBox.getItems().size() && (! found)) {
        FileSpec comboBoxFileSpec = getStartupFileSpec (i);
        if (launchAtStartup.equalsIgnoreCase(comboBoxFileSpec.getPath())) {
          startupComboBox.getSelectionModel().select(i);
          found = true;
        } else {
          i++;
        }
      } // while looking for a match
      if (! found) {
        specificFileAtStartup = launchAtStartup;
      }
    } // end if startup value is not a literal
    
    // Load essential path preferences
    essentialPath = UserPrefs.getShared().getPref
        (ESSENTIAL_PATH, "");
    setEssentialSelection();
    
    // Load purge inaccessible preferences
    String purgeInaccessible = UserPrefs.getShared().getPref 
        (PURGE_INACCESSIBLE_FILES, NEVER);
    if (purgeInaccessible.equalsIgnoreCase(AT_STARTUP)) {
      purgeWhenComboBox.getSelectionModel().select(AT_STARTUP_INDEX);
    } else {
      purgeWhenComboBox.getSelectionModel().select(NEVER_INDEX);
    }
    purgeInaccessiblePref 
        = purgeWhenComboBox.getSelectionModel().getSelectedIndex();
  }
  
  /**
   Set the essential combo box selection to reflect the current value of 
   the essential file path. 
  */
  private void setEssentialSelection() {

    essentialUserSelection = false;
    if (essentialPath.length() == 0) {
      essentialComboBox.getSelectionModel().select(NO_FILE_INDEX);
    } else {
      int i = ESSENTIAL_COMBO_BOX_LITERALS;
      boolean found = false;
      while (i < essentialComboBox.getItems().size() && (! found)) {
        FileSpec comboBoxFileSpec = getEssentialFileSpec (i);
        if (essentialPath.equalsIgnoreCase(comboBoxFileSpec.getPath())) {
          essentialComboBox.getSelectionModel().select(i);
          found = true;
        } else {
          i++;
        }
      } // while looking for a match
    } // end if essential value is not a literal
    essentialUserSelection = true;
  }
  
  /**
   Provide access to the list of recent files.
  
   @param recentFiles A list of recent files. 
  */
  public void setRecentFiles (RecentFiles recentFiles) {
    this.recentFiles = recentFiles;
    if (recentFiles != null) {
      recentFiles.setFilePrefs(this);
      recentFiles.setRecentFilesMax((int)recentFilesMaxSlider.getValue());
    }
  }
  
  public void addRecentFileAtEnd (FileSpec recentFile) {

    essentialUserSelection = false;
    startupComboBox.getItems().add(recentFile.getBriefDisplayName());
    if (recentFile.getPath().equalsIgnoreCase(specificFileAtStartup)) {
      startupComboBox.getSelectionModel().select
        (startupComboBox.getItems().size() - 1);
    }
    
    essentialComboBox.getItems().add(recentFile.getBriefDisplayName());
    if (recentFile.getPath().equalsIgnoreCase(essentialPath)) {
      essentialComboBox.getSelectionModel().select
        (essentialComboBox.getItems().size() - 1);
    }
    essentialUserSelection = true;

  }
  
  public void addRecentFileAtTop (FileSpec recentFile) {

    essentialUserSelection = false;
    startupComboBox.getItems().add
        (STARTUP_COMBO_BOX_LITERALS, recentFile.getBriefDisplayName());
    if (recentFile.getPath().equalsIgnoreCase(specificFileAtStartup)) {
      startupComboBox.getSelectionModel().select(STARTUP_COMBO_BOX_LITERALS);
    }
    
    essentialComboBox.getItems().add
        (ESSENTIAL_COMBO_BOX_LITERALS, recentFile.getBriefDisplayName());
    if (recentFile.getPath().equalsIgnoreCase(essentialPath)) {
      essentialComboBox.getSelectionModel().select(ESSENTIAL_COMBO_BOX_LITERALS);
    } else {
      setEssentialSelection();
    }
    essentialUserSelection = true;

  }
  
  public void addNotSoRecentFile (FileSpec notSoRecentFile) {
    
    startupComboBox.getItems().add
        (STARTUP_COMBO_BOX_LITERALS + 1, notSoRecentFile.getBriefDisplayName());
    if (notSoRecentFile.getPath().equalsIgnoreCase(specificFileAtStartup)) {
      startupComboBox.getSelectionModel().select(STARTUP_COMBO_BOX_LITERALS + 1);
    }
    
    essentialUserSelection = false;
    essentialComboBox.getItems().add
        (ESSENTIAL_COMBO_BOX_LITERALS + 1, notSoRecentFile.getBriefDisplayName());
    if (notSoRecentFile.getPath().equalsIgnoreCase(essentialPath)) {
      essentialComboBox.getSelectionModel().select(ESSENTIAL_COMBO_BOX_LITERALS + 1);
    } else {
      setEssentialSelection();
    }
    essentialUserSelection = true;
    
  }
  
  public void removeRecentFile (int i) {

    if (startupComboBox.getItems().size() > (i + STARTUP_COMBO_BOX_LITERALS)) {
      startupComboBox.getItems().remove(i + STARTUP_COMBO_BOX_LITERALS);
    }
    
    essentialUserSelection = false;
    if (essentialComboBox.getItems().size() > (i + ESSENTIAL_COMBO_BOX_LITERALS)) {
      essentialComboBox.getItems().remove (i + ESSENTIAL_COMBO_BOX_LITERALS);
    }
    setEssentialSelection();
    essentialUserSelection = true;
  }
  
  /**
   Remove the oldest files, leaving only the latest. 
  */
  public void clearHistory () {

    while (startupComboBox.getItems().size() > (STARTUP_COMBO_BOX_LITERALS + 1)) {
      startupComboBox.getItems().remove(STARTUP_COMBO_BOX_LITERALS + 1);
    }
    
    essentialUserSelection = false;
    while (essentialComboBox.getItems().size() > (ESSENTIAL_COMBO_BOX_LITERALS + 1)) {
      essentialComboBox.getItems().remove(ESSENTIAL_COMBO_BOX_LITERALS + 1);
    }
    essentialUserSelection = true;
  }
  
  /**
   Set everything to the given value, if they're not already equal,
   and if the input is in an acceptable range. 
  
   @param recentFilesMax The new value to be used. 
  
   @return The resulting value after the update (if any). 
  */
  private void setRecentFilesMax (int recentFilesMax) {
    if (recentFilesMax >= 1
        && recentFilesMax <= recentFilesMaxSlider.getMax()) {
      if (recentFiles != null) {
        recentFiles.setRecentFilesMax(recentFilesMax);
      }
      if (recentFilesMax != getRecentFilesMaxFromText()) {
        recentFilesMaxTextField.setText(String.valueOf(recentFilesMax));
      }
      if (recentFilesMax != recentFilesMaxSlider.getValue()) {
        recentFilesMaxSlider.setValue(recentFilesMax);
      }
    }
  }
  
  /**
   Return an integer value extracted from the text field.
  
   @return The equivalent integer, or -1 if the text field cannot be 
           parsed into an integer. 
  */
  private int getRecentFilesMaxFromText() {
    try {
      return (Integer.parseInt(recentFilesMaxTextField.getText()));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
  
  /**
   Set everything to the given value, if they're not already equal,
   and if the input is in an acceptable range. 
  
   @param recentFilesMax The new value to be used. 
  
   @return The resulting value after the update (if any). 
  */
  private void setBackupsToKeep (int backupsToKeep) {
    if (backupsToKeep >= 0
        && backupsToKeep <= backupsToKeepSlider.getMax()) {
      if (backupsToKeep != getBackupsToKeepFromText()) {
        backupsToKeepTextField.setText(String.valueOf(backupsToKeep));
      }
      if (backupsToKeep != backupsToKeepSlider.getValue()) {
        backupsToKeepSlider.setValue(backupsToKeep);
      }
    }
  }
  
  /**
   Return an integer value extracted from the text field.
  
   @return The equivalent integer, or -1 if the text field cannot be 
           parsed into an integer. 
  */
  private int getBackupsToKeepFromText() {
    try {
      return (Integer.parseInt(backupsToKeepTextField.getText()));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
  
  /**
   Return the user's preferred file to launch automatically at startup.
  
   @return The complete path to the file.  
   */
  public String getStartupFilePath () {
    // Return startup file launch prefs
    int i = startupComboBox.getSelectionModel().getSelectedIndex();
    if (specificFileAtStartup != null
        && specificFileAtStartup.length() > 0) {
      return specificFileAtStartup;
    }
    else
    if (i == 0) {
      return "";
    }
    else
    if (i == 1) {
      if (recentFiles != null
          && recentFiles.size() > 0) {
        return recentFiles.get(0).getPath();
      } else {
        return "";
      }
    } else {
      if (recentFiles != null
          && recentFiles.size() > (i - STARTUP_COMBO_BOX_LITERALS)) {
        return recentFiles.get(i - STARTUP_COMBO_BOX_LITERALS).getPath();
      } else {
        return specificFileAtStartup;
      }
    }
  }
  
  public boolean hasEssentialFilePath() {
    // refreshEssentialPath();
    return (essentialPath != null && essentialPath.length() > 0);
  }
  
  public String getEssentialFilePath() {
    
    // refreshEssentialPath();
    return essentialPath;
  }
  
  /**
   Return a File Spec identifying the user's preferred file to launch
   automatically at startup.
  
   @return The File Spec identifying the preferred file.  
   */
  public FileSpec getStartupFileSpec () {
    return getStartupFileSpec (startupComboBox.getSelectionModel().getSelectedIndex());
  }
  
  /**
   Return the file spec for the file identified by the passed index.
  
   @param i Index to the desired startup combo box value. 
  
   @return A file spec for the file implied by the passed index.  
  */
  public FileSpec getStartupFileSpec (int i) {

    if (startupComboBox.getItems().size() <= STARTUP_COMBO_BOX_LITERALS
        && specificFileAtStartup != null
        && specificFileAtStartup.length() > 0) {
      return new FileSpec (specificFileAtStartup);
    }
    else
    if (i == 0) {
      return null;
    }
    else
    if (i == 1) {
      if (recentFiles != null
          && recentFiles.size() > 0) {
        return recentFiles.get(0);
      } else {
        return null;
      }
    } else {
      if (recentFiles != null
          && recentFiles.size() > (i - STARTUP_COMBO_BOX_LITERALS)) {
        return recentFiles.get(i - STARTUP_COMBO_BOX_LITERALS);
      } else {
        return null;
      }
    }
  }
  
  /**
   Return a File Spec identifying the user's preferred file to launch
   with the Essential shortcut.
  
   @return The File Spec identifying the essential file.  
   */
  public FileSpec getEssentialFileSpec () {
    return getEssentialFileSpec (essentialComboBox.getSelectionModel().getSelectedIndex());
  }
  
  /**
   Return the file spec for the file identified by the passed index.
  
   @param i Index to the desired essential combo box value. 
  
   @return A file spec for the file implied by the passed index.  
  */
  public FileSpec getEssentialFileSpec (int i) {

    if (essentialComboBox.getItems().size() <= ESSENTIAL_COMBO_BOX_LITERALS) {
      return null;
    }
    else
    if (i == 0) {
      return null;
    } else {
      if (recentFiles != null
          && recentFiles.size() > (i - ESSENTIAL_COMBO_BOX_LITERALS)) {
        return recentFiles.get(i - ESSENTIAL_COMBO_BOX_LITERALS);
      } else {
        return null;
      }
    }
  }
  
  /**
   Set combo box index programmatically. 
  
   @param specified index
  */
  private void setEssentialIndex(int i) {
    essentialUserSelection = false;
    essentialComboBox.getSelectionModel().select(i);
    essentialUserSelection = true;
  }
  
  public boolean purgeRecentFilesAtStartup () {
    return (purgeWhenComboBox.getSelectionModel().getSelectedIndex() == AT_STARTUP_INDEX);
  }
  
  /**
   Handle a major event that could threaten data integrity (and thus
   should prompt a backup).
  
   @return True if backup occurred. 
  */
  public boolean handleMajorEvent(
      FileSpec fileSpec, 
      String prefsQualifier, 
      int recentFileNumber) {
    
    boolean backedUp = false;
    if (fileSpec != null
        && fileSpec.hasPath()) {
    
      if (automaticBackupsButton.isSelected()) {
        backedUp = appToBackup.backupWithoutPrompt();
      }
      else
      if (occasionalBackupsButton.isSelected()) {
        backedUp = promptForBackup();
      } 
      if (backedUp) {
        saveLastBackupDate(fileSpec, prefsQualifier, recentFileNumber);
      }
    }
    return backedUp;
  }
  
  public boolean handleClose() {
    if (recentFiles != null
        && recentFiles.size() > 0) {
      return handleClose
          (recentFiles.get(0), recentFiles.getPrefsQualifier(), 0);
    } else {
      return false;
    }
  }
  
  /**
   Handle the close operation for a recent file. 
  
   @return True if backup occurred. 
  */
  public boolean handleClose(
      FileSpec fileSpec, 
      String prefsQualifier, 
      int recentFileNumber) {
    
    boolean backedUp = false;
    
    if (fileSpec != null
        && fileSpec.hasPath()) {
    
      // For automatic backups, backup with every quit
      if (automaticBackupsButton.isSelected()) {
        backedUp = appToBackup.backupWithoutPrompt();
      }
      else

      // For occasional backups, offer to backup every 7 days
      if (occasionalBackupsButton.isSelected()) {
        long daysBetween = daysBetweenBackups; 
        Calendar today =  Calendar.getInstance();
        today.setTime(new Date());
        String lastBackupDateString = fileSpec.getLastBackupDateAsString();
        if (lastBackupDateString.equals(NO_DATE)
            || lastBackupDateString.length() == 0) {
          daysBetween = daysBetweenBackups;
        } else {
          try {
            DateFormat formatter = DateFormat.getDateTimeInstance();
            Date lastBackupDate = formatter.parse(lastBackupDateString);
            Calendar last = Calendar.getInstance();
            last.setTime(lastBackupDate);
            daysBetween = 0;
            while (last.before(today)) {  
              last.add(Calendar.DAY_OF_MONTH, 1);  
              daysBetween++;  
            } 
          } catch (ParseException e) {
            System.out.println ("  Parse Exception " + e.toString());
            daysBetween = daysBetweenBackups;
          } 
        }
        if (daysBetween >= daysBetweenBackups) {
          backedUp = promptForBackup();
        }
      } 

      if (backedUp) {
        saveLastBackupDate(fileSpec, prefsQualifier, recentFileNumber);
      }
    }
    return backedUp;
  }
  
  /**
   See if the user wants to do a backup now.
  
   @return Yes if the backup ended up getting done, false otherwise.
  */
  private boolean promptForBackup() {
    
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Backup Suggestion");
    alert.setHeaderText("May we suggest a backup?");
    alert.setContentText("Choose your option.");

    ButtonType yes = new ButtonType("Yes, please");
    ButtonType no  = new ButtonType("No, thanks");

    alert.getButtonTypes().setAll(yes, no);

    Optional<ButtonType> result = alert.showAndWait();
    
    if (result.get() == yes){
      return appToBackup.promptForBackup();
    } else {
      return false;
    }
  }
  
  /**
   Get the default file name to be used for backups. 
  
   @param primaryFile The file or folder to be backed up.
  
   @param ext The intended extension for the backup file. 
  
   @return THe suggested name for the backup file. 
  */
  public String getBackupFileName(File primaryFile, String ext) {
    StringBuilder backupFileName = new StringBuilder ();
    FileName name = new FileName (primaryFile);
    int numberOfFolders = name.getNumberOfFolders();
    int i = numberOfFolders - 1;
    if (i < 0) {
      i = 0;
    }
    while (i <= numberOfFolders) {
      if (backupFileName.length() > 0) {
        backupFileName.append (' ');
      }
      backupFileName.append (name.getFolder (i));
      i++;
    }
    backupFileName.append (" backup ");
    backupFileName.append (getBackupDate());
    if (ext.length() == 0) {
      // no extension
    }
    else
    if (ext.charAt(0) == '.') {
      backupFileName.append(ext);
    } else {
      backupFileName.append (".");
      backupFileName.append(ext);
    }
    return backupFileName.toString();
  }
  
  /**
   Remove older backup files or folders. 
  
   @param backupFolder The folder containing all the backups.
   @param fileNameWithoutDate The file name, without any date. 
  
   @return The number of backups pruned. 
  */
  public int pruneBackups(File backupFolder, String fileNameWithoutDate) {
    int pruned = 0;
    int backupsToKeep = (int)backupsToKeepSlider.getValue();
    if (backupsToKeep > 0) {
      ArrayList<String> backups = new ArrayList<String>();
      String[] dirEntries = backupFolder.list();
      for (int i = 0; i < dirEntries.length; i++) {
        String dirEntryName = dirEntries[i];
        if (dirEntryName.startsWith(fileNameWithoutDate)) {
          boolean added = false;
          int j = 0;
          while ((! added) && (j < backups.size())) {
            if (dirEntryName.compareTo(backups.get(j)) > 0) {
              backups.add(j, dirEntryName);
              added = true;
            } else {
              j++;
            }
          } // end while looking for insertion point
          if (! added) {
            backups.add(dirEntryName);
          }
        } // end if file/folder name matches prefix
      } // end of directory entries
      while (backups.size() > backupsToKeep) {
        String toDelete = backups.get(backups.size() - 1);
        File toDeleteFile = new File (backupFolder, toDelete);
        if (toDeleteFile.isDirectory()) {
          FileUtils.deleteFolderContents(toDeleteFile);
        }
        toDeleteFile.delete(); 
        Logger.getShared().recordEvent(LogEvent.NORMAL, 
            "Pruning older backup: " + toDeleteFile.toString(), 
            false);
        pruned++;
        backups.remove(backups.size() - 1);
      }
    } // if we have a backups to keep number
    return pruned;
  }
  
  /**
   Return the current date and time formatted in a way that can be 
   easily appended to a file or folder name. 
  
   @return Current date and time. 
  */
  public static String getBackupDate() {
    return BACKUP_DATE_FORMATTER.format (new Date());
  }
  
  public void saveLastBackupDate(
      FileSpec fileSpec, 
      String prefsQualifier, 
      int recentFileNumber) {
    
    fileSpec.setLastBackupDateToNow();
    fileSpec.saveToRecentPrefs(prefsQualifier, recentFileNumber);
  }
  
  private void updateRecentFilesMaxTextField() {
    if (! recentFilesMaxUpdateInProgress) {
      recentFilesMaxUpdateInProgress = true;
      msgToUser.setText(" ");    
      int recentFilesMaxText = getRecentFilesMaxFromText();
      if (recentFilesMaxText >= 0) {
        if (recentFilesMaxText >= 1
            && recentFilesMaxText <= recentFilesMaxSlider.getMax()) {
          setRecentFilesMax(recentFilesMaxText);
        } else {
          recentFilesMaxTextField.setText
              (String.valueOf(recentFilesMaxSlider.getValue()));
          msgToUser.setText("Number of Recent Files cannot be outside of slider range");
        }
      } else {
        recentFilesMaxTextField.setText
            (String.valueOf(recentFilesMaxSlider.getValue()));
        msgToUser.setText("Number of Recent Files smust be numeric");
      }
      recentFilesMaxUpdateInProgress = false;
    }
  }
  
  private void updateRecentFilesMax(int recentFilesMax) {
    if (recentFiles != null) {
      recentFiles.setRecentFilesMax(recentFilesMax);
    }
  }
  
  private void updateBackupsToKeepTextField() {
    if (! backupsToKeepUpdateInProgress) {
      backupsToKeepUpdateInProgress = true;
      msgToUser.setText(" ");    
      int backupsToKeepText = getBackupsToKeepFromText();
      if (backupsToKeepText >= 0) {
        if (backupsToKeepText >= 1
            && backupsToKeepText <= backupsToKeepSlider.getMax()) {
          setBackupsToKeep(backupsToKeepText);
        } else {
          backupsToKeepTextField.setText
              (String.valueOf(backupsToKeepSlider.getValue()));
          msgToUser.setText("Number of Backups to Keep cannot be outside of slider range");
        }
      } else {
        backupsToKeepTextField.setText
            (String.valueOf(backupsToKeepSlider.getValue()));
        msgToUser.setText("Number of Backups to Keep smust be numeric");
      }
      backupsToKeepUpdateInProgress = false;
    }
  }                      

private void manualBackupsButtonActionPerformed() {                                                    
  msgToUser.setText(" ");
  save();
}                                                   

private void occasionalBackupsButtonActionPerformed(ActionEvent evt) {                                                        
  msgToUser.setText(" ");
  save();
}                                                       

private void automaticBackupsButtonActionPerformed(ActionEvent evt) {                                                       
  msgToUser.setText(" ");
  save();
}                                                      

  private void recentFilesMaxSliderStateChanged() {                                                  

    if (! recentFilesMaxUpdateInProgress) {
      recentFilesMaxUpdateInProgress = true;
      msgToUser.setText(" ");
      int recentFilesMaxSliderValue = (int)recentFilesMaxSlider.getValue();
      if (recentFilesMaxSliderValue < 1) {
        recentFilesMaxSliderValue = 1;
      }
      recentFilesMaxTextField.setText(String.valueOf(recentFilesMaxSliderValue));
      if (! recentFilesMaxSlider.isValueChanging()) {
        updateRecentFilesMax(recentFilesMaxSliderValue);
      }
      recentFilesMaxUpdateInProgress = false;
    }
  }                                                 

  private void recentFilesMaxTextFieldActionPerformed(ActionEvent evt) {                                                        
    updateRecentFilesMaxTextField();
  }                                                       

  private void recentFilesMaxTextFieldFocusChanged(boolean gainedFocus) {                                                  
    if (! gainedFocus) {
      updateRecentFilesMaxTextField();
    }
  }                                                 

  private void purgeWhenComboBoxActionPerformed(ActionEvent evt) {                                                  
    if (purgeWhenComboBox.getSelectionModel().getSelectedIndex() == NOW_INDEX) {
      if (recentFiles != null) {
        recentFiles.purgeInaccessibleFiles();
      }
      purgeWhenComboBox.getSelectionModel().select(purgeInaccessiblePref);
    } else {
      purgeInaccessiblePref = purgeWhenComboBox.getSelectionModel().getSelectedIndex();
    }
  }                                                 

  private void backupsToKeepTextFieldActionPerformed(ActionEvent evt) {                                                       
    updateBackupsToKeepTextField();
  }                                                      

  private void backupsToKeepTextFieldFocusChanged(boolean focusGained) {                                                 
    if (! focusGained) {
      updateBackupsToKeepTextField();
    }
  }                                                

  private void backupsToKeepSliderStateChanged() {                                                 
    if (! backupsToKeepUpdateInProgress) {
      backupsToKeepUpdateInProgress = true;
      msgToUser.setText(" ");
      int backupsToKeepSliderValue = (int)backupsToKeepSlider.getValue();
      if (backupsToKeepSliderValue < 0) {
        backupsToKeepSliderValue = 0;
      }
      backupsToKeepTextField.setText(String.valueOf(backupsToKeepSliderValue));
      backupsToKeepUpdateInProgress = false;
    }
  }                                                

  private void startupComboBoxActionPerformed(ActionEvent evt) {                                                
    // ???
  }                                               

  private void essentialComboBoxActionPerformed(ActionEvent evt) {                                                  

    int i = essentialComboBox.getSelectionModel().getSelectedIndex();
    if (essentialUserSelection) {
      if (i <= 0) {
        essentialPath = "";
      } else {
        if (recentFiles != null
            && recentFiles.size() > (i - ESSENTIAL_COMBO_BOX_LITERALS)) {
          essentialPath = recentFiles.get(i - ESSENTIAL_COMBO_BOX_LITERALS).getPath();
        } 
      }
    }
  }    
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "Files";
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
    // Save backup prefs
    if (manualBackupsButton.isSelected()) {
      UserPrefs.getShared().setPref(BACKUP_FREQUENCY, MANUAL_BACKUPS);
    }
    else
    if (automaticBackupsButton.isSelected()) {
      UserPrefs.getShared().setPref(BACKUP_FREQUENCY, AUTOMATIC_BACKUPS);
    } else {
      UserPrefs.getShared().setPref(BACKUP_FREQUENCY, OCCASIONAL_BACKUPS);
    }
    
    // Save backups to keep
    UserPrefs.getShared().setPref
        (BACKUPS_TO_KEEP, backupsToKeepSlider.getValue());
    
    // Save recent files max
    UserPrefs.getShared().setPref
        (RECENT_FILES_MAX, recentFilesMaxSlider.getValue());
    
    // Save startup file launch prefs
    if (startupComboBox.getSelectionModel().getSelectedIndex() == NO_FILE_INDEX) {
      UserPrefs.getShared().setPref(LAUNCH_AT_STARTUP, NO_FILE);
    }
    else
    if (startupComboBox.getSelectionModel().getSelectedIndex() == LAST_FILE_OPENED_INDEX) {
      UserPrefs.getShared().setPref(LAUNCH_AT_STARTUP, LAST_FILE_OPENED);
    } else {
      FileSpec selectedFileSpec = getStartupFileSpec();
      UserPrefs.getShared().setPref
          (LAUNCH_AT_STARTUP, selectedFileSpec.getPath());
    }
    
    // Save Essential file prefs
    UserPrefs.getShared().setPref(ESSENTIAL_PATH, essentialPath);
    
    // Save purge inaccessible files prefs
    if (purgeWhenComboBox.getSelectionModel().getSelectedIndex() == AT_STARTUP_INDEX) {
      UserPrefs.getShared().setPref(PURGE_INACCESSIBLE_FILES, AT_STARTUP);
    } else {
      UserPrefs.getShared().setPref(PURGE_INACCESSIBLE_FILES, NEVER);
    }
  }
  
  /**
   Set the File Spec whose prefs are to be modified, if any. 
  
   @param fileSpec 
  */
  public void setFileSpec(FileSpec fileSpec) {
    // Nothing to do
  }
}
