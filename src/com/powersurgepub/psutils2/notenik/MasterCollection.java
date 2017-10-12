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

package com.powersurgepub.psutils2.notenik;

  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.values.*;

  import java.io.*;
  import java.net.*;
  import java.text.*;
  import java.util.*;

  import javafx.scene.control.*;

/**
 A Notenik Collection that contains information about all of a user's other
 Notenik collections. 

 @author Herb Bowie
 */
public class MasterCollection {
  
  public static final String            MASTER_COLLECTION_KEY   = "master-collection";
  
  private             File              masterCollectionFolder = null;
  
  private             NoteParms         masterParms;
  
  private             RecordDefinition  masterDef;
  
  private             NoteIO            masterIO = null;
  
  private             RecentFiles       recentFiles = null;
  
  private             Menu              recentFilesMenu = null;
  
  private             FileSpecOpener    fileSpecOpener = null;
  
  private             SimpleDateFormat  dateFormatter;
  
  public MasterCollection() {
    recentFiles = new RecentFiles();
    masterParms = new NoteParms(NoteParms.DEFINED_TYPE);
    masterDef = new RecordDefinition();
    masterDef.addColumn(NoteParms.TITLE_DEF);
    masterDef.addColumn(NoteParms.TAGS_DEF);
    masterDef.addColumn(NoteParms.LINK_DEF);
    masterDef.addColumn(NoteParms.DATE_DEF);
    masterDef.addColumn(NoteParms.BODY_DEF);
    masterParms.setRecDef(masterDef);
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    checkForMasterCollection();
  }
  
  /**
   Check to see if we have what looks like a good Master Collection. 
  */
  private void checkForMasterCollection() {
    String masterPath = UserPrefs.getShared().getPref(MASTER_COLLECTION_KEY, "");
    if (masterPath != null && masterPath.length() > 0) {
      File masterFile = new File(masterPath);
      if (masterFile.exists() 
          && masterFile.isDirectory() 
          && masterFile.canExecute() 
          && masterFile.canWrite()) {
        masterCollectionFolder = masterFile;
        masterIO = new NoteIO (masterFile, masterParms);
        Logger.getShared().recordEvent(LogEvent.NORMAL, 
            "Master Collection found at " + masterFile.toString(), false);
      }
    }
  }
  
  /**
   Let's load all available info about recently opened collections. 
  */
  public void load() {
    recentFiles.loadFromPrefs();
    
    if (hasMasterCollection()) {
      try {
        masterIO.openForInput();
        Note note = masterIO.readNextNote();
        while (note != null) {
          File noteLinkFile = note.getLinkAsFile();
          if (noteLinkFile != null) {
            FileSpec spec = recentFiles.get(note.getLinkAsFile());
            if (spec == null) {
              spec = new FileSpec(note.getLinkAsFile());
              spec.setCollectionTitle(note.getTitle());
              recentFiles.addNotSoRecentFile(spec);
            } else {
              recentFiles.modRecentFile(noteLinkFile, note.getTitle());
            }
          }
          note = masterIO.readNextNote();
        }
        masterIO.close();
      } catch (IOException e) {
        Logger.getShared().recordEvent(LogEvent.HIGH_SEVERITY, 
            "Trouble reading Master Collection", false);
      }
    }
  }
  
  public void purgeInaccessibleFiles() {
    recentFiles.purgeInaccessibleFiles();
  }
  
  public File getMasterCollectionFolder() {
    return masterCollectionFolder;
  }
  
  public int createMasterCollection(File selectedFile) {

    int filesSaved = -1;
    if(selectedFile != null) {
      setMasterCollection(selectedFile);
      masterIO = new NoteIO (masterCollectionFolder, masterParms);
      
      Note templateNote = new Note(masterDef);
      templateNote.setTitle("The unique title for this note");
      templateNote.setTags("One or more tags, separated by commas");
      templateNote.setLink("http://anyurl.com");
      StringDate today = new StringDate();
      today.set(StringDate.getTodayYMD());
      templateNote.setDate(today);
      templateNote.setBody("The body of the note");
      File templateFile = new File(selectedFile, "template.txt");
      masterIO.save(templateNote, templateFile, true);
      
      filesSaved = 0;
      for (int i = 0; i < recentFiles.size(); i++) {
        FileSpec fileSpec = recentFiles.get(i);
        
        String title = fileSpec.getCollectionTitle();
        Note masterNote = new Note(masterDef, title);
        
        String path = fileSpec.getPath();
        File collection = new File(path);
        String collectionLink = "file:/" + path;
        try {
          String webPage = collection.toURI().toURL().toString();
          String tweaked = StringUtils.tweakAnyLink(webPage, false, false, false, "");
          collectionLink = tweaked;
        } catch (MalformedURLException e) {
          // do nothing
        }
        masterNote.setLink(collectionLink);
        
        Date lastAccess = fileSpec.getLastAccessDate();
        String lastAccessStr = dateFormatter.format(lastAccess);
        
        masterNote.setDate(lastAccessStr);
        try {
          masterIO.save(masterNote, true);
          filesSaved++;
        } catch (java.io.IOException e) {
          Logger.getShared().recordEvent(LogEvent.HIGH_SEVERITY, 
              "I/O Error Saving File to Master Collection", false);
        }
      }
      Logger.getShared().recordEvent (LogEvent.NORMAL, String.valueOf(filesSaved) 
          + " Recent Files successfully saved to " 
          + selectedFile.toString(),
          false);
    }
    return filesSaved;
  }
  
