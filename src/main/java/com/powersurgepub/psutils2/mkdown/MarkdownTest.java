/*
 * Copyright 2013 - 2017 Herb Bowie
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
package com.powersurgepub.psutils2.mkdown;

  import com.powersurgepub.psutils2.textio.*;

  import java.io.*;

  import javafx.application.*;
  import javafx.event.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.scene.text.*;
  import javafx.stage.*;

/**
 Test the markdown routines.

 @author Herb Bowie
 */
public class MarkdownTest 
    extends Application {
  
  private     Stage       mainStage;
  
  private     Scene       mainScene;
  
  private     VBox mainPane;
  
  private     Menu editMenu;
  private     Menu fileMenu;
  private     MenuItem fileOpenMenuItem;
  private     MenuBar menuBar;
  private     Button openButton;
  private     TextArea resultsTextArea;
  
  private     FileChooser        chooser;
  private     TextLineReader     lineReader = null;
  
	public static void main(String[] args) {
		// Launch the JavaFX application
		Application.launch(args);
	}
  
	@Override
	public void start(Stage mainStage) {
    
    this.mainStage = mainStage;
    mainPane = new VBox();
    
    menuBar = new MenuBar();
    editMenu = new Menu("Edit");
    fileMenu = new Menu("File");
    fileOpenMenuItem = new MenuItem("Open...");
    fileOpenMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        fileOpen();
      }
    });
    fileMenu.getItems().add(fileOpenMenuItem);
    menuBar.getMenus().addAll(fileMenu, editMenu);
    mainPane.getChildren().add(menuBar);
    
    openButton = new Button("Open");
    openButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        fileOpen();
      }
    });
    mainPane.getChildren().add(openButton);
    
    resultsTextArea = new TextArea();
    mainPane.getChildren().add(resultsTextArea);
    
    mainScene = new Scene(mainPane);
    mainStage.setScene(mainScene);
		mainStage.setTitle("Markdown Test");
		mainStage.show();
	}
 
  
  private void fileOpen() {
    chooser = new FileChooser();
    chooser.setTitle("Open Markdown File");
    File mdFile = chooser.showOpenDialog(mainStage);
    if (mdFile != null) {
      MarkdownDoc doc = new MarkdownDoc();
      lineReader = new FileLineReader (mdFile);
      boolean ok = lineReader.open();
      if (ok) {
        String line = lineReader.readLine();
        while (lineReader.isOK() 
            && (! lineReader.isAtEnd())
            && line != null) {
          MarkdownLine mdLine = new MarkdownLine (doc, line);
          mdLine.display();
          doc.display();
          line = lineReader.readLine();
        }
        lineReader.close();
      }
    }
  }

}
