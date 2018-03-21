/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.service
import org.jetbrains.kotlin.fir.symbols.ConeSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirQualifierPart
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

interface FirQualifierResolver {
    fun resolveSymbolWithPrefix(parts: List<FirQualifierPart>, prefix: ClassId): ConeSymbol?

    fun resolveSymbol(parts: List<FirQualifierPart>): ConeSymbol?

    companion object {
        fun getInstance(session: FirSession): FirQualifierResolver = session.service()
    }
}

