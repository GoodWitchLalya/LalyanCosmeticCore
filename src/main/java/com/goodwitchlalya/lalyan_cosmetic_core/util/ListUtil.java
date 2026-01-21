package com.goodwitchlalya.lalyan_cosmetic_core.util;

import java.util.ArrayList;
import java.util.List;

// Utility class for list operations.
public class ListUtil {
    // Converts an immutable list to a mutable ArrayList.
    // @param list The original list, which may be immutable.
    // @param <T> The type of elements in the list.
    // @return A new ArrayList containing all elements of the original list.
    public static<T> List<T> mutable(List<T> list) {
        return new ArrayList<>(list);
    }
}
