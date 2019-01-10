package com.citi.csvvalidator.service;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosIgnore;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.citi.csvvalidator.tool.Configurations;
import com.citi.csvvalidator.tool.Validator;
/**
 * Service to validate csv files.
 *
 * @author <a href="xk98999@imcnam.ssmb.com">Kong, Xiaohui</a>
 */
@Service
public class CsvValidatorService {
	
	private static String report = "";
	private static Properties properties = new Properties();
	private static final String serverAddr = "localhost:8848";
	
	@NacosInjected
	private ConfigService configService;

	public CsvValidatorService() throws NacosException {
		properties.put("serverAddr", serverAddr);
		configService = NacosFactory.createConfigService(properties);
		//add listener to config
		configService.addListener("com.citi.csv.config", "DEFAULT_GROUP", new Listener() {
			@Override
			public void receiveConfigInfo(String configInfo) {
				System.out.println("recieve config:" + configInfo);
			}

			@Override
			public Executor getExecutor() {
				return null;
			}
		});
		//add listener to schema
		configService.addListener("com.citi.csv.schema", "DEFAULT_GROUP", new Listener() {
			@Override
			public void receiveConfigInfo(String configInfo) {
				System.out.println("recieve schema:" + configInfo);
			}

			@Override
			public Executor getExecutor() {
				return null;
			}
		});
	}
	
	public String demoGetConfig(String dataId, String group) throws IOException {
		// Get config from nacos server
		String content = "";
		try {
			configService = NacosFactory.createConfigService(properties);
			content = configService.getConfig(dataId, group, 5000);
		} catch (NacosException e) {
			// print error message
			System.out.println("GetConfig error: "+e.toString());
		}
		
		return content;
	}

	/*
	 * Validated function
	 * @see Configurations
	 * @see Validator
	 */
	public void CsvService() throws Exception {
		
		report = "";
		String config = demoGetConfig("com.citi.csv.config", "DEFAULT_GROUP");
		String schema = demoGetConfig("com.citi.csv.schema", "DEFAULT_GROUP");
		//Check config and load schema.
		if(!Configurations.START_UP(config, schema)){
			return;
		}	
		//Build configurations.
		Validator validator = new Validator.Builder().buildWithConfigurations();
		//Start to validate.
		validator.validate();
	}

	public static void setReport(String extractStr) {
		report += extractStr;
	}

	public static String getReport() {
		return report;
	}
}
