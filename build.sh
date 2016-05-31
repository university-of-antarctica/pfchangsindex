#!/bin/bash

cwd=$(pwd)
DATOMIC=datomic-free-0.9.5359
DATOMIC_CONSOLE=datomic-console-0.1.206

unzip $DATOMIC.zip
unzip $DATOMIC_CONSOLE.zip
cp $DATOMIC/config/samples/free-transactor-template.properties ./
$DATOMIC/bin/maven-install
$DATOMIC_CONSOLE/bin/install-console $cwd/$DATOMIC

bash ./run.sh
