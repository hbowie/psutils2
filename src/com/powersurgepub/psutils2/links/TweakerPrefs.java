/*
 * Copyright 2012 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.links;

  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.prefs.*;
	import com.powersurgepub.psutils2.ui.*;

	import javafx.scene.control.*;
  import javafx.scene.layout.*;

/**
 
 @author Herb Bowie
 */
public class TweakerPrefs 
    implements
      PrefSet {
  
  public static final String REDIRECT_URL               = "redirect-url";
  
  private     FXUtils             fxUtils;
  private     GridPane            tweakerPrefsPane;
  private     Label               redirectLabel;
  private     TextArea            redirectTextArea;
  
  public TweakerPrefs() {
    buildUI();
    redirectTextArea.setText(UserPrefs.getShared().getPref(REDIRECT_URL, ""));
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		tweakerPrefsPane = new GridPane();
		fxUtils.applyStyle(tweakerPrefsPane);

		redirectLabel = new Label("Redirect URL:");
		tweakerPrefsPane.add(redirectLabel, 0, rowCount, 1, 1);

		redirectTextArea = new TextArea();
		tweakerPrefsPane.add(redirectTextArea, 1, rowCount, 1, 1);
		redirectTextArea.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(redirectTextArea, Priority.ALWAYS);
		redirectTextArea.setMaxHeight(Double.MAX_VALUE);
		redirectTextArea.setPrefRowCount(100);
		GridPane.setVgrow(redirectTextArea, Priority.ALWAYS);
		redirectTextArea.setPrefRowCount(100);
		redirectTextArea.setWrapText(true);

		rowCount++;
  } // end method buildUI

  public String getRedirectURL () {
    return redirectTextArea.getText();
  }

  public void savePrefs() {
    UserPrefs.getShared().setPref(REDIRECT_URL, redirectTextArea.getText());
  }
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "Link Tweaker";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return tweakerPrefsPane;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    savePrefs();
  }

}
