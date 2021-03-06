/*
 * Copyright 2009 - 2017 Herb Bowie
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
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;

  import java.io.*;
  import java.util.*;
  import javafx.scene.control.*;

/**
 The data model for a collection of notes. This class coordinates
 changes to the collection. Whenever a particular note is modified, the note is
 updated on disk, in its sync folder (where applicable), in memory, and in 
 all lists and indexes maintained by the model. This class replaced 
 NoteList as part of the Notenik JavaFX rewrite. 

 @author Herb Bowie
 */
public class NoteCollectionModel {

  private             CollectionInfo          collectionInfo = null;
  
  private             NoteCollectionTemplate  template = null;
  
  private             NoteCollectionView      view;
  
  private             Home                    home;
  private             Logger                  logger;
  private             FilePrefs               filePrefs;
  
  private             MasterCollection        master;
  private             boolean                 editingMasterCollection = false;
  
  private             boolean                 open = false;
  private             FileSpec                fileSpec;
  private             FileName                fileName;
  private             String                  title = "Notes";
  private             FolderSyncPrefsData     syncPrefs;
  
  private             NoteSortParm            sortParm = null;
  private             NoteIO                  noteIO = null;
  private             boolean                 loadTaggedOnly = false;
  
  private             NoteCollectionList      list;
  private             NoteCollectionMap       map;
  private             NoteCollectionSorted    sorted;
  private             TagsList                tagsList;
  private             TagsView                tagsView;
  private             AuthorList              authorList;
  private             WorkList                workList;
  
  private             Note                    selectedNote = null;
  private             int                     selectedSortIndex = 0;
  private             String                  selectedTitle = "";
  private             String                  selectedUniqueKey = "";
  private             String                  selectedSortKey = "";
  private             String                  selectedTags = "";
  private             String                  selectedFileName = "";
  private             DataValueSeq            selectedSeq = null;
  
  private             String                  oldSeq = "";
  
  private             boolean                 quoteCollection = false;
  
  /**
   Create a new Model. This should be done whenever we're switching from one 
   Notes Collection to another. 
  */
  public NoteCollectionModel(NoteCollectionView view) {
    
    this.view = view;
    
    logger = Logger.getShared();
    home = Home.getShared();
    filePrefs = null;
    
    master = new MasterCollection();
    sortParm  = new NoteSortParm();
    sortParm.setModel(this);
    newCollection();
    
  }
  
  /* ============================================================================
   *  
   * Initialization routines.  
   *
   * =========================================================================*/
  
  /**
   Initialize collection values at startup, and every time we open 
   a new collection.   
  */
  private void newCollection() {

    deselect();
    title     = "Notes";
    collectionInfo = new CollectionInfo();
    noteIO    = new NoteIO();
    loadTaggedOnly = false;
    
    list       = new NoteCollectionList();
    map        = new NoteCollectionMap();
    sorted     = new NoteCollectionSorted(sortParm);
    tagsList   = new TagsList();
    tagsView   = new TagsView();
    authorList = new AuthorList();
    workList   = new WorkList();
    
    tagsList.registerValue("");
    authorList.registerValue("");
    workList.registerValue("");
    
    open = false;
    fileSpec = null;
    fileName = null;
    syncPrefs = new FolderSyncPrefsData();
  }
  
  /**
   Indicate that no note is currently selected.
  */
  public void deselect() {
    selectedNote = null;
    selectedTitle = "";
    selectedTags = "";
    selectedUniqueKey = "";
    selectedSortKey = "";
    selectedFileName = "";
    selectedSeq = null;
  }
  
  /**
   Set the Sort sortMenu that will be used to pick one of the sort options. 
  
   @param sortMenu The sort sortMenu to be used. 
  */
  public void setSortMenu(Menu sortMenu) {
    sortParm.setSortMenu(sortMenu);
  }
  
  public void setFilePrefs(FilePrefs filePrefs) {
    this.filePrefs = filePrefs;
  }
  
  /**
   Open a file when Notenik first launches. 
  
   @param openStartupTags Has the user requested notes with a startup tag
   to be launched at startup?
  
   @return True if ok, false if problems. 
  */
  public boolean openAtStartup(boolean openStartupTags) {

    open = false;
    FileSpec lastFileSpec = filePrefs.getStartupFileSpec();
    String lastFolderString = filePrefs.getStartupFilePath();
    String lastTitle = "";
    if (lastFolderString != null
        && lastFolderString.length() > 0) {
      File lastFolder = new File (lastFolderString);
      if (goodFolder(lastFolder)) {
        if (lastFileSpec != null) {
          lastTitle = lastFileSpec.getLastTitle();
        }
        openStart (lastFileSpec, false);
        openFinish();
        if (openStartupTags) {
          launchStartupURLs();
        }
      }
    }
    return open;
  }
  
  /**
   Open the Master Collection for review and use by the user. 
  
   @return True if we opened successfully. 
  
   @throws NoteCollectionException If we get a bum folder. 
  */
  public boolean openMasterCollection() 
      throws NoteCollectionException {

    if (master.hasMasterCollection()) {
      File masterFile = master.getMasterCollectionFolder();
      FileSpec masterSpec = master.getFileSpec(masterFile);
      if (masterSpec == null) {
        masterSpec = new FileSpec(masterFile);
      }
      String errMsg = folderError(masterFile);
      if (errMsg == null) {
        close();
        openStart(masterSpec, false);
        return openFinish ();
      } else {
        throw new NoteCollectionException("Master Collection Folder: " + errMsg);
      }
    } else {
      return false;
    } // end if we have a master collection

  }
  
  /**
   Set this flag before an open if the user wishes to exclude notes
   with no tags. 
  
   @param loadTaggedOnly False to load all notes, true to load only notes
                         with tags. 
  */
  private void setLoadTaggedOnly(boolean loadTaggedOnly) {
    this.loadTaggedOnly = loadTaggedOnly;
  }
  
