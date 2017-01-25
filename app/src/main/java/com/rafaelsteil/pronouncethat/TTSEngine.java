package com.rafaelsteil.pronouncethat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.UUID;

/**
 * TTS workhorse
 */
public class TTSEngine {
	private TextToSpeech tts;
	private Context context;
	private final TTSEngineListener listener;
	private Locale locale;
	private boolean isWorking;
	private static TTSEngine instance;

	public TTSEngine(Context context, TTSEngineListener listener) {
		this.context = context;
		this.listener = listener;
		tts = new TextToSpeech(context, this::onTtsInitListener);
	}

	public void setLanguage(Locale locale) {
		this.locale = locale;
	}

	public static TTSEngine instance() {
		return instance;
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
			configureTtsSettings();
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

	private void configureTtsSettings() {
		int langStatus = tts.isLanguageAvailable(locale);

		if (isLangAvailableSuccess(langStatus)) {
			tts.setLanguage(locale);
			tts.setOnUtteranceProgressListener(createUtteranceListener());
			isWorking = true;
			listener.onInitSuccess();
		}
		else if (langStatus == TextToSpeech.LANG_MISSING_DATA) {
			listener.onLanguageDataRequired(this::startLanguageDataDownload);
		}
		else if (langStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
			isWorking = false;
			listener.onInitFailed();
		}
	}

	private void startLanguageDataDownload(Boolean shouldDownload) {
		if (!shouldDownload) {
			return;
		}

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
}
