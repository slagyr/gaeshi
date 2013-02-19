# gaeshi

## Deprecated

Due to frustrations with Google App Engine, Gaeshi has been deprecated and is no longer maintained.  It's parent library
[Joodo (https://github.com/slagyr/joodo)](https://github.com/slagyr/joodo) is still alive and well.  
It is suggested that you use Joodo on Heroku or AWS rather than deploy anything to GAE.

## Installation

### Leiningen 1

1. Install [Leiningen](https://github.com/technomancy/leiningen) 1.5 or later.
2. Install the `gaeshi/lein-gaeshi` plugin

        lein plugin install gaeshi/lein-gaeshi 0.9.0

3. Make sure `~/.lein/bin` is in your path.

        echo "PATH=$PATH:$HOME/.lein/bin" >> ~/.profile
        . ~/.profile

4. Try it out!

        gaeshi help


### Leiningen 2

1. Install [Leiningen](https://github.com/technomancy/leiningen) 2.0 or later.
2. Install the `gaeshi/lein-gaeshi` plugin by updating your ~/.lein/profiles.clj file.

        {
         :user {:plugins [[gaeshi/lein-gaeshi "0.10.0"]]}
         }

3. Try it out!

        lein gaeshi help


If all goes well you should see a helpful message printed in your console.

## Usage

### Creating a new Project

1. Gaeshi will create a boiler plate project structure for you.

        [lein] gaeshi new my_new_project

2. Download/Install all the dependencies.

        cd my_new_project
        lein deps

3. Start the development server

        lein gaeshi server

## License

Copyright (C) 2011-2012 Micah Martin All Rights Reserved.

Distributed under the The MIT License.
