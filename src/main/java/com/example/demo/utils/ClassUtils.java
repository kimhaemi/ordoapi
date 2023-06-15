package com.example.demo.utils;

import java.util.Optional;

public class  ClassUtils {
    // 캐스팅을 안전하게 하기 위해서
    public static <T> Optional<T> getSafeCastInstance(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? Optional.of(clazz.cast(o)) : Optional.empty();
    }

    // for test
    public static <T> T getTest(Object o, Class<T> clazz) {
        return clazz.cast(o);
    }
}
