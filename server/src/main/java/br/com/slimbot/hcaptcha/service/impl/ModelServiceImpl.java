package br.com.slimbot.hcaptcha.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.com.slimbot.hcaptcha.dto.ClassificacaoDTO;
import br.com.slimbot.hcaptcha.dto.ModelDTO;
import br.com.slimbot.hcaptcha.dto.ReclassificacaoDTO;
import br.com.slimbot.hcaptcha.service.ModelService;
import br.com.slimbot.hcaptcha.service.TreinamentoRnnService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

	@Autowired
	private TreinamentoRnnService treinamentoRnnService;

	private File pastaModelo = new File("model");
	private File pastaImagens = new File("images");
	private Gson gson = new Gson();

	@Override
	public void salvarModel(ModelDTO modelDTO) {

		try {

			if (!pastaModelo.exists()) {
				pastaModelo.mkdirs();
			}

			File arquivoModelo = new File(pastaModelo, modelDTO.getName());
			byte[] fileBytes = Base64.getDecoder().decode(modelDTO.getData());

			FileUtils.writeByteArrayToFile(arquivoModelo, fileBytes);

		} catch (IOException e) {
			throw new RuntimeException("Não foi possível salvar o modelo");
		}
	}

	@Override
	public List<String> buscarPastaImagens() {

		return Arrays.asList(pastaImagens.listFiles()).stream().filter(pasta -> pasta.isDirectory())
				.map(pasta -> pasta.getName()).collect(Collectors.toList());
	}

	@Override
	public void limparImagens() {

		Arrays.asList(pastaImagens.listFiles()).forEach(pastaImagem -> {

			if (pastaImagem.isDirectory()) {

				Arrays.asList(pastaImagem.listFiles()).forEach(imagem -> {
					if (!imagem.isDirectory()) {
						try {
							FileUtils.delete(imagem);
						} catch (IOException e) {
							log.info("Não foi possível deletar a imagem", e);
						}
					}
				});

				try {
					FileUtils.deleteDirectory(pastaImagem);
				} catch (IOException e) {
					log.info("Não foi possível deletar o diretório {}", pastaImagem.getName(), e);
				}
			}
		});
	}

	@Override
	public List<String> buscarPastasImagensSemModelo() {

		return Arrays.asList(pastaImagens.listFiles()).stream()
				.filter(pasta -> pasta.isDirectory() && !new File(pastaModelo, pasta.getName() + ".onnx").exists())
				.map(pasta -> pasta.getName()).collect(Collectors.toList());
	}

	@Override
	public List<ClassificacaoDTO> buscarClassificacao(String pasta, String subpasta) {

		File pastaEspecifica = new File(pastaImagens, pasta);

		if (!pastaEspecifica.exists()) {
			throw new RuntimeException(String.format("A pasta %s não existe", pasta));
		}

		File arquivoSubPasta = this.obterSubPasta(pastaEspecifica, subpasta);

		return buscarImagensPasta(arquivoSubPasta);

	}

	private File obterSubPasta(File pastaEspecifica, String subpasta) {

		if (subpasta.equals("./")) {
			return pastaEspecifica;
		}

		return new File(pastaEspecifica, subpasta);
	}

	private List<ClassificacaoDTO> buscarImagensPasta(File file) {

		List<ClassificacaoDTO> retorno = new ArrayList<>();

		if (!file.exists()) {
			file.mkdirs();
		}

		obterArquivoClassificacao(file, retorno);

		return retorno;
	}

	private void obterArquivoClassificacao(File file, List<ClassificacaoDTO> retorno) {

		for (File img : file.listFiles()) {

			if (img.getName().endsWith(".png")) {

				try {

					ClassificacaoDTO classificacaoDTO = new ClassificacaoDTO();
					classificacaoDTO.setLocalizacao(img.getAbsolutePath());
					classificacaoDTO
							.setConteudo(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(img)));

					File arCoordenadas = new File(img.getAbsolutePath().replace(".png", ".coord"));

					if (arCoordenadas.exists()) {

						Type type = TypeToken.getParameterized(List.class,
								TypeToken.getParameterized(List.class, Double.class).getType()).getType();

						classificacaoDTO.setLabelImg(
								gson.fromJson(FileUtils.readFileToString(arCoordenadas, StandardCharsets.UTF_8), type));

					}

					retorno.add(classificacaoDTO);

				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public byte[] buscarImagens(String pasta) {

		File pastaEspecifica = new File(pastaImagens, pasta);

		if (!pastaEspecifica.exists()) {
			throw new RuntimeException(String.format("A pasta %s não existe", pasta));
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zipOut = new ZipOutputStream(baos);

			zipFile(pastaEspecifica, pastaEspecifica.getName(), zipOut);

			zipOut.close();
			byte[] zipBytes = baos.toByteArray();
			baos.close();

			return zipBytes;

		} catch (Exception e) {
			throw new RuntimeException("Não foi possível zipar a pasta de imagens");
		}
	}

	private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws Exception {
		if (fileToZip.isHidden()) {
			return;
		}

		if (fileToZip.isDirectory()) {
			if (fileName.endsWith(File.separator)) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + File.separator));
				zipOut.closeEntry();
			}

			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + File.separator + childFile.getName(), zipOut);
			}
			return;
		}

		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}

		fis.close();
	}

	@Override
	public void reclassificar(String pasta, ReclassificacaoDTO reclassificacaoDTO) {

		File pastaTitulo = new File(pastaImagens, pasta);

		File pastaInserir = reclassificacaoDTO.getPastaClassificar().equals("./") ? pastaTitulo
				: new File(pastaTitulo, reclassificacaoDTO.getPastaClassificar());

		if (!pastaInserir.exists()) {
			pastaInserir.mkdirs();
		}

		for (String strImagem : reclassificacaoDTO.getImagens()) {

			File arqImagem = new File(strImagem);

			File novaImagem = new File(pastaInserir, arqImagem.getName());

			try {
				FileUtils.moveFile(arqImagem, novaImagem);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void treinar(String pasta, Long epochs) {

		treinamentoRnnService.treinar(pasta, epochs);
	}

}
