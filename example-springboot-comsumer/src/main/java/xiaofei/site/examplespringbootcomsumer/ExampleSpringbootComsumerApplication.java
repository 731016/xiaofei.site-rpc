package xiaofei.site.examplespringbootcomsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xiaofei.site.rpc.springboot.starter.annotation.EnableRpc;

@EnableRpc(needServer = false)
@SpringBootApplication
public class ExampleSpringbootComsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringbootComsumerApplication.class, args);
    }

}
