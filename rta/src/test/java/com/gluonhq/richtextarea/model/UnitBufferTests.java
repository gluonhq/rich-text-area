/*
 * Copyright (c) 2023, Gluon
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
package com.gluonhq.richtextarea.model;

import com.gluonhq.richtextarea.Selection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class UnitBufferTests {

    private static final Document FACE_MODEL = new Document("One \ud83d\ude00 Text \ufeff@name\ufeff!");

    @Test
    @DisplayName("Unit: Original text is intact")
    public void originalTextIntact() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(FACE_MODEL.getText(), pt.getText());
    }

    @Test
    @DisplayName("Unit: internal length")
    public void internalLength() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(pt.getTextLength(), 13);
    }

    @Test
    @DisplayName("Unit: internal length from units")
    public void internalLengthFromUnits() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        AtomicInteger accum = new AtomicInteger();
        pt.walkFragments((u, d) -> accum.getAndAdd(u.length()), 0, 13);
        Assertions.assertEquals(accum.get(), 13);
    }

    @Test
    @DisplayName("Unit: walk units")
    public void walkUnits() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        StringBuilder sb = new StringBuilder();
        pt.walkFragments((u, d) -> sb.append(u).append(";"), 0, 13);
        Assertions.assertEquals(sb.toString(), "TU{'One '};EU{1F600};TU{' Text '};BU{'@name'};TU{'!'};");
    }

    @Test
    @DisplayName("Unit: internal text from units")
    public void internalTextFromUnits() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        StringBuilder internalSb = new StringBuilder();
        pt.walkFragments((u, d) -> internalSb.append(u.getInternalText()), 0, 13);
        Assertions.assertEquals(internalSb.toString(), "One \u2063 Text \ufffc!");
        Assertions.assertEquals(internalSb.toString().length(), 13);
    }

    @Test
    @DisplayName("Unit: external length")
    public void externalLength() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(pt.getText().length(), 20);
    }

    @Test
    @DisplayName("Unit: external text from units")
    public void externalTextFromUnits() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        StringBuilder externalSb = new StringBuilder();
        pt.walkFragments((u, d) -> externalSb.append(u.getText()), 0, 13);
        Assertions.assertEquals(externalSb.toString(), FACE_MODEL.getText());
        Assertions.assertEquals(externalSb.toString().length(), 20);
    }

    @Test
    @DisplayName("Unit: internal caret position")
    public void internalCaretPosition() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        int externalLength = pt.getText().length();
        Assertions.assertEquals(externalLength, 20);
        int internalCaret = pt.getInternalPosition(externalLength);
        Assertions.assertEquals(internalCaret, 13);
    }

    @Test
    @DisplayName("Unit: selection")
    public void selection() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        String text = "@na";
        int start = FACE_MODEL.getText().indexOf(text);

        Selection selection = new Selection(start, start + text.length());
        Selection internalSelection = pt.getInternalSelection(selection);
        Assertions.assertEquals(internalSelection.getStart(), "One \u2063 Text ".length());
        Assertions.assertEquals(internalSelection.getEnd(), "One \u2063 Text \ufffc".length());
    }

    @Test
    @DisplayName("Unit: insert unit")
    public void insertUnits() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        UnitBuffer originalText = pt.originalText;
        Assertions.assertEquals(5, originalText.getUnitList().size());
        Assertions.assertEquals("[TU{'One '}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(20, originalText.getText().length());
        Assertions.assertEquals(13, originalText.length());
        Assertions.assertEquals(pt.getTextLength(), originalText.length());
        originalText.insert(new TextUnit("?"), 0);
        Assertions.assertEquals("[TU{'?'}, TU{'One '}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(14, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.insert(new TextUnit("?"), 3);
        Assertions.assertEquals("[TU{'One'}, TU{'?'}, TU{' '}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(14, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.insert(new TextUnit("?"), 4);
        Assertions.assertEquals("[TU{'One '}, TU{'?'}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(14, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.insert(new TextUnit("?"), 5);
        Assertions.assertEquals("[TU{'One '}, EU{1F600}, TU{'?'}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(14, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.insert(new TextUnit("?"), 11);
        Assertions.assertEquals("[TU{'One '}, EU{1F600}, TU{' Text '}, TU{'?'}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(14, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.insert(new TextUnit("?"), 12);
        Assertions.assertEquals("[TU{'One '}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'?'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(14, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.insert(new TextUnit("?"), 13);
        Assertions.assertEquals("[TU{'One '}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'!'}, TU{'?'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(14, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.insert(new TextUnit("?"), 14);
        Assertions.assertEquals("[TU{'One '}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(13, originalText.length());
    }

    @Test
    @DisplayName("Unit: remove unit")
    public void removeUnits() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        UnitBuffer originalText = pt.originalText;
        Assertions.assertEquals(5, originalText.getUnitList().size());
        Assertions.assertEquals("[TU{'One '}, EU{1F600}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(20, originalText.getText().length());
        Assertions.assertEquals(13, originalText.length());
        Assertions.assertEquals(pt.getTextLength(), originalText.length());
        originalText.remove(3, 5);
        Assertions.assertEquals("[TU{'One'}, TU{' Text '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(11, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.remove(1, 8);
        Assertions.assertEquals("[TU{'O'}, TU{'xt '}, BU{'@name'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(6, originalText.length());
        originalText = new PieceTable(FACE_MODEL).originalText;
        originalText.remove(1, 12);
        Assertions.assertEquals("[TU{'O'}, TU{'!'}]", originalText.getUnitList().toString());
        Assertions.assertEquals(2, originalText.length());
    }
}
