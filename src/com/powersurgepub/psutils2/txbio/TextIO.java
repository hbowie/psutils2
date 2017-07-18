/*
 * Copyright 1999 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.txbio;

  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.txbmodel.*;

  import java.net.*;
  import java.util.*;

/**
 Loads/Stores a TextTree in any of a variety of different formats.
 <p>
 Upon initial construction, available types are registered from
 known i/o modules. Each i/o module declares the types that it supports. Later
 requests to load or store data of a certain type then makes use of the
 available modules.
 */
public class TextIO {

  public static final char INPUT  = 'i';
  public static final char OUTPUT = 'o';
  
  private TextIOTabs      tabs = new TextIOTabs();
  private TextIOxml       xml  = new TextIOxml();
  private TextIOhtml      html = new TextIOhtml();
  private MarkupWriter    markupWriter = new MarkupWriter();
  private TextLineWriter  lineWriter;
  private TextWriter      writer;
  
  private ArrayList types = new ArrayList();
  
  public TextIO () {
    
    html.registerTypes (types);
    tabs.registerTypes (types);
    xml.registerTypes (types);
    markupWriter.registerTypes (types);

  }
  
  public void logTypes () {
    for (int i = 0; i < types.size(); i++) {
      TextIOType iotype = (TextIOType)types.get(i);
      Logger.getShared().recordEvent(
          LogEvent.NORMAL,
          "I/O Type --  " + iotype.toLongerString(),
          false);
    } 
  }
  
  public ArrayList getTypes () {
    return types;
  }

  /**
   Returns the IO type for the given parameters.

   @param ext   A string containing the file extension, without any leading
                period.

   @param ioStr A String beginning with "I" (upper or lower case) to indicate
                that an input file is to be processed. Anything else indicates
                output.

   @param preferFragments True if HTML fragments are preferred for output.

   @return The IO Type for the given parameters, or null if none could be found.
   */
  public TextIOType getType
      (String ext, String ioStr, boolean Fragments) {

    // See whether we want input or output
    char io = 'i';
    if (ioStr.length() > 0) {
      io = Character.toLowerCase (ioStr.charAt (0));
    }
    if (io != INPUT && io != OUTPUT) {
      io = OUTPUT;
    }

    // Look for a matching type
    TextIOType type = null;
    boolean found = false;
    if (ext.length() > 0) {
      int i = 0;
      while (i < types.size() && (! found)) {
        type = (TextIOType)types.get (i);
        int j = 0;
        while (j < type.getExtensionsSize() && (! found)) {
          found = (ext.equalsIgnoreCase (type.getExtension (j)));
          if (io == INPUT && (! type.isGoodForInput())) {
            found = false;
          }
          if (io == OUTPUT && (! type.isGoodForOutput())) {
            found = false;
          }
          if (Fragments
              && type.getLabel().toLowerCase().indexOf ("fragment") < 0) {
            found = false;
          }
          if (! found) {
            j++;
          } // end if file extensions still not found
        } // end while more extensions for this type
        if (! found) {
          i++;
        } // end if file extension not found for this type
      } // end while more types to consider
    } // end if we have a file extension
    if (found) {
      return type;
    } else {
      return null;
    }
  }

  /**
   Returns the IO type for the given parameters.

   @param label A string containing the identifying label for the type.

   @param ioStr A String beginning with "I" (upper or lower case) to indicate
                that an input file is to be processed. Anything else indicates
                output.

   @return The IO Type for the given parameters, or null if none could be found.
   */
  public TextIOType getType (String label, String ioStr) {

    // See whether we want input or output
    char io = 'i';
    if (ioStr.length() > 0) {
      io = Character.toLowerCase (ioStr.charAt (0));
    }
    if (io != INPUT && io != OUTPUT) {
      io = OUTPUT;
    }

    // Look for a matching type
    TextIOType type = null;
    boolean found = false;
    if (label.length() > 0) {
      int i = 0;
      while (i < types.size() && (! found)) {
        type = (TextIOType)types.get (i);
        found = (label.equalsIgnoreCase (type.getLabel()));
        if (io == INPUT && (! type.isGoodForInput())) {
          found = false;
        }
        if (io == OUTPUT && (! type.isGoodForOutput())) {
          found = false;
        }
        if (! found) {
          i++;
        } // end if file label not found for this type
      } // end while more types to consider
    } // end if we have a file label
    if (found) {
      return type;
    } else {
      return null;
    }
  }
  
  public boolean load (TextTree tree, URL url, TextIOType type, String parm) {
    boolean ok = true;
    TextIOModule iomod = type.getModule();
    ok = iomod.load (tree, url, type, parm);
    return ok;
  } // end load method
  
  public boolean store (TextTree tree, TextLineWriter lineWriter, TextIOType type,
      boolean epub, String epubSite) {
    writer = new TextWriter (lineWriter);
    boolean ok = writer.openForOutput ();
    TextIOModule iomod = type.getModule();
    if (ok) {
      ok = iomod.store (tree, writer, type, epub, epubSite);
    }
    if (ok) {
      writer.close();
    }
    return ok;
  } // end load method

}
