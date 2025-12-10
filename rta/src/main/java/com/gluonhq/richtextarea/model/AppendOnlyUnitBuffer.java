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

import java.util.List;

/**
 *
 * Append-only buffer used as the PieceTable Addition Buffer
 */
public class AppendOnlyUnitBuffer extends UnitBuffer {

    private int[] unitLengths = new int[4096];

    @Override
    public void append(List<Unit> units) {
        for (Unit u: units) {
            append(u);
        }
    }

    @Override
    public void append(Unit unit) {
        int idx = unitList.size();
        int oldLength = idx == 0 ? 0 : unitLengths[idx-1];
        unitLengths[idx]= oldLength + unit.length();
        unitList.add(unit);
        dirty = true;
    }

    @Override
    public Unit getUnitWithRange(int start, int end) {
        int accum = 0;
        if (start < 0) return new TextUnit("");
        int maccum = 0;
        for (int i = 0; i < unitList.size(); i++) {
            int prev = i==0 ? 0 : unitLengths[i-1];
            if ((prev <= start) && (unitLengths[i] >= end)) {
               return unitList.get(i); 
            }
        }
        return new TextUnit("");
    }

    
    
}
