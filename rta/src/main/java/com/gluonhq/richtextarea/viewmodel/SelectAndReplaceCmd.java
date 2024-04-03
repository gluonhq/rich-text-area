/*
 * Copyright (c) 2023, Gluon
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
import com.gluonhq.richtextarea.model.BlockUnit;
import com.gluonhq.richtextarea.model.EmojiUnit;
import com.gluonhq.richtextarea.model.UnitBuffer;

import java.util.Objects;

class SelectAndReplaceCmd extends AbstractEditCmd {

    private final UnitBuffer content;
    private final Selection selection;
    private boolean undoLast;

    public SelectAndReplaceCmd(Selection selection, String content) {
        this.selection = selection;
        this.content = UnitBuffer.convertTextToUnits(content);
    }

    public SelectAndReplaceCmd(Selection selection, Emoji content, boolean undoLast) {
        this.selection = selection;
        this.content = new UnitBuffer(new EmojiUnit(content));
        this.undoLast = undoLast;
    }

    public SelectAndReplaceCmd(Selection selection, Block content) {
        this.selection = selection;
        this.content = new UnitBuffer(new BlockUnit(content));
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);

        if (undoLast) {
            // 0. undo adding last unit
            // This is needed to prevent an infinite loop if doUndo is called
            // (as inserting again that last unit triggers the call to doRedo)
            viewModel.undo();
        }

        // 1. select
        if (selection != null) {
            viewModel.setSelection(selection);
        }

        // 2. delete selection
        viewModel.remove(-1, 1);

        // 3. insert content
        if (content != null) {
            viewModel.insert(content.getText());
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        // 1. insert content
        Objects.requireNonNull(viewModel).undo();

        // 2. delete selection
        viewModel.undo();
    }

    @Override
    public String toString() {
        return "SelectAndReplaceCmd[" + super.toString() + ", " + content + ", " + selection + "]";
    }
}
