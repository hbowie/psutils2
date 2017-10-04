/*
 * Copyright 2017 - 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.logging.*;

  import java.io.*;
  import java.text.*;
  import java.util.*;
  import java.util.zip.*;

  import javafx.stage.*;

/**
 Information about a single backup occurrence, backing up a specific source
 destination at a particular date and time. 

 @author Herb Bowie
 */
public class BackupInfo {
  
  public static final DateFormat  BACKUP_DATE_FORMATTER 
      = new SimpleDateFormat ("yyyy-MM-dd-HH-mm");
  
  private Date          date = new Date();
  
  private File          source = null;
  private File          backupFolder = null;
  private FileName      sourceFileName = null;
  private String        ext = null;
  private StringBuilder backupFileName = new StringBuilder();
  private int           baseNameLength = 0;
  private int           backupsToKeep = 0;
  private boolean       okSoFar = true;
  private boolean       backupSuccess = false;
  
  /**
   Construct a new occurrence. The backup date and time are set at this point. 
  */
  public BackupInfo() {

  }
  
  /**
   Specify the file or folder to be backed up. The presumptive backup file
   name will begin to be calculated when this value is set, taking the
   source location, stripping off the top two folders, replacing
   directory separators with spaces, appending the word "backup", and 
   finally appending the current date and time.
  
   @param source The file or folder to be backed up.
  */
  public void setSource(FileSpec source) {
    setSource(source.getFile());
  }
  
  /**
   Specify the file or folder to be backed up. The presumptive backup file
   name will begin to be calculated when this value is set, taking the
   source location, stripping off the top two folders, replacing
   directory separators with spaces, appending the word "backup", and 
   finally appending the current date and time.
  
   @param source The file or folder to be backed up.
  */
  public void setSource(File source) {
     
   this.source = source;
    sourceFileName = new FileName (source);
    int numberOfFolders = sourceFileName.getNumberOfFolders();
    int i = 3;
    if (i > numberOfFolders) {
      i = 1;
    }
    backupFileName = new StringBuilder();
    while (i <= numberOfFolders) {
      if (backupFileName.length() > 0) {
        backupFileName.append (' ');
      }
      backupFileName.append (sourceFileName.getFolder (i));
      i++;
    }
    backupFileName.append (" backup");
    baseNameLength = backupFileName.length();
    backupFileName.append(" ");
    backupFileName.append (getDateString());
  }
  
  /**
   Set the file extension to the standard value for a zip file.
  */
  public void setToZip() {
    setExt(".zip");
  }
  
  /**
   Set the file extension to be used for the backup file.
  
   @param ext The file extension to be used for the presumptive backup
              file name. 
  */
  public void setExt(String ext) {
    this.ext = ext;
    if (ext == null || ext.length() == 0) {
      // no extension
    }
    else
    if (ext.charAt(0) == '.') {
      backupFileName.append(ext);
    } else {
      backupFileName.append (".");
      backupFileName.append(ext);
    }
  }
  
  /**
   Set the folder in which the backup will be stored. 
  
   @param backupFolder The folder in which the backup is to be stored. 
  */
  public void setBackupFolder(File backupFolder) {
    this.backupFolder = backupFolder;
  }
  
  /**
   Exactly specify a specific backup location to be used, potentially overriding 
   any normal conventions for where to place the file and how to name it. This
   will in turn set the location of the backup folder, and the name of the 
   backup file. 
  
   @param backupFile The destination for the backup. 
  */
  public void setBackupFile(File backupFile) {
    this.backupFolder = backupFile.getParentFile();
    setBackupFileName(backupFile.getName());
  }
  
  /**
   Specify a backup file name. If it includes an extension, then override
   any other extension assignments that may have occurred. 
  
   @param backupFileName The name to be used for the backup file. 
  */
  public void setBackupFileName(String backupFileName) {
    this.backupFileName = new StringBuilder(backupFileName);
    int dotPosition = backupFileName.lastIndexOf(".");
    if (dotPosition > 0) {
      this.ext = backupFileName.substring(dotPosition);
    }
  }
  
  /**
   Set the number of backups to keep, based on the user preferences. 
  
   @param filePrefs The file preferences set by the user. 
  */
  public void setBackupsToKeep(FilePrefs filePrefs) {
    setBackupsToKeep(filePrefs.getBackupsToKeep());
  }
  
  /**
   Set the backups to keep, to be used in a potential prune operation. 
  
   @param backupsToKeep The number of backups to keep around after 
                        a prune operation. 
  */
  public void setBackupsToKeep(int backupsToKeep) {
    this.backupsToKeep = backupsToKeep;
  }
  
