package br.com.slimbot.hcaptcha.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class LabelImageDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String localizacao;

	private String conteudo;

	private List<Double> labelImg;

	private boolean selecionado = false;
}
