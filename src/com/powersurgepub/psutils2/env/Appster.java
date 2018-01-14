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

package com.powersurgepub.psutils2.env;

  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.net.*;

  import javafx.application.*;
  import javafx.stage.*;

/**
  A coordinating class that sets up the XOS, Home, UserPrefs, ProgramVersion,
  Trouble and Localizer classes for use by a Swing application.
 */
public class Appster {
  
  public static final String LEFT           = "left";
  public static final String TOP            = "top";
  public static final String WIDTH          = "width";
  public static final String HEIGHT         = "height";

  public static final double DEFAULT_LEFT   = 40;
  public static final double DEFAULT_TOP    = 40;
  public static final double DEFAULT_WIDTH  = 620;
  public static final double DEFAULT_HEIGHT = 540;
  
  private             Home                home;
  
  private             UserPrefs           userPrefs;
  
  private             ProgramVersion      programVersion;
  
  private             Trouble             trouble    = Trouble.getShared();
  
  private             Application         app;
  private             Stage               mainFrame;
  
  /** Creates a new instance of Appster */
  public Appster(
      Application app,
      String domainLevel1, 
      String domainLevel2, 
      String programName,
      String currentVersion,
      Stage mainFrame) {
    
    init (
      app,
      domainLevel1,
      domainLevel2,
      programName,
      currentVersion,
      "",
      "",
      mainFrame);
  }
  
  /** Creates a new instance of Appster */
  public Appster(
      Application app,
      String domainLevel1, 
      String domainLevel2, 
      String programName,
      String currentVersion,
      String language,
      String country,
      Stage mainFrame) {
    init (
      app,
      domainLevel1,
      domainLevel2, 
      programName,
      currentVersion,
      language,
      country,
      mainFrame);
  }
  
  private void init (
      Application app,
      String domainLevel1, 
      String domainLevel2, 
      String programName,
      String currentVersion,
      String language,
      String country,
      Stage mainFrame) {

    this.app = app;
    this.mainFrame = mainFrame;
    
    /*
    xos.setDomainLevel1 (domainLevel1);
    xos.setDomainLevel2 (domainLevel2);
    xos.setProgramName (programName);
    if (xhandler.preferencesAvailable()) {
      xos.enablePreferences();
    }
    xos.initialize();
    */
    if (mainFrame != null) {
      mainFrame.setTitle (programName);
    }
    home = Home.getShared (app, programName, currentVersion);
    userPrefs = UserPrefs.getShared();
    programVersion = ProgramVersion.getShared();
    String resourceBundleName =
        domainLevel2 + "." +
        domainLevel1 + "." +
        home.getProgramNameLower() + "." +
        home.getProgramNameLower() + "strings";
    // System.out.println ("resource bundle = " + resourceBundleName);

    /*if ((language.length() > 0 && (! language.equals ("  ")))
        || (country.length() > 0 && (! country.equals ("  ")))) {
      localizer = Localizer.getShared (resourceBundleName, language, country);
    } else {
      localizer = Localizer.getShared (resourceBundleName);
    }*/

    trouble.setParent (mainFrame);
    if (mainFrame != null) {
      mainFrame.setX(userPrefs.getPrefAsDouble (LEFT, DEFAULT_LEFT));
      mainFrame.setY(userPrefs.getPrefAsDouble (TOP,  DEFAULT_TOP));
      mainFrame.setWidth(userPrefs.getPrefAsDouble (WIDTH, DEFAULT_WIDTH));
      mainFrame.setHeight(userPrefs.getPrefAsDouble (HEIGHT, DEFAULT_HEIGHT));
    }

  }

  public void setMainFrame(Stage mainFrame) {
    this.mainFrame = mainFrame;
  }
  
  public void handleQuit () {
    if (mainFrame != null) {
      userPrefs.setPref (LEFT, mainFrame.getX());
      userPrefs.setPref (TOP, mainFrame.getY());
      userPrefs.setPref (WIDTH, mainFrame.getWidth());
      userPrefs.setPref (HEIGHT, mainFrame.getHeight());
    }
    boolean prefsOK = userPrefs.savePrefs();
  }
  
  public boolean openURL (File file) {
    return home.openURL(file);
  }
  
  public boolean openURL (String url) {
    return home.openURL(url);
  }
  
}
