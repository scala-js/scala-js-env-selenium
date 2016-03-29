import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.core.ProblemFilters._

object BinaryIncompatibilities {
  val SeleniumJSEnv = Seq(
      // Breaking change
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.SeleniumBrowser.setupConsoleCapture"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.BrowserDriver.newConsoleLogsIterator"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Firefox#FirefoxDriver.newConsoleLogsIterator"),
      ProblemFilters.exclude[MissingMethodProblem]("org.scalajs.jsenv.selenium.Chrome#ChromeDriver.newConsoleLogsIterator")
  )
}
