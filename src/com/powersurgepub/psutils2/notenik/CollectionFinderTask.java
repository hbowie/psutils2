/*
 * Copyright 2018 Herb Bowie
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

  import com.powersurgepub.psutils2.files.*;

  import java.io.*;
  import java.util.*;

  import javafx.application.Platform;
  import javafx.collections.*;
  import javafx.concurrent.*;
  import javafx.stage.DirectoryChooser;

public class CollectionFinderTask
    extends Task<ObservableList<File>> {

  private File                      startingFolder;
  private MasterCollection          master;
  private ArrayList<File>           foldersToSearch;
  private ObservableList<File>      collectionsToAdd;

  /**
   * Construct a new occurrence and pass in needed variables.
   *
   * @param startingFolder The folder in which to scan.
   * @param master The master collection for this user.
   */
  public CollectionFinderTask(File startingFolder, MasterCollection master) {
    this.startingFolder = startingFolder;
    this.master = master;
  }

  /**
   * Search for existing collections and add them to the Master Collection.
   */
  @Override
  public ObservableList<File> call() {
    this.updateTitle("Collections Finder Task");
    collectionsToAdd = FXCollections.<File>observableArrayList();
    foldersToSearch = new ArrayList<>();
    foldersToSearch.add(startingFolder);
    while (foldersToSearch.size() > 0) {
      if (this.isCancelled()) { break; }
      searchNextFolder();
    }
    return collectionsToAdd;
  }

  /**
   * Search the next folder on the stack.
   */
  private void searchNextFolder() {
    File nextFolder = foldersToSearch.get(0);
    File[] files = nextFolder.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (this.isCancelled()) { break; }
      File nextFile = files[i];
      String nextLower = nextFile.getName().toLowerCase();
      if (nextFile.isHidden()) {
        // Skip hidden files and folders
      } else if (nextFile.isDirectory()) {
        if (nextLower.contains("archive")
            || nextLower.contains("backup")
            || nextLower.equals("deploy")
            || nextLower.equals("dist")
            || nextLower.equals("icons")
            || nextLower.equals("jars")
            || nextFile.getName().equalsIgnoreCase("Library")
            || nextFile.getName().equalsIgnoreCase("Music")
            || nextFile.getName().equalsIgnoreCase("Pictures")
            || nextFile.getName().equalsIgnoreCase("PSPub Omni Pack")
            || nextFile.getName().endsWith(".app")) {
          // Let's not search certain folders
        } else {
          foldersToSearch.add(nextFile);
        }
      } else if (nextFile.getName().equals(NoteIO.README_FILE_NAME)) {
        String line = "";
        boolean notenikLineFound = false;
        try {
          FileReader fileReader = new FileReader(nextFile);
          BufferedReader reader = new BufferedReader(fileReader);
          line = reader.readLine();
          while (line != null && (! notenikLineFound)) {
            int j = line.indexOf(NoteIO.README_LINE_1);
            notenikLineFound = (j >= 0);
            line = reader.readLine();
          }
        } catch(IOException e) {
          // Ignore
        }
        if (notenikLineFound) {
          FileSpec collectionSpec = master.getFileSpec(nextFolder);
          if (collectionSpec == null) {
            collectionsToAdd.add(nextFolder);
          }
        } // end if notenik line found within README file
      } // end if we found a README file
    } // end for each file in directory
    foldersToSearch.remove(0);
  }

  @Override
  protected void cancelled() {
    super.cancelled();
    updateMessage("The task was cancelled.");
  }

  @Override
  protected void failed() {
    super.failed();
    updateMessage("The task failed.");
  }

  @Override
  public void succeeded() {
    super.succeeded();
    updateMessage("The task finished successfully.");
  }
}
