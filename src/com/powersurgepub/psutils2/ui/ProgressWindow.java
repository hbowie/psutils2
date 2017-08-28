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

package com.powersurgepub.psutils2.ui;

  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.strings.*;

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
 A window to show progress. 

 @author Herb Bowie
 */
public class ProgressWindow 
    implements 
        WindowToManage {
  
  private Stage           primaryStage = null;
  
  private WindowToManage  windowToManage;
  
  private Stage           progressStage;
  private Scene           progressScene;
  private BorderPane      progressPane;
  private ProgressBar     progressBar;
  
  public ProgressWindow(Stage primaryStage, String title) {
    this.primaryStage = primaryStage;
    windowToManage = this;
    buildUI(title);
  }
  
  private void buildUI(String title) {

    progressStage = new Stage(StageStyle.UTILITY);
    progressStage.setTitle(title);
    progressStage.initOwner(primaryStage);
    
    progressStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    }); 
    
    progressStage.setOnHidden(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    });
    
    progressPane = new BorderPane();

    progressBar = new ProgressBar(0);
    
    progressPane.setCenter(progressBar);

    progressScene = new Scene(progressPane);
    
    progressStage.setScene(progressScene);
  }
  
  public void setProgress(int completed, int total) {
    setProgress(completed/total);
  }
  
  public void setProgress (double progress) {
    progressBar.setProgress(progress);
  }
  
  public String getTitle() {
    return progressStage.getTitle();
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      progressStage.show();
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
