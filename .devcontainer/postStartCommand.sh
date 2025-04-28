#!/bin/bash
# Adding .local/bin to PATH
export PATH="$HOME/.local/bin:$PATH" # current shell
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc  # for future shells
source ~/.bashrc 

echo "== Install dev-tools"
MIN_VERSION=1.33.2 # will be updated to latest version, just indicate the required minimum version
wget http://binaryindex.uib.gmbh/development/opsi-dev-tools/linux/x64/opsi-dev-tools_linux_x64_${MIN_VERSION}.tar.gz
tar -xf opsi-dev-tools_linux_x64_${MIN_VERSION}.tar.gz
mv opsi-dev-tool opsi-dev-cli
./opsi-dev-cli self install # installs into ~/.local/bin
rm -f opsi-dev-cli
rm -f opsi-dev-tools_linux_x64_${MIN_VERSION}.tar.gz


opsi-dev-cli self upgrade # using script from ~/.local/bin/opsi-dev-tool
opsi-dev-cli --version