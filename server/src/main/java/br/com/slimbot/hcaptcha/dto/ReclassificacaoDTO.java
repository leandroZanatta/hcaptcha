package br.com.slimbot.hcaptcha.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ReclassificacaoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String pastaClassificar;
	
	private List<String> imagens;
}
