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

package com.powersurgepub.psutils2.template;

	import com.powersurgepub.psutils2.basic.*;
	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.markup.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.strings.*;
	import com.powersurgepub.psutils2.tags.*;
	import com.powersurgepub.psutils2.textio.*;
	import com.powersurgepub.psutils2.txbio.*;
	import com.powersurgepub.psutils2.txbmodel.*;
	import com.powersurgepub.psutils2.values.*;

  import java.io.*;
  import java.net.*;
  import java.text.*;
  import java.util.*;

  import javafx.scene.control.*;

/**
   Persistent data needed by the TemplateLine class. <p>
  
   This code is copyright (c) 1999-2002 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
    2002/10/05 - Added the list item pending flag. <li>
    2002/03/23 - Added support for group processing. <li>
    2000/06/17 - Added global variables. <li>
    2000/05/28 - Created to prevent Template and TemplateLine from having
                 to both reference each other, and modified to be 
                 consistent with "The Elements of Java Style".</ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
   @version 
    2003/03/08 - Added code to the setGroup method. When a group break is now
                 detected, the method will reset lower level 
                 break values, forcing a new group if non-null. This fixed
                 a prior bug.
 */
public class TemplateUtil {
  
  /** Indicator to write line out without a trailing line break. */
  final static String NO_LINE_BREAK = "nobr";
  
  /** Variable name that will be replaced with the name of the template file. */
  final static String TEMPLATE_FILE_NAME_VARIABLE = "templatefilename";
  
  /** Variable name that will be replaced with the parent folder for the
      template file. */
  final static String TEMPLATE_PARENT_NAME_VARIABLE = "templateparent";
  
  /** Variable name that will be replaced with the name of the input data file. */
  final static String DATA_FILE_NAME_VARIABLE = "datafilename";

  /** Variable name that will be replaced with the name of the input data file,
   without the extension. */
  final static String DATA_FILE_BASE_NAME_VARIABLE = "datafilebasename";
  
  /** Variable name that will be replaced with the parent folder for the 
      input data file. */
  final static String DATA_PARENT_NAME_VARIABLE = "dataparent";
  
  /** Variable name to be replaced by the name of the folder in which the
      data is stored. */
  final static String DATA_PARENT_FOLDER_VARIABLE = "parentfolder";
  
  /** Variable name that will be replaced with today's date. */
  final static String TODAYS_DATE_VARIABLE = "today";
  
  /** Relative path to web site root. */
  final static String RELATIVE_VARIABLE = "relative";
  
  /** Default formatting string for dates. */
  final static String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
  
  /** Include parm value to cause include file to be copied as-is. **/
  public static final String INCLUDE_COPY = "copy";
  
  private       boolean   debug = false;
  
  /** Used to write events and data to a log file. */
  private    Logger      log;
  
  /** Work area for any event to be logged. */
  private    LogEvent    event;
  
  /** Simple file name for the template file. */
  private    String         templateFileSimpleName;
  
  /** File name, including path, for the template file. */
  private    String         templateFilePathAndName;
  
  /** File definition for the template file. */
  private    File           templateFileSpec;
  
  /** Template file as a XTextFile. */
  // private    XTextFile       templateFile;
  private    TextLineReader  templateFile;
  
  /** Did the template file open successfully? */
  private    boolean        templateFileOK;
  
  /** Have we hit end of all the template data yet? */
  private    boolean        templateFileAtEnd = false;
  
  /** Number of records so far read from the template file. */
  private    int            templateFileLineCount = 0;
  
  /** The name of the input template file being used. */
  private    String      templateFileName;
  
  /** The directory path to the template file. */
  private    String      templateFilePath;
  
  /** The folder enclosing the template file. */
  private    String      templateParent;
  
  /** First line of template file? */
  private    boolean     firstTemplateLine = true;
  
  /** The name of the input data file/folder, formatted for display to a user. */
  private    String      dataFileDisplay;

  /** The name of the input data file/folder, without any file extension. */
  private    String      dataFileBaseName;
  
  /** The folder enclosing the data file. */
  private    String      dataParent;
  
  private    String      dataParentFolder;
  
  /** Location of the web root directory. */
  private    File         webRootFile = null;
  private    FileName     webRootFileName = null;
  
  private    StringBuilder relativePathToRoot = new StringBuilder();
  
  /** Name of the output text file. */
  private    FileName     textFileOutName;
  
  /** Output text file where merged file is written. */
  private    TextLineWriter textFileOut;
  
  /** Is the output text file open? */
  private    boolean     textFileOutOpen = false;
  
  /** Number of lines written to the output text file so far. */
  private    int         textFileOutLineCount = 0;
  
  /** Number of times an output text file was successfully opened. */
  private    int         outputCommandCount = 0;
  
  /** Current settings for delimiters to indicate the start of a command. */
  private    String       nlStartCommand = "<<";
  
  /** Current settings for delimiters to indicate the end of a command. */
  private    String       nlEndCommand = ">>";
  
  /** Current settings for delimiters to indicate the start of a variable. */
  private    String       nlStartVariable = "<<";
  
  /** Current settings for delimiters to indicate the end of a variable. */
  private    String       nlEndVariable = ">>";
  
  /** Current setting for delimiters to indicate the start of variable modifiers. */
  private    String       nlStartModifiers = "&";
  
  /** Are we currently skipping template lines, due to an IF that returned false? */
  private    boolean      skippingData = false;
  
  /** Number of ENDIF commands we are looking for. */
  private    int          ifBypassDepth = 0;
  
  /** Last value of data tested by IFCHANGE command. */
  private    String       lastChangeData = "";
  
  /** Is there a non-blank list item pending? */
  private		 boolean			listItemPending = false;

  /** Are we producing an epub document. */
  private    boolean      epub = false;

  /** The site URL to use to replace relative references for an epub doc. */
  private    String       epubSite = "";
  
  /** Collection of global variables. */
  private    DataRecord   globals;
	
	/** Group data */
	public final static int MAX_GROUPS = 10;
	private int minorGroup = -1;
  private int listLevel = -1;
	private boolean[] endGroup    = new boolean [MAX_GROUPS];
	private boolean[] newGroup    = new boolean [MAX_GROUPS];
  private boolean[] newList     = new boolean [MAX_GROUPS];
  private boolean[] endList     = new boolean [MAX_GROUPS];
	private String[]  groupValue  = new String  [MAX_GROUPS];

