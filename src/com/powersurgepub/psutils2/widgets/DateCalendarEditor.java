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
      
  import java.text.*;
  import java.util.*;

  import javafx.beans.value.*;
 	import javafx.event.*;
 	import javafx.geometry.*;
 	import javafx.scene.*;
 	import javafx.scene.control.*;
 	import javafx.scene.control.Alert.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;

public class DateCalendarEditor 
    extends Stage {
  
  /** Dates with a year at or after this one are considered null. */
  public final static int               NULL_YEAR = 2050;
  
  /** Null Year Display Value. */
  public final static String            NULL_DISPLAY_DATE = "N/A";
  
  /** Prior Default date for a new to do item. */
  public final static GregorianCalendar OLD_DEFAULT_DATE 
      = new GregorianCalendar (2050, 11, 31);
  
  /** Default date for a new to do item. */
  public final static GregorianCalendar DEFAULT_DATE 
      = new GregorianCalendar (2050, 11, 1);
  
  private DateWidgetOwner               dateWidgetOwner;
  
  private Scene                         scene;
  
  // The editor consists of three grid panes
  
  private GridPane                      masterGrid = new GridPane();
  
  // Grid 1 contains controls to adjust the month, year and day of the month. 
  
  private GridPane                      grid1 = new GridPane();
  
  private Button                        monthDecrementButton;
  private Button                        monthIncrementButton;
  private TextField                     monthTextField;
  
  private Button                        yearDecrementButton;
  private Button                        yearIncrementButton;
  private TextField                     yearTextField;
  
  private Button                        dayDecrementButton;
  private Button                        dayIncrementButton;
  private TextField                     dayTextField;
  
  private GridPane                      grid2 = new GridPane();
  
  private GridPane                      grid3 = new GridPane();
  
  private Button                        noDateButton;
  private Button                        todayButton;
  private Button                        recurButton;
  private Tooltip                       recurBlankTip = new Tooltip("");
  private Button                        okButton;
  
  private GregorianCalendar             date = new GregorianCalendar();
  
  private boolean                       modified = false;
  
  private SimpleDateFormat              monthFormatter
      = new SimpleDateFormat ("MMMM");
  
  private SimpleDateFormat              actionFormatter
      = new SimpleDateFormat ("yyyy-MM-dd");
  
  public static final SimpleDateFormat  longDateFormatter
      = new SimpleDateFormat ("EEEE  MMMM d, yyyy");
  
  private ArrayList                     dayButton = new ArrayList ();
  
  /** Creates new form DateEditor */
  public DateCalendarEditor(Stage primaryStage, DateWidgetOwner dateWidgetOwner) {
    
    super(StageStyle.UTILITY);
    this.setTitle("Date Editor");
    this.dateWidgetOwner = dateWidgetOwner;
    
    yearDecrementButton = new Button();
    yearTextField = new TextField();
    yearIncrementButton = new Button();
    
    monthDecrementButton = new Button();
    monthTextField = new TextField();
    monthIncrementButton = new Button();
    
    dayDecrementButton = new Button();
    dayTextField = new TextField();
    dayIncrementButton = new Button();
    
    
    // setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    
    // gb.setAllInsets (2);
    
    // grid1.startLayout (grid1, 3, 3);
    // grid1.setAllInsets (4);
    // Set spacing between components
    grid1.setPadding(new Insets(4));
    grid1.setHgap(4);
    grid1.setVgap(4);
    
    yearDecrementButton.setText("<");
    // yearDecrementButton.setFocusable(false);
    yearDecrementButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        decrementYear();
      }
    });
        
    grid1.add(yearDecrementButton, 0, 0, 1, 1);

    yearTextField.setAlignment(Pos.CENTER);
    yearTextField.setText("2005");
    yearTextField.focusedProperty().addListener (
        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
          yearTextFieldFocusChange(newValue);
    });

    grid1.add(yearTextField, 1, 0, 1, 1);

    yearIncrementButton.setText(">");
    yearIncrementButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        incrementYear();
      }
    });
    grid1.add(yearIncrementButton, 2, 0, 1, 1);

    monthDecrementButton.setText("<");
    monthDecrementButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        decrementMonth();
      }
    });
    grid1.add(monthDecrementButton, 0, 1, 1, 1);

    monthTextField.setAlignment(Pos.CENTER);
    monthTextField.setText("WWWWWWWWWWW");
    monthTextField.focusedProperty().addListener (
        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
          monthTextFieldFocusChanged(newValue);
    });
    grid1.add(monthTextField, 1, 1, 1, 1);

    monthIncrementButton.setText(">");
    monthIncrementButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        incrementMonth();
      }
    });
    grid1.add(monthIncrementButton, 2, 1, 1, 1);
    
    dayDecrementButton.setText("<");
    dayDecrementButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        decrementDay();
      }
    });
    grid1.add(dayDecrementButton, 0, 2, 1, 1);

    dayTextField.setAlignment(Pos.CENTER);
    dayTextField.setText("  ");
    dayTextField.focusedProperty().addListener (
        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
          dayTextFieldFocusChanged(newValue);
    });
    grid1.add(dayTextField, 1, 2, 1, 1);

    dayIncrementButton.setText(">");
    dayIncrementButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        incrementDay();
      }
    });
    grid1.add(dayIncrementButton, 2, 2, 1, 1);
    
    masterGrid.add (grid1, 0, 0, 1, 1);

    grid2.setPadding(new Insets(4));
    grid1.setHgap(1);
    grid1.setVgap(1);

    // Lay out a 7 x 7 grid to show the days in the month
    
    Label sunday = new Label ("Su");
    sunday.setAlignment(Pos.CENTER);
    grid2.add (sunday, 0, 0, 1, 1);
    
    Label monday = new Label ("Mo");
    monday.setAlignment(Pos.CENTER);
    grid2.add (monday, 1, 0, 1, 1);
    
    Label tuesday = new Label ("Tu");
    tuesday.setAlignment(Pos.CENTER);
    grid2.add (tuesday, 2, 0, 1, 1);
    
    Label wednesday = new Label ("We");
    wednesday.setAlignment(Pos.CENTER);
    grid2.add (wednesday, 3, 0, 1, 1);
    
    Label thursday = new Label ("Th");
    thursday.setAlignment(Pos.CENTER);
    grid2.add (thursday, 4, 0, 1, 1);
    
    Label friday = new Label ("Fr");
    friday.setAlignment(Pos.CENTER);
    grid2.add (friday, 5, 0, 1, 1);
    
    Label saturday = new Label ("Sa");
    saturday.setAlignment(Pos.CENTER);
    grid2.add (saturday, 6, 0, 1, 1);
    
    int col = 0;
    int row = 1;
    for (int i = 0; i < 42; i++) {
      DayOfMonthButton db = new DayOfMonthButton("00");
      db.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          DayOfMonthButton db = (DayOfMonthButton)evt.getTarget();
          chooseDate(db.getDate());
        }
      });
      grid2.add (db, col, row, 1, 1);
      dayButton.add (db);
      if (col < 6) {
        col++;
      } else {
        col = 0;
        row++;
      }
    }
    
    masterGrid.add (grid2, 0, 1, 1, 1);
    
    // Build the third grid:
    // Four buttons arranged in a 2 x 2 grid
    
    noDateButton  = new Button ("N/A");
    noDateButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        setDateToNull();
      }
    });
    grid3.add(noDateButton, 0, 0, 1, 1);
    
    todayButton   = new Button ("Today");
    todayButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        setDateToToday();
      }
    });
    grid3.add(todayButton, 1, 1, 1, 1);
    
    recurButton   = new Button ("Recur");
    recurButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        recurDate();
      }
    });
    grid3.add(recurButton, 0, 1, 1, 1);
    
    okButton      = new Button ("OK"); 
    okButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        ok();
      }
    });
    grid3.add(okButton, 1, 1, 1, 1);
    
    masterGrid.add (grid3, 0, 2, 1, 1);
    
    yearTextField.requestFocus();
    
    scene = new Scene(masterGrid);
    this.setScene(scene);
    
  }
  
  public void ok() {
    dateWidgetOwner.dateModified(StringDate.COMMON_FORMAT.format(date.getTime()));
    this.hide();
  }
  
  public static String formatLong (Date date) {

    if (isNullDate(date)) {
      return NULL_DISPLAY_DATE;
    } else {
      return longDateFormatter.format (date);
    }
  }
  
  public boolean isNullDate() {
    return isNullDate (date.getTime());
  }
  
  public static boolean isNullDate (Date date) {
    GregorianCalendar cal = new GregorianCalendar ();
    cal.setTime (date);
    return (cal.get (Calendar.YEAR) >= NULL_YEAR);
  }
  
  public void setDate (Date date) {

    this.date.setTime (date);
    displayDate();
    modified = false;

  }
  
  private void modifyYear() {
    String yearStr = yearTextField.getText();
    try {
      int year = Integer.parseInt (yearStr);
      if (year < 100) {
        year = year + 2000;
      }
      GregorianCalendar modDate 
          = new GregorianCalendar (year, date.get (date.MONTH), date.get (date.DATE));
      date = modDate;
      dateModified();
    } catch (NumberFormatException e) {
      // do nothing
    }
    displayDate();
  }
  
  private void decrementYear() {
    if (isNullDate()) {
      // do nothing
    } else {
      date.add (Calendar.YEAR, -1);
      dateModified();
      displayDate();
    }
  }
  
  private void incrementYear() {
    if (isNullDate()) {
      // do nothing
    } else {
      date.add (Calendar.YEAR, 1);
      dateModified();
      displayDate();
    }
  }
  
  public void yearTextFieldFocusChange(boolean gainedFocus) {
    if (gainedFocus) {
      yearTextField.selectAll();
    } else {
      modifyYear();
    }
  }
  
  private void modifyMonth() {
    String monthStr = monthTextField.getText().toLowerCase();
    int month = 0;
    if (isNullDate()) {
      month = 0;
    } else {
      month = date.get (Calendar.MONTH);
    }
    if (monthStr.startsWith ("ja")) {
      month = 1;
    }
    else
    if (monthStr.startsWith ("f")) {
      month = 2;
    }
    else
    if (monthStr.startsWith ("mar")) {
      month = 3;
    }
    else
    if (monthStr.startsWith ("ap")) {
      month = 4;
    }
    else
    if (monthStr.startsWith ("may")) {
      month = 5;
    }
    else
    if (monthStr.startsWith ("jun")) {
      month = 6;
    }
    else
    if (monthStr.startsWith ("jul")) {
      month = 7;
    }
    else
    if (monthStr.startsWith ("au")) {
      month = 8;
    }
    else
    if (monthStr.startsWith ("s")) {
      month = 9;
    }
    else
    if (monthStr.startsWith ("o")) {
      month = 10;
    }
    else
    if (monthStr.startsWith ("n")) {
      month = 11;
    }
    else
    if (monthStr.startsWith ("d")) {
      month = 12;
    }
    else {
      try {
        month = Integer.parseInt (monthStr);
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
    if (month >= 1 && month <= 12) {
      GregorianCalendar modDate 
          = new GregorianCalendar (date.get (date.YEAR), month - 1, date.get (date.DATE));
      date = modDate;
      dateModified();
    }
    displayDate();
  }
  
  private void decrementMonth() {
    if (isNullDate()) {
      // do nothing
    } else {
      date.add (Calendar.MONTH, -1);
      dateModified();
      displayDate();
    }
  }
  
  private void incrementMonth() {
    if (isNullDate()) {
      // do nothing
    } else {
      date.add (Calendar.MONTH, 1);
      dateModified();
      displayDate();
    }
  }
  
  private void monthTextFieldFocusChanged(boolean gainedFocus) {
    if (gainedFocus) {
      monthTextField.selectAll();
    } else {
      modifyMonth();
    }
  }
  
  private void modifyDay() {
    String dayStr = dayTextField.getText().toLowerCase();
    int day = 0;
    if (isNullDate()) {
      day = 0;
    } else {
      day = date.get (Calendar.DATE);
    }

    try {
      day = Integer.parseInt (dayStr);
    } catch (NumberFormatException e) {
      // do nothing
    }

    if (day >= 1 && day <= 31) {
      GregorianCalendar modDate 
          = new GregorianCalendar (date.get (date.YEAR), date.get (date.MONTH), day);
      date = modDate;
      dateModified();
    }
    displayDate();
  }
  
  private void decrementDay() {
    if (isNullDate()) {
      // do nothing
    } else {
      date.add (Calendar.DATE, -1);
      dateModified();
      displayDate();
    }
  }
  
  private void incrementDay() {
    if (isNullDate()) {
      // do nothing
    } else {
      date.add (Calendar.DATE, 1);
      dateModified();
      displayDate();
    }
  }
  
  private void dayTextFieldFocusChanged(boolean gainedFocus) {
    if (gainedFocus) {
      dayTextField.selectAll();
    } else {
      modifyDay();
    }
  }
  
  private void chooseDate (String dateString) {
    ParsePosition pos = new ParsePosition (0);
    try {
      date.setTime (actionFormatter.parse (dateString, pos));
      dateModified();
      displayDate();
    } catch (IllegalArgumentException e) {
      System.out.println (dateString + " cannot be formatted as a date");
    }
  }
  
  public void setDateToToday() {
    GregorianCalendar today = new GregorianCalendar();
    date.set (Calendar.YEAR, today.get (Calendar.YEAR));
    date.set (Calendar.MONTH, today.get (Calendar.MONTH));
    date.set (Calendar.DATE, today.get (Calendar.DATE));
    dateModified();
    displayDate();
  }
  
  private void setDateToNull() {
    date.setTime (DEFAULT_DATE.getTime());
    dateModified();
    displayDate();
  }
  
  private void recurDate() {
    if (dateWidgetOwner.canRecur()) {
      StringDate str = new StringDate();
      str.set(date.getTime());
      String inc = dateWidgetOwner.recur(str);
      dateModified();
      displayDate();
    }
  }
  
  private void displayDate () {
    
    int year = date.get (Calendar.YEAR);
    int month = 0;
    int day = 0;
    
    if (year >= NULL_YEAR) {
      yearTextField.setText (NULL_DISPLAY_DATE);
      monthTextField.setText ("         ");
      dayTextField.setText ("  ");
      for (int i = 0; i < 42; i++) {
        DayOfMonthButton db = (DayOfMonthButton)dayButton.get (i);
        db.setText ("  ");
        db.setDate ("NULL");
      }
      recurButton.setDisable (true);
      recurButton.setTooltip(recurBlankTip);
    } else {
      yearTextField.setText (String.valueOf (year));
      month = date.get (Calendar.MONTH);
      monthTextField.setText (monthFormatter.format (date.getTime()));
      day = date.get (Calendar.DATE);
      dayTextField.setText (String.valueOf (day));
      int daysInMonth = date.getActualMaximum (GregorianCalendar.DAY_OF_MONTH);
      GregorianCalendar 
        firstDayOfMonth 
          = new GregorianCalendar (
              date.get (Calendar.YEAR),
              date.get (Calendar.MONTH), 
              1);
      int firstDayOfMonthDayOfWeek 
          = firstDayOfMonth.get (GregorianCalendar.DAY_OF_WEEK);
      GregorianCalendar firstDayToDisplay
          = new GregorianCalendar (
              firstDayOfMonth.get (Calendar.YEAR),
              firstDayOfMonth.get (Calendar.MONTH),
              firstDayOfMonth.get (Calendar.DATE));
      firstDayToDisplay.add (Calendar.DATE, ((firstDayOfMonthDayOfWeek * -1) + 1));
      GregorianCalendar calendarDay
          = new GregorianCalendar (
              firstDayToDisplay.get (Calendar.YEAR),
              firstDayToDisplay.get (Calendar.MONTH),
              firstDayToDisplay.get (Calendar.DATE));

      for (int i = 0; i < 42; i++) {
        DayOfMonthButton db = (DayOfMonthButton)dayButton.get (i);
        String dayText = String.valueOf (calendarDay.get (Calendar.DATE));
        String startItalics = "";
        String endItalics = "";
        String startBold = "";
        String endBold = "";
        if (month == calendarDay.get (Calendar.MONTH)) {
          if (day == calendarDay.get (Calendar.DATE)) {
            startBold = "<b><font color=red>";
            endBold = "</font></b>";
            db.setText ("<html>" + startItalics + startBold + dayText 
            + endBold + endItalics + "</html>");
          } else {
            db.setText (dayText);
          }
        } else {
          startItalics = "<i>";
          endItalics = "</i>";
          db.setText ("<html>" + startItalics + startBold + dayText 
            + endBold + endItalics + "</html>");
        }
        db.setDate (actionFormatter.format(calendarDay.getTime()));
        calendarDay.add (Calendar.DATE, 1);
      } // end for each Calendar Day displayed
      if (dateWidgetOwner.canRecur()) {
        recurButton.setDisable (false);
        Tooltip recursTip = new Tooltip(dateWidgetOwner.getRecurrenceRule());
        recurButton.setTooltip(recursTip);
      } else {
        recurButton.setDisable (true);
        recurButton.setTooltip(recurBlankTip);
      }
    } // end if year not null
  } // end method displayDate
  
  private void dateModified() {
    modified = true;
    // dateWidgetOwner.dateModified (date.getTime());
  }
  
  public boolean isModified () {
    return modified;
  }
  
  public Date getDate () {
    return date.getTime();
  }
  
}

