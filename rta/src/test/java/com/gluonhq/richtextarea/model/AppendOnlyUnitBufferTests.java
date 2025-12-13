/*
 * Copyright (C) 2025 Gluon
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author johan
 */
public class AppendOnlyUnitBufferTests {

    @Test
    public void testCapacity() {
        final AppendOnlyUnitBuffer additionBuffer = new AppendOnlyUnitBuffer();
        int len = additionBuffer.unitLengths.length;
        Assertions.assertTrue(len > 0);
        int belowMax = (int)(len*.9);
        for (int i = 0; i < belowMax; i++) {
            TextUnit tu = new TextUnit("a");
            additionBuffer.append(tu);
        }
        int len2 = additionBuffer.unitLengths.length;
        Assertions.assertEquals(len, len2);
        int aboveMax = (int)(len*1.1);
        for (int i = belowMax; i < aboveMax; i++) {
            TextUnit tu = new TextUnit("a");
            additionBuffer.append(tu);
        }
        int len3 = additionBuffer.unitLengths.length;
        Assertions.assertNotEquals(len, len3);
    }
}
