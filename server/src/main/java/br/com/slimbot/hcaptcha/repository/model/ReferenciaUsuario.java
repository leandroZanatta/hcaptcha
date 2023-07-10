package br.com.slimbot.hcaptcha.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tb_referenciausuario")
@SequenceGenerator(name = "GEN_REFERENCIAUSUARIO", sequenceName = "GEN_REFERENCIAUSUARIO", allocationSize = 1)
public class ReferenciaUsuario {

	@Id
	@Column(name = "id_referenciausuario")
	@GeneratedValue(generator = "GEN_REFERENCIAUSUARIO", strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "cd_usuario", referencedColumnName = "id_usuario")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Usuario usuario;

	@Column(name = "tx_carteira")
	private String carteira;

	@Column(name = "tx_referencia")
	private String referencia;
}
