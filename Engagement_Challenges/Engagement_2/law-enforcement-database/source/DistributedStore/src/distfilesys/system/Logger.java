package distfilesys.system;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 * 
 * @author Calebe de Paula Bianchini
 * 
 */
public class Logger {

	protected static java.util.logging.Logger logger = null;

	private static void create() {
		try {
			FileHandler file;
			file = new FileHandler("distfilesys.log", 131072, 1, true);
			file.setFormatter(new SimpleFormatter());
			logger = java.util.logging.Logger.getLogger("distfilesys.system");
			logger.addHandler(file);
		} catch (Exception e) {
			java.lang.System.out.println("Erro ao iniciar arquivo de log");
		}
	}

	public static void severe(String message, Throwable ex) {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(array);
		ex.printStackTrace(print);
		severe(message + "\n" + ex.getMessage() + "\n" + array.toString());
	}

	public static void severe(String message) {
		log(Level.SEVERE, message);
	}

	public static void warning(String message) {
		log(Level.WARNING, message);
	}

	public static void info(String message) {
		log(Level.INFO, message);
	}

	private static void log(Level level, String message) {
		if (logger == null)
			create();
		StackTraceElement stack = (new Throwable()).getStackTrace()[2];
		logger
				.logp(level, stack.getClassName(), stack.getMethodName(),
						message);
	}
}
