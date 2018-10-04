package com.powersurgepub.psutils2.notenik;

import com.powersurgepub.psutils2.strings.StringUtils;

public class NoteAttachment {

  private Note parentNote = null;
  private String fileNameSuffix = "";
  private String fileNameExtension = "";

  /**
   * Constructor with no values.
   */
  public NoteAttachment() {

  }

  public String toString() {
    return getFileName();
  }

  public String getFileName() {
    return parentNote.getFileName() + getSuffixAndExtension();
  }

  /**
   * Set the parent note to which this file is attached.
   *
   * @param parentNote The Note to which this file is attached.
   */
  public void setParentNote(Note parentNote) {
    this.parentNote = parentNote;
  }

  /**
   * Return the parent note to which this file is attached.
   *
   * @return The parent note to which this file is attached.
   */
  public Note getParentNote() {
    return parentNote;
  }

  /**
   * get the Suffix and the Extension, with a period between them.
   *
   * @return the suffix and the extension, with a period between them.
   */
  public String getSuffixAndExtension() {
    return fileNameSuffix + "." + fileNameExtension;
  }

  /**
   * Set the suffix for this file name.
   *
   * @param suffix The suffix for this file name.
   */
  public void setFileNameSuffix(String suffix) {
    StringBuilder work = new StringBuilder(suffix);
    while (work.length() > 0 && work.charAt(0) == '-') {
      work.deleteCharAt(0);
    }
    int l = work.length() - 1;
    while (work.length() > 0 && work.charAt(l) == '.') {
      work.deleteCharAt(l);
      l--;
    }
    this.fileNameSuffix = work.toString();
  }

  /**
   * Get the file name's suffix.
   *
   * @return The file name suffix.
   */
  public String getFileNameSuffix() {
    return fileNameSuffix;
  }

  /**
   * Set the file name extension.
   *
   * @param fileNameExtension The file extension following the period in the file name.
   */
  public void setFileNameExtension(String fileNameExtension) {
    StringBuilder work = new StringBuilder(fileNameExtension);
    while (work.length() > 0 && work.charAt(0) == '.') {
      work.deleteCharAt(0);
    }
    this.fileNameExtension = work.toString();
  }

  /**
   * Get the file name extension.
   *
   * @return The file extension follosing the period in the file name.
   */
  public String getFileNameExtension() {
    return fileNameExtension;
  }
}
