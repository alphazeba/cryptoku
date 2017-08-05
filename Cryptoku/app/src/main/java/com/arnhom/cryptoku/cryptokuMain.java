package com.arnhom.cryptoku;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.arnhom.cryptoku.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class cryptokuMain extends AppCompatActivity{
//TODO this program is horribly organized.


    //variables

    //main components
    SurfaceView surfaceView;
    TouchInput input;
    Puzzle puzzle;
    ExciteInformer exciteInformer;

    TextView solutionText;

    int x;

    //drawing thread items.
    Thread thread;
    boolean threadRunning;
    boolean canvasDirty;

    long lastFrameStartTime;
    long thisFrameStartTime;
    long onPauseOffsetTime;
    long minimumFrameLength;
    float frameTime;
    float oldWidth = 0;
    float oldHeight = 0;

    //used for input blocking.
    //only celebrate once newly entering the success state.
    boolean hasSucceeded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO remove this x variables
        x = 0;

        setContentView(R.layout.activity_cryptuko_main);

        surfaceView = (SurfaceView)findViewById(R.id.main_surface);
        solutionText = (TextView)findViewById(R.id.solutionTextView);
        threadRunning = false;
        thread = null;
        this.setFPS(60);
        onPauseOffsetTime = 0;
        canvasDirty = true;
        oldWidth = 0;
        oldHeight = 0;

        hasSucceeded = false;



        //The drawing thread is started later in onResume.

        input = new TouchInput(720,1280);
        puzzle = new Puzzle(0);//right now just builds a level 0 puzzle;
        exciteInformer = new ExciteInformer(720,1280);


        //surfaceView.getHolder().addCallback(this);
        surfaceView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view,MotionEvent me){
                TouchAction touch = input.update(me);
                if(touch != null){
                    //there was an action
                    onTouchAction(touch);
                }
                return true;
            }
        });
    }

    private void onTouchAction(TouchAction touch){
        //TODO pass touch actions over to the update thingy i guess.
        puzzle.onInput(touch);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i("test","onresume has run.");
        flagForRedraw();
        //handle pause offset time
        //adjust last and this frame so that it runs as if the phone was never off.
        onPauseOffsetTime = System.currentTimeMillis() - onPauseOffsetTime;
        lastFrameStartTime += onPauseOffsetTime;
        thisFrameStartTime += onPauseOffsetTime;

        startThread();
    }

    @Override
    protected void onPause(){
        super.onPause();

        //handle pause offset
        //stores the time at which the phone closed.
        onPauseOffsetTime = System.currentTimeMillis();

        //stop the drawing thread
        killThread();
    }

    private void startThread(){
        //do not start the thread if the thread is already running
        if(thread != null){
            Log.e("error","thread still alive");
            return;
        }

        Log.i("test","start thread has run.");

        //start the thread.
        threadRunning = true;

        //start the drawing thread.
        thread = new Thread(new Runnable(){
            public void run(){
                runnableLoop();
            }
        });
        thread.start();
    }

    private void killThread(){
        threadRunning = false;

        //if the thread has already been killed, we don't have to try again
        if(thread == null){
            return;
        }

        //join the thread.
        //this will continue to loop and wait to join until it succesfully joins without being interrupted.
        boolean joined = false;
        while(!joined){
            try{
                thread.join();
                joined = true;
            }
            catch(java.lang.InterruptedException e){
                //handle the interrupted exception.
                Log.e("error","join has been interrupted in killThread()");
            }
        }

        //empty the thread.
        thread = null;
    }

    private void setFPS(int fps){
        minimumFrameLength = (long)(1000.f/fps);
    }

    void draw(SurfaceHolder holder){
        //Log.i("debug","attempting to actualDraw somethign");
        if(holder == null){
            return;
        }
        Canvas canvas = holder.lockCanvas();
        if(canvas == null){
            Log.e("debug","we have failed to actualDraw this time :'(");
        } else {
            //check for resize
            if(oldWidth != canvas.getWidth() || oldHeight != canvas.getHeight()){
                onResize(canvas);
            }


            actualDraw(canvas);

            holder.unlockCanvasAndPost(canvas);
            canvasDirty = false;
        }
    }

    void update(){
        //update should use frameTime.
        if(puzzle.update(frameTime)){
            flagForRedraw();
        }

        if(puzzle.getSuccess() && !hasSucceeded){
            exciteInformer.inform("SUCCESS!!   SUCCESS!!  SUCCESS!!");
        }
        hasSucceeded = puzzle.getSuccess();

        if(exciteInformer.update(frameTime)){
            flagForRedraw();
        }
    }

    void onResize(Canvas canvas){
        oldHeight = canvas.getHeight();
        oldWidth = canvas.getWidth();

        //send resize events
        puzzle.onResize(canvas.getWidth(),canvas.getHeight());
        exciteInformer.onResize(canvas.getWidth(),canvas.getHeight());
    }

    void flagForRedraw(){
        //informs the program that something has changed on screen.
        canvasDirty = true;
    }

    //WARNING
    //runnableloop runs on a seperate thread from the ui thread.
    //This is the main loop of the side thread.
    void runnableLoop(){
        //thread Running is set to false by killThread().
        Log.i("test","runnable loop has begun");
        while(threadRunning){

            //clock stuff.
            long time = System.currentTimeMillis() - thisFrameStartTime;
            if(time < minimumFrameLength){
                SystemClock.sleep(minimumFrameLength - time);
            }

            lastFrameStartTime = thisFrameStartTime;
            thisFrameStartTime = System.currentTimeMillis();

            frameTime = ((float) (thisFrameStartTime - lastFrameStartTime))/1000; //frametime is in seconds.

            if(frameTime < 0){
                frameTime = 0;
            }
            //TODO add in an update and crap here.
            update();


            //drawing stuff.
            if(canvasDirty){
                draw(surfaceView.getHolder());
                //do not undirty the canvas here.
                //drawing can still fail.
            }
        }
        Log.i("test","runnable loop has exited.");
    }

    void actualDraw(Canvas canvas){
        //make up some paints.
        Paint bgColor = new Paint();
        bgColor.setColor(Color.BLACK);
        //actualDraw some crap.
        canvas.drawRect(new Rect(0,0,canvas.getWidth(),canvas.getHeight()),bgColor);

        puzzle.draw(canvas);
        exciteInformer.draw(canvas);
    }

    public void onReset(View view){
        solutionText.setText("");
        puzzle.reset();
        puzzle.shake();
        exciteInformer.inform("AGAIN!");
        flagForRedraw();
    }

    public void onNewPuzzle(View view){
        solutionText.setText("");
        killThread();
        puzzle = new Puzzle(0);
        puzzle.onResize(oldWidth,oldHeight);
        puzzle.shake();
        exciteInformer.inform("A NEW CHALLENGE!");
        startThread();
    }

    public void onGetSolution(View view){
        solutionText.setText(puzzle.solutionToString());
        puzzle.shake();
        exciteInformer.inform("TOO HARD?");
    }
}
