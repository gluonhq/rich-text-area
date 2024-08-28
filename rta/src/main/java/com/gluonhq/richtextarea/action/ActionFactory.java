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
package com.gluonhq.richtextarea.action;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Block;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.viewmodel.ActionCmdFactory;

public class ActionFactory {

    private static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();
    private final RichTextArea control;

    public ActionFactory(RichTextArea control) {
        this.control = control;
    }

    private Action undo;

    public Action undo() {
        if (undo == null) {
            undo = new BasicAction(control, action -> ACTION_CMD_FACTORY.undo());
        }
        return undo;
    }

    private Action redo;

    public Action redo() {
        if (redo == null) {
            redo = new BasicAction(control, action -> ACTION_CMD_FACTORY.redo());
        }
        return redo;
    }

    private Action copy;

    public Action copy() {
        if (copy == null) {
            copy = new BasicAction(control, action -> ACTION_CMD_FACTORY.copy());
        }
        return copy;
    }

    private Action cut;

    public Action cut() {
        if (cut == null) {
            cut = new BasicAction(control, action -> ACTION_CMD_FACTORY.cut());
        }
        return cut;
    }

    private Action paste;

    public Action paste() {
        if (paste == null) {
            paste = new BasicAction(control, action -> ACTION_CMD_FACTORY.paste());
        }
        return paste;
    }

    private Action newDocument;

    public Action newDocument() {
        if (newDocument == null) {
            newDocument = new BasicAction(control, action -> ACTION_CMD_FACTORY.newDocument());
        }
        return newDocument;
    }

    public Action open(Document document) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.open(document));
    }

    private Action save;

    public Action save() {
        if (save == null) {
            save = new BasicAction(control, action -> ACTION_CMD_FACTORY.save());
        }
        return save;
    }

    private Action selectAll;
    public Action selectAll() {
        if (selectAll == null) {
            selectAll = new BasicAction(control, action -> ACTION_CMD_FACTORY.selectAll());
        }
        return selectAll;
    }

    private Action selectNone;
    public Action selectNone() {
        if (selectNone == null) {
            selectNone = new BasicAction(control, action -> ACTION_CMD_FACTORY.selectNone());
        }
        return selectNone;
    }

    public Action selectAndDecorate(Selection selection, Decoration decoration) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.selectAndDecorate(selection, decoration));
    }

    public Action removeExtremesAndDecorate(Selection selection, Decoration decoration) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.removeExtremesAndDecorate(selection, decoration));
    }

    public Action selectAndInsertText(Selection selection, String text) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.selectAndInsertText(selection, text));
    }

    public Action insertEmoji(Emoji emoji) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.insertEmoji(emoji));
    }

    public Action selectAndInsertEmoji(Selection selection, Emoji emoji) {
        return selectAndInsertEmoji(selection, emoji, false);
    }

    public Action selectAndInsertEmoji(Selection selection, Emoji emoji, boolean undoLast) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.selectAndInsertEmoji(selection, emoji, undoLast));
    }

    public Action insertBlock(Block block) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.insertBlock(block));
    }

    public Action selectAndInsertBlock(Selection selection, Block block) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.selectAndInsertBlock(selection, block));
    }

    public Action insertTable(TableDecoration tableDecoration) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.insertTable(tableDecoration));
    }

    public Action decorate(Decoration... decorations) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.decorate(decorations));
    }
}
