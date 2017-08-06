/*
 * Copyright 2012 - 2017 Herb Bowie
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
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.ui.*;

  import javafx.event.*;
  import javafx.geometry.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.scene.text.*;
  import javafx.stage.*;

/**
 A user interface, along with accompanying functionality, allowing a user to 
 "tweak" a hyperlink (aka URL). 

 @author Herb Bowie
 */
public class LinkTweaker 
    implements
      LinkTweakerInterface, 
      WindowToManage {
  
  private             LinkTweakerApp      linkTweakerApp = null;
  
  public static final String PROGRAM_NAME = "LinkTweaker";
  public static final String PROGRAM_VERSION = "1.30";
  
  public static final boolean PREFS_AVAILABLE = true;
  
  public static final String DEFAULT_LINK_ID = "Link";

  private     String              linkID = DEFAULT_LINK_ID;
  
  private boolean runningAsMainApp = true;
  
  private     static  final double   DEFAULT_WIDTH = 620;
  private     static  final double   DEFAULT_HEIGHT = 540;
  
  private                   double   defaultX = 0;
  private                   double   defaultY = 0;
  
  // private             Appster appster;

  private             String  country = "  ";
  private             String  language = "  ";

  private             Home                home;
  private             ProgramVersion      programVersion;
  private             Trouble             trouble = Trouble.getShared();
  
  private             TweakerPrefs        tweakerPrefs;
  
  private             FXUtils             fxUtils;
  private             Stage               primaryStage = null;
  private             Stage               tweakerStage;
  private             Scene               tweakerScene;
  private             GridPane            tweakerPane;
  private             Label               inputLabel;
  private             TextArea            inputText;
  private             Label               outputLabel;
  private             TextArea            outputText;
  private             Button              getButton;
  private             Button              tweakButton;
  private             CheckBox            spCruftCheckBox;
  private             Button              launchButton;
  private             CheckBox            redirectCheckBox;
  private             Button              copyButton;
  private             CheckBox            spacesCheckBox;
  private             Button              putButton;
  private             Label               msgLabel;

  
  public LinkTweaker(
      LinkTweakerApp linkTweakerApp, 
      TweakerPrefs tweakerPrefs,
      Stage primaryStage) {
    this.linkTweakerApp = linkTweakerApp;
    this.tweakerPrefs = tweakerPrefs;
    this.primaryStage = primaryStage;
    buildUI();
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		tweakerStage = new Stage();
    tweakerStage.setTitle(getTitle());
    tweakerStage.initOwner(primaryStage);

		tweakerPane = new GridPane();
		fxUtils.applyStyle(tweakerPane);

		inputLabel = new Label("Input Link:");
		inputLabel.setTextAlignment(TextAlignment.CENTER);
		tweakerPane.add(inputLabel, 0, rowCount, 2, 1);
		inputLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(inputLabel, Priority.ALWAYS);

		rowCount++;

		inputText = new TextArea();
		tweakerPane.add(inputText, 0, rowCount, 2, 1);
		inputText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(inputText, Priority.ALWAYS);
		inputText.setMaxHeight(Double.MAX_VALUE);
		inputText.setPrefRowCount(100);
		GridPane.setVgrow(inputText, Priority.SOMETIMES);
		inputText.setPrefRowCount(100);
		inputText.setWrapText(true);

		rowCount++;

		outputLabel = new Label("Output Link:");
		outputLabel.setTextAlignment(TextAlignment.CENTER);
		tweakerPane.add(outputLabel, 0, rowCount, 2, 1);
		outputLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(outputLabel, Priority.ALWAYS);

		rowCount++;

		outputText = new TextArea();
		tweakerPane.add(outputText, 0, rowCount, 2, 1);
		outputText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(outputText, Priority.ALWAYS);
		outputText.setMaxHeight(Double.MAX_VALUE);
		outputText.setPrefRowCount(100);
		GridPane.setVgrow(outputText, Priority.SOMETIMES);
		outputText.setPrefRowCount(100);
		outputText.setWrapText(true);

		rowCount++;

		getButton = new Button("Get");
		getButton.setTextAlignment(TextAlignment.CENTER);
    getButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (linkTweakerApp != null) {
          inputText.setText(linkTweakerApp.getLinkToTweak());
        }
		  } // end handle method
		}); // end event handler
		tweakerPane.add(getButton, 0, rowCount, 1, 1);
		getButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(getButton, Priority.SOMETIMES);

		rowCount++;

		tweakButton = new Button("Tweak");
		tweakButton.setTextAlignment(TextAlignment.CENTER);
    tweakButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        tweakThisLink();
		  } // end handle method
		}); // end event handler
		tweakerPane.add(tweakButton, 0, rowCount, 1, 1);
		tweakButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(tweakButton, Priority.SOMETIMES);

		spCruftCheckBox = new CheckBox("Remove SharePoint Cruft?");
    spCruftCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        tweakThisLink();
		  } // end handle method
		}); // end event handler
		tweakerPane.add(spCruftCheckBox, 1, rowCount, 1, 1);
		spCruftCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(spCruftCheckBox, Priority.SOMETIMES);

		rowCount++;

		launchButton = new Button("Launch");
		launchButton.setTextAlignment(TextAlignment.CENTER);
    launchButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        tweakThisLink();
        boolean ok = Home.getShared().openURL(outputText.getText());
        if (! ok) {
          msgLabel.setText("Error in launching link in Web browser");
        } else {
          msgLabel.setText(" ");
        }
		  } // end handle method
		}); // end event handler
		tweakerPane.add(launchButton, 0, rowCount, 1, 1);
		launchButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(launchButton, Priority.SOMETIMES);

		redirectCheckBox = new CheckBox("Insert Redirect?");
    redirectCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        tweakThisLink();
		  } // end handle method
		}); // end event handler
		tweakerPane.add(redirectCheckBox, 1, rowCount, 1, 1);
		redirectCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(redirectCheckBox, Priority.SOMETIMES);

		rowCount++;

		copyButton = new Button("Copy");
		copyButton.setTextAlignment(TextAlignment.CENTER);
    copyButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        tweakThisLink();
        outputText.selectAll();
        outputText.copy();
		  } // end handle method
		}); // end event handler
		tweakerPane.add(copyButton, 0, rowCount, 1, 1);
		copyButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(copyButton, Priority.SOMETIMES);

		spacesCheckBox = new CheckBox("Show spaces as spaces?");
    spacesCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        tweakThisLink();
		  } // end handle method
		}); // end event handler
		tweakerPane.add(spacesCheckBox, 1, rowCount, 1, 1);
		spacesCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(spacesCheckBox, Priority.SOMETIMES);

		rowCount++;

		putButton = new Button("Put");
		putButton.setTextAlignment(TextAlignment.CENTER);
    putButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (linkTweakerApp != null) {
          linkTweakerApp.putTweakedLink(outputText.getText(), linkID);
          tweakerStage.hide();
        }
		  } // end handle method
		}); // end event handler
		tweakerPane.add(putButton, 0, rowCount, 1, 1);
		putButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(putButton, Priority.SOMETIMES);

		rowCount++;

		msgLabel = new Label();
		tweakerPane.add(msgLabel, 0, rowCount, 2, 1);
		msgLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(msgLabel, Priority.ALWAYS);

		rowCount++;
    
    tweakerScene = new Scene(tweakerPane, 300, 250);
    
    tweakerStage.setScene(tweakerScene);
  } // end method buildUI


  
  private void setDefaultScreenSizeAndLocation() {

    tweakerStage.setWidth(DEFAULT_WIDTH);
    tweakerStage.setHeight(DEFAULT_HEIGHT);
    tweakerStage.setResizable(true);
		calcDefaultScreenLocation();
    tweakerStage.setX(defaultX);
    tweakerStage.setY(defaultY);
  }
  
  private void calcDefaultScreenLocation() {
    Screen mainScreen = Screen.getPrimary();
    Rectangle2D mainBounds = mainScreen.getVisualBounds();
    defaultX = (mainBounds.getWidth() - tweakerStage.getWidth()) / 2;
    defaultY = (mainBounds.getHeight() - tweakerStage.getHeight()) / 2;
  }
  
  /**
   Set the input link to the passed value, when using this class as an
   auxiliary window to a main program. 
  
   @param passedLink The link to be tweaked. 
  */
  public void setLink(String passedLink) {
    inputText.setText(passedLink);
    outputText.setText(passedLink);
    linkID = "Link";
  }
  
  public void setLink(String passedLink, String linkID) {
    inputText.setText(passedLink);
    outputText.setText(passedLink);
    this.linkID = linkID;
  }
  
  /**
   Let's straighten out the URL submitted by the user. 
  */
  private void tweakThisLink() {
    
    String tweakedLink = StringUtils.tweakAnyLink (
        inputText.getText(),
        spacesCheckBox.isSelected(),
        spCruftCheckBox.isSelected(),
        redirectCheckBox.isSelected(),
        tweakerPrefs.getRedirectURL());
    
    outputText.setText(tweakedLink);
    msgLabel.setText(" ");
  }

  private void savePrefs() {
    tweakerPrefs.savePrefs();
  }
  
  /**
   Get the title for pane. 
  
   @return The title for this pane. 
  */
  public String getTitle() {
    return "Link Tweaker";
  }
  
  /**
   Get a JavaFX Pane presenting all the UI controls to the user. 
  
   @return  The JavaFX Pane containing Controls allowing the user to tweak 
            a hyperlink. . 
  */
  public Pane getPane() {
    return tweakerPane;
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      tweakerStage.show();
    } else {
      tweakerStage.close();
    }
  }
  
  public void toFront() {
    tweakerStage.show();
    tweakerStage.toFront();
  }
  
  public double getWidth() {
    return tweakerStage.getWidth();
    
  }
  
  public double getHeight() {
    return tweakerStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    tweakerStage.setX(x);
    tweakerStage.setY(y);
  }
  
}
