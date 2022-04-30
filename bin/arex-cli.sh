#!/bin/sh

# ----------------------------------------------------------------------------
#  program : arex sh
#     date : 2022-3-16
#  version : 0.1
# ----------------------------------------------------------------------------

CLASS_PATH="arex-cli-parent/arex-cli/target/"

CLIENT_JAR="../${CLASS_PATH}arex-cli.jar"

if [[ ! -x ${CLIENT_JAR} ]]; then
    echo "Can not find arex-cli jar under ${CLASS_PATH}, you can run: mvn clean install,to generate jar."
    exit 1
fi

java -cp ../${CLASS_PATH}* io.arex.cli.ArexCli

