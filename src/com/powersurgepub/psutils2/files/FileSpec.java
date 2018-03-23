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

package com.powersurgepub.psutils2.files;

  import com.powersurgepub.psutils2.env.*;

  import java.io.*;
  import java.text.*;
  import java.util.*;

/**
 A specification for a file or other data store. In addition to the file's
 location, a file type, a file format, a last access date, and a last backup
 date are also stored. 
 */
public class FileSpec {
  
  public static final String EQUALS = "=";
  public static final String DELIMITER = ";";
  
  public static final String PATH             = "path";
  public static final String TYPE             = "type";
  public static final String FORMAT           = "format";
  public static final String COLLECTION_TITLE = "collection-title";
  public static final String LAST_ACCESS      = "last-access";
  public static final String LAST_BACKUP      = "last-backup";
  public static final String BACKUP_FOLDER    = "backup-folder";
  public static final String ARCHIVE_FOLDER   = "archive-folder";
  public static final String TEMPLATES_FOLDER = "templates-folder";
  public static final String SCRIPTS_FOLDER   = "scripts-folder";
  public static final String HTML_FOLDER      = "html-folder";
  public static final String EASYPLAY         = "easyplay";
  public static final String SYNC             = "sync";
  public static final String SYNC_PREFIX      = "sync-prefix";
  public static final String LAST_TITLE       = "last-title";
  public static final String NOTE_SORT_PARM   = "note-sort-parm";

  public static final String RECENT_FILE            = "recent-file";
  public static final String RECENT_FILE_TYPE       = "recent-file-type";
  public static final String RECENT_FILE_NAME       = "recent-file-name";
  public static final String RECENT_FILE_FORMAT     = "recent-file-format";
  public static final String RECENT_FILE_DATE       = "recent-file-date";
  public static final String OLD_RECENT_FILE_PREFIX = "recent.file";
  
  public static final int    BRIEF_DISPLAY_NAME_MAX_LENGTH = 30;
  

  private             File   file = null;
  private             String type   = "";
  private             String path   = "";
  private             String format = "";
  private             String collectionTitle = "";
  private             Date   lastAccessDate   = new Date();
  private             Date   lastBackupDate   = new Date();
  private             String backupFolder = "";
  private             String archiveFolder = "";
  private             String templatesFolder = "";
  private             String scriptsFolder = "";
  private             String htmlFolder = "";
  private             String easyplay = "";
  private             String sync = "";
  private             String syncPrefix = "";
  private             String lastTitle = "";
  private             int    noteSortParm = 0;
  private             boolean masterNoteMatched = false;

  /**
   Construct a FileSpec without any data.
   */
  public FileSpec () {

  }

  /**
   Construct a FileSpec with a path.

   @param path A file path or url, typically.
   */
  public FileSpec (String path) {

    setPath(path);
  }
  
  /**
   Construct a file spec with a File object. 
  @param file 
  */
  public FileSpec (File file) {

    setFile(file);
  }

  /**
   Load this file specification from a recent file user preference.

   @param prefsQualifier Used to qualify a particular group of recent files
                         within an application. Typically supplied as an
                         empty string.
   @param recentFileNumber The number identifying the position of the recent
                           file in a list. Zero would identify the first
                           file in the list, and the file most recently
                           accessed. 
   */
  public void loadFromRecentPrefs (String prefsQualifier, int recentFileNumber) {

    // Apend the file number to the keys
    String keySuffix = String.valueOf(recentFileNumber);

    UserPrefs prefs = UserPrefs.getShared();
    
    String fileInfo = prefs.getPref
        (prefsQualifier + RECENT_FILE + "-" + keySuffix, "");
    if (fileInfo.length() > 0) {
      setFileInfo (fileInfo);
    } else {
      // Load the path
      setPath (prefs.getPref(prefsQualifier + RECENT_FILE_NAME
          + "-" + keySuffix, ""));

      if (path.length() == 0) {
        setPath (prefs.getPref(OLD_RECENT_FILE_PREFIX
            + keySuffix));
      } else {
        // Load the type
        setType (prefs.getPref(prefsQualifier + RECENT_FILE_TYPE + "-"
            + keySuffix, ""));

        // Load the format
        setFormat (prefs.getPref(prefsQualifier + RECENT_FILE_FORMAT + "-"
            + keySuffix, ""));

        // Load the lastAccessDate last accessed
        setLastAccessDate (prefs.getPref(prefsQualifier + RECENT_FILE_DATE + "-"
            + keySuffix, ""));
        
        // Load the generic backup folder, if one is available
        setBackupFolder (prefs.getPref (prefsQualifier + BACKUP_FOLDER + "-"
            + keySuffix, ""));
        
        // Load the Collection Title, if one is available
        setCollectionTitle (prefs.getPref(prefsQualifier + COLLECTION_TITLE + "-"
            + keySuffix, ""));

      }
    }
  }

