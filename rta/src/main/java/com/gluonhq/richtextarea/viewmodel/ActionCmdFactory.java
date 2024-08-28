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
package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Block;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TableDecoration;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.TextAlignment;

public final class ActionCmdFactory {

    private final ActionCmd copy  = new ActionCmdCopy();
    private final ActionCmd cut   = new ActionCmdCut();
    private final ActionCmd paste = new ActionCmdPaste();

    private final ActionCmd undo = new ActionCmdUndo();
    private final ActionCmd redo = new ActionCmdRedo();

    private final ActionCmd newDocument = new ActionCmdNew();
    private final ActionCmd save = new ActionCmdSave();

    private final ActionCmd selectAll = new ActionCmdSelectAll();
    private final ActionCmd selectNone = new ActionCmdSelectNone();

    public ActionCmd copy() {
        return copy;
    }

    public ActionCmd cut() {
        return cut;
    }

    public ActionCmd paste() {
        return paste;
    }

    public ActionCmd undo() {
        return undo;
    }

    public ActionCmd redo() {
        return redo;
    }

    public ActionCmd newDocument() {
        return newDocument;
    }

    public ActionCmd open(Document document) {
        return new ActionCmdOpen(document);
    }

    public ActionCmd save() {
        return save;
    }

    public ActionCmd selectAll() {
        return selectAll;
    }

    public ActionCmd selectNone() {
        return selectNone;
    }

    public ActionCmd selectAndDecorate(Selection selection, Decoration decoration) {
        return new ActionCmdSelectAndDecorate(selection, decoration);
    }

    public ActionCmd removeExtremesAndDecorate(Selection selection, Decoration decoration) {
        return new ActionCmdRemoveExtremesAndDecorate(selection, decoration);
    }

    public ActionCmd selectAndInsertText(Selection selection, String text) {
        return new ActionCmdSelectAndInsertText(text, selection);
    }
    public ActionCmd insertText(String text) {
        return new ActionCmdInsertText(text);
    }

    public ActionCmd insertEmoji(Emoji emoji) {
        return new ActionCmdInsertEmoji(emoji, null, false);
    }

    public ActionCmd selectAndInsertEmoji(Selection selection, Emoji emoji, boolean undoLast) {
        return new ActionCmdInsertEmoji(emoji, selection, undoLast);
    }

    public ActionCmd insertBlock(Block block) {
        return new ActionCmdInsertBlock(block, null);
    }

    public ActionCmd selectAndInsertBlock(Selection selection, Block block) {
        return new ActionCmdInsertBlock(block, selection);
    }

    public ActionCmd pasteDocument(Document document) {
        return new ActionCmdPasteDocument(document);
    }

    public ActionCmd insertTable(TableDecoration tableDecoration) {
        return new ActionCmdTable(tableDecoration);
    }

    public ActionCmd deleteTable() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_TABLE);
    }

    public ActionCmd insertTableColumnBefore() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_COLUMN_BEFORE);
    }

    public ActionCmd insertTableColumnAfter() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_COLUMN_AFTER);
    }

    public ActionCmd deleteTableColumn() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_COLUMN);
    }

    public ActionCmd insertTableRowAbove() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_ROW_ABOVE);
    }

    public ActionCmd insertTableRowBelow() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_ROW_BELOW);
    }

    public ActionCmd deleteTableRow() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_ROW);
    }

    public ActionCmd deleteTableCell() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_CELL_CONTENT);
    }

    public ActionCmd alignTableCell(TextAlignment textAlignment) {
        return new ActionCmdTable(textAlignment);
    }

    public ActionCmd removeText(int caretOffset) {
        return new ActionCmdRemoveText(caretOffset);
    }

    public ActionCmd removeText(int caretOffset, RichTextAreaViewModel.Remove remove) {
        return new ActionCmdRemoveText(caretOffset, remove);
    }

    public ActionCmd replaceText(String text) {
        return new ActionCmdReplaceText(text);
    }

    public ActionCmd decorate(Decoration... decorations) {
        return new ActionCmdDecorate(decorations);
    }

    public ActionCmd caretMove(RichTextAreaViewModel.Direction direction, KeyEvent event) {
        return new ActionCmdCaretMove(direction, event);
    }

    public ActionCmd caretMove(RichTextAreaViewModel.Direction direction, boolean changeSelection, boolean wordSelection, boolean lineSelection) {
        return new ActionCmdCaretMove(direction, changeSelection, wordSelection, lineSelection);
    }

    public ActionCmd insertAndDecorate(String content, Decoration decoration) {
        return new ActionCmdInsertAndDecorate(content, decoration);
    }

    public ActionCmd selectCell(Selection selection) {
        return new ActionCmdSelectCell(selection);
    }

}
