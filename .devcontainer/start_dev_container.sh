# This script is meant to be run from the .devcontainer directory.

docker build . -t rpk-dev-container
docker run -it -v %cd%\..\:/workspaces/RPKit rpk-dev-container /bin/bash