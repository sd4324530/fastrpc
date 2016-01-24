package com.github.sd4324530.fastrpc.core.message;

/**
 * @author peiyu
 */
public class ResponseMessage implements IMessage {

    private String seq;

    private int resultCode;

    private String errorMessage;

    private Object responseObject;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getResponseObject() {
        return responseObject;
    }

    public void setResponseObject(Object responseObject) {
        this.responseObject = responseObject;
    }

    @Override
    public void setSeq(String seq) {
        this.seq = seq;
    }

    @Override
    public String getSeq() {
        return this.seq;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "seq='" + seq + '\'' +
                ", resultCode=" + resultCode +
                ", errorMessage='" + errorMessage + '\'' +
                ", responseObject=" + responseObject +
                '}';
    }
}
