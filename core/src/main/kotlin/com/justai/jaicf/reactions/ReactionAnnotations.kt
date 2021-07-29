package com.justai.jaicf.reactions

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class PathValue()

@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class UsesReaction(val name: String)
