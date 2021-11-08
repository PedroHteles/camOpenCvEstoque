package com.example.camopencv;

public class Qr {
    double x,y;
    String resultado;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Qr(double x, double y, String resultado) {
        this.x = x;
        this.y = y;
        this.resultado = resultado;
    }
}
