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
package com.gluonhq.richtextarea.action;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ParagraphDecorateAction<T> extends DecorateAction<T> {

    private ChangeListener<T> valuePropertyChangeListener;
    private ChangeListener<ParagraphDecoration> decorationChangeListener;
    private final Function<ParagraphDecoration, T> function;
    private final BiFunction<ParagraphDecoration.Builder, T, ParagraphDecoration> builderTFunction;

    public ParagraphDecorateAction(RichTextArea control,
                                   ObjectProperty<T> valueProperty, Function<ParagraphDecoration, T> valueFunction,
                                   BiFunction<ParagraphDecoration.Builder, T, ParagraphDecoration> builderTFunction) {
        super(control, valueProperty);
        this.function = valueFunction;
        this.builderTFunction = builderTFunction;
    }

    @Override
    protected void bind() {
        valuePropertyChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                ParagraphDecoration.Builder builder = ParagraphDecoration.builder();
                if (!viewModel.getSelection().isDefined() && viewModel.getDecorationAtParagraph() != null) {
                    builder = builder.fromDecoration(viewModel.getDecorationAtParagraph());
                }
                ParagraphDecoration newParagraphDecoration = builderTFunction.apply(builder, nv);
                Platform.runLater(() ->  {
                    ACTION_CMD_FACTORY.decorate(newParagraphDecoration).apply(viewModel);
                    control.requestFocus();
                });
                updating = false;
            }
        };
        valueProperty.addListener(valuePropertyChangeListener);
        decorationChangeListener = (obs, ov, nv) -> {
            if (!updating && !nv.equals(ov)) {
                T newValue = function.apply(nv);
                if (newValue != null && (ov == null || !newValue.equals(function.apply(ov)))) {
                    updating = true;
                    valueProperty.set(newValue);
                    updating = false;
                }
            }
        };
        viewModel.decorationAtParagraphProperty().addListener(decorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(valuePropertyChangeListener);
        viewModel.decorationAtParagraphProperty().removeListener(decorationChangeListener);
    }
}
