package com.example.catchcat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import static com.example.catchcat.PlayGround.DIR.*;

/**
 * Created by 钦颖 on 2015/5/10.
 */
public class PlayGround extends SurfaceView implements  OnTouchListener{

    private static final int ROW = 12;
    private static final int COL = 10;
    private static final int BLOCKS = 4;
    private static boolean isGameOver = false;
    private int WIDTH = 50;
    public static enum DIR{
        LEFT,LEFTTOP,RIGHTTOP,RIGHT,RIGHTBOTTOM,LEFTBOTTOM
    };
    private Dot[][] matrix;
    private Dot theCat;

    /****
     * Function Name :PlayGround
     * Function Type :Constructor
     * It helps you to create game objects
     *****/
    public PlayGround(Context context, AttributeSet attrs) {
        super(context,attrs);
        getHolder().addCallback(callback);

        matrix = new Dot[ROW][COL];

        for (int i = 0 ; i < ROW ; i++)
            for (int j = 0 ; j < COL ; j++){
                matrix[i][j] = new Dot(j,i);
            }
        initGame();
        setOnTouchListener(this);
    }
    /****
     * Function Name : redraw
     * It can draw the game objects
     * ***/
    private void redraw()
    {
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.GRAY);

        Paint paint = new Paint();

        Bitmap catbmp= BitmapFactory.decodeResource(getResources(),R.mipmap.cat);
        Bitmap startbmp = BitmapFactory.decodeResource(getResources(), R.mipmap.starticon);
        Bitmap resizedCatBitmap = Bitmap.createScaledBitmap(catbmp, WIDTH-4, WIDTH-4,true);
        Bitmap resizedStartBitmap = Bitmap.createScaledBitmap(startbmp, WIDTH*2, WIDTH*2,true);

        for(int i = 0 ; i < ROW ; i++)
        for(int j = 0 ; j < COL ; j++)
        {
            Dot tmpDot = getDot(j,i);
            switch (tmpDot.getStatus())
            {
                case Dot.STATUS_OFF:
                    paint.setColor(0xFF595959);
                    break;
                case Dot.STATUS_ON:
                    paint.setColor(0xFF00BFFF);
                    break;
                case Dot.STATUS_IN:

                    if(i%2==1)
                    c.drawBitmap(resizedCatBitmap,j*WIDTH+WIDTH/2 + 2,i*WIDTH +2 , paint);
                    else
                    c.drawBitmap(resizedCatBitmap,j*WIDTH +2,i*WIDTH +2, paint);
                    c.drawBitmap(resizedStartBitmap,(COL/2)*WIDTH -WIDTH/2 ,(ROW)*WIDTH +2, paint);
                    continue;
            }

            if(i % 2 ==1)
            {
            c.drawOval(new RectF(tmpDot.getX()*WIDTH + WIDTH/2,tmpDot.getY()*WIDTH,(tmpDot.getX()+1)*WIDTH + WIDTH/2,(tmpDot.getY()+1)*WIDTH),paint);
            }
            else
            {
            c.drawOval(new RectF(tmpDot.getX()*WIDTH ,tmpDot.getY()*WIDTH,(tmpDot.getX()+1)*WIDTH ,(tmpDot.getY()+1)*WIDTH),paint);
            }
        }

