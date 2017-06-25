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

package com.powersurgepub.psutils2.widgets;

  import com.powersurgepub.psutils2.values.*;

  import java.io.*;
  import java.net.*;
  import java.text.*;
  import java.util.*;

  import javafx.event.*;
  import javafx.geometry.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
  UI Component that allows the user to enter a date. 

  @author  Herb Bowie. 
 */
public class DateWidget 
    extends GridPane 
    implements
        DataWidget,
        DateWidgetOwner {
  
  private Stage                   stage = null;
  private DateWidgetOwner         dateWidgetOwner = null;
  
  private boolean                 modified = false;
  private TextField               dateField;
  private Button                  calendarButton;
  private Button                  recurButton;
  private Button                  todayButton;
  
  /** 
    Creates new pane DateWidget 
   */
  public DateWidget() {
    
    // Set spacing between components
    this.setPadding(new Insets(10));
    this.setHgap(10);
    this.setVgap(10);
    
    dateField = new TextField();
    this.add(dateField, 0, 0, 1, 1);
    
    calendarButton = new Button();
    calendarButton.setText("Calendar");
    calendarButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        dateButtonActionPerformed(evt);
      }
    });
    this.add(calendarButton, 1, 0, 1, 1);
    
    recurButton = new Button();
    recurButton.setText("Recur");
    recurButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        recurButtonActionPerformed(evt);
      }
    });
    this.add(recurButton, 2, 0, 1, 1);
    
    todayButton = new Button();
    todayButton.setText("Today");
    todayButton.setDisable(false);
    todayButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        todayButtonActionPerformed(evt);
      }
    });
    this.add(todayButton, 3, 0, 1, 1);
    
  }
  
  public void setOwner(DateWidgetOwner dateWidgetOwner) {
    this.dateWidgetOwner = dateWidgetOwner;
    recurButton.setDisable(! dateWidgetOwner.canRecur());
  }
  
  public void setFrame(Stage stage) {
    this.stage = stage;
  }
  
    /**
   To be called whenever the date is modified by DateWidget.
   */
  public void dateModified (String date) {
    dateField.setText(date);
    if (dateWidgetOwner != null) {
      dateWidgetOwner.dateModified(date);
    }
  }
  
  /**
   Does this date have an associated rule for recurrence?
   */
  public boolean canRecur() {
    if (dateWidgetOwner != null) {
      return dateWidgetOwner.canRecur();
    } else {
      return false;
    }
  }
  
  /**
   Provide a text string describing the recurrence rule, that can
   be used as a tool tip.
   */
  public String getRecurrenceRule() {
    if (dateWidgetOwner != null) {
      return dateWidgetOwner.getRecurrenceRule();
    } else {
      return "";
    }
  }
  
  /**
   Apply the recurrence rule to the date.
  
   @param date Starting date.
  
   @return New date. 
   */
  public String recur (StringDate date) {
    if (dateWidgetOwner != null) {
      return dateWidgetOwner.recur(date);
    } else {
      return date.toString();
    }
  }
  
  /**
   Apply the recurrence rule to the date.
  
   @param date Starting date.
  
   @return New date. 
   */
  public String recur (String date) {
    if (dateWidgetOwner != null) {
      return dateWidgetOwner.recur(date);
    } else {
      return date;
    }
  }
  
  public void setText (String date) {
    dateField.setText(date);
  }
  
  public void setDate (Date date) {
    setText(StringDate.COMMON_FORMAT.format(date));
    modified = false;
    if (dateWidgetOwner != null) {
      recurButton.setDisable(! dateWidgetOwner.canRecur());
    } else {
      recurButton.setDisable(true);
    }
  }
  
  public boolean isModified() {
    return modified;
  }
  
  public Date getDate() {
    StringDate str = new StringDate();
    str.set(dateField.getText());
    return str.getDate();
  }
  
  public String getText() {
    return dateField.getText();
  }
  
  /**
   Edit the date using the Calendar editor. 
  */
  public void editDate() {
    if (stage != null && dateWidgetOwner != null) {
      DateCalendarEditor editor = new DateCalendarEditor(stage, this);
      StringDate str = new StringDate();
      str.set(dateField.getText());
      Date date = str.getDate();
      if (date == null) {
        editor.setDateToToday();
      } else {
        editor.setDate (date);
      }
      if (editor.isNullDate()) {
        editor.setDateToToday();
      }
      // editor.setLocationRelativeTo (stage);
      editor.showAndWait();
      // if (editor.isModified()) {
      //   modified = true;
      //   date.setTime(editor.getDate().getTime());
      //   displayDate();
      //   dateWidgetOwner.dateModified(StringDate.COMMON_FORMAT.format(date));
      // }  // End if we have a valid date
    } // end if we have a date owner and stage
  } // end editDate method
  
  private void recurButtonActionPerformed (ActionEvent evt) {
    if (dateWidgetOwner != null && dateWidgetOwner.canRecur()) {
      String oldDate = dateField.getText();
      String newDate = dateWidgetOwner.recur (oldDate);
      if (newDate != null && newDate.length() > 0) {
        modified = true;
        setText(newDate);
        dateWidgetOwner.dateModified(newDate);
      }
    }  
  }
  
  private void todayButtonActionPerformed (ActionEvent evt) {
    setText(StringDate.getTodayCommon());
  }

  private void dateButtonActionPerformed(ActionEvent evt) {
    editDate();
  }
  
}
