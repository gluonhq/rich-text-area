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
