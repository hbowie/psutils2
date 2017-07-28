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

  import javafx.application.*;
  import javafx.event.*;
  import javafx.geometry.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.image.*;
  import javafx.scene.input.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 
 @author Herb Bowie
 */
public class FXUtils {
  
  private GridPane grid = null;
  
  private double hgap = 10;
  private double vgap = 10;
  private Pos    gridAlignment = Pos.TOP_LEFT;
  private String gridStyle = "-fx-padding: 10; ";
  
  private     String                labelHeadingStyle =
      "-fx-border-color: gray; " +
      "-fx-border-width: 1; " + 
      "-fx-border-style: solid; " +
      "-fx-border-insets: 1; " + 
      "-fx-padding: 5; ";
  
  private static FXUtils fxUtils;
  
  public static FXUtils getShared() {
    if (fxUtils == null) {
      fxUtils = new FXUtils();
    }
    return fxUtils;
  }
  
  public FXUtils() {
    
  }
  
  public void setHGap(double hgap) {
    this.hgap = hgap;
  }
  
  public void setVGap(double vgap) {
    this.vgap = vgap;
  }
  
  public void setGridAlignment(Pos gridAlignment) {
    this.gridAlignment = gridAlignment;
  }
  
  public void setGridStyle(String gridStyle) {
    this.gridStyle = gridStyle;
  }
  
  public void setLabelHeadingStyle(String labelHeadingStyle) {
    this.labelHeadingStyle = labelHeadingStyle;
  }
  
  public void applyStyle(GridPane grid) {
    this.grid = grid;
    grid.setHgap(hgap);
    grid.setVgap(vgap);
    grid.setAlignment(gridAlignment);
    grid.setStyle(gridStyle);
  }
  
  public void addLabelHeading(Label label, int column, int row) {
    label.setStyle(labelHeadingStyle);
    label.setMaxWidth(Double.MAX_VALUE);
    grid.add(label, column, row, 1, 1);
    GridPane.setHgrow(label, Priority.SOMETIMES);
  }
  
  public void applyHeadingStyle(Label label) {
    label.setStyle(labelHeadingStyle);
  }

}
