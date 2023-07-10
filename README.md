# Hcaptcha Java

Projeto visa a resolução de captchas usando visão computacional.
- Projeto estruturado sob apis REST, sendo necessario uma requisição com site-key e host para receber uma chave captcha

# Requisitos
- Server
    - Java 17
    - NodeJs
    - Apache maven
 
# Compilação
  - Utiliza apache maven para a compilação.
  - << mvn clean install >> irá gerar um jar na pasta target

# Execução
  - Transfira o jar e as pastas model, node e nodevm para um diretório ou pasta de sua escolha.
  - Execute o comando java -jar <<nome_do_jar.jar>>
  - Projeto conta com open-api. 127.0.0.1/swagger-ui.html para acessar a documentação
  