  /** TextIO data for conversion of include file */
  private             TextIO              io;
  private             TextTree            tree;
  private             int                 tempCount = 0;
  
  private             StringConverter     noBreaksConverter = null;
  
  private  CommonMarkup      htmlConverter     = new CommonMarkup ("txt", "html");

  /**
     Constructs the utility collection.
   */
  public TemplateUtil () {
    this (new Logger (new LogOutput()));  
  }
  
  /**
     Constructs the utility collection with the passed Logger object.
    
     @param log Logger object to use for writing events to a log file.
   */
  public TemplateUtil (Logger log) {
    io = new TextIO ();
    this.log = log;
    event = new LogEvent();
    globals = new DataRecord ();
		resetGroupBreaks();
		resetGroupValues();
    // io.logTypes();
  }
  
  public void setDebug(boolean debug) {
    this.debug = debug;
  }
  
  public boolean getDebug() {
    return debug;
  }
  
  public boolean debugging() {
    return debug;
  }
  
  public void setWebRoot (File webRootFile) {
    this.webRootFile = webRootFile;
    if (webRootFile == null) {
      webRootFileName = null;
    } else {
      webRootFileName = new FileName (webRootFile);
    }
  }
  
  public File getWebRoot () {
    return webRootFile;
  }

  public void setEpub (boolean epub) {
    this.epub = epub;
  }

  public boolean isEpub () {
    return epub;
  }

  public void setEpubSite (String epubSite) {
    this.epubSite = epubSite;
  }

  public String getEpubSite () {
    return epubSite;
  }
  
  /**
     Tailor an event and send it to the log.
    
     @param severity Severity of the event.
    
     @param message Text description of the event.
    
     @param dataRelated Was the event related to some preceding data?
   */
  public void recordEvent (int severity, String message, boolean dataRelated) {
    tailorEvent (severity, message, dataRelated);
    // log.recordEvent (event); looks like bug
    recordEvent();
  }
  
  /**
     Tailor an event.
    
     @param severity Severity of the event.
    
     @param message Text description of the event.
    
     @param dataRelated Was the event related to some preceding data?
   */
  public void tailorEvent (int severity, String message, boolean dataRelated) {
    event.setSeverity (severity);
    event.setMessage (message);
    event.setDataRelated (dataRelated);
  }
  
  /**
     Send an event to the log. It should have been tailored previously.
   */
  public void recordEvent () {
    log.recordEvent (event);
  }
  
  public void sendIfStateDebugLine() {
    sendDebugLine("==> Skipping Data? " + String.valueOf(skippingData)
        + " If Bypass Depth: " + String.valueOf(ifBypassDepth));
  }
  
  public void sendDebugLine(String debugLine) {
    if (debug) {
      log.logDebugLine(debugLine);
    }
  }
  
  /**
     Returns the name of the last output text file opened
     in the last GenerateOutput execution.
    
     @return Name of output text file.
   */
  public FileName getTextFileOutName() {
    return textFileOutName;
  }
  
  /**
     Returns the last output text file opened
     in the last GenerateOutput execution.
    
     @return Output text file.
   */
  public TextLineWriter getTextFileOut() {
    return textFileOut;
  }
  
  public boolean openTemplate (File inTemplateFileSpec) {
    
    templateFileSpec = inTemplateFileSpec;
    // templateUtil.setTemplateFileName (templateFileSpec.getAbsolutePath());
    templateFile = new FileLineReader (templateFileSpec);

    templateFileOK = templateFile.open();
    templateFilePathAndName = inTemplateFileSpec.getAbsolutePath();
    templateFileSimpleName = inTemplateFileSpec.getName();
    setTemplateFileName (templateFileSimpleName);
    setTemplateFilePath (templateFilePathAndName.substring
      (0, (templateFilePathAndName.length() 
        - templateFileSimpleName.length())));
    setTemplateParent(templateFilePathAndName.substring 
      (0, (templateFilePathAndName.length() 
        - templateFileSimpleName.length() - 1)));

    templateFileAtEnd = (! templateFileOK);
    templateFileLineCount = 0;
    return templateFileOK;
  }
  
  /**
   Is the template file OK?
  
   @return True if the file's ok, false if we've run into an exception. 
  */
  public boolean isTemplateFileOK () {
    return templateFileOK;
  }
  
  /**
   Have we hit the end of all the template data, including any include files?
  
   @return True if at end, false if no end yet. 
  */
  public boolean isTemplateFileAtEnd() {
    return templateFileAtEnd;
  }
  
  /**
     Gets the next line in the template file, and returns
     it as a TemplateLine.
     
     @return The next line of the template.
   */
  public TemplateLine nextTemplateLine() {
    String        nextString = "";
    TemplateLine  nextLine = null;
    
    do {
      nextString = templateFile.readLine();
      if (templateFile.isAtEnd()) {
        templateFileAtEnd = true;
      } else {
        nextLine = new TemplateLine (nextString, this);
      }
    } while (nextLine == null && (! templateFileAtEnd));
    
    if (! templateFile.isOK()) {
      templateFileOK = false;
    }
    
    if (nextLine != null) {
      templateFileLineCount++;
    }
    return nextLine;
  } // end method nextTemplateLine
  
  public boolean closeTemplateFile() {

    templateFileOK = templateFile.close();

    return templateFileOK;
  }
  
  /**
     Returns the number of lines found in the input 
     template file read during the last GenerateOutput operation.</p>
      
     @return templateFileLineCount
   */
  public int getTemplateFileLineCount() {
    return templateFileLineCount;
  }
  
  /** 
     Returns the name of the input template file. 
    
     @return Name of the input template file. 
   */
  public String getTemplateFileName() { return templateFileName; }
  
  /**
     Returns the name of the folder containing the template file. 
    
     @return Name of the folder containing the template file.
   */
  public String getTemplateParent() { return templateParent; }
  
  /** 
     Returns the directory path to the input template file. 
    
     @return Directory path to the input template file. 
   */
  public String getTemplateFilePath() { return templateFilePath; }
  