  /**
   Begin opening a collection.
  */
  public void openStart(FileSpec fileSpec, boolean taggedOnly) {
    
    newCollection();
    setLoadTaggedOnly(taggedOnly);
    this.fileSpec = fileSpec;
    fileName = new FileName(fileSpec.getFolder());
    if (master.hasMasterCollection()
        && fileSpec.getFile().equals(master.getMasterCollectionFolder())) {
      editingMasterCollection = true;
    } else {
      editingMasterCollection = false;
    }
    
    logNormal("Opening folder " + fileSpec.getFile().toString());
    collectionInfo.setFolder(fileSpec.getFile());
    boolean infoFound = collectionInfo.readFromDisk();
    collectionInfo.setTitle(fileSpec.getCollectionTitle());
    collectionInfo.setNoteSortParm(fileSpec.getNoteSortParm());
    noteIO = new NoteIO(fileSpec.getFile(), NoteParms.NOTES_ONLY_TYPE);
    template = new NoteCollectionTemplate(fileSpec.getFile());
    quoteCollection = template.isQuoteTemplate();
  }

  /**
   Finish opening a collection.

   @return True if everything worked out ok.
   */
  public boolean openFinish() {
    
    boolean openOK = true;
    NoteParms templateParms = template.getNoteParms();
    if (templateParms != null) {
      noteIO = new NoteIO (fileSpec.getFile(), templateParms);
    } 
    sortParm.setParm(fileSpec.getNoteSortParm());
    sorted.genSortedList(list);
    try {
      loadFromDisk();
      if (syncPrefs.getSync()) {
        syncWithFolder();
      }
    } catch (IOException e) {
      logException(e);
      openOK = false;
    } catch (NoteCollectionException e) {
      logException(e);
      openOK = false;
    }
    
    addFirstNoteIfListEmpty();

    String lastTitle = collectionInfo.getLastKey();
    lastTitle = fileSpec.getLastTitle();
    if (lastTitle != null && lastTitle.length() > 0) {
      Note lastNote = map.getFromTitle(lastTitle);
      if (lastNote != null) {
        select(lastNote);
      }
    }
    if (! hasSelection()) {
      Note firstNote = sorted.get(0);
      select(firstNote);
    }
    
    if (openOK) {
      this.fileSpec = master.addRecentFile(fileSpec.getFolder());
    }

    open = openOK;

    return openOK;
  }
  
  public NoteCollectionTemplate getTemplate() {
    return template;
  }

  /**
   * Return the template file found, if any.
   * @return The template file found, or null if no template file found.
   */
  public File getTemplateFile() {
    if (template == null) {
      return null;
    } else {
      return template.getTemplateFile();
    }
  }
  
  /**
   The current specification for the current Collection of Notes. 
  
   @return The file spec for the collection that is currently open. 
  */
  public FileSpec getFileSpec() {
    return fileSpec;
  }
  
  /**
   Get the FileName object for the folder containing the current collection. 
  
   @return The file name of the current collection.
  */
  public FileName getFileName() {
    return fileName;
  }
  
  /**
   Set the title of the collection. 
  
   @param title The title of the collection. 
  */
  public void setTitle (String title) {
    this.title = title;
  }

  /**
   Get the title of the collection. 
  
   @return The title of the collection. 
  */
  public String getTitle () {
    return title;
  }
  
  public void setSyncPrefs(FolderSyncPrefsData syncPrefs) {
    this.syncPrefs = syncPrefs;
  }
  
  /**
   Get a File object pointing to a file in the sync folder, given 
   the appropriate info about the sync preferences.
   
   @param title         The original title of the note. 
  
   @return A file object pointing to the specific sync file for this note. 
  */
  public File getSyncFile (String title) {
    File syncFolder = new File(syncPrefs.getSyncFolder());
    return new File(syncFolder, syncPrefs.getSyncPrefix() 
        + title + noteIO.getNoteParms().getPreferredFileExt());
  }
  
  /**
   Are we currently editing the master collection? 
  
   @return Editing the master collection. 
  */
  public boolean editingMasterCollection() {
    return editingMasterCollection;
  }
  
  /**
   Get the disk locaton where the notes are stored. 
  
   @return The folder storing the notes on disk. 
  */
  public File getFolder() {
    if (open) {
      return fileSpec.getFolder();
    } else {
      return null;
    }
  }

  /**
   * Get the disk location containing the file attachments.
   *
   * @return The Note Attachments folder for this collection.
   */
  public File getAttachmentsFolder() {
    File notesFolder = getFileSpec().getFolder();
    File attachmentsFolder = new File(notesFolder, NoteIO.FILES_FOLDER_NAME);
    return attachmentsFolder;
  }
  
  /**
   Close the last opened collection.
  
   @return True if we had a collection to close. 
  */
  public boolean close() {
    boolean closed = false;
    if (open) {
      fileSpec.setNoteSortParm(sortParm.getParm());
      if (filePrefs != null) {
        filePrefs.handleClose();
      }
      collectionInfo.setNoteSortParm(sortParm.getParm());
      boolean infoWritten = collectionInfo.writeToDisk();
    }
    deselect();
    open = false;
    closed = true;
    return closed;
  }
  
  /**
   Do we currently have a valid collection that is open for business?
  
   @return True if open for business, false otherwise. 
  */
  public boolean isOpen() {
    return open;
  }
  
  /**
   Return the total number of notes in the collection. 
  
   @return Size of the notes list. 
  */
  public int size() {
    return list.size();
  }
  
  /**
   Get the size of the sorted list, after deletions and filtering. 
  
   @return Size of the sorted list. 
  */
  public int sortedSize() {
    return sorted.size();
  }
  
  /**
   If requested, launch any Note's link that has been tagged with "startup"
  */
  private void launchStartupURLs() {
    Note next;
    Tags tags;
    String tag;
    for (int i = 0; i < list.size(); i++) {
      next = list.get(i);
      tags = next.getTags();
      TagsIterator iterator = new TagsIterator (tags);
      while (iterator.hasNextTag()) {
        tag = iterator.nextTag();
        if (tag.equalsIgnoreCase("Startup")) {
          home.openURL(next.getLinkAsString());
        }
      }
    }
  } // end method launchStartupURLs
  
