package com.company.web_service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/hello")
public class HelloService {
	
	@RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
	public void hello(HttpServletRequest request, HttpServletResponse response, @PathVariable String name) throws IOException {
		response.getWriter().write("Hello " + name);
	}
	
	@RequestMapping(value = "/hello", method = RequestMethod.POST)
	public void hello2(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String name = request.getParameter("name");
		response.getWriter().write("Hello " + name);
	}
	
}
