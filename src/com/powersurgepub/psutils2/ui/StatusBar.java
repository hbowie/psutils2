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

package com.powersurgepub.psutils2.ui;

  import com.powersurgepub.psutils2.files.*;
  import java.text.*;
  import java.util.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;

/**
 A Status Bar to be placed at the bottom of a window.

 @author Herb Bowie
 */
public class StatusBar {
  
  private HBox statusBar;
  private Label fileNameLabel;
  private Label dividerLabel1;
  private Label indexLabel;
  private Label ofLabel;
  private Label sizeLabel;
  private Label dividerLabel2;
  private Label statusLabel;

  private DateFormat    longDateFormatter
      = new SimpleDateFormat ("EEEE MMMM d, yyyy");

  /** Creates new form StatusBar */
  public StatusBar() {
    statusBar = new HBox(0);
    
    dividerLabel1 = new Label(" | ");
    dividerLabel2 = new Label(" | ");
    fileNameLabel = new Label(" ");
    indexLabel = new Label("1");
    ofLabel = new Label(" of ");
    sizeLabel = new Label("1");
    statusLabel = new Label(" ");
    
    statusBar.getChildren().addAll(fileNameLabel, dividerLabel1, indexLabel, 
        ofLabel, sizeLabel, dividerLabel2, statusLabel);
    
    HBox.setHgrow(fileNameLabel, Priority.ALWAYS);
    HBox.setHgrow(statusLabel, Priority.ALWAYS);
  }
  
  public Pane getPane() {
    return statusBar;
  }
  
  public void setFileName (FileName fileName) {
    setFileName (fileName.getToFit(30), fileName.toString());
  }
  
  public void setFileName (String shortName, String longName) {
    fileNameLabel.setText (shortName);
    fileNameLabel.setTooltip(new Tooltip(longName));
  }
  
  public void setPosition(int position, int size) {
    indexLabel.setText(String.valueOf(position));
    ofLabel.setText(" of ");
    sizeLabel.setText(String.valueOf(size));
  }
  
  public void setStatus (String briefStatus) {
    statusLabel.setText(briefStatus);
  }                    
               
}
