package com.example.yrmultimediaco.opencvnativeinpaint;

import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;

class TouchPointsHolder {
    int color;
    float brushThickness;
    ArrayList<Point> pointsList;
    private Path path;

    public TouchPointsHolder(int color, float brushThickness) {
        this.color = color;
        this.brushThickness = brushThickness;
        this.pointsList = new ArrayList<>();

        path = new Path();
    }

    public void addPoint(Point point){
        //this.pointsList.add(point);
        path.lineTo(point.x, point.y);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
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
