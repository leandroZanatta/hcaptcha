package br.com.slimbot.hcaptcha.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabelImageVO {

	private int x;
	private int y;
	private String uuid;
}
