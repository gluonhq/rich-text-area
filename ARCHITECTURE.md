# RichTextArea Architecture

This document provides an overview of the architecture of the **RichTextArea** component.

---

## Architecture Layers

### 1. Presentation Layer (JavaFX UI)
- **RichTextArea**: Main control class (extends `Control`)
- **RichTextAreaSkin**: Custom skin (extends `SkinBase`) handling layout & rendering
- **ParagraphTile**, **RichListCell**, **BackgroundColorPath**, **IndexRangeColor**: Visual components for paragraph rendering, background highlights, and list integration
- **CSS + Properties**: Styling and configuration

### 2. ViewModel Layer
- **Selection**: Manages caret position and selection ranges
- **Paragraph management**: Coordinates how text blocks are represented
- **Factories**  
  - `DefaultLinkCallbackFactory`: Handles hyperlink callbacks  
  - `DefaultParagraphGraphicFactory`: Provides paragraph-level graphics

### 3. Model Layer
- **Piece Table Implementation** (efficient text editing model)  
  - `PieceTable`: Core structure for managing text insertions/deletions  
  - `UnitBuffer`: Stores immutable text units  
  - `Table`: Indexing and mapping between original/edited text
- **Undo/Redo System**  
  - `CmdManager`: Manages commands and undo/redo stacks  

### 4. Action Layer
The **Action Layer** provides **abstraction and extensibility** — developers can register custom actions or override existing ones without touching the model or UI internals.

- **Action** → Represents an executable command, such as `Bold`, `Italic`, `InsertImage`, or `Undo`
- **ActionManager** → Central registry to get different types of `Action` instances
- **Integration with CmdManager** → Each executed action is wrapped as a `Command` to support undo/redo

### 5. Utilities
- **SmartTimer**: Scheduled UI and background tasks
- **Tools**: Helper utilities for formatting, validation, etc.
- **Extensibility Hooks**: Factories & utilities for customization

---

## High-Level Architecture Diagrams

### Component Architecture
```mermaid
flowchart TD
    UI[RichTextArea Control] --> Skin[RichTextAreaSkin]
    Skin --> Paragraphs[ParagraphTile / RichListCell]
    UI --> Selection
    Skin --> ViewModel

    ViewModel --> Model
    Model --> PieceTable
    Model --> CmdManager

    PieceTable --> UnitBuffer
    PieceTable --> Table
```

### Data Flow
```mermaid
sequenceDiagram
    User->>RichTextArea: Input (typing, formatting, selection)
    RichTextArea->>RichTextAreaSkin: Forward events
    RichTextAreaSkin->>Selection: Update caret/selection
    RichTextAreaSkin->>PieceTable: Modify text buffer
    PieceTable->>CmdManager: Register operation (undo/redo)
    PieceTable->>ParagraphTile: Update paragraph rendering
    ParagraphTile->>RichTextAreaSkin: Refresh UI
```

### Undo/Redo Subsystem
```mermaid
flowchart LR
    CmdManager -->|executes| Command
    Command --> PieceTable
    CmdManager -->|undo/redo| History
```
