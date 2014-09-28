package com.example.fast.preferences;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.example.fast.R;

/**
 * 
 * Preferences activity launched when the preferences section is selected from the menu button.
 */
public class FastPreferences extends PreferenceActivity{

		@Override
		protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			
			/* Display the fragment as the main content */
			getFragmentManager().beginTransaction().replace(android.R.id.content, new FastPreferencesFragment()).commit();
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu){
			//Inflate the menu; this adds items to the action bar if it it is present
			return true;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		
			Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
            } else {
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, upIntent);
            }

            return true;
		}
		
		/**This fragment shows the preferences for Fast */
		public static class FastPreferencesFragment extends PreferenceFragment{
			@Override
			public void onCreate(Bundle savedInstanceState){
				super.onCreate(savedInstanceState);
				
				/* Make sure default values are applied */
				PreferenceManager.setDefaultValues(getActivity(), R.xml.fast_preferences, false);
				/*Load the preferences from an XML resource */
				addPreferencesFromResource(R.xml.fast_preferences);
				
				/* Sign out button */
				Preference button = (Preference)findPreference("signOut");
				button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				                @Override
				                public boolean onPreferenceClick(Preference arg0) { 
				                		//getActivity().getParentActivityIntent()
				                    return true;
				                }
				            });
				
			}
		}
}
