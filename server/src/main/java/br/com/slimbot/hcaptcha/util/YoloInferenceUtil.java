package br.com.slimbot.hcaptcha.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.com.slimbot.hcaptcha.vo.LabelImageVO;

public class YoloInferenceUtil {

	private static File arquivoModelo = new File("model", "yolo.onnx");
	private static Random random = new Random();

	private YoloInferenceUtil() {

	}

	public static LabelImageVO executarInferencia(File arquivoImagem, String titulo) {

		String arqImagemName = arquivoImagem.getAbsolutePath();
		File arCoordenadas = new File(arqImagemName.substring(0, arqImagemName.lastIndexOf(".")) + ".coord");

		if (arCoordenadas.exists()) {

			return montarLabelImg(arCoordenadas);
		}
		try {

			List<String> titulos = FileUtils.readLines(new File("model", "yoloTitles.txt"), StandardCharsets.UTF_8);
			int index = titulos.indexOf(titulo);

			Net yoloNet = Dnn.readNet(arquivoModelo.getAbsolutePath());
			List<String> outputLayerNames = yoloNet.getUnconnectedOutLayersNames();

			Mat image = Imgcodecs.imread(arqImagemName);

			Imgproc.resize(image, image, new Size(256, 256));

			Mat blob = Dnn.blobFromImage(image, 1.0 / 255, new Size(256, 256), new Scalar(0, 0, 0), true, false);

			yoloNet.setInput(blob);

			List<Mat> outputs = new ArrayList<Mat>();

			yoloNet.forward(outputs, outputLayerNames);

			Map<Integer, List<double[]>> shapes = new HashMap<>();

			for (Mat output : outputs) {

				int width = output.size(1);
				int height = output.size(2);

				for (int i = 0; i < width; i++) {

					List<Double> shape = new ArrayList<>();

					for (int k = 0; k < height; k++) {
						shape.add(output.get(new int[] { 0, i, k })[0]);
					}

					int indexLabel = argmax(shape.subList(4, shape.size()));

					if (shape.get(indexLabel + 4) > 0.9 && index + 1 == indexLabel) {

						shapes.putIfAbsent(indexLabel, new ArrayList<>());

						shapes.get(indexLabel).add(new double[] { shape.get(0), shape.get(1), shape.get(2),
								shape.get(3), shape.get(indexLabel + 4) });
					}
				}
			}

			if (!shapes.isEmpty()) {

				int maiorRepresentacao = argmax(shapes.get(index + 1).stream()
						.mapToDouble(item -> item[item.length - 1]).boxed().collect(Collectors.toList()));

				FileUtils.writeStringToFile(arCoordenadas,
						new Gson().toJson(shapes.get(index + 1).get(maiorRepresentacao)), StandardCharsets.UTF_8);
			}

		} catch (IOException e) {
		}

		return montarLabelImg(arCoordenadas);
	}

	private static int argmax(List<Double> array) {

		double max = array.get(0);

		int re = 0;

		for (int i = 1; i < array.size(); i++) {
			if (array.get(i) > max) {
				max = array.get(i);
				re = i;
			}
		}
		return re;
	}

	private static LabelImageVO montarLabelImg(File arCoordenadas) {

		if (arCoordenadas.exists()) {
			try {

				List<Double> coordenadas = new Gson().fromJson(
						FileUtils.readFileToString(arCoordenadas, StandardCharsets.UTF_8),
						TypeToken.getParameterized(List.class, Double.class).getType());

				if (coordenadas.size() > 0) {

					return new LabelImageVO(coordenadas.get(0).intValue(), coordenadas.get(1).intValue(), null);
				}
			} catch (IOException e) {
			}
		}

		return new LabelImageVO(random.nextInt(250 - 231 + 1) + 231, random.nextInt(250 - 200 + 1) + 200, null);
	}

}
