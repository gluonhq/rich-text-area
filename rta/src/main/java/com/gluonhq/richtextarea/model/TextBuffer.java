/*
 * Copyright (c) 2022, 2024, Gluon
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
package com.gluonhq.richtextarea.model;

import com.gluonhq.richtextarea.Selection;
import javafx.beans.property.ReadOnlyIntegerProperty;

import java.text.CharacterIterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface TextBuffer {

    String ZERO_WIDTH_TEXT = "\u200b";
    String ZERO_WIDTH_NO_BREAK_SPACE_TEXT = "\ufeff";
    String OBJECT_REPLACEMENT_CHARACTER_TEXT = "\ufffc";
    String EMOJI_ANCHOR_TEXT = "\u2063";
    char ZERO_WIDTH_TABLE_SEPARATOR = '\u200b';

    int getTextLength();
    ReadOnlyIntegerProperty textLengthProperty();
    String getText();
    String getText(int start, int end);
    int getInternalPosition(int position);
    Selection getInternalSelection(Selection selection);
    List<DecorationModel> getDecorationModelList(int start, int end);

    CharacterIterator getCharacterIterator();
    char charAt(int pos);
    List<Integer> getLineFeeds();
    void resetCharacterIterator();

    void insert(String text, int insertPosition);
    void append(String text);
    void delete(final int deletePosition, int length);

    /**
     * Adds decoration to Text in the specified range.
     * @param start index to start, inclusive.
     * @param end index to end, exclusive.
     * @param decoration decoration to apply.
     */
    void decorate(int start, int end, Decoration decoration);

    void undo();
    void redo();

    void walkFragments(BiConsumer<Unit, Decoration> onFragment, int start, int end);

    void addChangeListener(Consumer<TextBuffer.Event> listener);
    void removeChangeListener(Consumer<TextBuffer.Event> listener);

    Decoration getDecorationAtCaret(int caretPosition);
    void setDecorationAtCaret(TextDecoration decoration);
    ParagraphDecoration getParagraphDecorationAtCaret(int caretPosition);

    interface Event {}

    class InsertEvent implements Event {

        private final String text;
        private final int position;

        InsertEvent(String text, int position) {
            this.text = text;
            this.position = position;
        }

        public String getText() {
            return text;
        }

        public int getPosition() {
            return position;
        }
    }

    class DeleteEvent implements Event {

        private final int position;
        private final int length;

        DeleteEvent(int position, int length) {
            this.position = position;
            this.length = length;
        }

        public int getPosition() {
            return position;
        }

        public int getLength() {
            return length;
        }
    }

    class DecorateEvent implements Event {

        private final int start;
        private final int end;
        private final Decoration decoration;

        DecorateEvent(int start, int end, Decoration decoration) {
            this.start = start;
            this.end = end;
            this.decoration = decoration;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public Decoration getDecoration() {
            return decoration;
        }
    }

}

