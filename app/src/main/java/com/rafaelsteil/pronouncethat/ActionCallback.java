package com.rafaelsteil.pronouncethat;

/**
 * General type to be used as callback
 * @param <T>
 */
public interface ActionCallback<T> {
	void result(T result);
}