  /**
   Compress the visible contents of one folder into a new Zip file, including all
   sub-folders. Skip any hidden files and any backup folders. 
   */
  public boolean backupToZip () {

    if (ext == null || ext.length() == 0) {
      ext = ".zip";
      backupFileName.append(ext);
    }
    
    backupSuccess = false;
    
    okSoFar = (source != null
        && source.exists() 
        && source.canRead() 
        && source.isDirectory());

    if (okSoFar) {
      try {
        String sourcePath = source.getAbsolutePath();
        FileOutputStream fileOut = new FileOutputStream(getBackupFile());
        ZipOutputStream  zipOut  = new ZipOutputStream(fileOut);
        ArrayList<File> fromDirList = new ArrayList<>();
        fromDirList.add (source);
        int i = 0;
        while (i < fromDirList.size()) {
          // for each directory or sub-directory
          File fromDir = fromDirList.get(i);
          String[] dirEntry = fromDir.list();

          for (int j = 0; j < dirEntry.length; j++) {
            String entry = dirEntry [j];
            File fromFile = new File (fromDir, entry);
            if (fromFile.exists()
                && fromFile.canRead()
                && (! fromFile.isHidden())) {
              if (fromFile.isDirectory()) {
                if (entry.equalsIgnoreCase("backups")
                    || entry.equalsIgnoreCase("backup")) {
                  // Skip it -- let's not compress the contents of a 
                  // backups folder into a new backup.
                } else {
                  fromDirList.add (fromFile);
                }
              } else {
                String fromFilePath = fromFile.getAbsolutePath();
                String entryName 
                    = fromFilePath.substring(sourcePath.length() + 1).replace("\\", "/");
                ZipEntry zipEntry = new ZipEntry(entryName);
                zipEntry.setTime(fromFile.lastModified());
                FileInputStream fileIn = new FileInputStream(fromFile);
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fileIn.read(bytes)) >= 0) {
                  zipOut.write(bytes, 0, length);
                }
                zipOut.closeEntry();
                fileIn.close();
              } // end if not a directory
            } // end if directory entry is readable
          } // end for each directory entry in current directory
          i++;
        } // end for each directory to be exploded
        zipOut.close();
        fileOut.close();
        backupSuccess = true;
      } catch (IOException e) {
        okSoFar = false;
        Logger.getShared().recordEvent(LogEvent.MEDIUM, 
            "I/O Error compressing into a zip file", false);
      }
    } // end if ok
    return backupSuccess;
  } // end backupToZip method
  
  /**
   Remove older backup files or folders. 
  
   @return The number of backups pruned. 
  */
  public int pruneBackups() {
    int pruned = 0;
    String baseName = backupFileName.substring(0, baseNameLength);
    if (backupsToKeep > 0) {
      ArrayList<String> backups = new ArrayList<>();
      String[] dirEntries = backupFolder.list();
      for (int i = 0; i < dirEntries.length; i++) {
        String dirEntryName = dirEntries[i];
        if (dirEntryName.startsWith(baseName)) {
          boolean added = false;
          int j = 0;
          while ((! added) && (j < backups.size())) {
            if (dirEntryName.compareTo(backups.get(j)) > 0) {
              backups.add(j, dirEntryName);
              added = true;
            } else {
              j++;
            }
          } // end while looking for insertion point
          if (! added) {
            backups.add(dirEntryName);
          }
        } // end if file/folder name matches prefix
      } // end of directory entries
      while (backups.size() > backupsToKeep) {
        String toDelete = backups.get(backups.size() - 1);
        File toDeleteFile = new File (backupFolder, toDelete);
        if (toDeleteFile.isDirectory()) {
          FileUtils.deleteFolderContents(toDeleteFile);
        }
        toDeleteFile.delete(); 
        Logger.getShared().recordEvent(LogEvent.NORMAL, 
            "Pruning older backup: " + toDeleteFile.toString(), 
            false);
        pruned++;
        backups.remove(backups.size() - 1);
      }
    } // if we have a backups to keep number
    return pruned;
  }
  
  /**
   Return the current date and time formatted in a way that can be 
   easily appended to a file or folder name. 
  
   @return Current date and time. 
  */
  public String getDateString() {
    return BACKUP_DATE_FORMATTER.format (date);
  }
  
  public Date getDate() {
    return date;
  }
  
  public File getSource() {
    return source;
  }
  
  public File getBackupFolder() {
    return backupFolder;
  }
  
  public File getBackupFile() {
    return new File(getBackupFolder(), getBackupFileName());
  }
  
  public String getBackupFileName() {
    return backupFileName.toString();
  }
  
  public boolean okSoFar() {
    return okSoFar;
  }
  
  public boolean backupSuccess() {
    return backupSuccess;
  }
  
  /**
   Let the user choose a backup file. 
  
   @param primaryStage The primary stage for the application.
  
   @return The backup file chosen, or null if no choice. 
  */
  public File letUserChooseBackupFile(Stage primaryStage) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle ("Designate an Output Backup File");
    chooser.setInitialDirectory (backupFolder);
    chooser.setInitialFileName(getBackupFileName());
    File userChoice = chooser.showSaveDialog (primaryStage);
    if (userChoice == null) {
      okSoFar = false;
    } else {
      if (! userChoice.getName().equals(getBackupFileName())) {
        setBackupFile(userChoice);
      }
    }
    return userChoice;
  }

}
