// SpotOnView.java
// View that displays and manages the game
package com.example.s4astya.spoton;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpotOnView extends View 
{
   private static final String HIGH_SCORE = "HIGH_SCORE";
   private SharedPreferences preferences;

   private int spotsTouched;
   private int level;
   private int score;
   private int highScore;
   private int viewHeight;
   private int viewWidth;
   private long animationTime;

   private boolean gameOver;
   private boolean gamePaused;
   private boolean dialogDisplayed;

   private final Queue<ImageView> spots = new ConcurrentLinkedQueue<ImageView>();
   private final Queue<Animator> animators = new ConcurrentLinkedQueue<Animator>();

   private TextView highScoreTextView;
   private TextView currentScoreTextView;
   private TextView levelTextView;
   private LinearLayout livesLinearLayout;

   private Resources resources;
   private LayoutInflater layoutInflater;
   private RelativeLayout relativeLayout;

   private static final int INITIAL_ANIMATION_DURATION = 6000;
   private static final Random random = new Random();

   private static final int SPOT_DIAMETER =100;

   private static final float SCALE_X = 0.25f;
   private static final float SCALE_Y = 0.25f;

   private static final int INITIAL_SPOTS = 5;
   private static final int SPOT_DELAY = 500;
   private static final int LIVES = 3;
   private static final int MAX_LIVES = 7;
   private static final int NEW_LEVEL = 10;

   private Handler spotHandler;

   private static final int HIT_SOUND_ID = 1;
   private static final int MISS_SOUND_ID = 2;
   private static final int DISAPPEAR_SOUND_ID = 3;
   private static final int SOUND_PRIORITY = 1;
   private static final int SOUND_QUALITY = 100;
   private static final int MAX_STREAMS = 4;
   private Map<Integer,Integer> soundMap;
   private int volume;
   private SoundPool soundPool;

   public SpotOnView(Context context,SharedPreferences sharedPreferences,RelativeLayout parentLayout)
   {
      super(context);
      preferences = sharedPreferences;
      highScore = preferences.getInt(HIGH_SCORE,0);

      resources = context.getResources();
      layoutInflater = (LayoutInflater) context.getSystemService(
               Context.LAYOUT_INFLATER_SERVICE);
      relativeLayout = parentLayout;
      livesLinearLayout = (LinearLayout)relativeLayout.findViewById(R.id.lifeLinearLayout);
      highScoreTextView = (TextView)relativeLayout.findViewById(R.id.highScoreTextView);
      levelTextView = (TextView) relativeLayout.findViewById(R.id.levelTextView);
      currentScoreTextView= (TextView)relativeLayout.findViewById(R.id.scoreTextView);
      spotHandler = new Handler();

   }

   @Override
   protected void onSizeChanged(int w, int h, int oldw, int oldh)
   {
     viewWidth = w;
      viewHeight = h;
   }
   public  void pause()
   {
      gamePaused =true;
      soundPool.release();
      soundPool = null;
      cancelAnimations();
   }
   private  void cancelAnimations()
   {
      for(Animator animator : animators)
      {
         animator.cancel();
      }
      for(ImageView view : spots)
      {
         relativeLayout.removeView(view);
      }
      spotHandler.removeCallbacks(addSpotRunnable);
      animators.clear();
      spots.clear();

   }
   public void resume(Context context)
   {
      gamePaused = false;
      initializeSoundEffects(context);
      if(!dialogDisplayed)
         resetGame();
   }
   public void resetGame()
   {
      spots.clear();
      animators.clear();
      livesLinearLayout.removeAllViews();
      gameOver = false;
      level = 1;
      score = 0;
      spotsTouched = 0;
      animationTime = INITIAL_ANIMATION_DURATION;
      displayScores();
      for(int i = 0;i<LIVES;i++)
         livesLinearLayout.addView((ImageView) layoutInflater.inflate(R.layout.life,null));
      for(int i = 0;i<=INITIAL_SPOTS;i++)
      {
         spotHandler.postDelayed(addSpotRunnable,i*SPOT_DELAY);
      }

   }
   public void initializeSoundEffects(Context context)
   {
      soundPool = new SoundPool(MAX_STREAMS,AudioManager.STREAM_MUSIC,SOUND_QUALITY);
      AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
      volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
      soundMap = new HashMap<Integer,Integer>();
      soundMap.put(HIT_SOUND_ID, soundPool.load(context, R.raw.hit, SOUND_PRIORITY));
      soundMap.put(MISS_SOUND_ID, soundPool.load(context, R.raw.miss, SOUND_PRIORITY));
      soundMap.put(DISAPPEAR_SOUND_ID, soundPool.load(context, R.raw.disappear, SOUND_PRIORITY));
   }
   public void displayScores()
   {
      levelTextView.setText(resources.getString(R.string.level) + " " + level);
      highScoreTextView.setText(resources.getString(R.string.high_score) + " " + highScore);
      currentScoreTextView.setText(resources.getString(R.string.score) + " " + score);
   }
   private Runnable addSpotRunnable = new Runnable() {
      @Override
      public void run() {
         addNewSpot();
      }
   };
   public void addNewSpot()
   {
      int x = random.nextInt(viewWidth - SPOT_DIAMETER);
      int y = random.nextInt(viewHeight - SPOT_DIAMETER);
      int x2 = random.nextInt(viewWidth - SPOT_DIAMETER);
      int y2 = random.nextInt(viewHeight - SPOT_DIAMETER);

      final ImageView spot =
              (ImageView) layoutInflater.inflate(R.layout.untouched, null);
      spots.add(spot);
      spot.setLayoutParams(new RelativeLayout.LayoutParams(
              SPOT_DIAMETER, SPOT_DIAMETER));
      spot.setImageResource(random.nextInt(2) == 0 ?
              R.drawable.green_spot : R.drawable.red_spot);
      spot.setX(x);
      spot.setY(y);
      spot.setOnClickListener(
              new OnClickListener()
              {
                 public void onClick(View v)
                 {
                    touchedSpot(spot);
                 }
              }
      );
      relativeLayout.addView(spot);
      spot.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y)
              .setDuration(animationTime).setListener(
              new AnimatorListenerAdapter()
              {
                 @Override
                 public void onAnimationStart(Animator animation)
                 {
                    animators.add(animation);
                 }

                 public void onAnimationEnd(Animator animation)
                 {
                    animators.remove(animation);

                    if (!gamePaused && spots.contains(spot))
                    {
                       missedSpot(spot);
                    }
                 }
              }
      );
   }
   @Override
   public boolean onTouchEvent(MotionEvent event) {
      if(soundPool!=null)
      {
         soundPool.play(MISS_SOUND_ID,volume,volume,SOUND_QUALITY,0,1f);
      }
      score-=15*level;
      score = Math.max(score,0);
      displayScores();
      return true;
   }
   private void touchedSpot(ImageView v)
   {
      relativeLayout.removeView(v);
      spots.remove(v);
      ++spotsTouched;
      if(soundPool!=null)
      {
         soundPool.play(HIT_SOUND_ID,volume,volume,SOUND_QUALITY,0,1f);
      }
      score+=10*level;
      if(spotsTouched%NEW_LEVEL==0)
      {
         ++level;
         animationTime*=0.95;
      }
      if(livesLinearLayout.getChildCount()<MAX_LIVES)
      {
         ImageView life = (ImageView)layoutInflater.inflate(R.layout.life,null);
         livesLinearLayout.addView(life);
      }
      displayScores();
      if(!gameOver)
         addNewSpot();
   }
   public void missedSpot(ImageView v) {
      spots.remove(v);
      relativeLayout.removeView(v);
      if (gameOver)
         return;
      if (soundPool != null)
         soundPool.play(DISAPPEAR_SOUND_ID, volume, volume, SOUND_QUALITY, 0, 1f);
      if (livesLinearLayout.getChildCount() == 0) {
         gameOver = true;
      }
      if (score > highScore) {
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt(HIGH_SCORE, score);
         editor.commit();
         highScore = score;

      cancelAnimations();
      Builder dialogBuilder = new AlertDialog.Builder(getContext());
      dialogBuilder.setTitle(R.string.game_over);
      dialogBuilder.setMessage(getResources().getString(R.string.score) + " " + score);
      dialogBuilder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            displayScores();
            dialogDisplayed = false;
            resetGame();
         }
      });
      dialogDisplayed = true;
      dialogBuilder.show();
   }else
   {
     livesLinearLayout.removeViewAt(livesLinearLayout.getChildCount()-1);
      addNewSpot();
   }
   }
}


