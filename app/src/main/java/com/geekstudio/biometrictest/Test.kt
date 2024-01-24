package com.geekstudio.biometrictest

import java.util.Objects

/**
 * Test
 *
 * @constructor Create empty Test
 */
open class Test {

    /**
     * Test function2
     *
     */
    fun TestFunction2(){

    }

    /**
     * Test function3
     *
     */
    fun TestFunction3(){

    }

    /**
     * Test function4
     *
     */
    fun TestFunction4(){

    }

    /**
     * Test function5
     *
     */
    fun TestFunction5(){

    }

    /**
     * TODO
     *
     * @param T T 설명1
     * @param x x 설명1
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <T:Test> TestTemple(x: Any): T? {
        return Test() as? T
    }
}