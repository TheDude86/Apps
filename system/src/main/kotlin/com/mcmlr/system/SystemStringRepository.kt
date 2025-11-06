package com.mcmlr.system

import com.mcmlr.system.products.data.StringRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemStringRepository @Inject constructor(): StringRepository() {
    init {
        loadStrings("system/en_US.json")
    }
}