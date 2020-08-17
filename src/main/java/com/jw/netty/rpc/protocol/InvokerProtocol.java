package com.jw.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class InvokerProtocol implements Serializable {

    private String className; //
    private String methodName; // 函数名称
    private Class<?>[] params; // 参数类型
    private Object[] values; // 参数列表

}
