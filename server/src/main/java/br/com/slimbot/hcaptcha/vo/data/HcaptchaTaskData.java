package br.com.slimbot.hcaptcha.vo.data;

import lombok.Data;

@Data
public class HcaptchaTaskData {

	private String v;
	private String sitekey;
	private String host;
	private String hl = "pt-Br";
	private String c;
	private String n;
	private String motionData;
}
