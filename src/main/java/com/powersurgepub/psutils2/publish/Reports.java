/*
 * Copyright 2016 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.publish;

	import com.powersurgepub.psutils2.env.*;
	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.script.*;
  import com.powersurgepub.psutils2.textmerge.*;
	import com.powersurgepub.psutils2.txbio.*;
	import com.powersurgepub.psutils2.txbmodel.*;

  import java.io.*;
  import java.util.*;

 	import javafx.event.*;
 	import javafx.scene.control.*;

/**
 A collection of reports available within an application. Each report requires
 a matching PSTextMerge script file that can be run in order to generate the 
 report. These script files will retrieved from a folder named 'reports'. If
 an application data folder already contains such a folder, then that folder
 will be used. If not, then the reports will be loaded from the application's 
 resources folder. The first time that any report is executed for a particular
 data collection, the standard reports folder will be copied to the application
 data folder. After this initial copy, the contents of this folder may be 
 customized as needed. 

 @author Herb Bowie
 */
public class Reports {
  
  public static final String REPORTS_FOLDER = "reports";
  public static final String WEBPREFS_FILE  = "webprefs.css";
  public static final String CSSHREF_FILE   = "csshref.html";
  
  private TextMergeHarness    textMerge = null;
  private ScriptExecutor      scriptExec = null;
  
  private Menu                reportsMenu = null;
  private File                appReportsFolder = null;
  private File                dataReportsFolder = null;
  
  private boolean             dataReportsFolderPopulated = false;
  
  private SortedMap<String, Report>   reports = new TreeMap<String, Report>();
  
  private WebPrefsProvider    webPrefs = null;
  
  private     MarkupWriter        markupWriter;
  
  /**
   Construct a new report object. 
  
   @param reportsMenu The report menu under which the list of available
                      reports will be available. 
  */
  public Reports(Menu reportsMenu) {
    this.reportsMenu = reportsMenu;
    textMerge = TextMergeHarness.getShared();
    textMerge.initTextMergeModules();
  }
  
  /**
   Set the Executor that is to receive any callbacks from the script. 
  
   @param scriptExec The Executor that is to receive any 
                     callbacks from the script.
  */
  public void setScriptExecutor(ScriptExecutor scriptExec) {
    this.scriptExec = scriptExec;
    textMerge.setExecutor(scriptExec);
  }
  
  public void setWebPrefs(WebPrefsProvider webPrefs) {
    this.webPrefs = webPrefs;
  }
 
  /**
   Provide a new folder full of application data, which may contain a 
   folder named 'reports'. The reports menu will be re-populated with 
   reports available for this data folder. 
  
   @param dataFolder The application data folder. 
  */
  public void setDataFolder(File dataFolder) {

    dataReportsFolder = new File(dataFolder, REPORTS_FOLDER);
    appReportsFolder = new File(Home.getShared().getAppFolder(), REPORTS_FOLDER);
    reports = new TreeMap<String, Report>();
    while (reportsMenu.getItems().size() > 0) {
      reportsMenu.getItems().remove(0);
    }
    
    dataReportsFolderPopulated = true;
    
    if (dataReportsFolder.exists()) {
      loadReports (dataReportsFolder);
      Logger.getShared().recordEvent(LogEvent.NORMAL, 
          "Loading reports from " + dataReportsFolder.toString(), false);
    }
    
    if (reports.isEmpty()) {
      Logger.getShared().recordEvent(LogEvent.NORMAL, 
          "Loading reports from " + appReportsFolder.toString(), false);
      loadReports (appReportsFolder);
      dataReportsFolderPopulated = false;
    }
    
  }
  
  /**
   Get the location of the reports folder into which reports should be written. 
  
   @return The location of the reports folder into which 
           reports should be written. 
  */
  public File getReportsFolder() {
    return dataReportsFolder;
  }
  
  /**
   Load the reports from the passed folder. 
  
   @param folder The folder to be used. 
  */
  private void loadReports (File folder) {
    if (folder != null && folder.exists() && folder.canRead()) {
      String[] fileNames = folder.list();
      for (String fileName : fileNames){
        File candidate = new File (folder, fileName);
        if (Report.isValidFile(candidate)) {
          Report report = new Report(candidate);
          reports.put(report.getKey(), report);
          MenuItem menuItem = new MenuItem(report.getName());
          menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
              if (! dataReportsFolderPopulated) {
                dataReportsFolderPopulated = FileUtils.copyFolder
                  (appReportsFolder, dataReportsFolder);
              }
              Report selectedReport = reports.get(report.getKey());
              runReport(selectedReport);
            }
          });
          reportsMenu.getItems().add(menuItem);
        } // end if the right kind of file
      } // end for each file in the folder
    } // end if good folder
  } // end loadReports method
  
  /**
   Run the report that has been requested and attempt to open the output 
   in a web browsers window. 
  
   @param report The report to be run. 
  */
  private void runReport(Report report) {
    if (report != null) {
      File reportScript 
          = new File(dataReportsFolder, report.getFileName());
      if (reportScript != null
          && reportScript.exists()
          && reportScript.canRead()) {
        if (scriptExec != null) {
          textMerge.setExecutor(scriptExec);
        }
        
        if (webPrefs != null) {
          File cssFile = new File(dataReportsFolder, WEBPREFS_FILE);
          markupWriter = new MarkupWriter (cssFile, MarkupWriter.HTML_FRAGMENT_FORMAT);
          markupWriter.openForOutput();
          markupWriter.writeLine ("      body, p, h1, li {");
          markupWriter.writeLine ("        font-family: \"" + webPrefs.getFontFamily() 
                                            + "\"" + ", Verdana, Geneva, sans-serif;");
          markupWriter.writeLine ("        font-size: " + webPrefs.getFontSize() + ";");
          markupWriter.writeLine ("      }");
          markupWriter.close();
          
          String cssHref = webPrefs.getCSShref();
          if (cssHref != null && cssHref.length() > 0) {
            File cssHrefFile = new File(dataReportsFolder, CSSHREF_FILE);
            markupWriter = new MarkupWriter (cssHrefFile, MarkupWriter.HTML_FRAGMENT_FORMAT);
            markupWriter.openForOutput();
            StringBuffer textOut = new StringBuffer("  ");
            textOut.append ("<" + TextType.LINK);
            textOut.append (" " 
                + TextType.REL + "=\""  
                + TextType.STYLESHEET + "\"");
            textOut.append (" " 
                + TextType.TYPE + "=\""  
                + TextType.TEXT_CSS + "\"");
            textOut.append (" " 
                + TextType.HREF + "=\""  
                + cssHref + "\"");
            textOut.append (" />");
            markupWriter.writeLine (textOut.toString());
            markupWriter.close();
          }
        }
        
        textMerge.resetOutputFileName();
        textMerge.playScript(reportScript);
        String reportFileName = textMerge.getOutputFileName().trim();
        File reportFile = null;
        if (reportFileName == null || reportFileName.length() == 0) {
          reportFile = new File(dataReportsFolder, report.getHTMLName());
        } else {
          reportFile = new File(reportFileName);
        }
        if (reportFile != null
            && reportFile.exists()
            && reportFile.canRead()) {
          Home.getShared().openURL(reportFile);
        }
      } // end if we have a report script to execute
    } // end if we have a selected report
  }

}
