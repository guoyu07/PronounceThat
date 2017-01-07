package com.rafaelsteil.pronouncethat;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
	private TextToSpeech tts;

	@BindView(R.id.textField)
	MaterialEditText textField;

	@BindView(R.id.pronounceButton)
	Button pronounceButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		initTTS();
		textField.setShowClearButton(true);
	}

	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}

		super.onPause();
	}

	private void initTTS() {
		tts = new TextToSpeech(this, status -> {
			if (status != TextToSpeech.ERROR) {
				int langStatus = tts.setLanguage(Locale.US);

				if (langStatus == TextToSpeech.LANG_NOT_SUPPORTED || langStatus == TextToSpeech.LANG_MISSING_DATA) {
					// TODO
				}

				for (TextToSpeech.EngineInfo engine : tts.getEngines()) {
					Log.d("APP", "Engine => " + engine.name);
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					try {
						Set<Voice> voices = tts.getVoices();

						if (voices != null) {
							for (Voice voice : voices) {
								Log.d("APP", "Voice => " + voice.getName());
							}
						}
					}
					catch (Exception e) {
						Log.w("APP", "Looks like we cannot retrieve available voices. Ignoring it");
					}

					try {
						for (Locale language : tts.getAvailableLanguages()) {
							Log.d("APP", "Language => " + language.getDisplayName());
						}
					}
					catch (Exception e) {
						Log.w("APP", "Looks like we cannot retrieve available languages. Ignoring it");
					}
				}
			}
			else {
				Log.e("APP", "Error on TTS init. status=" + status);
			}
		});

		tts.setOnUtteranceProgressListener(createUtteranceListener());
	}

	private UtteranceProgressListener createUtteranceListener() {
		return new UtteranceProgressListener() {
			@Override
			public void onStart(String s) {
				Log.d("APP", "Pronunciation started");
				pronounceButton.setEnabled(false);
			}

			@Override
			public void onDone(String s) {
				Log.d("APP", "Pronunciation finished");
				pronounceButton.setEnabled(true);
			}

			@Override
			public void onError(String s) {
				Log.w("APP", "Pronunciation error");
				pronounceButton.setEnabled(true);
			}
		};
	}

	@OnClick(R.id.pronounceButton)
	void onPronounceButtonClicked() {
		String text = textField.getText().toString();

		if (text.trim().length() == 0 || tts.isSpeaking()) {
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
}
