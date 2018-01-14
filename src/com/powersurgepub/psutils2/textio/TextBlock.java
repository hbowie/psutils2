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

package com.powersurgepub.psutils2.textio;

  import com.powersurgepub.psutils2.basic.*;

  import java.util.*;

/**
   A block of text containing all significant non-null fields for an object,
   that can be easily read by a person, as well as by software. <p>
 
   Following is sample code for creating a TextBlock object. <p>
 
   <pre><code>
      TextBlock block1 = new TextBlock(); 
      block1.startBlockOut ("tester"); 
      block1.addField ("Name", "Herb Bowie", ""); 
      block1.addField ("Address", "10557 E. Mercer Lane\nScottsdale, AZ 85259", ""); 
      block1.addField ("Phone", "480-451-9732", ""); 
      block1.endBlockOut(); 
      ArrayList labelList = new ArrayList (block1.getLabels()); <p> 
  </code></pre>
 
    Following is sample code for extracting the data from a TextBlock object. <p>
 
    <pre><code>
      TextBlock block2 = new TextBlock (block1.toString()); 
      block2.startBlockIn ("tester", labelList); 
      boolean anotherField = false; 
      do { 
        anotherField = block2.findNextField(); 
        if (anotherField) { 
          System.out.println ("Label = " + block2.getNextLabel()); 
          System.out.println ("Field = " + block2.getNextField()); 
        } 
      } while (! block2.endOfBlock()); <p>
    </code></pre>
  
   @author Herb Bowie of PowerSurge Publishing
  
 */
public class TextBlock {
  
  private final static  String BLOCK_START_PREFIX = "*** Start Text Block for ";
  private final static  String BLOCK_START_SUFFIX = " Object ***";
  private final static  String BLOCK_END_PREFIX   = "*** End Text Block for ";
  private final static  String BLOCK_END_SUFFIX   = " Object ***";
  private final static  String LABEL_SUFFIX       = ": ";
  private               TextLineWriter writer;
  // private               StringBuilder  blockOut;
  private               String        blockIn;
  private               int           lineEnd       = 0;
  private               int           lineStart     = 0;
  private               String        objectName;
  private               boolean       lineHasLabel = false;
  private               String        label         = "";
  private               String        nextLabel     = "";
  private               StringBuilder  nextField     = new StringBuilder();
  private               List          labels;
  private               int           labelStart    = -1;
  private               int           labelEnd      = -1;
  private               int           fieldStart    = -1;
  private               int           fieldEnd      = -1;
  
  /** Pointer to the current position within the block. */
  private               int           index         = 0;
  
  private               boolean       blockStarted  = false;
  private               boolean       blockEnded    = false;
  private               boolean       textEnded     = false;
  private               boolean       fieldFound    = false;
  
  /**
    Test the TextBlock class.
   */
  public static void test () {
    
    System.out.println ("Testing TextBlock");
    TextBlock block1 = new TextBlock();
    block1.startBlockOut ("tester");
    block1.addField ("Name", "Herb Bowie");
    block1.addField ("Address", "10557 E. Mercer Lane\nScottsdale, AZ 85259", "");
    block1.addField ("Phone", "480-451-9732", "");
    block1.endBlockOut();
    ArrayList labelList = new ArrayList (block1.getLabels());
    System.out.println (block1.toString());
    TextBlock block2 = new TextBlock (block1.toString());
    block2.startBlockIn ("tester", labelList);
    boolean anotherField = false;
    do {
      anotherField = block2.findNextField();
      if (anotherField) {
        System.out.println ("Label = " + block2.getNextLabel());
        System.out.println ("Field = " + block2.getNextField());
      }
    } while (! block2.endOfBlock());
  }
  
  /** 
    Creates a new instance of TextBlock with no data.
   */
  public TextBlock() {
    writer = new StringMaker();
    writer.openForOutput();
    // blockOut = new StringBuilder();
    labels = new ArrayList();
    // writer.close();
  }
  
  /** 
    Creates a new instance of TextBlock with a passed line writer.
   */
  public TextBlock(TextLineWriter writer) {
    this.writer = writer;
    
    // blockOut = new StringBuilder();
    labels = new ArrayList();
  }
  
  /** 
    Creates a new instance of TextBlock with existing data.
   
    @param blockIn A string of data formatted as a TextBlock, or containing
                   a TextBlock string.
   */
  public TextBlock(String blockIn) {
    this.blockIn = blockIn;
  }
  
