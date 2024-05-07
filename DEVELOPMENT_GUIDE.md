# opsi-configed Development Guide

This development guide provides instructions on how to set up your development
environment for the project and describes the recommended tools and resources
for development.

## Development Environment Setup

To get started with development for the project, follow these steps:

1. Install [VSCode](https://code.visualstudio.com/) as Integrated Development
   Environment (IDE). Any other IDE is a viable option, however, we highly
   recommend using VSCode for its extensive support and compatibility with our
   development setup.
2. Clone the project repository to your local machine using the following
   command:

```
git clone repository
```

3. Install the [Remote - Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
   extension in VSCode. This extension allows you to develop inside a Docker
   container seamlessly.
4. Open the project folder in VSCode. If you have the Remote - Containers
   extension installed, you will see a popup recommending to reopen the project
   in a container. Click on "Reopen in Container" to start the development
   environment within the Docker container.
5. If you are using Windows, you will need to modify the Docker container
   script to accommodate any additional tools or configurations required for
   the Windows environment. For Windows environment configuration instructions,
   please refer to the [Windows Environment Configuration](#windows-environment-configuration)
   section.
6. The Docker container is pre-configured with all the necessary tools and
   extensions needed for development. It provides a consistent and isolated
   environment for all contributors.

## Development Workflow

Once your development environment is set up, you can follow this workflow:

1. Make sure you are developing inside the Docker container. This ensures that
   your development environment is consistent with other contributors.
2. Create a new branch for your changes. Use a descriptive branch name that
   reflects the purpose of your changes. Follow the [branch creation rules](CONTRIBUTING.md#creating-branches)
   described in the contributing guidelines.
3. Write your code following the coding style guidelines described in the [CODING_STYLE.md](CODING_STYLE.md)
   file.
4. Commit your changes regularly in small, logical units. Follow the
   [commit message conventions](CONTRIBUTING.md#commit-message-convention)
   described in the contributing guidelines.
5. Push your branch to the remote repository when you are ready to submit
   your changes.
6. Create a merge request to propose your changes for review. Provide a clear
   and detailed description of the changes, referencing any related issues or
   relevant information.
7. Collaborate with other contributors and address any feedback or comments
   received during the review process.
8. Once your changes have been reviewed and approved, they will be merged into
   the ```main``` branch.

## Windows Environment Configuration

To configure the development environment in Windows, follow these additional
steps:

1. Install MobaXterm and start it.
2. Configure X server: Go to Settings -> X11 (tab) -> set X11 Remote Access to
   "full".
3. Uncomment/comment the necessary lines in the devcontainer.json file to
   enable X server support.
4. Set the `DISPLAY` environment variable in the devcontainer.json file to `host
   docker.internal:0`.
5. Disable the "mounts" section in the devcontainer.json file.
6. Reopen the project in the devcontainer.
7. Start the configuration server by running the following commands in the
   terminal within the devcontainer:
    ```bash
    mvn package
    java -jar target/configed-<VERSION>-jar-with-dependencies.jar
    ```

These steps ensure that the necessary configurations are in place for running
the project within the Docker container on a Windows environment using
MobaXterm and X server settings.

Note: Make sure to replace <VERSION> with the appropriate version number of
the Configed project.

(Source: https://stackoverflow.com/a/36190462)

## Conclusion

Following the guidelines and using the recommended tools, such as VSCode with
the Remote - Containers extension, will help ensure a smooth and consistent
development experience. The integration with Docker provides an isolated and
pre-configured development environment, eliminating the need for each
contributor to set up their own local environment.

If you have any questions or encounter any issues during the development
process, feel free to reach out to the project maintainers for assistance.

Happy coding!
