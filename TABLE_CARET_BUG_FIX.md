# Table Caret Visual Jump Bug - Documentation

## Bug Description

When typing in a table cell, the visual cursor jumps one line down, even though the input caret is logically positioned correctly.

**Example:** After typing "a" in the first cell of a 4x4 table:
- Logical caret position: 1 ✅
- Visual cursor position: Jumps from Y=0 to Y=19 (one line height) ❌

## Root Cause

The `caretPositionListener` in `ParagraphTile.java` fires **synchronously** immediately after the ViewModel change. However, for tables, the TextFlow content is only updated **asynchronously** by the VirtualFlow. As a result, `textFlow.caretShape()` calculates the cursor position based on the old (empty) TextFlow content, causing a Y-jump of ~19 pixels.

**Incorrect flow:**
1. User types "a"
2. ViewModel updates TextBuffer → fires `caretPositionProperty`
3. `caretPositionListener` fires IMMEDIATELY
4. `textFlow.caretShape(1, true)` is called
5. TextFlow still has OLD (empty) content → Position 1 is already on line 2
6. Cursor jumps down (Y=38 instead of Y=19)
7. Only afterwards is TextFlow populated with new content

## Fix

**File:** `rta/src/main/java/com/gluonhq/richtextarea/ParagraphTile.java`

The `caretPositionListener` must be wrapped with `Platform.runLater` so that the cursor update happens in the next JavaFX event loop iteration - after the TextFlow has been populated with the current content.

### Before (buggy):

```java
private final ChangeListener<Number> caretPositionListener = (o, ocp, p) -> updateCaretPosition(p.intValue());
```

### After (fixed):

```java
private final ChangeListener<Number> caretPositionListener = (o, ocp, p) -> 
    Platform.runLater(() -> updateCaretPosition(p.intValue()));
```

## Unit Test

**File:** `rta/src/test/java/com/gluonhq/richtextarea/ui/TableCaretDebugTest.java`

The test `testTableCaretVisualJumpBug` reproduces the bug automatically:

```java
@Test
@DisplayName("Table Caret Bug - Visual cursor jumps down after typing")
public void testTableCaretVisualJumpBug(FxRobot robot) throws Exception, InterruptedException {
    // Step 1: Create 4x4 table
    createTableInRTA();
    waitForFxEvents();
    Thread.sleep(500);

    // Step 2: Position caret at start of first cell
    Platform.runLater(() -> {
        RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
        com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
        for (Paragraph p : vm.getParagraphList()) {
            if (p.getDecoration().hasTableDecoration()) {
                vm.setCaretPosition(p.getStart());
                break;
            }
        }
    });
    waitForFxEvents();
    Thread.sleep(300);

    // Record visual position BEFORE typing
    double[] visualPosBefore = new double[2];
    Platform.runLater(() -> {
        Point2D caretOrigin = richTextArea.getCaretOrigin();
        visualPosBefore[0] = caretOrigin.getX();
        visualPosBefore[1] = caretOrigin.getY();
    });
    waitForFxEvents();

    // Type 'a'
    robot.write("a");
    waitForFxEvents();
    Thread.sleep(300);

    // Record visual position AFTER typing
    double[] visualPosAfter = new double[2];
    final int[] caretPositionAfter = new int[1];
    Platform.runLater(() -> {
        RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
        com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
        caretPositionAfter[0] = vm.getCaretPosition();
        Point2D caretOrigin = richTextArea.getCaretOrigin();
        visualPosAfter[0] = caretOrigin.getX();
        visualPosAfter[1] = caretOrigin.getY();
    });
    waitForFxEvents();

    // Assert: Y position should NOT jump down significantly
    double yJump = visualPosAfter[1] - visualPosBefore[1];
    double maxAllowedYJump = 5.0;
    assertTrue(yJump < maxAllowedYJump, 
        "BUG REPRODUCED: Visual cursor jumped down by " + yJump + " pixels after typing 'a'. " +
        "Expected Y position to remain stable, but it moved from " + visualPosBefore[1] + " to " + visualPosAfter[1]);
}
```

## Regression Test Without Debug Output

The measurement no longer depends on `System.out.println` in `ParagraphTile`.
`rta/src/test/java/com/gluonhq/richtextarea/ui/TableCaretVisualPositionTest.java`
reads the same values from the test side:

* the caret `Path` node of the active layer (style class `.caret`): the raw
  `MoveTo` coordinates inside the TextFlow plus the same point in scene
  coordinates,
* `RichTextArea.getCaretOrigin()`, the public caret origin,
* the logical caret position of the view model.

Two tests are part of it:

| Test | Purpose |
|------|---------|
| `typingInTableCellKeepsCaretOnSameLine` | Regression test for the visual jump. It fails with a Y difference of 19.068px (the value documented above) as soon as the `Platform.runLater` wrapper in `caretPositionListener` is removed. |
| `caretVisualPositionFollowsLogicalCaret` | Proves that the measurement really reacts to line changes, so the regression test cannot pass vacuously. |

Every assertion message contains the complete measurement
(`Caret position: ..., visual position: (..., ...), text flow position: (...),
caret origin: (...)`), so a failing run reproduces the old debug output while a
successful run stays quiet.

```bash
./mvnw -B -ntp -Ptest -Dtest=TableCaretVisualPositionTest test -f rta
```

## Running the Tests

```bash
# Run unit test
cd rta && mvn test -Dtest=TableCaretDebugTest#testTableCaretVisualJumpBug

# Test with samples (after mvn install in the rta module)
cd samples && mvn clean javafx:run
```

## Test Results

### Before the Fix (bug):
```
BEFORE: Visual caret position: (0.0, 0.0)
AFTER:  Visual caret position: (0.0, 19.06800079345703)
Y-axis jump: 19.06800079345703 pixels  ❌
```

### After the Fix (correct):
```
BEFORE: Visual caret position: (0.0, 0.0)
AFTER:  Visual caret position: (7.854000091552734, 0.0)
Y-axis jump: 0.0 pixels  ✅
```

## Summary

| Aspect | Details |
|--------|---------|
| **Bug** | Visual cursor jumps one line down when typing in table cells |
| **Root Cause** | `caretPositionListener` fires synchronously before TextFlow update |
| **Fix** | `Platform.runLater` wrapper in `caretPositionListener` |
| **Test** | `TableCaretDebugTest.testTableCaretVisualJumpBug` |
| **Regression Test** | `TableCaretVisualPositionTest` (measurement in the test, no debug output in the production code) |
| **Status** | ✅ Fixed and verified |