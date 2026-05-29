package com.project.common.usecase;

/**
 * O: Output (İşlem sonucunda dönülecek nesne tipi)
 * I: Input (İşleme girecek olan input nesne tipi)
 */
public interface UseCaseHandler<O, I> {

    O handle(I input);

}