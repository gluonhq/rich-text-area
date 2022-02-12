# RichTextArea component

## Design Notes

Component's internal design is based on MVVM pattern.

### Model aka TextBuffer and its implementations

The data model  is based on piece table implementation, which helps to easily deal 
with huge texts and simplifies to some extent undo functionality. It implements insert, 
append and delete text operations and a simplistic change listening. Within the piece table 
changes represented by one or more pieces. Piece does not contain the text itself but points 
to one of two buffers: original - fixed original text, additional - "add only" buffer for changes. 
Actual text can be restored by walking the prices. For now, each piece also contains simplistic 
`TextDecoration` definition, which is meant to be used to decorate related text. The model also 
implements an undo/redo mechanism which uses pieces but is based on abstract independent API 
(which is reused ViewModel too). Each document operation is represented as a command, which stores 
the state and can be undone/redone.

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




