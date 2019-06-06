package com.gluonhq.emoji.impl.skin;

import org.fxmisc.richtext.model.NodeSegmentOpsBase;


public class LinkedEmojiOps<S> extends NodeSegmentOpsBase<LinkedEmoji, S> {

    public LinkedEmojiOps() {
        super(new EmptyLinkedEmoji());
    }

    @Override
    public int length(LinkedEmoji emoji) {
        return emoji.isReal() ? 1 : 0;
    }

    @Override
    public String realGetText(LinkedEmoji linkedEmoji) {
        return linkedEmoji.isReal() ? linkedEmoji.getEmoji().get().getCodeName() : "";
    }

}
