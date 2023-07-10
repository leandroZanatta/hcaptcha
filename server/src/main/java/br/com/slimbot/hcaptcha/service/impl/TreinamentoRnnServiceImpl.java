package br.com.slimbot.hcaptcha.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import br.com.slimbot.hcaptcha.service.TreinamentoRnnService;

@Service
public class TreinamentoRnnServiceImpl implements TreinamentoRnnService {

	private String pythonPath = "/usr/bin/python3";
	private String modelFactoryPath = "/hcaptcha-model-factory";

	private File imagesPath = new File("images");
	private File modelsPath = new File("model");

	@Override
	public void treinar(String nomeModelo, Long epochs) {

		File pastaImagensModelo = new File(imagesPath, nomeModelo);

		File pastaValidos = new File(pastaImagensModelo, "validos");
		File pastaInvalidos = new File(pastaImagensModelo, "invalidos");

		File pastaDataModelFactory = new File(modelFactoryPath, "data");
		File pastaModelModelFactory = new File(modelFactoryPath, "model");

		if (!pastaModelModelFactory.exists()) {
			pastaModelModelFactory.mkdirs();
		}
		if (!pastaDataModelFactory.exists()) {
			pastaDataModelFactory.mkdirs();
		}

		File pastaDataRna = new File(pastaDataModelFactory, nomeModelo);
		File pastaBad = criarPasta(new File(pastaDataRna, "bad"));
		File pastaYes = criarPasta(new File(pastaDataRna, "yes"));
		File pastaClass0 = criarPasta(new File(pastaDataRna, "class 0"));
		File pastaClass1 = criarPasta(new File(pastaDataRna, "class 1"));

		copiarImagens(pastaInvalidos, pastaBad);
		copiarImagens(pastaInvalidos, pastaClass1);
		copiarImagens(pastaValidos, pastaYes);
		copiarImagens(pastaValidos, pastaClass0);

		File mainPy = new File(new File(modelFactoryPath, "src"), "main.py");

		ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(new File(pythonPath).getAbsolutePath(),
				mainPy.getAbsolutePath(), "trainval", "--task=" + pastaDataRna.getName(), "--epochs=" + epochs));

		processBuilder.inheritIO();

		try {

			Process process = processBuilder.start();

			process.waitFor();

		} catch (IOException | InterruptedException e) {

			e.printStackTrace();
		}

		File modeloRnna = new File(modelsPath, nomeModelo + ".onnx");
		File modeloAtualizado = new File(new File(pastaModelModelFactory, nomeModelo), nomeModelo + ".onnx");

		if (modeloRnna.exists()) {
			modeloRnna.delete();
		}

		copyFile(modeloAtualizado, modeloRnna);
	}

	private void copiarImagens(File src, File dest) {

		for (File img : src.listFiles()) {

			File arqDest = new File(dest, img.getName());

			if (!arqDest.exists()) {
				copyFile(img, arqDest);
			}
		}
	}

	private void copyFile(File src, File dest) {
		try {
			FileUtils.copyFile(src, dest);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File criarPasta(File nomePasta) {

		if (!nomePasta.exists()) {
			nomePasta.mkdirs();
		}
		return nomePasta;
	}

}
