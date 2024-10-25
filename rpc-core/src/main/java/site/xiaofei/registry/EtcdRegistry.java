package site.xiaofei.registry;

import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/10/25
 */
public class EtcdRegistry {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /// create client using endpoints
        Client client = Client.builder().endpoints("http://localhost:2379").build();

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

// put the key-value
        kvClient.put(key, value).get();

// get the CompletableFuture
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

// get the value from CompletableFuture
        GetResponse response = getFuture.get();
        List<KeyValue> kvs = response.getKvs();
        System.out.println(kvs);
//        System.out.println(kvs);

// delete the key
//        kvClient.delete(key).get();

        Lease leaseClient = client.getLeaseClient();
//        leaseClient.keepAlive()


        Watch watchClient = client.getWatchClient();
        watchClient.watch(ByteSequence.from("test_key".getBytes()),(item)->{
            System.out.println("key:test_key,发生变化");
            System.out.println(item);
        });

        kvClient.delete(key).get();

    }
}
