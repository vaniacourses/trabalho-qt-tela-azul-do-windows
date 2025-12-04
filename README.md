# Projeto Banco ATM

Este projeto é uma aplicação web que simula as funcionalidades básicas de um caixa eletrônico (ATM), permitindo a gestão de contas de clientes e oferecendo um painel administrativo para um gerente. A aplicação foi construída utilizando a arquitetura MVC (Model-View-Controller) com Java Servlets e JSP, e agora está sendo utilizada para estudos na disciplina de Qualidade e Teste de Software.

---

## 📚 Documentação do Projeto e Artefatos

Todos os documentos e artefatos de teste gerados para o projeto estão disponíveis nos links abaixo:

* **Plano de Teste:** [Link](https://docs.google.com/document/d/1gZI48qXrKt-7Ol-iNRwgWNd4ShzFlkxM/)
* **Planilha de Casos de Teste (Manuais e Unitários):** [Link](https://docs.google.com/spreadsheets/d/1hIZYAHNf7_Ws7WwVpP2s7LdwW3uqgnCW/)
* **Pasta de Evidências (Screenshots e Logs):** [Link](https://drive.google.com/drive/folders/1dq5cM1rdKQ8Lkx3HSO6in07fJbf6mxd-)
* **Apresentação PPTX (Entrega 1):** [Link](https://docs.google.com/presentation/d/1cnJ-3ldlAAhiPNQFaiI9VJ19VoUX42sp/)
* **Apresentação PDF (Entrega 2):** [Link](https://drive.google.com/file/d/1F2ByDviHUEJr2pn_MLm5k4HtyOoe-cMP/)
* **Instruções Oficiais do Trabalho (PDF):** [Link](https://docs.google.com/document/d/1lPZmF6VMtyhSkKBZBjU5IbR2nYfEZ-alYI1ak89zXL8/)
* **Repositório GitHub (Código da Entrega 1):** [Link](https://github.com/renatouff/trabalho-banco-atm)
* **Repositório GitHub (Código da Entrega 2):** [Link](https://github.com/vaniacourses/trabalho-qt-tela-azul-do-windows)

---

## Tecnologias Utilizadas

- **Frontend:** Bootstrap 5 (v5.3.3)
- **Backend:** Java 11, Servlets, JSP, JSTL (Jakarta EE 10)
- **Banco de Dados:** MySQL Server 8.x
- **Build Tool:** Apache Maven
- **Servidor de Aplicação:** GlassFish 7
- **IDE:** Eclipse IDE for Enterprise Java and Web Developers

---

## 🚀 Guia de Configuração do Ambiente

Siga estes passos para configurar e executar o projeto na sua máquina.

### 1. Pré-requisitos (Downloads)
Antes de começar, baixe e instale os seguintes softwares:

* **JDK 17 (Recomendado):**
   * Necessário JDK 11 ou superior. O JDK 17 é uma versão LTS estável e recomendada.
   * **Link:** [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

* **MySQL Server 8.4.5:**
   * Durante a instalação, defina uma senha para o usuário `root` e anote-a.
   * **Link:** [MySQL Community Downloads](https://downloads.mysql.com/archives/community/)

* **MySQL Workbench 8.0.42:**
   * Ferramenta para gerenciar o banco de dados.
   * **Link:** [MySQL Workbench Download](https://downloads.mysql.com/archives/workbench/)

* **Eclipse IDE for Enterprise Java and Web Developers:**
   * **Link:** [Eclipse Downloads](https://www.eclipse.org/downloads/packages/release/2025-09/r/eclipse-ide-enterprise-java-and-web-developers)

* **GlassFish Server 7:**
   * **Link:** [GlassFish 7 Download](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.0.25.zip)
   * Apenas descompacte o arquivo `.zip` em uma pasta de fácil acesso (ex: `C:\dev\glassfish7`).

### 2. Preparando o Banco de Dados
1. Abra o **MySQL Workbench** e conecte-se ao seu servidor local usando o usuário `root`.
2. Abra uma nova aba de script e execute os seguintes comandos para criar o banco de dados e o usuário da aplicação:

    ```sql
	CREATE DATABASE banco_atm;
	CREATE USER 'grupo'@'localhost' IDENTIFIED BY '123';
	GRANT ALL PRIVILEGES ON banco_atm.* TO 'grupo'@'localhost';
	FLUSH PRIVILEGES;

### 3. Configurando o Projeto no Eclipse
1. **Clone o Repositório:** `git clone https://github.com/renatouff/trabalho-banco-atm.git`
2. **Importe o Projeto no Eclipse:**
    * Vá em `File > Import...` > `Maven > Existing Maven Projects`.
    * Aponte para a pasta do projeto (a que contém o `pom.xml`) e finalize a importação.
3. **Instale o Plugin do Servidor:**
    * Vá em `Help > Eclipse Marketplace...`.
    * Na busca, procure por: **`OmniFish Tools`** e instale.
    * Reinicie o Eclipse quando solicitado.
4. **Configure o Servidor de Aplicação no Eclipse:**
    * Abra a view `Servers` (`Window > Show View > Servers`).
    * Clique para criar um novo servidor, selecione **GlassFish** na lista.
    * Aponte para o diretório de instalação do seu GlassFish (ex: `C:\dev\glassfish7\glassfish`).
    * Selecione um JDK 11+ (recomendado JDK 17) e clique em `Finish`.
5. **Crie as Tabelas no MySQL Workbench:**
    * Abra o arquivo `scripts/banco_inicial.sql` localizado no projeto.
    * Copie todo o conteúdo do script, cole em uma nova aba SQL no **MySQL Workbench** conectado ao banco `banco_atm` e execute.
    * Isso criará todas as tabelas necessárias para a aplicação.

### 4. Executando a Aplicação
1. Clique com o botão direito no projeto no `Project Explorer`.
2. Vá em `Properties > Web Project Settings` e altere o **Context root** para `banco-atm`.
3. Em seguida, vá em `Run As > Run on Server`.
4. Selecione o servidor GlassFish que você configurou e clique em `Finish`.
5. A página de login da aplicação deve abrir no navegador.

## Credenciais de Teste
- **Login de Gerente:**
    - **CPF:** `00000000000`
    - **Senha:** `admin`

---

## 🧑‍💻 Autores

**Grupo:** Tela Azul do Windows

* Felipe Lopes
* Israel Bousquet
* Marcos Rodrigues
* Renato Luiz
* Rodrigo Dias
