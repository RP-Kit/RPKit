# RPKit
RPKit is a suite of plugins for Minecraft roleplay servers. It includes standard libraries, which allow easy interfacing with a specific subset of functionality, and reference implementations of these.

## Quick Links
- [Website](http://rpkit.com/)
- [Wiki](https://github.com/RP-Kit/RPKit/wiki)
- [Discord](https://discord.gg/C9br2MdPcc)

## Table of Contents
- [Support](#support)
- [Installation](#installation)
- [Compiling the project](#compiling-the-project)
- [Contributing](#contributing)
- [Authors and acknowledgement](#authors-and-acknowledgement)
- [License](#license)

## Support
For support, please join our [Discord server](https://discord.gg/C9br2MdPcc) and ask in the #support channel.

Alernatively, you can open a [GitHub issue](https://github.com/RP-Kit/RPKit/issues/new) if you believe you have found a bug or have a feature request.

## Installation
1. Download the latest version of RPKit from the [releases page](https://github.com/RP-Kit/RPKit/releases). You will need to download each module individually. If you're not sure which modules you need, you can download all of them.
2. Place the JAR files in the `plugins` directory of your server.
3. Start your server. RPKit will generate a configuration file in the `plugins/RPKit` directory.
4. Configure RPKit to your liking by editing the `config.yml` file in the `plugins/RPKit` directory.
5. Restart your server.

## Compiling the project
To compile the project, run the following command in the root directory of the project:

Windows:
```cmd
.\gradlew.bat clean build
```
Linux:
```shell
./gradlew clean build
```

This will compile the project and generate a JAR file in the `build/libs` directory of each module. For example, the `rpk-chat-bukkit` module will generate a JAR file in `rpk-chat-bukkit/build/libs`.

## Contributing
To contribute to RPKit, please [fork](https://github.com/RP-Kit/RPKit/fork) the repository and submit a pull request. Please ensure that your code is well-tested and follows the existing code style.

## Authors and acknowledgement
### Developers
| Name | Contributions |
| ---- | ------------- |
| Ren Binden | Creator |

## License
RPKit is licensed under the Apache License, Version 2.0. See the [LICENSE](https://github.com/RP-Kit/RPKit/blob/main/LICENSE) file for more information.