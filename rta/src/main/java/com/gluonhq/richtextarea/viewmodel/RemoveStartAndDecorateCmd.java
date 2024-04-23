/*
 * Copyright (c) 2024, Gluon
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

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.TextDecoration;

import java.util.Objects;

class RemoveStartAndDecorateCmd extends AbstractEditCmd {

    private final Selection selection;
    private final Decoration decoration;
    private Decoration prevDecoration;

    public RemoveStartAndDecorateCmd(Selection selection, Decoration decoration) {
        this.selection = Objects.requireNonNull(selection);
        this.decoration = Objects.requireNonNull(decoration);
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        prevDecoration = viewModel.getDecorationAtCaret();
        int prevCaret = viewModel.getCaretPosition();

        // 1. select start of selection, delete it, move back caret, select remaining content again
        viewModel.setSelection(new Selection(selection.getStart(), selection.getStart() + 1));
        viewModel.remove(-1, 1);
        viewModel.setCaretPosition(prevCaret - 1);
        viewModel.setSelection(new Selection(selection.getStart(), selection.getEnd() - 1));

        // 2. apply decoration
        viewModel.decorate(decoration);

        // unselect and apply previous decoration after selection
        viewModel.setSelection(Selection.UNDEFINED);
        if (prevDecoration != null && prevDecoration instanceof TextDecoration) {
            viewModel.setDecorationAtCaret(prevDecoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        // 1. reset decoration
        if (prevDecoration != null && prevDecoration instanceof TextDecoration) {
            viewModel.setDecorationAtCaret(prevDecoration);
        }

        // 2. apply decoration
        viewModel.undo();

        // 3. remove single selection
        viewModel.undo();
    }

    @Override
    public String toString() {
        return "RemoveStartAndDecorateCmd[" + super.toString() + ", " + selection+ ", " + decoration + "]";
    }
}
