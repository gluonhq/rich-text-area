/*
 * Copyright (c) 2022, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea;

import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gluonhq.richtextarea.model.TableDecoration.TABLE_SEPARATOR;

class RichListCell extends ListCell<Paragraph> {

    private static final Font MIN_LF_FONT = Font.font(10);

    private final RichTextAreaSkin richTextAreaSkin;
    private final ParagraphTile paragraphTile;

    RichListCell(RichTextAreaSkin richTextAreaSkin) {
        this.richTextAreaSkin = richTextAreaSkin;
        // controls spacing between paragraphs
        // (it is also needed to avoid Font 13 for text, even if it is graphic only)
        // TODO: control paragraph spacing
        setFont(MIN_LF_FONT);

        paragraphTile = new ParagraphTile(richTextAreaSkin);
        setText(null);

        addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            event.consume();
            startFullDrag();
            richTextAreaSkin.anchorIndex = getIndex();
        });
        addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
            event.consume();
            if (richTextAreaSkin.anchorIndex != -1) {
                getParagraphTile().ifPresent(p -> p.mouseDraggedListener(event));
            }
        });
        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            getParagraphTile().ifPresentOrElse(
                    p -> p.mousePressedListener(event),
                    () -> {
                        if (!richTextAreaSkin.getSkinnable().isFocused()) {
                            richTextAreaSkin.getSkinnable().requestFocus();
                        }
                        // process click event on lower empty cells
                        int textLength = richTextAreaSkin.getViewModel().getTextLength();
                        if (richTextAreaSkin.getViewModel().getSelection().isDefined()) {
                            if (!event.isShiftDown()) {
                                richTextAreaSkin.getViewModel().clearSelection();
                            }
                        } else {
                            // move caret to beginning or end of document
                            richTextAreaSkin.getViewModel().setCaretPosition(textLength);
                        }
                        // allow dragging
                        if (richTextAreaSkin.anchorIndex == -1) {
                            richTextAreaSkin.mouseDragStart = textLength;
                        }
                        // hide context menu
                        if (richTextAreaSkin.contextMenu.isShowing()) {
                            richTextAreaSkin.contextMenu.hide();
                        }
                    });
        });
        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (richTextAreaSkin.anchorIndex != -1) {
                event.consume();
                richTextAreaSkin.mouseDragStart = -1;
                richTextAreaSkin.anchorIndex = -1;
            }
        });
        addEventHandler(DragEvent.DRAG_OVER, de -> {
            if (richTextAreaSkin.dragAndDropStart != -1) {
                getParagraphTile().ifPresent(p -> {
                    if (!richTextAreaSkin.getSkinnable().isFocused()) {
                        richTextAreaSkin.getSkinnable().requestFocus();
                    }
                    // caret follows dnd movement
                    p.mousePressedListener(new MouseEvent(de.getSource(), de.getTarget(), MouseEvent.MOUSE_PRESSED,
                            de.getX(), de.getY(), de.getScreenX(), de.getScreenY(),
                            MouseButton.PRIMARY, 1,
                            false, false, false, false,
                            true, false, false,
                            false, false, false, null));
                });
            }
        });
    }

    @Override
    protected void updateItem(Paragraph item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            var fragments = new ArrayList<Node>();
            var backgroundIndexRanges = new ArrayList<IndexRangeColor>();
            var length = new AtomicInteger();
            var positions = new ArrayList<Integer>();
            positions.add(item.getStart());
            AtomicInteger tp = new AtomicInteger(item.getStart());
            richTextAreaSkin.getViewModel().walkFragments((text, decoration) -> {
                if (decoration instanceof TextDecoration && !text.isEmpty()) {
                    if (item.getDecoration().hasTableDecoration()) {
                        AtomicInteger s = new AtomicInteger();
                        IntStream.iterate(text.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR),
                                        index -> index >= 0,
                                        index -> text.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR, index + 1))
                                .boxed()
                                .forEach(i -> {
                                    String tableText = text.substring(s.getAndSet(i + 1), i + 1);
                                    final Text textNode = buildText(tableText, (TextDecoration) decoration);
                                    textNode.getProperties().put(TABLE_SEPARATOR, tp.get());
                                    fragments.add(textNode);
                                    positions.add(tp.addAndGet(tableText.length()));
                                });
                        if (s.get() < text.length()) {
                            String tableText = text.substring(s.get()).replace("\n", TextBuffer.ZERO_WIDTH_TEXT);
                            final Text textNode = buildText(tableText, (TextDecoration) decoration);
                            textNode.getProperties().put(TABLE_SEPARATOR, tp.getAndAdd(tableText.length()));
                            fragments.add(textNode);
                            if (text.substring(s.get()).contains("\n")) {
                                positions.add(tp.get());
                            }
                        }
                    } else {
                        final Text textNode = buildText(text.replace("\n", TextBuffer.ZERO_WIDTH_TEXT), (TextDecoration) decoration);
                        fragments.add(textNode);
                        Color background = ((TextDecoration) decoration).getBackground();
                        if (background != Color.TRANSPARENT) {
                            backgroundIndexRanges.add(new IndexRangeColor(
                                    length.get(), length.get() + text.length(), background));
                        }
                    }
                    length.addAndGet(text.length());
                } else if (decoration instanceof ImageDecoration) {
                    fragments.add(buildImage((ImageDecoration) decoration));
                    length.incrementAndGet();
                    richTextAreaSkin.nonTextNodes.incrementAndGet();
                }
            }, item.getStart(), item.getEnd());
            paragraphTile.setParagraph(item, fragments, positions, backgroundIndexRanges);
            setGraphic(paragraphTile);
            // required: update caret and selection
            paragraphTile.updateLayout();
        } else {
            // clean up listeners
            paragraphTile.setParagraph(null, null, null, null);
            setGraphic(null);
        }
    }

    private Text buildText(String content, TextDecoration decoration) {
        if ("\n".equals(content)) {
            Text lfText = new Text(TextBuffer.ZERO_WIDTH_TEXT);
            lfText.setFont(MIN_LF_FONT);
            return lfText;
        }
        Objects.requireNonNull(decoration);
        Text text = new Text(Objects.requireNonNull(content));
        text.setFill(decoration.getForeground());
        text.setStrikethrough(decoration.isStrikethrough());
        text.setUnderline(decoration.isUnderline());

        // Caching fonts, assuming their reuse, especially for default one
        int hash = Objects.hash(
                decoration.getFontFamily(),
                decoration.getFontWeight(),
                decoration.getFontPosture(),
                decoration.getFontSize());

        Font font = richTextAreaSkin.getFontCache().computeIfAbsent(hash,
                h -> Font.font(
                        decoration.getFontFamily(),
                        decoration.getFontWeight(),
                        decoration.getFontPosture(),
                        decoration.getFontSize()));

        text.setFont(font);
        String url = decoration.getURL();
        if (url != null) {
            text.setUnderline(true);
            text.setFill(Color.BLUE);
            text.setCursor(Cursor.HAND);
            text.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    Function<Node, Consumer<String>> linkCallbackFactory = richTextAreaSkin.getSkinnable().getLinkCallbackFactory();
                    if (linkCallbackFactory != null) {
                        Consumer<String> consumer = linkCallbackFactory.apply(text);
                        if (consumer != null) {
                            consumer.accept(url);
                        }
                    }
                }
            });
        }
        return text;
    }

    private ImageView buildImage(ImageDecoration imageDecoration) {
        Image image = richTextAreaSkin.getImageCache().computeIfAbsent(imageDecoration.getUrl(), Image::new);
        final ImageView imageView = new ImageView(image);
        // TODO Create resizable ImageView
        if (imageDecoration.getWidth() > -1 && imageDecoration.getHeight() > -1) {
            imageView.setFitWidth(imageDecoration.getWidth());
            imageView.setFitHeight(imageDecoration.getHeight());
        } else {
            // for now, limit the image within the content area
            double width = Math.min(image.getWidth(), richTextAreaSkin.textFlowPrefWidthProperty.get() - 10);
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }
        if (imageDecoration.getLink() != null) {
            imageView.setCursor(Cursor.HAND);
            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                Function<Node, Consumer<String>> linkCallbackFactory = richTextAreaSkin.getSkinnable().getLinkCallbackFactory();
                if (linkCallbackFactory != null) {
                    Consumer<String> consumer = linkCallbackFactory.apply(imageView);
                    if (consumer != null) {
                        consumer.accept(imageDecoration.getLink());
                    }
                }
            });
        }
        return imageView;
    }

    void evictUnusedObjects() {
        getParagraphTile().ifPresent(ParagraphTile::evictUnusedObjects);
    }

    public void forwardDragEvent(MouseEvent e) {
        getParagraphTile().ifPresent(tile -> tile.mouseDraggedListener(e));
    }

    public boolean hasCaret() {
        return getParagraphTile()
                .map(ParagraphTile::hasCaret)
                .orElse(false);
    }

    public int getNextRowPosition(double x, boolean down) {
        return getParagraphTile()
                .map(tile -> tile.getNextRowPosition(x, down))
                .orElse(-1);
    }

    private Optional<ParagraphTile> getParagraphTile() {
        if (getGraphic() instanceof ParagraphTile) {
            return Optional.of((ParagraphTile) getGraphic());
        }
        return Optional.empty();
    }

}
