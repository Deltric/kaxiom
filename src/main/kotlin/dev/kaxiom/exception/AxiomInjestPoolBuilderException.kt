package dev.kaxiom.exception

/**
 * Exception for when an AxiomInjestPoolBuilder fails to build
 * @param error the cause of the error
 */
class AxiomInjestPoolBuilderException(error: String) : Exception(error)