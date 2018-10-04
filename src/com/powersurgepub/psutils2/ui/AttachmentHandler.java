package com.powersurgepub.psutils2.ui;

  import com.powersurgepub.psutils2.notenik.*;

  import javafx.scene.control.*;

public interface AttachmentHandler {

  /**
   * Populate the combo box with attachments.
   *
   * @param cb The ComboBox to be populated.
   */
  public void populateAttachmentComboBox(ComboBox cb);

  /**
   * Respond to a user selection of an attachment.
   *
   * @param attachment The Note Attachment that has been selected.
   */
  public void attachmentSelected(Object attachment);

}
