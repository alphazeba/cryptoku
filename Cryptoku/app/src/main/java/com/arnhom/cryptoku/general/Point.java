package com.arnhom.cryptoku.general;

public class Point {
    private float x,y;

    public Point(Point point){
        set(point.x,point.y);
    }

    public Point(float x,float y){
        set(x,y);
    }

    public Point set(float x, float y){
        this.x = x;
        this.y = y;
        return this;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public Point add(Point other){
        this.x += other.getX();
        this.y += other.getY();
        return this;
    }

    public Point multiply(Point other){
        this.x *= other.getX();
        this.y *= other.getY();
        return this;
    }
    public Point multiply(float other){
        this.x *= other;
        this.y *= other;
        return this;
    }

}