  /**
   Check to see if a chunk of text is the format expected by these routines. 
   */
  public static boolean isTextBlockFormat 
      (String blockIn, String objectName) {
    int index = 0;
    boolean blockEnded = false;
    boolean blockStarted = false;
    int lineEnd = 0;
    int lineStart = 0;
    String line;
    // Check each line of text in input block
    while ((! blockStarted) && (index < blockIn.length())) {
      int lineEndLength = 1;
      char c = ' ';
      char d = ' ';
      char x = ' ';
      lineEnd = index;
      boolean endOfLine = false;
      while (! endOfLine) {
        if (lineEnd >= blockIn.length()) {
          endOfLine = true;
        } else {
          c = blockIn.charAt (lineEnd);
          if (c == GlobalConstants.CARRIAGE_RETURN) {
            x = GlobalConstants.LINE_FEED;
            endOfLine = true;
          }
          else
          if (c == GlobalConstants.LINE_FEED) {
            x = GlobalConstants.CARRIAGE_RETURN;
            endOfLine = true;
          }
          if (endOfLine) {
            if ((lineEnd + 1) < blockIn.length()) {
              d = blockIn.charAt (lineEnd + 1);
              if (d == x) {
                lineEndLength = 2;
              }
            }
          } else { 
            // not end of line
            lineEnd++;
          }
        } // end if another char to look at
      } // end while not end of line
      lineStart = index;
      if (lineEnd < lineStart) {
        lineEnd = blockIn.length();
      }
      if (lineEnd >= lineStart) {
        index = lineEnd + lineEndLength;
        line = blockIn.substring (lineStart, lineEnd);
        if (line.equals 
              (BLOCK_START_PREFIX + objectName + BLOCK_START_SUFFIX)) {
          blockStarted = true;
        } // end if line contains block start string
      } // end if we found another line
    } // end while looking for start of block
    return blockStarted;
  } // end of isTextBlockFormat method
  
  /*
    -----------------------------------------
    The following methods create a TextBlock.
    -----------------------------------------
   */
  
  /**
    Begin the creation of a new TextBlock.
   
    @param objectName A name used to identify the object, and to help
                      mark the beginning and the end of the TextBlock.
   */
  public void startBlockOut (String objectName) {
        
    this.objectName = objectName;
    // writer.open();
    addLine (getBlockStart());
  }
  
  /**
    Add another field to a TextBlock being built, assuming that an
    empty string does not need to be stored in the text block.
    
    @param label A string to be used as a label for the field.
    @param field The field itself, formatted as a String. The field will not
                 be written to the text block if it is empty.
   */
  public void addField (String label, String field) {
    addField (label, field, "");
  }
  
  /**
    Add another field to a TextBlock being built.
    
    @param label A string to be used as a label for the field.
    @param field The field itself, formatted as a String.
    @param nada  A string representation of this field with no significant
                 data in it (an empty string, for example).
   */
  public void addField (String label, String field, String nada) {
    labels.add (label);
    if (! field.equals (nada)) {
      addLine (label + LABEL_SUFFIX + field);
      // System.out.println ("TextBlock.addField field follows:");
      // StringUtils.println (System.out, field);
    }
  }
  
  /**
    Indicate that there are no more fields to be added to the TextBlock.
   */
  public void endBlockOut () {
    addLine (getBlockEnd());
    // writer.close();
  }
  
  /**
    Add another line to the TextBlock, delimited by a line feed character.
   
    @param line A string of text to be added to the TextBlock being built.
   */
  private void addLine (String line) {
    writer.write (line);
    writer.newLine();
    // blockOut.append (line);
    // blockOut.append (GlobalConstants.LINE_FEED);
  }
  
  /**
    Returns the current list of labels being used as a List.
   
    @return A List of labels for all fields identified so far.
   */
  public List getLabels () {
    return labels;
  }
  
  /*
    ----------------------------------------------
    The following methods deconstruct a TextBlock.
    ----------------------------------------------
   */
  
  /**
    Start decoding of an existing TextBlock.
   
    @param objectName The name of the object to be decoded.
    @param labelList  A list containing all the value labels that might be found
                      in this TextBlock.
   */
  public void startBlockIn (String objectName, ArrayList labelList) {
    this.objectName = objectName;
    labels = labelList;
    findStartOfBlock();
  }
  
  /**
    Start decoding of an existing TextBlock, passing an array 
    of labels rather than a List.
   
    @param objectName  The name of the object to be decoded.
    @param labelArray  An array containing all the value labels that might be found
                       in this TextBlock.
   */
  public void startBlockIn (String objectName, String[] labelArray) {
    this.objectName = objectName;
    labels = new ArrayList();
    for (int i = 0; i < labelArray.length; i++) {
      labels.add (labelArray [i]);
    }
    textEnded = false;
    findStartOfBlock();
  }
  
  /**
    Finds the beginning of the text block.
   */
  private void findStartOfBlock () {
    int index = 0;
    blockEnded = false;
    blockStarted = false;
    while ((! blockStarted) && (! endOfText())) {
      String line = getNextLine();
      if (line.equals (getBlockStart())) {
        blockStarted = true;
      } // end if line contains block start string
    } // end while looking for start of block
    lineHasLabel = false;
    while ((! lineHasLabel) && (! endOfText())) {
      getNextLine();
    }
  } // end method to find start of block

