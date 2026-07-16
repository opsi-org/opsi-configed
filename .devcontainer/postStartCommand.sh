#!/bin/bash
# Adding .local/bin to PATH
export PATH="$HOME/.local/bin:$PATH" # current shell
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc  # for future shells
source ~/.bashrc 

echo "== Install dev-tools"
SCRIPT_NAME="installer.sh"
DEV_TOOLS_URL=https://binaryindex.uib.gmbh/development/opsi-dev-tools/linux/x64/$SCRIPT_NAME

rm -f "./${SCRIPT_NAME}"
wget $DEV_TOOLS_URL
chmod 750 $SCRIPT_NAME && ./${SCRIPT_NAME}
rm -f "./${SCRIPT_NAME}"
rm -f "./${SCRIPT_NAME}*"
opsi-dev-cli self upgrade
