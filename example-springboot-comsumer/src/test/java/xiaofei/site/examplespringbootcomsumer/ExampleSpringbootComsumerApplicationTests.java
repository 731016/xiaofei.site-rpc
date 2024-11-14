package xiaofei.site.examplespringbootcomsumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ExampleSpringbootComsumerApplicationTests {

    @Resource
    private ExampleServiceImpl exampleService;

    @Test
    void test1(){
        exampleService.test();
    }

}
