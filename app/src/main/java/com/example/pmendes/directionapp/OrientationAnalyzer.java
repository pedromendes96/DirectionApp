package com.example.pmendes.directionapp;

import android.util.Log;

import java.util.ArrayList;

public class OrientationAnalyzer implements Publisher{

    private MainActivity.ORIENTATION mMode;
    private ArrayList<Float> xAccelRounds;
    private ArrayList<Float> yAccelRounds;
    private ArrayList<Subscriber> subscribers;
    private final int LIMIT_ROUNDS = 3;

    private final float VERTICAL_CRITERION = 2.5f;
    private final float HORIZONTAL_CRITERION = 2.5f;

    public boolean isInTimeout = false;

    @Override
    public void Add(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void Remove(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void Publish(DIRECTION direction) {
        for (Subscriber subscriber :
                subscribers) {
            subscriber.Update(direction);
        }
    }

    public static enum DIRECTION {
        TOP,
        DOWN,
        RIGHT,
        LEFT,
        STOPPED
    }

    public OrientationAnalyzer(MainActivity.ORIENTATION mode){
        mMode = mode;
        xAccelRounds = new ArrayList<>();
        yAccelRounds = new ArrayList<>();
        subscribers = new ArrayList<>();
    }

    public void setOrientation(MainActivity.ORIENTATION mode){
        mMode = mode;
    }

    public void updateValues(float xAccel, float yAccel){
        Log.d("xValue","X:"+xAccel);
        Log.d("yValue","Y:"+yAccel);
        xAccelRounds.add(xAccel);
        yAccelRounds.add(yAccel);
        if(xAccelRounds.size() == LIMIT_ROUNDS + 1){
            xAccelRounds.remove(0);
            yAccelRounds.remove(0);
            if(!isInTimeout){
                DIRECTION direction = ResolveDirection();
                Publish(direction);
                isInTimeout = true;
                Thread enablePublish = new EnablePublish();
                enablePublish.start();
            }
        }
    }

    private DIRECTION ResolveDirection(){
        DIRECTION chosen;
        DIRECTION vertical;
        DIRECTION horizontal;
        if(mMode == MainActivity.ORIENTATION.PORTRAIT){
            vertical = getVerticalPortraitDirection();
            horizontal = getHorizontalPortraitDirection();
        }else{
            vertical = getVerticalLandscapeDirection();
            horizontal = getHorizontalLandscapeDirection();
        }

        if(vertical == DIRECTION.STOPPED && horizontal == DIRECTION.STOPPED) {
            chosen = DIRECTION.STOPPED;
        }
        else if(vertical == DIRECTION.STOPPED && horizontal != DIRECTION.STOPPED){
            chosen = horizontal;
        }
        else if(vertical != DIRECTION.STOPPED && horizontal == DIRECTION.STOPPED){
            chosen = vertical;
        }
        else{
            float avgXAccel = 0.0f;
            float avgyAccel = 0.0f;
            for (int i = 0; i < xAccelRounds.size(); i++) {
                avgXAccel += xAccelRounds.get(i);
                avgyAccel += yAccelRounds.get(i);
            }
            if((avgXAccel/xAccelRounds.size()) >= (avgyAccel/yAccelRounds.size())){
                chosen = horizontal;
            }else{
                chosen = vertical;
            }
        }
        Log.d("RESULT", chosen.toString());
        return chosen;
    }

    private DIRECTION getHorizontalLandscapeDirection() {
        if(xAccelRounds.size() < LIMIT_ROUNDS)
            return DIRECTION.STOPPED;

        int upCount = 0;
        int downCount = 0;
        float criterion = HORIZONTAL_CRITERION;
        for (float round :
                xAccelRounds) {
            if(round >= criterion)
                upCount++;
            criterion *= -1;
            if(round <= criterion)
                downCount++;
        }
        Log.d("getHorizontalDirection", "upCount:" + upCount);
        Log.d("getHorizontalDirection", "downCount:" + downCount);
        if(upCount == LIMIT_ROUNDS)
            return DIRECTION.TOP;
        else if(downCount == LIMIT_ROUNDS)
            return DIRECTION.DOWN;
        else
            return DIRECTION.STOPPED;
    }

    private DIRECTION getVerticalLandscapeDirection() {
        if(yAccelRounds.size() < LIMIT_ROUNDS)
            return DIRECTION.STOPPED;

        int leftCount = 0;
        int rightCount = 0;
        float criterion = VERTICAL_CRITERION;
        for (float round :
                yAccelRounds) {
            if(round >= criterion)
                rightCount++;
            criterion *= -1;
            if(round <= criterion)
                leftCount++;
        }
        if(leftCount == LIMIT_ROUNDS)
            return DIRECTION.LEFT;
        else if(rightCount == LIMIT_ROUNDS)
            return DIRECTION.RIGHT;
        else
            return DIRECTION.STOPPED;
    }

    private DIRECTION getVerticalPortraitDirection(){
        if(yAccelRounds.size() < LIMIT_ROUNDS)
            return DIRECTION.STOPPED;

        int upCount = 0;
        int downCount = 0;
        float criterion = VERTICAL_CRITERION;
        for (float round :
                yAccelRounds) {
            if(round >= criterion)
                upCount++;
            criterion *= -1;
            if(round <= criterion)
                downCount++;
        }
        if(upCount == LIMIT_ROUNDS)
            return DIRECTION.TOP;
        else if(downCount == LIMIT_ROUNDS)
            return DIRECTION.DOWN;
        else
            return DIRECTION.STOPPED;
    }

    private DIRECTION getHorizontalPortraitDirection(){
        if(xAccelRounds.size() < LIMIT_ROUNDS)
            return DIRECTION.STOPPED;

        int rightCount = 0;
        int leftCount = 0;
        float criterion = HORIZONTAL_CRITERION;
        for (float round :
                xAccelRounds) {
            if(round >= criterion)
                leftCount++;
            criterion *= -1;
            if(round <= criterion)
                rightCount++;
        }
        Log.d("getHorizontalDirection", "leftCount:" + leftCount);
        Log.d("getHorizontalDirection", "rightCount:" + rightCount);
        if(leftCount == LIMIT_ROUNDS)
            return DIRECTION.LEFT;
        else if(rightCount == LIMIT_ROUNDS)
            return DIRECTION.RIGHT;
        else
            return DIRECTION.STOPPED;
    }

    private class EnablePublish extends Thread{
        public void run() {
            try
            {
                Thread.sleep(400);
                isInTimeout = false;
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }

        }
    }
}
