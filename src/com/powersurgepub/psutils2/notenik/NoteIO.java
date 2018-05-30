/*
 * Copyright 2012 - 2017 Herb Bowie
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
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.values.StringDate;

  import java.io.*;
  import java.util.*;

/**
 A class to perform input and output for note files. 

 @author Herb Bowie
 */
public class NoteIO 
    implements DataSource 
  {
  
  public static final String              PARMS_TITLE         = "Collection Parms";
  
  public static final String              README_FILE_NAME    = "- README.txt";

  public static final String              README_LINE_1       =
          "This folder contains a collection of notes created by the Notenik application.";
  public static final String              README_LINE_2       = " ";
  public static final String              README_LINE_3       =
        "Learn more at Notenik.net.";
  
  private static final TemplateFilter     templateFilter      = new TemplateFilter();
  
  private             NoteParms           noteParms 
      = new NoteParms(NoteParms.NOTES_ONLY_TYPE);
  
  private             File                homeFolder          = null;
  private             String              homePath            = "";
  
  private             File                altFolder           = null;
  private             NoteCollectionModel model               = null;
  private             int                 notesLoaded         = 0;
  
  /** Sequential number identifying last record read or written. */
  private             int                 recordNumber;
  
  private             ArrayList<DirToExplode> dirList;
  private             int							    dirNumber = 0;
  
  private             File                currDirAsFile = null;
  private             int                 currDirDepth = 0;
  private             int                 maxDepth = 99;
  
  private             ArrayList<String>   dirEntries;
  private             int							    entryNumber = 0;
  
  private             File                noteFileToRead = null;
  private             Note                nextNote = null;
  
  private             BufferedReader      inBuffered;
  // private             BufferedWriter      outBuffered;
  private             TextLineWriter      writer;
  
  private             int                 ioStyle = IO_STYLE_UNDETERMINED;
  public static final int IO_STYLE_UNDETERMINED   = -1;
  public static final int IO_EXPLICIT             = 0;
  public static final int IO_IMPLICIT             = 1;
  public static final int IO_IMPLICIT_UNDERLINES  = 2;
  public static final int IO_IMPLICIT_FILENAME    = 3;
  
  private             NoteBuilder builder;
  
  // The following fields are used for logging
  
  /** Log to record events. */
  private  Logger       log;
  
  /** Do we want to log all data, or only data preceding significant events? */
  private  boolean      dataLogging = false;
  
  /** Data to be sent to the log. */
  private  LogData      logData;
  
  /** Events to be logged. */
  private  LogEvent     logEvent;
  
  /** Identifier for this file (to be printed in the log as a source ID). */
  private  String       fileId;
  
  public NoteIO () {
    initialize();
  }
  
  /**
     Constructs a NoteIO object.
    
     @param  inPath Directory path to be read.
   */
  public NoteIO (String inPath, int inType) {
    if (inPath.startsWith("http")) {
      noteFileToRead = null;
    } else {
      noteFileToRead = new File (inPath);
    }
    setHomeFolder(null);
    noteParms.setNoteType(inType);
    initialize();
  }

  /**
     Constructs a NoteIO object.
    
     @param  fileOrFolder Directory path to be read.
   */
  public NoteIO (File fileOrFolder, int inType) {
    if (fileOrFolder.isDirectory()) {
      setHomeFolder(fileOrFolder);
    } else {
      noteFileToRead = fileOrFolder;
      setHomeFolder(null);
    }
    noteParms.setNoteType(inType);
    initialize();
  }
  
  public NoteIO (TextLineReader lineReader, int inType) {
    noteFileToRead = null;
    if (lineReader instanceof FileLineReader) {
      FileLineReader fileLineReader = (FileLineReader) lineReader;
      noteFileToRead = fileLineReader.getFile();
    }
    setHomeFolder(null);
    noteParms.setNoteType(inType);
    initialize();
  }
  
  /**
   Construct a new Note I/O module with a home folder, a collection type,
   and a pre-defined record definition. 
  
   @param folder The home folder to use for the collection.
   @param inType The type of collection. 
   @param recDef The record definition to be used for the collection. 
  */
  public NoteIO (File folder, int inType, RecordDefinition recDef) {
    noteParms.setNoteType(inType);
    noteParms.setRecDef(recDef);
    setHomeFolder(folder);
    initialize();
  } 
  
  public NoteIO (File folder) {
    setHomeFolder(folder);
    initialize();
  }
  
  public NoteIO (File folder, NoteParms parms) {
    setHomeFolder(folder);
    noteParms = parms;
    initialize();
  }
  
  /**
     Performs standard initialization for all the constructors.
     By default, fileId is set to "directory".
   */
  private void initialize () {
        
    fileId = "NoteIO";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }
  
  /**
   Indicate whether we are reading a template file. 
  
   @param isTemplate True if reading a template file. 
  */
  public void setIsTemplate(boolean isTemplate) {
    noteParms.setIsTemplate(isTemplate);
  }
  
  /**
   Is this Note I/O instance being used to read a template file?
  
   @return True if we're reading a template file; false otherwise. 
  */
  public boolean isTemplate() {
    return noteParms.isTemplate();
  }
  
  public void setHomeFolder (File homeFolder) {
    this.homeFolder = homeFolder;
    if (homeFolder == null) {
      homePath = "";
    } else {
      try {
        homePath = homeFolder.getCanonicalPath();
      } catch (IOException e) {
        homePath = homeFolder.getAbsolutePath();
      }
      File readMe = new File(homeFolder, README_FILE_NAME);
      if (! readMe.exists()) {
        FileMaker readMeWriter = new FileMaker(readMe);
        readMeWriter.openForOutput();
        readMeWriter.writeLine (README_LINE_1);
        readMeWriter.writeLine (README_LINE_2);
        readMeWriter.writeLine (README_LINE_3);
        readMeWriter.close();
        Logger.getShared().recordEvent(LogEvent.NORMAL, 
            "README file created at " + readMe.toString(), false);
      }
    }
  }
  
  public void setNoteType(int noteType) {
    noteParms.setNoteType(noteType);
  }
  
  public void setNoteParms(NoteParms noteParms) {
    this.noteParms = noteParms;
  }
  
  /**
   Pass any metadata lines to the markdown parser as well. 
  
   @param metadataAsMarkdown True if metadata lines should appear as part
                             of output HTML, false otherwise. 
  */
  public void setMetadataAsMarkdown (boolean metadataAsMarkdown) {
    noteParms.setMetadataAsMarkdown(metadataAsMarkdown); 
  }
  
  public boolean treatMetadataAsMarkdown() {
    return noteParms.treatMetadataAsMarkdown();
  }
  
  /* =======================================================================
   * 
   * This section of the class contains input routines. 
   *
   * ======================================================================= */
  
  /**
   Load the notes from disk to memory. 
  
   @param model The model to contain the loaded notes. 
   @param loadUnTagged Should untagged notes be loaded? If not, they will
                       be suppressed. 
   @throws IOException If there's a problem reading the notes from disk. 
  */
  public void load (NoteCollectionModel model, boolean loadUnTagged) 
      throws IOException {
    
    notesLoaded = 0;
    String taggedMsg = "";
    if (! loadUnTagged) {
      taggedMsg = " Tagged";
    }
    this.model = model;
    openForInput();
    Note note = readNextNote();
    while (note != null) {
      if (note.hasTags() || loadUnTagged) {
        model.add(note);
        notesLoaded++;
      }
      note = readNextNote();
    }
    close();

    Logger.getShared().recordEvent(LogEvent.NORMAL, 
        String.valueOf(notesLoaded) + taggedMsg + " Notes loaded", false);
  }
  
  public int getNotesLoaded() {
    return notesLoaded;
  }
  
  /**
     Opens the reader for input.
    
     @param inDict A data dictionary to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (DataDictionary inDict)
      throws IOException {
    noteParms.newRecordDefinition(inDict);
    noteParms.buildRecordDefinition ();
    openForInputCommon();
  } 
      
  /**
     Opens the reader for input.
    
     @param inRecDef A record definition to use.
    
     @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (RecordDefinition inRecDef)
      throws IOException {
    noteParms.setRecDef(inRecDef);
    openForInputCommon();
  }
  
  public void openForInput () 
      throws IOException {
    
    // noteParms.buildRecordDefinition();
    openForInputCommon();
  }
  
  /**
   Get the NoteParms instance currently being used by this NoteIO instance. 
  
   @return The current NoteParms instance. 
  */
  public NoteParms getNoteParms() {
    return noteParms;
  }
  
  /**
   Build record definition. 
  */
  public void buildRecordDefinition() {
    noteParms.buildRecordDefinition();
  }
  
  /**
   Not yet used anywhere.
   */
  public NoteParms readNoteParms() {
    File noteParmsFile = new File (homeFolder, NoteParms.FILENAME);
    if (noteParmsFile.exists() && noteParmsFile.canRead()) {
      NoteParms noteParms = new NoteParms();
      return noteParms;
    } else {
      return null;
    }
  }
  
  private void openForInputCommon () 
      throws IOException {
    notesLoaded = 0;
    dirList = new ArrayList();
    dirEntries = new ArrayList<String>();
    if (homeFolder == null) {
      dirEntries.add(noteFileToRead.getName());
    } else {
      dirList.add (new DirToExplode (1, homeFolder.getAbsolutePath()));
    }
    dirNumber = -1;
    noteFileToRead = null;
    nextNote = null;
    entryNumber = 0;
    recordNumber = 0;
    nextNote();
  }
  
  /**
     Sets the file ID to be passed to the Logger.
    
     @param fileId Used to identify the source of the data being logged.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }
  
  /**
     Sets the maximum directory explosion depth.
    
     @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {
    this.maxDepth = maxDepth;
  }
  
  /**
     Returns the next input data record.
    
     @return Next data record.
    
     @throws IOException If reading from a source that might generate
                         these.
   */
  public DataRecord nextRecordIn ()
      throws IOException {
    return readNextNote();
  }
  
  /**
   Read the next note.
  
   @return Next note, or null if no more notes left. 
  
   @throws IOException 
  */
  public Note readNextNote() 
      throws IOException {
    
    Note noteToReturn = nextNote;
    nextNote();
    recordNumber++;
    return noteToReturn;
  }
  
  /**
     Returns the record number of the last record
     read or written.
    
     @return Number of last record read or written.
   */
  public int getRecordNumber () {
    return recordNumber;
  }
  
  /**
     Indicates whether there are more records to return.
    
     @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return (nextNote == null);
  }
  
  public void close() {
    noteFileToRead = null;
    nextNote = null;
  }
  
  private void nextNote() 
      throws IOException {
    nextNote = null;
    while (nextNote == null && dirNumber < dirList.size()) {
      nextDirEntry();
    }
  }
  
  private void nextDirEntry () 
      throws IOException {
    
    if (entryNumber >= 0 && entryNumber < dirEntries.size()) {
      String nextDirEntry = dirEntries.get (entryNumber);
      noteFileToRead = new File (currDirAsFile, nextDirEntry);
      if (noteFileToRead.isDirectory()) {
        if (nextDirEntry.equalsIgnoreCase("templates")
            || nextDirEntry.equalsIgnoreCase("publish")
            || nextDirEntry.equalsIgnoreCase("reports")
            || nextDirEntry.equalsIgnoreCase("files")
            || nextDirEntry.equalsIgnoreCase("images")
            || currDirDepth >= maxDepth) {
          // skip
        } else {
          DirToExplode newDirToExplode = new DirToExplode 
              (currDirDepth + 1, noteFileToRead.getAbsolutePath());
          dirList.add (newDirToExplode);
        }
      } 
      else
      if (isInterestedIn (noteFileToRead)) {
        nextNote = getNote(noteFileToRead, "");
        if (nextNote == null) {
        }
      } else {
        // No interest
      }
      entryNumber++;
    } else {
      dirNumber++;
      nextDirectory();
    }
  }
  
  /**
   Let's explode the next directory, if we have any more.
  */
  private void nextDirectory() 
      throws IOException {

    if (dirNumber >= 0 && dirNumber < dirList.size()) {
      DirToExplode currDir = dirList.get(dirNumber);
      currDirAsFile = new File (currDir.path);
      currDirDepth = currDir.depth;
      String[] dirEntry = currDirAsFile.list();
      if (dirEntry != null) {
        dirEntries = new ArrayList (Arrays.asList(dirEntry));
      }
      entryNumber = 0;
    }
  }
  
  /**
   Is this input module interested in processing the specified file?
  
   @param candidate The file being considered. 
  
   @return True if the input module thinks this file is worth processing,
           otherwise false. 
  */
  public static boolean isInterestedIn(File candidate) {
    File parent = candidate.getParentFile();
    String name = candidate.getName();
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
    if (candidate.getParent().toLowerCase().endsWith("templates")) {
      return false;
    }
    else
    if (candidate.getParent().toLowerCase().endsWith("reports")) {
      return false;
    }
    else
    if (candidate.getName().equalsIgnoreCase("New Event.txt")) {
      return false;
    }
    else
    if (candidate.getName().contains("conflicted copy")) {
      return false;
    }
    else
    if (candidate.getName().contains(PARMS_TITLE)) {
      return false;
    }
    else
    if (templateFilter.accept(parent, name)) {
      return false;
    }
    else
    if (candidate.getName().equalsIgnoreCase(README_FILE_NAME)) {
      return false;
    }
    else
    if (candidate.getName().endsWith (".txt")
        || candidate.getName().endsWith (".text")
        || candidate.getName().endsWith (".markdown")
        || candidate.getName().endsWith (".md")
        || candidate.getName().endsWith (".mdown")
        || candidate.getName().endsWith (".mkdown")
        || candidate.getName().endsWith (".mdtext")
        || candidate.getName().endsWith (".nnk")
        || candidate.getName().endsWith (".notenik")) {
      return true;
    } else {
      return false;
    } 
  }
  
  public Note getNote(String fileName) 
      throws IOException, FileNotFoundException {
    return getNote(getFile(fileName), "");
  }
  
  /**
   Read one note from disk and return it as a note object. 
  
   @param noteFile The file containing the note on disk. 
   @param syncPrefix An optional prefix that might be appended to the front
          of the note's title to form the file name. 
   @return A Note object. 
   @throws IOException
   @throws FileNotFoundException 
  */
  public Note getNote(File noteFile, String syncPrefix) 
      throws IOException, FileNotFoundException {
    
    Note note = null;
    
    if (FileUtils.isGoodInputFile(noteFile)) {
      FileName noteFileName = new FileName(noteFile);
      String fileNameIn = "";
      if (syncPrefix != null
          && syncPrefix.length() > 0
          && noteFileName.getBase().startsWith(syncPrefix)) {
        fileNameIn = noteFileName.getBase().substring(syncPrefix.length());
      } else {
        fileNameIn = noteFileName.getBase();
      }
      NoteBuilder builder = new NoteBuilder (noteParms);
      note = new Note(noteParms.getRecDef());
      note.setDiskLocation(noteFile);
      
      // Use the file name (minus the path and extension) as the default title
      note.setTitle(fileNameIn);
      
      // Set the last modified date
      Date lastModDate = new Date(noteFile.lastModified());
      note.setLastModDate(lastModDate);
      
      // Get ready to read the text file
      FileInputStream fileInputStream = new FileInputStream(noteFile);
      InputStreamReader inReader = new InputStreamReader (fileInputStream);
      inBuffered = new BufferedReader (inReader);
      
      this.builder = new NoteBuilder(noteParms);
      
      String line = inBuffered.readLine();
      
      // For each line in the file
      while (line != null) {
        NoteLine noteLine = new NoteLine
          (noteParms, builder, note, line);
        line = inBuffered.readLine();
      }
      inBuffered.close();
      if (! note.hasDateAdded()) {
        // Use the last modified date as a default value for the Date Added field
        note.setDateAdded(StringDate.YMDHMS_FORMAT.format(lastModDate));
      }
    }
    
    return note;
  }
  
  /**
   Read one note the passed line reader and return it as a note object. 
  
   @param lineReader A reader for a note.  
   @return A Note object. 
  */
  public Note getNote(TextLineReader lineReader) {
    
    Note note = null;
    NoteBuilder builder = new NoteBuilder (noteParms);
    note = new Note(noteParms.getRecDef());
    builder = new NoteBuilder(noteParms);
    lineReader.open();
    String line = lineReader.readLine();
    // For each line in the file
    while (line != null && lineReader.isOK() && (! lineReader.isAtEnd())) {
      NoteLine noteLine = new NoteLine(noteParms, builder, note, line);
      line = lineReader.readLine();
    }
    lineReader.close();
    return note;
  }
  
  public void  save (NoteCollectionModel noteList) 
      throws IOException {
    
    save (homeFolder, noteList, true);
    
  }
  
  /* =======================================================================
   * 
   * This section of the class contains output routines. 
   *
   * ======================================================================= */
 
  public void save (File folder, NoteCollectionModel noteList, boolean primaryLocation) 
      throws IOException {
    for (int i = 0; i < noteList.size(); i++) {
      Note nextNote = noteList.get(i);
      save (folder, nextNote, primaryLocation);
    }
  } // end method save
  
  public void save (Note note, boolean primaryLocation) 
      throws IOException {
    save (homeFolder, note, primaryLocation);
  }
 
  public void save (File folder, Note note, boolean primaryLocation) 
      throws IOException {
    File file = getFile(folder, note);
    save (note, file, primaryLocation);
  }
  
  public void save(Note note, File file, boolean primaryLocation) {
    openOutput (file);
    String oldDiskLocation = note.getDiskLocation();
    saveOneItem (note);
    if (primaryLocation) {
      note.setDiskLocation (file);
    }
    closeOutput();
  }
  
  /**
   Save the passed note to the passed text line writer. 
  
   @param note The note to be saved. 
   @param outWriter The writer to use for saving the note. 
  
   @return True if the save was successful. 
  */
  public boolean save (Note note, TextLineWriter outWriter) {
    boolean ok = true;
    writer = outWriter;
    ok = writer.openForOutput();
    if (ok) {
      ok = saveOneItem(note);
    }
    if (ok) {
      ok = writer.close();
    }
    return ok;
  }
  
  /**
   Save one note to a sync folder. 
  
   @param syncFolder The folder to which the note is to be saved. 
   @param syncPrefix The prefix to be appended to the front of the file name. 
   @param note       The note to be saved. 
  
   @throws IOException 
  */
  public void saveToSyncFolder (String syncFolder, String syncPrefix, Note note) 
      throws IOException {
    openOutput (getSyncFile(syncFolder, syncPrefix, note.getTitle()));
    saveOneItem (note);
    closeOutput();
  }
  
  /**
   Get a File object pointing to a file in the sync folder, given 
   the appropriate info about the sync preferences.
  
   @param syncFolderStr The path to the sync folder. 
   @param syncPrefix    The prefix appended to the title of each synced note. 
   @param title         The original title of the note. 
  
   @return A file object pointing to the specific sync file for this note. 
  */
  public File getSyncFile (String syncFolderStr, String syncPrefix, String title) {
    File syncFolder = new File(syncFolderStr);
    return new File(syncFolder, syncPrefix + title + noteParms.getPreferredFileExt());
  }
 
  /**
   Open the output writer.
 
   @param outFile The file to be opened.
 
  */
  private boolean openOutput (File outFile) {
    writer = new FileMaker(outFile);
    return writer.openForOutput();
  }
 
  private boolean saveOneItem (Note note) {
    
    NoteFieldSeqList fields = new NoteFieldSeqList();
    fields.addAll(note);
    boolean ok = true;
    for (int i = 0; i < fields.getNumberOfFields() && ok; i++) {
      DataField nextField = fields.getField(note, i);
      if (nextField != null
          && nextField.hasData()) {
        ok = writeFieldName (nextField.getProperName());
        if (ok && NoteParms.saveWithLineBreaks(nextField.getCommonName())) {
          ok = writeLine("");
          if (ok) {
            ok = writeLine(" ");
          }
        }
        if (ok) {
          ok = writeFieldValue (nextField.getData());
        }
        if (ok) {
          ok = writeLine("");
        }
      }
    }
    return ok;
  } // end of method saveOneItem
 
  private boolean writeFieldName (String fieldName) {
    boolean ok = true;
    ok = write(fieldName);
    if (ok) {
      ok = write(": ");
    }
    for (int i = fieldName.length(); i < 6 && ok; i++) {
      ok = write (" ");
    }
    return ok;
  }
 
  private boolean writeFieldValue (String fieldValue) {
    return writeLine (fieldValue);
  }
 
  private boolean writeLine (String s) {
    boolean ok = true;
    ok = writer.write (s);
    if (ok) {
      ok = writer.newLine();
    }
    return ok;
  }
 
  private boolean write (String s) {
    return writer.write (s);
  }
 
  /**
   Close the output writer.
 
   @return True if close worked ok.
  */
  public boolean closeOutput() {
    return writer.close();
  }
  
  /* =======================================================================
   * 
   * This section of the class contains other file-related routines. 
   *
   * ======================================================================= */
  
  /**
   Does the given note exist on disk?
  
   @param note The note to be evaluated. 
  
   @return True if a disk file is found, false otherwise. 
  */
  public boolean exists (Note note) {
    return getFile(homeFolder, note).exists();
  }

   /**
   Does the given Note already exist on disk?
 
   @param folder    The folder in which the item is to be stored.
   @param Note      The Note to be stored.
 
   @return True if a disk file with the same path already exists,
           false if not.
   */
  public boolean exists (File folder, Note note) {
    return getFile(folder, note).exists();
  }
 
  /**
   Does the given Note already exist on disk?
 
   @param folder    The folder in which the item is to be stored.
   @param localPath The local path (folder plus file name) for the
                    Note to be stored.
 
   @return True if a disk file with the same path already exists,
           false if not.
   */
  public boolean exists (File folder, String localPath) {
    return getFile(folder, localPath).exists();
  }
  
  /**
   Does this note already exist on disk?
  
   @param localPath The path that would be used for the note. 
  
   @return True if a note already exists on disk  at this location. 
  */
  public boolean exists (String localPath) {
    return getFile(homeFolder, localPath).exists();
  }
  
  public boolean delete (Note note) {
    return getFile(homeFolder, note).delete();
  }
 
  /**
   Delete the passed note from disk.
 
   @param folder    The folder in which the note is to be stored.
   @param Note      The Note to be stored.
 
   @return True if the file was deleted successfully,
           false if not.
   */
  public boolean delete (File folder, Note Note) {
    return getFile(folder, Note).delete();
  }
 
  /**
   Delete the passed note from disk.
 
   @param folder    The folder in which the item is to be stored.
   @param localPath The local path (folder plus file name) for the
                    Note to be stored.
 
   @return True if the file was deleted successfully,
           false if not.
   */
  public boolean delete (File folder, String localPath) {
    return getFile(folder, localPath).delete();
  }
  
  public File getFile (Note note) {
    return getFile(homeFolder, note);
  }
  
  public File getFile(String localPath) {

    return getFile (homeFolder, localPath);

  }
 
  /**
   Return a standard File object representing the note's stored location on disk.
 
   @param folder  The folder in which the item is to be stored.
   @param Note    The Note to be stored.
 
   @return The File pointing to the intended disk location for the given note.
   */
  public File getFile (File folder, Note note) {
    return new File (folder, note.getFileName() + "." + getFileExt(note));
  }
 
  /**
   Return a standard File object representing the item's stored location on disk.
 
   @param folder    The folder in which the item is to be stored.
   @param localPath The local path (folder plus file name) for the
                    Note to be stored.
 
   @return The File pointing to the intended disk location for the given item.
   */
  public File getFile (File folder, String localPath) {
    StringBuilder completePath = new StringBuilder();
    try {
      completePath = new StringBuilder (folder.getCanonicalPath());
    } catch (Exception e) {
      completePath = new StringBuilder (folder.getAbsolutePath());
    }
    completePath.append('/');
    completePath.append(localPath);
    completePath.append('.');
    completePath.append(noteParms.getPreferredFileExt());
    return new File (completePath.toString());
  }
  
  /**
   Return the file extension to be used to store this particular note. 
  
    @param note The note to be stored. 
    @return The file extension to be used. 
  */
  public String getFileExt(Note note) {
    String ext = note.getDiskFileExt();
    if (ext == null || ext.length() == 0) {
      ext = noteParms.getPreferredFileExt();
    }
    if (ext == null || ext.length() == 0) {
      ext = NoteParms.DEFAULT_FILE_EXT;
    }
    return ext;
  }
  
  /**
   Return the number of fields defined in the record definition. 
  
   @return The number of fields/columns in the record definition. 
  */
  public int getNumberOfFields() {
    return noteParms.getRecDef().getNumberOfFields();
  }
  
  /**
     Returns the record definition for the file.
    
     @return Record definition for this tab-delimited file.
   */
  public RecordDefinition getRecDef () {
    return noteParms.getRecDef();
  }
  
  /**
     Retrieves the path to the parent folder of the notes (if any).
    
     @return Path to the parent folder of the notes (if any).
   */
  public String getDataParent () {
    if (homeFolder == null) {
      return null;
    } else {
      return homeFolder.getAbsolutePath();
    }
  }
  
 /**
     Sets the Logger object to be used for logging. 
    
     @param log The Logger object being used for logging significant events.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  /**
     Gets the Logger object to be used for logging. 
    
     @return The Logger object being used for logging significant events.
   */
  public Logger getLog () {
    return log;
  } 
  
  public void logNormal (String msg) {
    Logger.getShared().recordEvent (LogEvent.NORMAL, msg, false);
  }
  
  /**
     Sets the option to log all data off or on. 
    
     @param dataLogging True to send all data read or written to the
                        log file.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
  /**
     Gets the option to log all data. 
    
     @return True to send all data read or written to the
             log file.
   */
  public boolean getDataLogging () {
    return dataLogging;
  }
  
  /**
     Inner class to define a directory to be processed.
   */
  class DirToExplode {
    int 		depth = 0;
    String	path  = "";
    
    DirToExplode (int depth, String path) {
      this.depth = depth;
      this.path = path;
    } // DirToExplode constructor
  } // end DirToExplode inner class

}
