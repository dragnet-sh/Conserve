package com.gemini.energy

import io.reactivex.*
import junit.framework.Assert
import org.junit.Test

class RXJavaTest {
    var result = ""

    @Test
    fun returnAValue() {
        result = ""
        var observer: Observable<String> = Observable.just("Hello")
        observer.subscribe { s -> result = s }
        Assert.assertTrue(result.equals("Hello"))
    }


    @Test
    fun flowableTest() {
        val flowable = Flowable.create<Int>({ emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)

        flowable.subscribe {
            println(it)
        }

    }

}