  /**
   Save this file specification to a recent file user preference.

   @param prefsQualifier Used to qualify a particular group of recent files
                         within an application. Typically supplied as an
                         empty string.
   @param recentFileNumber The number identifying the position of the recent
                           file in a list. Zero would identify the first
                           file in the list, and the file most recently
                           accessed.
   */
  public void saveToRecentPrefs (String prefsQualifier, int recentFileNumber) {
    
    // Apend the file number to the keys
    String keySuffix = String.valueOf(recentFileNumber);

    UserPrefs prefs = UserPrefs.getShared();
    
    // Save the entire bundle as one preference
    prefs.setPref(prefsQualifier + RECENT_FILE + "-" + keySuffix, getFileInfo());
  }
  
  /**
   Set the various File Spec variables based on the info encoded in the passed
   string.
  
   @param fileInfo A string containing encoded file spec attributes, with each
                   attribute separated by a semi-colon, and each attribute 
                   consisting of a key-value pair, using an equals sign as 
                   a separator.
  */
  public void setFileInfo(String fileInfo) {
    int i = 0;
    int equalsIndex;
    int delimIndex;
    while (i >= 0 && i < fileInfo.length()) {
      while (i < fileInfo.length()
          && Character.isWhitespace(fileInfo.charAt(i))) {
        i++;
      }
      equalsIndex = fileInfo.indexOf(EQUALS, i);
      delimIndex = -1;
      if (equalsIndex > 0) {
        delimIndex = fileInfo.indexOf(DELIMITER, equalsIndex);
        if (delimIndex < 0) {
          delimIndex = fileInfo.length();
        } // end if we found a delimiter
        String name = fileInfo.substring(i, equalsIndex);
        String data = fileInfo.substring(equalsIndex + 1, delimIndex);
        setAttribute (name, data);
      } // end if we found an equals sign
      i = delimIndex;
      if (i < fileInfo.length()) {
        i++;
      }
    } // end while more characters to evaluate
  } // end method setFileInfo
  
  public void setAttribute (String name, String data) {
    if (name.equalsIgnoreCase(PATH)) {
      setPath (data);
    }
    else
    if (name.equalsIgnoreCase(TYPE)) {
      setType (data);
    }
    else
    if (name.equalsIgnoreCase(FORMAT)) {
      setFormat (data);
    }
    else
    if (name.equalsIgnoreCase(COLLECTION_TITLE)) {
      setCollectionTitle (data);
    }
    else
    if (name.equalsIgnoreCase(LAST_ACCESS)) {
      setLastAccessDate (data);
    }
    else
    if (name.equalsIgnoreCase(LAST_BACKUP)) {
      setLastBackupDate (data);
    }
    else
    if (name.equalsIgnoreCase(BACKUP_FOLDER)) {
      setBackupFolder (data);
    }
    else
    if (name.equalsIgnoreCase(TEMPLATES_FOLDER)) {
      setTemplatesFolder (data);
    }
    else
    if (name.equalsIgnoreCase(ARCHIVE_FOLDER)) {
      setArchiveFolder (data);
    }
    else
    if (name.equalsIgnoreCase(SCRIPTS_FOLDER)) {
      setScriptsFolder (data);
    }
    else
    if (name.equalsIgnoreCase(HTML_FOLDER)) {
      setHTMLFolder (data);
    }
    else
    if (name.equalsIgnoreCase(EASYPLAY)) {
      setEasyPlay (data);
    }
    else
    if (name.equalsIgnoreCase(SYNC)) {
      setSync(data);
    }
    else
    if (name.equalsIgnoreCase(SYNC_PREFIX)) {
      setSyncPrefix(data);
    }
    else
    if (name.equalsIgnoreCase(LAST_TITLE)) {
      setLastTitle(data);
    }
    else
    if (name.equalsIgnoreCase(NOTE_SORT_PARM)) {
      setNoteSortParm(data);
    }
  }
  
