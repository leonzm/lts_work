package com.company.tool;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Leon on 2016/12/23.
 * 邮件发送
 */
public class Tool_Email {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Tool_Email.class);
	
	private static final String HOSTNAME = "smtp.163.com";
	private static final int SMTPPORT = 465;
	private static final boolean SSLONCONNECT = true;
	private static String username = "15001848348@163.com";
	private static String password = "zbzhcxyzhc4444";

	/**
	 * 邮件发送
	 * @param to
	 * @param subject
	 * @param message
	 */
	public static void sendEmail(String to, String subject, String message) {
		try {
			Email email = new SimpleEmail();
			email.setHostName(HOSTNAME);
			email.setSmtpPort(SMTPPORT);
			email.setAuthenticator(new DefaultAuthenticator(username, password));
			email.setSSLOnConnect(SSLONCONNECT);
			email.setFrom(username);
			email.addTo(to);
			email.setSubject(subject);
			email.setMsg(message);
			email.send();
		} catch (EmailException e) {
			LOGGER.warn("发送邮件异常", e);
		}
	}
	
	public static void main(String[] args) {
		Tool_Email.sendEmail("zhaoman@daihoubang.com", "Subject Test", "A message. now: " + new java.sql.Timestamp(System.currentTimeMillis()));

	}
	
}
