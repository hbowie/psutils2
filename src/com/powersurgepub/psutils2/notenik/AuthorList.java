package com.powersurgepub.psutils2.notenik;

  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.ui.*;

  import javafx.scene.control.*;

public class AuthorList
    extends ValueList {

  /** Creates a new instance of TagsList */
  public AuthorList (ComboBox<String> comboBox) {
    super(comboBox);
  }

  public AuthorList () {
    super();
  }

  public void add(Note note) {
    if (note.hasAuthor()) {
      Author author = note.getAuthor();
      registerValue (author.getCompleteName());
      registerValue (author.getCompleteNameLastNamesFirst());
    }
  }

  public void modify(Note note) {
    if (note.hasAuthor()) {
      Author author = note.getAuthor();
      registerValue (author.getCompleteName());
      registerValue (author.getCompleteNameLastNamesFirst());
    }
  }

  public void remove(Note note) {
    // No need to do anything
  }
}
