package com.gluonhq.richtext.undo;

public abstract class AbstractCommand<T> {

    protected abstract void doUndo(T context);
    protected abstract void doRedo(T context);

    protected void storeContext(T context){}
    protected void restoreContext(T context){}

    public final void execute(T context) {
        storeContext(context);
        doRedo(context);
    }

    public final void undo(T context) {
        doUndo(context);
        restoreContext(context);
    }

    public final void redo(T context) {
        restoreContext(context);
        doRedo(context);
    }

}
