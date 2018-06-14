package com.gemini.energy

import io.reactivex.Observable
import junit.framework.Assert
import org.junit.Test

class RxJavaUnitTest {
    var result = ""

    @Test
    public fun returnAValue() {
        result = ""
        var observer: Observable<String> = Observable.just("Hello")
        observer.subscribe { s -> result=s }
        Assert.assertTrue(result.equals("Hello"))
    }
}