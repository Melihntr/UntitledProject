package com.project.common.usecase;

/**
 * A generic interface representing a distinct business use case in the application.
 * This contract enforces the Single Responsibility Principle by ensuring that 
 * each specific business action is encapsulated within its own dedicated handler.
 *
 * @param <O> The Output type representing the result returned after the use case execution.
 * @param <I> The Input type containing the data required to execute the business logic.
 */
public interface UseCaseHandler<O, I> {

    /**
     * Executes the core business logic for this specific use case.
     *
     * @param input The input payload required for the operation.
     * @return The resulting output after processing the business rules.
     */
    O handle(I input);

}