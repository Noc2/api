package org.polkadot.example.promise;

import com.onehilltech.promises.Promise;
import org.polkadot.api.promise.ApiPromise;
import org.polkadot.direct.IRpcFunction;
import org.polkadot.rpc.provider.ws.WsProvider;
import org.polkadot.types.type.Header;

import java.util.concurrent.atomic.AtomicReference;

public class E04_Unsubscribe {

    //static String endPoint = "wss://poc3-rpc.polkadot.io/";
    //static String endPoint = "wss://substrate-rpc.parity.io/";
    //static String endPoint = "ws://45.76.157.229:9944/";
    static String endPoint = "ws://127.0.0.1:9944";

    static void initEndPoint(String[] args) {
        if (args != null && args.length >= 1) {
            endPoint = args[0];
            System.out.println(" connect to endpoint [" + endPoint + "]");
        } else {
            System.out.println(" connect to default endpoint [" + endPoint + "]");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Create an await for the API
        //Promise<ApiPromise> ready = ApiPromise.create();
        initEndPoint(args);

        WsProvider wsProvider = new WsProvider(endPoint);

        Promise<ApiPromise> ready = ApiPromise.create(wsProvider);

        AtomicReference<IRpcFunction.Unsubscribe> unsubscribe = new AtomicReference<>();
        ready.then(api -> {
            Promise<IRpcFunction.Unsubscribe<Promise>> invoke = api.rpc().chain().function("subscribeNewHead").invoke(
                    (IRpcFunction.SubscribeCallback<Header>) (Header header) ->
                    {
                        //System.out.println("Chain is at block: " + JSON.toJSONString(header));
                        System.out.println("Chain is at block: " + header.getBlockNumber());
                    });
            return invoke;
        }).then((IRpcFunction.Unsubscribe<Promise> result) -> {
            unsubscribe.set(result);
            synchronized (unsubscribe) {
                unsubscribe.notify();
            }
            System.out.println(" init unsubscribe ");
            return null;
        })._catch((err) -> {
            err.printStackTrace();
            return Promise.value(err);
        });


        synchronized (unsubscribe) {
            unsubscribe.wait();
        }
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("do unsubscribe");
        if (unsubscribe.get() != null) {
            unsubscribe.get().unsubscribe();
        }
    }
}
