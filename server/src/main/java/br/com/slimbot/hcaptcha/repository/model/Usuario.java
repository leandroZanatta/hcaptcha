package br.com.slimbot.hcaptcha.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "tb_usuario")
@SequenceGenerator(name = "GEN_USUARIO", sequenceName = "GEN_USUARIO", allocationSize = 1)
public class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id_usuario")
	@GeneratedValue(generator = "GEN_USUARIO", strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column(name = "tx_email")
	private String email;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "usuario", fetch = FetchType.EAGER)
	private List<ReferenciaUsuario> referenciaUsuarios = new ArrayList<>();

}