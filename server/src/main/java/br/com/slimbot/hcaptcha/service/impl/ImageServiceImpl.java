package br.com.slimbot.hcaptcha.service.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import br.com.slimbot.hcaptcha.service.ImageService;
import br.com.slimbot.hcaptcha.util.PastaImagemUtil;

@Service
public class ImageServiceImpl implements ImageService {

	private File pastaImagens = PastaImagemUtil.getPastaImages();

	@Override
	public File salvarImage(String base64, String titulo) {

		File pastaTitulo = new File(pastaImagens, titulo);

		if (!pastaTitulo.exists()) {
			pastaTitulo.mkdirs();
		}

		byte[] source = Base64.getDecoder().decode(base64.split(",")[1].trim());

		String name = DigestUtils.md5DigestAsHex(source).toUpperCase();

		File validDest = new File(pastaTitulo, "classificados");

		if (!validDest.exists()) {
			validDest.mkdirs();
		}

		File validFile = new File(validDest, name + ".png");

		if (validFile.exists()) {
			return validFile;
		}

		File dest = new File(pastaTitulo, name + ".png");

		try {

			FileUtils.writeByteArrayToFile(dest, source);

			return dest;

		} catch (IOException e) {
			throw new RuntimeException("Não foi possível decodificar a imagem: " + base64);
		}
	}

	@Override
	public File salvarImageRedimensionada(InputStream is, String titulo) {

		try {
			BufferedImage originalImage = ImageIO.read(is);

			BufferedImage resizedImage = new BufferedImage(64, 64, originalImage.getType());

			Graphics2D g2d = resizedImage.createGraphics();

			Image scaledImage = originalImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH);

			g2d.drawImage(scaledImage, 0, 0, null);

			g2d.dispose();

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			ImageIO.write(resizedImage, "jpg", byteArrayOutputStream);

			return salvarImage(byteArrayOutputStream.toByteArray(), titulo);

		} catch (IOException e) {

			throw new RuntimeException("Não foi possível decodificar a imagem");
		}
	}

	@Override
	public File salvarImage(byte[] source, String titulo) {

		File pastaTitulo = new File(pastaImagens, titulo);

		if (!pastaTitulo.exists()) {
			pastaTitulo.mkdirs();
		}

		String name = DigestUtils.md5DigestAsHex(source).toUpperCase() + ".png";

		File validDest = verificarDestino(name, pastaTitulo, "classificados", "validos", "invalidos");

		if (validDest != null) {

			return validDest;
		}

		File dest = new File(pastaTitulo, name);

		try {

			FileUtils.writeByteArrayToFile(dest, source);

			return dest;

		} catch (IOException e) {
			throw new RuntimeException("Não foi possível decodificar a imagem: " + name);
		}
	}

	@Override
	public File salvarImage(byte[] source, String titulo, File pasta) {

		File pastaTitulo = new File(pasta, titulo);

		if (!pastaTitulo.exists()) {
			pastaTitulo.mkdirs();
		}

		String name = DigestUtils.md5DigestAsHex(source).toUpperCase() + ".png";

		File dest = new File(pastaTitulo, name);

		if (dest.exists()) {

			return dest;
		}

		try {

			FileUtils.writeByteArrayToFile(dest, source);

			return dest;

		} catch (IOException e) {
			throw new RuntimeException("Não foi possível decodificar a imagem: " + name);
		}
	}

	private File verificarDestino(String nomeArquivo, File pastaTitulo, String... destinos) {

		for (String destino : destinos) {

			File pastaDestino = new File(pastaTitulo, destino);

			if (!pastaDestino.exists()) {
				pastaDestino.mkdirs();
			}

			File arquivoDestino = new File(pastaDestino, nomeArquivo);

			if (arquivoDestino.exists()) {

				return arquivoDestino;
			}
		}

		return null;
	}

	@Override
	public File moverClassificados(File dest) {

		File pastaTitulo = dest.getParentFile();

		File validDest = new File(pastaTitulo, "classificados");

		if (!validDest.exists()) {
			validDest.mkdirs();
		}

		File validFile = new File(validDest, dest.getName());

		try {

			FileUtils.moveFile(dest, validFile);

		} catch (IOException e) {

			throw new RuntimeException("Não foi possível mover a imagem: " + dest.getName());
		}

		return validFile;
	}

	@Override
	public void moverSemClassificacao(File dest) {
		File pastaTitulo = dest.getParentFile();

		File validFile = new File(pastaTitulo, dest.getName());

		try {

			FileUtils.moveFile(dest, validFile);

		} catch (IOException e) {

			throw new RuntimeException("Não foi possível mover a imagem: " + dest.getName());
		}
	}
}
