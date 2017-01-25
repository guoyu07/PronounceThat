package com.rafaelsteil.pronouncethat;

/**
 * Listener for all tts operations
 * */
public interface TTSEngineListener {

	/**
	 * TTS initialization failed in such a way that is not possible to do anything
	 */
	void onInitFailed();

	/**
	 * TTS is ready to use
	 */
	void onInitSuccess();

	/**
	 * Called just before speaking
	 */
	void onPronounceStart();

	/**
	 * Called after speaking has finished
	 */
	void onPronounceFinish();

	/**
	 * Called when a language pack is missing and requires download.
	 */
	void onLanguageDataRequired();

	/**
	 * Called just before language download will start
	 */
	void onWillDownloadLanguage();

	/**
	 * Called after language download has finished
	 */
	void onLanguageDownloadFinished();
}
