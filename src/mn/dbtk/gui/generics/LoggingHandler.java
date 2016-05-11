package mn.dbtk.gui.generics;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JTextArea;

public class LoggingHandler extends Handler{
	boolean      closed;
	JTextArea    console;
	StringBuffer output;

	public LoggingHandler() {
		closed  = false;
		console = null;
		output  = new StringBuffer();
		Logger.getGlobal().setLevel(Level.ALL);
		Logger.getGlobal().addHandler(this);
	}
	
	public void setConsole(JTextArea console) {
		this.console = console;
		console.setEditable(false);
		console.setDisabledTextColor(Color.BLACK);
		console.setLineWrap(true);
		console.setWrapStyleWord(true);
		flush();
	}
	
	public void publish(LogRecord record) {
		final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM HH:mm:ss, ");
		
		if (closed)
			return;
		
		Level  level   = record.getLevel();
		String message = record.getMessage();
		Date   date    = new Date(record.getMillis());
		String line    = dateFmt.format(date) + level.toString() + ": " + message;
		
		if(output.length() != 0)
			output.append("\r\n");
		output.append(line);
		flush();
	}

	public void flush() {
		if (console != null){
			console.setText(output.toString());
			console.setCaretPosition(output.length());
		}
	}

	public void close() throws SecurityException {
		flush();
		closed = true;
	}
}
