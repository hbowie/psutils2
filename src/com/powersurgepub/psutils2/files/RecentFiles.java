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
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.util.*;

  import javafx.event.*;
  import javafx.scene.control.*;
  import javafx.stage.*;

/**
 A list of files recently accessed by an application, accessible
 to the user through the Open Recent sub-menu of the File menu. <p>

 The singleton design pattern is not used, since a single application may
 legitimately use different files for different purposes, and so may need
 different instances of RecentFiles for the different types of files. An
 optional file qualifier is provided to support these cases. <p>

 An internal list of file specs is maintained, and kept in sync with a JMenu
 list of menu items. Both lists are maintained in chronological sequence,
 with the most recently accessed files at the top. <p>

 Usage scenario: <p>

 <pre>
 {@code

     implements FileSpecOpener
 
     RecentFiles recentFiles;

     // Program initialization
     recentFiles = new RecentFiles();
     recentFiles.loadFromPrefs();
     recentFiles.registerMenu(fileOpenRecentMenu, this);

   public RecentFiles getRecentFiles () {
     return recentFiles;
   }

     // When opening a file
     recentFiles.addRecentFile ("file", file.toString(), "html");


   public void handleOpenFile (FileSpec fileSpec) {
     // open recent file when selected by user
   }

   public void savePrefs() {
     recentFiles.savePrefs();
   }
 </pre>

 @author Herb Bowie
 */
public class RecentFiles {
  
  private         String              prefsQualifier          = "";
  
  private         int                 recentFilesMax          
                                        = FilePrefs.RECENT_FILES_MAX_DEFAULT;

  private         ArrayList<FileSpec> files                   = new ArrayList();

  private         Menu                recentFilesMenu         = null;
  
  private         FileSpecOpener      fileOpener              = null;
  
  private         FilePrefs           filePrefs               = null;
  
  private         String              fileContentsName        = "Items";
  
  private         int                 fileSelectionMode       = 0;
  public static final int               SELECT_FOLDERS        = 0;
  public static final int               SELECT_FILES          = 1;

  /**
   Construct a RecentFiles instance without any prefs qualifier.
   */
  public RecentFiles () {
    
  }

  /**
   Construct a RecentFiles instance with a prefs qualifier.

   @param prefsQualifier If non-blank, will be used to distinguish between
                         different sets of recent files used for different
                         purposes by the same program.
   */
  public RecentFiles (String prefsQualifier) { 
    this.prefsQualifier = prefsQualifier;
    if (prefsQualifier.length() > 0
        && prefsQualifier.charAt(prefsQualifier.length() - 1) != '-') {
      this.prefsQualifier = prefsQualifier + "-";
    } else {
      this.prefsQualifier = prefsQualifier;
    }
    fileContentsName = Home.getShared().getProgramName() + " Items";
  }
  
  public String getPrefsQualifier () {
    return prefsQualifier;
  }
  
  public void setFileContentsName (String fileContentsName) {
    this.fileContentsName = fileContentsName;
  }
  
  public void setFilePrefs (FilePrefs filePrefs) {
    this.filePrefs = filePrefs;
  }
  
  /**
   Load the recent files from the user's preferences.
   */
  public void loadFromPrefs () {
    
    int j = 0;
    for (int i = 0; i < recentFilesMax; i++) {
      FileSpec recentFile = new FileSpec();
      recentFile.loadFromRecentPrefs(prefsQualifier, i);
      if (recentFile.hasPath()) {
        int k = 0;
        boolean found = false;
        while (k < files.size() && (! found)) {
          if (recentFile.getPath().equals(get(k).getPath())) {
            found = true;
          } else {
            k++;
          }
        }
        if (! found) {
          files.add(recentFile);
  
          if (recentFilesMenu != null) {
            recentFilesMenu.getItems().add (j, createMenuItem(recentFile));
          }
          j++;

          if (filePrefs != null) {
            filePrefs.addRecentFileAtEnd(recentFile);
          }
        }
      }
    }
  }
  
