package kr.ac.gachon.www.SaveMe.Entity;

public class XYZ  { //좌표를 저장할 클래스
    double X;
    double Y;
    double Z;

    public XYZ(double X, double Y, double Z) {
        this.X=X;
        this.Y=Y;
        this.Z=Z;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public double getZ() {
        return Z;
    }

    public double getDiff(XYZ xyz) {    //다른 XYZ 클래스와 값 비교
        double DiffX=Math.abs(X-xyz.getX());
        double DiffY=Math.abs(Y-xyz.getY());
        double DiffZ=Math.abs(Z-xyz.getZ());

        return DiffX+DiffY+DiffZ;   //모두의 차이를 더해서 출력
    }
}
