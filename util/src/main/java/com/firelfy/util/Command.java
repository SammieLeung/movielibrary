package com.firelfy.util;

public class Command
{
	public static final Command REBOOT = new Command("reboot");

	public static final Command REBOOT_SOFT = new Command("pkill zygote");

	public static final Command REBOOT_RECOVERY = new Command("reboot recovery");

	public static final Command POWEROFF = new Command("reboot -p");
	public String input;
	public String[] output;
	
	/***
	 * exitStatus==0代表命令执行成功
	 */
	public int exitStatus;

	public Command(String input)
	{
		if (input == null)
			throw new NullPointerException(
					"Cannot use a null input for the command.");
		this.input = input;
	}
	
	public Command()
	{
		this.exitStatus = -1;
	}

	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("Command:\n");
		buffer.append("======================Shell======================\n");
		buffer.append("$"+input+"\n");
		for (String out:output) {
			buffer.append(out+"\n");
		}
		buffer.append("\nexitStatus=["+exitStatus+"]\n");
		buffer.append("=================================================\n");
		return buffer.toString();
	}

}

