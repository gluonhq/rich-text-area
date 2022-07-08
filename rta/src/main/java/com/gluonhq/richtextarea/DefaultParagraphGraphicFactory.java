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
package com.gluonhq.richtextarea;

import com.gluonhq.richtextarea.model.ParagraphDecoration;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.function.BiFunction;

public class DefaultParagraphGraphicFactory {

    public static BiFunction<Integer, ParagraphDecoration.GraphicType, Node> getFactory() {
        return (indent, type) -> {
            if (type == null) {
                return null;
            }
            switch (type) {
                case NUMBERED_LIST:
                    Label label = new Label("#.");
                    label.getStyleClass().add("numbered-list-label");
                    return label;
                case BULLETED_LIST:
                    if (indent == 0) {
                        return null;
                    }
                    switch ((indent - 1) % 4 + 1) {
                        case 1:
                            Circle circle1 = new Circle(2);
                            circle1.getStyleClass().add("bulleted-list-shape-1");
                            return circle1;
                        case 2:
                            Circle circle2 = new Circle(2);
                            circle2.getStyleClass().add("bulleted-list-shape-2");
                            return circle2;
                        case 3:
                            Rectangle rectangle3 = new Rectangle(4, 4);
                            rectangle3.getStyleClass().add("bulleted-list-shape-3");
                            return rectangle3;
                        case 4:
                            Rectangle rectangle4 = new Rectangle(4, 4);
                            rectangle4.getStyleClass().add("bulleted-list-shape-4");
                            return rectangle4;
                    }
                default:
                    return null;
            }
        };
    }
}