  public void setMasterCollection(File masterCollectionFolder) {
    this.masterCollectionFolder = masterCollectionFolder;
    UserPrefs.getShared().setPref
        (MASTER_COLLECTION_KEY, masterCollectionFolder.toString());
  }
  
  public boolean hasMasterCollection() {
    return (masterCollectionFolder != null);
  }
  
  /**
   Register the menu to contain the recent file menu items.

   @param recentFilesMenu The menu to contain the recent files.
   @param fileSpecOpener The object to be used to open a recent file when
                     it is selected. 
   */
  public void registerMenu (Menu recentFilesMenu, FileSpecOpener fileSpecOpener) {
    this.recentFilesMenu = recentFilesMenu;
    this.fileSpecOpener = fileSpecOpener;
    recentFiles.registerMenu(recentFilesMenu, fileSpecOpener);
  }
  
  /**
   Save the recent files to the user's preferences. 
   */
  public void savePrefs () {
    
    if (hasMasterCollection()) {
      UserPrefs.getShared().setPref
        (MASTER_COLLECTION_KEY, masterCollectionFolder.toString());
    }
    
    recentFiles.savePrefs();
  }
  
  public RecentFiles getRecentFiles() {
    return recentFiles;
  }
  
  /**
   A file has been opened -- let's make sure it's on the recent files list,
   and at the top of it. 
  
   @param file The file being opened. 
  
   @return The File Spec identifying this file, after all operations are 
           completed. 
  */
  public FileSpec addRecentFile(File file) {

    // System.out.println("MasterCollection.addRecentFile");
    // System.out.println("  - File to be added = " + file.toString());
    FileSpec currentFileSpec = recentFiles.addRecentFile (file);
    // System.out.println("  - Collection Title = " + currentFileSpec.getCollectionTitle());
    Date accessed = currentFileSpec.getLastAccessDate();
    
    // Maintain the Master Collection, if we have one
    if (hasMasterCollection()) {
      Note recent = new Note(masterDef, currentFileSpec.getCollectionTitle());
      recent.setLink(file);
      
      Note existing = null;
      try {
        existing = masterIO.getNote(recent.getFileName());
      } catch (IOException e) {
        System.out.println("I/O Exception trying to read existing Master Note");
      }
      if (existing == null) {
        System.out.println("  - Unable to find collection within Master Collection");
      }
      if (existing != null) {
        existing.merge(recent);
        recent = existing;
      } 
      recent.setDate(accessed);
      try {
        masterIO.save(recent, true);
      } catch (IOException e) {
        Logger.getShared().recordEvent(LogEvent.HIGH_SEVERITY, 
            "Trouble adding recent file to Master Collection", false);
      }
    }
    
    return currentFileSpec;
  }
  
  /**
   Modify the title of a recent file spec. 
  
   @param oldTitle The title before the modification. 
   @param newTitle The title after the modification. 
  */
  public void modRecentFile(String oldTitle, String newTitle) {

    if (! oldTitle.equals(newTitle)) {
      recentFiles.modRecentFile(oldTitle, newTitle);
    }
  }
  
  public void removeRecentFile(String oldTitle) {
    recentFiles.removeRecentFile(oldTitle);
  }
  
  /**
   Get a particular FileSpec entry, given its position in the list.

   @param i The index position of the desired entry in the list.

   @return The specified FileSpec entry, if one exists at the index given,
           otherwise null.
   */
  public FileSpec getFileSpec (int i) {
    return recentFiles.get(i);
  }
  
  /**
   Given a file, return the corresponding recent file spec, or null
   in this file has no match. 
  
   @param file The file of interest. 
  
   @return The corresponding file spec, if any, otherwise null.
  */
  public FileSpec getFileSpec(File file) {
    return recentFiles.get(file);
  }
  
  public String getPrefsQualifier () {
    return recentFiles.getPrefsQualifier();
  }
}
