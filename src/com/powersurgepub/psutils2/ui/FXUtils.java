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
  
  private double labelMaxWidth = 120;
  private double labelMaxHeight = 25;
  private double labelMinWidth = 100;
  private double labelMinHeight = 19;
  private double labelPrefWidth = 100;
  private double labelPrefHeight = 19;
  
  private double buttonMaxWidth = 150;
  private double buttonMaxHeight = 35;
  private double buttonMinWidth = 130;
  private double buttonMinHeight = 29;
  private double buttonPrefWidth = 130;
  private double buttonPrefHeight = 29;
  
  private double comboBoxMaxWidth = Double.MAX_VALUE;
  private double comboBoxMaxHeight = Double.MAX_VALUE;
  private double comboBoxMinWidth = 169;
  private double comboBoxMinHeight = 27;
  private double comboBoxPrefWidth = 169;
  private double comboBoxPrefHeight = 27;
  
  private     String                labelHeadingStyle =
      "-fx-border-color: gray; " +
      "-fx-border-width: 1; " + 
      "-fx-border-style: solid; " +
      "-fx-border-insets: 1; " + 
      "-fx-padding: 5; ";
  
  private     String                separatorStyle = 
      "-fx-text-fill: gray; ";
  
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
  
  public void standardize (Label label) {
    label.setMaxSize(labelMaxWidth, labelMaxHeight);
    label.setMinSize(labelMinWidth, labelMinHeight);
    label.setPrefSize(labelPrefWidth, labelPrefHeight);
  }
  
  public void standardize (Button button) {
    button.setMaxSize(buttonMaxWidth, buttonMaxHeight);
    button.setMinSize(buttonMinWidth, buttonMinHeight);
    button.setPrefSize(buttonPrefWidth, buttonPrefHeight);
  }
  
  public void standardize (ComboBox cb) {
    cb.setMaxSize(comboBoxMaxWidth, comboBoxMaxHeight);
    cb.setMinSize(comboBoxMinWidth, comboBoxMinHeight);
    cb.setPrefSize(comboBoxPrefWidth, comboBoxPrefHeight);
  }
  
  /**
   Build a menu item that can be used as a separator and add it to the 
   passed menu. 
  
   @param menu The menu to have a separator added to it. 
  */
  public void addSeparator(Menu menu) {
    MenuItem sep = new MenuItem("------------");
    sep.setStyle(separatorStyle);
    menu.getItems().add(sep);
  }
  
  public void displayControlAttributes(Control control, String name) {
    System.out.println("FXUtils.displayControlAttributes for " + name);
    System.out.println("  Min Width   = " + String.valueOf(control.getMinWidth()));
    System.out.println("  Min Height  = " + String.valueOf(control.getMinHeight()));
    System.out.println("  Pref Width  = " + String.valueOf(control.getPrefWidth()));
    System.out.println("  Pref Height = " + String.valueOf(control.getPrefHeight()));
    System.out.println("  Max Width   = " + String.valueOf(control.getMaxWidth()));
    System.out.println("  Max Height  = " + String.valueOf(control.getMaxHeight()));
    System.out.println("  Width       = " + String.valueOf(control.getWidth()));
    System.out.println("  Height      = " + String.valueOf(control.getHeight()));
  }

}
