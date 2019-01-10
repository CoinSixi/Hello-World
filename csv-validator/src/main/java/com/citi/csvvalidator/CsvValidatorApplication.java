package com.citi.csvvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
/**
 * Provide service to validate csv files.
 *
 * @author <a href="xk98999@imcnam.ssmb.com">Kong, Xiaohui</a>
 */
@SpringBootApplication
@NacosPropertySource(dataId = "com.citi.csv.config", autoRefreshed = true)
@NacosPropertySource(dataId = "com.citi.csv.schema", autoRefreshed = true)
public class CsvValidatorApplication {
	public static void main(String[] args) throws NacosException {
		SpringApplication.run(CsvValidatorApplication.class, args);
		String serviceName = "csvValidator.services";
	    //NamingService naming = NamingFactory.createNamingService("localhost:8848");
	    //naming.deregisterInstance(serviceName,"127.0.0.1",8085);
	    //naming.registerInstance(serviceName,"127.0.0.1",8085);
	}
}
