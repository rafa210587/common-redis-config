# Repositório padrão para serviços spring boot com maven release

![Docker Automated build](https://img.shields.io/docker/automated/jrottenberg/ffmpeg.svg) ![Pronto](https://img.shields.io/badge/Ready-ok-green.svg)

Esse  repositório serve como base para novos serviços.

#### Para usar:
Basta `clonar/forkar/baixar` esse esqueleto e adaptar ao serviço desejado. 

* No arquivo `.gitlab-ci.yml` estão as configurações de pipeline.
* No arquivo `.gitlab-ci.yml` do novo projeto deve ser apagado todo o conteúdo e substituido por:

```yml
 include:
  - project: 'COMUM/repo-default'
    ref: com-maven-release 
    file: '.gitlab-ci.yml'
```
* Onde o ref é o nome da branch que está sendo clonada.

* No diretório `deploy/` estão configurações acerca do deploy, como Dockerfile, ansible, etc..

* No arquivo `start.sh` estão a configuração de inicialização do serviço em rodando em container.

* No arquivo `pom.xml` do seu projeto de ser configurado o `scm` com a url do projeto Git
```xml
<scm>
    <connection>scm:git:https://maven-release:maven-passwd@gitlab.interno.srmasset.com/GRUPO/REPOSITORIO.git</connection>
    <developerConnection>scm:git:https://maven-release:maven-passwd@gitlab.interno.srmasset.com/GRUPO/REPOSITORIO.git</developerConnection>
    <url>https://gitlab.interno.srmasset.com/GRUPO/REPOSITORIO.git</url>
    <tag>HEAD</tag>
</scm>

```
#### Como Funciona:

* No build é usado o maven para instalar dependências e compilar o pacote .jar.
```shell
mvn clean install -Dmaven.test.skip=true
```
* O .jar é copiado para a container image em tempo de build

* É copiado também um script que faz a verificação de qual profile será usado e sobe a aplicação com todoas as flags e necessárias:
```shell
#/bin/sh
DOMAIN=$(echo "$CI_BUILD_URL" | awk -F/ '{print $3}')
export SERVICE_HOSTNAME=${DOMAIN}

echo $SERVICE_HOSTNAME > /tmp/start.log

JVM_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -Xms128M -Xmx128m -Xss256k"

if [ "$1" = "production" ]; then
   echo "perfil=producao" > /config/perfil.properties
   SPRING_PROFILES="prod"
elif [ "$1" = "staging" ]; then
   echo "perfil=homologacao" > /config/perfil.properties
   SPRING_PROFILES="hom"
elif [ "$1" = "review-staging" ]; then
   echo "perfil=homologacao" > /config/perfil.properties
   SPRING_PROFILES="hom"
elif [ "$1" = "preprod" ]; then
   echo "perfil=preproducao" > /config/perfil.properties
   SPRING_PROFILES="prod"
else
   echo "perfil=desenvolvimento" > /config/perfil.properties
   SPRING_PROFILES="dev"
fi

export CI_ENVIRONMENT=$1

java $JVM_OPTS -Djava.net.preferIPv4Stack=true -Dserver.port=443 -jar /tmp/*.jar -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 --spring.profiles.active=$SPRING_PROFILES --eureka.instance.hostname=${DOMAIN}

```

* Nesse padrão existe os jobs de maven release no qual geram nova versão e publicam no Nexus
```shell
mvn clean -DskipTests -Darguments=-DskipTests release:prepare -B -Dresume=false -DscmCommentPrefix="[ci skip]"
```

```shell
mvn release:perform
```

#### Como forçar atualização completa do pipeline a partir do repo-default
* Os arquivos start.sh, config.conf e Dockerfile não são atualizados automaticamente pois tratam-se de templates que são criados automaticamente durante a primeira execução de um pipeline.
No entanto, caso precise que sejam alterados em seu repositório, basta remover a pasta DEPLOY de seu repositório que o pipeline re-criará com o conteúdo existente no repo-default.

