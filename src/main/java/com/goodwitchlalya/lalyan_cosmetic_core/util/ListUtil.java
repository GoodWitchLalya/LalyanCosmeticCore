package com.goodwitchlalya.lalyan_cosmetic_core.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    public static<T> List<T> mutable(List<T> list) {
        return new ArrayList<>(list);
    }
}
