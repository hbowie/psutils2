/*
 * Copyright 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.markup.*;

  import java.text.*;

  import javafx.event.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.scene.web.*;

/**
 A class that positions a WebView on a layout pane, along with some controls.

 @author Herb Bowie
 */
public class WebPane {
  
  private StringBuilder page;

  private AttachmentHandler attachmentHandler = null;
  private WebLauncher   launcher = null;
  
  private MdToHTML      mdToHTML;
  
  private DateFormat    dateFormat = new SimpleDateFormat ("EEEE MMMM d, yyyy");
  
  private FXUtils       fxUtils;
  private GridPane      webPane;
  private WebView       webView;
  private WebEngine     webEngine;
  private Button        reloadButton;
  private Button        launchButton = null;
  private ComboBox      attachmentComboBox = null;
  
  public WebPane() {

    initPage();
    
    fxUtils = FXUtils.getShared();
    mdToHTML = MdToHTML.getShared();
    
    webView = new WebView();
    webEngine = webView.getEngine();
    webPane = new GridPane();
    fxUtils.applyStyle(webPane);
    webPane.add(webView, 0, 0, 3, 1);
    GridPane.setVgrow(webView, Priority.ALWAYS);
    GridPane.setHgrow(webView, Priority.ALWAYS);

    reloadButton = new Button("Reload");
    reloadButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        loadPage();
      }
    });
    webPane.add(reloadButton, 0, 1, 1, 1);

    launchButton = new Button("Launch Link");
    launchButton.setDisable(true);
    webPane.add(launchButton, 1, 1, 1, 1);

    attachmentComboBox = new ComboBox();
    attachmentComboBox.setOnAction(e -> attachmentSelected());
    webPane.add(attachmentComboBox, 2, 1, 1, 1);
    attachmentComboBox.setDisable(true);
  }
  
  public void setLaunchLink(String link) {
    launchButton.setText("Launch Link");
    if (link != null && link.length() > 0) {
      launchButton.setDisable(false);
      launchButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          Home.getShared().openURL(link);
        }
      });
    } else {
      launchButton.setDisable(true);
      launchButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          // No action
        }
      });
    }
  }

  public void noLink() {
    if (launchButton != null) {
      launchButton.setDisable(true);
      launchButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          // No action
        }
      });
    }
  }
  
  public void setLauncher(
      String link, 
      String launchButtonTitle, 
      WebLauncher launcher) {

    this.launcher = launcher;
    launchButton.setText(launchButtonTitle);
    if (link != null && link.length() > 0) {
      launchButton.setDisable(false);
      launchButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          launcher.launchButtonPressed(link);
        }
      });
    } else {
      launchButton.setDisable(true);
      launchButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          // No action
        }
      });
    }
  }

  /**
   * Provide a handler for optional attachments.
   *
   * @param attachmentHandler An object capabable of handling attachments.
   */
  public void setAttachmentHandler(AttachmentHandler attachmentHandler) {
    this.attachmentHandler = attachmentHandler;
    attachmentComboBox.getItems().remove(0, attachmentComboBox.getItems().size());
    attachmentHandler.populateAttachmentComboBox(attachmentComboBox);
    attachmentComboBox.setPromptText("Select an attachment to be opened");
    attachmentComboBox.setDisable(false);

  }

  /**
   * Indicate that we have no attachments for this item.
   */
  public void noAttachments() {
    this.attachmentHandler = null;
    attachmentComboBox.getItems().remove(0, attachmentComboBox.getItems().size());
    attachmentComboBox.setPromptText("No attachments");
    attachmentComboBox.setDisable(true);
  }

  private void attachmentSelected() {

      Object attachment = attachmentComboBox.getSelectionModel().getSelectedItem();
      if (attachment != null) {
        attachmentHandler.attachmentSelected(attachment);
        // attachmentComboBox.getSelectionModel().clearSelection();
      }

  }
  
  public void initPage() {
    page = new StringBuilder();
  }
  
  /**
   Convert Markdown text to HTML and add it to the page we are building. 
  
   @param md Text formatted following Markdown conventions. 
  */
  public void appendMarkdown(String md) {
    String bodyHTML = mdToHTML.markdownToHtml(md);
    appendLine(bodyHTML);
  }
  
  public void appendLine(String additionalText) {
    append(additionalText);
    endLine();
  }
  
  public void append(String additionalText) {
    page.append(additionalText);
  }
  
  public void endLine() {
    page.append(GlobalConstants.LINE_FEED);
  }
  
  public void loadPage() {
    webEngine.loadContent(page.toString());
  }
  
  /**
   Get the pane containing the webview. 
  
   @return The pane containing the webview. 
  */
  public Pane getPane() {
    return webPane;
  }

}
