# The Rich Text Area Project - Implementation Details

This document will shed some light into the implementation details of the project
to make it easier for developers to understand and contribute back to it.

We'll discuss some important classes:

## RichTextAreaSkin

Contains the skin/view of the control.

The skin contains just one child aka `ParagraphListView`, which is a custom `ListView` and contains `RichListCell`
to represent each `Paragraph` in the TextArea. Each of these Paragraph can contain a Text along with Emojis, Image or a Table.

## Document

Document is the basic model that contains all the information required for the RichTextArea control,
in order to render all the rich content, including decorated text, images and other non-text objects.
A document is basically a string with the full text, and a list of DecorationModel that contain the text and paragraph decorations for one or more fragments of the text,
where a fragment can be defined as the longest substring of the text that shares the same text and paragraph decorations.
Any change to the document invalidates the undo/redo stack, forces the RichTextAreaSkin to recreate the `PieceTable` and sets it on the `RichTextAreaViewModel`.

## Decoration
Decorations are changes that can be performed on RTA’s fragments like Text, Image, Table etc.

### TextDecoration
TextDecorations are the simplest decorations that can be applied to Text fragments and can be used to change the font family, weight, size, color etc. of the text.

### ImageDecoration
ImageDecorations can be used to change the width and height of an Image. It can also be used to add a hyperlink to the image.

### TableDecoration
Decorations related to a Table: number of rows, columns and text alignment of each of the cells.

### ParagraphDecoration
Decorations which can be applied directly at a paragraph level. For example, text alignment, indentation, line spacing etc.

## AbstractCommand

AbstractCommand is an internal API in the `com.gluonhq.richtextarea.undo` package, so basically it is a way of saying that everything done under it can be redone/undone, and the `CommandManager` will take care of it.
This is an internal API, not exposed to the users, and it deals directly with PieceTable.
Each implementation define a command or group of commands that will be applied in a batch operation, so they can be undone/redone in a single call.

Typically, these actions can be broadly categorised into:

### Normal actions
Actions like insert, append, replace, delete 

### Decorate actions
Actions like text, image, table, paragraph

## ActionCmd

`ActionCmd` is pretty much the public API for accessing the same private API which are present in `AbstractCommand`.
It defines actions, commands, or operations that the user can perform over the control, like the same above.

Here are few actions that the user can execute:

new, open, save
cut, copy, paste,
select,
undo, redo
create table

## CommandManager

CommandManager is responsible for the undo/redo actions performed in RTA.
It contains a stack containing all the actions performed by the user, so that at any point of time these actions can be undone via undo, or re-executed via redo actions.

## Design Notes

Component's internal design is based on MVVM pattern.

### Model aka TextBuffer and its implementations

The data model is based on piece table implementation, which helps to easily deal
with huge texts and simplifies to some extent undo functionality. It implements insert,
append and delete text operations and a simplistic change listening. Within the piece table
changes are represented by one or more pieces. Piece does not contain the text itself but points
to one of two buffers: original - fixed original text, additional - "add only" buffer for changes.
Actual text can be restored by walking the pieces. Each piece can contain text, image, table, or paragraph
decoration. The model also implements an undo/redo mechanism which uses pieces but is based on
abstract independent API (which is reused ViewModel too). Each document operation is represented
as a command, which stores the state and can be undone/redone.

### ViewModel aka RichTextAreaViewModel

This is where all the “business” logic is concentrated, fully independent of the view. Given the text
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
automatically.

The view also supports some mouse operations already, like:

- Move the caret and select text
- Select a word by two clicks
- Select a paragraph with three clicks

Currently, each document change refreshes the whole text flow, which is very inefficient,
but that can change later, once the design is ironed out.