        getHolder().unlockCanvasAndPost(c);
    }

    Callback callback = new Callback() {
         @Override
         public void surfaceCreated(SurfaceHolder surfaceHolder) {
             redraw();
         }

         @Override
         public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
             WIDTH = i2 / (1+ COL);
             redraw();
         }

         @Override
         public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

         }
     };

    private Dot getDot(int x,int y)
    {
        return matrix[y][x];
    }

    private  Dot getNeighborhoodDot(Dot dot, DIR dir)
    {
        int tx = dot.getX(),ty = dot.getY(),offset=0;
        if(ty % 2 == 0)
        {
            offset = -1;
        }
        switch (dir)
        {
            case LEFT:
                tx--;
                break;
            case LEFTTOP:
                tx += offset;
                ty--;
                break;
            case RIGHTTOP:
                tx += offset +1;
                ty--;
                break;
            case RIGHT:
                tx++;
                break;
            case RIGHTBOTTOM:
                tx += offset +1;
                ty++;
                break;
            case LEFTBOTTOM:
                tx += offset;
                ty++;
                break;
        }


        if(tx < 0 || ty <0|| tx >= COL || ty >= ROW)
        {
            return null;
        }
        return getDot(tx, ty);
    }

    private boolean isCatOut(Dot dot)
    {
        if(dot.getX() == 0 ||dot.getY()==0||dot.getX() == COL-1 ||dot.getY() == ROW-1 )
        {
            return true;
        }
        return false;
    }

    private int getLifeValue(Dot dot)
    {
        int v = 0;
        if(dot.getStatus() != Dot.STATUS_OFF)
        {
            return v;
        }  //if the dot locates at the border of map,give it the top value
        else if(isCatOut(dot))
        {
            return 20;
        }
        else v++;
        for(DIR d : DIR.values())
        {
            try{
                Dot tmpDot =getNeighborhoodDot(dot, d);
                if(tmpDot.getStatus() == Dot.STATUS_OFF) {
                   v++;
                    //if the dot locates at the border of map,give it the second value
                   if(isCatOut(tmpDot))
                   {
                       return 10;
                   }
                }
            }catch (Exception ex)
            {

            }
        }
        return v;
    }

    /* As for the return value step
     1. positive number represents the cat needs step steps to the border in dir direction
     2. negative number represents the cat needs |step| steps to the block in dir direction
     3. 0 represents the blocks is near to the cat
     */
    private int getDistance(Dot dot,DIR dir)
    {

        int step = 0 ;
        Dot tmpDot = getNeighborhoodDot(dot,dir);
        while (true)
        {
            if(isCatOut(tmpDot)&&tmpDot.getStatus() == Dot.STATUS_OFF)
            {
                step++;
                return step;
            }
            else if(tmpDot.getStatus() == Dot.STATUS_ON)
            {
                  return -step;
            }
            step++;
            tmpDot = getNeighborhoodDot(tmpDot,dir);
        }
    }

    /*
    * change the dot status when the cat moves on
    * */
    private void catMove(Dot tmpDot)
    {
        //set the last cat dot to STATUS_OFF
        getDot(theCat.getX(),theCat.getY()).setStatus(Dot.STATUS_OFF);
        //cat move
        theCat.setXY(tmpDot.getX(),tmpDot.getY());
        getDot(theCat.getX(),theCat.getY()).setStatus(Dot.STATUS_IN);
    }


    private void initGame()
    {
        isGameOver = false;
        for (int i = 0 ; i < ROW ; i++)
            for (int j = 0 ; j < COL ; j++){
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        theCat = new Dot(4,5);
        theCat.setStatus(Dot.STATUS_IN);
        getDot(theCat.getX(),theCat.getY()).setStatus(Dot.STATUS_IN);

        for(int i = 0 ;i < BLOCKS ; )
        {
            int tx = (int) ((Math.random()*10) % COL);
            int ty = (int) ((Math.random()*10) % ROW);
            if(getDot(tx,ty).getStatus() == Dot.STATUS_OFF)
            {
                getDot(tx,ty).setStatus(Dot.STATUS_ON);
                i++;
            }
        }

    }


    @Override
    public boolean onTouch(View view, MotionEvent e) {

        if(e.getAction() == MotionEvent.ACTION_UP)
        {
             int tx,ty = (int) (e.getY()/WIDTH);
            if(ty % 2 == 0)
            {
                tx = (int) (e.getX()/WIDTH);
            }
            else
            {
                tx = (int) ((e.getX() - WIDTH/2)/WIDTH);
            }
            if(tx >= COL || ty >= ROW)
            {
                if(tx>=(COL/2) && tx<COL/2+1 &&ty>=ROW &&ty<ROW+1)
                initGame();
                redraw();
                return false;
            }
            else
            {
                //GameOver
                if(isGameOver)
                {
                    return false;
                }

                if(getDot(tx,ty).getStatus() == getDot(tx,ty).STATUS_OFF) {
                //change the Touched Dot
                getDot(tx, ty).setStatus(Dot.STATUS_ON);
                 //set cat-move flag
                boolean isCatmove = false;
                int [] lifeValue = new int[6];
                int [] distance = new int[6];
                int k =0,maxLifeValue=0,tmplife = 0,minDistance=0;
                Dot [] dots = new Dot[6];
                Dot lifeDot = new Dot(0,0);
                Dot tmpDot = new Dot(0,0);

                for(DIR d : DIR.values())
                {
                    try {
                        dots[k] = getNeighborhoodDot(theCat, d);
                        lifeValue[k] = getLifeValue(dots[k]);
                        distance[k] = getDistance(theCat,d);
                        k++;
                    }catch (Exception ex)
                    {

                    }
                }


                for(int i = 0 ; i < distance.length ;i++)
                {
                    if(lifeValue[i] > maxLifeValue)
                    {
                        lifeDot = dots[i];
                    }
                    if(minDistance == distance[i] && tmplife < lifeValue[i])
                    {
                        tmpDot = dots[i];
                        //set the cat-move flag
                        isCatmove = true;
                    }
                    else
                    {
                        if(minDistance == 0 &&distance[i] !=0)
                        {
                            minDistance = distance[i];
                            tmpDot = dots[i];
                            tmplife = lifeValue[i];
                            //set the cat-move flag
                            isCatmove = true;
                        }
                        else if(minDistance > 0)
                        {
                            if(distance[i] >0 && distance[i] < minDistance)
                            {
                                minDistance = distance[i];
                                tmpDot = dots[i];
                            }
                        }
                        else if(minDistance < 0)
                        {
                            if(distance[i] >0 || (distance[i] <0 && distance[i] < minDistance))
                            {
                                minDistance = distance[i];
                                tmplife = lifeValue[i];
                                tmpDot = dots[i];
                            }
                        }
                    }
                }
                    if(maxLifeValue >= 10)
                    {
                        tmpDot = lifeDot;
                    }
                    //if cat can't move , the user win
                    if(isCatmove == false)
                    {
                        //Notification Message
                        Toast.makeText(getContext(),"Win",Toast.LENGTH_LONG).show();
                        isGameOver = true;
                    }
                    else
                    {
                        catMove(tmpDot);
                    }
                    if(isCatOut(theCat))
                    {
                        //Notification Message
                        Toast.makeText(getContext(),"Lose",Toast.LENGTH_LONG).show();
                        isGameOver = true;
                    }
                    redraw();

                }
            }
        }
        return true;
    }

}
