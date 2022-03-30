package com.gluonhq.emoji.impl.skin;

//import com.gluonhq.attach.keyboard.KeyboardService;
import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.control.EmojiTextArea;
import com.gluonhq.emoji.popup.EmojiPopOver;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.*;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.ReadOnlyStyledDocument;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.StyledSegment;
import org.reactfx.util.Either;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.BreakIterator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gluonhq.emoji.control.EmojiTextArea.Side.LEFT;

public class EmojiTextAreaSkin extends SkinBase<EmojiTextArea> {

    private final EmojiPopOver popOver;
    private final Button emojiButton;
    private final EmojiSuggestion emojiSuggestion;
    private final EmojiStyledTextArea textarea;
    private final VirtualizedScrollPane<EmojiStyledTextArea> virtualizedScrollPane;

    private StyleableDoubleProperty spacing = new SimpleStyleableDoubleProperty(SPACING, this, "spacing", 0.0);

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public EmojiTextAreaSkin(EmojiTextArea control) {

        super(control);
        textarea = new EmojiStyledTextArea();
        // textarea.appendText(control.getText());
        textarea.setWrapText(true);
        virtualizedScrollPane = new VirtualizedScrollPane<>(textarea) {
            @Override
            public Val<Double> totalHeightEstimateProperty() {
                return Var.newSimpleVar(
                        getContent().getPadding().getTop() + 
                        super.totalHeightEstimateProperty().getOrElse(0.0) + 
                        getContent().getPadding().getBottom()
                );
            }

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                getContent().requestLayout();
            }

            @Override
            protected double computeMinWidth(double height) {
                return super.computeMinWidth(height) + getContent().computeMinWidth(height);
            }
        };

        emojiSuggestion = new EmojiSuggestion();
        emojiSuggestion.setOnAction(emojiEvent -> {
            final Emoji emoji = emojiEvent.getEmoji();
            final IndexRange wordIndex = findWordIndexAtCaret();
            if (!(wordIndex.getStart() == 0 && wordIndex.getEnd() == 0)) {
                textarea.replace(wordIndex.getStart(), wordIndex.getEnd(), emoji);
                emojiSuggestion.hide();
            }
        });

        popOver = new EmojiPopOver();
        // TODO: Find a way to perform this elegantly
        popOver.setOnAction(e -> {
            final Emoji emoji = (Emoji) e.getSource();
            final RealLinkedEmoji realLinkedEmoji = new RealLinkedEmoji(emoji);
            ReadOnlyStyledDocument<ParStyle, Either<String, LinkedEmoji>, TextStyle> ros =
                    ReadOnlyStyledDocument.fromSegment(
                            Either.right(realLinkedEmoji),
                            ParStyle.EMPTY,
                            TextStyle.EMPTY,
                            textarea.getSegOps()
                    );
            textarea.replaceSelection(ros);
            textarea.requestFocus();
        });
        updateSide();
        getSkinnable().emojiSideProperty().addListener(o -> updateSide());

        emojiButton = new Button(EmojiData.emojiForText("smile"));
        emojiButton.getStyleClass().add("emoji-button");
        emojiButton.setOnAction(e -> popOver.show(emojiButton));
        getChildren().addAll(virtualizedScrollPane, emojiButton, emojiSuggestion);

        final Region region = new Region();
        region.getStyleClass().add("icon");
        emojiButton.setGraphic(region);
        emojiButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        spacing.addListener(o -> control.requestLayout());

        BooleanProperty isReplacing = new SimpleBooleanProperty();
        textarea.textProperty().addListener((obs, ov, nv) -> {
            if (!isReplacing.get()) {
                isReplacing.set(true);
//                System.out.println("skin Replacing text :::" + ov + "::: with ::" + nv + "::");
                control.setText(nv);
                isReplacing.set(false);
            }
        });
        control.textProperty().addListener((obs, ov, nv) -> {
            if (!isReplacing.get()) {
                isReplacing.set(true);
//                System.out.println("Replace text :::" + nv + "::: from ::" + ov + "::");
                textarea.replaceText(nv);
                isReplacing.set(false);
            }
        });
        control.focusedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                textarea.requestFocus();
            }
        });