  /**
   Set the primary disk location in which this collection's notes are to 
   be stored. This would normally be done as soon as the the disk location
   for the collection has been identified. 
  
   @param folder The folder in which we should store notes. 
  
   @throws NoteCollectionException If there is a problem with the passed
                                   disk location. 
  */
  public void setDiskLocation(File folder) 
      throws NoteCollectionException {
    String errorMsg = folderError(folder);
    if (errorMsg == null || errorMsg.length() == 0) {
      noteIO = new NoteIO(folder, NoteParms.NOTES_ONLY_TYPE);
    } else {
      throw new NoteCollectionException(errorMsg);
    }
  }
  
  /**
   Get the sort parms object being used. 
  
   @return The sort parms object being used for this collection. 
  */
  public NoteSortParm getSortParm() {
    return sortParm;
  }
  
  /**
   Called to indicate that the user's selected sort parm has been changed. 
  */
  public void sortParmChanged() {
    sorted.genSortedList(list);
    if (selectedNote != null) {
      selectedSortKey = selectedNote.getSortKey(sortParm);
    }
    view.newViews();
    fileSpec.setNoteSortParm(sortParm.getParm());
    collectionInfo.setNoteSortParm(sortParm.getParm());
  }
  
  /**
   Create a Master Collection for the user, assuming he does not yet have one. 
  
   @param selectedFile The folder in which to store the master collection. 
  
   @return The number of collections save to the Master Collection, or -1
           if the file supplied was not a good location. 
  */
  public int createMasterCollection(File selectedFile) {
    if (goodFolder(selectedFile)) {
      return master.createMasterCollection(selectedFile);
    } else {
      return -1;
    }
  }
  
  public MasterCollection getMaster() {
    return master;
  }
  
  /**
   Load a collection of notes into the various lists stored in memory. 
  
   @throws IOException If we have problems reading from disk. 
   @throws NoteCollectionException If we have duplicates, or some other
                                   logical problem. 
  */
  public void loadFromDisk() 
      throws 
        IOException,
        NoteCollectionException {
    
    if (loadTaggedOnly) {
      Logger.getShared().recordEvent(LogEvent.NORMAL, 
        "Loading only Notes with Tags", false);
    }
    if (noteIO == null) {
      throw new NoteCollectionException
        ("Attempt to Load from Unspecified Disk Location");
    } else {
      int notesLoaded = 0;
      noteIO.openForInput();
      Note noteFromDisk = noteIO.readNextNote();
      while (noteFromDisk != null) {
        if (noteFromDisk.hasTags() || (! loadTaggedOnly)) {
          boolean added = addToMemory(noteFromDisk);
          if (added) {
            notesLoaded++;
          } else {
            Logger.getShared().recordEvent(LogEvent.MEDIUM,
                "Could not load duplicate title found at: " + noteFromDisk.getDiskLocation(),
                false);
          }
        }
        noteFromDisk = noteIO.readNextNote();
      }
      noteIO.loadAttachments(this);

      noteIO.close();

      Logger.getShared().recordEvent(LogEvent.NORMAL, 
          String.valueOf(notesLoaded) + " Notes loaded", false);
    }
  }
  
  /**
   Add one note if the list is empty. 
  
   @return The note added, or null, if nothing added. 
  */
  public Note addFirstNoteIfListEmpty() {
    if (list.size() == 0) {
      return addFirstNote();
    } else {
      return null;
    }
  }

  /**
   Add the first note to an empty collection. 
  
   @return The first note added, or null if any problems trying to add. 
  */
  public Note addFirstNote() {
    deselect();
    Note firstNote = getNewNote();
    firstNote.setTitle("Notenik.net");
    firstNote.setLink("http://www.notenik.net/");
    firstNote.setTags("Software.Java.Groovy");
    firstNote.setBody("Home to Notenik");

    boolean saved = saveNote(firstNote);
    if (saved) { 
      saved = addToMemory(firstNote);
    }
    if (saved) {
      select(firstNote);
      return firstNote;
    } else {
      return null;
    }
  }
  
  /**
   Find the sorted note whose sort key matches that of the passed note. 
  
   @param findNote The note we're looking for. 
  
   @return The SortedNote with a matching key, or null if not match. 
  */
  public SortedNote getSortedNote(Note findNote) {
    return sorted.getSortedNote(findNote);
  }
  
  /** 
   Get a brand new note with empty values. 
  
   @return A brand new note. 
  */
  public Note getNewNote() {
    Note newNote = new Note(getRecDef());
    int newID = list.size();
    newNote.setCollectionID(newID);
    if (noteIO.getNoteParms().isDateAddedExplicit()) {
      newNote.setDateAdded(StringDate.getNowYMDHMS());
    }
    return newNote;
  }
  
  /**
   Add a new note to the model, saving to disk and adding to lists in memory.
  
   @param newNote The new note to be added. 
  
   @return True if add ok, false if problems. 
  */
  public boolean add(Note newNote) {
    boolean saved = saveNote(newNote);
    if (saved) { 
      saved = addToMemory(newNote);
    }
    return saved;
  }
  
  /**
   Add a new note to lists stored in memory, assuming it is coming from disk, 
   and so does not need to be stored to disk. 
  
   @param newNote 
  
   @return True if successfully added, false if a duplicate. 
  */
  public boolean addToMemory(Note newNote) {
    boolean duplicate = map.contains(newNote);
    boolean added = false;
    if (duplicate) {
      added = false;
    } else {
      list.add(newNote);
      map.add(newNote);
      sorted.add(newNote);
      tagsList.add(newNote);
      tagsView.add(newNote);
      authorList.add(newNote);
      workList.add(newNote);
      added = true;
    }
    return added;
  }
  
