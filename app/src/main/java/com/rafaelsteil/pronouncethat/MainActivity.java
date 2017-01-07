package com.rafaelsteil.pronouncethat;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
	private TextToSpeech tts;

	@BindView(R.id.textField)
	EditText textField;

	@BindView(R.id.pronounceButton)
	Button pronounceButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		initTTS();
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
				tts.setLanguage(Locale.US);
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

		String utteranceId = String.valueOf(System.currentTimeMillis());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
		}
		else {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
