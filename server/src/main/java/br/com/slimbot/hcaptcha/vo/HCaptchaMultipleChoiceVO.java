package br.com.slimbot.hcaptcha.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HCaptchaMultipleChoiceVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;
	private String choice;
}
