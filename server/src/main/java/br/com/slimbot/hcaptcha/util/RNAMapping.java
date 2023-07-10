package br.com.slimbot.hcaptcha.util;

import java.io.File;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

public class RNAMapping {

	private static File pastaModel = getRnnaFolder();

	public static Net obterModelo(String title) {

		File arquivoOnnx = new File(pastaModel, title + ".onnx");

		if (arquivoOnnx.exists()) {

			return Dnn.readNetFromONNX(arquivoOnnx.getAbsolutePath());
		}

		return null;
	}

	private static File getRnnaFolder() {

		File images = new File("model");

		if (!images.exists()) {

			images.mkdirs();
		}

		return images;
	}
}
