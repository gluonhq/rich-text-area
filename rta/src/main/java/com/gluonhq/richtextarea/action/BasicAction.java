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

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Skin;

import java.util.Objects;
import java.util.function.Function;

class BasicAction implements Action {

    private RichTextAreaViewModel viewModel;
    private ActionCmd actionCmd;

    private final RichTextArea control;
    private final Function<Action, ActionCmd> actionCmdFunction;

    private final BooleanProperty disabledImplProperty = new SimpleBooleanProperty(this, "disabledImpl", false);

    public BasicAction(RichTextArea control, Function<Action, ActionCmd> actionCmdFunction) {
        this.control = control;
        this.actionCmdFunction = Objects.requireNonNull(actionCmdFunction);
        if (control.getSkin() != null) {
            initialize(control.getSkin());
        } else {
            control.skinProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (control.getSkin() != null) {
                        initialize(control.getSkin());
                        control.skinProperty().removeListener(this);
                    }
                }
            });
        }
    }

    private void initialize(Skin<?> skin) {
        if (!(skin instanceof RichTextAreaSkin)) {
            return;
        }
        viewModel = ((RichTextAreaSkin) skin).getViewModel();
        BooleanBinding binding = getActionCmd().getDisabledBinding(viewModel);
        if (binding != null) {
            disabledImplProperty.bind(binding);
        }
    }

    private ActionCmd getActionCmd() {
        if (actionCmd == null) {
            actionCmd = actionCmdFunction.apply(this);
        }
        return actionCmd;
    }

    @Override
    public void execute(ActionEvent event) {
        if (viewModel != null) {
            Platform.runLater(() -> getActionCmd().apply(viewModel));
        } else {
            control.skinProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (control.getSkin() != null) {
                        if (viewModel != null) {
                            Platform.runLater(() -> getActionCmd().apply(viewModel));
                        }
                        control.skinProperty().removeListener(this);
                    }
                }
            });
        }
    }

    @Override
    public ReadOnlyBooleanProperty disabledProperty() {
        return disabledImplProperty;
    }
}

