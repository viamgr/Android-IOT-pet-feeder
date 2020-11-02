package com.viam.feeder.core.task

class CompositeException(val errors: List<Exception>) : Exception()