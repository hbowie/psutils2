/*
 * Copyright 1999 - 2014 Herb Bowie
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

package com.powersurgepub.psutils2.txbio;

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.elements.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.markup.*;
  import com.powersurgepub.psutils2.mkdown.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.textio.*;

  import java.io.*;
  import java.net.*;
  import java.text.*;
  import java.util.*;

/**
 Reads a markdown file, extracts its metadata, and converts its content to
 HTML. 

 @author Herb Bowie
 */
public class MetaMarkdownReader 
    implements  DataSource {
  
  public static final String    COMPLETE_PATH   = "Complete Path";
  public static final String    BASE_PATH       = "Base Path";
  public static final String    LOCAL_PATH      = "Local Path";
  public static final String    PATH_TO_TOP     = "Path to Top";
  public static final String    DEPTH           = "Depth";
  public static final String    FILE_NAME       = "File Name";
  public static final String    FILE_NAME_BASE  = "File Name Base";
  public static final String    FILE_EXT        = "File Ext";
  public static final String    LAST_MOD_DATE   = "Last Mod Date";
  public static final String    FILE_SIZE       = "File Size";
  public static final String    STATUS          = "Status";
  public static final String    BREADCRUMBS     = "Breadcrumbs";
  public static final String    TAGS            = "Tags";
  public static final String    LINKED_TAGS     = "Linked Tags";
  public static final String    SINGLE_TAG      = "Tag";
  
  public static final String    UP_ONE_FOLDER   = "../";
  
  /** The type of data set to generate: markdown or tagStr. */
  private    int              inType = 1;
  public static final int     MARKDOWN_TYPE = 1;
  public static final int     TAG_TYPE = 2;
  
  private    File               inFile = null;
  
  private    TextLineReader     lineReader = null;
  
  private    boolean            metadataAsMarkdown = true;
  
  private    String             basePath = "";
  private    String             completePath = "";
  private    String             localPath = "";
  private    StringBuilder      pathToTop = new StringBuilder();
  private    int                depth = 0;
  private    String             fileName = "";
  private    String             fileNameBase = "";
  private    String             fileExt = "";
  private    String             lastModDate = "";
  private    ActionStatus       status = new ActionStatus();
  private    ArrayList<String>  parents = new ArrayList();
  private    StringBuilder      breadcrumbs = new StringBuilder();
  private    String             tagStr = "";
  private    Tags               tags = new Tags();
  private    int                tagIndex = 0;
  private    StringBuilder      tagsPath = new StringBuilder();
  private    String             linkedTags = "";
  
  private    SimpleDateFormat   dateFormat 
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
  
  private    MdToHTML           mdToHTML = MdToHTML.getShared();
  
  private    MarkdownDoc        mdDoc;
  private    MarkdownLine       mdLine;
  
  /** The data dictionary to be used by this record. */
  private    DataDictionary     dict = new DataDictionary();
  
  /** The record definition to be used by this record. */
  private    RecordDefinition   recDef = null;
  
  private    DataRecord         dataRec = new DataRecord();
  
  private    StringBuilder      md = new StringBuilder();
  
  /** The logger to use to log events. */    
  private    Logger           log;
  
  /* Let's not log all data. */
  private    boolean          dataLogging = false;
  
  /** Count of the number of records returned, with 1 meaning the first.  */
  private    int              recordNumber = 0;
  
  private    boolean          atEnd = false;
  
  /** Data to be sent to the log. */
  private    LogData          logData;
  
  /** An event to be sent to the log. */
  private    LogEvent         logEvent;
  
  /** The identifier for this reader. */
  private    String           fileId;
  
  
  public MetaMarkdownReader () {
     initialize();
  }
  
  /**
     Constructs a directory reader given a path defining the directory
     to be read.
    
     @param  inPath Directory path to be read.
   */
  public MetaMarkdownReader (String inPath, int inType) {
    if (inPath.startsWith("http")) {
      inFile = null;
    } else {
      inFile = new File (inPath);
    }
    lineReader = new FileLineReader(inPath);
    this.inType = inType;
    initialize();
  }

  /**
     Constructs a directory reader given a file object 
     defining the directory to be read.
    
     @param  inPathFile Directory path to be read.
   */
  public MetaMarkdownReader (File inFile, int inType) {
    this.inFile = inFile;
    lineReader = new FileLineReader(inFile);
    this.inType = inType;
    initialize();
  }
  
  public MetaMarkdownReader (URL inURL, int inType) {
    this.inFile = null;
    lineReader = new FileLineReader (inURL);
    this.inType = inType;
    initialize();
  }
  
  public MetaMarkdownReader (TextLineReader lineReader, int inType) {
    this.inFile = null;
    this.lineReader = lineReader;
    this.inType = inType;
    initialize();
  }
  
  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "directory".
   */
  private void initialize () {
    
    fileId = "MetaMarkdownReader";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }
  
  /**
   Is this input module interested in processing the specified file?
  
   @param candidate The file being considered. 
  
   @return True if the input module thinks this file is worth processing,
           otherwise false. 
  */
  public static boolean isInterestedIn(File candidate) {
    if (candidate.isHidden()) {
      return false;
    }
    else
    if (candidate.getName().startsWith(".")) {
      return false;
    }
    else
    if (! candidate.canRead()) {
      return false;
    }
    else
    if (candidate.isFile() 
        && candidate.length() == 0
        && candidate.getName().equals("Icon\r")) {
      return false;
    }
    else
    if (candidate.isDirectory()) {
      return false;
    }
    else
    if (candidate.getName().endsWith (".txt")
        || candidate.getName().endsWith (".text")
        || candidate.getName().endsWith (".markdown")
        || candidate.getName().endsWith (".md")
        || candidate.getName().endsWith (".mdown")
        || candidate.getName().endsWith (".mkdown")
        || candidate.getName().endsWith (".mdtext")) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   Set the base path to be used for constructing the local path. 
  
   @param basePath The base path to the root of all MetaMarkdown docs to be
                   processed.
  */
  public void setBasePath (String basePath) {
    this.basePath = basePath;
  }
  
  /**
   Pass any metadata lines to the markdown parser as well. 
  
   @param metadataAsMarkdown True if metadata lines should appear as part
                             of output HTML, false otherwise. 
  */
  public void setMetadataAsMarkdown (boolean metadataAsMarkdown) {
    this.metadataAsMarkdown = metadataAsMarkdown;
  }
  
  /**
     Returns the record definition for the reader.
    
     @return Record definition.
   */
  public RecordDefinition getRecDef () {
    if (recDef == null) {
      initRecDef();
    }
    return recDef;
  }
  
  private void initRecDef() {
    recDef = new RecordDefinition (dict);
    recDef.addColumn (COMPLETE_PATH);
    recDef.addColumn (BASE_PATH);
    recDef.addColumn (LOCAL_PATH);
    recDef.addColumn (PATH_TO_TOP);
    recDef.addColumn (DEPTH);
    recDef.addColumn (FILE_NAME);
    recDef.addColumn (FILE_NAME_BASE);
    recDef.addColumn (FILE_EXT);
    recDef.addColumn (LAST_MOD_DATE);
    recDef.addColumn (FILE_SIZE);
    recDef.addColumn (MarkdownDoc.TITLE);
    recDef.addColumn (MarkdownDoc.AUTHOR);
    recDef.addColumn (MarkdownDoc.DATE);
    recDef.addColumn (STATUS);
    recDef.addColumn (BREADCRUMBS);

    switch (inType) {
      case MARKDOWN_TYPE:
        recDef.addColumn (TAGS);
        recDef.addColumn (LINKED_TAGS);
        break;
      case TAG_TYPE:
        recDef.addColumn (SINGLE_TAG);
        break;
    }
  }
  
  /**
     Opens the reader for input using a newly defined
     data dictionary.
    
     @throws IOException If there are problems reading the directory.
   */
  public void openForInput () 
      throws IOException {
    openForInput (new DataDictionary());
  }
  
  /**
     Opens for input with the supplied record definition.
    
     @param  inRecDef Record definition already constructed.
    
     @throws IOException If there are problems reading the directory.
   */
  public void openForInput (RecordDefinition inRecDef) 
      throws IOException {
    openForInput (inRecDef.getDict());
  } // end of openForInput method
  
  /**
     Opens for input with the supplied data dictionary.
    
     @param  inDict Data dictionary already constructed.
    
     @throws IOException If there are problems opening the file.
   */
  public void openForInput (DataDictionary inDict) 
      throws IOException {
    
    ensureLog();
    dict = inDict;
    
    initRecDef();
    
    mdDoc = new MarkdownDoc();
    
    completePath = lineReader.toString();
    
    // Get location of file extension and file name
    int period = completePath.length();
    int slash = -1;
    int i = completePath.length() - 1;
    while (i >= 0 && slash < 0) {
      if (completePath.charAt(i) == '.'
          && period == completePath.length()) {
        period = i;
      } 
      else
      if (completePath.charAt(i) == '/' ||
          completePath.charAt(i) == '\\') {
        slash = i;
      }
      i--;
    }
    int localPathStart = 0;
    if (completePath.startsWith(basePath)) {
      localPathStart = basePath.length();
    }
    if (completePath.charAt(localPathStart) == '/' ||
        completePath.charAt(localPathStart) == '\\') {
      localPathStart++;
    }
    
    // Let's get as much info as we can from the file name or URL
    if (slash > localPathStart) {
      localPath = completePath.substring(localPathStart, slash) + '/';
    } else {
      localPath = "";
    }
    
    int lastSlash = 0;
    if (lastSlash < localPath.length()
        && (localPath.charAt(0) == '/'
          || localPath.charAt(0) == '\\')) {
      lastSlash++;
    }
    while (lastSlash < localPath.length()) {
      depth++;
      tagsPath.append(UP_ONE_FOLDER);
      pathToTop.append(UP_ONE_FOLDER);
      int nextSlash = localPath.indexOf("/", lastSlash);
      if (nextSlash < 0) {
        nextSlash = localPath.indexOf("\\", lastSlash);
      }
      if (nextSlash < 0) {
        nextSlash = localPath.length();
      }
      parents.add(localPath.substring(lastSlash, nextSlash));
      lastSlash = nextSlash;
      lastSlash++;
    }
    tagsPath.append("tags/");
    
    // Now let's build breadcrumbs to higher-level index pages
    int parentIndex = 0;
    int parentStop = parents.size() - 1;
    while (parentIndex < parentStop) {
      addBreadcrumb (parents.size() - parentIndex, parentIndex);
      parentIndex++;
    }
    if (! fileNameBase.equalsIgnoreCase("index")) {
      addBreadcrumb (0, parentIndex);
    } 
    
    fileName = completePath.substring(slash + 1);
    fileNameBase = completePath.substring(slash + 1, period);
    fileExt = completePath.substring(period + 1);
    mdDoc.setTitle(completePath.substring(slash + 1, period));
    
    if (inFile != null && inFile.exists()) {
      Date lastMod = new Date (inFile.lastModified());
      lastModDate = dateFormat.format(lastMod);
    }
    
    // Now let's read the file
    md = new StringBuilder();
    
    boolean ok = lineReader.open();
    if (ok) {
      String line = lineReader.readLine();
      while (line != null
          && lineReader.isOK() 
          && (! lineReader.isAtEnd())) {  
        mdLine = new MarkdownLine (mdDoc, line);
            
        // Now process potential metadata based on type of line
        if (mdLine.isBlankLine()) {
          markdownLine (line);
        }
        else
        if (mdLine.isUnderlines()) {
          markdownLine (line);
        }
        else
        if (mdLine.isMetadata()) {
          if (mdLine.getMetaKey().equalsIgnoreCase(STATUS)) {
            status.setValue(mdLine.getMetaData());
          }
          else
          if (mdLine.getMetaKey().equalsIgnoreCase(TAGS)
              || mdLine.getMetaKey().equalsIgnoreCase("Keywords")
              || mdLine.getMetaKey().equalsIgnoreCase("Category")) {
            tagStr = mdLine.getMetaData();
            tags.setValue(tagStr);
          }
        } // end if metadata
        else {
          markdownLine (line);
        }

        line = lineReader.readLine();
      } // end while more input lines are available
    } // end if opened ok
  
    if (ok) {
      lineReader.close();
      atEnd = false;
    } else {
      atEnd = true;
    }
    
    recordNumber = 0;
    
    
    if (! ok) {
      throw (new IOException());
    }
  } // end of openForInput method
  
  /**
   Add another breadcrumb level. 
  
   @param levels      The number of levels upwards to point to. 
   @param parentIndex The parent to point to. 
  */
  private void addBreadcrumb (int levels, int parentIndex) {
    if (breadcrumbs.length() > 0) {
      breadcrumbs.append(" &gt; ");
    }
    breadcrumbs.append("<a href=\"");
    for (int i = 0; i < levels; i++) {
      breadcrumbs.append(UP_ONE_FOLDER);
    }
    breadcrumbs.append("index.html");
    breadcrumbs.append("\">");
    if (parentIndex < 0 || parentIndex >= parents.size()) {
      breadcrumbs.append("Home");
    } else {
      breadcrumbs.append(
          StringUtils.wordDemarcation(parents.get(parentIndex), " ", 1, 1, -1));
    }
    breadcrumbs.append("</a>");
  }
  
  /**
   Store the specified line as another line of markdown.
  
   @param mdline The line to be stored, without any terminating linefeed 
                 or carriage return.
  */
  private void markdownLine (String mdline) {
    md.append(mdline);
    md.append(GlobalConstants.LINE_FEED);
  }
  
  public String getTitle() {
    return mdDoc.getTitle();
  }
  
  public String getAuthor() {
    return mdDoc.getAuthor();
  }
  
  public String getDate() {
    return mdDoc.getDate();
  }
  
  public String getTags() {
    return tags.toString();
  }
  
  public String getStatus() {
    return status.getLabel();
  }
  
  public DataRecord nextRecordIn () {
    
    switch (inType) {
      case MARKDOWN_TYPE:
        return nextMarkdownRecordIn();
      case TAG_TYPE:
        return nextTagRecordIn();
      default:
        return nextMarkdownRecordIn();
    }

  }
  
  private DataRecord nextMarkdownRecordIn() {
    if (recordNumber < 1) {
      dataRec = new DataRecord();
      dataRec.storeField(recDef, COMPLETE_PATH, completePath);
      dataRec.storeField(recDef, BASE_PATH, basePath);
      dataRec.storeField(recDef, LOCAL_PATH, localPath);
      dataRec.storeField(recDef, PATH_TO_TOP, pathToTop.toString());
      dataRec.storeField(recDef, DEPTH, String.valueOf(depth));
      dataRec.storeField(recDef, FILE_NAME, fileName);
      dataRec.storeField(recDef, FILE_NAME_BASE, fileNameBase);
      dataRec.storeField(recDef, FILE_EXT, fileExt);
      dataRec.storeField(recDef, LAST_MOD_DATE, lastModDate);
      dataRec.storeField(recDef, FILE_SIZE, String.valueOf(mdDoc.getFileSize()));
      dataRec.storeField(recDef, MarkdownDoc.TITLE, mdDoc.getTitle());
      dataRec.storeField(recDef, MarkdownDoc.AUTHOR, mdDoc.getAuthor());
      dataRec.storeField(recDef, MarkdownDoc.DATE, mdDoc.getDate());
      dataRec.storeField(recDef, STATUS, String.valueOf(status.getValueAsInt()));
      dataRec.storeField(recDef, BREADCRUMBS, breadcrumbs.toString());
      dataRec.storeField(recDef, TAGS, tagStr);
      dataRec.storeField(recDef, LINKED_TAGS, 
          tags.getLinkedTags(tagsPath.toString()));
      recordNumber++;
      return dataRec;
    } else {
      atEnd = true;
      return null;
    }
  }
  
  private DataRecord nextTagRecordIn() {
    if (tags == null) {
      atEnd = true;
      return null;
    } else {
      String nextTag = tags.getTag(tagIndex);
      if (nextTag == null || nextTag.length() == 0) {
        atEnd = true;
        return null;
      } else {
        dataRec = new DataRecord();
        dataRec.storeField(recDef, COMPLETE_PATH, completePath);
        dataRec.storeField(recDef, BASE_PATH, basePath);
        dataRec.storeField(recDef, LOCAL_PATH, localPath);
        dataRec.storeField(recDef, DEPTH, String.valueOf(depth));
        dataRec.storeField(recDef, FILE_NAME, fileName);
        dataRec.storeField(recDef, FILE_NAME_BASE, fileNameBase);
        dataRec.storeField(recDef, FILE_EXT, fileExt);
        dataRec.storeField(recDef, LAST_MOD_DATE, lastModDate);
        dataRec.storeField(recDef, FILE_SIZE, String.valueOf(mdDoc.getFileSize()));
        dataRec.storeField(recDef, MarkdownDoc.TITLE, mdDoc.getTitle());
        dataRec.storeField(recDef, MarkdownDoc.AUTHOR, mdDoc.getAuthor());
        dataRec.storeField(recDef, BREADCRUMBS, breadcrumbs.toString());
        dataRec.storeField(recDef, SINGLE_TAG, nextTag);
        tagIndex++;
        recordNumber++;
        return dataRec;
      }
    } 
  }
  
  /**
     Returns the sequential record number of the last record returned.
    
     @return Sequential record number of the last record returned via 
             nextRecordIn, where 1 identifies the first record.
   */
  public int getRecordNumber () {
    return recordNumber;
  }
  
  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return (atEnd);
  }
  
  /**
     Closes the reader.
   */
  public void close() {
    lineReader.close();
    ensureLog();
    logEvent.setSeverity (LogEvent.NORMAL);
    logEvent.setMessage (lineReader.toString() + " closed successfully.");
    log.recordEvent (logEvent);
  }
  
  /**
     Ensures that a log is available, by allocating a new one if
     one has not already been supplied.
   */
  protected void ensureLog () {
    if (log == null) {
      setLog (Logger.getShared());
    }
  }
  
  /**
     Retrieves the path to the original source data (if any).
    
     @return Path to the original source data (if any).
   */
  public String getDataParent () {
    if (inFile == null) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return inFile.getParent();
    }
  }
  
  /**
     Sets the maximum directory explosion depth. The default is 1, meaning
     that only one level is returned (no explosion). If this is changed, it
     should be done after the reader is constructed, but before it is opened
     for input.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {

  }
  
  /**
     Sets a file ID to be used to identify this reader in the log.
    
     @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }
  
  /**
     Indicates whether all data records are to be logged.
    
     @param  dataLogging True if all data records are to be logged.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
  /**
     Sets a log to be used by the reader to record events.
    
     @param  log A logger object to use.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  public String getHTML () {
    return mdToHTML.markdownToHtml(md.toString());
  }

}
