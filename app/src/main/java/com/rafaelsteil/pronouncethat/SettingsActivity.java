package com.rafaelsteil.pronouncethat;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setupWindowAnimations();
		setContentView(R.layout.settings_activity);
		configureToolbar();

		getFragmentManager()
			.beginTransaction()
			.replace(R.id.settingsContainer, new SettingsFragment())
			.commit();
	}

	private void setupWindowAnimations() {
		if (Build.VERSION.SDK_INT >= 21) {
			Fade fade = (Fade) TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
			getWindow().setEnterTransition(fade);
		}
	}

	private void configureToolbar() {
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle(R.string.settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return false;
	}

	public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
		private SharedPreferences prefs;
		private Map<String, String> unformatedSummaries = new HashMap<>();
		private String originalLanguage;

		@Override
		public void onCreate(Bundle savedInstance) {
			super.onCreate(savedInstance);
			addPreferencesFromResource(R.xml.preferences);
			saveOriginalLanguage();
		}

		private void saveOriginalLanguage() {
			originalLanguage = getPrefLanguage().getValue();

			if (originalLanguage == null || "English".equals(originalLanguage)) {
				originalLanguage = Locale.US.getDisplayLanguage();
			}
		}

		@Override
		public void onResume() {
			super.onResume();

			prefs = getPreferenceManager().getSharedPreferences();
			prefs.registerOnSharedPreferenceChangeListener(this);

			List<CharSequence> list = new ArrayList<>();

			for (Locale l : Locale.getAvailableLocales()) {
				list.add(l.getDisplayName());
			}

			ListPreference p = getPrefLanguage();
			p.setEntries(list.toArray(new CharSequence[0]));
			p.setEntryValues(list.toArray(new CharSequence[0]));

			restoreSummaries(getPreferenceScreen());
		}

		private ListPreference getPrefLanguage() {
			return (ListPreference)findPreference("PrefLanguage");
		}

		@Override
		public void onPause() {
			super.onPause();
			prefs.unregisterOnSharedPreferenceChangeListener(this);
		}

		private void restoreSummaries(Preference p) {
			if (p instanceof PreferenceGroup) {
				iterateOverPreferenceGroup((PreferenceGroup) p);
			}
			else if (p instanceof EditTextPreference) {
				setEditPreferenceSummary(p);
			}
		}

		private void iterateOverPreferenceGroup(PreferenceGroup pg) {
			int total = pg.getPreferenceCount();

			for (int i = 0; i < total; i++) {
				restoreSummaries(pg.getPreference(i));
			}
		}

		private void setEditPreferenceSummary(Preference p) {
			String text = ((EditTextPreference)p).getText();
			String summary;

			if (!unformatedSummaries.containsKey(p.getKey())) {
				summary = p.getSummary().toString();
				unformatedSummaries.put(p.getKey(), summary);
			}
			else {
				summary = unformatedSummaries.get(p.getKey());
			}

			p.setSummary(String.format(summary, text));
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
			restoreSummaries(getPreferenceScreen());
			checkSelectedLanguageAvailability();
		}

		private void checkSelectedLanguageAvailability() {
			String selectedLang = getPrefLanguage().getValue();
			TTSEngine engine = TTSEngine.instance();

			if (!(selectedLang.equals(originalLanguage)) && !engine.isLanguageAvailable(selectedLang)) {
				Dialogs.langNotAvailable(getActivity(), result -> {
					if (result) {
						engine.startTTSDataInstall(getActivity());
					}
					else {
						getPrefLanguage().setValue(originalLanguage);
					}
				});
			}
		}
	}
}
