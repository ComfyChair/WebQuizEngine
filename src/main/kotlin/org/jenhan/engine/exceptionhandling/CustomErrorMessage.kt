package org.jenhan.engine.exceptionhandling

/**
 * Data class representing a custom error message for API error responses.
 *
 * Used to provide structured error information to clients when exceptions occur,
 * allowing consistent error response formatting across the API.
 *
 * @property error Description of the error that occurred
 */
class CustomErrorMessage (val error: String)