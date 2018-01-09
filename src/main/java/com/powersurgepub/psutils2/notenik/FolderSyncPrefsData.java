/*
 * Copyright 2014 - 2017 Herb Bowie
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

  import java.io.*;

/**
 Maintain user prefs for syncing with a Notational Velocity style folder. 

 @author Herb Bowie
 */
public class FolderSyncPrefsData {
  
  public static final String SYNC_FOLDER = "sync-folder";
  
  private boolean               syncSetting = false;
  private String                collectionPath = "";
  private String                syncFolder = "";
  private StringBuilder         prefix = new StringBuilder();

  /**
   Creates new form FolderSyncPrefs
   */
  public FolderSyncPrefsData() {
    syncFolder = UserPrefs.getShared().getPref(SYNC_FOLDER, "");
  }
  
  public void setCollection(FileSpec collection) {
    if (collection != null) {
      collectionPath = collection.getPath();
      FileName collectionName = new FileName(collection.getFile());
      String folder = collectionName.getLastFolder();
      prefix = new StringBuilder();
      if (folder.length() > 1
          && folder.charAt(folder.length() - 1) == 's') {
        prefix.append(folder.substring(0, folder.length() - 1));
      } else {
        prefix.append(folder);
      }
      if (prefix.length() > 0) {
        prefix.append(" - ");
      }
      syncSetting = collection.getSync();
      String collectionPrefix = collection.getSyncPrefix().trim();
      if (collectionPrefix.length() > 0) {
        prefix = new StringBuilder(collection.getSyncPrefix());
      }
    } else {
      syncSetting = false;
    }
  }
  
  public void setSyncFolder (File folder) {
    try {
      String folderString = folder.getCanonicalPath();
      setSyncFolder(folderString);
    } catch (java.io.IOException e) {
      setSyncFolder(folder.toString());
    }
    
  }
  
  public void setSyncFolder (String folderString) {
    syncFolder = folderString;
    savePrefs();
  }
  
  public String getSyncFolder() {
    return syncFolder;
  }
  
  public void setSyncPrefix(String syncPrefix) {
    prefix = new StringBuilder(syncPrefix);
  }
  
  public String getSyncPrefix() {
    return prefix.toString();
  }
  
  public void setSync(boolean syncSetting) {
    this.syncSetting = syncSetting;
  }
  
  public boolean getSync() {
    return syncSetting;
  }
  
  public void savePrefs() {
    File syncFolderFile = new File(syncFolder);
    if (syncFolder.trim().length() == 0
        || (syncFolderFile.exists() 
          && syncFolderFile.canRead()
          && syncFolderFile.canWrite())) {
      UserPrefs.getShared().setPref(SYNC_FOLDER, syncFolder);
    } 
  }

}
