/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.descriptors.FirTotalResolveTransformer
import org.jetbrains.kotlin.fir.builder.RawFirBuilder
import org.jetbrains.kotlin.fir.resolve.FirProvider
import org.jetbrains.kotlin.fir.resolve.FirQualifierResolver
import org.jetbrains.kotlin.fir.resolve.FirTypeResolver
import org.jetbrains.kotlin.fir.resolve.impl.FirProviderImpl
import org.jetbrains.kotlin.fir.resolve.impl.FirQualifierResolverImpl
import org.jetbrains.kotlin.fir.resolve.impl.FirTypeResolverImpl
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.KotlinTestWithEnvironment
import java.io.File

abstract class AbstractFirResolveTestCase : KotlinTestWithEnvironment() {
    override fun createEnvironment(): KotlinCoreEnvironment {
        return createEnvironmentWithMockJdk(ConfigurationKind.JDK_NO_RUNTIME)
    }

    fun doTest(path: String) {
        val file = File(path)

        val allFiles = listOf(file) + file.parentFile.listFiles { sibling ->
            sibling.name.removePrefix(file.nameWithoutExtension).removeSuffix(file.extension).matches("\\.[0-9]+\\.".toRegex())
        }


        val ktFiles = allFiles.map {
            val text = KotlinTestUtils.doLoadFile(it)
            KotlinTestUtils.createFile(it.name, text, project)

        }

        val session = object : FirSessionBase() {
            init {
                registerComponent(FirProvider::class, FirProviderImpl(this))
                registerComponent(FirQualifierResolver::class, FirQualifierResolverImpl(this))
                registerComponent(FirTypeResolver::class, FirTypeResolverImpl())
            }
        }

        val builder = RawFirBuilder(session)

        val transformer = FirTotalResolveTransformer()
        val firFiles = ktFiles.map {
            val firFile = builder.buildFirFile(it)
            (session.service<FirProvider>() as FirProviderImpl).recordFile(firFile)
            firFile
        }.also {
            transformer.processFiles(it)
        }

        val firFileDump = StringBuilder().also { firFiles.first().accept(FirRenderer(it), null) }.toString()
        val expectedPath = path.replace(".kt", ".txt")
        KotlinTestUtils.assertEqualsToFile(File(expectedPath), firFileDump)
    }
}