  public void purgeInaccessibleFiles () {
    
    int i = 0;
    while (i < files.size()) {
      FileSpec recentFile = files.get(i);
      if (! recentFile.exists()) {
        removeFile(i);
      } else {
        i++;
      }
    }
  }

  /**
   Save the recent files to the user's preferences. 
   */
  public void savePrefs () {
    
    int count = 0;
    for (int i = 0; i < files.size(); i++) {
      FileSpec recentFile = get(i);
      if (recentFile.hasPath()) {
        recentFile.saveToRecentPrefs(prefsQualifier, count);
        count++;
      }
    }
  }
  
  public void setFileSelectionMode (int fileSelectionMode) {
    this.fileSelectionMode = fileSelectionMode;
  }
  
  /**
   Prompt the user to choose a file to be opened. 
  
   @return The file specification for the file to be opened, or null
           if the user did not choose a file or folder. 
  */
  public void chooseFileToOpen (Stage ownerStage) {
    File result = null;
    if (fileSelectionMode == SELECT_FILES) {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Open " + fileContentsName);
      result = chooser.showOpenDialog(ownerStage);
    } else {
      DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle("Open " + fileContentsName);
      result = chooser.showDialog(ownerStage);
    }
    if (result != null
        && result.exists()
        && result.canRead()) {
      FileSpec fileSpec = addRecentFile (result);
      fileOpener.handleOpenFile(fileSpec);
    } else {
      Trouble.getShared().report ("Trouble opening file " + result.toString(),
          "File Open Error");
    }
  }

  /**
   Add a file that's been recently used. 
  
   @param file The file that's been recently used. 
  
   @return The file spec for the file. 
  */
  public FileSpec addRecentFile (File file) {
    return addRecentFile ("file", file.getAbsolutePath(), "");
  }

  /**
   Add a file that's been recently used.

   @param type A string indicating the type of file or data store.
   @param path The path of the file or data store.
   @param format The format of the data within the data store. 
   */
  public FileSpec addRecentFile (String type, String path, String format) {
    
    FileSpec recentFile = new FileSpec();
    recentFile.setType(type);
    recentFile.setPath(path);
    recentFile.setFormat(format);
    return addRecentFile (recentFile);

  } // end method addRecentFile
  
  /**
   Add a file that's been recently used.

   @param type   A string indicating the type of file or data store.
   @param path   The path of the file or data store.
   @param format The format of the data within the data store. 
   */
  public FileSpec addRecentFile (FileSpec recentFile) {

    // Let's add the most recent file opened to the top of the list
    files.add (0, recentFile);
    MenuItem recentItem = createMenuItem(recentFile);
    if (recentFilesMenu != null) {
      recentFilesMenu.getItems().add(0, recentItem);
    }
    if (filePrefs != null) {
      filePrefs.addRecentFileAtTop(recentFile);
    }
    
    for (int i = 1; i < files.size(); i++) {
      if (get(i).getPath().equals(recentFile.getPath())) {
        recentFile.merge(get(i));
        recentItem.setText(recentFile.getCollectionTitle());
        removeFile (i);
        i--;
      }
      else
      if (i >= recentFilesMax) {
        removeFile (i);
        i--;
      } // end if we found a file to remove
    } // end for each file in list
    savePrefs();
    return get(0);
  } // end method addRecentFile
      
  /**
   Add a file that should be near the top, but not replace the file
   currently at the top of the list. 

   @param notSoRecentFile   The file to be added. 
   */
  public FileSpec addNotSoRecentFile (FileSpec notSoRecentFile) {

 
    files.add (1, notSoRecentFile);
    if (recentFilesMenu != null) {
      recentFilesMenu.getItems().add(1, createMenuItem(notSoRecentFile));
    }
    if (filePrefs != null) {
      filePrefs.addNotSoRecentFile(notSoRecentFile);
    }
    for (int i = 1; i < files.size(); i++) {
      if (get(i).getPath().equals(notSoRecentFile.getPath())) {
        notSoRecentFile.merge(get(i));
        removeFile (i);
        i--;
      }
      else
      if (i >= recentFilesMax) {
        removeFile (i);
        i--;
      } // end if we found a file to remove
    } // end for each file in list
    savePrefs();
    return get(0);
  } // end method addRecentFile
  
