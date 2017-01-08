package com.rafaelsteil.pronouncethat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.widget.Button;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rafaelsteil.pronouncethat.R.id.settingsButton;

public class MainActivity extends AppCompatActivity {
	private TextToSpeech tts;

	@BindView(R.id.textField)
	MaterialEditText textField;

	@BindView(R.id.pronounceButton)
	Button pronounceButton;

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupWindowAnimations();
		ButterKnife.bind(this);
		configureToolbar();
		initTTS();
		textField.setShowClearButton(true);
	}

	private void setupWindowAnimations() {
		if (Build.VERSION.SDK_INT >= 21) {
			Fade e = new Fade();
			e.setDuration(1000);
			getWindow().setExitTransition(e);
		}
	}

	private void configureToolbar() {
		setSupportActionBar(toolbar);
		toolbar.setTitle(R.string.app_name);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String hint = getResources().getString(R.string.word_field_hint);
		String lang = PreferenceManager.getDefaultSharedPreferences(this).getString("PrefLanguage", "english").toLowerCase();
		hint = String.format(hint, lang);
		textField.setHint(hint);
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

	@OnClick(settingsButton)
	void onSettingsButtonClicked() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
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
