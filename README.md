# scalajs-env-selenium

[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.8.svg)](https://www.scala-js.org/)

## Usage
Simply add the following line to your `project/plugins.sbt`:
```scala
libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "0.1.3"
```
and the following line to your sbt settings:
```scala
// Apply to the 'run' command
jsEnv := new org.scalajs.jsenv.selenium.SeleniumJSEnv(BROWSER)

// Apply to tests
jsEnv in Test := new org.scalajs.jsenv.selenium.SeleniumJSEnv(BROWSER)
```
where the `BROWSER` can be either `org.scalajs.jsenv.selenium.Firefox()` or
`org.scalajs.jsenv.selenium.Chrome()`.

When executing the program with `run` a new browser window will be created,
the code will be executed in it and finally the browser will close itself.
All the console outputs will appear in SBT as usual. Executing `test` will open
several browser windows and close them all before the end of the tests.

### In browser debugging
If you wish to keep the browser window opened after the execution has terminated simply
add the option `withKeepAlive` on the environment (`new SeleniumJSEnv(BROWSER).withKeepAlive()`).
It is recommend to use this with a `run` and not `test` because the latter tends
to leave too many browser windows open.

#### Debugging tests on a single window
By default tests are executed in their own window for parallelism.
When debugging tests with `withKeepAlive` it is possible to disable this option
using the `sbt` setting `parallelExecution in Test := false`.

### Additional requirements for Google Chrome
Selenium requires an additional driver to connect to Google Chrome.
This driver can be found at https://sites.google.com/a/chromium.org/chromedriver/.
You will also need to make sure the driver location is in the systems `PATH` variable.

### Headless Usage
It is often desirable to run Selenium headlessly.
This could be to run tests on a server without graphics, or to just prevent browser
windows popping up when running locally.

##### xvfb
A common approach on Linux and Mac OSX, is to use `xvfb`, "X Virtual FrameBuffer".
It starts an X server headlessly, without the need for a graphics driver.

Once you have `xvfb` installed, usage here with SBT is as simple as:
```sh
Xvfb :1 &
DISPLAY=:1 sbt
```

The `:1` indicates the X display-number, which is a means to uniquely identify an
X server on a host. The `1` is completely arbitrary—you can choose any number so
long as there isn't another X server running that's already associated with it.

## License

`scalajs-env-selenium` is distributed under the
[BSD 3-Clause license](./LICENSE).

## Contributing

Follow the [contributing guide](./CONTRIBUTING.md).
