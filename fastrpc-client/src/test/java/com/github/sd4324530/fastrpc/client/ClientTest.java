package com.github.sd4324530.fastrpc.client;

import com.github.sd4324530.fastrpc.core.message.RequestMessage;
import com.github.sd4324530.fastrpc.core.message.ResponseMessage;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author peiyu
 */
public class ClientTest {

    public static void main(String[] args) {
        for(int i = 0;i < 1;i++) {
            new Thread(() -> {
                try (IClient client = new FastRpcClient()) {
                    client.connect(new InetSocketAddress("127.0.0.1", 4567));
                    TestService service = client.getService(TestService.class);
                    while(true) {
                        System.out.println(service.say("Hello"));
                        TimeUnit.SECONDS.sleep(1);
                    }
                } catch (Exception e) {
                    System.out.println("错误");
                    e.printStackTrace();
                }
            }).start();
        }
//        RequestMessage requestMessage = new RequestMessage();
//        requestMessage.setServerName("TestService");
//        requestMessage.setMethodName("say");
//        requestMessage.setArgsClassTypes(new Class[]{String.class});
//        requestMessage.setArgs(new Object[]{"hello"});
//
//        long start = System.currentTimeMillis();
//        ResponseMessage responseMessage = client.invoke(requestMessage);
//        ResponseMessage responseMessage2 = client.invoke(requestMessage);
//        long time = System.currentTimeMillis() - start;
//        System.out.println("RPC调用耗时:" + time + "毫秒");
//        System.out.println(responseMessage.toString());
//        System.out.println(responseMessage2.toString());


//        System.out.println("name:" + service.name());
//        service.ok("asd");
//        service.none();
    }

//    @Test
//    public void porxy() {
//        TestService service = (TestService) Proxy.newProxyInstance(TestService.class.getClassLoader()
//                , new Class[]{TestService.class}
//                , (proxy, method, args) -> {
//                    System.out.println("proxy:" + proxy.getClass());
//                    System.out.println("method:" + method);
//                    System.out.println("args:" + Arrays.toString(args));
//                    return args[0].toString();
//                });
//
//        String hello = service.say("hello");
//        System.out.println(hello);
//    }

}
