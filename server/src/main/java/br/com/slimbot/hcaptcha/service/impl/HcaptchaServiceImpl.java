package br.com.slimbot.hcaptcha.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import br.com.slimbot.hcaptcha.dto.CaptchaDTO;
import br.com.slimbot.hcaptcha.dto.CaptchaImageDTO;
import br.com.slimbot.hcaptcha.dto.DadosCaptchaDTO;
import br.com.slimbot.hcaptcha.service.HcaptchaApiService;
import br.com.slimbot.hcaptcha.service.HcaptchaHswService;
import br.com.slimbot.hcaptcha.service.HcaptchaService;
import br.com.slimbot.hcaptcha.service.ImageService;
import br.com.slimbot.hcaptcha.util.ConversaoTituloUtil;
import br.com.slimbot.hcaptcha.util.RNAMapping;
import br.com.slimbot.hcaptcha.util.WidgetUtil;
import br.com.slimbot.hcaptcha.util.YoloInferenceUtil;
import br.com.slimbot.hcaptcha.vo.HCaptchaBinaryVO;
import br.com.slimbot.hcaptcha.vo.HCaptchaImageVO;
import br.com.slimbot.hcaptcha.vo.HCaptchaMultipleChoiceVO;
import br.com.slimbot.hcaptcha.vo.HcaptchaConfigVO;
import br.com.slimbot.hcaptcha.vo.LabelImageVO;
import br.com.slimbot.hcaptcha.vo.SiteConfigVO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HcaptchaServiceImpl implements HcaptchaService {

	@Autowired
	private ImageService imageService;

	@Autowired
	private HcaptchaApiService hcaptchaApiService;

	@Autowired
	private HcaptchaHswService hcaptchaHswService;

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	@Override
	public String resolve(DadosCaptchaDTO dadosCaptcha) {

		log.info("Iniciando Resolução de Captcha: {}", simpleDateFormat.format(new Date()));

		SiteConfigVO siteConfig = obterDadosApi(dadosCaptcha);

		while (true) {

			String task = obterTask(dadosCaptcha, siteConfig, obterAutorizacao(siteConfig.getC()));

			JsonObject taskObject = new Gson().fromJson(task, JsonElement.class).getAsJsonObject();

			if (taskObject.has("tasklist")) {

				String type = taskObject.get("request_type").getAsString();

				String retorno = null;

				switch (type) {
				case "image_label_area_select":
					retorno = resolverLabelArea(taskObject, dadosCaptcha, siteConfig);
					break;
				case "image_label_multiple_choice":
					retorno = resolverMultipleChoice(taskObject, dadosCaptcha, siteConfig);
					break;
				case "image_label_binary":
					retorno = resolverLabelBinary(taskObject, dadosCaptcha, siteConfig);
					break;
				default:
					throw new RuntimeException("Tipo de resolução não implementada");
				}

				if (retorno != null) {
					return retorno;
				}
			}
		}
	}

	private String resolverLabelArea(JsonObject taskObject, DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig) {
		try {
			String titulo = ConversaoTituloUtil.converterTitulo(
					taskObject.get("requester_question").getAsJsonObject().get("pt").getAsString().toLowerCase());

			String entityType = new ArrayList<>(
					taskObject.get("requester_restricted_answer_set").getAsJsonObject().keySet()).get(0);

			log.info("Task Atual: {}, Tipo: image_label_area_select", titulo);

			JsonArray images = taskObject.get("tasklist").getAsJsonArray();

			File pastaLabelArea = new File("label_area");

			List<LabelImageVO> labelImageVOs = new ArrayList<>();

			for (JsonElement image : images) {

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				IOUtils.copy(new URL(image.getAsJsonObject().get("datapoint_uri").getAsString()).openStream(), bos);

				File dest = imageService.salvarImage(bos.toByteArray(), titulo, pastaLabelArea);

				LabelImageVO labelImageVO = YoloInferenceUtil.executarInferencia(dest, dest.getParentFile().getName());

				labelImageVO.setUuid(image.getAsJsonObject().get("task_key").getAsString());

				labelImageVOs.add(labelImageVO);
			}

			try {
				Thread.sleep(WidgetUtil.between(2000, 5000));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			String taskValue = hcaptchaApiService.resolverTaskMotion(taskObject.get("key").getAsString(), entityType,
					labelImageVOs, dadosCaptcha, siteConfig, obterAutorizacao(siteConfig.getC()));

			log.info("Conseguiu Resolver: {}", taskValue != null ? "Sim" : "Não");

			return taskValue;

		} catch (Exception e) {

			log.error("Não foi possível resolver image_label_area_select", e);
		}

		return null;
	}

	private String resolverMultipleChoice(JsonObject taskObject, DadosCaptchaDTO dadosCaptcha,
			SiteConfigVO siteConfig) {

		try {
			File pastaMultipleChoice = new File("multiple_choice");

			JsonObject tasks = taskObject.get("requester_restricted_answer_set").getAsJsonObject();

			log.info("Task Atual: Multiplas Opções, Tipo: image_label_multiple_choice");

			Set<String> taskNames = tasks.keySet();
			Map<String, String> mapaTitulos = new HashMap<>();

			for (String taskName : taskNames) {

				JsonObject taskObjectName = tasks.get(taskName).getAsJsonObject();
				String dataImage = taskObjectName.get("answer_example_uri").getAsString();
				String titulo = ConversaoTituloUtil
						.converterTitulo(taskObjectName.get("pt-BR").getAsString().toLowerCase());

				mapaTitulos.put(taskName, titulo);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				IOUtils.copy(new URL(dataImage).openStream(), bos);

				imageService.salvarImage(bos.toByteArray(), titulo, pastaMultipleChoice);
			}

			List<HCaptchaMultipleChoiceVO> imagens = new ArrayList<>();

			JsonArray images = taskObject.get("tasklist").getAsJsonArray();

			for (JsonElement image : images) {

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				IOUtils.copy(new URL(image.getAsJsonObject().get("datapoint_uri").getAsString()).openStream(), bos);

				File dest = imageService.salvarImage(bos.toByteArray(), "multiple_choice",
						pastaMultipleChoice.getParentFile());

				HCaptchaMultipleChoiceVO captchaImageVO = new HCaptchaMultipleChoiceVO();

				captchaImageVO.setUuid(image.getAsJsonObject().get("task_key").getAsString());
				captchaImageVO.setChoice(getChoice(dest, mapaTitulos));

				imagens.add(captchaImageVO);
			}

			try {
				Thread.sleep(WidgetUtil.between(2000, 5000));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			String taskValue = hcaptchaApiService.resolverTaskMultipleChoice(taskObject.get("key").getAsString(),
					imagens, dadosCaptcha, siteConfig, obterAutorizacao(siteConfig.getC()));

			log.info("Conseguiu Resolver: {}", taskValue != null ? "Sim" : "Não");

		} catch (Exception e) {

			log.error("Não foi possível resolver image_label_binary", e);
		}

		return null;
	}

	private String getChoice(File dest, Map<String, String> mapaTitulos) throws IOException {

		for (Entry<String, String> entry : mapaTitulos.entrySet()) {

			File newDest = new File(new File(dest.getParent(), entry.getValue()), dest.getName());

			if (newDest.exists()) {

				dest.delete();

				return entry.getKey();
			}

			Net net = RNAMapping.obterModelo(entry.getValue());

			if (net != null && aplicarRedeResnet(net, dest.getAbsolutePath())) {

				if (!newDest.exists()) {
					FileUtils.moveFile(dest, newDest);
				}

				return entry.getKey();
			}
		}

		List<String> titulos = new ArrayList<>(mapaTitulos.keySet());

		Collections.shuffle(titulos);

		return titulos.get(0);
	}

	private String resolverLabelBinary(JsonObject taskObject, DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig) {
		try {

			HCaptchaBinaryVO hCaptchaVO = new HCaptchaBinaryVO();
			hCaptchaVO.setTitulo(ConversaoTituloUtil.converterTitulo(
					taskObject.get("requester_question").getAsJsonObject().get("pt").getAsString().toLowerCase()));
			hCaptchaVO.setKey(taskObject.get("key").getAsString());

			JsonArray images = taskObject.get("tasklist").getAsJsonArray();

			images.forEach(image -> {
				HCaptchaImageVO captchaImageVO = new HCaptchaImageVO();
				captchaImageVO.setImagem(image.getAsJsonObject().get("datapoint_uri").getAsString());
				captchaImageVO.setUuid(image.getAsJsonObject().get("task_key").getAsString());

				hCaptchaVO.getImagens().add(captchaImageVO);
			});

			log.info("Task Atual: {}, Tipo: image_label_binary", hCaptchaVO.getTitulo());

			executarClassificacao(hCaptchaVO);

			try {
				Thread.sleep(WidgetUtil.between(2000, 5000));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			String taskValue = hcaptchaApiService.resolverTaskClassificacao(hCaptchaVO, dadosCaptcha, siteConfig,
					obterAutorizacao(siteConfig.getC()));

			log.info("Conseguiu Resolver: {}", taskValue != null ? "Sim" : "Não");

			List<HCaptchaImageVO> dadosImagem = hCaptchaVO.getImagens();

			for (HCaptchaImageVO imagem : dadosImagem) {

				if (imagem.getLocalizacao().getParent().equals("classificados")) {
					imageService.moverSemClassificacao(imagem.getLocalizacao());
				}
			}

			if (taskValue != null) {

				log.info("Resolução de Captcha Concluída: {}", simpleDateFormat.format(new Date()));

				return taskValue;
			}

		} catch (Exception e) {

			log.error("Não foi possível resolver image_label_binary", e);
		}

		return null;
	}

	private void executarClassificacao(HCaptchaBinaryVO task) {
		String titulo = ConversaoTituloUtil.converterTitulo(task.getTitulo().toLowerCase());

		Net net = RNAMapping.obterModelo(titulo);

		for (HCaptchaImageVO captchaImagem : task.getImagens()) {

			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				IOUtils.copy(new URL(captchaImagem.getImagem()).openStream(), bos);

				File dest = imageService.salvarImage(bos.toByteArray(), titulo);

				if (dest.getParentFile().getName().equals("classificados")
						|| dest.getParentFile().getName().equals("validos")) {

					captchaImagem.setValido(true);
					captchaImagem.setLocalizacao(dest);

					continue;
				}

				if (dest.getParentFile().getName().equals("invalidos")) {
					captchaImagem.setLocalizacao(dest);

					continue;
				}

				if (net != null && aplicarRedeResnet(net, dest.getAbsolutePath())) {

					captchaImagem.setValido(true);

					if (!dest.getParentFile().getName().equals("classificados")) {

						dest = imageService.moverClassificados(dest);
					}
				}

				captchaImagem.setLocalizacao(dest);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String obterTask(DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig, String autorizacao) {

		try {

			return hcaptchaApiService.obterTask(dadosCaptcha, siteConfig, autorizacao);

		} catch (Exception e) {

			throw new RuntimeException("Não foi possível obter autorização para executar captcha", e);
		}
	}

	private String obterAutorizacao(HcaptchaConfigVO hcaptchaConfigVO) {

		try {

			return hcaptchaHswService.obterHSW(hcaptchaConfigVO);

		} catch (Exception e) {

			throw new RuntimeException("Não foi possível obter autorização para executar captcha", e);
		}
	}

	private SiteConfigVO obterDadosApi(DadosCaptchaDTO dadosCaptcha) {

		try {

			return hcaptchaApiService.obterSiteConfig(dadosCaptcha);

		} catch (Exception e) {

			throw new RuntimeException("Não foi possível obter os dados de configuração", e);
		}
	}

	@Override
	public List<String> resolverCaptcha(CaptchaDTO captchaDTO) {
		List<String> uuidsValidos = new ArrayList<String>();

		String titulo = ConversaoTituloUtil.converterTitulo(captchaDTO.getTitulo().toLowerCase());
		Map<String, File> mapaImagens = new HashMap<String, File>();

		for (CaptchaImageDTO captchaImagem : captchaDTO.getImagens()) {

			mapaImagens.put(captchaImagem.getUuid(), imageService.salvarImage(captchaImagem.getImagem(), titulo));
		}

		Net net = RNAMapping.obterModelo(titulo);

		mapaImagens.forEach((key, imagem) -> {
			if (aplicarRedeResnet(net, imagem.getAbsolutePath())) {
				uuidsValidos.add(key);
			}
		});

		return uuidsValidos;
	}

	private boolean aplicarRedeResnet(Net net, String filePath) {

		return foward(net, filePath).get(0, 0)[0] > 1;
	}

	private Mat foward(Net net, String filePath) {

		Size frame_size = new Size(64, 64);
		Scalar mean = new Scalar(0, 0, 0);

		Mat img = Imgcodecs.imread(filePath);

		Mat resizeimage = new Mat();

		Imgproc.resize(img, resizeimage, frame_size);

		img = null;

		Mat blob = Dnn.blobFromImage(resizeimage, 1.0 / 255, new Size(64, 64), mean, true, false);

		net.setInput(blob);

		return net.forward();
	}

}
