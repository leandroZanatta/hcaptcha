package br.com.slimbot.hcaptcha.util;

public class WidgetUtil {

	public static String randomWidgetId() {

		int length = between(10, 12);

		var result = "";
		var characters = "abcdefghijklmnopqrstuvwxyz0123456789";

		for (var i = 0; i < length; i++) {
			result += characters.charAt((int) Math.floor(Math.random() * characters.length()));
		}

		return result;
	}

	public static int between(int min, int max) {
		return (int) Math.floor(Math.random() * (max - min) + min);
	}
}
