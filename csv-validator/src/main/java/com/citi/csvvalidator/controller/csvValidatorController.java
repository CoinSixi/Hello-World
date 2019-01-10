package com.citi.csvvalidator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.citi.csvvalidator.service.CsvValidatorService;
/*
 * @author <a href="xk98999@imcnam.ssmb.com">Kong, Xiaohui</a>
 */
@RestController
public class csvValidatorController {
	
	@Autowired
	private CsvValidatorService service;

	@RequestMapping("/")
	public String HelloWorld(){
		
		return "hi";
	}
/*
	@RequestMapping(value = "/write", method = RequestMethod.GET)
	@ResponseBody
	public String write() throws NacosException, InterruptedException {
		String config = service.demoWriteConfig("com.citi.csv.config", "DEFAULT_GROUP");
		String schema = service.demoWriteSchema("com.citi.csv.schema", "DEFAULT_GROUP");
		return config+schema;
	}

	@RequestMapping(value = "/read", method = RequestMethod.GET)
	@ResponseBody
	public String post() throws NacosException, IOException {
		String config = service.demoGetConfig("com.citi.csv.config", "DEFAULT_GROUP");
		String schema = service.demoGetConfig("com.citi.csv.schema", "DEFAULT_GROUP");
		return config+schema;
	}*/
	@RequestMapping(value = "/csv", method = RequestMethod.GET)
	@ResponseBody
	public String csvValidator() throws Exception {
		service.CsvService();
		return CsvValidatorService.getReport();
	}
}
