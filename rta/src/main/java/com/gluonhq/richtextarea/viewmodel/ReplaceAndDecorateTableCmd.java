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
package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;

import java.util.Objects;

class ReplaceAndDecorateTableCmd extends AbstractEditCmd {

    private final int caretOffset;
    private final int length;
    private final String content;
    private final Decoration decoration;

    public ReplaceAndDecorateTableCmd(int caretOffset, int length, String content, Decoration decoration) {
        this.caretOffset = caretOffset;
        this.length = length;
        this.content = content;
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        // 1. Remove
        Objects.requireNonNull(viewModel);
        viewModel.remove(caretOffset, length);

        // 2. Insert
        int caretPosition = viewModel.getCaretPosition();
        if (!content.isEmpty()) {
            viewModel.insert(content);
        }

        // move caret back to paragraph with table
        viewModel.setCaretPosition(caretPosition + (content.length() > 1 && content.startsWith("\n") ? 1 : 0));

        // 3. Decorate
        if (decoration instanceof ParagraphDecoration) {
            Objects.requireNonNull(viewModel).decorate(decoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        // 1. Decorate
        if (decoration instanceof ParagraphDecoration) {
            Objects.requireNonNull(viewModel).undoDecoration();
        }
        // 2. Insert
        if (!content.isEmpty()) {
            viewModel.undo();
        }
        // 3- Remove
        viewModel.undo();
    }

    @Override
    public String toString() {
        return "ReplaceAndDecorateTableCmd[" + super.toString() + ", Remove <" + caretOffset + ", " + length + "]> " +
                ", Insert: <" + (content != null ? content.replace("\n", "<n>") : "") + "]>" +
                " <Decorate: [" + decoration + "]>";
    }
}
