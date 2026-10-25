# 🛠️ Manual de Instalação do IntelliJ IDEA e MySQL 

Este manual apresenta o passo a passo para instalar e realizar a configuração inicial do IntelliJ IDEA, ambiente utilizado para desenvolvimento de projetos em Java, e do MySQL, utilizado para criação e configuração do banco de dados para execução do programa. 

##  1. Download

Acesse o site oficial da **[JetBrains](https://www.jetbrains.com/idea/download/?section=windows)** e faça o download do IntelliJ IDEA compatível com o seu sistema operacional.

##  2. Abrir o instalador

Após o download, abra o arquivo do instalador e siga as instruções apresentadas na tela.

##  3. Escolher as opções de instalação

Durante a instalação, serão apresentadas algumas opções de configuração. Se desejar, é possível criar um atalho do IntelliJ IDEA na área de trabalho.

##  4. Instalar

Clique em **"Install"** e aguarde enquanto o programa é instalado no computador.

##  5. Finalizar a instalação

Quando a instalação for concluída, clique em **"Finish"** para fechar o instalador.

##  6. Abrir o IntelliJ IDEA

Após a instalação, procure pelo **IntelliJ IDEA** no computador e abra o programa pelo atalho da área de trabalho ou pelo menu **Iniciar**.

##  7. Configuração inicial

Na primeira execução, siga as configurações iniciais solicitadas pelo IntelliJ IDEA.

##  8. Configuração do JDK

Para desenvolver e executar projetos em Java, é necessário ter um **JDK (Java Development Kit)** instalado e configurado no IntelliJ IDEA.

Caso o projeto utilize uma versão específica do JDK, certifique-se de selecionar a versão correspondente durante a configuração do projeto.

##  9. Conclusão

Após concluir esses passos, o **IntelliJ IDEA** estará instalado e configurado, estando pronto para abrir e executar o projeto.



# MySQL

##  1. Download

Acesse o site oficial do **[MySQL](https://dev.mysql.com/downloads/)** e faça o download do instalador do MySQL compatível com o seu sistema operacional.

##  2. Abrir o instalador

Após o download, abra o arquivo do instalador e siga as instruções apresentadas na tela.

##  3. Escolher o tipo de instalação

Durante a instalação, serão apresentadas algumas opções de configuração. Selecione a opção **Custom** e escolha para baixar o **MySQL Server** e o **MySQL Workbench**, ferramenta gráfica utilizada para gerenciar o banco de dados.

##  4. Instalar

Siga os demais passos para instalação.

##  5. Configurar o servidor

Após a instalação, será iniciada a configuração do **MySQL Server**. Mantenha as opções padrão, incluindo a porta **3306**, utilizada pelo MySQL para a conexão com o banco de dados.

##  6. Definir a senha do usuário root

Crie uma senha para o usuário **"root"**, que é o administrador do banco de dados. Guarde essa senha, pois ela será necessária para acessar o MySQL e para conectar o programa ao banco de dados.

##  7. Finalizar a instalação

Clique em **"Execute"** para aplicar as configurações e, quando o processo for concluído, clique em **"Finish"** para fechar o instalador.

##  8. Abrir o MySQL Workbench

Após a instalação, procure pelo **MySQL Workbench** no computador e abra o programa pelo atalho da área de trabalho ou pelo menu **Iniciar**. Em seguida, acesse a conexão local utilizando a senha definida para o usuário **root**.

##  9. Criação do banco de dados

Abra o **MySQL Workbench**, conecte com o usuário **root** e use **File → Open SQL Script…** para abrir o arquivo `anota_database.sql`, da pasta **Schema** do projeto. Execute-o no ícone do raio. Ele cria o banco `anota_database`, as tabelas e o usuário `lovelace` (senha `pass123`) usados pelo programa. Depois, abra e execute do mesmo jeito o arquivo `anota_data.sql`, que insere os dados iniciais.

##  10. Conclusão

Após concluir esses passos, o **MySQL** estará instalado e configurado, estando pronto para ser utilizado na execução do programa.
