package org.acme;

public class RequestDao {

    int param1;
    int param2;

    public Object[] getOperation() {
        return new Object[]{param1, param2};
    }

    public int getParam1() {
        return param1;
    }

    public void setParam1(int param1) {
        this.param1 = param1;
    }

    public int getParam2() {
        return param2;
    }

    public void setParam2(int param2) {
        this.param2 = param2;
    }

    @Override
    public String toString() {
        return "RequestDao [param1=" + param1 + ", param2=" + param2 + "]";
    }    
}