  public String getFileInfo() {
    StringBuilder str = new StringBuilder();
    addAttribute(str, PATH, path);
    addAttribute(str, TYPE, type);
    addAttribute(str, FORMAT, format);
    addAttribute(str, COLLECTION_TITLE, getCollectionTitle());
    addAttribute(str, LAST_ACCESS, getLastAccessDateAsString());
    addAttribute(str, LAST_BACKUP, getLastBackupDateAsString());
    addAttribute(str, BACKUP_FOLDER, getBackupFolder());
    addAttribute(str, ARCHIVE_FOLDER, getArchiveFolder());
    addAttribute(str, TEMPLATES_FOLDER, getTemplatesFolder());
    addAttribute(str, SCRIPTS_FOLDER, getScriptsFolder());
    addAttribute(str, HTML_FOLDER, getHTMLFolder());
    addAttribute(str, EASYPLAY, getEasyPlay());
    addAttribute(str, SYNC, getSyncAsString());
    addAttribute(str, SYNC_PREFIX, getSyncPrefix());
    addAttribute(str, LAST_TITLE, getLastTitle());
    addAttribute(str, NOTE_SORT_PARM, getNoteSortParmAsString());
    return str.toString();
  }
  
  private void addAttribute (StringBuilder str, String name, String data) {
    if (data != null && data.length() > 0) {
      str.append(name);
      str.append(EQUALS);
      str.append(data);
      str.append(DELIMITER);
    }
  }
  
  /**
   Capture info from an older file spec entry before deleting it. 
  
   @param file2 The older file spec from which we are capturing data. 
  */
  public void merge(FileSpec file2) {
    setLastBackupDate(file2.getLastBackupDate());
    setBackupFolder(file2.getBackupFolder());
    setArchiveFolder(file2.getArchiveFolder());
    setScriptsFolder(file2.getScriptsFolder());
    setHTMLFolder(file2.getHTMLFolder());
    setTemplatesFolder(file2.getTemplatesFolder());
    setEasyPlay(file2.getEasyPlay());
    setSyncPrefix(file2.getSyncPrefix());
    setSync(file2.getSync());
    setLastTitle(file2.getLastTitle());
    setNoteSortParm(file2.getNoteSortParm());
    setCollectionTitle(file2.getCollectionTitle());
  }
  
  public void setFile (File file) {
    this.file = file;
    path = file.getAbsolutePath();
    try {
      path = file.getCanonicalPath();
    } catch (java.io.IOException e) {
      
    }
    setCollectionTitleFromPath();
  }
  
  public void setPath(String path) {
    this.path = path;
    setCollectionTitleFromPath();
    file = new File(path);
  }
  
  public File getFile() {
    return file;
  }
  
  /**
   If the file spec identifies a file, return its parent folder; 
   if the file spec identifies a folder, then return that. 
  
   @return A folder (aka directory).  
  */
  public File getFolder() {
    if (file == null) {
      return null;
    }
    else
    if (! file.exists()) {
      return file;
    }
    else
    if (file.isDirectory()) {
      return file;
    }
    else
    if (file.isFile()) {
      return file.getParentFile();
    } else {
      return null;
    }

  }
  
  /**
   Indicate whether the given file is accessible in the file system. 
  
   @return True if a file exists, false otherwise. 
  */
  public boolean exists() {
    if (file != null) {
      return file.exists();
    } else {
      return false;
    }
  }

