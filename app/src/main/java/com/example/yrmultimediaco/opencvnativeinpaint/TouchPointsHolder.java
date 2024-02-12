package com.example.yrmultimediaco.opencvnativeinpaint;

import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;

class TouchPointsHolder {
    int color;
    float brushThickness;
    ArrayList<Point> pointsList;
    boolean isStraightLine;
    boolean isRectangle;
    private Path path;

    public TouchPointsHolder(int color, float brushThickness, boolean isStraightLine, boolean isRectangle) {
        this.color = color;
        this.brushThickness = brushThickness;
        this.pointsList = new ArrayList<>();
        this.isStraightLine = isStraightLine;
        this.isRectangle = isRectangle;

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

    public boolean isStraightLine() {
        return isStraightLine;
    }

    public void setStraightLine(boolean straightLine) {
        isStraightLine = straightLine;
    }

    public boolean isRectangle() {
        return isRectangle;
    }

    public void setRectangle(boolean rectangle) {
        isRectangle = rectangle;
    }
}
