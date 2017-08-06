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

package com.powersurgepub.psutils2.prefs;

  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.util.*;

  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 A useful container for multiple sets of user preferences. 

 @author Herb Bowie
 */
public class PrefsJuggler 
    implements
      WindowToManage { 
  
  private SortedMap<String, PrefSet>  prefSets;
  private Stage                       prefStage;
  private Scene                       prefScene;
  private TabPane                     tabs;
  
  private GeneralPrefs                generalPrefs = null;
  
  /**
   Constructor. 
  
   @param primaryStage The primary stage for the application. 
  */
  public PrefsJuggler(Stage primaryStage) {
    prefSets = new TreeMap<String, PrefSet>();
    prefStage = new Stage(StageStyle.DECORATED);
    prefStage.setTitle(Home.getShared().getProgramName() + " Preferences");
    prefStage.initOwner(primaryStage);
    tabs = new TabPane();
  }
  
  /**
   Add the General Preferences. 
  */
  public void addGeneralPrefs() {
    generalPrefs = new GeneralPrefs();
    addSet(generalPrefs);
  }
  
  public GeneralPrefs getGeneralPrefs() {
    return generalPrefs;
  }
  
  /**
   Add another set of preferences to the collection for this application. 
  
   @param newPrefSet Another set of preferences to be managed. 
  */
  public void addSet(PrefSet newPrefSet) {
    prefSets.put(newPrefSet.getTitle(), newPrefSet);
    Tab newTab = new Tab();
    newTab.setText(newPrefSet.getTitle());
    newTab.setClosable(false);
    newTab.setContent(newPrefSet.getPane());
    tabs.getTabs().add(newTab);
  }
  
  /**
   Get a set of preferences, based on the order in which they were added. 
  
   @param i An index indicating the order in which the sets were added, 
            starting with zero. 
  
   @return The desired set of preferences, or null for an invalid index. 
  */
  public PrefSet getSet(int i) {
    if (i < 0 || i >= tabs.getTabs().size()) {
      return null;
    } else {
      Tab tab = tabs.getTabs().get(i);
      return getSet(tab.getText());
    }
  }
  
  /**
   Get a set of preferences based on the title. 
  
   @param title The title of the desired set of preferences.
  
   @return The requested set of preferences, or null if not found. 
  */
  public PrefSet getSet(String title) {
    return prefSets.get(title);
  }
  
  /**
   Finalize the UI after adding all the desired sets. 
  */
  public void setScene() {
    prefScene = new Scene(tabs);
    prefStage.setScene(prefScene);
  }
  
  public Window getWindow() {
    return prefStage;
  }
  
  /**
   Returns the TabPane containing tabs for all of the preference sets. 
  
   @return The TabPane containing all the Preference Tabs. 
  */
  public TabPane getTabPane() {
    return tabs;
  }
  
  /**
   Relays the command to save preferences to all of the Preference Sets. 
  */
  public void save() {
    Collection<PrefSet> sets = prefSets.values();
    Iterator<PrefSet> iterator = sets.iterator();
    while (iterator.hasNext()) {
      PrefSet nextSet = iterator.next();
      nextSet.save();
    }
  }
  
  /**
   Get the title of the prefs window containing all the tabs for all the sets. 
  
   @return The title of the window. 
  */
  public String getTitle() {
    return prefStage.getTitle();
  }
  
  /**
   Make the preferences window visible or hide it.  
  
   @param visible 
  */
  public void setVisible (boolean visible) {
    if (visible) {
      prefStage.show();
    } else {
      prefStage.close();
    }
  }
  
  /** 
   Bring the window to the front. 
  */
  public void toFront() {
    prefStage.show();
    prefStage.toFront();
  }
  
  /**
   Get the width of the window. 
  
   @return The width of the window. 
  */
  public double getWidth() {
    return prefStage.getWidth();
    
  }
  
  /**
   Get the height of the window. 
  
   @return The height of the window. 
  */
  public double getHeight() {
    return prefStage.getHeight();
  }
  
  /**
   Set the location of the window on the screen. 
  
   @param x The horizontal offset from the left side of the screen. 
   @param y The vertical offset from the top of screen. . 
  */
  public void setLocation(double x, double y) {
    prefStage.setX(x);
    prefStage.setY(y);
  }

}