  public void setFirstTemplateLine (boolean first) {
    firstTemplateLine = first;
  }
  
  public boolean isFirstTemplateLine () {
    return firstTemplateLine;
  }
  
  /**
     Returns the number of lines written to the last output 
     file generated during the last GenerateOutput operation.</p>
     
   @return textFileOutLineCount
   */
  public int getTextFileOutLineCount() {
    return textFileOutLineCount;
  }
  
  /**
     Returns the name of the data file, formatted for display. 
    
     @return Name of the data file.
   */
  public String getDataFileDisplay() { return dataFileDisplay; }

  /**
   Returns the name of the data file base name, without any file extension.

   @return Name of the data file base name.
   */
  public String getDataFileBaseName() { return dataFileBaseName; }
  
  /**
     Returns the name of the folder containing the data file. 
    
     @return Name of the folder containing the data file.
   */
  public String getDataParent() { return dataParent; }
  
  /**
   Returns the name of the folder containing the data file. 
  
   @return The name of the folder, without any of its parent path info. 
  */
  public String getDataParentFolder() { return dataParentFolder; }
  
  /**
     Returns the skippingData variable.
    
     @return Are we skipping template lines until the next ENDIF? 
   */
  public boolean isSkippingData() { return skippingData; }
  
  /** 
     Returns the current delimiters used to start a command.
     
     @return Delimiters that identify the start of a command.
   */
  public String getNlStartCommand() { return nlStartCommand; } 
  
  /** 
     Returns the current delimiters used to end a command.
     
     @return Delimiters that identify the end of a command.
   */
  public String getNlEndCommand() { return nlEndCommand; }
  
  /** 
     Returns the current delimiters used to start a variable.
     
     @return Delimiters that identify the start of a variable.
   */
  public String getNlStartVariable() { return nlStartVariable; }
  
  /** 
     Returns the current delimiters used to end a variable.
     
     @return Delimiters that identify the end of a variable.
   */
  public String getNlEndVariable() { return nlEndVariable; }
  
  /** 
     Returns the current delimiters used to start the variable modifiers.
     
     @return Delimiters that identify the start the variable modifiers.
   */
  public String getNlStartModifiers() { return nlStartModifiers; }
  
  /** 
     Determines whether there is a non-blank list item pending.
     
     @return True if there is a non-blank list item pending.
   */
  public boolean isListItemPending() { return listItemPending; }
  
  /**
     Returns the collection of global variables.
    
     @return The global variables.
   */
  public DataRecord getGlobals() { return globals; }
  
	/**
	   Reset all group breaks to their off position.
	 */
	public void resetGroupBreaks() {
	  for (int i = 0; i < MAX_GROUPS; i++) {
			endGroup [i] = false;
			newGroup [i] = false;
      endList  [i] = false;
      newList  [i] = false;
		}
	}
	
	/**
	   Reset all group values to blanks.
	 */
	public void resetGroupValues() {
	  for (int i = 0; i < MAX_GROUPS; i++) {
			groupValue[i] = "";
		}
	}
	
	/**
	   Set a new group value from DEFINEGROUP command.
	 */
	public void setGroup (int groupNumber, String inputValue) {
		String nextValue = inputValue.trim();

		if (groupNumber > minorGroup) {
			minorGroup = groupNumber;
		}
		if (groupValue[groupNumber].equals(nextValue)) {
      // No change -- do nothing
		}
		else {
			setEndGroupsTrue (groupNumber);
			if (nextValue.length() > 0) {
				newGroup[groupNumber] = true;
        if (groupValue[groupNumber].length() == 0) {
          newList[groupNumber] = true;
        }
			} else {
        endList[groupNumber] = true;
      }
      // reset lower level break values, forcing a new group if non-null
      for (int i = groupNumber + 1; i <= minorGroup; i++) {
        if (groupValue[i].length() > 0) {
          endList[i] = true;
          groupValue[i] = "";
        }
      } 
			groupValue[groupNumber] = nextValue;
		} // end break condition
	}
	
	/**
	   Set end group breaks.
	  
	   @param majorGroup - Group number to start with. 
	 */
	public void setEndGroupsTrue (int majorGroup) {
		for (int i = majorGroup; i <= minorGroup; i++) {
			if (groupValue[i].length() > 0) {
				endGroup[i] = true;
			} // end if non-null prior group value
		} // end for loop through end group breaks
	}
	
	/**
	   Process an IFENDGROUP command.
	  
     @param groupNumber - index to group being processed.
   */
  public void setIfEndGroup (int groupNumber) { 
    setSkippingData 
      (! endGroup [groupNumber]);
  }
	
	/**
	   Process an IFNEWGROUP command.
	  
     @param groupNumber - index to group being processed.
   */
  public void setIfNewGroup (int groupNumber) { 
    setSkippingData 
      (! newGroup [groupNumber]);
  }
  
  public void setIfEndList (int groupNumber) {
    
    setSkippingData
        (! endList [groupNumber]);
  }
  
  public void setIfNewList (int groupNumber) {

    setSkippingData
        (! newList [groupNumber]);
  }
  
  /**
     Increment ifBypassDepth by +1.
     
     This method should be called every time an inactive IF command
     is encountered as part of the TemplateLine.generateOutput processing.
     
     In general, as we process template lines, we need to keep track of
     two state conditions relative to conditional processing. If we are within
     the scope of a conditional that has returned false, then we need to know
     to skip all ouput lines until we hit the matching ENDIF command or
     its equivalent (some sort of group command). The skippingData variable
     is used to indicate that we are within the scope of a negative 
     conditional.
     
     The second state variable we need to keep track of is the number of 
     nested conditionals we have run across while inside the scope of a
     conditional that has returned false. As we end conditional blocks, we
     need to decrement this counter so that we know when we have found the
     matching ENDIF command (or its equivalent). The ifBypassDepth variable
     is used to keep track of this number. 
   */
  public void anotherIf () {
    ifBypassDepth++;
  }
  
  /**
    Clear all pending conditionals.
    
    This method should be called for commands that should
    never be found within the scope of a conditional block.
   */
  public void clearIfs () {
    ifBypassDepth = 0;
    skippingData = false;
    sendDebugLine("Clearing Ifs");
  }
  
