package com.mcmlr.blocks.api.plugin

interface Plugin<T> {

    fun isApplicable(data: T): Boolean

    fun execute(data: T)

}