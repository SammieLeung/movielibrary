package com.firelfy.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shell
{
	private static final String SU_COMMAND = "vm";
	private static final String ANDROID_SHELL = "sh";
	private static final String[] PATHS = { "/system/bin", "/system/xbin", 
		"/sbin", "/vendor/bin", "/system/sbin", "/system/bin/failsafe/", 
	"/data/local/" };
	private static final String COMMAND_END = "__END_SHELL_COMMAND";
	private static final int OTHER_EXIT_STATUS = -1337;
	protected ProcessBuilder builder;
	protected Process process;
	public BufferedReader stdout;
	public BufferedWriter stdin;

	public Shell()
	{
		this(ANDROID_SHELL);
	}

	public Shell(String interpreter)
	{
		this.builder = new ProcessBuilder(new String[] { interpreter });
		try {
			this.builder.redirectErrorStream(true);
			this.process = this.builder.start();
			this.stdout = new BufferedReader(new InputStreamReader(
					this.process.getInputStream()));
			this.stdin = new BufferedWriter(new OutputStreamWriter(
					this.process.getOutputStream()));

			String oldPath = exec("echo \"$PATH\"").output[0];
			LogUtil.d("Original shell path: " + oldPath);
			StringBuilder newPath = new StringBuilder();
			for (String path : PATHS) {
				if (!oldPath.contains(path)) {
					LogUtil.d("Adding new path " + path);
					newPath.append(':').append(path);
				}
			}
			exec("PATH=$PATH" + newPath);
		} catch (IOException ioe) {
			handleWriteOnClosedShell(ioe);
		}
	}

	private void handleWriteOnClosedShell(IOException ioe) {
		LogUtil.e("Attempted to write on a closed shell", ioe);
		close();
	}

	private Command writeCommand(Command command, boolean log)
	{
		if (command == null)
			throw new NullPointerException("Cannot execute a null command"); String line;
			try {
				this.stdin.write(command.input + "\n");
				this.stdin.write("echo \""+COMMAND_END+" $?\"\n");
				this.stdin.flush();

				ArrayList<String> output = new ArrayList<String>();
				line = null;
				while (((line = this.stdout.readLine()) != null) && 
						(!line.startsWith(COMMAND_END))) {
					output.add(line);
				}
				command.output = ((String[])output.toArray(new String[output.size()]));
				try {
					command.exitStatus = Integer.parseInt(line.split(" ")[1]);
				}
				catch (ArrayIndexOutOfBoundsException e) {
					LogUtil.w("Command returned an empty exit status");
					command.exitStatus = 0;
				} catch (Exception e) {
					command.exitStatus = OTHER_EXIT_STATUS;
				}
			} catch (IOException ioe) {
				handleWriteOnClosedShell(ioe);
			}

			//			if (log) {
			//				LogUtil.i("Executing command: " + command);
			//				LogUtil.d(" > input: " + command.input);
			//				if (command.output.length > 0) {
			//					LogUtil.d(" > output:");
			//					String[] arrayOfString;
			//					e = (arrayOfString = command.output).length; for (line = 0; line < e; line++) { String outLine = arrayOfString[line];
			//					LogUtil.d(" - " + outLine);
			//					}
			//				}
			//				if (command.exitStatus == 0)
			//					LogUtil.d(" > exit: " + command.exitStatus);
			//				else {
			//					LogUtil.w(" > exit: " + command.exitStatus);
			//				}
			//			}
			return command;
	}

	private Command exec(String command)
	{
		return writeCommand(new Command(command), false);
	}

	public Command execute(Command command)
	{
		return writeCommand(command, true);
	}

	public Command[] execute(Command[] commands)
	{
		for (Command c : commands) {
			writeCommand(c, true);
		}
		return commands;
	}

	public Command execute(String command)
	{
		return writeCommand(new Command(command), true);
	}

	public Command[] execute(String[] commands)
	{
		Command[] out = new Command[commands.length];
		for (int i = 0; i < commands.length; i++) {
			out[i] = writeCommand(new Command(commands[i]), true);
		}
		return out;
	}

	public Command[] tryExecute(Error.ErrorHandler errorHandler, String[] commands)
	{
		Command[] out = new Command[commands.length];
		for (int i = 0; i < commands.length; i++) {
			out[i] = writeCommand(new Command(commands[i]), true);

			if (errorHandler != null) {
				boolean shouldContinue = errorHandler.onError(out[i], i);

				if (!shouldContinue) {
					break;
				}
			}
		}
		return out;
	}

	public Command[] tryExecute(Error.ErrorHandler errorHandler, Command[] commands)
	{
		for (int i = 0; i < commands.length; i++) {
			writeCommand(commands[i], true);

			if (errorHandler != null) {
				boolean shouldContinue = errorHandler.onError(commands[i], i);

				if (!shouldContinue) {
					break;
				}
			}
		}
		return commands;
	}

	public Shell getRoot()
	{
		if (!isRootShell()) {
			LogUtil.i("Getting root");
			Command c = execute(SU_COMMAND);

			LogUtil.v("Su command exit value:" + c.exitStatus);
			if (isRootShell())
				LogUtil.i("Got root");
			else
				LogUtil.w("Couldn't get root");
		}
		else
		{
			LogUtil.w("Attempted to get root on this shell, but it was already root.");
		}
		return this;
	}

	public boolean isRootShell()
	{
		return getUID() == 0;
	}

	public int getUID()
	{
		String idOutput = exec("id").output[0];

		Matcher match = Pattern.compile("uid=([0-9]*)").matcher(idOutput);

		if (match.find()) {
			String uid = match.group(1);
			return Integer.parseInt(uid);
		}
		return -1;
	}

	public void close()
	{
		try
		{
			this.stdin.write("\n");
			for (int i = 0; i < 5; i++) {
				this.stdin.write("exit\n");
			}
			this.stdout.close();
			this.stdin.close();
			this.process.destroy();
		} catch (IOException e) {
			LogUtil.w("IOException closing shell:", e);
		}
	}

	public static abstract class Error
	{
		public static final ErrorHandler DEFAULT_HANDLER = new ErrorHandler()
		{
			public boolean onError(Command c, int index) {
				if (c.exitStatus == 0) {
					return true;
				}
				LogUtil.w("Stopping execution, exitStatus:" + c.exitStatus);
				return false;
			}
		};

		public static abstract interface ErrorHandler
		{
			public abstract boolean onError(Command paramCommand, int paramInt);
		}
	}
}
