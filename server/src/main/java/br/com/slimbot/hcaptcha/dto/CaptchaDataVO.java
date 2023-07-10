package br.com.slimbot.hcaptcha.dto;

public class CaptchaDataVO {

	private String host;
	private String siteKey;
	private String version;
	private String req;
	private String widgetId;
	private String n;

	public void setHost(String host) {
		this.host = host;
	}

	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setReq(String req) {
		this.req = req;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public void setN(String n) {
		this.n = n;
	}

	@Override
	public String toString() {

		return String.format("v=%s&sitekey=%s&host=%s&hl=\"ptBr\"&c=%s&n=%s&motionData=%s", version, siteKey, host,
				getC(), n, getMotionData());
	}

	private String getC() {
		return String.format("{\"type\":\"hsw\",\"req\":\"%s\"}", req);
	}

	private String getMotionData() {

		return String.format(
				"{\"v\":\"1\", \"widgetList\":\"[%s]\", \"widgetId\":\"%s\", \"href\"=\"%s\", \"prev\":{\"escaped\":false,\"passed\":false,\"expiredChallenge\":false,\"expiredResponse\":false}}",
				widgetId, widgetId, host);
	}
}
