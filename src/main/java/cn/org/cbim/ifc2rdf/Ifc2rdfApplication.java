package cn.org.cbim.ifc2rdf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {MultipartAutoConfiguration.class})
@MapperScan({"cn.org.cbim.ifc2rdf.dao"})
@EnableScheduling
public class Ifc2rdfApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ifc2rdfApplication.class, args);
    }

}
