package org.springframework.roo.support.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.springframework.roo.support.util.Assert;

/**
 * Wraps an {@link OutputStream} and automatically passes each line to the {@link Logger}
 * when {@link OutputStream#flush()} or {@link OutputStream#close()} is called.
 * 
 * @author Ben Alex
 * @since 1.1
 *
 */
public class LoggingOutputStream extends OutputStream {
	private Level level;
	private String sourceClassName = LoggingOutputStream.class.getName();
	protected static final Logger logger = HandlerUtils.getLogger(LoggingOutputStream.class);

	private int count = 0;
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	public LoggingOutputStream(Level level) {
		Assert.notNull(level, "A logging level is required");
		this.level = level;
	}
	
	@Override
	public void write(int b) throws IOException {
		baos.write(b);
		count++;
	}
	
	@Override
	public void flush() throws IOException {
		if (count > 0) {
			String msg  = new String(baos.toByteArray());
			LogRecord record = new LogRecord(level, msg);
			record.setSourceClassName(sourceClassName);
			try {
				logger.log(record);
			} finally {
				count = 0;
				baos = new ByteArrayOutputStream();
			}
		}
	}

	@Override
	public void close() throws IOException {
		flush();
	}

	public String getSourceClassName() {
		return sourceClassName;
	}

	public void setSourceClassName(String sourceClassName) {
		this.sourceClassName = sourceClassName;
	}
}
