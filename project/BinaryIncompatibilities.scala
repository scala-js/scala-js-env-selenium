import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.core.ProblemFilters._

object BinaryIncompatibilities {
  val SeleniumJSEnv = Seq(
      // Breaking change
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.SeleniumBrowser.setupConsoleCapture"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.BrowserDriver.newConsoleLogsIterator"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Firefox#FirefoxDriver.newConsoleLogsIterator"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Chrome#ChromeDriver.newConsoleLogsIterator"),
      ProblemFilters.exclude[MissingTypesProblem]("org.scalajs.jsenv.selenium.Chrome$"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Chrome.newDriver"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Chrome.initFiles"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Chrome.name"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Chrome#ChromeDriver.this"),
      ProblemFilters.exclude[MissingTypesProblem]("org.scalajs.jsenv.selenium.Firefox$"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Firefox.newDriver"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Firefox.initFiles"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Firefox.name"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Firefox#FirefoxDriver.this")
  )
}
