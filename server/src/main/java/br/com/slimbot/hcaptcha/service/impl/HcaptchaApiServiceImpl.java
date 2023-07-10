package br.com.slimbot.hcaptcha.service.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import br.com.slimbot.hcaptcha.dto.DadosCaptchaDTO;
import br.com.slimbot.hcaptcha.service.HcaptchaApiService;
import br.com.slimbot.hcaptcha.util.WidgetUtil;
import br.com.slimbot.hcaptcha.vo.HCaptchaBinaryVO;
import br.com.slimbot.hcaptcha.vo.HCaptchaImageVO;
import br.com.slimbot.hcaptcha.vo.HCaptchaMultipleChoiceVO;
import br.com.slimbot.hcaptcha.vo.LabelImageVO;
import br.com.slimbot.hcaptcha.vo.ResponseVO;
import br.com.slimbot.hcaptcha.vo.SiteConfigVO;
import br.com.slimbot.hcaptcha.vo.data.HcaptchaTaskData;
import br.com.slimbot.hcaptcha.vo.data.MotionData;
import br.com.slimbot.hcaptcha.vo.data.PrevData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class HcaptchaApiServiceImpl implements HcaptchaApiService {

	private String versao = null;
	private Dispatcher dispatcher = new Dispatcher();
	private OkHttpClient client;
	private static Random random = new Random();

	public HcaptchaApiServiceImpl() {

		dispatcher.setMaxRequests(1);

		client = new OkHttpClient.Builder().connectTimeout(10L, TimeUnit.SECONDS).writeTimeout(10L, TimeUnit.SECONDS)
				.readTimeout(60L, TimeUnit.SECONDS).dispatcher(dispatcher).build();
	}

	@Override
	public SiteConfigVO obterSiteConfig(DadosCaptchaDTO dadosCaptcha) throws Exception {

		Request request = new Request.Builder()
				.url(String.format("https://hcaptcha.com/checksiteconfig?v=%s&host=%s&sitekey=%s&sc=1&swa=1",
						this.getVersao(), dadosCaptcha.getHost(), dadosCaptcha.getSiteKey()))
				.header("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"")
				.header("Accept", "application/json").header("Content-Type", "text/plain")
				.header("sec-ch-ua-mobile", "?0")
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
				.header("sec-ch-ua-platform", "\"Windows\"").header("Sec-Fetch-Site", "same-site")
				.header("Sec-Fetch-Mode", "cors").header("Sec-Fetch-Dest", "empty").header("host", "hcaptcha.com")
				.build();

		return new Gson().fromJson(dispatchRequest(request).body().string(), SiteConfigVO.class);
	}

	@Override
	public String obterTask(DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig, String autorizacao)
			throws Exception {

		MotionData motionData = new MotionData();
		motionData.setHref(dadosCaptcha.getHost());
		motionData.setPrev(new PrevData());

		String widget = WidgetUtil.randomWidgetId();

		motionData.setWidgetId(widget);
		motionData.getWidgetList().add(widget);

		HcaptchaTaskData hcaptchaTaskData = new HcaptchaTaskData();
		hcaptchaTaskData.setC(new Gson().toJson(siteConfig.getC()));
		hcaptchaTaskData.setHost(dadosCaptcha.getHost());
		hcaptchaTaskData.setN(autorizacao);
		hcaptchaTaskData.setSitekey(dadosCaptcha.getSiteKey());
		hcaptchaTaskData.setV(this.versao);
		hcaptchaTaskData.setMotionData(new Gson().toJson(motionData));

		RequestBody body = new FormBody.Builder().add("v", hcaptchaTaskData.getV())
				.add("sitekey", hcaptchaTaskData.getSitekey()).add("host", hcaptchaTaskData.getHost())
				.add("hl", hcaptchaTaskData.getHl()).add("c", hcaptchaTaskData.getC()).add("n", hcaptchaTaskData.getN())
				.add("motionData", hcaptchaTaskData.getMotionData()).build();

		Request request = new Request.Builder()
				.url(String.format("https://hcaptcha.com/getcaptcha/%s", dadosCaptcha.getSiteKey()))
				.method("POST", body)
				.addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"")
				.addHeader("Accept", "application/json").addHeader("Content-type", "application/x-www-form-urlencoded")
				.addHeader("sec-ch-ua-mobile", "?0")
				.addHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
				.addHeader("sec-ch-ua-platform", "\"Windows\"").addHeader("Sec-Fetch-Site", "same-site")
				.addHeader("Sec-Fetch-Mode", "cors").addHeader("Sec-Fetch-Dest", "empty")
				.addHeader("host", "hcaptcha.com").build();

		try (Response response = dispatchRequest(request)) {

			if (response.code() == 200) {

				return response.body().string();
			}

			throw new RuntimeException("Task Hcaptcha falhou ");
		}
	}

	@Override
	public String resolverTaskMultipleChoice(String key, List<HCaptchaMultipleChoiceVO> imagens,
			DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig, String autorizacao) throws Exception {

		JsonObject answers = new JsonObject();

		for (HCaptchaMultipleChoiceVO image : imagens) {

			JsonArray item = new JsonArray();
			item.add(image.getChoice());

			answers.add(image.getUuid(), item);
		}

		Gson gson = new Gson();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("v", versao);
		jsonObject.addProperty("job_mode", "image_label_multiple_choice");
		jsonObject.add("answers", answers);
		jsonObject.addProperty("serverdomain", dadosCaptcha.getHost());
		jsonObject.addProperty("sitekey", dadosCaptcha.getSiteKey());
		jsonObject.addProperty("motionData", gson.toJson(gson.fromJson(motionData(), JsonObject.class)));
		jsonObject.addProperty("c", gson.toJson(siteConfig.getC()));
		jsonObject.addProperty("n", autorizacao);

		MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");

		RequestBody body = RequestBody.create(mediaType, gson.toJson(jsonObject));

		Request request = new Request.Builder()
				.url(String.format("https://hcaptcha.com/checkcaptcha/%s/%s", dadosCaptcha.getSiteKey(), key))
				.method("POST", body)
				.addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"")
				.addHeader("sec-ch-ua-platform", "\"Windows\"")//
				.addHeader("sec-ch-ua-mobile", "?0")//
				.addHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
				.addHeader("Content-type", "application/json;charset=UTF-8")//
				.addHeader("Accept", "*/*")//
				.addHeader("Sec-Fetch-Site", "same-site")//
				.addHeader("Sec-Fetch-Mode", "cors")//
				.addHeader("Sec-Fetch-Dest", "empty")//
				.addHeader("host", "hcaptcha.com").build();

		try (Response response = dispatchRequest(request)) {

			String data = response.body().string();

			JsonObject taskObject = new Gson().fromJson(data, JsonElement.class).getAsJsonObject();

			if (taskObject.get("pass").getAsBoolean()) {

				return taskObject.get("generated_pass_UUID").getAsString();
			}
		}
		return null;
	}

	@Override
	public String resolverTaskMotion(String key, String entityType, List<LabelImageVO> imagens,
			DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig, String autorizacao) throws Exception {

		JsonObject answers = new JsonObject();

		for (LabelImageVO image : imagens) {

			JsonObject entity = new JsonObject();
			entity.addProperty("entity_name", 0);
			entity.addProperty("entity_type", entityType);

			JsonArray jsonArray = new JsonArray();

			jsonArray.add(randomizar(image.getX()));
			jsonArray.add(randomizar(image.getY()));

			entity.add("entity_coords", jsonArray);

			JsonArray item = new JsonArray();
			item.add(entity);

			answers.add(image.getUuid(), item);
		}

		Gson gson = new Gson();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("v", versao);
		jsonObject.addProperty("job_mode", "image_label_area_select");
		jsonObject.add("answers", answers);
		jsonObject.addProperty("serverdomain", dadosCaptcha.getHost());
		jsonObject.addProperty("sitekey", dadosCaptcha.getSiteKey());
		jsonObject.addProperty("motionData", gson.toJson(gson.fromJson(motionData(), JsonObject.class)));
		jsonObject.addProperty("c", gson.toJson(siteConfig.getC()));
		jsonObject.addProperty("n", autorizacao);

		MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");

		RequestBody body = RequestBody.create(mediaType, gson.toJson(jsonObject));

		Request request = new Request.Builder()
				.url(String.format("https://hcaptcha.com/checkcaptcha/%s/%s", dadosCaptcha.getSiteKey(), key))
				.method("POST", body)
				.addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"")
				.addHeader("sec-ch-ua-platform", "\"Windows\"")//
				.addHeader("sec-ch-ua-mobile", "?0")//
				.addHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
				.addHeader("Content-type", "application/json;charset=UTF-8")//
				.addHeader("Accept", "*/*")//
				.addHeader("Sec-Fetch-Site", "same-site")//
				.addHeader("Sec-Fetch-Mode", "cors")//
				.addHeader("Sec-Fetch-Dest", "empty")//
				.addHeader("host", "hcaptcha.com").build();

		try (Response response = dispatchRequest(request)) {

			String data = response.body().string();

			JsonObject taskObject = new Gson().fromJson(data, JsonElement.class).getAsJsonObject();

			if (taskObject.get("pass").getAsBoolean()) {

				return taskObject.get("generated_pass_UUID").getAsString();
			}
		}
		return null;
	}

	private int randomizar(int valor) {

		int maximo = valor + 3;
		int minimo = valor - 3;

		return random.nextInt(maximo - minimo + 1) + minimo;
	}

	@Override
	public String resolverTaskClassificacao(HCaptchaBinaryVO task, DadosCaptchaDTO dadosCaptcha,
			SiteConfigVO siteConfig, String autorizacao) throws Exception {

		JsonObject answers = new JsonObject();

		for (HCaptchaImageVO image : task.getImagens()) {
			answers.addProperty(image.getUuid(), String.valueOf(image.isValido()));
		}

		Gson gson = new Gson();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("v", versao);
		jsonObject.addProperty("job_mode", "image_label_binary");
		jsonObject.add("answers", answers);
		jsonObject.addProperty("serverdomain", dadosCaptcha.getHost());
		jsonObject.addProperty("sitekey", dadosCaptcha.getSiteKey());
		jsonObject.addProperty("motionData", gson.toJson(gson.fromJson(motionData(), JsonObject.class)));
		jsonObject.addProperty("c", gson.toJson(siteConfig.getC()));
		jsonObject.addProperty("n", autorizacao);

		MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");

		RequestBody body = RequestBody.create(mediaType, gson.toJson(jsonObject));

		Request request = new Request.Builder()
				.url(String.format("https://hcaptcha.com/checkcaptcha/%s/%s", dadosCaptcha.getSiteKey(), task.getKey()))
				.method("POST", body)
				.addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"")
				.addHeader("sec-ch-ua-platform", "\"Windows\"")//
				.addHeader("sec-ch-ua-mobile", "?0")//
				.addHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
				.addHeader("Content-type", "application/json;charset=UTF-8")//
				.addHeader("Accept", "*/*")//
				.addHeader("Sec-Fetch-Site", "same-site")//
				.addHeader("Sec-Fetch-Mode", "cors")//
				.addHeader("Sec-Fetch-Dest", "empty")//
				.addHeader("host", "hcaptcha.com").build();

		try (Response response = dispatchRequest(request)) {

			String data = response.body().string();

			JsonObject taskObject = new Gson().fromJson(data, JsonElement.class).getAsJsonObject();

			if (taskObject.get("pass").getAsBoolean()) {

				return taskObject.get("generated_pass_UUID").getAsString();
			}
		}

		return null;
	}

	private Response dispatchRequest(Request request) {

		ResponseVO responseVO = new ResponseVO();

		try {
			CountDownLatch countDownLatch = new CountDownLatch(1);

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onResponse(Call arg0, Response arg1) throws IOException {
					responseVO.setResponse(arg1);
					countDownLatch.countDown();
				}

				@Override
				public void onFailure(Call arg0, IOException arg1) {
					countDownLatch.countDown();
				}
			});

			countDownLatch.await();

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
		}

		if (responseVO.getResponse() == null) {
			throw new RuntimeException("Não foi possível realizar a requisição");
		}

		return responseVO.getResponse();
	}

	private String getVersao() throws Exception {

		if (this.versao == null) {

			String data = IOUtils.toString(
					((HttpURLConnection) new URL("https://hcaptcha.com/1/api.js").openConnection()).getInputStream(),
					StandardCharsets.UTF_8);

			int starts = data.indexOf("https://newassets.hcaptcha.com/captcha/v1/") + 42;

			this.versao = data.substring(starts, data.indexOf('/', starts));
		}

		return this.versao;
	}

	private String motionData() {
		return "{\r\n" + "        \"st\": 1674610364922,\r\n" + "        \"dct\": 1674610364923,\r\n"
				+ "        \"mm\": [[24, 361, 1674610373713], [78, 341, 1674610373734], [98, 332, 1674610373751], [106, 326, 1674610373767], [108, 324, 1674610373783], [109, 322, 1674610373823], [109, 321, 1674610373863], [109, 319, 1674610373879], [109, 317, 1674610373896], [109, 314, 1674610373912], [106, 305, 1674610373935], [101, 298, 1674610373951], [99, 294, 1674610373967], [97, 292, 1674610373983], [97, 291, 1674610373999], [99, 293, 1674610374097], [118, 299, 1674610374118], [133, 303, 1674610374135], [143, 306, 1674610374151], [152, 310, 1674610374174], [155, 310, 1674610374239], [158, 312, 1674610374255], [167, 314, 1674610374271], [183, 318, 1674610374287], [211, 326, 1674610374311], [225, 333, 1674610374327], [246, 345, 1674610374351], [264, 356, 1674610374367], [290, 371, 1674610374383], [284, 570, 1674610376479], [282, 569, 1674610376510], [282, 568, 1674610376534], [281, 567, 1674610376598], [280, 567, 1674610376654], [279, 567, 1674610376687], [278, 568, 1674610376711], [276, 570, 1674610376735], [276, 570, 1674610376879], [276, 564, 1674610376903], [274, 555, 1674610376927], [269, 547, 1674610376950], [267, 545, 1674610376967], [264, 534, 1674610376991], [261, 524, 1674610377007], [259, 522, 1674610377023], [259, 498, 1674610377056], [259, 434, 1674610377134], [258, 432, 1674610377159], [258, 431, 1674610377175], [256, 429, 1674610377198], [255, 427, 1674610377223], [255, 426, 1674610377503], [254, 425, 1674610377527], [253, 423, 1674610377559], [251, 421, 1674610377575], [250, 418, 1674610377591], [248, 415, 1674610377607], [244, 410, 1674610377624], [241, 403, 1674610377647], [239, 401, 1674610377663], [238, 399, 1674610377679], [236, 398, 1674610377703], [232, 397, 1674610377719], [227, 393, 1674610377735], [216, 386, 1674610377751], [201, 381, 1674610377767], [180, 370, 1674610377783], [138, 343, 1674610377808], [83, 307, 1674610377830], [53, 286, 1674610377846], [32, 269, 1674610377862], [21, 257, 1674610377878], [16, 249, 1674610377895], [12, 240, 1674610377911], [10, 234, 1674610377927], [10, 230, 1674610377943], [11, 226, 1674610377959], [13, 224, 1674610377976], [16, 217, 1674610377992], [19, 207, 1674610378015], [20, 199, 1674610378031], [22, 192, 1674610378047], [23, 184, 1674610378063], [24, 180, 1674610378079], [25, 176, 1674610378095], [25, 174, 1674610378119], [26, 173, 1674610378343], [27, 176, 1674610378359], [31, 178, 1674610378375], [35, 180, 1674610378391], [39, 181, 1674610378407], [44, 181, 1674610378423], [51, 181, 1674610378439], [57, 181, 1674610378455], [62, 181, 1674610378471], [65, 181, 1674610378487], [67, 181, 1674610378974], [68, 183, 1674610378990], [74, 185, 1674610379006], [85, 185, 1674610379022], [107, 186, 1674610379038], [140, 192, 1674610379054], [190, 201, 1674610379070], [245, 205, 1674610379086], [287, 205, 1674610379103], [309, 205, 1674610379119], [318, 205, 1674610379150], [318, 204, 1674610379319], [318, 203, 1674610379335], [318, 200, 1674610379440], [318, 202, 1674610379647], [306, 216, 1674610379670], [292, 229, 1674610379686], [274, 244, 1674610379702], [256, 258, 1674610379719], [221, 278, 1674610379743], [186, 296, 1674610379767], [163, 305, 1674610379790], [155, 306, 1674610379807], [147, 306, 1674610379832], [142, 306, 1674610379848], [137, 306, 1674610379870], [133, 306, 1674610379886], [127, 308, 1674610379902], [118, 310, 1674610379918], [111, 311, 1674610379934], [105, 313, 1674610379951], [101, 314, 1674610379967], [99, 314, 1674610379991], [98, 314, 1674610380015], [97, 314, 1674610380038], [96, 314, 1674610380070], [95, 314, 1674610380094], [94, 314, 1674610380110], [93, 314, 1674610380126], [92, 315, 1674610380407], [93, 316, 1674610380423], [98, 318, 1674610380439], [119, 321, 1674610380462], [144, 324, 1674610380479], [178, 327, 1674610380495], [222, 328, 1674610380518], [238, 328, 1674610380535], [244, 328, 1674610380567], [244, 329, 1674610380607], [245, 330, 1674610380623], [246, 332, 1674610380639], [249, 335, 1674610380656], [251, 342, 1674610380687], [252, 436, 1674610380855], [253, 436, 1674610380879], [254, 436, 1674610380919], [255, 436, 1674610380943], [258, 436, 1674610380959], [266, 436, 1674610380975], [277, 436, 1674610380991], [291, 435, 1674610381007], [298, 434, 1674610381023], [304, 432, 1674610381039], [309, 432, 1674610381063], [311, 432, 1674610381103], [312, 433, 1674610381119], [314, 435, 1674610381135], [315, 438, 1674610381159], [316, 440, 1674610381183], [317, 442, 1674610381223], [320, 443, 1674610381247], [323, 443, 1674610381271], [326, 443, 1674610381287], [327, 444, 1674610381303], [328, 445, 1674610381326], [329, 446, 1674610381342], [330, 446, 1674610381383], [331, 446, 1674610381414], [331, 447, 1674610381703], [329, 448, 1674610381719], [327, 448, 1674610381735], [324, 456, 1674610381758], [324, 461, 1674610381775], [322, 468, 1674610381791], [320, 475, 1674610381807], [320, 477, 1674610381823], [320, 507, 1674610382151], [320, 506, 1674610382182], [321, 505, 1674610382215], [323, 501, 1674610382240], [324, 498, 1674610382303], [324, 499, 1674610382398], [325, 503, 1674610382415], [326, 509, 1674610382431], [327, 517, 1674610382447], [327, 529, 1674610382471], [327, 537, 1674610382495], [327, 540, 1674610382511], [327, 542, 1674610382542], [328, 542, 1674610382567], [331, 542, 1674610382591], [335, 543, 1674610382607], [340, 544, 1674610382631], [343, 545, 1674610382647], [346, 546, 1674610382663], [350, 549, 1674610382687], [352, 550, 1674610382703], [352, 552, 1674610382751], [351, 552, 1674610382775], [350, 554, 1674610382791], [349, 555, 1674610382807], [348, 556, 1674610382830], [347, 557, 1674610382854], [347, 558, 1674610382871], [345, 561, 1674610382903], [345, 563, 1674610382919], [345, 564, 1674610382935], [344, 565, 1674610382967], [343, 565, 1674610383007], [342, 565, 1674610383023], [340, 564, 1674610383063], [340, 563, 1674610383118], [340, 562, 1674610383254]],\r\n"
				+ "        \"mm-mp\": 36.81649484536084,\r\n"
				+ "        \"md\": [[66, 181, 1674610378770], [318, 200, 1674610379440], [92, 314, 1674610380232], [331, 446, 1674610381519], [340, 562, 1674610383297]],\r\n"
				+ "        \"md-mp\": 1131.75,\r\n"
				+ "        \"mu\": [[66, 181, 1674610378879], [318, 200, 1674610379550], [92, 314, 1674610380334], [331, 446, 1674610381615], [340, 562, 1674610383447]],\r\n"
				+ "        \"mu-mp\": 1142,\r\n" + "        \"topLevel\": {\r\n"
				+ "            \"st\": 1674610361328,\r\n" + "            \"sc\": {\r\n"
				+ "                \"availWidth\": 1366,\r\n" + "                \"availHeight\": 728,\r\n"
				+ "                \"width\": 1366,\r\n" + "                \"height\": 768,\r\n"
				+ "                \"colorDepth\": 24,\r\n" + "                \"pixelDepth\": 24,\r\n"
				+ "                \"availLeft\": 0,\r\n" + "                \"availTop\": 0,\r\n"
				+ "                \"onchange\": null,\r\n" + "                \"isExtended\": false\r\n"
				+ "            },\r\n" + "            \"nv\": {\r\n" + "                \"vendorSub\": \"\",\r\n"
				+ "                \"productSub\": \"20030107\",\r\n"
				+ "                \"vendor\": \"Google Inc.\",\r\n" + "                \"maxTouchPoints\": 0,\r\n"
				+ "                \"scheduling\": {},\r\n" + "                \"userActivation\": {},\r\n"
				+ "                \"doNotTrack\": null,\r\n" + "                \"geolocation\": {},\r\n"
				+ "                \"connection\": {},\r\n" + "                \"pdfViewerEnabled\": true,\r\n"
				+ "                \"webkitTemporaryStorage\": {},\r\n"
				+ "                \"hardwareConcurrency\": 4,\r\n" + "                \"cookieEnabled\": true,\r\n"
				+ "                \"appCodeName\": \"Mozilla\",\r\n" + "                \"appName\": \"Netscape\",\r\n"
				+ "                \"appVersion\": \"5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36\",\r\n"
				+ "                \"platform\": \"Win32\",\r\n" + "                \"product\": \"Gecko\",\r\n"
				+ "                \"userAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36\",\r\n"
				+ "                \"language\": \"pt-BR\",\r\n"
				+ "                \"languages\": [\"pt-BR\", \"pt\", \"en-US\", \"en\"],\r\n"
				+ "                \"onLine\": true,\r\n" + "                \"webdriver\": false,\r\n"
				+ "                \"bluetooth\": {},\r\n" + "                \"clipboard\": {},\r\n"
				+ "                \"credentials\": {},\r\n" + "                \"keyboard\": {},\r\n"
				+ "                \"managed\": {},\r\n" + "                \"mediaDevices\": {},\r\n"
				+ "                \"storage\": {},\r\n" + "                \"serviceWorker\": {},\r\n"
				+ "                \"virtualKeyboard\": {},\r\n" + "                \"wakeLock\": {},\r\n"
				+ "                \"deviceMemory\": 8,\r\n" + "                \"ink\": {},\r\n"
				+ "                \"hid\": {},\r\n" + "                \"locks\": {},\r\n"
				+ "                \"mediaCapabilities\": {},\r\n" + "                \"mediaSession\": {},\r\n"
				+ "                \"permissions\": {},\r\n" + "                \"presentation\": {},\r\n"
				+ "                \"serial\": {},\r\n" + "                \"usb\": {},\r\n"
				+ "                \"windowControlsOverlay\": {},\r\n" + "                \"xr\": {},\r\n"
				+ "                \"userAgentData\": {\r\n" + "                    \"brands\": [{\r\n"
				+ "                        \"brand\": \"Not_A Brand\",\r\n"
				+ "                        \"version\": \"99\"\r\n" + "                    }, {\r\n"
				+ "                        \"brand\": \"Google Chrome\",\r\n"
				+ "                        \"version\": \"109\"\r\n" + "                    }, {\r\n"
				+ "                        \"brand\": \"Chromium\",\r\n"
				+ "                        \"version\": \"109\"\r\n" + "                    }],\r\n"
				+ "                    \"mobile\": false,\r\n" + "                    \"platform\":\r\n"
				+ "                        \"Windows\"\r\n" + "                },\r\n"
				+ "                \"plugins\": [\"internal-pdf-viewer\", \"internal-pdf-viewer\", \"internal-pdf-viewer\", \"internal-pdf-viewer\", \"internal-pdf-viewer\"]\r\n"
				+ "            },\r\n" + "            \"dr\": \"\",\r\n" + "            \"inv\": true,\r\n"
				+ "            \"exec\": true,\r\n"
				+ "            \"wn\": [[1349, 286, 1, 1674610376025], [1349, 293, 1, 1674610376146], [1349, 402, 1, 1674610376191], [1349, 474, 1, 1674610376269], [1349, 481, 1, 1674610376293], [1349, 482, 1, 1674610376349]],\r\n"
				+ "            \"wn-mp\": 2145.285714285714,\r\n"
				+ "            \"xy\": [[0, 99, 1, 1674610376968], [0, 87, 1, 1674610376991], [0, 76, 1, 1674610377007], [0, 64, 1, 1674610377025], [0, 35, 1, 1674610377056], [0, 22, 1, 1674610377073], [0, 12, 1, 1674610377090], [0, 5, 1, 1674610377106], [0, 1, 1, 1674610377123], [0, 0, 1, 1674610377140], [0, 1, 1, 1674610380665], [0, 13, 1, 1674610380686], [0, 24, 1, 1674610380703], [0, 37, 1, 1674610380719], [0, 51, 1, 1674610380737], [0, 65, 1, 1674610380753], [0, 78, 1, 1674610380769], [0, 88, 1, 1674610380786], [0, 95, 1, 1674610380804], [0, 99, 1, 1674610380820], [0, 100, 1, 1674610380837], [0, 101, 1, 1674610381991], [0, 113, 1, 1674610382024], [0, 124, 1, 1674610382041], [0, 136, 1, 1674610382058], [0, 151, 1, 1674610382075], [0, 169, 1, 1674610382091], [0, 209, 1, 1674610382122], [0, 231, 1, 1674610382140], [0, 251, 1, 1674610382157], [0, 268, 1, 1674610382173], [0, 282, 1, 1674610382190], [0, 292, 1, 1674610382206], [0, 298, 1, 1674610382224], [0, 300, 1, 1674610382241]],\r\n"
				+ "            \"xy-mp\": 418.16,\r\n"
				+ "            \"mm\": [[428, 384, 1674610373696], [500, 372, 1674610373712], [760, 581, 1674610376480], [752, 581, 1674610376880]],\r\n"
				+ "            \"mm-mp\": 111.12592592592593,\r\n" + "            \"md\": [],\r\n"
				+ "            \"md-mp\": 0,\r\n" + "            \"mu\": [],\r\n" + "            \"mu-mp\": 0\r\n"
				+ "        },\r\n" + "        \"v\": 1\r\n" + "    }";
	}

}