  /**
   Process an else command. 
   */
  public void anElse () {
    if (skippingData && ifBypassDepth > 0) {
      // If we bypassed the If, then bypass the Else as well,
      // but we're still looking for an Endif
    } else {
      boolean skippingBeforeElse = isSkippingData();
      setSkippingData (! skippingBeforeElse);
    }
  }
  
  /**
     Decrement ifBypassDepth by +1.
     
     This method should be called every time an ENDIF command
     is encountered as part of the TemplateLine.generateOutput processing.
   */
  public void anotherEndIf () {
    if (ifBypassDepth > 0) {
      ifBypassDepth--;
    } else {
      setSkippingData (false);
    }
  } // end method anotherEndIf
  
  /**
     Sets skippingData on if data has not changed since last time through.
     Sets lastChangeData to the latest value. 
    
     @param ifChangeData Data found as parameter of IFCHANGE command.
   */
  public void setIfChangeData (String ifChangeData) { 
    setSkippingData 
      (ifChangeData.equals (lastChangeData));
    lastChangeData = ifChangeData;
  }
  
  /**
     Sets the skippingData variable.
     
     This method should be called every time an active IF command
     is encountered as part of the TemplateLine.generateOutput processing.
    
     @param skippingData Should we skip subsequent template lines? 
                         Note that this value would be the logical 
                         opposite of the result of an if evaluation.
   */
  public void setSkippingData (boolean skippingData) {
    this.skippingData = skippingData;
  }
  
  /**
     Sets the name of the input template file.
    
     @param templateFileName Name of the input template file.
   */
  public void setTemplateFileName (String templateFileName) {
    this.templateFileName = templateFileName;
  }
  
  /**
     Sets the directory path to the input template file.
    
     @param templateFilePath Directory path to the input template file.
   */
  public void setTemplateFilePath (String templateFilePath) {
    this.templateFilePath = templateFilePath;
  }
  
  /**
     Sets the name of the folder containing the template file.
    
     @param templateParent Name of the folder containing the template file.
   */
  public void setTemplateParent (String templateParent) {
    this.templateParent = templateParent;
  }
  
  /**
     Sets the name of the input data file, formatted for display.
    
     @param dataFileDisplay Name of the input data file, formatted for display.
   */
  public void setDataFileDisplay (String dataFileDisplay) {
    this.dataFileDisplay = dataFileDisplay;
  }

  /**
   Sets the name of the input data file, without any file extension.

   @param dataFileBaseName Name of the input data file, without any
                           file extension. 
   */
  public void setDataFileBaseName (String dataFileBaseName) {
    this.dataFileBaseName = dataFileBaseName;
  }
  
  /**
     Sets the name of the folder containing the input data file.
    
     @param dataParent Name of the folder containing the input data file.
   */
  public void setDataParent (String dataParent) {
    this.dataParent = dataParent;
    if (dataParent != null && dataParent.length() > 0) {
      FileName folderName = new FileName(dataParent);
      dataParentFolder = folderName.getFolder();
    } else {
      dataParentFolder = "";
    }
    
  }
  
  /**
     Sets the name of the output text file. Closes the last output
     text file, if one is open. Attempts to open the new output
     text file. If unsuccessful, writes an event to the log. If successful,
     increments outputCommandCount by 1. 
    
     @param textFileOutName New name for the output text file.
   */
  public void setTextFileOutName (String textFileOutName) {
    this.textFileOutName = new FileName (textFileOutName, FileName.FILE_TYPE);
    close();
    if (! textFileOutName.startsWith ("/")) {
      FileName path = new FileName (templateFilePath);
      this.textFileOutName = new FileName (path.resolveRelative(textFileOutName));
    }
    if (this.webRootFile == null) {
      relativePathToRoot = null;
    } 
    else
    if (this.textFileOutName.isBeneath(webRootFileName)) {
      relativePathToRoot = new StringBuilder();
      for (int i = this.textFileOutName.getNumberOfFolders();
               i > webRootFileName.getNumberOfFolders();
               i--) {
        relativePathToRoot.append("../");
      }
    } else {
      relativePathToRoot = null;
    }
    textFileOut = new FileMaker
      (this.textFileOutName.toString());
    outputCommandCount++;
    boolean ok = textFileOut.openForOutput();
    if (ok) {
      textFileOutOpen = true;
    } else {
      recordEvent (LogEvent.MAJOR, 
        "Attempt to Open File " + textFileOutName + " was unsuccessful", false);
    }
  } // end method setTextFileOutName
  
  /**
   Return the path to the web root folder relative to the current output file. 
  
   @return A relative path (i.e., '../../../') or null if the output file is
           not beneath the web root folder. 
  */
  public String getRelativePathToRoot() {
    if (relativePathToRoot == null) {
      return null;
    } else {
      return relativePathToRoot.toString();
    }
  }
  
  public String noBreaks (String inStr) {
    if (noBreaksConverter == null) {
      noBreaksConverter = new StringConverter();
      noBreaksConverter.add(StringConverter.HTML_BREAK, "");
      noBreaksConverter.add(StringConverter.HTML_BREAK_2, "");
      noBreaksConverter.add(StringConverter.HTML_BREAK_3, "");
    }
    return (noBreaksConverter.convert(inStr));
  }
  
