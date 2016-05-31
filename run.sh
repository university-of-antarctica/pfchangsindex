#!/bin/bash

cwd=$(pwd)
DATOMIC=datomic-free-0.9.5359
DATOMIC_CONSOLE=datomic-console-0.1.206

tmux new-session -d -s datomic "$DATOMIC/bin/transactor $cwd/free-transactor-template.properties; sleep 8" \; \
     split-window -d "$DATOMIC/bin/console -p 8080 pfchangs datomic:free://localhost:4334/" \;

