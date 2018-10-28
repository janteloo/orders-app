package com.geocom.orders.util;

import com.geocom.orders.exception.ResourceNotFoundException;

import java.util.Optional;

public class ApplicationUtil {

    public static <T> void checkIfPresent(Optional<T> object, String message) {
        if(!object.isPresent()) {
            throw new ResourceNotFoundException(message);
        }
    }
}
