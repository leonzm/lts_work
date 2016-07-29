package com.company.tool;

import org.apache.log4j.Logger;

import com.github.ltsopensource.tasktracker.logger.BizLogger;

public class BizLoggerMock implements BizLogger {
	
	private static final Logger LOG = Logger.getLogger(BizLoggerMock.class);

	@Override
	public void debug(String msg) {
		LOG.debug(msg);
	}

	@Override
	public void info(String msg) {
		LOG.info(msg);
	}

	@Override
	public void error(String msg) {
		LOG.error(msg);
	}

}