  /**
     Includes the contents of the named include file into the output file stream. 
    
     @param includeFileNameStr Name of the text file to be included.
     @param includeParm     Optional parameter to modify how the include
                            file is processed. 
                            * copy - indicates the include file should be copied
                                     as-is, without any modifications. 
   */
  public void includeFile (String includeFileNameStr, 
                           String includeParm, 
                           DataRecord dataRec) {

    // Make sure we have a complete file name
    TextLineReader includeFile;
    if (includeFileNameStr.startsWith("file:")) {
      includeFile = new FileLineReader (includeFileNameStr);
    }
    else
    if (includeFileNameStr.startsWith ("/")) {
      includeFile = new FileLineReader (includeFileNameStr);
    } else {
      FileName templatePathFileName = new FileName(templateFilePath);
      String resolved = templatePathFileName.resolveRelative(includeFileNameStr);
      includeFile = new FileLineReader (resolved);
    }
    
    recordEvent (LogEvent.NORMAL, "Including file " + includeFile.toString(), false);
    
    boolean converted = false;
    FileLineReader includeFileReader = (FileLineReader)includeFile;
    File incFile = includeFileReader.getFile();
    if (incFile != null && (! incFile.exists())) {
      recordEvent (LogEvent.MEDIUM, 
          "File " + includeFile.toString() + " not found", false);
    } else {

      // See if include file should be converted
      FileName includeFileName = new FileName (includeFile.toString());
      FileName textFileOutFileName = new FileName (textFileOut.getDestination());
      String inExt = includeFileName.getExt();
      String outExt = textFileOutFileName.getExt();
      TextIOType inType = io.getType (inExt, "Input", false);
      TextIOType outType = io.getType (outExt, "Output", true);
      if ((includeParm == null
          || includeParm.length() == 0
          || (! includeParm.equalsIgnoreCase(INCLUDE_COPY)))
          && inExt.length() > 0
          && outExt.length() > 0
          // && inType != null
          // && outType != null
          && ((! inExt.equalsIgnoreCase (outExt))
            // || inExt.equalsIgnoreCase("html")  ???
          )) {
        // The stars are aligned: let's try to convert from the input format
        // to the intended output format (typically markdown to html).
        if (isMarkdown(inExt) && isHTML(outExt)) {
          // Convert markdown to HTML
          StringBuilder md = new StringBuilder();
          while (! includeFile.isAtEnd()) {
            String line = includeFile.readLine();
            if (line != null && (! includeFile.isAtEnd())) {
              md.append(line);
              md.append(GlobalConstants.LINE_FEED);
            }
          } 
          String html = MdToHTML.getShared().markdownToHtml(md.toString());
          TextLineReader htmlReader = new StringLineReader(html);
          includeFile = htmlReader;
        }
        else
        if (isMarkdown(inExt) 
            && (! isMarkdownTOC(inExt))
            && isMarkdownTOC(outExt)) {
          try {
            File temp
              = File.createTempFile
                ("pstm_include_temp_" + String.valueOf (tempCount++),
                      "." + outExt);
            // Delete temp file when program exits.
            temp.deleteOnExit();
            FileMaker writer = new FileMaker (temp);
            AddToCtoMarkdown addToC = new AddToCtoMarkdown();
            int startHeadingLevel = 2;
            int endHeadingLevel = 4;
            char parmStart = '2';
            char parmEnd   = '4';
            if (includeParm.length() > 0) {
              parmStart = includeParm.charAt(0);
            }
            if (includeParm.length() > 1) {
              parmEnd = includeParm.charAt(includeParm.length() - 1);
            }
            if (Character.isDigit(parmStart)
                && parmStart > '0'
                && parmStart < '7') {
              startHeadingLevel = Character.getNumericValue(parmStart);
            }
            if (Character.isDigit(parmEnd)
                && parmEnd > '0'
                && parmEnd < '7'
                && (! (parmEnd < parmStart))) {
              endHeadingLevel = Character.getNumericValue(parmEnd);
            }

            addToC.transformNow(includeFile, writer, 
                startHeadingLevel, endHeadingLevel);
            converted = true;
            if (converted) {
              recordEvent (LogEvent.NORMAL,
                "Added Table of Contents to "
                  + includeFile.toString(),
                  false);
              includeFile = new FileLineReader (temp);
            } // end if stored successfully
          } catch (IOException e) {
            System.out.println("I/O Exception");
            converted = false;
          }
        } 
        else 
        if (inType != null && outType != null) { 
          // Use pspub routines for other conversions, such as Textile to HTML
          TextData rootData = new TextData();
          rootData.setType (TextType.LOCATION_FILE);
          rootData.setText (includeFile.toString());
          tree = new TextTree (rootData);
          try {
            URL url = includeFileReader.toURL();
            converted = io.load (tree, url, inType, includeParm);
            if (converted) {
              File temp
                  = File.createTempFile
                    ("pstm_include_temp_" + String.valueOf (tempCount++),
                      "." + outExt);
              // Delete temp file when program exits.
              temp.deleteOnExit();
              FileMaker writer = new FileMaker (temp);
              converted = io.store (tree, writer, outType, epub, epubSite);
              if (converted) {
                recordEvent (LogEvent.NORMAL,
                    "Converted Include file "
                      + includeFile.toString()
                      + " from "
                      + inType.getLabel()
                      + " to "
                      + outType.getLabel(),
                      false);
                includeFile = new FileLineReader (temp);
              } // end if stored successfully
            } // end if loaded successfully
          } catch (MalformedURLException e) {
            converted = false;
          } catch (IOException e) {
            converted = false;
          }
        } // end try
      } // end if using a pspub routine
 
      boolean inOK = includeFile.open();
      if (inOK) {
        String includeLine = includeFile.readLine(); 
        while (! includeFile.isAtEnd()) { 
          LineWithBreak lineWithBreak = replaceVariables
            (new StringBuilder(includeLine), dataRec);
          if (lineWithBreak.getLineBreak()) {
            writeLine (lineWithBreak.getLine());
          } else {
            write (lineWithBreak.getLine());
          }
          includeLine = includeFile.readLine(); 
        } 
        includeFile.close(); 
      } else {
        recordEvent (LogEvent.MEDIUM, 
          "Attempt to Open Include File " + includeFileNameStr + " was unsuccessful",
            true);
      } 
      
    } // end if include file exists
    
  } // end method setTextFileOutName
  
  public static boolean isMarkdown(String ext) {
    return (ext.equalsIgnoreCase("md")
              || ext.equalsIgnoreCase("markdown")
              || ext.equalsIgnoreCase("mdown")
              || ext.equalsIgnoreCase("mkdown")
              || ext.equalsIgnoreCase("mdtoc"));
  }
  
  public static boolean isHTML(String ext) {
    return (ext.equalsIgnoreCase("htm")
        || ext.equalsIgnoreCase("html"));
  }
  
  public static boolean isMarkdownTOC(String ext) {
    return (ext.equalsIgnoreCase("mdtoc"));
  }
  
