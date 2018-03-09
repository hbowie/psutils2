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

package com.powersurgepub.psutils2.textmerge;

	import com.powersurgepub.psutils2.list.*;
	import com.powersurgepub.psutils2.script.*;
	import com.powersurgepub.psutils2.ui.*;

  import java.io.*;

 	import javafx.event.*;
 	import javafx.scene.*;
 	import javafx.scene.control.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;

/**
 
 @author Herb Bowie
 */
public class TextMergeHarness 
    implements 
      ScriptExecutor,
      TextMergeController,
      WindowToManage {
  
  public  static final int                TEXT_MERGE_WINDOW_DEFAULT_X = 60;
  public  static final int                TEXT_MERGE_WINDOW_DEFAULT_Y = 60;
  public  static final int                TEXT_MERGE_WINDOW_DEFAULT_WIDTH = 640;
  public  static final int                TEXT_MERGE_WINDOW_DEFAULT_HEIGHT = 480;
  
  private static      TextMergeHarness    sharedHarness = null;  
  
  private             DataRecList         list = null;
  private             boolean             listAvailable = false;
  
  private             TextMergeController controller = null;
  
  private             ScriptExecutor      executor = null;
  private             boolean             allow = false;
  private             boolean             combineAllowed = false;
  private             boolean             inputModule = true;
  private             boolean             outputModule = false;
  
  private             TextMergeInput      textMergeInput = null;
  private             TextMergeScript     textMergeScript = null;
  private             TextMergeFilter     textMergeFilter = null;
  private             TextMergeSort       textMergeSort = null;
  private             TextMergeTemplate   textMergeTemplate = null;
  private             TextMergeOutput     textMergeOutput = null;
  
  private             int                 filterTabIndex = 0;
  private             int                 sortTabIndex = 1;
  private             int                 templateTabIndex = 2;
  
  private             FXUtils             fxUtils;
  private             Stage               primaryStage = null;
  private             Stage               textMergeStage = null;
  private             Scene               textMergeScene;
  private             GridPane            textMergePane;
  private             TabPane             tabPane;
  private             WindowToManage      windowToManage;

	/**
     Play a script.
   
     @param script    Name of script file to be played.
   */
	public static void playScript (String script) {
    TextMergeHarness textMerge = TextMergeHarness.getShared();
    textMerge.initTextMergeModules();
    File scriptFile = new File(script);
    textMerge.playScript(scriptFile);
	} // end execScript method

  public static TextMergeHarness getShared() {
    if (sharedHarness == null) {
      sharedHarness = new TextMergeHarness();
    }
    return sharedHarness;
  }
  
  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
    if (primaryStage != null && textMergeStage != null) {
      textMergeStage.initOwner(primaryStage);
    }
  }
  
  private TextMergeHarness() {
    list = new DataRecList();
    windowToManage = this;
    initComponents();
  }
  
  public void setList(DataRecList list) {
    this.list = list;
    if (textMergeInput != null
        && (list instanceof DataRecList)) {
      textMergeInput.setList(list);
    }
    if (textMergeFilter != null) {
      textMergeFilter.setList(list);
    }
    if (textMergeSort != null) {
      textMergeSort.setList(list);
    }
    if (textMergeTemplate != null) {
      textMergeTemplate.setList(list);
    }
    if (textMergeScript != null) {
    textMergeScript.setList(list);
    }
    if (textMergeOutput != null
        && (list instanceof DataRecList)) {
      textMergeOutput.setList(list);
    }
  }
  
  public void setController(TextMergeController controller) {
    this.controller = controller;
  }
  
  public void setExecutor(ScriptExecutor executor) {
    this.executor = executor;
    if (textMergeScript != null) {
      textMergeScript.setScriptExecutor(executor);
    }
  }

  /**
   Let's reset as many variables as we can to restore the
   text merge state to its original condition.
   */
  public void textMergeReset() {
  }
  
  public void enableInputModule(boolean inputModule) {
    this.inputModule = inputModule;
  }
  
  public void enableOutputModule(boolean outputModule) {
    this.outputModule = outputModule;
  }
  
  public void initTextMergeModules() {
    
    Window passedWindow = primaryStage;
    if (passedWindow == null) {
      passedWindow = textMergeStage;
    }
    
    if (textMergeScript == null) {
      textMergeScript   = new TextMergeScript(passedWindow, list, this);

      if (list instanceof DataRecList) {
        if (controller == null) {
          textMergeInput  = new TextMergeInput 
            (passedWindow, list, this, textMergeScript);
        } else {
          textMergeInput  = new TextMergeInput 
            (passedWindow, list, controller, textMergeScript);
        }
      }
      textMergeFilter   = new TextMergeFilter
        (passedWindow, list, this, textMergeScript);
      textMergeSort     = new TextMergeSort  
        (passedWindow, list, this, textMergeScript);
      textMergeTemplate = new TextMergeTemplate 
        (passedWindow, list, this, textMergeScript);
      textMergeOutput   = new TextMergeOutput   
        (passedWindow, list, this, textMergeScript);

      textMergeScript.allowAutoplay(allow);

      textMergeScript.setInputModule(textMergeInput);
      textMergeScript.setFilterModule(textMergeFilter);
      textMergeScript.setSortModule(textMergeSort);
      textMergeScript.setTemplateModule(textMergeTemplate);
      textMergeScript.setOutputModule(textMergeOutput);

      if (executor == null) {
        textMergeScript.setScriptExecutor(this);
      } else {
        textMergeScript.setScriptExecutor(executor);
      }

      int tabCount = 0;
      
      if (inputModule) {
        textMergeInput.setTabs(this.getTabs());
        tabCount++;
      }
      
      textMergeScript.setTabs(this.getTabs());
      filterTabIndex = tabCount;
      tabCount++;
      
      textMergeFilter.setTabs(this.getTabs());
      sortTabIndex = tabCount;
      tabCount++;
      
      textMergeSort.setTabs(this.getTabs(), combineAllowed);
      templateTabIndex = tabCount;
      tabCount++;
      
      textMergeTemplate.setTabs(this.getTabs());
      tabCount++;
      
      if (outputModule) {
        textMergeOutput.setTabs(this.getTabs());
        tabCount++;
      }
      
      textMergeScript.selectEasyTab();
    }
  }
  
  public void allowAutoplay(boolean allow) {
    this.allow = allow;
    if (textMergeScript != null) {
      textMergeScript.allowAutoplay(allow);
    }
  }
  
  public void setCombineAllowed(boolean combineAllowed) {
    this.combineAllowed = combineAllowed;
    if (textMergeSort != null) {
      textMergeSort.setTabs(this.getTabs(), combineAllowed);
    }
  }
  
  public void setMenus(MenuBar menus, String menuText) {
    if (textMergeScript != null) {
      textMergeScript.setMenus(menus, menuText);
    }
  }
  
  public TextMergeScript getTextMergeScript() {
    return textMergeScript;
  }
  
  public void playScript (File sFile) {
    textMergeScript.playScript(sFile);
  }
  
  public void resetOutputFileName() {
    textMergeTemplate.resetOutputFileName();
  }
  
  public String getOutputFileName() {
    return textMergeTemplate.getOutputFileName();
  }
  
  /**
   Get the tabPane containing all the TextMerge panels. 
  
   @return the tabPane containing all the TextMerge panels. 
  */
  public TabPane getTabs() {
    return tabPane;
  }
  
  /**
   Add to Window Menu. 
  */
  public void addToWindowMenuManager() {
    WindowMenuManager.getShared().add(this);
  }
  
  public void addScriptsToMenuBar(MenuBar menuBar) {
    textMergeScript.setMenus(menuBar, "Scripts");
  }
  
  public void selectEasyTab() {
    textMergeScript.selectEasyTab();
  }
  
  public void clearSortAndFilterSettings() {
    if (textMergeScript != null) {
      textMergeScript.clearSortAndFilterSettings();
    }
  }
  
  /**
   A method provided to PSTextMerge 
  
   @param operand
   */
  public void scriptCallback(String operand) {
    if (executor != null) {
      executor.scriptCallback(operand);
    }
  }
  
  /**
   Indicate whether or not a list has been loaded. 
  
   @param listAvailable True if a list has been loaded, false if the list
                        is not available. 
  */
  public void setListAvailable (boolean listAvailable) {
    this.listAvailable = listAvailable;
  }
  
  /**
   Indicate whether or not a list has been loaded. 
  
   @return True if a list has been loaded, false if the list is not 
           available. 
  */
  public boolean isListAvailable() {
    return listAvailable;
  }
  
  private void initComponents() {
    textMergeStage = new Stage(StageStyle.DECORATED);
    textMergeStage.setTitle("TextMerge");
    if (primaryStage != null) {
      textMergeStage.initOwner(primaryStage);
    }
    
    textMergeStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    }); 
    
    textMergeStage.setOnHidden(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    });
    
    fxUtils = new FXUtils();
    textMergePane = new GridPane();
    fxUtils.applyStyle(textMergePane);
    
    tabPane = new TabPane();
    
		textMergePane.add(tabPane, 0, 0, 1, 1);
		tabPane.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(tabPane, Priority.ALWAYS);
		tabPane.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(tabPane, Priority.ALWAYS);
    
    textMergeScene = new Scene(textMergePane, 600, 540);
    
    textMergeStage.setScene(textMergeScene);
  }                       

  private void formWindowClosing(java.awt.event.WindowEvent evt) {                                   
    
  }                                  

  private void formWindowActivated(java.awt.event.WindowEvent evt) {                                     

  }
  
  public String getTitle() {
    return textMergeStage.getTitle();
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      textMergeStage.show();
    } else {
      textMergeStage.hide();
    }
  }
  
  public void toFront() {
    textMergeStage.toFront();
  }
  
  public double getWidth() {
    return textMergeStage.getWidth();
  }
  
  public double getHeight() {
    return textMergeStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    textMergeStage.setX(x);
    textMergeStage.setY(y);
  }

}