  /**
    Find the next field in the input text block.
   
    @return True if field found.
   */
	public boolean findNextField () {
    if (lineHasLabel) {
      nextLabel = label;
      fieldStart = labelEnd + LABEL_SUFFIX.length();
      nextField = new StringBuilder();
      do {
        if (nextField.length() > 0) {
          nextField.append (GlobalConstants.LINE_FEED);
        }
        if (lineEnd >= fieldStart) {
          fieldEnd = lineEnd;
          nextField.append (blockIn.substring (fieldStart, fieldEnd));
        }
        getNextLine();
        fieldStart = lineStart;
      } while ((! lineHasLabel) && (! endOfBlock()));
      return true;
    } // end if we have a label for the field
    return false;
  } // end method to get next field
  
  /**
    If findNextField returned true, then a call to this method
    will return the index pointing to the field label found.
   
    @return An index pointing to the field label found.
   */
  public int getNextFieldIndex () {
    return labelStart;
  }
  
  /**
    If findNextField returned true, then a call to this method
    will return the label found.
   
    @return The field label found.
   */
  public String getNextLabel () {
    return nextLabel;
  }
  
  /**
    If findNextField returned true, then a call to this method
    will return the field found.
   
    @return The field data found.
   */
  public String getNextField () {
    return nextField.toString();
  }
  
  /**
    Returns the next line in the text block, ended by a line feed,
    with or without an optional carriage return before or after, and
    advances the index past the LF and optional CR, to the end of the block
    or the beginning of the following line.
    
    @return Next line in text block. Also sets line field to this value.
   */
  private String getNextLine () {
    String line;
    if (endOfText()) {
      line = "";
    } else {
      int lineEndLength = 1;
      char c = ' ';
      char d = ' ';
      char x = ' ';
      lineEnd = index;
      boolean endOfLine = false;
      while (! endOfLine) {
        if (lineEnd >= blockIn.length()) {
          endOfLine = true;
        } else {
          c = blockIn.charAt (lineEnd);
          if (c == GlobalConstants.CARRIAGE_RETURN) {
            x = GlobalConstants.LINE_FEED;
            endOfLine = true;
          }
          else
          if (c == GlobalConstants.LINE_FEED) {
            x = GlobalConstants.CARRIAGE_RETURN;
            endOfLine = true;
          }
          if (endOfLine) {
            if ((lineEnd + 1) < blockIn.length()) {
              d = blockIn.charAt (lineEnd + 1);
              if (d == x) {
                lineEndLength = 2;
              }
            }
          } else { 
            // not end of line
            lineEnd++;
          }
        } // end if another char to look at
      } // end while not end of line
      lineStart = index;
      if (lineEnd < lineStart) {
        lineEnd = blockIn.length();
      }
      if (lineEnd >= lineStart) {
        index = lineEnd + lineEndLength;
        line = blockIn.substring (lineStart, lineEnd);
        if (line.equals (getBlockEnd())) {
          blockEnded = true;
        }
      } else {
        line = "";
      } // end if no line to return
    } // end if more of block left
    checkNextField();
    return line;
  } // end getNextLine method
  
  /**
    Check current line to see if it contains a field label.
   
    @return True if the line just found begins with a valid field label.
   */
  private boolean checkNextField () {
    lineHasLabel = false;
    if (! endOfText()) {
      labelEnd = blockIn.indexOf (LABEL_SUFFIX, lineStart);
      if (labelEnd >= 0) {
        label = blockIn.substring (lineStart, labelEnd).trim();
        labelStart = labels.indexOf (label);
        if (labelStart >= 0) {
          lineHasLabel = true;
        }
      }
    }
    return lineHasLabel;
  }
  
  /**
    Returns true if the end of the block has been encountered.
   
    @return True if end of block has been found.
   */
  public boolean endOfBlock () {
    if (index >= blockIn.length()) {
      blockEnded = true;
    }
    return blockEnded;
  }
  
  /**
    Returns true if the end of all text has been encountered.
   
    @return True if end of text has been found.
   */
  public boolean endOfText () {
    if (index >= blockIn.length()) {
      blockEnded = true;
      textEnded = true;
    }
    return textEnded;
  }
  
  /**
    Returns the string used to mark the start of the block.
   
    @return block start prefix + object name + block start sufffix.
   */
  public String getBlockStart () {
    return BLOCK_START_PREFIX + objectName + BLOCK_START_SUFFIX;
  }
  
  /**
    Returns the string used to mark the end of the text block.
   
    @return block end prefix + object name + block end suffix.
   */
  public String getBlockEnd () {
    return BLOCK_END_PREFIX + objectName + BLOCK_END_SUFFIX;
  }
  
  /**
    Returns the entire block (input or output) as a string.
   
    @return Entire text block as a string.
   */
  public String toString() {
    if (writer != null) {
      writer.close();
      return writer.toString();
    }
    else
    if (blockIn != null) {
      return blockIn;
    } else {
      return "";
    }
  }
} // end class TextBlock
