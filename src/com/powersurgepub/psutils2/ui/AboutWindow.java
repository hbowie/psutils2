/*
 * Copyright 1999 - 2018 Herb Bowie
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
  A panel to display information about the version of the 
  software being executed.  Note that COPYRIGHT_YEAR_THRU
  should be updated to the current year at the beginning 
  of each new year. 

  @author Herb Bowie
 */
public class AboutWindow 
    implements 
        WindowToManage {
  
  public  static final String COPYRIGHT_YEAR_THRU = "2018";
  
  private String    copyRightYears = "";
  private String    fontBegin = "<font face=\"Arial\" size=\"4\">";
  private String    fontEnd   = "</font>";
  
  private Home      home       = Home.getShared();
  private File      appFolder  = null;
  private URL       pageURL;
  private URL       aboutURL;
  private String    aboutFileName = "about.html";
  private Trouble   trouble = Trouble.getShared();
  
  private boolean   loadFromDisk = true;
  
  private boolean   jxlUsed              = true;
  private boolean   markdownUsed          = true;
  private boolean   xercesUsed           = false;
  private boolean   saxonUsed            = false;
  
  private Stage     primaryStage = null;
  
  private WindowToManage windowToManage;
  
  private FXUtils   fxUtils;
  
  private Stage     aboutStage;
  private Scene     aboutScene;
  private GridPane  aboutPane;
  
  private     String                labelHeadingStyle =
      "-fx-border-color: gray; " +
      "-fx-border-width: 1; " + 
      "-fx-border-style: solid; " +
      "-fx-border-insets: 1; " + 
      "-fx-padding: 5; " +
      "-fx-font-size: 16px; ";
  
  private     Label           programNameAndVersionText;
  
  private     StringBuilder   aboutText;
  private     WebView         aboutWebView;
  private     WebEngine       aboutWebEngine;
  
  private     Label           aboutJavaLabel;
  
  private     TextArea        aboutJavaTextArea;

  
  /** Creates new form AboutWindow */
  public AboutWindow(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.loadFromDisk = true;
    setupWindow ();
  }
  
  public AboutWindow (Stage primaryStage, boolean loadFromDisk) {
    this.primaryStage = primaryStage;
    this.loadFromDisk = loadFromDisk;
    setupWindow ();
  }
  
  /**
   Constructor specifying optional parameters to tailor the About window. 
  
   @param loadFromDisk Should the about file be loaded from disk?
   @param jxlUsed              Does this app use jxl?
   @param markdownUsed         Does this app use a markdown converter?
   @param xercesUsed           Does this app use xerces for xml parsing?
   @param saxonUsed            Does this app use saxon for xslt processing?
   @param copyRightYearFrom    Specify the year first published. 
  */
  public AboutWindow (
      Stage primaryStage,
      boolean loadFromDisk, 
      boolean jxlUsed,
      boolean markdownUsed,
      boolean xercesUsed,
      boolean saxonUsed,
      String  copyRightYearFrom) {
    this.primaryStage = primaryStage;
    this.loadFromDisk = loadFromDisk;
    this.jxlUsed = jxlUsed;
    this.markdownUsed = markdownUsed;
    this.xercesUsed = xercesUsed;
    this.saxonUsed = saxonUsed;
    home.setCopyrightYearFrom(copyRightYearFrom);
    setupWindow ();
  }
  
  public void setJXLUsed (boolean jxlUsed) {
    this.jxlUsed = jxlUsed;
    setupWindow();
  }
  
  public void setMarkdownUsed (boolean markdownUsed) {
    this.markdownUsed = markdownUsed;
    setupWindow();
  }
  
  private void setupWindow () {
    
    windowToManage = this;
    
    buildUI();
    
    programNameAndVersionText.setText
        (home.getProgramName() 
        + " version " + home.getProgramVersion());

    aboutJavaTextArea.setText
        (System.getProperty("java.vm.name") + 
        " version " + System.getProperty("java.vm.version") +
        " from " + StringUtils.removeQuotes(System.getProperty("java.vm.vendor")) +
        ", JRE version " + System.getProperty("java.runtime.version"));
    
    boolean loadedFromDisk = loadFromDisk;
    appFolder = home.getAppFolder();
    if (loadFromDisk 
        && appFolder == null) {
      aboutFileError();
      loadedFromDisk = false;
    }
    if (loadedFromDisk) {
      try {
        URI pageURI = appFolder.toURI();
        pageURL = pageURI.toURL();
      } catch (MalformedURLException e) {
        loadedFromDisk = false;
        trouble.report ("Trouble forming pageURL from " + appFolder.toString(), 
            "URL Problem");
      }
    }
    if (loadedFromDisk) {
      try {
        aboutURL = new URL (pageURL, aboutFileName);
      } catch (MalformedURLException e) {
        loadedFromDisk = false;
        trouble.report ("Trouble forming aboutURL", "URL Problem");
      }
    }
    if (loadedFromDisk) {
      aboutWebEngine.load(aboutURL.toString());
    }
    if (! loadedFromDisk) {
      aboutText = new StringBuilder();
      aboutText.append("<html>");
      
      aboutText.append("<p>");
      aboutText.append(fontBegin);
      aboutText.append("Copyright &copy; ");
      if (home.getCopyrightYearFrom().equals(COPYRIGHT_YEAR_THRU)) {
        copyRightYears = COPYRIGHT_YEAR_THRU;
      } else {
        copyRightYears = home.getCopyrightYearFrom() + " - " + COPYRIGHT_YEAR_THRU;
      }
      aboutText.append(copyRightYears);
      aboutText.append(" Herb Bowie");
      aboutText.append(fontEnd);
      aboutText.append("</p>");
      
      aboutText.append("<p>");
      aboutText.append(fontBegin);
      aboutText.append("Licensed under the ");
      aboutText.append("<a href=\"http://www.apache.org/licenses/LICENSE-2.0\">");
      aboutText.append("Apache License 2.0");
      aboutText.append("</a>");
      aboutText.append(fontEnd);
      aboutText.append("</p>");
      
      aboutText.append("<p>");
      aboutText.append(fontBegin);
      aboutText.append("To receive support, report bugs, request enhancements, ");
      aboutText.append("or simply express unbridled enthusiasm for this product and its author, ");
      aboutText.append("send an e-mail to the address below.");
      aboutText.append(fontEnd);
      aboutText.append("</p>");
      
      aboutText.append("<p>");
      aboutText.append(fontBegin);
      aboutText.append(home.getProgramName());
      aboutText.append(" is written in Java. It may be run on Windows, Macintosh and other Unix platforms. ");
      aboutText.append(home.getProgramName());
      aboutText.append(" requires a Java Virtual Machine (JVM/JRE) of version 7 or later. ");
      aboutText.append("You may wish to visit www.Java.com to download a compatible JVM. ");
      aboutText.append(fontEnd);
      aboutText.append("</p>");
      
      aboutText.append("<br>");
      
      aboutText.append("<table border=0 cellpadding=0 cellspacing=0>");
      
      aboutText.append("<tr><td width=70 align=left valign=top>");
      aboutText.append(fontBegin);
      aboutText.append("E-mail: ");
      aboutText.append(fontEnd);
      aboutText.append("</td>");
      aboutText.append("<td>");
      aboutText.append(fontBegin);
      aboutText.append("<a href=\"mailto:support@powersurgepub.com\">");
      aboutText.append("support@powersurgepub.com");
      aboutText.append("</a>");
      aboutText.append(fontEnd);
      aboutText.append("</td></tr>");
      
      aboutText.append("<tr><td columns=2>&nbsp;</td></tr>");
      
      aboutText.append("<tr><td width=70 align=left valign=top>");
      aboutText.append(fontBegin);
      aboutText.append("WWW: ");
      aboutText.append(fontEnd);
      aboutText.append("</td>");
      aboutText.append("<td>");
      aboutText.append(fontBegin);
      aboutText.append("<a href=\"http://www.powersurgepub.com/\">");
      aboutText.append("www.powersurgepub.com");
      aboutText.append("</a>");
      aboutText.append(fontEnd);
      aboutText.append("</td></tr>");
      
      boolean firstCredit = true;
      
      if (jxlUsed) {
        aboutText.append("<tr><td columns=2>&nbsp;</td></tr>");
        aboutText.append("<tr><td width=70 align=left valign=top>");
        aboutText.append(fontBegin);
        if (firstCredit) {
          aboutText.append("Credits:");
          firstCredit = false;
        } else {
          aboutText.append("&nbsp;");
        }
        aboutText.append(fontEnd);
        aboutText.append("</td>");
        aboutText.append("<td>");
        aboutText.append(fontBegin);
        aboutText.append("<a href=\"http://sourceforge.net/projects/jexcelapi\">");
        aboutText.append("JExcelAPI");
        aboutText.append("</a>");
        aboutText.append(" Copyright 2002 Andrew Khan, ");
        aboutText.append("used under the terms of the ");
        aboutText.append("<a href=\"http://www.gnu.org/licenses/lgpl.html\">");
        aboutText.append("GNU Lesser General Public License");
        aboutText.append("</a>");
        aboutText.append(fontEnd);
        aboutText.append("</td></tr>");
      }
      
      if (markdownUsed) {
        
        aboutText.append("<tr><td columns=2>&nbsp;</td></tr>");
        aboutText.append("<tr><td width=70 align=left valign=top>");
        aboutText.append(fontBegin);
        if (firstCredit) {
          aboutText.append("Credits:");
          firstCredit = false;
        } else {
          aboutText.append("&nbsp;");
        }
        aboutText.append(fontEnd);
        aboutText.append("</td>");
        aboutText.append("<td>");
        aboutText.append(fontBegin);
        aboutText.append("<a href=\"https://github.com/vsch/flexmark-java\">");
        aboutText.append("flexmark-java");
        aboutText.append("</a>");
        aboutText.append(" Copyright (c) 2016, Vladimir Schneider, ");
        aboutText.append("used under the terms of the ");
        aboutText.append("<a href=\"https://opensource.org/licenses/BSD-2-Clause\">");
        aboutText.append("BSD 2-Clause Simplified License");
        aboutText.append("</a>");
        aboutText.append(fontEnd);
        aboutText.append("</td></tr>");
      }
      
      if (xercesUsed) {
        aboutText.append("<tr><td columns=2>&nbsp;</td></tr>");
        aboutText.append("<tr><td width=70 align=left valign=top>");
        aboutText.append(fontBegin);
        if (firstCredit) {
          aboutText.append("Credits:");
          firstCredit = false;
        } else {
          aboutText.append("&nbsp;");
        }
        aboutText.append(fontEnd);
        aboutText.append("</td>");
        aboutText.append("<td>");
        aboutText.append(fontBegin);
        aboutText.append("<a href=\"http://xerces.apache.org\">");
        aboutText.append("Xerces");
        aboutText.append("</a>");
        aboutText.append(" Copyright 1999-2012 The Apache Software Foundation, ");
        aboutText.append("used under the terms of the ");
        aboutText.append("<a href=\"http://www.apache.org/licenses/LICENSE-2.0\">");
        aboutText.append("Apache License, Version 2.0");
        aboutText.append("</a>");
        aboutText.append(fontEnd);
        aboutText.append("</td></tr>");
      }
      
      if (saxonUsed) {
        aboutText.append("<tr><td columns=2>&nbsp;</td></tr>");
        aboutText.append("<tr><td width=70 align=left valign=top>");
        aboutText.append(fontBegin);
        if (firstCredit) {
          aboutText.append("Credits:");
          firstCredit = false;
        } else {
          aboutText.append("&nbsp;");
        }
        aboutText.append(fontEnd);
        aboutText.append("</td>");
        aboutText.append("<td>");
        aboutText.append(fontBegin);
        aboutText.append("<a href=\"http://saxon.sourceforge.net\">");
        aboutText.append("Saxon");
        aboutText.append("</a>");
        aboutText.append(" Copyright Michael H. Kay, ");
        aboutText.append("used under the terms of the ");
        aboutText.append("<a href=\"http://www.mozilla.org/MPL/\">");
        aboutText.append("Mozilla Public License, Version 1.0");
        aboutText.append("</a>");
        aboutText.append(fontEnd);
        aboutText.append("</td></tr>");
      }
      
      aboutText.append("</table>");
      
      aboutText.append("</html>");
      aboutWebEngine.loadContent(aboutText.toString());
    }

    aboutStage.setX(100);
    aboutStage.setY(100);
    aboutStage.setWidth(600);
    aboutStage.setHeight(540);
    // aboutTextPane.addHyperlinkListener (this);
  }
  
  private void buildUI() {

    aboutStage = new Stage(StageStyle.DECORATED);
    aboutStage.setTitle("About " + Home.getShared().getProgramName());
    aboutStage.initOwner(primaryStage);
    
    aboutStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    }); 
    
    aboutStage.setOnHidden(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        WindowMenuManager.getShared().hideAndRemove(windowToManage);
      }
    });
    
    fxUtils = new FXUtils();
    aboutPane = new GridPane();
    fxUtils.applyStyle(aboutPane);

    programNameAndVersionText = new Label();
    programNameAndVersionText.setAlignment(Pos.CENTER);
    programNameAndVersionText.setText("xxx version n.nn");
    programNameAndVersionText.setStyle(labelHeadingStyle);
    programNameAndVersionText.setMaxWidth(Double.MAX_VALUE);
    aboutPane.add(programNameAndVersionText, 0, 0, 2, 1);
    GridPane.setHgrow(programNameAndVersionText, Priority.ALWAYS);

    aboutWebView = new WebView();
    aboutWebEngine = aboutWebView.getEngine();
    aboutWebView.setMaxWidth(Double.MAX_VALUE);
    aboutPane.add(aboutWebView, 0, 1, 2, 1);
    GridPane.setHgrow(aboutWebView, Priority.ALWAYS);


    aboutJavaLabel = new Label("About Java: ");
    aboutPane.add(aboutJavaLabel, 0, 2, 1, 1);

    aboutJavaTextArea = new TextArea();
    aboutJavaTextArea.setWrapText(true);
    aboutJavaTextArea.setEditable(false);
    aboutJavaTextArea.setPrefColumnCount(20);
    aboutJavaTextArea.setPrefRowCount(3);
    aboutJavaTextArea.setMaxWidth(Double.MAX_VALUE);
    aboutPane.add(aboutJavaTextArea, 1, 2, 1, 1);
    GridPane.setHgrow(aboutJavaTextArea, Priority.SOMETIMES);

    aboutScene = new Scene(aboutPane, 600, 540);
    
    aboutStage.setScene(aboutScene);
  }
  
  /*
  public void hyperlinkUpdate (HyperlinkEvent e) {
    HyperlinkEvent.EventType type = e.getEventType();
    if (type == HyperlinkEvent.EventType.ACTIVATED) {
      openURL (e.getURL());
    }
  }
  */
  
  private void aboutFileError () {
    Trouble.getShared().report(primaryStage, 
        "About File named "
        + aboutFileName
        + " could not be opened successfully", 
        "About File Error", 
        AlertType.ERROR);
  }
  
  public String getTitle() {
    return aboutStage.getTitle();
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      aboutWebEngine.loadContent(aboutText.toString());
      aboutStage.show();
    } else {
      aboutStage.hide();
    }
  }
  
  public void toFront() {
    aboutWebEngine.loadContent(aboutText.toString());
    aboutStage.toFront();
  }
  
  public double getWidth() {
    return aboutStage.getWidth();
  }
  
  public double getHeight() {
    return aboutStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    aboutStage.setX(x);
    aboutStage.setY(y);
  }

}
