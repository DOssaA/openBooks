package com.darioossa.openbooks

import com.darioossa.openbooks.di.dataModule
import com.darioossa.openbooks.di.domainModule
import com.darioossa.openbooks.di.platformModule
import com.darioossa.openbooks.di.presentationModule
import org.koin.test.verify.verify
import kotlin.test.Test

class SharedCommonTest {
    @Test
    fun checkAllModules() {
        // Throws MissingKoinDefinitionException if definitions are incomplete
        presentationModule.verify()
        dataModule.verify()
        domainModule.verify()
        platformModule.verify()
    }
}