  /**
   Does this collection already contain a note with this unique key?
  
   @param uniqueKey The key of interest.
  
   @return True if note with this key already exists, false if no
           matching key could be found. 
  */
  public boolean contains(String uniqueKey) {
    return map.contains(uniqueKey);
  }
  
  /**
   Get the first note in the basic list, skipping any that have been deleted. 
  
   @return Index to the first note in the basic list, skipping any deletions. 
  */
  public int firstNote() {
    int i = 0;
    Note noteToTest = get(i);
    while (noteToTest != null && noteToTest.isDeleted()) {
      i++;
      noteToTest = get(i);
    }
    if (noteToTest == null) {
      return -1;
    } else {
      return i;
    }
  }
  
  /**
   Get the next note in the basic list, skipping any that have been deleted. 
  
   @param start The starting index. 
  
   @return Index to the next note in the basic list, skipping any deletions. 
  */
  public int nextNote(int start) {
    int i = start + 1;
    Note noteToTest = get(i);
    while (noteToTest != null && noteToTest.isDeleted()) {
      i++;
      noteToTest = get(i);
    }
    if (noteToTest == null) {
      return -1;
    } else {
      return i;
    }
  }
  
  /**
   Get a note based on its tile. 
  
   @param title The title of a note. 
  
   @return The note with that title, if any, otherwise null. 
  */
  public Note getFromTitle(String title) {
    return map.getFromTitle(title);
  }
  
  /**
   Get a note from the plain, unsorted list of all notes. 
  
   @param i The index value.
  
   @return The Note at the indicated index, or null if 
           no note available at this time for this index.
  */
  public Note get(int i) {
    if (open && i >= 0 && i < list.size()) {
      return list.get(i);
    } else {
      return null;
    }
  }

  /**
   * Return the pointer to the entire note list.
   *
   * @return The note list.
   */
  public NoteCollectionList getNoteList() {
    return list;
  }
  
  public Note getSorted(int i) {
    if (open && i >= 0 && i < sorted.size()) {
      return sorted.get(i);
    } else {
      return null;
    }
  }
  
  /**
   Find the position of the given note within the sorted list.
  
   @param note The note we're looking for. 
  
   @return The position of the note within the sorted list, or -1
           if no matching sort key found. 
  */
  public int findSorted(Note note) {
    return sorted.findSorted(note);
  }
  
  /**
   Find the position of the note with the given sort key within the sorted list. 
  
   @param findKey The sort key of the note we're looking for. 
  
   @return The position of the note within the sorted list, or -1
           if no matching sort key found. 
  */
  public int findSorted(String findKey) {
    return sorted.findSorted(findKey);
  }
  
  /**
   Remove the selected note from the model, deleting its disk file. 
  
   @return True if everything ok, false if problems.
  */
  public boolean removeSelection() {
    String titleToRemove = "";
    boolean ok = hasSelection();
    if (ok) {
      titleToRemove = selectedNote.getTitle();
      ok = remove(selectedNote);
    }
    if (ok && editingMasterCollection) {
      master.removeRecentFile(titleToRemove);
    }
    if (ok && syncPrefs.getSync()) {
      File syncFile = noteIO.getSyncFile(
          syncPrefs.getSyncFolder(), 
          syncPrefs.getSyncPrefix(), 
          titleToRemove);
      syncFile.delete();
    }
    return ok;
  }
  
  /**
   Remove this note from the model, deleting its disk file. 
  
   @param noteToRemove 
  */
  public boolean remove(Note noteToRemove) {

    boolean sortedOK  = sorted.remove(noteToRemove);
    boolean mapOK     = map.remove(noteToRemove);
    boolean listOK    = list.remove(noteToRemove);
    tagsView.remove(noteToRemove);
    boolean deleted   = false;
    if (noteToRemove.hasDiskLocation()) {
      String locToDelete = noteToRemove.getDiskLocation();
      File fileToDelete = new File(locToDelete);
      deleted = fileToDelete.delete();
    }
    return (sortedOK && mapOK && listOK && deleted);
  }
  
  /**
   Return a table view that can be used to view the sorted list. 
  
   @return A table view for the list. 
  */
  public TableView getTableView() {
    return sorted.getTableView();
  }
  
  /**
   Get the tree view of the tags and their notes. 
  
   @return The tree view of the tags and their notes. 
  */
  public TreeView getTree() {
    return tagsView.getTreeView();
  }
  
  /**
   Expand all the nodes in the tags view. 
  */
  public void expandAll() {
    tagsView.expandAll();
  }
  
  /**
   Collapse all the nodes in the tags view, except for the first level 
   beneath the root. 
  */
  public void collapseAll() {
    tagsView.collapseAll();
  }
  
  /**
   Get the NoteParms instance currently being used for this collection. 
  
   @return The current NoteParms instance. 
  */
  public NoteParms getNoteParms() {
    return noteIO.getNoteParms();
  }
  
  /**
     Returns the record definition for the collection.
    
     @return Record definition for this collection.
   */
  public RecordDefinition getRecDef () {
    return noteIO.getRecDef();
  }
  
  /**
   Return the number of fields defined in the record definition. 
  
   @return The number of fields/columns in the record definition. 
  */
  public int getNumberOfFields() {
    return getRecDef().getNumberOfFields();
  }
  
  public TagsList getTagsList () {
    return tagsList;
  }

  public TagsView getTagsModel () {
    return tagsView;
  }

  public AuthorList getAuthorList () { return authorList; }

  public WorkList getWorkList () { return workList; }
  
  /* ============================================================================
   *  
   * The methods in this section deal with a single selected note 
   * from the collection that has been explicitly identified for
   * possible changes. 
   *
   * =========================================================================*/
  
  /**
   Get the title of the note following this one, based on the current sort.
  
   @return The title of the note following this one, based on the current sort.
  */
  public String nextTitle() {
    int i = sorted.findSorted(selectedNote);
    if (i < sorted.size()) {
      i++;
    }
    if (i < 0) {
      i = 0;
    } 
    else
    if (i >= sorted.size()) {
      i = sorted.size() - 1;
    }
    return (sorted.get(i).getTitle());
  }
  
