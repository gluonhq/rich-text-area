/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.gluonhq.chat.impl.chatlistview.util;

/**
 * Some basic utilities which need to be in java (for shifting operations or
 * other reasons), which are not toolkit dependent.
 *
 */
public class Utils {

    private static final double FACTOR = 1_000_000_000;
    
    /***************************************************************************
     *                                                                         *
     * Math-related utilities                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static float clamp(float min, float value, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static int clamp(int min, int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static double clamp(double min, double value, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    // used for layout to adjust widths to honor the min/max policies consistently
    public static double boundedSize(double value, double min, double max) {
        // if max < value, return max
        // if min > value, return min
        // if min > max, return min
        return Math.min(Math.max(value, min), Math.max(min, max));
    }
    
    /**
     * Truncates a value to a given decimal.
     * It helps avoiding numerical precision errors when using snap methods
     * 
     * @param value
     * @return 
     */
    public static double truncate(double value) {
        return Math.floor(value * FACTOR) / FACTOR;
    }
}
