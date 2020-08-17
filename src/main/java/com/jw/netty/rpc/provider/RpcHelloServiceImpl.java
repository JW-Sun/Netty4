package com.jw.netty.rpc.provider;

import com.jw.netty.rpc.api.IRpcHelloService;

public class RpcHelloServiceImpl implements IRpcHelloService {
    public String hello(String name) {
        return "Hello " + name + ".";
    }
}
