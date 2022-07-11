# The Rich Text Area Project

Gluon presents a new JavaFX control, created with Java and JavaFX standard APIs, called the `RichTextArea` control.

## Rich Text Area control

Based on the combination of VirtualFlow and multiple TextFlow controls, the RichTextArea control has features like:

– Any part of the content can be selected and can have any styling (all font attributes, foreground or background color, …). This can be changed at any time. Selection of caret navigation of words, lines or paragraphs and can be done via mouse and keyboard platform-standard keys (arrows and key modifiers).
– Any paragraph can be styled with text alignment, line separation, indentation, bulleted or numbered list.
– Has support for non-text nodes like images that can be added at any point, even via drag and drop
– Has support for event handling of hyperlinks and image custom actions
– Copy/cut/paste, unlimited undo and redo are supported.
– Save/open documents to persist and restore the content with its different styles.
– Inline table support
– … (and more to come)

### License

The RichTextArea control is available for free under the GPLv3 license. If you create an Open Source application, you can use our software for free.
The Gluon Mobile license includes commercial usage of the RichTextArea.

### Usage

To use the RichTextArea control in your project add the following dependency:

```
    <dependencies>
        <dependency>
            <groupId>com.gluonhq</groupId>
            <artifactId>rich-text-area-control</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>gluon-nexus-releases</id>
            <url>https://nexus.gluonhq.com/nexus/content/repositories/releases/</url>
        </repository>
    </repositories>    
 ```

and then simply create an instance and add it to your JavaFX application:

```
    @Override
    public void start(Stage stage) {
        RichTextArea editor = new RichTextArea();
        BorderPane root = new BorderPane(editor);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
```

### Samples

#### RichTextEditor sample

One possible use of the RichTextArea control is as a RichTextEditor. The sample includes a number of menus and toolbars that allow the user apply actions over selections or at the caret location.

#### Instructions

To run this sample, using Java 11+, do as follows:

    mvn javafx:run

## Issues and Contributions

Issues can be reported to the [Issue tracker](https://github.com/gluonhq/rich-text-area/issues)

Contributions can be submitted via [Pull requests](https://github.com/gluonhq/rich-text-area/pulls),
providing you have signed the [Gluon Individual Contributor License Agreement (CLA)](https://cla.gluonhq.com).

## Design Notes

Component's internal design is based on MVVM pattern.

### Model aka TextBuffer and its implementations

The data model is based on piece table implementation, which helps to easily deal 
with huge texts and simplifies to some extent undo functionality. It implements insert, 
append and delete text operations and a simplistic change listening. Within the piece table 
changes are represented by one or more pieces. Piece does not contain the text itself but points 
to one of two buffers: original - fixed original text, additional - "add only" buffer for changes. 
Actual text can be restored by walking the pieces. Each piece contains text, image and paragraph 
decoration. The model also implements an undo/redo mechanism which uses pieces but is based on 
abstract independent API (which is reused ViewModel too). Each document operation is represented 
as a command, which stores the state and can be undone/redone.

### ViewModel aka RichTextAreaViewModel

This is where all the “business” logic concentrated, fully independent of the view. Given the text 
buffer(model) it can manipulate it by either executing predefined actions or internal methods. 
Internally, some of its actions are using builtin undo/redo manager again, since in addition 
to manipulating text, we have to deal with additional state - caret position and text selection. 
Actions are independent of keyboard shortcuts, which are defined in the view, and in theory 
can even allow us to define custom keyboard mappings for the component in the future, kind of like IDEs do.

### View aka RichTextAreaSkin

The view is where all this comes together. Currently, implementation of the view is based on TextFlow, 
but it can be anything (Canvas for example, if TextFlow proves to be not performant enough for texts 
with a lot of  fragments). View can react on changes within ViewModel and also ask view model to execute actions.
For example, when caret position or text selection changes in viewModel, view reacts and represents them visually.
It also has a (currently fixed) map of keyboard shortcuts to actions. Those actions are then executed by those shortcuts 
automatically. Currently, each document change refreshes the whole text flow, which is very inefficient, 
but that can change later, once the design is ironed out. The view supports some mouse operations already, 
such as moving the caret and selecting text. More planned in the future (see project within the repo), such as selecting
a word by double-clicking and more.


## Project Notes

- All tasks are kept within Project's kanban board with the repo
- Tasks are converted to issues as they are prepared to be worked on
- Tasks are organized into milestones to simplify progress/release tracking.




