package com.gluonhq.emoji.impl.skin;

import com.gluonhq.emoji.Emoji;
import javafx.scene.Node;

import java.util.Optional;

public interface LinkedEmoji {

    boolean isReal();
    
    Optional<Emoji> getEmoji();

    Node createNode();
}
