package com.jw.netty.rpc.registry;

import com.jw.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {

    // 保存所有的可用服务
    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();

    // 保存所有相关的服务类
    private List<String> classNames = new ArrayList<>();

    public RegistryHandler() {
        // 完成递归扫描
        scannerClass("com.jw.netty.rpc.provider");
        doRegister();
    }

    // 完成注册
    private void doRegister() {
        if (classNames.size() == 0) return;

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                // 获得这个类实现的所有的接口
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(), clazz.newInstance());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    // 递归扫描
    private void scannerClass(String s) {
        URL url = this.getClass().getClassLoader().getResource(s.replaceAll("\\.", "/"));

        File dir = new File(url.getFile());

        for (File file : dir.listFiles()) {
            // 是目录继续递归
            if (file.isDirectory()) {
                scannerClass(s + "." + file.getName());
            } else {
                classNames.add(s + "." + file.getName().replace(".class", "").trim());
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object res = new Object();

        InvokerProtocol request = (InvokerProtocol)msg;

        // 当客户端建立连接的时候，需要从自定义协议中获取信息，以及具体的服务和实参
        // 使用反射调用
        if (registryMap.containsKey(request.getClassName())) {
            Object clazz = registryMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParams());

            res = method.invoke(clazz, request.getValues());
        }
        ctx.write(res);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