  /**
     Writes a String to the output text file, if one is open.
    
     @param outString Text to be written to the output text file.
   */
  public void write (String outString) {  
    if (textFileOutOpen) {
      boolean ok = textFileOut.write (outString);
      if (! ok) {
      } // end catch
    } // end if file is open
  } // end method write
  
  /**
     Writes a line to the output text file, if one is open.
    
     @param outString Line to be written to the output text file.
   */
  public void writeLine (String outString) {  
    if (textFileOutOpen) {
      boolean ok = textFileOut.writeLine (outString);
      if (! ok) {
      } // end catch
    } // end if file is open
  } // end method writeLine
  
  /**
     Closes the output text file, if one is open.
   */
  public void close() {
    if (textFileOutOpen) {
      // textFileOutLineCount = textFileOut.getLineNumber();
      boolean ok = textFileOut.close();
    } // end if open
  } // end method close
  
  /**
     Is an output text file currently open?
    
     @return True if an output text file is ready for output.
   */
  public boolean isTextFileOutOpen() { return textFileOutOpen; }
  
  /**
     Returns outputCommandCount.
    
     @return The number of output commands found in the template
             file.
   */
  public int getOutputCommandCount() { return outputCommandCount; }
  
  /**
     Sets the new delimiters used to start a command.
    
     @param newValue Delimiters used from this point on to identify the
                     start of a new command.
   */
  public void setNlStartCommand(String newValue) { nlStartCommand = newValue; } 
  
  /**
     Sets the new delimiters used to end a command.
    
     @param newValue Delimiters used from this point on to identify the
                     end of a command.
   */
  public void setNlEndCommand(String newValue) { nlEndCommand = newValue; }
  
  /**
     Sets the new delimiters used to start a variable.
    
     @param newValue Delimiters used from this point on to identify the
                     start of a new variable.
   */
  public void setNlStartVariable(String newValue) { nlStartVariable = newValue; }
  
  /**
     Sets the new delimiters used to end a variable.
    
     @param newValue Delimiters used from this point on to identify the
                     end of a variable.
   */
  public void setNlEndVariable(String newValue) { nlEndVariable = newValue; }
  
  /**
     Sets the new delimiters used to start the variable modifiers.
    
     @param newValue Delimiters used from this point on to identify the
                     start of variable modifiers.
   */
  public void setNlStartModifiers(String newValue) { nlStartModifiers = newValue; }
  
  /**
     Sets the list Item Pending flag.
    
     @param listItemPending Indicator that there is a non-blank list item pending.
   */
  public void setListItemPending (boolean listItemPending) { 
    this.listItemPending = listItemPending; 
  }

  /**
     Return this object as some kind of string.
    
     @return Name of class plus name of template file.
   */
  public String toString () {
    return ("TemplateUtil Text File Name is "
      + templateFileName.toString ());
  }
  
