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
        Point p = new Point(this);
        return p.addInPlace(other);
    }

    public Point addInPlace(Point other){
        this.x += other.getX();
        this.y += other.getY();
        return this;
    }

    public Point multiplyInPlace(Point other){
        this.x *= other.getX();
        this.y *= other.getY();
        return this;
    }

    public Point multiplyInPlace(float multiplier){
        this.x *= multiplier;
        this.y *= multiplier;
        return this;
    }
}
