package cn.saa.demo;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Administrator
 */
@Slf4j
@SpringBootApplication
@MapperScan("cn.saa.demo.mapper")
public class SpringAiAgentsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAgentsDemoApplication.class, args);
        log.info("start success!");
    }

}
