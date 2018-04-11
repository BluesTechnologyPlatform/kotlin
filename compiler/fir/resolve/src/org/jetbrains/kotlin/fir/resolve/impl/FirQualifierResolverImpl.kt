/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.FirProvider
import org.jetbrains.kotlin.fir.resolve.FirQualifierResolver
import org.jetbrains.kotlin.fir.symbols.ConeSymbol
import org.jetbrains.kotlin.fir.symbols.FirSymbolOwner
import org.jetbrains.kotlin.fir.types.FirQualifierPart
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

class FirQualifierResolverImpl(val session: FirSession) : FirQualifierResolver {

    override fun resolveSymbolWithPrefix(parts: List<FirQualifierPart>, prefix: ClassId): ConeSymbol? {
        val firProvider = FirProvider.getInstance(session)

        val fqName = ClassId(
            prefix.packageFqName,
            parts.drop(1).fold(prefix.relativeClassName) { prefix, suffix -> prefix.child(suffix.name) },
            false
        )
        val classifier = firProvider.getFirClassifierByFqName(fqName) ?: return null

        return (classifier as? FirSymbolOwner<*>)?.symbol
    }

    override fun resolveSymbol(parts: List<FirQualifierPart>): ConeSymbol? {
        val firProvider = FirProvider.getInstance(session)

        if (parts.isNotEmpty()) {
            val lastPart = mutableListOf<FirQualifierPart>()
            val firstPart = parts.toMutableList()

            while (firstPart.isNotEmpty()) {
                lastPart.add(0, firstPart.last())
                firstPart.removeAt(firstPart.lastIndex)

                val fqName = ClassId(firstPart.toFqName(), lastPart.toFqName(), false)
                val foundClassifier = firProvider.getFirClassifierByFqName(fqName)
                if (foundClassifier != null) {
                    return (foundClassifier as? FirSymbolOwner<*>)?.symbol
                }
            }
        }
        return null
    }

    private fun List<FirQualifierPart>.toFqNameUnsafe() = toFqName().toUnsafe()
    private fun List<FirQualifierPart>.toFqName() = fold(FqName.ROOT) { a, b -> a.child(b.name) }
}