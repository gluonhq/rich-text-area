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
 */
package com.gluonhq.richtextarea.model;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Append-only buffer used as the PieceTable Addition Buffer
 */
public class AppendOnlyUnitBuffer extends UnitBuffer {

    int[] unitLengths = new int[4096];

    @Override
    public void append(List<Unit> units) {
        ensureCapacity(unitList.size() + units.size());
        for (Unit u: units) {
            doAppend(u);
        }
    }

    @Override
    public void append(Unit unit) {
        ensureCapacity(unitList.size() + 1);
        doAppend(unit);
    }

    private void doAppend(Unit unit) {
        int idx = unitList.size();
        int oldLength = idx == 0 ? 0 : unitLengths[idx-1];
        unitLengths[idx]= oldLength + unit.length();
        unitList.add(unit);
        dirty = true;
    }

    @Override
    public void insert(Unit unit, int position) {
        throw new UnsupportedOperationException("Do not insert in an append-only buffer");
    }

    @Override
    public void remove(int start, int end) {
        throw new UnsupportedOperationException("Do not remove from an append-only buffer");
    }

    @Override
    public Unit getUnitWithRange(int start, int end) {
        if (start < 0 || unitList.isEmpty()) return new TextUnit("");
        int index = Arrays.binarySearch(unitLengths, 0, unitList.size(), start);
        if (index >=0) { // exact start at index
            index = index+1;
        } else { // no exact item found
            index = -(index) -1;
        }
        return unitList.get(index); 
    }

    @Override
    public int length() {
        return unitList.isEmpty() ? 0 : unitLengths[unitList.size() - 1];
    }

    private void ensureCapacity(int required) {
        if (required > unitLengths.length) {
            int newCapacity = Math.max(unitLengths.length * 2, required);
            unitLengths = Arrays.copyOf(unitLengths, newCapacity);
        }
    }

}
