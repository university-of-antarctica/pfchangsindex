## Requirements
# - root of proj is mounted as volume to allow local editing of src files.
# - need to connect to repl running in docker container
# - need datomic port open as well.
# -   need healthy separation of app and db containers
# - may need to turn this into docker compose.

FROM java:8
MAINTAINER gpwclark

# Update phusion and install necessary programs
RUN apt-get update && apt-get install -y \
  git \
  wget \
  vim \
  tmux \
  unzip \
  libssl-dev \
  curl \
  ca-certificates \
  maven \
  wamerican 

# Make non sudo user and copy code from git repo to the docker image
RUN useradd -ms /bin/bash user
RUN mkdir -p /home/user/app
ADD . /home/user/app

# set up environment for development
WORKDIR /home/user
RUN wget https://raw.githubusercontent.com/gpwclark/System-files/master/.vimrc
RUN wget https://raw.githubusercontent.com/gpwclark/System-files/master/.tmux.conf
RUN wget https://raw.githubusercontent.com/gpwclark/System-files/master/.bashrc
WORKDIR /home/user/app
RUN mkdir -p /home/user/.vim
RUN git clone https://github.com/VundleVim/Vundle.vim.git /home/user/.vim/bundle/Vundle.vim

# Install lein
WORKDIR /home/user/app
RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
RUN chmod a+x ./lein
RUN cp ./lein /usr/local/bin/lein

# Install datomic
RUN mkdir datomic
RUN unzip datomic-free-0.9.5359.zip -d ./datomic
WORKDIR /home/user/app/datomic
RUN bin/maven-install

# Housekeeping
RUN chown -R user:user /home/user
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


USER user
RUN lein

