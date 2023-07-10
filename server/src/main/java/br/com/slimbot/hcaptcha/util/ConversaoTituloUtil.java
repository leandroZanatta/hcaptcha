package br.com.slimbot.hcaptcha.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ConversaoTituloUtil {

	private static Map<String, String> mapaBadCodes = new HashMap<>();

	static {
		mapaBadCodes.put("\u0430", "a");
		mapaBadCodes.put("\u0435", "e");
		mapaBadCodes.put("\u0456", "i");
		mapaBadCodes.put("\u03BF", "o");
		mapaBadCodes.put("\u0441", "c");
		mapaBadCodes.put("\u0501", "d");
		mapaBadCodes.put("\u0455", "s");
		mapaBadCodes.put("\u04BB", "h");
		mapaBadCodes.put("\u0443", "y");
		mapaBadCodes.put("\u0440", "p");
	}

	public static String converterTitulo(String title) {

		for (Entry<String, String> entry : mapaBadCodes.entrySet()) {

			if (title.contains(entry.getKey())) {

				title = title.replace(entry.getKey(), entry.getValue());
			}
		}

		return Arrays
				.asList(removerAcentos(title).replace("por favor,", "").replace("clique em cada imagem", "")
						.replace("que contem", "").replace("uma ", "").replace("um ", "").replace("contendo ", "")
						.replace("-", "_").replace(".", "").replace("?", "").trim().split(" "))
				.stream().collect(Collectors.joining("_"));
	}

	public static String removerAcentos(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
}
