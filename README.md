# gitolite-webui

A simple webui to manage access to gitolite repositories.

this readme is still in progress

## Usage

Current version is running against clojure 1.5.1 and associated deps, updated as of 2013-06-24

### settings

configuration is stored and loaded in yaml format from `config.yml`

The initial example config by narkisr is in `config-example.yml`. Copy this to `config.yml` in the base directory
and modify it to your liking. The current setup is ready to run, using `test/resources` as the working gitolite-admin
directory.

`gitolite-home` should be set to a working gitolite-admin directory *with trailing slash*

### running from emacs

start an nrepl session and from the `gitolite-webui.core` namespace, eval `(-main "start")`

This will load `config.yml` from the project root, which is also the `:prod` (production) setting. There is also a `:dev` setting



## License

Copyright (C) 2011 narkisr@gmail.com

Distributed under the Eclipse Public License, the same as Clojure.
