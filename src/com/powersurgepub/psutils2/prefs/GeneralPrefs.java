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

package com.powersurgepub.psutils2.prefs;

	import com.powersurgepub.psutils2.env.*;
	import com.powersurgepub.psutils2.ui.*;

 	import javafx.event.*;
 	import javafx.geometry.*;
 	import javafx.scene.control.*;
 	import javafx.scene.control.Alert.*;
 	import javafx.scene.layout.*;
	import javafx.scene.text.*;
 	import javafx.stage.*;

/**
 General user preferences applicable to most applications. 

 @author Herb Bowie
 */
public class GeneralPrefs 
    implements
      PrefSet {
  
  public static final String SPLIT_HORIZONTAL             = "splithorizontal";
  public static final String DIVIDER_LOCATION             = "divider-location";
  public static final String CONFIRM_DELETES              = "confirm-deletes";
  public static final String CHECK_FOR_SOFTWARE_UPDATES   = "check-updates";
  public static final String CHECK_VERSION_AUTO           = "versioncheckauto";
  
  public static final String PREFS_LEFT    = "left";
  public static final String PREFS_TOP     = "top";
  public static final String PREFS_WIDTH   = "width";
  public static final String PREFS_HEIGHT  = "height";

  /** Single shared occurrence of GeneralPrefs. */
  private static  GeneralPrefs       generalPrefs = null;

  private         ProgramVersion    programVersion = ProgramVersion.getShared();
  private         SplitPane         splitPane = null;
  
  private     Window              mainWindow = null;
  
  private     FXUtils             fxUtils;
  private     GridPane            generalPrefsPane;
  private     Label               splitPaneLabel;
  private     CheckBox            splitPaneCheckBox;
  private     Label               confirmDeletesLabel;
  private     CheckBox            confirmDeletesCheckBox;
  private     Label               softwareUpdatesLabel;
  private     CheckBox            softwareUpdatesCheckBox;
  private     Button              softwareUpdatesCheckNowButton;
  
  /**
   Returns a single instance of CommonPrefs that can be shared by many classes.
   This is the only way to obtain an instance of CommonPrefs, since the
   constructor is private.

  @return A single, shared instance of CommonPrefs.
 */
  public static GeneralPrefs getShared() {
    if (generalPrefs == null) {
      generalPrefs = new GeneralPrefs();
    }
    return generalPrefs;
  }

  /** Creates new form CommonPrefs */
  public GeneralPrefs() {
    
    buildUI();

    splitPaneCheckBox.setSelected
        (UserPrefs.getShared().getPrefAsBoolean (SPLIT_HORIZONTAL, false));

    confirmDeletesCheckBox.setSelected
        (UserPrefs.getShared().getPrefAsBoolean (CONFIRM_DELETES, true));
    
    boolean oldValue
      = UserPrefs.getShared().getPrefAsBoolean
        (CHECK_VERSION_AUTO, true);
    softwareUpdatesCheckBox.setSelected
      (UserPrefs.getShared().getPrefAsBoolean
        (CHECK_FOR_SOFTWARE_UPDATES, oldValue));
  }
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		generalPrefsPane = new GridPane();
    fxUtils.setGridAlignment(Pos.TOP_CENTER);
		fxUtils.applyStyle(generalPrefsPane);

		splitPaneLabel = new Label("Split Pane:");
    splitPaneLabel.setAlignment(Pos.BASELINE_RIGHT);
		generalPrefsPane.add(splitPaneLabel, 0, rowCount, 1, 1);
		splitPaneLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(splitPaneLabel, Priority.SOMETIMES);
    GridPane.setHalignment(splitPaneLabel, HPos.RIGHT);

		splitPaneCheckBox = new CheckBox("Horizontal Split?");
    splitPaneCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
          (SPLIT_HORIZONTAL, splitPaneCheckBox.isSelected());
        setSplit(splitPaneCheckBox.isSelected());
		  } // end handle method
		}); // end event handler
		generalPrefsPane.add(splitPaneCheckBox, 1, rowCount, 1, 1);
		splitPaneCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(splitPaneCheckBox, Priority.SOMETIMES);

		rowCount++;

		confirmDeletesLabel = new Label("Deletion:");
    confirmDeletesLabel.setAlignment(Pos.BASELINE_RIGHT);
		generalPrefsPane.add(confirmDeletesLabel, 0, rowCount, 1, 1);
		confirmDeletesLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(confirmDeletesLabel, Priority.SOMETIMES);

		confirmDeletesCheckBox = new CheckBox("Confirm Deletes?");
    confirmDeletesCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
          (CONFIRM_DELETES, confirmDeletesCheckBox.isSelected());
		  } // end handle method
		}); // end event handler
		generalPrefsPane.add(confirmDeletesCheckBox, 1, rowCount, 1, 1);
		confirmDeletesCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(confirmDeletesCheckBox, Priority.SOMETIMES);

		rowCount++;

		softwareUpdatesLabel = new Label("Software Updates:");
    softwareUpdatesLabel.setAlignment(Pos.BASELINE_RIGHT);
		generalPrefsPane.add(softwareUpdatesLabel, 0, rowCount, 1, 1);
		softwareUpdatesLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(softwareUpdatesLabel, Priority.SOMETIMES);

		softwareUpdatesCheckBox = new CheckBox("Check Automatically?");
    softwareUpdatesCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
          (CHECK_FOR_SOFTWARE_UPDATES, softwareUpdatesCheckBox.isSelected());
		  } // end handle method
		}); // end event handler
		generalPrefsPane.add(softwareUpdatesCheckBox, 1, rowCount, 1, 1);
		softwareUpdatesCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(softwareUpdatesCheckBox, Priority.SOMETIMES);

		rowCount++;

		softwareUpdatesCheckNowButton = new Button("Check Now");
    softwareUpdatesCheckNowButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        programVersion.informUserIfNewer();
        programVersion.informUserIfLatest();
		  } // end handle method
		}); // end event handler
		generalPrefsPane.add(softwareUpdatesCheckNowButton, 1, rowCount, 1, 1);

		rowCount++;
  } // end method buildUI

  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "General";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return generalPrefsPane;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    if (splitPane != null) {
      UserPrefs.getShared().setPref
          (DIVIDER_LOCATION, splitPane.getDividerPositions()[0]);
    }
  }
  
  public void appLaunch() {
    checkVersionIfAuto();
  }

  public void checkVersionIfAuto () {
    if (programVersion == null) {
      System.out.println("programVersion == null");
    }
    if (checkForSoftwareUpdates()) {
      programVersion.informUserIfNewer();
    }
  }

  public boolean confirmDeletes () {
    return confirmDeletesCheckBox.isSelected();
  }

  public boolean checkForSoftwareUpdates () {
    return softwareUpdatesCheckBox.isSelected();
  }

  public void setSplitPane(SplitPane splitPane) {
    this.splitPane = splitPane;
    setSplit(splitPaneCheckBox.isSelected());
    splitPane.setDividerPosition
        (0, UserPrefs.getShared().getPrefAsInt (DIVIDER_LOCATION, 240));
  }

  public boolean splitPaneHorizontal () {
    return splitPaneCheckBox.isSelected();
  }

  public void setSplit (boolean splitPaneHorizontal) {
    Orientation splitOrientation = Orientation.VERTICAL;
    if (splitPaneHorizontal) {
      splitOrientation = Orientation.HORIZONTAL;
    }
    if (splitPane != null) {
      splitPane.setOrientation (splitOrientation);
    }
  }
  
  /**
   Provides the main window for the application.

   @param mainWindow The main window for the application. Normally a JFrame, but
   only required to be a Component. 
   */
  public void setMainWindow (Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  private void warnRelaunch() {
    
    Alert alert = new Alert(AlertType.WARNING);
    alert.initOwner(mainWindow);
    alert.setTitle("Relaunch Warning");
    alert.setContentText("You may need to Quit and relaunch "
            + Home.getShared().getProgramName()
            + " for your preferences to take effect.");
    alert.showAndWait();
  }

}
