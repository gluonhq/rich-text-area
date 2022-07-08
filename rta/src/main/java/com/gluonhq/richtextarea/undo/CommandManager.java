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
package com.gluonhq.richtextarea.undo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandManager<T> {

    public static final Logger LOGGER = Logger.getLogger(CommandManager.class.getName());

    final Deque<AbstractCommand<T>> undoStack = new ArrayDeque<>();
    final Deque<AbstractCommand<T>> redoStack = new ArrayDeque<>();
    final T context;
    private final Runnable runnable;

    public CommandManager(T context) {
        this(context, null);
    }

    public CommandManager(T context, Runnable runnable) {
        this.context = context;
        this.runnable = runnable;
    }

    public void execute(AbstractCommand<T> cmd) {
        Objects.requireNonNull(cmd).execute(context);
        undoStack.push(cmd);
        redoStack.clear();
        end();
        LOGGER.log(Level.FINE, "Execute: " + this);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            var cmd = undoStack.pop();
            cmd.undo(context);
            redoStack.push(cmd);
            end();
            LOGGER.log(Level.FINE, "Undo: " + this);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            var cmd = redoStack.pop();
            cmd.redo(context);
            undoStack.push(cmd);
            end();
            LOGGER.log(Level.FINE, "Redo: " + this);
        }
    }

    public int getUndoStackSize() {
        return undoStack.size();
    }

    public int getRedoStackSize() {
        return redoStack.size();
    }

    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }

    private void end() {
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    public String toString() {
        return "CommandManager<" + context.getClass().getSimpleName() + ">{\n" +
                " - undoStack=" + undoStack +
                "\n - redoStack=" + redoStack +
                "\n}";
    }
}
