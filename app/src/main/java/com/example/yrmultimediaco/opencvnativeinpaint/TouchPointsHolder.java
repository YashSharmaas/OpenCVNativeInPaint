package com.example.yrmultimediaco.opencvnativeinpaint;

import android.graphics.Point;

import java.util.ArrayList;

class TouchPointsHolder {
    int color;
    float brushThickness;
    ArrayList<Point> pointsList;

    public TouchPointsHolder(int color, float brushThickness) {
        this.color = color;
        this.brushThickness = brushThickness;
        this.pointsList = new ArrayList<>();
    }

    public void addPoint(Point point){
        this.pointsList.add(point);
    }

    public ArrayList<Point> getPointsList() {
        return pointsList;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getBrushThickness() {
        return brushThickness;
    }

    public void setBrushThickness(float brushThickness) {
        this.brushThickness = brushThickness;
    }

}
