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
import com.gluonhq.richtextarea.model.Document;

import java.util.Objects;

class PasteDocumentCmd extends AbstractEditCmd {

    private final Document content;

    public PasteDocumentCmd(Document content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        // Go through all decorations: inserting unit and decorating it
        content.getDecorations().forEach(dm -> {
            int caretPosition = viewModel.getCaretPosition();
            int initialLength = viewModel.getTextLength();
            // 1. insert unit
            String text = content.getText().substring(dm.getStart(), dm.getStart() + dm.getLength());
            viewModel.insert(text);
            // 1. decorate unit
            int addedLength = viewModel.getTextLength() - initialLength;
            Selection newSelection = new Selection(caretPosition, Math.min(caretPosition + addedLength, viewModel.getTextLength()));
            viewModel.setSelection(newSelection);
            viewModel.decorate(dm.getDecoration());
            // For now: ignore paragraph decoration
//            viewModel.decorate(dm.getParagraphDecoration());
            viewModel.setSelection(Selection.UNDEFINED);
            viewModel.setCaretPosition(newSelection.getEnd());
        });

    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);

        content.getDecorations().forEach(dm -> {
            // 1. remove unit
            viewModel.undo();
            // 2. delete decoration
            viewModel.undoDecoration();
        });
    }

    @Override
    public String toString() {
        return "PasteDocumentCmd[" + super.toString() + ", " + content + "]";
    }
}