  /**
   Get the title of the note prior to this one, based on the current sort.
  
   @return The title of the note prior to this one, based on the current sort.
  */
  public String priorTitle() {
    int i = sorted.findSorted(selectedNote);
    if (i > 0) {
      i--;
    }
    if (i < 0) {
      i = 0;
    } 
    else
    if (i >= sorted.size()) {
      i = sorted.size() - 1;
    }
    return (sorted.get(i).getTitle());
  }
  
  /**
   Get the title of the first note, based on the current sort.
  
   @return The title of the first note, based on the current sort.
  */
  public String firstTitle() {
    int i = 0;
    return (sorted.get(i).getTitle());
  }
  
  /**
   Get the title of the last note, based on the current sort.
  
   @return The title of the last note, based on the current sort.
  */
  public String lastTitle() {
    int i = sorted.size() - 1;
    return (sorted.get(i).getTitle());
  }
  
  /**
   Select the note with the given title.
  
   @param titleToSelect The title to select.
  
   @return The Note selected, or null if title not found.
  */
  public Note select(String titleToSelect) {
    Note noteToSelect = map.getFromTitle(titleToSelect);
    if (noteToSelect == null) {
      System.out.println("  model could not find a note with this title");
      return null;
    } else {
      select (noteToSelect);
      return noteToSelect;
    }
  }
  
  /**
   Selected the note at this index position in the basic list. 
  
   @param i Index into the basic list. 
  */
  public void select(int i) {
    Note noteToSelect = get(i);
    if (noteToSelect != null) {
      select(noteToSelect);
    }
  }
  
  /**
   Select a particular note for possible future actions. 
  
   @param noteToSelect The note being selected. 
  */
  public void select(Note noteToSelect) {
    selectedNote = noteToSelect;
    updateSelection();
  }
  
  /**
   Update saved information about the selected note. 
  */
  public void updateSelection() {
    selectedTitle = selectedNote.getTitle();
    selectedTags = selectedNote.getTagsAsString();
    selectedUniqueKey = selectedNote.getUniqueKey();
    selectedSortKey = selectedNote.getSortKey(sortParm);
    selectedSortIndex = sorted.findSorted(selectedSortKey);
    selectedFileName = selectedNote.getFileName();
    selectedSeq = selectedNote.getSeqValue();
    
    oldSeq = "";
    
    if (selectedNote.hasTitle() && isOpen()) {
      fileSpec.setLastTitle(selectedNote.getTitle());
      collectionInfo.setLastKey(selectedNote.getTitle());
    }
  }
  
  /**
   Let's save the modified note in all of the right places. 
  
   @return True if everything went ok. 
  */
  public boolean modifySelection() {

    boolean saveOK = true;
    selectedNote.setLastModDateToday();
    saveSelectionAndDeleteOnRename();
    if (selectionIsNew()) {
      if (selectedNote.hasUniqueKey()) {
        addToMemory(selectedNote);
      } // end if we have newNote worth adding
    } else {
      modifyMemoryForSelection();
    }

    if (editingMasterCollection) {
      master.modRecentFile(selectedTitle, selectedNote.getTitle(), selectedNote.getSeq());
      CollectionInfo collectionInfo = new CollectionInfo();
      collectionInfo.setFolder(selectedNote.getLinkAsFile());
      collectionInfo.readFromDisk();
      collectionInfo.setTitle(selectedNote.getTitle());
      collectionInfo.setTags(selectedNote.getTagsAsString());
      collectionInfo.setSeq(selectedNote.getSeq());
      collectionInfo.setBody(selectedNote.getBody());
      collectionInfo.writeToDisk();
    }
    return saveOK;
  }
  
  /**
   Modify the given note on disk and in memory, based on current Note and prior
   values for various key fields. 
  
   @param note The note to be saved. 
   @param priorTitle   The prior version of the title, before modifications. 
   @param priorSortKey The prior version of the sort key, before mods. 
   @param priorTags    The prior version of the tags, before mods. 
  
   @return True if everything ok. 
  */
  public boolean modify(
      Note note, String priorTitle, String priorSortKey, String priorTags) {
    boolean saveOK = true;
    note.setLastModDateToday();
    saveNoteAndDeleteOnRename(note, priorTitle);
    sortParm.maintainSeqStats(selectedNote.getSeqValue());
    boolean uniqueKeyChanged = false;
    boolean sortKeyChanged = false;
    boolean tagsChanged = false;
    
    if (! Note.makeUniqueKey(priorTitle).equals(note.getUniqueKey())) {
      map.remove(selectedUniqueKey);
      map.add(selectedNote);
      uniqueKeyChanged = true;
    }
    
    if (! priorSortKey.equals(note.getSortKey(sortParm))) {
      sorted.remove(selectedSortKey);
      sorted.add(selectedNote);
      sortKeyChanged = true;
    }
    
    if (! priorTags.equals(note.getTagsAsString())) {
      tagsChanged = true;
    }
    
    if (uniqueKeyChanged
        || sortKeyChanged
        || tagsChanged) {
      tagsList.modify(note);
      tagsView.modify(note);
      authorList.modify(note);
    }
    workList.modify(note);
    if (editingMasterCollection) {
      master.modRecentFile(priorTitle, note.getTitle(), note.getSeq());
    }
    return saveOK;
  }
  
  private void modifyMemoryForSelection() {

    sortParm.maintainSeqStats(selectedNote.getSeqValue());
    
    if (uniqueKeyChanged()) {
      map.remove(selectedUniqueKey);
      map.add(selectedNote);
    }
    
    if (sortKeyChanged()) {
      boolean removed = sorted.remove(selectedSortKey);
      if (removed) {
        sorted.add(selectedNote);
      }
    }
    
    if (uniqueKeyChanged()
        || sortKeyChanged()
        || tagsChanged()) {
      tagsList.modify(selectedNote);
      tagsView.modify(selectedNote);
      authorList.modify(selectedNote);
    }

    workList.modify(selectedNote);
    
  }
  
