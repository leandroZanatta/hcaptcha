package br.com.slimbot.hcaptcha.service.impl;

import java.io.IOException;

import org.springframework.stereotype.Service;

import br.com.slimbot.hcaptcha.service.HcaptchaHswService;
import br.com.slimbot.hcaptcha.vo.HcaptchaConfigVO;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Service
public class HcaptchaHswServiceImpl implements HcaptchaHswService {

	@Override
	public String obterHSW(HcaptchaConfigVO hcaptchaConfigVO) throws IOException {

		HttpUrl mySearchUrl = new HttpUrl.Builder().scheme("http").host("localhost").port(3600).addPathSegment("hsw")
				.addQueryParameter("req", hcaptchaConfigVO.getReq()).build();

		Request request = new Request.Builder().url(mySearchUrl).method("GET", null)
				.addHeader("Accept", "application/json").addHeader("Content-type", "application/json").build();

		return new OkHttpClient().newCall(request).execute().body().string();
	}

}
