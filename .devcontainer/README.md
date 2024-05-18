# Dev Container
This directory contains files related to the dev container for the project, which is a defined environment with the dependencies for compilation pre-installed.

## Install Docker
If you do not already have Docker installed, download it [here](https://www.docker.com/get-started/)_.

## Entering Dev Container Environment via VSCode
To enter the dev container environment using VSCode, follow these steps:
1. Click on the "Open a Remote Window" button in the bottom left of VSCode
1. Click "Reopen in Container"

## Entering Dev Container Environment via Command Line
### Windows
To enter the dev container environment via the command line on Windows, run the following commands:
```
cd .\.devcontainer
.\start_dev_container.bat
```

### Linux
To enter the dev container environment via the command line on Linux, run the following commands:
```
cd ./.devcontainer
./start_dev_container.sh
```

## Compiling
To compile the project in the provided dev container, follow these steps:
1. Make sure the End of Line (EOF) Sequence for the `gradlew` script is set to LF. If it is set to CRLF, the script will fail to run.
1. Run the following command:
```
./gradlew clean build
```