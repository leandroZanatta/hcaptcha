package br.com.slimbot.hcaptcha.service;

import java.io.File;
import java.io.InputStream;

public interface ImageService {

	public File salvarImage(String base64, String titulo);

	public File salvarImage(byte[] source, String titulo);

	public File salvarImage(byte[] source, String titulo, File pasta);

	public File moverClassificados(File dest);

	public void moverSemClassificacao(File localizacao);

	public File salvarImageRedimensionada(InputStream is, String string);
}
