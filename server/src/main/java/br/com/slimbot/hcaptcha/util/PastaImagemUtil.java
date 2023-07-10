package br.com.slimbot.hcaptcha.util;

import java.io.File;

public class PastaImagemUtil {

	private static File images = new File("images");

	public static File getPastaImages() {

		if (!images.exists()) {

			images.mkdirs();
		}

		return images;
	}

}
