package com.example.fast.share;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.fast.MainActivity;
import com.example.fast.R;
import com.example.fast.Statistics;
import com.google.android.gms.plus.PlusShare;

/**
 * 
 * Share screen that allows the user to share their running stats to either Google+ or Twitter.
 *
 */
public class ShareFragment extends Fragment{
     
		private Button googleShareButton;
		private Button twitterShareButton;
	
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	  
	        View rootView = inflater.inflate(R.layout.fragment_share, container, false);
	          
	        return rootView;
	    }
	    
	    
	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	         super.onActivityCreated(savedInstanceState);
	    
	         googleShareButton = (Button) getActivity().findViewById(R.id.google_share_button);
	         twitterShareButton = (Button) getActivity().findViewById(R.id.twitter_share_button);
	         
	 		/* Share to Google Plus! */
	         googleShareButton.setOnClickListener(new OnClickListener() {
	 			
	 			@Override
	 			public void onClick(View v) {
	 				
	 				/* Pull the stats to share */
	 				Statistics stat = ((MainActivity) getActivity()).getAllTimeStatistics();
	 				String units, distance, heartrate;
	 				if(((MainActivity) getActivity()).getUsingMetric()){
	 					units = "km";
	 					distance = MainActivity.format(Statistics.kilometers(stat.getDistanceTravelled()));
	 					heartrate = MainActivity.format(stat.getAverageHeartrate());
	 				}
	 				else{
	 					units = "miles";
	 					distance = MainActivity.format(Statistics.miles(stat.getDistanceTravelled()));
	 					heartrate = MainActivity.format(stat.getAverageHeartrate());
	 				}
	 					
	 				
	 			      Intent shareIntent = new PlusShare.Builder(getActivity())
	 			          .setType("text/plain")
	 			          .setText("Using Fast, I've ran a total of " + distance + " " + units + " so far with an average heartrate of " + heartrate + " bpm!")
	 			          .setContentUrl(Uri.parse("https://developers.google.com/+/"))
	 			          .getIntent();
	 
	 			      startActivityForResult(shareIntent, 0);
	 			}
	 		});
	         
	        twitterShareButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					String token ="42538523-riSxpubu0dpLSbEClo1HxSJ5BmAcYT2jKKPvcpBzV"; 
		            String secret = "4c6WtS0016qze9o45uTiYppXZ3LC7nXx8iuz0bHYdxHoc";
		            
		            ConfigurationBuilder cb = new ConfigurationBuilder();
		            cb.setDebugEnabled(true)
		                .setOAuthConsumerKey("FUERun5hcYzglM8gg6bLig")
		                .setOAuthConsumerSecret("m8fc5qpbGP7p7ZmsIztm9Y1vujb54JUzQLaUgE2Q")
		                .setOAuthAccessToken(token)
		                .setOAuthAccessTokenSecret(secret);

		            	TwitterFactory factory = new TwitterFactory(cb.build());
		            	Twitter twitter = factory.getInstance();
		            	
		            	TwitterPostTask task = new TwitterPostTask();
		            	task.execute(twitter);
		            	
		            	Toast.makeText(getActivity(), "Sucessfully posted your data to Twitter!", Toast.LENGTH_LONG).show();
				}
			}); 
	    }
	    
	    private class TwitterPostTask extends AsyncTask<Twitter, Integer, Boolean> {

	    	ProgressDialog dialog;
	    	
	    	public TwitterPostTask(){
	    		dialog = new ProgressDialog(getActivity());
	    	}
	    	
			@Override
			protected Boolean doInBackground(Twitter... params) {
					
				/* Pull the stats to share */
 				Statistics stat = ((MainActivity) getActivity()).getAllTimeStatistics();
 				String units, distance, heartrate;
 				if(((MainActivity) getActivity()).getUsingMetric()){
 					units = "km";
 					distance = MainActivity.format(Statistics.kilometers(stat.getDistanceTravelled()));
 					heartrate = MainActivity.format(stat.getAverageHeartrate());
 				}
 				else{
 					units = "miles";
 					distance = MainActivity.format(Statistics.miles(stat.getDistanceTravelled()));
 					heartrate = MainActivity.format(stat.getAverageHeartrate());
 				}
				
					try {
						params[0].updateStatus("Using Fast, I've ran a total of " + distance + " " + units + " so far with an average heartrate of " + heartrate + " bpm!");
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				return true;
			}
			
			protected void onPreExecute() {
		        this.dialog.setMessage("Posting to Twitter...");
		        this.dialog.show();
		    }

		    @Override
		    protected void onPostExecute(final Boolean success) {
		        if (dialog.isShowing()) {
		            dialog.dismiss();
		        }
	    }
	}    
}

