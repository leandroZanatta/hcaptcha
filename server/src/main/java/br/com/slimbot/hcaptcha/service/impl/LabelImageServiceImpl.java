package br.com.slimbot.hcaptcha.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.com.slimbot.hcaptcha.dto.LabelImageDTO;
import br.com.slimbot.hcaptcha.service.LabelImageService;
import br.com.slimbot.hcaptcha.util.YoloInferenceUtil;

@Service
public class LabelImageServiceImpl implements LabelImageService {

	private File pastaLabelImage = new File("label_area");
	private Gson gson = new Gson();

	@Override
	public List<String> buscarPastas() {

		return Arrays.asList(pastaLabelImage.listFiles()).stream().filter(pasta -> pasta.isDirectory())
				.map(pasta -> pasta.getName()).collect(Collectors.toList());
	}

	@Override
	public List<LabelImageDTO> buscarClassificacao(String pasta) {

		File pastaEspecifica = new File(pastaLabelImage, pasta);

		if (!pastaEspecifica.exists()) {
			throw new RuntimeException(String.format("A pasta %s não existe", pasta));
		}

		return buscarImagensPasta(pastaEspecifica);
	}

	@Override
	public void salvarClassificacao(LabelImageDTO label) {

		try {
			File arquivoCoordenadas = new File(label.getLocalizacao().replace(".png", ".coord"));

			FileUtils.writeStringToFile(arquivoCoordenadas, gson.toJson(label.getLabelImg()), StandardCharsets.UTF_8);

			salvarLabelTreinamento(label);
		} catch (IOException e) {
		}
	}

	private void salvarLabelTreinamento(LabelImageDTO label) {
		try {

			File arqImagem = new File(label.getLocalizacao());

			String nomePasta = arqImagem.getParentFile().getName();

			ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

			File arquivoYaml = new File("D:\\yolov5\\data\\hcaptcha.yaml");

			Map<String, Object> dadosYaml = objectMapper.readValue(arquivoYaml, Map.class);

			Map<Integer, String> nomes = (Map<Integer, String>) dadosYaml.get("names");

			Integer indexLabel = 0;

			if (!nomes.values().contains(nomePasta)) {

				indexLabel = nomes.keySet().stream().mapToInt(item -> item).max().getAsInt() + 1;

				nomes.put(indexLabel, nomePasta);

				objectMapper.writeValue(arquivoYaml, dadosYaml);
			} else {

				List<Integer> indexes = new ArrayList<>(nomes.keySet());

				for (int i = 0; i < indexes.size(); i++) {

					if (nomes.get(String.valueOf(i)).equals(nomePasta)) {
						indexLabel = i;
						break;
					}
				}
			}

			BufferedImage imagem = ImageIO.read(arqImagem);

			int largura = imagem.getWidth();
			int altura = imagem.getHeight();

			File coordenadas = new File("D:\\datasets\\hcaptcha\\labels\\hcaptchatrain2017",
					arqImagem.getName().replace(".png", ".txt"));
			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append(indexLabel).append(" ").append(label.getLabelImg().get(0) / largura).append(" ")
					.append(label.getLabelImg().get(1) / altura).append(" ")
					.append(label.getLabelImg().get(2) / largura).append(" ")
					.append(label.getLabelImg().get(3) / altura);

			File newArqImagem = new File("D:\\datasets\\hcaptcha\\images\\hcaptchatrain2017", arqImagem.getName());

			if (!newArqImagem.exists()) {
				FileUtils.copyFile(arqImagem, newArqImagem);
			}

			Map<Integer, String> lines = new HashMap<>();

			if (coordenadas.exists()) {
				List<String> existentes = FileUtils.readLines(coordenadas, StandardCharsets.UTF_8);

				for (String existente : existentes) {
					lines.put(Integer.valueOf(existente.split(" ")[0].trim()), existente);
				}
			}

			lines.put(indexLabel, stringBuilder.toString());

			FileUtils.writeLines(coordenadas, lines.values(), false);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<LabelImageDTO> buscarImagensPasta(File file) {

		List<LabelImageDTO> retorno = new ArrayList<>();

		if (!file.exists()) {
			file.mkdirs();
		}

		obterArquivoClassificacao(file, retorno);

		return retorno;
	}

	private void obterArquivoClassificacao(File file, List<LabelImageDTO> retorno) {

		for (File img : file.listFiles()) {

			if (img.getName().endsWith(".png")) {

				try {

					LabelImageDTO labelImageDTO = new LabelImageDTO();
					labelImageDTO.setLocalizacao(img.getAbsolutePath());
					labelImageDTO.setConteudo(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(img)));

					File arCoordenadas = new File(img.getAbsolutePath().replace(".png", ".coord"));

					if (!arCoordenadas.exists()) {
						YoloInferenceUtil.executarInferencia(img, arCoordenadas.getParentFile().getName());
					}

					if (arCoordenadas.exists()) {

						List<Double> coordenada = new ArrayList<>();

						try {
							Type type = TypeToken.getParameterized(List.class, Double.class).getType();

							List<Double> coordenadas = gson
									.fromJson(FileUtils.readFileToString(arCoordenadas, StandardCharsets.UTF_8), type);

							if (!coordenadas.isEmpty()) {
								coordenada = coordenadas;
							}

						} catch (Exception e) {
						}

						labelImageDTO.setLabelImg(coordenada);

					}

					retorno.add(labelImageDTO);

				} catch (Exception e) {
				}
			}
		}
	}

}
