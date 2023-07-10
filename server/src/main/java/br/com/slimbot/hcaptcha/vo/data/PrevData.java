package br.com.slimbot.hcaptcha.vo.data;

import lombok.Data;

@Data
public class PrevData {

	private boolean escaped = false;
	private boolean passed = false;
	private boolean expiredChallenge = false;
	private boolean expiredResponse = false;
}