  /**
   Replace any variables found in passed data. 
  
   @param str The StringBuilder to have its variables replaced. 
  */
  public LineWithBreak replaceVariables (StringBuilder str, DataRecord dataRec) {
    
    LineWithBreak lineWithBreak = new LineWithBreak();
    int varIndex = 0;
    while ((varIndex >= 0) && (varIndex < str.length())) {
      // find the beginning of the next variable
      int startDelim = str.indexOf (nlStartVariable, varIndex);
      // If a variable starting delimiter also begins 1 character to the right,
      // then use that instead
      if (startDelim >= 0) {
        int startDelim2 = str.indexOf (nlStartVariable, startDelim + 1);
        if (startDelim2 == (startDelim + 1)) {
          startDelim = startDelim2;
        }
      }
      if (startDelim < 0) {
        varIndex = startDelim;
      } else {
        // if beginning found, now find the end
        int endDelim = str.indexOf 
          (nlEndVariable, startDelim + nlStartVariable.length() + 1);
        if (endDelim < 0) {
          varIndex = endDelim;
        } else {
          // found beginning and end of variable -- process it
          int endVar = endDelim;
          // find beginning of variable modifiers, if any
          int startMods = str.indexOf
            (nlStartModifiers, startDelim + nlStartVariable.length() + 1);
          int leadingCount = 0;
          int caseCode = 0;
          boolean initialCase = false;
          char listSep = ' ';
          boolean formatStringFound = false;
          boolean underscoreFound = false;
          StringBuilder formatStringBuf = new StringBuilder();
          Date date = null;

          boolean xml = false;
          boolean html = false;
          boolean markdown = false;
          boolean fileBaseName = false;
          boolean keepRight = false;
          boolean convertLinks = false;
          boolean makeFileName = false;
          boolean makeFileNameReadable = false;
          boolean noBreaks = false;
          boolean noPunctuation = false;
          boolean emailPunctuation = false;
          boolean digitToLetter = false;
          boolean linkedTags = false;
          boolean replaceAgain = false;
          boolean summary = false;
          boolean vary = false;
          char varyDelim = ' ';
          StringBuilder varyBuf = new StringBuilder();
          String varyFrom = "";
          String varyTo = "";
          String formatString;
          
          boolean demarcation = false;
          int firstCase = 0;
          int leadingCase = 0;
          int normalCase = 0;
          int caseCount = 0;
          StringBuilder delimiter = new StringBuilder();
          
          // if we found any variable modifiers, then collect them now
          if ((startMods > 0) && (startMods < endDelim)) {
            endVar = startMods;
            for (int i = startMods + 1; i < endDelim; i++) {
              char workChar = str.charAt (i);
              if (formatStringFound) {
                formatStringBuf.append (workChar);
              } else
              if (vary) {
                if (varyDelim == ' ') {
                  varyDelim = workChar;
                } else
                if (workChar == varyDelim && varyBuf.length() > 1) {
                  varyFrom = varyBuf.toString();
                  varyBuf = new StringBuilder();
                  varyTo = "";
                } else {
                  varyBuf.append(workChar);
                  varyTo = varyBuf.toString();
                }
              } else
              if (Character.toLowerCase(workChar) == 'v') {
                vary = true;
              } else
              if (Character.toLowerCase (workChar) == 'c') {
                demarcation = true;
              } else
              if (demarcation) {
                int wordCase = -2;
                if (Character.toLowerCase (workChar) == 'u') {
                  wordCase = 1;
                } else
                if (Character.toLowerCase (workChar) == 'l') {
                  wordCase = -1;
                } else
                if (Character.toLowerCase (workChar) == 'a') {
                  wordCase = 0;
                }
                if (wordCase > -2) {
                  caseCount++;
                  switch (caseCount) {
                    case 1:
                      firstCase = wordCase;
                      break;
                    case 2:
                      leadingCase = wordCase;
                      break;
                    default:
                      normalCase = wordCase;
                      break;
                  }
                } else {
                  delimiter.append (workChar);
                }
              } else
              if (Character.isDigit (workChar)) {
                leadingCount = (leadingCount * 10)
                  + Character.getNumericValue (workChar);
              } else
              if (workChar == '\'') {
                emailPunctuation = true;
              }
              else
              if (Character.toLowerCase(workChar) == 'f') {
                makeFileName = true;
              } else
              if (makeFileName 
                  && (! makeFileNameReadable)
                  && Character.toLowerCase(workChar) == 'r') {
                makeFileNameReadable = true;
              } else
              if (Character.toLowerCase (workChar) == 'l') {
                caseCode = -1;
              } else
              if (Character.toLowerCase (workChar) == 'u') {
                caseCode = +1;
              } else
              if (Character.toLowerCase (workChar) == 'i') {
                initialCase = true;
              } else
              if (Character.toLowerCase(workChar) == 's') {
                summary = true;
              }
              else
              if (Character.toLowerCase (workChar) == 'x') {
                xml = true;
              } else
              if (workChar == 'h') {
                html = true;
              } else
              if (Character.toLowerCase(workChar) == 'o') {
                markdown = true;
              } else
              if (Character.toLowerCase (workChar) == 'b') {
                fileBaseName = true;
              } else
              if (Character.toLowerCase (workChar) == 'r') {
                keepRight = true;
              } else
              if (Character.toLowerCase(workChar) == 'j') {
                convertLinks = true;
              }
              else
              if (Character.toLowerCase(workChar) == 'n') {
                noBreaks = true;
              } else
              if (Character.toLowerCase(workChar) == 'p') {
                noPunctuation = true;
              } else
              if (Character.toLowerCase(workChar) == 't') {
                digitToLetter = true;
              } else
              if (Character.toLowerCase(workChar) == 'g') {
                linkedTags = true;
              }
              else
              if (Character.isLetter (workChar)) {
                formatStringFound = true;
                formatStringBuf.append (workChar);
              } else
              if (workChar == '_') {
                underscoreFound = true;
              } else
              if (! Character.isLetterOrDigit (workChar)) {
                listSep = workChar;
              }
            }
          } // end of variable modifier processing
          
          // get variable name and replacement value
          String variable = str.substring
            ((startDelim + nlStartVariable.length()), endVar);
          CommonName common = new CommonName (variable);
          variable = common.getCommonForm();
          String replaceData = GlobalConstants.EMPTY_STRING;
          if (variable.equals (NO_LINE_BREAK)) {
            lineWithBreak.setLineBreak(false);
          } else
          if (variable.equals (TEMPLATE_FILE_NAME_VARIABLE)) {
            replaceData = getTemplateFileName();
          } else
          if (variable.equals (TEMPLATE_PARENT_NAME_VARIABLE)) {
            replaceData = getTemplateParent();
          } else
          if (variable.equals (DATA_FILE_NAME_VARIABLE)) {
            replaceData = getDataFileDisplay();
          } else
          if (variable.equals (DATA_FILE_BASE_NAME_VARIABLE)) {
            replaceData = getDataFileBaseName();
          }else
          if (variable.equals (DATA_PARENT_NAME_VARIABLE)) {
            replaceData = getDataParent();
          } else 
          if (variable.equals (DATA_PARENT_FOLDER_VARIABLE)) {
            replaceData = getDataParentFolder();
          } else
          if (variable.equals (TODAYS_DATE_VARIABLE)) {
            date = Calendar.getInstance().getTime();
          } else
          if (variable.equals (RELATIVE_VARIABLE)) {
            replaceData = getRelativePathToRoot();
          } else
          if (globals.containsField (variable)) {
            replaceData = globals.getFieldData (variable);
          } else {
            replaceData = dataRec.getFieldData (variable);
          }
          
          // transform replacement value according to variable modifiers
          if ((replaceData != null) 
              && ((replaceData.length() > 0) 
                || (variable.equals(RELATIVE_VARIABLE)))
              ) {
            if (digitToLetter) {
              try {
                int digit = Integer.parseInt(replaceData);
                if (digit > 0 && digit <= 26) {
                  replaceData = String.valueOf((char)(digit + 'A' - 1));
                }
              } catch (NumberFormatException e) {
                // do nothing
              }
            } // end digit to letter
            if (summary) {
              int max = 250;
              if (leadingCount > 0) {
                max = leadingCount;
              }
              if (replaceData.length() > max) {
                int sentenceCount = 0;
                int endOfLastSentence = 0;
                int lastSpace = 0;
                int i = 0;
                char c = ' ';
                char lastChar = ' ';
                
                while (i < max) {
                  lastChar = c;
                  c = replaceData.charAt(i);
                  if (c == ' ') {
                    lastSpace = i;
                    if (lastChar == '.') {
                      endOfLastSentence = i;
                      sentenceCount++;
                    } // end if end of sentence
                  } // end if space
                  i++;
                } // end of characters within summarization range
                if (sentenceCount > 0) {
                  replaceData = replaceData.substring(0, endOfLastSentence);
                } else {
                  replaceData = replaceData.substring(0, lastSpace) + "....";
                }
              } // end if we have any need to summarize at all
            } // end if summarization requested
            else
            if (leadingCount > 0) {
              if (leadingCount < replaceData.length()) {
                if (keepRight) {
                  replaceData = replaceData.substring (replaceData.length() - leadingCount);
                } else {
                  replaceData = replaceData.substring (0, leadingCount);
                }
              } else {
                while (leadingCount > replaceData.length()) {
                  replaceData = "0" + replaceData;
                }
              }
            } // end if leadingCount > 0
            
            if (initialCase) {
              StringBuilder work = new StringBuilder ("");
              if (replaceData.length() > 0) {
                if (caseCode > 0) {
                  work.append (replaceData.substring(0,1).toUpperCase());
                } else
                if (caseCode < 0) {
                  work.append (replaceData.substring(0,1).toLowerCase());
                } else {
                  work.append (replaceData.substring(0,1));
                }
                if (replaceData.length() > 1) {
                  work.append (replaceData.substring (1));
                }
              } // end if replaceData length > 0
              replaceData = work.toString();
            } else {
              if (caseCode > 0) {
                replaceData = replaceData.toUpperCase();
              } 
              else
              if (caseCode < 0) {
                replaceData = replaceData.toLowerCase();
              }
            } // end if not initialCase
            
            if (makeFileNameReadable) {
              replaceData = StringUtils.makeReadableFileName(replaceData.trim());
            } else
            if (makeFileName) {
              replaceData = StringUtils.makeFileName(replaceData.trim(), false);
            }
            
            if (underscoreFound) {
              replaceData = StringUtils.replaceChars 
                  (replaceData.trim(), " ", "_");
            }
            if (demarcation) {
              replaceData = StringUtils.wordDemarcation 
                  (replaceData, delimiter.toString(), firstCase, leadingCase, normalCase);
            }
            if (noBreaks) {
              replaceData = noBreaks(replaceData);
            }
            if (noPunctuation) {
              replaceData = StringUtils.purifyPunctuation(replaceData);
            }
            if (linkedTags) {
              Tags tags = new Tags(replaceData);
              replaceData = tags.getLinkedTags("=$relative$=tags/");
              replaceAgain = true;
            }
          } // end if replaceData non-blank
          
          if (listSep == ' ') {
            setListItemPending (false);
          } 
          else 
          if ((replaceData != null) && (replaceData.length() > 0)) {
            if (isListItemPending()) {
              if (listSep == '/' || listSep == '\\') {
                replaceData = String.valueOf(listSep) + replaceData;
              } else {
                replaceData = String.valueOf(listSep) + " " + replaceData;
              }
            }
            setListItemPending (true);
          }
          
          if (date != null || 
              (formatStringFound
              && replaceData != null
              && replaceData.length() > 0)) {
            if (date == null) {
              StringDate dateString = new StringDate();
              dateString.parse(replaceData);
              Calendar cal = dateString.getCalendar();
              if (cal != null) {
                date = cal.getTime();
              } else {
                date = new Date();
              }
              // StringScanner dateString = new StringScanner (replaceData);
              // date = dateString.getDate("mdy");
            }
            if (formatStringBuf.length() > 0) {
              formatString = formatStringBuf.toString();
            } else {
              formatString = DEFAULT_DATE_FORMAT;
            }
            
            try {
              SimpleDateFormat dateFormat = new SimpleDateFormat (formatString);
              replaceData = dateFormat.format (date);
            } catch (IllegalArgumentException e) {
              replaceData = "";
            }
          }
          
          if (markdown) {
            replaceData = MdToHTML.getShared().markdownToHtml(replaceData);
          }
          
          if (xml) {
            StringConverter xmlConverter = StringConverter.getXML();
            replaceData = xmlConverter.convert (replaceData);
          }

          if (html) {
            replaceData = htmlConverter.markup (replaceData, true);
          }
          
          if (convertLinks) {
            replaceData = StringUtils.convertLinks (replaceData);
          }
          
          if (fileBaseName) {
            FileName fn = new FileName (replaceData);
            replaceData = fn.getBase();
          }
          
          if (emailPunctuation) {
            replaceData = emailQuotes(replaceData);
          }

          if (vary && (varyFrom.length() > 0)) {
            StringBuilder varyStr = new StringBuilder(replaceData);
            int i = 0;
            while ((i >= 0) && (i < varyStr.length())) {
              int start = i;
              i = varyStr.indexOf(varyFrom, start);
              if (i < 0) {
                // We're done
              } else {
                varyStr.delete(i, (i + varyFrom.length()));
                varyStr.insert(i, varyTo);
                i = i + varyTo.length();
              }
            } // End while searching for variances
            replaceData = varyStr.toString();
          } // End if we found a variance modifier
          
          // now perform the variable replacement
          if (replaceData != null) {
            str.delete(startDelim, endDelim + nlEndVariable.length());
            str.insert(startDelim, replaceData);
            if (replaceAgain) {
              varIndex = startDelim;
            } else {
              varIndex = startDelim + replaceData.length();
            }
          } else {
            varIndex = endDelim + nlEndVariable.length();
          }
        } // end processing when ending delimiters found
      } // end processing when starting delimiters found
    } // end processing of all variables in line
    
    // Check for a back slash at the end of the line
    // If found, replace with a space, to ensure two spaces
    // Which will generat a line break when converting Markdown to HTML
    if (str.length() > 2
        && str.charAt(str.length() - 1) == '\\'
        && str.charAt(str.length() - 2) == ' ') {
      str.deleteCharAt(str.length() - 1);
      str.append(' ');
    }
    
    lineWithBreak.setLine(str);
    return lineWithBreak;
  }
  
  public String emailQuotes (String html) {
    StringBuilder str = new StringBuilder(html);
    int i = 0;
    while (i >= 0 && i < str.length()) {
      int j = str.indexOf("&", i);
      i = j;
      if (j >= 0) {
        i = j + 1;
        int k = str.indexOf(";", j + 2);
        if (k >= 0) {
          String entity = str.substring(j, k + 1);
          if (entity.length() > 2 && entity.length() <= 8) {
            i = k + 1;
            String repl = "";
            if (entity.equals("&lsquo;")
                || entity.equals("&rsquo;")
                || entity.equals("&#8217;")
                || entity.equals("&#x2019;")
                || entity.equals("&#39;")) {
              repl = "'";
            }
            if (repl.length() > 0) {
              str.delete(j, k + 1);
              str.insert(j, repl);
              i = j + repl.length();
            }
          } // end if we found an entity of a suitable length
        } // end if we found an ending semi-colon
      } // end if we found a starting ampersand
    }
    i = 0;
    while (i >= 0 && i < str.length()) {
      int j = str.indexOf("’", i);
      if (j >= 0) {
        str.delete(j, j + 1);
        str.insert(j, "'");
        i = j + 1;
      } else {
        i = j;
      }
    }
    return str.toString();
  }
}

