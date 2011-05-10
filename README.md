# gaeshi

## Installation

1. Install [Leiningen][https://github.com/technomancy/leiningen] 1.5 or later.
2. Install the gaeshi/kuzushi plugin

    lein plugin install gaeshi/kuzushi 0.5.0

3. Make sure `~/.lein/bin` is in your path.

    echo "PATH=$PATH:$HOME/.lein/bin" >> ~/.profile
    . ~/.profile

4. Try it out!

    gaeshi help

If all goes well you should see a helpful message printed in your console.

## Usage

### Creating a new Project

1. Gaeshi will create a boiler plate project structure for you.

    gaeshi new my_new_project

2. Download/Install all the dependencies.

    cd my_new_project
    lein deps

3. Start the development server

    gaeshi server

## License

Copyright (C) 2011 Micah Martin All Rights Reserved.

Distributed under the The MIT License.