  public void removeRecentFile(String oldTitle) {
    boolean found = false;
    int i = 0;
    while (i < files.size() && (! found)) {
      FileSpec fs = get(i);
      if (fs != null && fs.getCollectionTitle().equalsIgnoreCase(oldTitle)) {
        found = true;
        removeFile(i);
      }
      i++;
    }
  }
  
  private void removeFile (int i) {
   
    files.remove(i);
    if (filePrefs != null) {
      filePrefs.removeRecentFile (i);
    }
    if (recentFilesMenu != null && i < recentFilesMenu.getItems().size()) {
      recentFilesMenu.getItems().remove(i);
    }
  }
  
  /**
   Modify the title of a recent file spec. 
  
   @param oldTitle The title before the modification. 
   @param newTitle The title after the modification. 
  */
  public void modRecentFile(String oldTitle, String newTitle) {

    boolean found = false;
    int i = 0;
    while (i < files.size() && (! found)) {
      FileSpec fs = get(i);
      if (fs != null && fs.getCollectionTitle().equalsIgnoreCase(oldTitle)) {
        found = true;
        fs.setCollectionTitle(newTitle);
      }
      i++;
    }
    
    if (recentFilesMenu != null) {
      found = false;
      i = 0;
      while (i < recentFilesMenu.getItems().size() && (! found)) {
        MenuItem mc = recentFilesMenu.getItems().get(i);
        if (mc instanceof MenuItem) {
          MenuItem mi = (MenuItem)mc;
          if (mi.getText().equalsIgnoreCase(oldTitle)) {
            found = true;
            mi.setText(newTitle);
          }
        }
        i++;
      } 
    }
    
    savePrefs();
  }
  
  /**
   Modify the title of a recent file spec. 
  
   @param file The file whose menu item we are looking for. 
   @param newTitle The title to be applied to this menu item. 
  */
  public void modRecentFile(File file, String newTitle) {
    boolean found = false;
    int i = 0;
    String oldTitle = null;
    while (i < files.size() && (! found)) {
      FileSpec fs = get(i);
      if (fs != null && fs.getFile().equals(file)) {
        found = true;
        oldTitle = fs.getCollectionTitle();
        fs.setCollectionTitle(newTitle);
      }
      i++;
    }
    
    if (recentFilesMenu != null && oldTitle != null) {
      found = false;
      i = 0;
      while (i < recentFilesMenu.getItems().size() && (! found)) {
        MenuItem mc = recentFilesMenu.getItems().get(i);
        if (mc instanceof MenuItem) {
          MenuItem mi = (MenuItem)mc;
          if (mi.getText().equals(oldTitle)) {
            found = true;
            mi.setText(newTitle);
          }
        }
        i++;
      } 
    }
  }

  /**
   Set the maximum number of recent files to be retained.

   @param recentFilesMax The number of recent files to be retained.
   */
  public void setRecentFilesMax (int recentFilesMax) {
    this.recentFilesMax = recentFilesMax;
    while (files.size() > recentFilesMax) {
      removeFile (files.size() - 1);
    }
  }
  
  /**
   Get the maximum number of recent files to be retained. 
   
   @return The number of recent files to be retained.
  */
  // public int getRecentFilesMax () {
  //   return recentFilesMax;
  // }

  /**
   Register the menu to contain the recent file menu items.

   @param recentFilesMenu The menu to contain the recent files.
   @param fileOpener The object to be used to open a recent file when
                     it is selected. 
   */
  public void registerMenu (Menu recentFilesMenu, FileSpecOpener fileOpener) {
    this.recentFilesMenu = recentFilesMenu;
    this.fileOpener = fileOpener;
    buildMenu();
  }