  /**
   Set the type of file specification. Could be a file, url, etc.

   @param type File, url, etc.
   */
  public void setType (String type) {
    this.type = type;
  }

  /**
   Get the type of file specification.

   @return File, url, etc.
   */
  public String getType () {
    return type;
  }

  /**
   Does the file specification have a path?

   @return True if the path has a non-zero length.
   */
  public boolean hasPath () {
    return (path.length() > 0);
  }

  /**
   Return the path of the file or url.

   @return Name of the file or url.
   */
  public String getPath () {
    return path;
  }
  
  private void setCollectionTitleFromPath() {
    if (collectionTitle == null || collectionTitle.length() == 0) {
      StringBuilder work = new StringBuilder();
      int i = path.length();
      int end = path.length();
      char c = ' ';
      boolean done = false;
      while (i > 0 && (! done)) {
        i--;
        c = path.charAt(i);
        if (c == '%') {
          if (((i + 2) < path.length())
              && ((i + 2) < end)
              && (path.substring(i, i + 3).equals("%20"))) {
            work.insert(0, path.substring(i + 3, end));
            end = i;
          }
        }
        else
        if (c == '/') {
          if (work.length() == 0
              || (work.length() + (end - i) < BRIEF_DISPLAY_NAME_MAX_LENGTH)) {
            if (work.length() > 0) {
              work.insert(0, " ");
            }
            work.insert(0, path.substring(i + 1, end));
            end = i;
          } else {
            done = true;
          }
        } // end if we found a slash
      } // end while scanning path from the back
      collectionTitle = work.toString();
    } // end if we don't yet have a collection title
  } // end method 

  /**
   Return a path suitable for display. Currently replaces occurrences of "%20"
   with spaces.

   @return A path suitable for display.
   */
  public String getDisplayName () {
    StringBuffer work = new StringBuffer (path);
    int i = 0;
    while (i >= 0) {
      i = work.indexOf("%20", i);
      if (i >= 0) {
        work.delete (i, i + 3);
        work.insert (i, " ");
        i++;
      } // end if search string was found
    } // end while still finding occurrences of the search string
    return work.toString();
  }

  /**
   Replaces occurrences of "%20" with spaces, and only show the file name
   and as many of its enclosing folders as can fit in a reasonable length.

   @return A brief path suitable for display.
   */
  public String getBriefDisplayName () {
    String displayName = getDisplayName();
    int lastSlashIndex = -1;
    int j = displayName.length() - 1;
    int length = 0;
    while (j >= 0 && length < BRIEF_DISPLAY_NAME_MAX_LENGTH) {
      if (displayName.charAt(j) == '/' || displayName.charAt(j) == '\\') {
        lastSlashIndex = j;
      }
      j--;
      length++;
    }
    return displayName.substring(lastSlashIndex + 1);
  }

  /**
   Set the format of the data contained in the file.

   @param format Indicator of the format of the data contained in the file.
   */
  public void setFormat (String format) {
    this.format = format;
  }

  /**
   Return the format of the data contained in the file.

   @return Indicator of the format of the data contained in the file.
   */
  public String getFormat () {
    return format;
  }

  /**
   Set the lastAccessDate on which the file was last accessed by the application currently
   running.

   @param dateStr A string representing the lastAccessDate on which the file was last
                  accessed by the application currently running.
   */
  public void setLastAccessDate (String dateStr) {
    DateFormat formatter = DateFormat.getDateTimeInstance();
    if (dateStr.length() == 0) {
      setLastAccessDateToNow();
    } else {
      try {
        lastAccessDate = formatter.parse(dateStr);
      } catch (ParseException e) {
        setLastAccessDateToNow();
      } // end catch Parse Exception
    } // end if lastAccessDate string length > 0
  } // end method setLastAccessDate