//        double fontSize = ((Region) textarea.lookup(".styled-text-area")).getChildrenUnmodifiable().stream()
//                .filter(Text.class::isInstance)
//                .findFirst()
//                .map(Text.class::cast)
//                .map(t -> t.getFont().getSize())
//                .orElse(12.0);
//        Color front = ((Region) textarea.lookup(".styled-text-area")).getChildrenUnmodifiable().stream()
//                .filter(Text.class::isInstance)
//                .findFirst()
//                .map(Text.class::cast)
//                .map(Text::getFill)
//                .map(Color.class::cast)
//                .orElse(Color.BLACK);
//
//        Color back = textarea.getBackground().getFills().stream()
//                .map(BackgroundFill::getFill)
//                .filter(Color.class::isInstance)
//                .findFirst()
//                .map(Color.class::cast)
//                .orElse(Color.WHITE);

//        KeyboardService.create().ifPresent(k ->
//                k.forNode(textarea, control.textProperty(), fontSize, front, back));

    }

    private void updateSide() {
        if (getSkinnable().getEmojiSide() == LEFT) {
            popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
        } else {
            popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {

        final double buttonHeight = emojiButton.prefHeight(-1);
        final double buttonWidth = emojiButton.prefWidth(buttonHeight);

        emojiButton.resizeRelocate(
                getSkinnable().getEmojiSide() == LEFT ? contentX : contentX + contentWidth - buttonWidth,
                contentHeight + snappedTopInset() - buttonHeight, 
                buttonWidth,
                buttonHeight
        );

        virtualizedScrollPane.resizeRelocate(
                getSkinnable().getEmojiSide() == LEFT ? contentX + buttonWidth + spacing.get() : contentX,
                contentY,
                contentWidth - buttonWidth - spacing.get(),
                contentHeight
        );

        final double emojiSuggestionWidth = contentWidth - buttonWidth - spacing.get();
        final double emojiSuggestionHeight = emojiSuggestion.prefHeight(emojiSuggestionWidth);
        emojiSuggestion.resizeRelocate(
                getSkinnable().getEmojiSide() == LEFT ? contentX + buttonWidth + spacing.get() : contentX, 
                contentY - emojiSuggestionHeight, 
                emojiSuggestionWidth, 
                emojiSuggestionHeight
        );
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + virtualizedScrollPane.minWidth(height) + emojiButton.minWidth(-1) + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + virtualizedScrollPane.prefWidth(height) + emojiButton.prefWidth(-1) + rightInset;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + emojiButton.prefHeight(-1) + bottomInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + virtualizedScrollPane.prefHeight(width) + bottomInset;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getScene().getWindow().getHeight() / 2;
    }

    private static Node createNode(StyledSegment<Either<String, LinkedEmoji>, TextStyle> seg, BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
                text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
                LinkedEmoji::createNode);
    }

    /**
     * Returns the IndexRange of the word where caret is currently positioned
     * @return IndexRange of word where caret is currently positioned
     */
    private IndexRange findWordIndexAtCaret() {
        final int caretPosition = textarea.getCaretPosition();
        Pattern pattern = Pattern.compile("\\S+");
        Matcher matcher = pattern.matcher(textarea.createEmojiText());
        while (matcher.find()) {
            if (matcher.start() <= caretPosition && matcher.end() >= caretPosition) {
                return new IndexRange(matcher.start(), matcher.end());
            }
        }
        return IndexRange.valueOf("0, 0");
    }

    /***************************************************************************
     *                                                                         *
     *                         Stylesheet Handling                             *
     *                                                                         *
     **************************************************************************/

    private static final CssMetaData<EmojiTextArea, Number> SPACING =
            new CssMetaData<>("-fx-spacing",
                    SizeConverter.getInstance(), 0.0) {

                @Override
                public boolean isSettable(EmojiTextArea node) {
                    final EmojiTextAreaSkin skin = (EmojiTextAreaSkin) node.getSkin();
                    return skin.spacing == null || !skin.spacing.isBound();
                }

                @Override
                public StyleableProperty<Number> getStyleableProperty(EmojiTextArea node) {
                    final EmojiTextAreaSkin skin = (EmojiTextAreaSkin) node.getSkin();
                    return skin.spacing;
                }
            };

    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
    static {
        final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<>(Control.getClassCssMetaData());
        styleables.add(SPACING);
        STYLEABLES = Collections.unmodifiableList(styleables);
    }

    /**
     * {@inheritDoc}
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /***************************************************************************
     *                                                                         *
     *                         Inner Class                                     *
     *                                                                         *
     **************************************************************************/

    private class EmojiStyledTextArea extends GenericStyledArea<ParStyle, Either<String, LinkedEmoji>, TextStyle> {

        private static final int LINE_SIZE = 22;
        private static final int MIN_WIDTH = 250;

        EmojiStyledTextArea() {
            super(ParStyle.EMPTY,
                    (paragraph, style) -> paragraph.setStyle(style.toCss()),
                    TextStyle.EMPTY,
                    SegmentOps.<TextStyle>styledTextOps()._or(new LinkedEmojiOps<>(), (s1, s2) -> Optional.empty()),
                    seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss())));

            multiRichChanges()
                .successionEnds(Duration.ofMillis(100))
                .subscribe(change -> {
                    Pattern pattern = Pattern.compile(":[\\w-]*:");
                    Matcher matcher = pattern.matcher(createEmojiText());
                    // Using AtomicInteger to avoid creating another variable for final
                    AtomicInteger diff = new AtomicInteger(0);
                    while (matcher.find()) {
                        final int caretPosition = getCaretPosition();
                        if (matcher.start() <= caretPosition && matcher.end() >= caretPosition) {
                            EmojiData.emojiFromCodeName(matcher.group()).ifPresent(emoji -> {
                                final int startIndex = matcher.start() - diff.get();
                                final int endIndex = matcher.end() - diff.get();
                                final int rosLength = replace(startIndex, endIndex, emoji);
                                diff.addAndGet(matcher.end() - matcher.start() - rosLength);
                            });
                        }
                    }
                });

            // selection text should be white
            selectionProperty().addListener((observable, oldValue, newValue) -> {
                // When a text is converted to emoji, the old value contains
                // the old length which can cause IndexOutOfBoundsException
                if (inRange(oldValue)) {
                    setStyle(oldValue.getStart(), oldValue.getEnd(), TextStyle.textColor(Color.BLACK));
                }
                // Do not add white for new paragraphs
                // inRange check is required to avoid IndexOutOfBoundException when a user
                // double click at the end of a sentence
                if (inRange(newValue) && newValue.getStart() != newValue.getEnd()) {
                    setStyle(newValue.getStart(), newValue.getEnd(), TextStyle.textColor(Color.WHITE));
                }
                // Make sure to revert back to BLACK when the TextArea is cleared
                if (newValue.equals(GenericStyledArea.EMPTY_RANGE)) {
                    setStyle(0, 0, TextStyle.textColor(Color.BLACK));
                }
            });
            addBehaviorChanges();
        }

        @Override
        protected double computeMinWidth(double height) {
            return snappedLeftInset() + MIN_WIDTH + snappedRightInset();
        }
        
        @Override
        protected double computePrefHeight(double width) {
            if (!getChildren().isEmpty() && getChildren().get(0) instanceof VirtualFlow) {
                final VirtualFlow virtualFlow = (VirtualFlow) getChildren().get(0);
                int noOfLines = 0;
                int paragraphIndex = 0;
                while (paragraphIndex < getParagraphs().size()) {
                    noOfLines += (Integer) invoke(mGetLineCount, virtualFlow.getCell(paragraphIndex++).getNode());
                }
                return snappedTopInset() + noOfLines * LINE_SIZE + snappedTopInset();
            }
            return snappedTopInset() + getParagraphs().size() * LINE_SIZE + snappedBottomInset();
        }

        private void addBehaviorChanges() {
            addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                // TODO: Find a better way
                if (emojiSuggestion.isVisible()) {
                    switch (e.getCode()) {
                        case ENTER:
                        case TAB:
                        case RIGHT:
                        case LEFT:
                        case UP:
                        case DOWN:
                        case SHIFT:
                            emojiSuggestion.fireEvent(e);
                            e.consume();
                            requestFocus();
                            return;
                    }
                }
                if (new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHIFT_DOWN).match(e) ||
                        new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN).match(e)) {
                    replaceSelection("\n");
                    requestFollowCaret();
                    e.consume();
                } else if (e.getCode() == KeyCode.ENTER) {
                    if (getSkinnable().getOnAction() != null) {
                        getSkinnable().getOnAction().handle(new ActionEvent());
                    }
                    e.consume();
                }
            });
            
            addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                // Need a delay for the text property to be updated
                Platform.runLater(() -> checkAndShowPopup(e));
            });

            addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (e.getClickCount() == 2) {
                    selectWord();
                    e.consume();
                }
            });
        }

        public void selectWord() {
            BreakIterator breakIterator = BreakIterator.getWordInstance();
            breakIterator.setText(createEmojiText());

            int start = calculatePositionViaBreakingBackwards(1, breakIterator, getCaretPosition());
            int end = calculatePositionViaBreakingForwards(1, breakIterator, getCaretPosition());
            selectRange(start, end);
        }

        String createEmojiText() {
            StringBuilder text = new StringBuilder();
            for (int index = 0; index < getParagraphs().size(); index++) {
                if (index > 0) {
                    text.append("\n");
                }
                Paragraph<ParStyle, Either<String, LinkedEmoji>, TextStyle> paragraph = getParagraphs().get(index);
                for (Either<String, LinkedEmoji> segments : paragraph.getSegments()) {
                    if (segments.isLeft()) {
                        final String left = segments.getLeft();
                        text.append(left);
                    } else if (segments.isRight()) {
                        text.append("E"); // A dummy character for Emoji
                    }
                }
            }
            return text.toString();
        }

        /**
         * Replaces a range with emoji, adds a whitespace after emoji
         * and return the length of the document replaced
         */
        int replace(int start, int end, Emoji emoji) {
            final RealLinkedEmoji realLinkedEmoji = new RealLinkedEmoji(emoji);
            ReadOnlyStyledDocument<ParStyle, Either<String, LinkedEmoji>, TextStyle> ros =
                    ReadOnlyStyledDocument.fromSegment(
                            Either.right(realLinkedEmoji),
                            ParStyle.EMPTY,
                            TextStyle.EMPTY,
                            getSegOps()
                    );
            replace(start, end, ros);
            textarea.insertText(textarea.getCaretPosition(), " ");
            return ros.length();
        }

        private void checkAndShowPopup(KeyEvent e) {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.SPACE) {
                emojiSuggestion.hide();
            } else {
                final String emojiWord = findEmojiWordAtCaret();
                if (!emojiWord.isEmpty()) {
                    final List<Emoji> emojis = EmojiData.search(emojiWord.substring(1));
                    if (emojis.isEmpty()) {
                        emojiSuggestion.hide();
                    } else {
                        emojiSuggestion.getEmojis().setAll(emojis);
                        if (!emojiSuggestion.isVisible()) {
                            emojiSuggestion.show();
                        }
                    }
                } else {
                    emojiSuggestion.hide();
                }
            }
        }

        // Finds the word in the given string at the given index and checks if it resembles an emoji
        // If the index is greater than the length of the string, it will return empty
        private String findEmojiWordAtCaret() {
            Pattern pattern = Pattern.compile(":[\\w-]{3,}");
            Matcher matcher = pattern.matcher(createEmojiText());
            while (matcher.find()) {
                final int caretPosition = getCaretPosition();
                if (matcher.start() <= caretPosition && matcher.end() >= caretPosition) {
                    return matcher.group();
                }
            }
            return "";
        }

        /** Assumes that {@code getArea().getLength != 0} is true and {@link BreakIterator#setText(String)} has been called */
        private int calculatePositionViaBreakingForwards(int numOfBreaks, BreakIterator breakIterator, int position) {
            breakIterator.following(position);
            for (int i = 1; i < numOfBreaks; i++) {
                breakIterator.next(numOfBreaks);
            }
            return breakIterator.current();
        }

        /** Assumes that {@code getArea().getLength != 0} is true and {@link BreakIterator#setText(String)} has been called */
        private int calculatePositionViaBreakingBackwards(int numOfBreaks, BreakIterator breakIterator, int position) {
            breakIterator.preceding(position);
            for (int i = 1; i < numOfBreaks; i++) {
                breakIterator.previous();
            }
            return breakIterator.current();
        }

        private boolean inRange(IndexRange indexRange) {
            final int textWithEmojiLength = createEmojiText().length();
            return indexRange.getStart() < textWithEmojiLength &&
                     indexRange.getEnd() < textWithEmojiLength;
        }
    }

    private static Method mGetLineCount;
    static {
        try {
            mGetLineCount = Class.forName("org.fxmisc.richtext.ParagraphBox").getDeclaredMethod("getLineCount");
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        mGetLineCount.setAccessible(true);
    }

    private static Object invoke(Method m, Object obj, Object... args) {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
