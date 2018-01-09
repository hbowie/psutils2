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

package com.powersurgepub.psutils2.elements;

/**
 An action status data element.
 
 @author Herb Bowie.
 */
public class ActionStatus 
    implements DataElement {

  public final static String  SIMPLE_NAME   = "status";
  public final static String  DISPLAY_NAME  = "Status";
  public final static String  BRIEF_NAME    = "Status";
  public final static int     COLUMN_WIDTH  = 80;

  /** Status of item. */
	private    	int          		status = 0;
  public  final static int      OPEN            = 0;
  public  final static int      IN_WORK         = 1;
  public  final static int      PENDING         = 2;
  public  final static int      CANCELED        = 5;
  public  final static int      PENDING_RECURS  = 8;
  public  final static int      CLOSED          = 9;
  public  final static int      STATUS_LOW      = 0;
  public  final static int      STATUS_HIGH     = 9;

  public  final static String[] STATUS_LABEL = {
    "Open",
    "In Work",
    "Pending",
    "3-Unknown",
    "4-Unknown",
    "Canceled",
    "6-Unknown",
    "7-Unknown",
    "Closed (R)",
    "Closed"
  };

  public ActionStatus() {
    
  }

  public ActionStatus(int status) {
    setValue(status);
  }

  public void setValue(int value) {
    setStatus(value);
  }

  public void setValue(String value) {
    setStatus(value);
  }

  public void setValue(Object obj) {
    setValue(obj.toString());
  }

  public void setValue(boolean closed) {
    setStatus(closed);
  }

  public void setStatus(String label) {
    String stat = label.toLowerCase().trim();
    if (stat.length() > 0
        && Character.isDigit(stat.charAt(0))) {
      try {
        int digit = Integer.parseInt(stat.substring(0, 1));
        this.setValue(digit);
      } catch (NumberFormatException e) {
        // ??!
      }
    }
    else
    if (stat.startsWith ("o")) {
      setStatus (OPEN);
    }
    else
    if (stat.startsWith ("i")) {
      setStatus (IN_WORK);
    }
    else
    if (stat.startsWith ("p")) {
      setStatus (PENDING);
    }
    else
    if (stat.startsWith ("ca")) {
      setStatus (CANCELED);
    }
    else
    if (stat.startsWith ("c")) {
      setStatus (CLOSED);
    }
  }

  public void setStatus(boolean closed) {
    if (closed) {
      setStatus(CLOSED);
    } else {
      setStatus(OPEN);
    }
  }

  public void setStatus(int status) {
    if (status >= STATUS_LOW && status <= STATUS_HIGH) {
      this.status = status;
    }
  }

  public int getValueAsInt() {
    return status;
  }

  public int getStatus() {
    return status;
  }

  public String getStatusLabel() {
    return getLabel();
  }

  public String toString() {
    return getLabel();
  }

  public String getLabel() {
    return STATUS_LABEL[status];
  }

  public Object getValue() {
    return getLabel();
  }

  /**
   Get the class definition for the preferred form of the element.

   @return The class definition for the preferred form of the element.
   */
  public Class getElementClass() {
    return String.class;
  }

  public static String getLabel(int status) {
    if (status >= STATUS_LOW && status <= STATUS_HIGH) {
      return STATUS_LABEL[status];
    } else {
      return String.valueOf (status) + "-Out of Range";
    }
  }

  /**
     Indicates whether item is still pending.

     @return True if item is canceled or closed.
   */
  public boolean isDone () {
    return (status >= CANCELED);
  }

  /**
     Indicates whether item is still pending.

     @return True if item is open or in-work.
   */
  public boolean isNotDone () {
    return (status < CANCELED);
  }

  public String getSimpleName() {
    return SIMPLE_NAME;
  }

  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  public String getBriefName() {
    return BRIEF_NAME;
  }

  public int getColumnWidth() {
    return COLUMN_WIDTH;
  }

}
