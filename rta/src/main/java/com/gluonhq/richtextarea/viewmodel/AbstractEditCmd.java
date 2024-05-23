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

import com.gluonhq.richtextarea.Selection;

import java.util.Objects;


/**
 * Abstract command add context store/restore operations
 * to already existing undo/redo framework
 */
abstract class AbstractEditCmd extends com.gluonhq.richtextarea.undo.AbstractCommand<RichTextAreaViewModel> {

    private int caretPosition;
    Selection selection;


    protected void storeContext( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel);
        this.caretPosition = viewModel.getCaretPosition();
        this.selection = viewModel.getSelection();
    }

    protected void restoreContext( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel);
        viewModel.setCaretPosition(caretPosition);
        viewModel.setSelection(selection);
    }

    @Override
    protected void attachContext(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel).attach();
    }

    @Override
    protected void detachContext(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel).detach();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " { " +
                "c=" + caretPosition +
                ", s=[" + selection.getStart() + "," + selection.getEnd() + "] " +
                "}";
    }
}
