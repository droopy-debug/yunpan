package com.qst.yunpan.pojo;


//result用来封装返回数据
public class Result<T>{               /*<T> 的作用是让 Result 类可以使用任意类型的数据。也就是说，T 是一个占位符，表示一种类型，可以在实例化 Result 类时指定具体的类型。*/

    private int code;
    private boolean success;
    private T data;
    private String msg;

    public Result() {
    }

    public Result(int code, boolean success, String msg) {
        super();
        this.code = code;
        this.success = success;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
