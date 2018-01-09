/*
 * Copyright 2017 Herb Bowie
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

  import java.io.*;
  import java.net.*;

 	import javafx.event.*;
 	import javafx.geometry.*;
 	import javafx.scene.*;
 	import javafx.scene.control.*;
 	import javafx.scene.control.Alert.*;
 	import javafx.scene.layout.*;
  import javafx.scene.web.*;
 	import javafx.stage.*;

/**
 A window to show progress of URL Validation. 

 @author Herb Bowie
 */
public class URLValidationProgressWindow 
    implements 
        WindowToManage {
  
  private AppWithLinksToValidate app;
  
  private Stage           primaryStage = null;
  
  private WindowToManage  windowToManage;
  
  public static final String LINKS_TO_CHECK = "Links to Check: ";
  public static final String LINKS_CHECKED  = "Links Checked: ";
  public static final String BAD_LINKS = "Bad Links: ";
  public static final String LINKS_REMAINING = "Remaining to Check: ";
  
  private int             linksToCheck = 0;
  private int             linksChecked = 0;
  private int             badLinks = 0;
  private int             linksRemaining = 0;
  
  private FXUtils         fxUtils;
  private Stage           progressStage;
  private Scene           progressScene;
  private GridPane        progressPane;
  private Label           checkInternetLabel;
  private ProgressBar     progressBar;
  private Label           linksToCheckLabel;
  private Label           linksCheckedLabel;
  private Label           badLinksLabel;
  private Label           linksRemainingLabel;
  private Button          cancelButton;
  private Button          startButton;
  
  public URLValidationProgressWindow(Stage primaryStage, 
      String title,
      AppWithLinksToValidate app) {
    this.primaryStage = primaryStage;
    this.app = app;
    windowToManage = this;
    buildUI(title);
  }
  
  private void buildUI(String title) {

    fxUtils = FXUtils.getShared();
    
    progressStage = new Stage(StageStyle.UTILITY);
    progressStage.setTitle(title);
    progressStage.initModality(Modality.APPLICATION_MODAL);
    progressStage.initOwner(primaryStage);
    
    progressStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        app.stopLinkValidation();
      }
    }); 
    
    progressStage.setOnHidden(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        app.stopLinkValidation();
      }
    });
    
    progressPane = new GridPane();
    fxUtils.applyStyle(progressPane);
    
    checkInternetLabel = new Label("Make Sure Your Internet Connection is Active");
    progressPane.add(checkInternetLabel, 0, 0, 2, 1);

    progressBar = new ProgressBar(0);
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressPane.add(progressBar, 0, 1, 2, 1);
    GridPane.setHgrow(progressBar, Priority.ALWAYS);
    
    linksToCheckLabel = new Label();
    progressPane.add(linksToCheckLabel, 0, 2, 2, 1);
    
    linksCheckedLabel = new Label();
    progressPane.add(linksCheckedLabel, 0, 3, 2, 1);
    
    badLinksLabel = new Label();
    progressPane.add(badLinksLabel, 0, 4, 2, 1);
    
    linksRemainingLabel = new Label();
    progressPane.add(linksRemainingLabel, 0, 5, 2, 1);
    
    startButton = new Button("Start");
    startButton.setOnAction(e -> app.startLinkValidation());
    progressPane.add(startButton, 0, 6, 1, 1);
    
    cancelButton = new Button("Cancel");
    cancelButton.setOnAction(e -> app.stopLinkValidation());
    cancelButton.setDisable(true);
    progressPane.add(cancelButton, 1, 6, 1, 1);

    progressScene = new Scene(progressPane, 300, 300);
    
    progressStage.setScene(progressScene);
    
    setLinksToCheck(0);
    setLinksChecked(0);
    setBadLinks(0);
  }
  
  public void validationStarting() {
    startButton.setDisable(true);
    cancelButton.setDisable(false);
  }
  
  public void setLinksToCheck(int linksToCheck) {
    this.linksToCheck = linksToCheck;
    linksToCheckLabel.setText(LINKS_TO_CHECK + String.valueOf(linksToCheck));
    setLinksRemaining(linksToCheck - linksChecked);
    setProgress(linksChecked, linksToCheck);
  }
  
  public void setLinksChecked(int linksChecked) {
    this.linksChecked = linksChecked;
    linksCheckedLabel.setText(LINKS_CHECKED + String.valueOf(linksChecked));
    setLinksRemaining(linksToCheck - linksChecked);
    setProgress(linksChecked, linksToCheck);
    if (allDone()) {
      cancelButton.setText("Close");
    }
  }
  
  public boolean allDone() {
    return (linksChecked >= linksToCheck);
  }
  
  public void setBadLinks(int badLinks) {
    this.badLinks = badLinks;
    badLinksLabel.setText(BAD_LINKS + String.valueOf(badLinks));
  }
  
  public void setLinksRemaining(int linksRemaining) {
    this.linksRemaining = linksRemaining;
    linksRemainingLabel.setText(LINKS_REMAINING + String.valueOf(linksRemaining));
  }
  
  public void setProgress(int completed, int total) {
    if (total == 0 && completed > 0) {
      setProgress(1.0);
    }
    else
    if (total == 0 && completed == 0) {
      setProgress(0.0);
    } else {
      setProgress(completed/total);
    }
  }
  
  public void setProgress (double progress) {
    progressBar.setProgress(progress);
  }
  
  public String getTitle() {
    return progressStage.getTitle();
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      progressStage.showAndWait();
    } else {
      progressStage.hide();
    }
  }
  
  public void toFront() {
    progressStage.toFront();
  }
  
  public double getWidth() {
    return progressStage.getWidth();
  }
  
  public double getHeight() {
    return progressStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    progressStage.setX(x);
    progressStage.setY(y);
  }

}
