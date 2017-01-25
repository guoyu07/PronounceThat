package com.rafaelsteil.pronouncethat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * TTS workhorse
 */
public class TTSEngine {
	private TextToSpeech tts;
	private final TTSEngineListener listener;
	private boolean isWorking;
	private static TTSEngine instance;

	public TTSEngine(Context context, TTSEngineListener listener) {
		this.listener = listener;
		tts = new TextToSpeech(context, this::onTtsInitListener);
		instance = this;
	}

	public static TTSEngine instance() {
		return instance;
	}

	/**
	 * Changes the current language. It will check if the necessary data
	 * is available, and call the proper callbacks depending on the result
	 */
	public void setLanguage(Locale locale) {
		if (locale == null || !isWorking()) {
			return;
		}

		int langStatus = tts.isLanguageAvailable(locale);

		if (isLangAvailableSuccess(langStatus)) {
			tts.setLanguage(locale);
			isWorking = true;
		}
		else if (langStatus == TextToSpeech.LANG_MISSING_DATA) {
			listener.onLanguageDataRequired();
			isWorking = false;
		}
		else if (langStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
			listener.onInitFailed();
			isWorking = false;
		}
	}

	/**
	 * Sets the language based on its name
	 * @param displayName The language's name, the same value of {@link Locale#getDisplayName()}
	 */
	public void setLanguage(String displayName) {
		setLanguage(findLocaleByDisplayName(displayName));
	}

	@Nullable
	private Locale findLocaleByDisplayName(String displayName) {
		for (Locale locale : Locale.getAvailableLocales()) {
			if (locale.getDisplayName().equals(displayName)) {
				return locale;
			}
		}

		return null;
	}

	/**
	 * Shutdown the tts service. It will not be possible to use it anymore
	 */
	public void shutdown() {
		if (tts != null) {
			tts.shutdown();
		}
	}

	/**
	 * @return true if tts has been intialized and is good to use
	 */
	public boolean isWorking() {
		return isWorking;
	}

	/**
	 * Say something. It is ignored it currently speaking or if blank text
	 * @param text what to pronounce
	 */
	public void speak(String text) {
		if (text.trim().length() == 0 || tts.isSpeaking() || !isWorking()) {
			return;
		}

		String utteranceId = UUID.randomUUID().toString();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
		}
		else {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	private void onTtsInitListener(int status) {
		if (status == TextToSpeech.SUCCESS) {
			isWorking = true;
			tts.setOnUtteranceProgressListener(createUtteranceListener());
			listener.onInitSuccess();
		}
		else {
			listener.onInitFailed();
		}
	}

	private UtteranceProgressListener createUtteranceListener() {
		return new UtteranceProgressListener() {
			@Override
			public void onStart(String s) {
				listener.onPronounceStart();
			}

			@Override
			public void onDone(String s) {
				listener.onPronounceFinish();
			}

			@Override
			public void onError(String s) {
				listener.onPronounceFinish();
			}
		};
	}

	public void startTTSDataInstall(Context context) {
		Intent intent = new Intent();
		intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		context.startActivity(intent);
	}

	/**
	 * Checks if the given status of language availability is a success status
	 * @return true if the language is available on the user's device
	 */
	private boolean isLangAvailableSuccess(int status) {
		return status == TextToSpeech.LANG_AVAILABLE
				|| status == TextToSpeech.LANG_COUNTRY_AVAILABLE
				|| status == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
	}

	/**
	 * Checks if a given language is installed
	 * @param displayName the language name, as returned by {@link Locale#getDisplayName()}s
	 * @return true if the language is installed in the selected tts engine
	 */
	public boolean isLanguageAvailable(String displayName) {
		Locale locale = findLocaleByDisplayName(displayName);
		return locale != null && isLangAvailableSuccess(tts.isLanguageAvailable(locale));
	}
}