  /**
   Build the menu based on the entries in the files list.
   */
  private void buildMenu () {
    for (int i = 0; i < files.size(); i++) {
      recentFilesMenu.getItems().add(createMenuItem(get(i)));
    }
    SeparatorMenuItem sep = new SeparatorMenuItem();
    recentFilesMenu.getItems().add(sep);

    MenuItem clear = new MenuItem("Clear History");
    clear.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        clearHistory(evt);
      }
    });
    recentFilesMenu.getItems().add(clear);
  }

  /**
   Clear the recent files history, when requested by the user.
   
   @param evt
   */
  private void clearHistory (ActionEvent evt) {
    while (files.size() > 1) {
      removeFile(files.size() - 1);
    }
    FileSpec nullFileSpec = new FileSpec();
    for (int i = 1; i < recentFilesMax; i++) {
      nullFileSpec.saveToRecentPrefs("", i);
    }
    if (filePrefs != null) {
      filePrefs.clearHistory();
    }
  }

  /**
   Create one menu item for the recent files menu.

   @param fileSpec The FileSpec identifying the file or url.
   @return The new menu item created. 
   */
  private MenuItem createMenuItem (FileSpec fileSpec) {

    // Create the new menu item
    // Label label = new Label(fileSpec.getCollectionTitle());
    // Tooltip tip = new Tooltip(fileSpec.getDisplayName());
    // Tooltip.install(label, tip);
    // CustomMenuItem customMenuItem = new CustomMenuItem(label);
    MenuItem menuItem = new MenuItem(fileSpec.getCollectionTitle());
    menuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        fileRecentMenuItemActionPerformed(evt);
      }
    });
    return menuItem;
  } // end method

  /**
    Action listener for recent file menu items.

    @param evt = Action event.
   */
  private void fileRecentMenuItemActionPerformed(ActionEvent evt) {
    EventTarget target = evt.getTarget();
    if (target instanceof MenuItem) {
      MenuItem mi = (MenuItem)target;
      String name = mi.getText();
      int i = 0;
      boolean found = false;
      FileSpec fileSpec = null;
      while (i < files.size() && (! found)) {
        fileSpec = get(i);
        if (name.equals(fileSpec.getPath())) {
          found = true;
        } else {
          i++;
        }
      }
      if (found) {
        File file = new File(fileSpec.getPath());
        if (file.exists()) {
          fileOpener.handleOpenFile(fileSpec);
        }
      }
    }
  } // end method
  
  public FileSpec get(File file) {

    boolean found = false;
    int i = 0;
    FileSpec spec = null;
    while (i < files.size() && (! found)) {
      spec = files.get(i);
      if (file.equals(spec.getFile())) {
        found = true;
      } else {
        i++;
      }
    }
    if (found) {
      return spec;
    } else {
      return null;
    }
  }

  /**
   Get a particular FileSpec entry, given its position in the list.

   @param i The index position of the desired entry in the list.

   @return The specified FileSpec entry, if one exists at the index given,
           otherwise null.
   */
  public FileSpec get (int i) {
    if (i < 0 || i >= files.size()) {
      return null;
    } else {
      return files.get(i);
    }
  }
  
  /**
   Return the number of recent files in the list. 
  
   @return The number of recent files in the list.  
  */
  public int size() {
    return files.size();
  }
  
  public void displayRecentFiles() {
    System.out.println ("RecentFiles.displayRecentFiles");
    System.out.println("  files");
    for (int i = 0; i < files.size(); i++) {
      System.out.println("    " + String.valueOf(i) + ": " + get(i).getPath());
    }
    
    if (recentFilesMenu != null) {
      System.out.println ("  Recent Files menu");
      for (int i = 0; i < recentFilesMenu.getItems().size(); i++) {
        System.out.println("  " + String.valueOf(i) + ": " + 
            recentFilesMenu.getItems().get(i).toString());
      }
    }
  }

}
