/*
 * Copyright 2017 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.ui;

  import java.util.*;

  import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
  import javafx.stage.*;

  import javax.swing.*;

/**
 A JavaFX replacement for JOptionPane.

 @author Herb Bowie
 */
public class PSOptionPane {
  
  public static Optional<ButtonType> showMessageDialog(
      Window parent, String message, String title, int messageType) {
    Alert alert;
    
    switch (messageType) {
      case JOptionPane.ERROR_MESSAGE:
        alert = new Alert(AlertType.ERROR);
        break;
      case JOptionPane.WARNING_MESSAGE:
        alert = new Alert(AlertType.WARNING);
        break;
      case JOptionPane.QUESTION_MESSAGE:
        alert = new Alert(AlertType.CONFIRMATION);
        break;
      case JOptionPane.INFORMATION_MESSAGE:
        alert = new Alert(AlertType.INFORMATION);
        break;
      default:
        alert = new Alert(AlertType.INFORMATION);
    }
    
    alert.setTitle(title);
    alert.setContentText(message);
    return alert.showAndWait();
  }
  
}
