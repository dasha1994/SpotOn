// SpotOn.java
// Activity for the SpotOn app
package com.example.s4astya.spoton;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class SpotOn extends Activity 
{
   private SpotOnView view; // displays and manages the game
   
   // called when this Activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      // create a new SpotOnView and add it to the RelativeLayout
      RelativeLayout layout = 
         (RelativeLayout) findViewById(R.id.relativeLayout);
      view = new SpotOnView(this, getPreferences(Context.MODE_PRIVATE), 
         layout); 
      layout.addView(view, 0); // add view to the layout
   } // end method onCreate
   
   // called when this Activity moves to the background
   @Override
   public void onPause()
   {
	   super.onPause();
	   view.pause(); // release resources held by the View
   } // end method onPause
   
   // called when this Activity is brought to the foreground
   @Override
   public void onResume()
   {
	   super.onResume();
	   view.resume(this); // re-initialize resources released in onPause
   } // end method onResume
} // end class SpotOn


