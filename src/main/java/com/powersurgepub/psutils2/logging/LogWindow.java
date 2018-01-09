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

package com.powersurgepub.psutils2.logging;

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.ui.*;

  import javafx.stage.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;

/**
 A window for displaying a log of events using JavaFX. 

 @author Herb Bowie
 */
public class LogWindow 
    extends LogOutput
      implements 
        WindowToManage {
  
  public static final String    WINDOW_TITLE = "Log";
  
  private             Stage     logStage;
  
  private             TextArea  logTextArea;
  
  public LogWindow(Stage primaryStage) {
    
    logStage = new Stage();
    logStage.setTitle(WINDOW_TITLE);
    logStage.initOwner(primaryStage);
    
    logTextArea = new TextArea();
    logTextArea.setWrapText(true);
    logTextArea.setEditable(false);
    
    StackPane logPane = new StackPane();
    logPane.getChildren().add(logTextArea);
    
    Scene logScene = new Scene(logPane, 600, 400);
    
    logStage.setScene(logScene);
  }
  
  public void writeLine (String line) {
    logTextArea.appendText(line + GlobalConstants.LINE_FEED_STRING);
  } // end writeLine method
  
  public String getTitle() {
    return WINDOW_TITLE;
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      logStage.show();
    } else {
      logStage.close();
    }
  }
  
  public void toFront() {
    logStage.show();
    logStage.toFront();
  }
  
  public double getWidth() {
    return logStage.getWidth();
    
  }
  
  public double getHeight() {
    return logStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    logStage.setX(x);
    logStage.setY(y);
  }

}
