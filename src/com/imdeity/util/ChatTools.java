package com.imdeity.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;

public class ChatTools {
    public static final String Black = "\u00A70";
    public static final String Navy = "\u00A71";
    public static final String Green = "\u00A72";
    public static final String Blue = "\u00A73";
    public static final String Red = "\u00A74";
    public static final String Purple = "\u00A75";
    public static final String Gold = "\u00A76";
    public static final String LightGray = "\u00A77";
    public static final String Gray = "\u00A78";
    public static final String DarkPurple = "\u00A79";
    public static final String LightGreen = "\u00A7a";
    public static final String LightBlue = "\u00A7b";
    public static final String Rose = "\u00A7c";
    public static final String LightPurple = "\u00A7d";
    public static final String Yellow = "\u00A7e";
    public static final String White = "\u00A7f";
	public static final int lineLength = 54;

	public static List<String> listArr(Object[] args) {
		return list(Arrays.asList(args));
	}
	
	public static List<String> listArr(Object[] args, String prefix) {
		return list(Arrays.asList(args), prefix);
	}

	@SuppressWarnings("rawtypes")
	public static List<String> list(List args) {
		return list(args, "");
	}
	
	@SuppressWarnings("rawtypes")
	public static List<String> list(List args, String prefix) {
		if (args.size() > 0) {
			String line = "";
			for (int i = 0; i < args.size() - 1; i++)
				line += args.get(i) + ", ";
			line += args.get(args.size() - 1).toString();

			return color(prefix + line);
		}

		return new ArrayList<String>();
	}

	public static List<String> wordWrap(String[] tokens) {
		List<String> out = new ArrayList<String>();
		out.add("");

		for (String s : tokens) {
			if (stripColour(out.get(out.size() - 1)).length() + stripColour(s).length() + 1 > lineLength)
				out.add("");
			out.set(out.size() - 1, out.get(out.size() - 1) + s + " ");
		}

		return out;
	}

	public static List<String> color(String line) {
		List<String> out = wordWrap(line.split(" "));

		String c = "f";
		for (int i = 0; i < out.size(); i++) {
			if (!out.get(i).startsWith("¤") && !c.equalsIgnoreCase("f"))
				out.set(i, "¤" + c + out.get(i));

			for (int index = 0; index < lineLength; index++)
				try {
					if (out.get(i).substring(index, index + 1).equalsIgnoreCase("\u00A7"))
						c = out.get(i).substring(index + 1, index + 2);
				} catch (Exception e) {
				}
		}

		return out;
	}

	public static String stripColour(String s) {
		String out = "";
		for (int i = 0; i < s.length() - 1; i++) {
			String c = s.substring(i, i + 1);
			if (c.equals("¤"))
				i += 1;
			else
				out += c;
		}
		return out;
	}

	public static String formatTitle(String title) {
		String line = ".oOo.__________________________________________________.oOo.";
		int pivot = line.length() / 2;
		String center = ".[ " + Yellow + title + Gold + " ].";
		String out = Gold + line.substring(0, pivot - center.length() / 2);
		out += center + line.substring(pivot + center.length() / 2);
		return out;
	}

	public static String formatCommand(String requirement, String command, String subCommand, String help) {
		String out = "  ";
		if (requirement.length() > 0)
			out += Rose + requirement + ": ";
		out += Blue + command;
		if (subCommand.length() > 0)
			out += " " + LightBlue + subCommand;
		if (help.length() > 0)
			out += " " + LightGray + " : " + help;
		return out;
	}
	
	public static String formatUserList(String user, String winnings) {
        return (White + user + LightGray+" won " + Yellow + winnings + ".00 "+LightGray+"Dei");
    }

	public static String strip(String line) {
        for (ChatColor cc : ChatColor.values())
            line.replaceAll(cc.toString(), "");
        return line;
    }
}