  /**
   Set the lastAccessDate and time on which the file was last accessed by the
   application currently running.

   @param lastAccessDate    A string representing the lastAccessDate on which the file was last
                  accessed by the application currently running.
   */
  public void setLastAccessDate (Date lastAccessDate) {
    this.lastAccessDate = lastAccessDate;
  }

  /**
   Set the lastAccessDate and time to right now.
   */
  public void setLastAccessDateToNow () {
    lastAccessDate = new Date();
  }

  /**
   Get the lastAccessDate and time on which the currently running application last
   accessed this file.

   @return The lastAccessDate and time on which the currently running application last
   accessed this file.
   */
  public Date getLastAccessDate () {
    return lastAccessDate;
  }
  
  /**
   Get the lastAccessDate and time on which the currently running application last
   accessed this file.

   @return The lastAccessDate and time on which the currently running application last
   accessed this file.
   */
  public String getLastAccessDateAsString () {
    DateFormat formatter = DateFormat.getDateTimeInstance();
    return (formatter.format(getLastAccessDate()));
  }

  /**
   Set the lastBackupDate on which the file was last accessed by the application currently
   running.

   @param dateStr A string representing the lastBackupDate on which the file was last
                  accessed by the application currently running.
   */
  public void setLastBackupDate (String dateStr) {
    DateFormat formatter = DateFormat.getDateTimeInstance();
    if (dateStr.length() == 0) {
      setLastBackupDateToNow();
    } else {
      try {
        lastBackupDate = formatter.parse(dateStr);
      } catch (ParseException e) {
        setLastBackupDateToNow();
      } // end catch Parse Exception
    } // end if lastBackupDate string length > 0
  } // end method setLastBackupDate

  /**
   Set the lastBackupDate and time on which the file was last accessed by the
   application currently running.

   @param lastBackupDate    A string representing the lastBackupDate on which the file was last
                  accessed by the application currently running.
   */
  public void setLastBackupDate (Date lastBackupDate) {
    this.lastBackupDate = lastBackupDate;
  }

  /**
   Set the lastBackupDate and time to right now.
   */
  public void setLastBackupDateToNow () {
    lastBackupDate = new Date();
  }

  /**
   Get the lastBackupDate and time on which the currently running application last
   accessed this file.

   @return The lastBackupDate and time on which the currently running application last
   accessed this file.
   */
  public Date getLastBackupDate () {
    return lastBackupDate;
  }
  
  /**
   Get the lastBackupDate and time on which the currently running application last
   accessed this file.

   @return The lastBackupDate and time on which the currently running application last
   accessed this file.
   */
  public String getLastBackupDateAsString () {
    DateFormat formatter = DateFormat.getDateTimeInstance();
    return (formatter.format(getLastBackupDate()));
  }
  
  public void setBackupFolder (File backupFolder) {
    if (backupFolder.isFile()) {
      backupFolder = backupFolder.getParentFile();
    }
    try {
      this.backupFolder = backupFolder.getCanonicalPath();
    } catch (java.io.IOException e) {
      this.backupFolder = backupFolder.getAbsolutePath();
    }
  }
  
  public void setBackupFolder (String backupFolder) {
    this.backupFolder = backupFolder;
  }
  
  public String getBackupFolder () {
    return backupFolder;
  }
  
  public void setArchiveFolder (File archiveFolder) {
    if (archiveFolder.isFile()) {
      archiveFolder = archiveFolder.getParentFile();
    }
    try {
      this.archiveFolder = archiveFolder.getCanonicalPath();
    } catch (IOException e) {
      this.archiveFolder = archiveFolder.getAbsolutePath();
    }
  }
  
  public void setArchiveFolder (String archiveFolder) {
    this.archiveFolder = archiveFolder;
  }
  
  public String getArchiveFolder() {
    return archiveFolder;
  }
  
  public void setTemplatesFolder (File templatesFolder) {
    if (templatesFolder.isFile()) {
      templatesFolder = templatesFolder.getParentFile();
    }
    try {
      this.templatesFolder = templatesFolder.getCanonicalPath();
    } catch (java.io.IOException e) {
      this.templatesFolder = templatesFolder.getAbsolutePath();
    }
  }
  
