package com.powersurgepub.psutils2.notenik;

  import com.powersurgepub.psutils2.records.*;

  import java.io.*;

/**
 * Information about a particular collection.
 */
public class CollectionInfo {

  public static final String COLLECTION_INFO_FILE_NAME = "- INFO.nnk";

  public static final String SORT_PARM_FIELD_NAME = "Sort Parm";
  public static final String LAST_KEY_FIELD_NAME  = "Last Key";

  private             File              folder = null;

  private             String            title = "";
  private             String            tags = "";
  private             String            seq = "";
  private             String            lastKey = "";

  private             int               noteSortParm = 0;
  public static final int                 SORT_BY_TITLE         = 0;
  public static final int                 SORT_BY_SEQ_AND_TITLE = 1;
  public static final int                 SORT_TASKS_BY_DATE    = 2;
  public static final int                 SORT_TASKS_BY_SEQ     = 3;
  public static final int                 SORT_BY_AUTHOR        = 4;

  private             String            body = "";

  private RecordDefinition  recDef;

  public CollectionInfo() {
    recDef = new RecordDefinition();
    recDef.addColumn(NoteParms.TITLE_FIELD_NAME);
    recDef.addColumn(NoteParms.TAGS_FIELD_NAME);
    recDef.addColumn(NoteParms.LINK_FIELD_NAME);
    recDef.addColumn(NoteParms.SEQ_FIELD_NAME);
    recDef.addColumn(SORT_PARM_FIELD_NAME);
    recDef.addColumn(LAST_KEY_FIELD_NAME);
    recDef.addColumn(NoteParms.BODY_FIELD_NAME);
  }


  public void setFolder(File folder) {
    this.folder = folder;
  }

  public File getFolder() {
    return folder;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public String getTags() {
    return tags;
  }

  public void setSeq(String seq) {
    this.seq = seq;
  }

  public String getSeq() {
    return seq;
  }

  public void setNoteSortParm(int noteSortParm) {
    this.noteSortParm = noteSortParm;
  }

  public int getNoteSortParm() {
    return noteSortParm;
  }

  public void setLastKey(String lastKey) {
    this.lastKey = lastKey;
  }

  public String getLastKey() {
    return lastKey;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getBody() {
    return body;
  }

    /**
     * Read Collection Info from a disk file.
     *
     * @return True if successfully read.
     */
  public boolean readFromDisk() {

    boolean ok = (folder != null
        && folder.exists()
        && folder.canRead()
        && folder.isDirectory());

    File collectionInfoFile = null;
    if (ok) {
      collectionInfoFile = new File(folder, COLLECTION_INFO_FILE_NAME);
      ok = (collectionInfoFile != null
          && collectionInfoFile.exists()
          && collectionInfoFile.isFile()
          && collectionInfoFile.canRead());
    }

    Note collectionInfoNote = null;
    if (ok) {
      NoteIO noteIO = new NoteIO(folder, NoteParms.DEFINED_TYPE, recDef);
      try {
        collectionInfoNote = noteIO.getNote(collectionInfoFile, "");
        if (collectionInfoNote.hasTitle()) {
          title = collectionInfoNote.getTitle();
          tags  = collectionInfoNote.getTagsAsString();
          seq   = collectionInfoNote.getSeq();
          String noteSortParmStr = collectionInfoNote.getFieldData(SORT_PARM_FIELD_NAME);
          try {
            noteSortParm = Integer.parseInt(noteSortParmStr);
          } catch (NumberFormatException e) {
            // Bum parm
          }
          lastKey = collectionInfoNote.getFieldData(LAST_KEY_FIELD_NAME);
          body  = collectionInfoNote.getBody();
        } else {
          ok = false;
        }
      } catch (FileNotFoundException e) {
        ok = false;
      } catch (IOException e) {
        ok = false;
      }
    }
    return ok;
  }

  /**
   * Write Collection Info to a disk file.
   *
   * @return True if successfully written.
   */
  public boolean writeToDisk() {

    boolean ok = (folder != null
        && folder.exists()
        && folder.canWrite()
        && folder.isDirectory()
        && title.length() > 0);

    File collectionInfoFile = null;
    if (ok) {
      collectionInfoFile = new File(folder, COLLECTION_INFO_FILE_NAME);
      ok = (collectionInfoFile != null);
    }

    Note collectionInfoNote = new Note(recDef, title);
    collectionInfoNote.setTags(tags);
    collectionInfoNote.setLink(folder);
    collectionInfoNote.setSeq(seq);
    collectionInfoNote.setField(SORT_PARM_FIELD_NAME, String.valueOf(noteSortParm));
    collectionInfoNote.setField(LAST_KEY_FIELD_NAME, lastKey);
    collectionInfoNote.setBody(body);
    if (ok) {
      NoteIO noteIO = new NoteIO(folder, NoteParms.DEFINED_TYPE, recDef);
      ok = noteIO.save(collectionInfoNote, collectionInfoFile, false);
    }
    return ok;

  }

  public void display() {
    System.out.println("CollectionInfo");
    System.out.println("- Folder = " + folder.toString());
    System.out.println("- Title  = " + title);
    System.out.println("- Tags   = " + tags);
    System.out.println("- Seq    = " + seq);
    System.out.println("- Sort Parm = " + String.valueOf(noteSortParm));
    System.out.println("- Last Key  = " + lastKey);
    System.out.println("- Body      = " + body);
  }
}
