package com.gluonhq.richtext;

import com.gluonhq.richtext.viewmodel.RichTextAreaViewModel;

public interface Action {
    void apply(RichTextAreaViewModel viewModel);
}