  public void setTemplatesFolder (String templatesFolder) {
    this.templatesFolder = templatesFolder;
  }
  
  public String getTemplatesFolder () {
    return templatesFolder;
  }
  
  public void setScriptsFolder (File scriptsFolder) {
    if (scriptsFolder.isFile()) {
      scriptsFolder = scriptsFolder.getParentFile();
    }
    try {
      this.scriptsFolder = scriptsFolder.getCanonicalPath();
    } catch (java.io.IOException e) {
      this.scriptsFolder = scriptsFolder.getAbsolutePath();
    }
  }
  
  public void setHTMLFolder (File htmlFolder) {
    if (htmlFolder.isFile()) {
      htmlFolder = htmlFolder.getParentFile();
    }
    try {
      this.htmlFolder = htmlFolder.getCanonicalPath();
    } catch (java.io.IOException e) {
      this.htmlFolder = htmlFolder.getAbsolutePath();
    }
  }
  
  public void setScriptsFolder (String scriptsFolder) {
    this.scriptsFolder = scriptsFolder;
  }
  
  public void setHTMLFolder (String htmlFolder) {
    this.htmlFolder = htmlFolder;
  }
  
  public String getScriptsFolder () {
    return scriptsFolder;
  }
  
  public String getHTMLFolder () {
    return htmlFolder;
  }
  
  public void setEasyPlay (String easyplay) {
    this.easyplay = easyplay;
  }
  
  public String getEasyPlay () {
    return easyplay;
  }
  
  public void setSync(String sync) {
    this.sync = sync;
  }
  
  public void setSync(boolean sync) {
    if (sync) {
      this.sync = "Yes";
    } else {
      this.sync = "No";
    }
  }
  
  public boolean getSync() {
    return (sync.length() > 0
        && (sync.toLowerCase().charAt(0) == 'y'
          || sync.toLowerCase().charAt(0) == 't'));
    
  }
  
  public String getSyncAsString() {
    return sync;
  }
  
  public void setSyncPrefix(String syncPrefix) {
    this.syncPrefix = syncPrefix;
  }
  
  public String getSyncPrefix() {
    return syncPrefix;
  }
  
  public void setLastTitle(String lastTitle) {
    this.lastTitle = lastTitle;
  }
  
  public boolean hasLastTitle() {
    return (lastTitle != null && lastTitle.length() > 0);
  }
  
  public String getLastTitle() {
    return lastTitle;
  }
  
  public void setNoteSortParm(int noteSortParm) {
    this.noteSortParm = noteSortParm;
  }
  
  public void setNoteSortParm(String noteSortParm) {
    try {
      int parm = Integer.parseInt(noteSortParm);
      this.noteSortParm = parm;
    } catch (NumberFormatException e) {
      System.out.println("  - bad note sort parm");
      // Leave it alone if bad number
    }
  }
  
  public boolean hasNoteSortParm() {
    return true;
  }
  
  public int getNoteSortParm() {
    return noteSortParm;
  }
  
  public String getNoteSortParmAsString() {
    String str = Integer.toString(noteSortParm);
    return str;
  }
  
  public void setCollectionTitle(String collectionTitle) {
    if (collectionTitle != null && collectionTitle.length() > 0) {
      this.collectionTitle = collectionTitle;
    }
  }
  
  public boolean hasCollectionTitle() {
    return (collectionTitle != null && collectionTitle.length() > 0);
  }
  
  public String getCollectionTitle() {
    return collectionTitle;
  }

  public void setMasterNoteMatched(boolean masterNoteMatched) {
    this.masterNoteMatched = masterNoteMatched;
  }

  public boolean isMasterNoteMatched() {
    return masterNoteMatched;
  }
  
  public String toString() {
    return path;
  }
  
  /**
   Determines whether this file spec is the same as a second. 
  
   @param spec2 The second file spec to be compared to this one. 
  
   @return True if both specs point to the same file. 
  */
  public boolean equals(FileSpec spec2) {
    return (this.file.equals(spec2.getFile()));
  }
  
}
