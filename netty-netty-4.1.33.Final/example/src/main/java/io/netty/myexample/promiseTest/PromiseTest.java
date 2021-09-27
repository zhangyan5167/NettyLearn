package io.netty.myexample.promiseTest;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Promise;

/**
 *
 *
 * @author ZXF
 */
public class PromiseTest {

    public static void main(String[] args) {
        PromiseTest promiseTest = new PromiseTest();
        Promise<Object> promise = promiseTest.doSomething("testttttt");
        promise.addListener(future -> System.out.println(promise.get() + ", promise.get() result"));

        System.out.println("finished");
    }

    private Promise<Object> doSomething(String value) {
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

        //final DefaultPromise<String> promise = new DefaultPromise<>(nioEventLoopGroup.next());
        Promise<Object> promise = nioEventLoopGroup.next().newPromise();
        nioEventLoopGroup.submit(() -> {
            try {
                Thread.sleep(1000);
                //promise.setSuccess("执行成功" + value);
                //promise.trySuccess("try 成功");

                return promise.setSuccess("执行成功" + value);
            } catch (InterruptedException e) {
                promise.setFailure(e);
            }
            return promise;
        });

        return promise;
    }
}