  public int getSelectedID() {
    if (open && selectedNote != null) {
      return selectedNote.getCollectionID();
    } else {
      return -1;
    }
  }
  
  public String getSelectedTitle() {
    return selectedTitle;
  }
  
  public boolean selectionIsNew() {
    return (selectedTitle == null || selectedTitle.length() == 0);
  }
  
  public String getSelectedTags() {
    return selectedTags;
  }
  
  public boolean tagsChanged() {
    return (! selectedNote.getTagsAsString().equals(selectedTags));
  }
  
  public String getSelectedUniqueKey() {
    return selectedUniqueKey;
  }
  
  public boolean uniqueKeyChanged() {
    return (! selectedNote.equalsUniqueKey(selectedUniqueKey));
  }
  
  public String getSelectedSortKey() {
    return selectedSortKey;
  }
  
  public boolean sortKeyChanged() {
    return (! selectedNote.getSortKey(sortParm).equalsIgnoreCase(selectedSortKey));
  }
  
  public int getSelectedSortIndex() {
    return selectedSortIndex;
  }
  
  public String getSelectedFileName() {
    return selectedFileName;
  }
  
  public boolean fileNameChanged() {
    String newFileName = selectedNote.getFileName();
    if ((newFileName.equalsIgnoreCase(selectedFileName))
        && (newFileName.equalsIgnoreCase(selectedNote.getDiskLocationBase()))) {
      return false;
    } else {
      return true;
    }
  }
  
  public DataValueSeq getSelectedSeq() {
    return selectedSeq;
  }
  
  /**
   Does the selected note already exist on disk?
  
   @return True if a note already exists on disk  at this location. 
  */
  public boolean selectedExists () {
    return noteIO.exists(selectedNote.getFileName());
  }
  
  /**
   Save the selected note, and if it now has a new disk location, 
   delete the file at the old disk location.
  */
  public void saveSelectionAndDeleteOnRename() {
    saveNoteAndDeleteOnRename(selectedNote, selectedTitle);
  }
  
  /**
   Save the note, and if it now has a new disk location, 
   delete the file at the old disk location. 
  
   @param noteToSave The note to be saved.
  */
  public void saveNoteAndDeleteOnRename(Note noteToSave, String oldTitle) {

    String oldDiskLocation = noteToSave.getDiskLocation();
    saveNote(noteToSave);
    String newDiskLocation = noteToSave.getDiskLocation();

    if (! newDiskLocation.equals(oldDiskLocation)) {

      File oldDiskFile = new File (oldDiskLocation);
      oldDiskFile.delete();

      if (noteToSave.hasAttachments()) {
        File attachmentsFolder = getAttachmentsFolder();
        String oldNoteFileName = StringUtils.makeReadableFileName(oldTitle);
        String newNoteFileName = noteToSave.getFileName();
        for (int i = 0; i < noteToSave.getNumberOfAttachments(); i++) {
          NoteAttachment attachment = noteToSave.getAttachment(i);
          String oldAttachmentName = oldNoteFileName + attachment.getSuffixAndExtension();
          File oldAttachmentFile = new File(attachmentsFolder, oldAttachmentName);
          String newAttachmentName = attachment.getFileName();
          File newAttachmentFile = new File(attachmentsFolder, newAttachmentName);
          boolean renameSuccessful = oldAttachmentFile.renameTo(newAttachmentFile);
          if (! renameSuccessful) {
            logger.recordEvent (LogEvent.MEDIUM,
                "Could not rename attachment '"
                + oldAttachmentFile.toString()
                + "' to '"
                + newAttachmentFile.toString()
                + "'",
                false);
          }  // end if not rename successful
        } // end for each attachment
      } // end if we have any attachments

      if (syncPrefs.getSync()) {
        File oldSyncFile = noteIO.getSyncFile(
            syncPrefs.getSyncFolder(), 
            syncPrefs.getSyncPrefix(), 
            oldTitle);
        oldSyncFile.delete();
      } // End if sync folder needs updating
    } // end if disk location changed
  } // end method saveNoteAndDeleteOnRename
  
  /**
   Saves a newNote in its primary location and in its sync folder, if specified. 
  
   @param note The newNote to be saved. 
  */
  protected boolean saveNote(Note note) {
    try {
      noteIO.save(note, true);
      if (syncPrefs.getSync()) {
        noteIO.saveToSyncFolder(
            syncPrefs.getSyncFolder(), 
            syncPrefs.getSyncPrefix(), 
            note);
        note.setSynced(true);
      }
      return true;
    } catch (IOException e) {
      logException(e);
      return false;
    }
  }
  
  /**
   Save the passed note to the passed text line writer. 
  
   @param note The note to be saved. 
   @param outWriter The writer to use for saving the note. 
  
   @return True if the save was successful. 
  */
  public boolean save (Note note, TextLineWriter outWriter) {
    if (open) {
      return noteIO.save(note, outWriter);
    } else {
      return false;
    }
  }
  
  /**
   Read one note the passed line reader and return it as a note object. 
  
   @param lineReader A reader for a note.  
   @return A Note object. 
  */
  public Note getNote(TextLineReader lineReader) {
    
    return noteIO.getNote(lineReader);
  }
  
  /**
   Does this note already exist on disk?
  
   @param localPath The path that would be used for the note. 
  
   @return True if a note already exists on disk  at this location. 
  */
  public boolean exists (String localPath) {
    return noteIO.exists(localPath);
  }
  
  /**
   Do we currently have a selected note?
  
   @return True if yes. 
  */
  public boolean hasSelection() {
    return (selectedNote != null);
  }
  
  public Note getSelection() {
    return selectedNote;
  }
  
