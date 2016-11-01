package cn.ucai.superwechat.bean;

import java.io.Serializable;

/**
 * Created by 11039 on 2016/10/13.
 */
public class Result implements Serializable {
    /**
     * retCode : 0
     * retMsg : true
     * retData : null
     */

    private int retCode;
    private boolean retMsg;
    private Object retData;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public boolean isRetMsg() {
        return retMsg;
    }

    public void setRetMsg(boolean retMsg) {
        this.retMsg = retMsg;
    }

    public Object getRetData() {
        return retData;
    }

    public void setRetData(Object retData) {
        this.retData = retData;
    }

    public Result(int retCode, boolean retMsg, Object retData) {
        this.retCode = retCode;
        this.retMsg = retMsg;
        this.retData = retData;
    }

    public Result() {
    }

    @Override
    public String toString() {
        return "Result{" +
                "retCode=" + retCode +
                ", retMsg=" + retMsg +
                ", retData=" + retData +
                '}';
    }
}
