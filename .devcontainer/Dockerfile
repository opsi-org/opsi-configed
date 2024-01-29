FROM buildpack-deps:jammy-scm

ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

ENV DEBIAN_FRONTEND=noninteractive
RUN set -ex; \ 
    apt-get update; \
    # Adding non-root user
    groupadd --gid "$USER_GID" "$USERNAME"; \
    useradd -s /bin/bash --uid "$USER_UID" --gid "$USER_GID" -m "$USERNAME"; \
    apt-get install -y sudo; \
    echo "$USERNAME" ALL=\(root\) NOPASSWD:ALL > "/etc/sudoers.d/$USERNAME" ;\
    chmod 0440 "/etc/sudoers.d/$USERNAME"; \
    # Installing packages
    apt-get install -y  --no-install-recommends \
    zip \
    unzip \
    bash-completion \
    build-essential \
    locales \
    man-db \
    software-properties-common \
    sudo \
    time \
    iputils-ping \
    maven \
    openjdk-19-jdk; \
    # Installing current Node.js runtime LTS version from NodeSource
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash && \
    apt-get install -y nodejs; \
    # Clean up
    apt-get autoremove -y; \
    apt-get clean -y; \
    rm -rf /var/lib/apt/lists/*
ENV DEBIAN_FRONTEND=dialog