  /**
   Increment the sequence value for the selected note and any notes with 
   equal sequences. 
  
   @return New Sequence Number for Starting Note
  */
  public String incrementSeq() {
    
    DataValueSeq resultSeq = null;
    if (isOpen() && hasSelection()
        && getSortParm().getParm() == NoteSortParm.SORT_BY_SEQ_AND_TITLE) {
      boolean incrementing = true;
      boolean incrementingOnLeft = true;
      ArrayList<DataValueSeq> newSeqs = new ArrayList<>();
      int startingIndex = sorted.findSorted(selectedNote);
      int index = startingIndex;
      SortedNote incSortedNote;
      Note incNote;
      DataValueSeq incSeq;
      DataValueSeq lastSeq = null;
      DataValueSeq newSeq;
      while (incrementing && index < sorted.size()) {
        
        // Get needed fields for current note
        incSortedNote = sorted.getSortedNote(index);
        incNote = incSortedNote.getNote();
        incSeq = incNote.getSeqValue();

        // Special logic for first note processed
        if (index == startingIndex) {
          lastSeq = new DataValueSeq(incSeq);
          resultSeq = lastSeq;
          if (incSeq.getPositionsToRightOfDecimal() > 0) {
            incrementingOnLeft = false;
          }
        }
        
        // See if the current sequence is already greater than the last one
        int result = incSeq.compareTo(lastSeq);
        if (result > 0) {
           if (incrementingOnLeft) {
             result = incSeq.getLeft().compareTo(lastSeq.getLeft());
           }
        }
        
        // See if we're done, or need to keep incrementing
        if (result > 0) {
          incrementing = false;
        } else {
          incrementing = true;
          newSeq = new DataValueSeq(incSeq);
          newSeq.increment(incrementingOnLeft);
          newSeqs.add(newSeq);
          lastSeq = new DataValueSeq(newSeq);
          if (index == startingIndex) {
            resultSeq = lastSeq;
          }
          index++;
        }
      } // end while incrementing
      
      index--;
      
      // Now apply the new sequences from the top down, in order to
      // keep notes from changing position in the sorted list.
      int newSeqIndex = newSeqs.size() - 1;
      while (index >= startingIndex && newSeqIndex >= 0) {
        incSortedNote = sorted.getSortedNote(index);
        incNote = incSortedNote.getNote();
        newSeq = newSeqs.get(newSeqIndex);
        String priorSortKey = incNote.getSortKey(sortParm);
        sorted.remove(priorSortKey);
        incNote.setSeq(newSeq.toString());
        incNote.setLastModDateToday();
        saveNoteAndDeleteOnRename(incNote, incNote.getTitle());
        sorted.add(incNote);
        sortParm.maintainSeqStats(newSeq);
        index--;
        newSeqIndex--;
      }

      // fireTableDataChanged();
    } 
    return resultSeq.toString();
  }
  
  /**
   Record any changes to the Note's sequence value. 
  
   @param modifiedNote The note whose sequence key has changed. 
  
   @param oldSortKey The sort key of the note before the seq was changed. 
  */
  private void modifyNoteSeq(Note modifiedNote, String oldSortKey) {
    saveNoteAndDeleteOnRename(modifiedNote, modifiedNote.getTitle());
    sortParm.maintainSeqStats(modifiedNote.getSeqValue());
    sorted.remove(oldSortKey);
    sorted.add(modifiedNote);
  }
  
  /**
   Sync the list with a Notational Velocity style folder. 
  
   @return True if everything went OK. 
  */
  public boolean syncWithFolder() 
         throws IOException {
    
    boolean ok = true;
    StringBuilder msgs = new StringBuilder();
    
    File syncFolder = null;
    
    // Check to see if we have the info we need to do a sync
    if (syncPrefs == null
        || syncPrefs.getSyncFolder() == null
        || syncPrefs.getSyncFolder().length() == 0
        || syncPrefs.getSyncPrefix() == null
        || syncPrefs.getSyncPrefix().length() == 0
        || (! syncPrefs.getSync())) {
      ok = false;
    }
    
    if (ok) {
      syncFolder = new File (syncPrefs.getSyncFolder());
      if (! goodFolder(syncFolder)) {
        ok = false;
      }
    }
    
    int synced = 0;
    int added = 0;
    int addedToSyncFolder = 0;
    
    if (ok) {  
      
      // Now go through the items on the list and mark them all as unsynced
      Note workNote;
      for (int workIndex = 0; workIndex < list.size(); workIndex++) {
        workNote = list.get (workIndex);
        workNote.setSynced(false);
      }

      // Now match directory entries in the folder with items on the list
      DirectoryReader directoryReader = new DirectoryReader (syncFolder);
      directoryReader.setLog (Logger.getShared());
      directoryReader.openForInput();
      while (! directoryReader.isAtEnd()) {
        File nextFile = directoryReader.nextFileIn();
        FileName nextFileName = new FileName(nextFile);
        if ((nextFile != null) 
            && (! nextFile.getName().startsWith ("."))
            && nextFile.exists()
            && NoteIO.isInterestedIn(nextFile)
            && nextFile.getName().startsWith(syncPrefs.getSyncPrefix())
            && nextFileName.getBase().length() > syncPrefs.getSyncPrefix().length()) {
          String fileNameBase = nextFileName.getBase();
          String nextTitle 
              = fileNameBase.substring(syncPrefs.getSyncPrefix().length()).trim();
          int i = 0;
          boolean found = false;
          while (i < list.size() && (! found)) {
            workNote = list.get(i);
            found = (workNote.getTitle().equals(nextTitle));
            if (found) {
              workNote.setSynced(true);
              Date lastModDate = new Date (nextFile.lastModified());
              if (lastModDate.compareTo(workNote.getLastModDate()) > 0) {
                Note syncNote = noteIO.getNote(nextFile, syncPrefs.getSyncPrefix());
                msgs.append(
                    "Note updated to match more recent info from sync folder for "
                    + syncNote.getTitle()
                    + "\n");
                workNote.setTags(syncNote.getTagsAsString());
                workNote.setLink(syncNote.getLinkAsString());
                workNote.setBody(syncNote.getBody());
                noteIO.save(syncNote, true);
              }
              synced++;
            } else {
              i++;
            }
          } // end while looking for a matching newNote
          if ((! found)) {
            // Add new nvAlt newNote to Notenik collection
            Note syncNote = noteIO.getNote(nextFile, syncPrefs.getSyncPrefix());
            syncNote.setLastModDateToday();
            noteIO.save(syncNote, true);
            add (syncNote);
          }
        } // end if file exists, can be read, etc.
      } // end while more files in sync folder
      directoryReader.close();
    }
      
    if (ok) {
      msgs.append(String.valueOf(added) + " "
          + StringUtils.pluralize("item", added)
          + " added\n");
      
      msgs.append(String.valueOf(synced)  + " existing "
          + StringUtils.pluralize("item", synced)
          + " synced\n");
      
      // Now add any unsynced notes to the sync folder
      Note workNote;
      for (int workIndex = 0; workIndex < list.size(); workIndex++) {
        workNote = list.get(workIndex);
        if (! workNote.isSynced()) {
          workNote.setLastModDateToday();
          saveNote(workNote);
          msgs.append("Added to Sync Folder " + workNote.getTitle() + "\n");
          addedToSyncFolder++;
        }
      } // end of list of notes
      msgs.append(String.valueOf(addedToSyncFolder) + " "
          + StringUtils.pluralize("note", addedToSyncFolder)
          + " added to sync folder\n");
      msgs.append("Folder Sync Completed!\n");
    }
    
    if (ok) {
      logger.recordEvent (LogEvent.NORMAL,
        msgs.toString(),
        false);
    }
    return ok;
  } // end method syncWithFolder
  
