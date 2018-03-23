package com.powersurgepub.psutils2.records;

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.textio.*;

  import java.io.*;
  import java.util.*;
  import java.text.*;

public class VCardReader
    extends     File
    implements  DataSource {

  public static final String BEGIN  = "BEGIN";
  public static final String END    = "END";
  public static final String VCARD  = "VCARD";
  public static final String NAME   = "N";
  public static final String EMAIL  = "EMAIL";
  public static final String EMAIL_FIELD_NAME = "Email";
  public static final String FULL_NAME = "FN";
  public static final String FULL_NAME_FIELD_NAME = "Full Name";

  /** Data to be sent to the log. */
  private    LogData          logData;

  /** An event to be sent to the log. */
  private    LogEvent         logEvent;

  /** The identifier for this reader. */
  private    String           fileId;

  private String path = "";

  private     String                fileEncoding = "UTF-8";
  private     InputStreamReader     inReader;
  private     BufferedReader        inBuffered;
  private     boolean               openForInput = false;
  private     boolean               atEnd = false;
  private     String                line = "";
  private     int                   i = 0;
  private     char                  c = ' ';
  private     StringBuilder         chunk;
  private     String                propertyName = "";
  private     String                objectName = "";

  /** The data dictionary to be used by this record. */
  private    DataDictionary   dict = new DataDictionary();

  /** The record definition to be used by this record. */
  private    RecordDefinition recDef = new RecordDefinition(dict);

  private     DataRecord            dataRec = new DataRecord();

  /** Pointer to a particular record within the array. */
  private    int              recordNumber;

  /**
   Constructs a directory reader given a path defining the directory
   to be read.

   @param  inPath File path to be read.
   */
  public VCardReader (String inPath) {
    super (inPath);
    path = inPath;
    initialize();
  }

  /**
   Constructs a directory reader given a file object
   defining the directory to be read.

   @param  inPathFile Directory path to be read.
   */
  public VCardReader (File inPathFile) {
    super (inPathFile.getAbsolutePath());
    path = this.getAbsolutePath();
    initialize();
  }

  /**
   Performs standard initialization for all the constructors.
   By default, fileId is set to "directory".
   */
  private void initialize () {
    FileName dirName = new FileName (path, FileName.GUESS_TYPE);
    fileId = "vcard";
    logData = new LogData ("", fileId, 0);
    logEvent = new LogEvent (0, "");
  }

  /**
   Opens the reader for input.

   @param inDict A data dictionary to use.

   @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (DataDictionary inDict)
      throws IOException {
    dict = inDict;
    recDef = new RecordDefinition(dict);
    openForInput();

  }

  /**
   Opens the reader for input.

   @param inRecDef A record definition to use.

   @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput (RecordDefinition inRecDef)
      throws IOException {
    dict = inRecDef.getDict();
    recDef = inRecDef;
    openForInput();
  }

  /**
   Opens the reader for input.

   @throws IOException If there is trouble opening a disk file.
   */
  public void openForInput ()
      throws IOException {

    FileInputStream fileInputStream = new FileInputStream(this);
    inReader = new InputStreamReader (fileInputStream, fileEncoding);
    inBuffered = new BufferedReader (inReader);
    openForInput = true;
    atEnd = false;
    recordNumber = 0;
    readLine();
  }

  /**
   Returns the next input data record.

   @return Next data record.

   @throws IOException If reading from a source that might generate
   these.
   */
  public DataRecord nextRecordIn ()
      throws IOException {

    while ((! atEnd)
        && (! (propertyName.equalsIgnoreCase(BEGIN)
            && objectName.equalsIgnoreCase(VCARD)))) {
      readLine();
    }

    if (atEnd) {
      return null;
    } else {
      dataRec = new DataRecord();
      do {
        processLine();
      } while ((! atEnd)
          && (! (propertyName.equalsIgnoreCase(END)
            && objectName.equalsIgnoreCase(VCARD))));
      recordNumber++;
      return dataRec;
    }
  }

  private void processLine()
      throws IOException {

    if (propertyName.equalsIgnoreCase(NAME)) {

    }
    else if (propertyName.equalsIgnoreCase(FULL_NAME)) {
      getNextLineChunk();
      addField(FULL_NAME_FIELD_NAME, chunk.toString());
    } else if (propertyName.equalsIgnoreCase(EMAIL) || propertyName.endsWith(EMAIL)) {
      while (c != ':' && i < line.length()) {
        getNextLineChunk();
        if (c == ':') {
          addField(EMAIL_FIELD_NAME, getNextLineChunk());
        }
      }
    }
    readLine();
  }

  /**
   Add another field to the record

   @param fieldName The name of the field.
   @param fieldValue The data value for the field.
   */
  private void addField(String fieldName, String fieldValue) {
    if (fieldName.length() > 0 && fieldValue.length() > 0) {
      if (! recDef.contains(fieldName)) {
        recDef.addColumn(fieldName);
      }
      dataRec.storeField(recDef, fieldName, fieldValue);
    }
  }

  /**
   Obtains the next line from the text file. <p>

   If the text file has not yet been opened, then the first execution of this
   method will automatically attempt to open the file. When the end of the file
   is encountered, an empty String will be returned as the next line, the
   atEnd variable will be turned on, and the file will be closed.

   @return    The next line in the file (or an empty string at end of file).
   */
  public String readLine ()
      throws IOException {
    line = "";
    if ((! openForInput) && (! atEnd)) {
      openForInput();
    }
    if (openForInput) {
      line = inBuffered.readLine();
      if (line == null) {
        line = "";
        atEnd = true;
      }
      if (atEnd && openForInput) {
        close();
      }
    }
    i = 0;
    propertyName = getNextLineChunk();
    if (propertyName.equalsIgnoreCase(BEGIN)
        || propertyName.equalsIgnoreCase(END)) {
      objectName = getNextLineChunk();
    }
    System.out.println("Return line = " + line);
    return line;
  }


  /**
   Extract the  next set of characters up to the next punctuation symbol.

   @return The next set of characters.
   */
  private String getNextLineChunk() {
    chunk = new StringBuilder();
    boolean punctuation = false;
    c = ' ';
    while (i < line.length() && (! punctuation)) {
      c = line.charAt(i);
      if (c == ':' || c == ';' || c == '=') {
        punctuation = true;
      } else {
        chunk.append(c);
      }
      i++;
    }
    System.out.println("Returning chunk = " + chunk.toString());
    return chunk.toString();
  }

  /**
   Returns the record definition for the reader.

   @return Record definition.
   */
  public RecordDefinition getRecDef () {
    return recDef;
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
   Returns the reader as some kind of string.

   @return String identification of the reader.
   */
  public String toString () {
    return "VCardReader reading " + getPath();
  }

  /**
   Indicates whether there are more records to return.

   @return True if no more records to return.
   */
  public boolean isAtEnd() {
    return atEnd;
  }


  /**
   Closes the reader.

   @throws IOException If there is trouble closing the file.
   */
  public void close ()
      throws IOException  {

    if (openForInput) {
      inBuffered.close ();
      openForInput = false;
      atEnd = true;
    }
  }

  /**
   Sets a log to be used by the reader to record events.

   @param  log A logger object to use.
   */
  public void setLog (Logger log) {
  }

  /**
   Indicates whether all data records are to be logged.

   @param  dataLogging True if all data records are to be logged.
   */
  public void setDataLogging (boolean dataLogging) {
  }

  /**
   Sets a file ID to be used to identify this reader in the log.

   @param  fileId An identifier for this reader.
   */
  public void setFileId (String fileId) {
  }

  /**
   Sets the maximum directory explosion depth.

   @param maxDepth Desired directory/sub-directory explosion depth.
   */
  public void setMaxDepth (int maxDepth) {
  }

  /**
   Retrieves the path to the parent folder of the original source file (if any).

   @return Path to the parent folder of the original source file (if any).
   */
  public String getDataParent () {
    return getParent();
  }

}
