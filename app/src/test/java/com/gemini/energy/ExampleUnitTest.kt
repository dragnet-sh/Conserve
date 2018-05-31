package com.gemini.energy

import org.junit.Test
import io.reactivex.Observable
import junit.framework.Assert.assertTrue


public class RxJavaUnitTest {
    var result = ""

    @Test
    public fun returnAValue() {
        result = ""
        var observer: Observable<String> = Observable.just("Hello")
        observer.subscribe { s -> result=s }
        assertTrue(result.equals("Hello"))
    }






}