  /**
   Assemble the info needed for a backup. 
  
   @return The assembled info. 
  */
  public BackupInfo getBackupInfo() {
    BackupInfo backupInfo = new BackupInfo();
    backupInfo.setSource(fileSpec);
    backupInfo.setToZip();
    backupInfo.setBackupFolder(getBackupFolder());
    backupInfo.setBackupsToKeep(filePrefs);
    return backupInfo;
  }
  
  /**
   Backup without prompting the user. 
  
   @return True if backup was successful. 
  */
  public boolean backupZipWithoutPrompt() {

    BackupInfo backupInfo = getBackupInfo();
    if (open) {
      backupInfo.backupToZip();
      if (backupInfo.okSoFar()) {
        setBackupFolder(backupInfo.getBackupFolder());
        saveLastBackupDate();
        backupInfo.pruneBackups();
        backupInfo.backupsComplete();
      }
    }
    return backupInfo.backupSuccess();
  }
  
  /**
   Save the last used backup folder for this collection. 
  
   @param backupFolder The last used backup folder for this collection. 
  */
  public void setBackupFolder(File backupFolder) {
    if (open) {
      fileSpec.setBackupFolder(backupFolder);
    }
  }
  
  public void saveLastBackupDate() {
    
    fileSpec.setLastBackupDateToNow();
  }
  
  /**
   Return the presumptive folder to be used for backups. 
  
   @return The folder we think the user wishes to use for backups,
           based on his past choices, or on the application defaults.
  */
  public File getBackupFolder() {
    
    File userHome = home.getUserHome();
    File userDocs = home.getUserDocs();   
    File embeddedBackupFolder = null;    
    String lastBackupFolderStr = "";
    File lastBackupFolder = null;
    
    if (open) {   
      FileSpec fileSpec = master.getFileSpec(0);
      embeddedBackupFolder = new File (fileSpec.getFolder(), "backups");
      lastBackupFolderStr = fileSpec.getBackupFolder();
      if (lastBackupFolderStr != null
          && lastBackupFolderStr.length() > 1) {
        lastBackupFolder = new File(lastBackupFolderStr);
      }
    }

    File masterBackupFolder = filePrefs.getMasterBackupFolder();

    File backupFolder;
    if (goodFolder(masterBackupFolder)) {
      backupFolder = masterBackupFolder;
    }
    else
    if (goodFolder(lastBackupFolder)) {
      backupFolder = lastBackupFolder;
    }
    else
    if (goodFolder(embeddedBackupFolder)) {
      backupFolder = embeddedBackupFolder;
    } 
    else 
    if (goodFolder(userHome)) {
      backupFolder = userHome;
    } else {
      backupFolder = userDocs;
    }
    return backupFolder;
  }
  
  public void logException(Exception e) {
    logger.recordEvent
        (LogEvent.MAJOR, "Exception: " + e.getMessage(), false);
  }
  
  public void logNormal (String msg) {
    Logger.getShared().recordEvent (LogEvent.NORMAL, msg, false);
  }
  
  /**
   Is this a valid disk location for a single note? In other words, can a 
   note be read from and written to this disk location?
  
   @param noteFile The file to be tested. 
  
   @return True if good, false if bad. 
  */
  public static boolean goodFile(File noteFile) {
    return (noteFile.exists()
        && noteFile.isFile()
        && noteFile.canRead()
        && noteFile.canWrite());
  }
  
  /** 
   Is this a good folder in which we can store notes?
  
   @param folder The folder in question.
  
   @return True if good, false if no good.
  */
  public static boolean goodFolder(File folder) {
    String errMsg = folderError(folder);
    return (errMsg == null);
  }
  
  /**
   See what's wrong with this folder, an an intended collection storage
   location, if anything. 
  
   @param folder The candidate folder
  
   @return An error message, or null if no problems found.
  */
  public static String folderError(File folder) {
    if (folder == null) {
      return ("Folder is null");
    }
    else
    if (! folder.exists()) {
      return ("Folder could not be found");
    } 
    else
    if (! folder.isDirectory()) {
      return ("Intended folder is not a directory");
    }
    else
    if (! folder.canRead()) {
      return ("Unable to read from this folder");
    }
    else
    if (! folder.canWrite()) {
      return ("Unable to write to this folder");
    } else {
      return null;
    }
  }
  
  /**
   Save any user preferences contained within the model. 
  */
  public void savePrefs() {
    master.savePrefs();
  }

}
